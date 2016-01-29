package application.shared;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Constants {
	
	public static final int RESPAWN_TIME = 3;
	public static final int HUNTER_R = 5;
	public static final float HUNTER_BASE_SPEED = 120.0f; // px / s
	public static final float BULLET_BASE_SPEED = 300.0f;
	public static final float BULLET_R = 1.5f;
	
	public static final float COOLDOWN_RATE = 0.75f; // the fraction cooldown that occurs in 1 sec
	
	// Weapon constants
	
	// public static final float PISTOL_USE_COST = 0.25f;
	// public static final float SPEED_WITH_PISTOL = 100.0f;
	// public static final float KNIFE_USE_COST = 0.5f;
	// public static final float SPEED_WITH_KNIFE = 200.0f;
	
	// Networking
	public static final int MAX_IRRESPONSE_TIME = 5; // in seconds
	
	// The address of my computer
	// Private IP: 192.168.2.18 (works for home network)
	// Public IP: 70.48.69.116
	// Linode public IP: 45.79.169.184
	public static SocketAddress SERVER_ADDRESS = new InetSocketAddress("45.79.169.184", 50001);
}
