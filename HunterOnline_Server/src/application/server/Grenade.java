package application.server;

import application.shared.Vect2D;

public class Grenade extends Bullet {
	
	private static final float MIN_EXPLODE_TIME = 0.5f;
	private static final float GET_SPAN = 1.5f;
	private static final float SPAWN_SPAN = 0.125f;
	private static final int NUM_BULLETS = 15;
	
	public Grenade(Hunter h, Vect2D source, Vect2D destination, int clientID) {
		super(source, destination, h, clientID, GET_SPAN);
		setFracSpeed(0.6f);
	}
	
	@Override
	public void update(float deltaT) {
		move(deltaT);
		if (isLifeDone()) {
			explode();
		}
	}
	
	// True if the explosion was successful, false ow
	public void explode() {
		
		// If the grenade was just fired, prevent it from exploding
		if (secsElapsed() < MIN_EXPLODE_TIME) {
			return;
		}
		
		setPlayActive(false);
		float randStartAngle = (float) Math.random() * Vect2D.PI();
		for (int i = 0; i < NUM_BULLETS; i++) {
			float angle = randStartAngle + i * Vect2D.PI2() / NUM_BULLETS;
			Vect2D dest = getPosition().withPolarOffset(angle, 1);
			Bullet b = new Bullet(getPosition(), dest, getFirer(), getClientID(), SPAWN_SPAN);
			getFirer().addGameObject(b);
		}
	}
}
