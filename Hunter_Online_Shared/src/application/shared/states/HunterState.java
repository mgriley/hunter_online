package application.shared.states;

import java.io.IOException;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import application.shared.Constants;
import application.shared.Vect2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class HunterState implements EntityState {
	
	private static Color[] playerColors = {
			Color.DODGERBLUE,
			Color.FORESTGREEN,
			Color.DARKORANGE,
			Color.MEDIUMPURPLE,
			Color.DEEPPINK,
			Color.CHARTREUSE,
			Color.DARKSALMON
	};
	
	private String username;
	private int clientID;
	private Vect2D pos;
	private float aimHeading;
	
	public HunterState() {}
	
	public HunterState(String username, int clientID, Vect2D pos, float aimHeading) {
		this.username = username;
		this.clientID = clientID;
		this.pos = pos;
		this.aimHeading = aimHeading;
	}
	
	public void draw(GraphicsContext gc, int myID) {
		
		// Draw the player's username above his / her head
		// Don't draw my own name though b/c it obscures the aimer
		if (myID != clientID) {
			gc.setFill(Color.LIGHTGRAY);
			gc.setTextAlign(TextAlignment.CENTER);
			gc.setFont(new Font(10));
			gc.fillText(username.toUpperCase(), pos.x(), pos.y() - Constants.HUNTER_R - 15);
			gc.setTextAlign(TextAlignment.LEFT);
		}
		
		// Draw an aiming line
		Vect2D aim = pos.withPolarOffset(aimHeading, 15);
		gc.setLineWidth(1.5f);
		gc.setStroke(Color.SLATEGRAY);
		pos.drawLineTo(gc, aim);
		
		// Draw the player
		// gc.setFill(clientID == myID ? Color.MEDIUMBLUE : Color.CRIMSON);
		int i = clientID % playerColors.length;
		gc.setFill(playerColors[i]);
		pos.fillCircle(gc, Constants.HUNTER_R);
	}
	
	@Override
	public void write(MessageBufferPacker packer) throws IOException {
		packer.packByte(EntityState.Type.HUNTER.getID());
		packer.packString(username);
		packer.packInt(clientID);
		packer.packFloat(pos.x());
		packer.packFloat(pos.y());
		packer.packFloat(aimHeading);
	}
	
	@Override
	public void read(MessageUnpacker unpacker) throws IOException {
		username = unpacker.unpackString();
		clientID = unpacker.unpackInt();
		float x = unpacker.unpackFloat();
		float y = unpacker.unpackFloat();
		pos = new Vect2D(x, y);
		aimHeading = unpacker.unpackFloat();
	}
	
	public String getUserName() {
		return username;
	}
	
	public int getID() {
		return clientID;
	}
	
	public Vect2D getPos() {
		return pos;
	}
	
	public float getHeading() {
		return aimHeading;
	}
}
