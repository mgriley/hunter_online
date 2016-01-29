package application.shared.states;

import java.io.IOException;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessageUnpacker;

import javafx.scene.canvas.GraphicsContext;

public interface EntityState {
	
	public enum Type {
		HUNTER, BULLET;
		public byte getID() {
			return (byte) this.ordinal();
		}
	}
	
	/**
	 * Writes this the state of this entity to the given packer. The first piece of
	 * state that must be written is the ID of the Type of the Entity.
	 * 
	 * @param packer
	 * @throws IOException
	 */
	public void write(MessageBufferPacker packer) throws IOException;
	
	/**
	 * 
	 * @param unpacker. Note that read should be called -after- the ID of the state is
	 *            unpacked
	 * @throws IOException
	 */
	public void read(MessageUnpacker unpacker) throws IOException;
	
	/**
	 * Use the entity's state to draw the entity
	 * 
	 * @param gc
	 */
	public void draw(GraphicsContext gc, int myID);
}
