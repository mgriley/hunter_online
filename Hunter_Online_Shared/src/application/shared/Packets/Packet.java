package application.shared.Packets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

import org.msgpack.core.MessageUnpacker;

/**
 * TODO: move the logic out of the packets and into the processors. Keeping in the packets
 * is confusing, it's hard to find where everything is! Change processAndRespond to just process.
 * 
 * @author matthewriley
 * 		
 */
public interface Packet {
	
	public enum Type {
		HELLO, WELCOME, USERINPUT, STATEUPDATE, LEAVE, FEEDMSG;
		
		public byte getID() {
			return (byte) this.ordinal();
		}
	}
	
	/**
	 * Process the given packet using the given unpacker. When process is called, the packet
	 * header (which contains the header type has already been processed).
	 * 
	 * @param packet
	 * @param unpacker
	 * @return true if the packet was successfully processed, false ow
	 * @throws IOException
	 */
	public boolean process(DatagramPacket packet, MessageUnpacker unpacker);
	
	public void send(DatagramSocket socket, SocketAddress address) throws IOException;
}
