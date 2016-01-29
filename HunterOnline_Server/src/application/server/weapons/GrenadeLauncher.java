package application.server.weapons;

import application.server.Grenade;
import application.server.Hunter;
import application.shared.Vect2D;

public class GrenadeLauncher implements Weapon {
	
	private Grenade grenade;
	private static float COST = 0.90f;
	
	@Override
	public void use(Hunter hunter) {
		
		// Fire the grenade
		if (grenade == null || !grenade.isPlayActive()) {
			Vect2D source = hunter.getPosition();
			Vect2D dest = hunter.getProxy().mousePos;
			grenade = new Grenade(hunter, source, dest, hunter.getClientID());
			hunter.addGameObject(grenade);
		}
		else {
			// Explode the grenade
			grenade.explode();
		}
	}
	
	@Override
	public float getUseCost() {
		
		// If this is the first grenade, must fire a new grenade
		if (grenade == null) {
			return COST;
		}
		// If the grenade is active in midair, the next use will explode it with 0 cost
		if (grenade.isPlayActive()) {
			return 0.0f;
		}
		// Grenade has already exploded, so must fire another
		else {
			return COST;
		}
	}
	
	@Override
	public void swapIn(Hunter h) {
		h.setFracSpeed(0.5f);
	}
	
	@Override
	public Type getType() {
		return Type.GRENADE;
	}
	
}
