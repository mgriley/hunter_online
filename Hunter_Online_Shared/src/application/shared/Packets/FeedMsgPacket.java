package application.shared.Packets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

public class FeedMsgPacket implements Packet {
	
	private boolean highPriority;
	private String msg;
	
	private FeedMsgPacket() {}
	
	public static FeedMsgPacket toSend(boolean highPriority, String msg) {
		FeedMsgPacket fm = new FeedMsgPacket();
		fm.highPriority = highPriority;
		fm.msg = msg;
		return fm;
	}
	
	public static FeedMsgPacket toProcess() {
		return new FeedMsgPacket();
	}
	
	@Override
	public boolean process(DatagramPacket packet, MessageUnpacker unpacker) {
		try {
			highPriority = unpacker.unpackBoolean();
			msg = unpacker.unpackString();
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
		packer.packByte(Packet.Type.FEEDMSG.getID());
		packer.packBoolean(highPriority);
		packer.packString(msg);
		packer.flush();
		byte[] buf = packer.toByteArray();
		DatagramPacket dp = new DatagramPacket(buf, buf.length, address);
		socket.send(dp);
	}
	
	public boolean isHighPriority() {
		return highPriority;
	}
	
	public String getMsg() {
		return msg;
	}
}
