package application.server.weapons;

import application.server.Bullet;
import application.server.Hunter;

public class Pistol implements Weapon {
	
	@Override
	public void use(Hunter hunter) {
		Bullet b = new Bullet(hunter.getPosition(), hunter.getProxy().mousePos,
				hunter, hunter.getProxy().id, 3);
		hunter.addGameObject(b);
	}
	
	@Override
	public float getUseCost() {
		return 0.25f;
	}
	
	@Override
	public void swapIn(Hunter h) {
		h.setFracSpeed(1.0f);
	}
	
	@Override
	public Type getType() {
		return Type.PISTOL;
	}
}
