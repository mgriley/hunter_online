package application.shared;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketRunner implements Runnable {
	
	private AtomicBoolean running = new AtomicBoolean(true);
	private DatagramSocket socket;
	private PacketProcessor processor;
	private int poolSize;
	
	public SocketRunner(DatagramSocket socket, PacketProcessor processor, int poolSize) {
		this.socket = socket;
		this.processor = processor;
		this.poolSize = poolSize;
		
		// Configure the socket
		try {
			socket.setSoTimeout(1000);
		}
		catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
		// For processing received packets
		ExecutorService packetExecutor = Executors.newFixedThreadPool(poolSize);
		
		// Listen for UDP packets
		try {
			byte[] buffer = new byte[750];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			while (running.get()) {
				try {
					// Wait for a packet to arrive
					socket.receive(packet);
					
					// Slay the packet
					packetExecutor.execute(new PacketWorker(packet, processor));
					
					// Create a new buffer and packet for the next packet
					buffer = new byte[500];
					packet = new DatagramPacket(buffer, buffer.length);
				}
				catch (SocketTimeoutException e) {
					// Nothing to do here, loop just continues
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			socket.close();
			try {
				packetExecutor.shutdown();
				boolean finished = packetExecutor.awaitTermination(3, TimeUnit.SECONDS);
				System.out.println("Executor terminated successfully: " + finished);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stopRunning() {
		running.set(false);
	}
}
