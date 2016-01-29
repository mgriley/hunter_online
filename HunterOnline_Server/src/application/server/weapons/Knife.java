package application.server.weapons;

import application.server.BladeBullet;
import application.server.Hunter;
import application.server.PhysicsCategory;

public class Knife implements Weapon {
	
	@Override
	public void use(Hunter hunter) {
		
		// The "knife" will be some bullets spawned directly in front of the player
		BladeBullet bladeBullet = new BladeBullet(hunter, hunter.getClientID(), PhysicsCategory.BULLET);
		hunter.addGameObject(bladeBullet);
	}
	
	@Override
	public float getUseCost() {
		return 0.5f;
	}
	
	@Override
	public void swapIn(Hunter h) {
		h.setFracSpeed(2.0f);
	}
	
	@Override
	public Type getType() {
		return Type.KNIFE;
	}
}
