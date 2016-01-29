package application.shared.Packets;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

public class WelcomePacket implements Packet {
	
	private int clientID;
	private String[] weaponNames;
	private int fieldW, fieldH; // field dimensions
	
	private WelcomePacket() {}
	
	public static WelcomePacket toProcess() {
		return new WelcomePacket();
	}
	
	public static WelcomePacket toSend(int argID, int fieldW, int fieldH, String[] weaponNames) {
		WelcomePacket p = new WelcomePacket();
		p.clientID = argID;
		p.fieldW = fieldW;
		p.fieldH = fieldH;
		p.weaponNames = weaponNames;
		return p;
	}
	
	public int getClientID() {
		return clientID;
	}
	
	public int getFieldW() {
		return fieldW;
	}
	
	public int getFieldH() {
		return fieldH;
	}
	
	public String[] getWeaponNames() {
		return weaponNames;
	}
	
	@Override
	public boolean process(DatagramPacket packet, MessageUnpacker unpacker) {
		try {
			clientID = unpacker.unpackInt();
			fieldW = unpacker.unpackInt();
			fieldH = unpacker.unpackInt();
			int numWeapons = unpacker.unpackInt();
			weaponNames = new String[numWeapons];
			for (int i = 0; i < numWeapons; i++) {
				weaponNames[i] = unpacker.unpackString();
			}
			unpacker.close();
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}
	
	@Override
	public void send(DatagramSocket socket, SocketAddress address) throws IOException {
		MessageBufferPacker packer = MessagePack.newDefaultBufferPacker();
		packer.packByte(Packet.Type.WELCOME.getID());
		packer.packInt(clientID);
		packer.packInt(fieldW);
		packer.packInt(fieldH);
		packer.packInt(weaponNames.length);
		for (int i = 0; i < weaponNames.length; i++) {
			packer.packString(weaponNames[i]);
		}
		packer.flush();
		byte[] buf = packer.toByteArray();
		DatagramPacket response = new DatagramPacket(buf, buf.length, address);
		socket.send(response);
		packer.close();
	}
}
