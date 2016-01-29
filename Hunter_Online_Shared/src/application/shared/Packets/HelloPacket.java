package application.shared.Packets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

public class HelloPacket implements Packet {
	
	private String username;
	
	// Public constructor in this case b/c client and server constructor is the same
	private HelloPacket() {}
	
	public static HelloPacket toSend(String username) {
		HelloPacket p = new HelloPacket();
		p.username = username;
		return p;
	}
	
	public static HelloPacket toProcess() {
		return new HelloPacket();
	}
	
	@Override
	public boolean process(DatagramPacket packet, MessageUnpacker unpacker) {
		// In this case, no need to further unpack the packet
		try {
			username = unpacker.unpackString();
			unpacker.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	@Override
	public void send(DatagramSocket socket, SocketAddress address) throws IOException {
		
		MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
		packer.packByte(Packet.Type.HELLO.getID());
		packer.packString(username);
		packer.flush();
		byte[] buf = packer.toByteArray();
		DatagramPacket dp = new DatagramPacket(buf, buf.length, address);
		socket.send(dp);
		packer.close();
		
		// TODO
		System.out.println("Sent HelloPacket to ADD " + dp.getAddress() + " PORT " + dp.getPort());
	}
	
	// Validate the username -> only return a 3-letter string of letters
	// If the name is invalid, return a default name
	public String getUserName() {
		
		// Check for proper length
		if (username.length() != 3) {
			return "FOO";
		}
		
		// Check for all letters
		char[] arr = username.toCharArray();
		for (int i = 0; i < arr.length; i++) {
			if (!Character.isAlphabetic(arr[i])) {
				return "FOO";
			}
		}
		
		return username;
	}
}