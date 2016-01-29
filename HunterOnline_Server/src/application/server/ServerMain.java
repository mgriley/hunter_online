package application.server;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.AbstractMap;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import application.shared.Constants;
import application.shared.SocketRunner;
import application.shared.Packets.FeedMsgPacket;

/**
 * TODO:
 * Make a Bit input and output stream, then transition to fixed-pt floating pt #s, or just
 * shorts! (the screen coordinates likely do not need to be in floats...)
 * 
 * TODO:
 * Print out the packets. Issue may be with msgpack. If so, just use built in Input and Output
 * streams. Don't worry about performance for now.
 * 
 * TODO:
 * Allow the server running to type a high-priority msg into the terminal to send to the players
 * This way, one can alert them before an update by typing:
 * Ex. "~ msg Yo, I'm updating the server now. It will be back up in < 5mins"
 * 
 * @author matthewriley
 * 		
 */
public class ServerMain {
	
	public static int clientIDCount = 0;
	
	public static void main(String[] args) {
		
		System.out.println("Starting server");
		
		SocketRunner sr = null;
		Game game = null;
		Thread srThread = null;
		Thread gameThread = null;
		try {
			// Prep the game
			AbstractMap<SocketAddress, ClientProxy> clientProxies = new ConcurrentHashMap<>(4);
			game = new Game();
			
			// Prep the server to listen
			DatagramSocket socket = new DatagramSocket(Constants.SERVER_ADDRESS);
			ServerProcessor processor = new ServerProcessor(socket, clientProxies, game);
			sr = new SocketRunner(socket, processor, 10);
			game.setNetworkRefs(socket, clientProxies);
			
			// Start the game, and start listening for packets
			srThread = new Thread(sr);
			srThread.start();
			gameThread = new Thread(game);
			gameThread.start();
			
			Scanner sc = new Scanner(System.in);
			String input;
			do {
				input = sc.nextLine();
				
				// Send a high-priority msg to the clients
				FeedMsgPacket fmp = FeedMsgPacket.toSend(true, input);
				for (ClientProxy p : clientProxies.values()) {
					try {
						fmp.send(socket, p.address);
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			} while (!input.equals("quit"));
			sc.close();
		}
		catch (SocketException e1) {
			e1.printStackTrace();
		}
		finally {
			if (sr != null) {
				sr.stopRunning();
			}
			if (game != null) {
				game.stopGame();
			}
			try {
				if (srThread != null) {
					srThread.join();
				}
				if (gameThread != null) {
					gameThread.join();
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Game server killed successfully");
		}
	}
}