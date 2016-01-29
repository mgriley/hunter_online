package application.server.weapons;

import application.server.Bullet;
import application.server.Hunter;
import application.shared.Vect2D;

public class Sniper implements Weapon {
	
	@Override
	public void use(Hunter hunter) {
		
		// Spawn the arrow just in front of the hunter, where he/she is aiming
		Vect2D source = hunter.getPosition();
		
		Bullet b = new Bullet(source, hunter.getProxy().mousePos, hunter, hunter.getProxy().id, 3);
		b.setFracSpeed(2.0f);
		hunter.addGameObject(b);
	}
	
	@Override
	public float getUseCost() {
		return 0.75f;
	}
	
	@Override
	public void swapIn(Hunter h) {
		h.setFracSpeed(0.65f);
	}
	
	@Override
	public Type getType() {
		return Type.SNIPER;
	}
}
