package application.shared.states;

import java.io.IOException;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import application.shared.Constants;
import application.shared.Vect2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class BulletState implements EntityState {
	
	private Vect2D pos;
	
	public BulletState() {}
	
	public BulletState(Vect2D pos) {
		this.pos = pos;
	}
	
	public void draw(GraphicsContext gc, int clientID) {
		gc.setFill(Color.DARKRED);
		pos.fillCircle(gc, Constants.BULLET_R);
	}
	
	@Override
	public void write(MessageBufferPacker packer) throws IOException {
		packer.packByte(EntityState.Type.BULLET.getID());
		packer.packFloat(pos.x());
		packer.packFloat(pos.y());
	}
	
	@Override
	public void read(MessageUnpacker unpacker) throws IOException {
		float x = unpacker.unpackFloat();
		float y = unpacker.unpackFloat();
		pos = new Vect2D(x, y);
	}
	
	public Vect2D getPos() {
		return pos;
	}
}
