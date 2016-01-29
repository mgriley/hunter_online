package application.shared.Packets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import application.shared.Vect2D;

public class UserInputPacket implements Packet {
	
	private boolean[] keyPresses = new boolean[9]; // w, a, s, d, q, e, 1, 2, 3
	private boolean mouseDown;
	private Vect2D mousePos = new Vect2D();
	
	private UserInputPacket() {}
	
	public static UserInputPacket toSend(boolean[] keyPresses,
			boolean mouseDown, Vect2D mousePos) {
		UserInputPacket p = new UserInputPacket();
		p.keyPresses = keyPresses;
		p.mousePos = mousePos;
		p.mouseDown = mouseDown;
		return p;
	}
	
	public static UserInputPacket toProcess() {
		UserInputPacket p = new UserInputPacket();
		return p;
	}
	
	@Override
	public boolean process(DatagramPacket packet, MessageUnpacker unpacker) {
		try {
			keyPresses[0] = unpacker.unpackBoolean();
			keyPresses[1] = unpacker.unpackBoolean();
			keyPresses[2] = unpacker.unpackBoolean();
			keyPresses[3] = unpacker.unpackBoolean();
			keyPresses[4] = unpacker.unpackBoolean();
			keyPresses[5] = unpacker.unpackBoolean();
			keyPresses[6] = unpacker.unpackBoolean();
			keyPresses[7] = unpacker.unpackBoolean();
			keyPresses[8] = unpacker.unpackBoolean();
			mouseDown = unpacker.unpackBoolean();
			float x = unpacker.unpackFloat();
			float y = unpacker.unpackFloat();
			mousePos = new Vect2D(x, y);
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}
	
	@Override
	public void send(DatagramSocket socket, SocketAddress address) throws IOException {
		
		MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
		packer.packByte(Packet.Type.USERINPUT.getID());
		for (boolean b : keyPresses) {
			packer.packBoolean(b);
		}
		packer.packBoolean(mouseDown);
		packer.packFloat(mousePos.x()).packFloat(mousePos.y());
		packer.flush();
		byte[] buf = packer.toByteArray();
		
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address);
		socket.send(packet);
	}
	
	public boolean[] getLetterKeyPresses() {
		boolean[] cut = new boolean[keyPresses.length - 3];
		for (int i = 0; i < cut.length; i++) {
			cut[i] = keyPresses[i];
		}
		return cut;
	}
	
	public boolean[] getDigitKeyPresses() {
		boolean[] numPresses = new boolean[3];
		for (int i = keyPresses.length - 3; i < keyPresses.length; i++) {
			numPresses[i - (keyPresses.length - 3)] = keyPresses[i];
		}
		return numPresses;
	}
	
	public boolean isMouseDown() {
		return mouseDown;
	}
	
	public Vect2D getMousePos() {
		return mousePos;
	}
}
