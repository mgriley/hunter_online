package application.server.weapons;

import application.server.Bullet;
import application.server.Hunter;
import application.shared.Vect2D;

public class Shotgun implements Weapon {
	
	private static final int NUM_SHOTS = 6;
	
	@Override
	public void use(Hunter hunter) {
		
		// Spawn the arrow just in front of the hunter, where he/she is aiming
		Vect2D source = hunter.getPosition();
		
		// Shoot bullets in a tight burst centered where the player is facing
		float heading = hunter.getHeading();
		float angleSpread = Vect2D.PI() / 4;
		float delta = angleSpread / NUM_SHOTS;
		for (float angle = heading - angleSpread / 2; angle < heading + angleSpread / 2; angle += delta) {
			Vect2D dest = source.withPolarOffset(angle, 3);
			Bullet b = new Bullet(source, dest, hunter, hunter.getProxy().id, 0.3f);
			hunter.addGameObject(b);
		}
	}
	
	@Override
	public float getUseCost() {
		return 0.5f;
	}
	
	@Override
	public void swapIn(Hunter h) {
		h.setFracSpeed(1.0f);
	}
	
	@Override
	public Type getType() {
		return Type.SHOTGUN;
	}
}
