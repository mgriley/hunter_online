package application.shared;

import java.io.IOException;
import java.net.DatagramPacket;

import org.msgpack.core.MessageUnpacker;

import application.shared.Packets.Packet;

public interface PacketProcessor {
	public void process(DatagramPacket packet, Packet.Type type, MessageUnpacker unpacker) throws IOException;
}
