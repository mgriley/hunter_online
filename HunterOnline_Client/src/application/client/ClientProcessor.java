package application.client;

import java.io.IOException;
import java.net.DatagramPacket;

import org.msgpack.core.MessageUnpacker;

import application.shared.PacketProcessor;
import application.shared.Packets.FeedMsgPacket;
import application.shared.Packets.Packet;
import application.shared.Packets.StateUpdatePacket;
import application.shared.Packets.WelcomePacket;

public class ClientProcessor implements PacketProcessor {
	
	private PlayState playState;
	
	public ClientProcessor(PlayState state) {
		playState = state;
	}
	
	@Override
	public void process(DatagramPacket packet, Packet.Type type, MessageUnpacker unpacker) throws IOException {
		// System.out.println(type.name() + " packet received."); // TODO dbg
		switch (type) {
			case WELCOME: {
				WelcomePacket wp = WelcomePacket.toProcess();
				boolean success = wp.process(packet, unpacker);
				if (success) {
					playState.setClientID(wp.getClientID());
					playState.setFieldSize(wp.getFieldW(), wp.getFieldH());
					playState.setWeaponNames(wp.getWeaponNames());
					playState.beginPlay(); // start the game
				}
				else {
					// TODO: Resend a Hello packet?
				}
				break;
			}
			case STATEUPDATE: {
				StateUpdatePacket sup = StateUpdatePacket.toProcess();
				boolean success = sup.process(packet, unpacker);
				if (success) {
					playState.queueStateUpdate(sup);
				}
				break;
			}
			case FEEDMSG: {
				System.out.println("Received feed msg");
				FeedMsgPacket fmp = FeedMsgPacket.toProcess();
				boolean success = fmp.process(packet, unpacker);
				if (success) {
					playState.pushText(fmp.isHighPriority(), fmp.getMsg());
				}
				break;
			}
			default:
				System.out.println(type.name() + " is not handled by the client.");
				break;
		}
	}
}
