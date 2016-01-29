package application.server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.AbstractMap;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import application.shared.Constants;
import application.shared.Vect2D;
import application.shared.Packets.FeedMsgPacket;
import application.shared.Packets.StateUpdatePacket;

public class Game implements Runnable {
	
	boolean DEBUGGING = true; // TODO
	
	private DatagramSocket socket;
	private AbstractMap<SocketAddress, ClientProxy> proxies;
	
	private int fieldW, fieldH;
	private GameLoop gameLoop;
	private Physics engine;
	private List<GameObject> gameObjects = new ArrayList<>();
	private AbstractQueue<GameObject> objectsToAdd = new ConcurrentLinkedQueue<>();
	
	public Game() {
		fieldW = 600;
		fieldH = 600;
		configurePhysicsEngine();
		gameLoop = new GameLoop(30, this::update);
	}
	
	public void setNetworkRefs(DatagramSocket socket, AbstractMap<SocketAddress, ClientProxy> proxies) {
		this.socket = socket;
		this.proxies = proxies;
	}
	
	@Override
	public void run() {
		gameLoop.run();
	}
	
	// NB: deltaT is in seconds
	private void update(float deltaT) {
		
		// Update each game object
		Iterator<GameObject> it = gameObjects.iterator();
		while (it.hasNext()) {
			GameObject go = it.next();
			if (!go.isPlayActive()) {
				it.remove();
			}
			else {
				go.update(deltaT);
			}
		}
		
		// Check for collisions
		engine.computeCollisions(gameObjects);
		
		// Add any new objects
		while (!objectsToAdd.isEmpty()) {
			GameObject go = objectsToAdd.poll();
			if (go != null) {
				gameObjects.add(go);
			}
		}
		
		// Remove irresponsive players
		Iterator<ClientProxy> cpIt = proxies.values().iterator();
		while (cpIt.hasNext()) {
			ClientProxy p = cpIt.next();
			if (p.secsSinceLastPacket() > Constants.MAX_IRRESPONSE_TIME) {
				Hunter h = p.hunter;
				h.setPlayActive(false);
				cpIt.remove();
				System.out.println("Client " + p.username + " removed for inactivity. Now"
						+ proxies.size() + " clients.");
			}
		}
		
		// Respawn dead players
		cpIt = proxies.values().iterator();
		while (cpIt.hasNext()) {
			ClientProxy p = cpIt.next();
			Hunter h = p.hunter;
			if (!h.isPlayActive() && h.shouldRespawn()) {
				h.setPlayActive(true);
				double randX = 50 + (int) (Math.random() * 300);
				double randY = 50 + (int) (Math.random() * 300);
				h.setPosition(new Vect2D(randX, randY));
				objectsToAdd.add(h);
			}
		}
		
		// Inform each client of the current game state
		// Create a packet containing the game state, and send it to each client
		cpIt = proxies.values().iterator();
		while (cpIt.hasNext()) {
			ClientProxy cp = cpIt.next();
			try {
				// Even when the player's hunter is dead, still send the state updates, so that
				// the player can watch the game while they wait to be respawned
				Hunter h = cp.hunter;
				float globalCooldown = h.getGlobalCooldown();
				int equippedId = h.getEquippedId();
				StateUpdatePacket sup = StateUpdatePacket.toSend(
						h.isPlayActive(),
						gameObjects,
						globalCooldown,
						equippedId);
				sup.send(socket, cp.address);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Hunter addPlayer(ClientProxy proxy, float x, float y) {
		Hunter h = new Hunter(objectsToAdd, proxy, x, y);
		objectsToAdd.add(h);
		return h;
	}
	
	private void configurePhysicsEngine() {
		
		engine = new Physics();
		
		engine.setHandler(PhysicsCategory.BULLET, PhysicsCategory.BULLET, (a, b) -> {
			if (a.getClientID() != b.getClientID()) {
				a.setPlayActive(false);
				b.setPlayActive(false);
			}
		});
		
		engine.setHandler(PhysicsCategory.HUNTER, PhysicsCategory.BULLET, (a, b) -> {
			
			// Don't allow players to kill themselves with their own bullets
			if (a.getClientID() != b.getClientID()) {
				a.setPlayActive(false);
				b.setPlayActive(false);
				
				// TODO: register a listener on the engine, don't put this here
				Hunter h;
				Bullet bullet;
				if (a instanceof Hunter) {
					h = (Hunter) a;
					bullet = (Bullet) b;
				}
				else {
					h = (Hunter) b;
					bullet = (Bullet) a;
				}
				
				// Used for kill tracking
				h.registerKill();
				int killChainCount = h.getKillChainCount();
				
				// TODO: in the evt of a high kill-chain, send a high-priority msg
				
				String killed = h.getProxy().username.toUpperCase();
				String firer = bullet.getFirer().getProxy().username.toUpperCase();
				String msg = firer + " destroyed " + killed + ". Kill Streak: " + killChainCount;
				FeedMsgPacket fmp = FeedMsgPacket.toSend(false, msg);
				for (ClientProxy cp : proxies.values()) {
					try {
						fmp.send(socket, cp.address);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			
		});
	}
	
	public void stopGame() {
		gameLoop.stopRunning();
	}
	
	public int getFieldW() {
		return fieldW;
	}
	
	public int getFieldH() {
		return fieldH;
	}
}
