package application.client;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import application.shared.Constants;
import application.shared.SocketRunner;
import application.shared.Timer;
import application.shared.Vect2D;
import application.shared.Packets.HelloPacket;
import application.shared.Packets.LeavePacket;
import application.shared.Packets.StateUpdatePacket;
import application.shared.Packets.UserInputPacket;
import application.shared.states.EntityState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Popup;

public class PlayState extends State {
	
	// For networking
	
	private DatagramSocket socket;
	private SocketAddress serverAddress = Constants.SERVER_ADDRESS;
	private int clientID;
	
	// Timer to ensure that the client sends a packet to the server at least every once in a while
	// ow, the server will think that the client disconnected
	private Timer contactServerTimer = new Timer(1);
	
	private boolean receivedNewState = false;
	private StateUpdatePacket statePacket;
	
	private Label basicFeed; // the feed for simple messages
	private Label priorityFeed; // the feed for high-priority messages
	private String basicMsg = "", priorityMsg = "";
	
	private Popup respawnPopup;
	private Canvas canvas;
	private Thread socketRunnerThread;
	private FXLoop loop; // runs on the FX application thread
	private SocketRunner socketRunner; // Runnable: exit before closing
	
	// Input trackers
	private final Map<KeyCode, Boolean> keyMap = new HashMap<>();
	private boolean mousePressed;
	private Vect2D mousePos = new Vect2D();
	private AtomicBoolean mustSendInput = new AtomicBoolean(false);
	
	/**
	 * TODO: scale drawing by the field size
	 */
	private Vect2D fieldSize;
	// TODO: incorporate the scaleFactor into the canvas size
	private float scaleFactor; // scaleFactor = canvasH / fieldH = canvasW / fieldW
	
	private boolean isAlive; // true if my player is alive, false ow
	private int equippedIndex;
	private String[] weaponNames;
	
	public PlayState(String username) {
		
		// Send a packet to the server
		try {
			socket = new DatagramSocket(); // selects a random available port on this address
			ClientProcessor processor = new ClientProcessor(this);
			socketRunner = new SocketRunner(socket, processor, 1);
			socketRunnerThread = new Thread(socketRunner);
			socketRunnerThread.setDaemon(true);
			socketRunnerThread.start();
			
			// Say Hello to the Server
			// TODO: send Hello packets until a) timeout or b) response (Welcome or CannotJoin)
			HelloPacket hp = HelloPacket.toSend(username);
			hp.send(socket, serverAddress);
			
			// Init the game loop
			loop = new FXLoop(this::update);
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// Configure the feeds
		priorityFeed = new Label(priorityMsg);
		priorityFeed.setFont(new Font(15.0f));
		basicFeed = new Label(basicMsg);
		basicFeed.setFont(new Font(10.0f));
		VBox vBox = new VBox();
		VBox.setMargin(priorityFeed, new Insets(5));
		vBox.setAlignment(Pos.TOP_CENTER);
		vBox.getChildren().addAll(priorityFeed, basicFeed);
		getChildren().add(vBox);
		
		// Configure the canvas
		canvas = new Canvas(600, 600);
		getChildren().add(canvas);
		
		// Configure the respawn popup
		// TODO: use css styling, later, and fxml files to define the layout
		respawnPopup = new Popup();
		respawnPopup.hide(); // start hidden
		respawnPopup.setAutoFix(true); // spawn the popup in the window
		BorderPane respawnPane = new BorderPane();
		respawnPane.setPrefHeight(200);
		Label respawnLabel = new Label("Wrecked");
		BorderPane.setAlignment(respawnLabel, Pos.CENTER);
		respawnLabel.setFont(new Font(30));
		Label respawnLabel2 = new Label("respawning momentarily");
		respawnLabel2.setFont(new Font(15));
		BorderPane.setAlignment(respawnLabel2, Pos.CENTER);
		respawnPane.setTop(respawnLabel);
		respawnPane.setBottom(respawnLabel2);
		respawnPopup.getContent().add(respawnPane);
		
		// Configure the game over btn
		Button gameOverBtn = new Button("Game Over");
		gameOverBtn.setOnAction(e -> exitPlayState());
		StackPane.setAlignment(gameOverBtn, Pos.BOTTOM_RIGHT);
		getChildren().add(gameOverBtn);
		
		// Register input listeners
		
		canvas.setOnKeyPressed((evt) -> {
			keyMap.put(evt.getCode(), true);
			mustSendInput.set(true);
		});
		canvas.setOnKeyReleased((evt) -> {
			keyMap.put(evt.getCode(), false);
			mustSendInput.set(true);
		});
		canvas.setOnMousePressed((evt) -> {
			mousePressed = true;
			mustSendInput.set(true);
		});
		canvas.setOnMouseReleased((evt) -> {
			mousePressed = false;
			mustSendInput.set(true);
		});
		canvas.setOnMouseMoved((evt) -> {
			mousePos = new Vect2D((float) evt.getX(), (float) evt.getY());
			mustSendInput.set(true);
		});
		canvas.setOnMouseDragged(canvas.getOnMouseMoved());
	}
	
	private void update(float deltaT) {
		
		// Update the feed msg
		basicFeed.setText(basicMsg);
		priorityFeed.setText(priorityMsg);
		
		// If a new game state was received, redraw the game scene
		// using the most recent state packet
		if (receivedNewState && statePacket != null) {
			receivedNewState = false;
			
			boolean aliveNow = statePacket.isHunterAlive();
			
			// Check if this client is still alive
			if (isAlive && !aliveNow) {
				// Show the respawn popup when this player is killed
				respawnPopup.show(this.getScene().getWindow());
			}
			else if (!isAlive && aliveNow) {
				// Hide the respawn popup
				respawnPopup.hide();
			}
			
			// Reset isAlive
			isAlive = aliveNow;
			
			// Update the currently equipped weapon
			equippedIndex = statePacket.getEquippedId();
			
			// Create a new reference to the packet, so that it isn't overridden
			// if a new state packet arrives
			List<EntityState> states = statePacket.getStates();
			redraw(states);
		}
		
		// Inform the server of the client's inputs, if input changed
		if ((isAlive && mustSendInput.get()) || contactServerTimer.isDone()) {
			
			boolean[] presses = new boolean[9];
			presses[0] = isKeyPressed(KeyCode.W);
			presses[1] = isKeyPressed(KeyCode.A);
			presses[2] = isKeyPressed(KeyCode.S);
			presses[3] = isKeyPressed(KeyCode.D);
			presses[4] = isKeyPressed(KeyCode.Q);
			presses[5] = isKeyPressed(KeyCode.E);
			presses[6] = isKeyPressed(KeyCode.DIGIT1);
			presses[7] = isKeyPressed(KeyCode.DIGIT2);
			presses[8] = isKeyPressed(KeyCode.DIGIT3);
			
			UserInputPacket packet = UserInputPacket.toSend(presses, mousePressed, mousePos);
			try {
				packet.send(socket, serverAddress);
				mustSendInput.set(false);
				contactServerTimer.start();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	// Use the most recent gamestate update to draw the game state
	private void redraw(List<EntityState> statesToDraw) {
		
		if (statesToDraw == null) {
			throw new IllegalStateException("States to draw is null!");
		}
		
		// Prep the canvas
		GraphicsContext gc = canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		gc.setStroke(Color.BLACK);
		gc.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());
		
		// Draw the cooldown timer
		int cooldownR = 20;
		float cooldownXY = 1.5f * cooldownR;
		Vect2D timerCenter = new Vect2D(cooldownXY, cooldownXY);
		float globalCooldown = statePacket.getGlobalCooldown();
		gc.setLineWidth(4);
		gc.setStroke(Color.CRIMSON);
		timerCenter.strokeArc(gc, cooldownR, 0, globalCooldown * 360);
		
		// Draw the names of the weapons
		float initY = cooldownXY + 2 * cooldownR;
		float initX = cooldownXY - cooldownR;
		int ySpacing = 10;
		gc.setFont(new Font(10));
		for (int i = 0; i < weaponNames.length; i++) {
			String name = weaponNames[i].toLowerCase();
			boolean isEquipped = equippedIndex == i;
			gc.setFill(isEquipped ? Color.CRIMSON : Color.SLATEGRAY);
			String txt = String.format("%s%s", isEquipped ? ">" : "", name);
			gc.fillText(txt, initX, initY + ySpacing * i);
		}
		
		// Draw the game entities from their received state
		for (EntityState es : statesToDraw) {
			es.draw(gc, clientID);
		}
		
		// TODO: Too disorienting for a bullet hell game
		// // TODO: Try translating the gc such that the player remains in the middle of the canvas
		// Affine currentTransform = gc.getTransform();
		// Vect2D myPos = statePacket.getMyPosition();
		// double centerX = canvas.getWidth() / 2;
		// double centerY = canvas.getHeight() / 2;
		// gc.translate(centerX - myPos.x(), centerY - myPos.y());
		//
		// gc.setFill(Color.BLACK);
		// Vect2D refLol = new Vect2D(200, 200);
		// refLol.fillCircle(gc, 2);
		// Vect2D ref2 = new Vect2D(300, 100);
		// ref2.fillCircle(gc, 2);
		//
		// // TODO: Restore the original transform
		// gc.setTransform(currentTransform);
	}
	
	public void queueStateUpdate(StateUpdatePacket packet) {
		statePacket = packet;
		receivedNewState = true;
	}
	
	private Boolean isKeyPressed(KeyCode code) {
		Boolean pressed = keyMap.get(code);
		return pressed == null ? false : pressed;
	}
	
	public void setWeaponNames(String[] names) {
		this.weaponNames = names;
		
		// TODO: assume that the first weapon type is the one that starts equipped
		equippedIndex = 0;
	}
	
	public void pushText(boolean highPriority, String newMsg) {
		System.out.println("Text pushed");
		if (highPriority) {
			priorityMsg = newMsg;
		}
		else {
			basicMsg = newMsg;
		}
	}
	
	// Start the game loop
	public void beginPlay() {
		
		resizeCanvas();
		loop.start();
	}
	
	private void resizeCanvas() {
		
		// Get the client's preferred canvas dimensions
		double prefW = canvas.getWidth();
		double prefH = canvas.getHeight();
		
		// Scale the canvas proportionally to the field dimensions
		double fieldRatio = fieldSize.y() / fieldSize.x();
		double canvasIdealW = prefH / fieldRatio;
		double canvasIdealH = prefW * fieldRatio;
		
		// Don't allow the resized canvas dimensions to exceed the preferred canvas dimensions
		// That is, only resize down.
		double canvasW, canvasH;
		if (canvasIdealW <= prefW) {
			canvasW = canvasIdealW;
			canvasH = canvasW * fieldRatio;
		}
		// Otherwise, canvasIdealH is less than prefH
		else {
			canvasH = canvasIdealH;
			canvasW = canvasH / fieldRatio;
		}
		
		// TODO: assert that this = canvasH / fieldSize.y, too
		scaleFactor = (float) canvasW / fieldSize.x();
		
		canvas.setWidth(canvasW);
		canvas.setHeight(canvasH);
	}
	
	@Override
	public void transitionIn() {
		canvas.requestFocus();
	}
	
	// Called once a welcome packet arrives from the server
	public void setClientID(int argID) {
		clientID = argID;
	}
	
	public void exitPlayState() {
		cleanup();
		State.transitionState(new MenuState());
	}
	
	@Override
	public void close() {
		System.out.println("Closing from the Play State");
		cleanup();
	}
	
	private void cleanup() {
		
		// Notify the server that the client is leaving the game
		LeavePacket lp = new LeavePacket();
		try {
			// TODO: send this as a TCP packet?
			lp.send(socket, serverAddress);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		// Stop both threads
		loop.stop();
		socketRunner.stopRunning();
		try {
			socketRunnerThread.join();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Threads successfully closed");
	}
	
	public void setFieldSize(int x, int y) {
		fieldSize = new Vect2D(x, y);
	}
}
