package application.server;

import java.util.AbstractQueue;

import application.server.weapons.GrenadeLauncher;
import application.server.weapons.Knife;
import application.server.weapons.Pistol;
import application.server.weapons.Shotgun;
import application.server.weapons.Sniper;
import application.server.weapons.Weapon;
import application.shared.Constants;
import application.shared.Timer;
import application.shared.Vect2D;
import application.shared.states.EntityState;
import application.shared.states.HunterState;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Hunter extends GameObject {
	
	// the # of secs between consecutive kills for them to be considered in a chain
	private static int KILL_CHAIN_SEC = 3;
	private int killChainCount = 0;
	private Timer killChainTimer = new Timer(KILL_CHAIN_SEC);
	
	private AbstractQueue<GameObject> objectsToAdd;
	private ClientProxy lastProxy = new ClientProxy(null, 0, null, null);
	private ClientProxy proxy;
	private float globalCooldown = 1; // 1 for completely cool, 0 for cannot fire anymore
	
	private int weaponIndex = -1; // index of the current weapon in equipped weapons
	private Weapon currentWeapon;
	private Weapon[] equippedWeapons;
	
	// Starts when the player is killed, and once complete the player is set to alive again
	private Timer respawnTimer = new Timer(Constants.RESPAWN_TIME);
	
	public Hunter(AbstractQueue<GameObject> toAdd, ClientProxy proxy, float x, float y) {
		super(proxy.id, PhysicsCategory.HUNTER);
		this.objectsToAdd = toAdd;
		this.proxy = proxy;
		setPosition(x, y);
		setSpeed(Constants.HUNTER_BASE_SPEED);
		setRadius(Constants.HUNTER_R);
		
		// Init the Hunter with the available weapons
		// Note: there is no weapon at key 0
		equippedWeapons = new Weapon[6];
		equippedWeapons[1] = new Pistol();
		equippedWeapons[2] = new Shotgun();
		equippedWeapons[3] = new Sniper();
		equippedWeapons[4] = new Knife();
		equippedWeapons[5] = new GrenadeLauncher();
		equip(1); // inits currentWeapon and weaponIndex
	}
	
	@Override
	public void update(float deltaT) {
		
		// Compare the client proxies
		
		// Compute movement
		Vect2D movement = new Vect2D();
		if (proxy.wDown) {
			movement = movement.add(new Vect2D(0, -1));
		}
		if (proxy.aDown) {
			movement = movement.add(new Vect2D(-1, 0));
		}
		if (proxy.sDown) {
			movement = movement.add(new Vect2D(0, 1));
		}
		if (proxy.dDown) {
			movement = movement.add(new Vect2D(1, 0));
		}
		if (!movement.equals(new Vect2D())) {
			move(deltaT, movement.getAngle());
		}
		
		// Turn to face the mouse
		Vect2D toMouse = proxy.mousePos.subtract(getPosition()); // TODO: something wrong here
		setHeading(toMouse.getAngle());
		
		// Decrease the cooldown
		globalCooldown += deltaT * Constants.COOLDOWN_RATE;
		globalCooldown = Math.min(1, globalCooldown);
		
		// Switch weapons, if necessary
		if (proxy.qDown && !lastProxy.qDown) {
			int newIndex = weaponIndex - 1;
			newIndex = newIndex == 0 ? equippedWeapons.length - 1 : newIndex;
			equip(newIndex);
		}
		else if (proxy.eDown && !lastProxy.eDown) {
			int newIndex = weaponIndex + 1;
			newIndex = newIndex == equippedWeapons.length ? 1 : newIndex;
			equip(newIndex);
		}
		else {
			// Check for direct weapon swaps
			for (int i = 0; i < proxy.digitDown.length; i++) {
				boolean nowDown = proxy.digitDown[i];
				boolean lastDown = lastProxy.digitDown[i];
				if (nowDown && !lastDown) {
					equip(i + 1);
					break;
				}
			}
		}
		
		// Compute weapon use
		if (proxy.mouseDown && !lastProxy.mouseDown) {
			if (globalCooldown - currentWeapon.getUseCost() > 0) {
				globalCooldown -= currentWeapon.getUseCost();
				currentWeapon.use(this);
			}
		}
		
		lastProxy = proxy.getCopy();
	}
	
	private void equip(int id) {
		// Note again: there is no weapon at key 0
		if (0 < id && id < equippedWeapons.length) {
			if (id != weaponIndex) {
				weaponIndex = id;
				currentWeapon = equippedWeapons[weaponIndex];
				currentWeapon.swapIn(this);
				
				// Reset the cooldown so that the player can't use the new weapon immediately
				// ow, weapon swaps could be used to defeat cooldowns
				globalCooldown = 0;
			}
		}
	}
	
	public void registerKill() {
		if (killChainTimer.isDone()) {
			killChainCount = 0;
		}
		killChainCount++;
		killChainTimer.start();
	}
	
	@Override
	public void draw(GraphicsContext gc) {
		gc.setFill(Color.BLACK);
		getPosition().fillCircle(gc, getRadius());
	}
	
	@Override
	public void setPlayActive(boolean active) {
		super.setPlayActive(active);
		if (!active) {
			// In case the respawn time ever becomes less than the Kill-Seq-Min time
			killChainCount = 0;
			// If killed, start the respawn timer
			respawnTimer.start();
		}
	}
	
	public boolean shouldRespawn() {
		if (isPlayActive()) {
			throw new IllegalStateException("This hunter is still active!");
		}
		return respawnTimer.isDone();
	}
	
	public ClientProxy getProxy() {
		return proxy;
	}
	
	public void addGameObject(GameObject go) {
		objectsToAdd.add(go);
	}
	
	public float getGlobalCooldown() {
		return globalCooldown;
	}
	
	public void setFracSpeed(float fracSpeed) {
		setSpeed(Constants.HUNTER_BASE_SPEED * fracSpeed);
	}
	
	public int getEquippedId() {
		return weaponIndex - 1;
	}
	
	public int[] getWeaponIds() {
		// Note: no weapon at index 0
		int[] ids = new int[equippedWeapons.length - 1];
		for (int i = 1; i < equippedWeapons.length; i++) {
			ids[i - 1] = equippedWeapons[i].getType().id();
		}
		return ids;
	}
	
	public int getKillChainCount() {
		return killChainCount;
	}
	
	// TODO: dbg
	@Override
	public String toString() {
		return "Hunter position " + getPosition();
	}
	
	@Override
	public EntityState getState() {
		return new HunterState(proxy.username, proxy.id, getPosition(), getHeading());
	}
}
