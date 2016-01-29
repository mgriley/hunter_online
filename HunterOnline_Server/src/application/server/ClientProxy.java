package application.server;

import java.net.SocketAddress;

import application.shared.Vect2D;

public class ClientProxy {
	
	public String username; // the user's preferred username
	public int id;
	public SocketAddress address; // store the address from which the Welcome packet was received
	public Hunter hunter;
	
	// The time in millis at which the last packet from this client was received
	public long lastReceived;
	
	public boolean wDown, aDown, sDown, dDown, qDown, eDown, mouseDown;
	public boolean[] digitDown = new boolean[3];
	public Vect2D mousePos;
	
	public ClientProxy(String username, int id, SocketAddress address, Hunter h) {
		this.username = username;
		this.id = id;
		this.address = address;
		this.hunter = h;
		wDown = aDown = sDown = dDown = mouseDown = qDown = eDown = false;
		mousePos = new Vect2D();
	}
	
	public void update(boolean wDown, boolean aDown, boolean sDown, boolean dDown,
			boolean qDown, boolean eDown,
			boolean[] digitPresses, boolean mouseDown, Vect2D mousePos) {
		this.wDown = wDown;
		this.aDown = aDown;
		this.sDown = sDown;
		this.dDown = dDown;
		this.qDown = qDown;
		this.eDown = eDown;
		this.digitDown = digitPresses;
		this.mouseDown = mouseDown;
		this.mousePos = mousePos;
	}
	
	public ClientProxy getCopy() {
		ClientProxy p = new ClientProxy(username, id, address, hunter);
		p.update(wDown, aDown, sDown, dDown, qDown, eDown, digitDown, mouseDown, mousePos);
		return p;
	}
	
	public void resetLastReceivedTime() {
		lastReceived = System.currentTimeMillis();
	}
	
	public float secsSinceLastPacket() {
		return (System.currentTimeMillis() - lastReceived) / 1000.0f;
	}
	
	@Override
	public String toString() {
		return "W: " + wDown + " A: " + aDown + " S: " + sDown + " D: " + dDown + " MouseDown: " +
				mouseDown + " MousePos: " + mousePos;
	}
}
