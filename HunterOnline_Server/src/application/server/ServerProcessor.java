package application.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.AbstractMap;

import org.msgpack.core.MessageUnpacker;

import application.server.weapons.Weapon;
import application.shared.PacketProcessor;
import application.shared.Vect2D;
import application.shared.Packets.HelloPacket;
import application.shared.Packets.LeavePacket;
import application.shared.Packets.Packet;
import application.shared.Packets.UserInputPacket;
import application.shared.Packets.WelcomePacket;

/**
 * TODO:
 * Proxies must be thread-safe. It isn't right now.
 * It would be a lot! safer to a thread per client, then no chance of modifying the same
 * client from two different threads.
 * 
 * @author matthewriley
 * 		
 */
public class ServerProcessor implements PacketProcessor {
	
	private Game game;
	private DatagramSocket socket;
	private AbstractMap<SocketAddress, ClientProxy> proxies;
	
	public ServerProcessor(DatagramSocket socket, AbstractMap<SocketAddress, ClientProxy> proxies, Game game) {
		this.socket = socket;
		this.proxies = proxies;
		this.game = game;
	}
	
	public void process(DatagramPacket packet, Packet.Type type, MessageUnpacker unpacker) throws IOException {
		// System.out.println(type.name() + " packet received!");
		switch (type) {
			case HELLO: {
				HelloPacket hp = HelloPacket.toProcess();
				hp.process(packet, unpacker);
				
				// Add the new client to the client map
				SocketAddress add = packet.getSocketAddress();
				if (!proxies.containsKey(add)) {
					
					// Get an ID and hunter for the new player
					int clientID = ServerMain.clientIDCount++;
					ClientProxy proxy = new ClientProxy(
							hp.getUserName(),
							clientID,
							packet.getSocketAddress(),
							null);
					proxy.resetLastReceivedTime();
					
					// Add a new player to the game simulation, and give the proxy a ref to it
					int randX = 50 + (int) (300 * Math.random());
					int randY = 50 + (int) (300 * Math.random());
					proxy.hunter = game.addPlayer(proxy, randX, randY);
					
					// NB!: don't add the proxy to proxies until it is completely initialized
					proxies.put(add, proxy);
					
					// TODO: keep tabs on the # of players
					System.out.println("Client " + hp.getUserName() + " added. "
							+ "Now " + proxies.size() + " clients.");
							
					// Respond to the new client, informing them of their ID
					String[] weaponNames = Weapon.Type.getNamesFromIds(proxy.hunter.getWeaponIds());
					WelcomePacket wp = WelcomePacket.toSend(clientID, game.getFieldW(),
							game.getFieldH(), weaponNames);
					wp.send(socket, packet.getSocketAddress());
				}
				break;
			}
			case USERINPUT: {
				UserInputPacket uip = UserInputPacket.toProcess();
				boolean success = uip.process(packet, unpacker);
				if (success) {
					// Update the client proxy
					ClientProxy proxy = proxies.get(packet.getSocketAddress());
					if (proxy != null) {
						boolean[] presses = uip.getLetterKeyPresses();
						boolean w = presses[0];
						boolean a = presses[1];
						boolean s = presses[2];
						boolean d = presses[3];
						boolean q = presses[4];
						boolean e = presses[5];
						boolean mouseDown = uip.isMouseDown();
						Vect2D mousePos = uip.getMousePos();
						proxy.update(w, a, s, d, q, e, uip.getDigitKeyPresses(), mouseDown, mousePos);
						proxy.resetLastReceivedTime();
					}
					else {
						System.out.println("No such client at address " + packet.getSocketAddress());
					}
				}
				else {
					System.out.println("User input processing failure");
				}
				break;
			}
			case LEAVE: {
				LeavePacket lp = new LeavePacket();
				boolean success = lp.process(packet, unpacker);
				if (success) {
					// Remove the client from the proxies map
					ClientProxy proxy = proxies.remove(packet.getSocketAddress());
					if (proxy != null) {
						// Deactivate the client's player
						proxy.hunter.setPlayActive(false);
						
						// TODO
						System.out.println("Client " + proxy.username + " left. Now "
								+ proxies.size() + " clients.");
					}
				}
				break;
			}
			default:
				System.out.println(type.name() + " is not handled by the server.");
				break;
		}
	}
}
