package application.shared;

import java.io.IOException;
import java.net.DatagramPacket;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;

import application.shared.Packets.Packet;

public class PacketWorker implements Runnable {
	
	private DatagramPacket packet;
	private PacketProcessor processor;
	
	public PacketWorker(DatagramPacket aPacket, PacketProcessor aProcessor) {
		packet = aPacket;
		processor = aProcessor;
	}
	
	@Override
	public void run() {
		MessageUnpacker unpacker = MessagePack.newDefaultUnpacker(packet.getData());
		try {
			int msgType = unpacker.unpackByte();
			if (0 <= msgType && msgType < Packet.Type.values().length) {
				Packet.Type type = Packet.Type.values()[msgType];
				processor.process(packet, type, unpacker);
			}
			else {
				throw new IllegalStateException(msgType + " is not a valid packet type.");
			}
		}
		catch (IOException | IllegalStateException e) {
			e.printStackTrace();
		}
	}
}
