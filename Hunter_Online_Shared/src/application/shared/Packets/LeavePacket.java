package application.shared.Packets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

public class LeavePacket implements Packet {
	
	public LeavePacket() {}
	
	@Override
	public boolean process(DatagramPacket packet, MessageUnpacker unpacker) {
		try {
			unpacker.close();
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public void send(DatagramSocket socket, SocketAddress address) throws IOException {
		MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
		packer.packByte(Packet.Type.LEAVE.getID());
		packer.flush();
		byte[] buf = packer.toByteArray();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address);
		socket.send(packet);
	}
}
