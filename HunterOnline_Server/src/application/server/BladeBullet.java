package application.server;

import application.shared.Vect2D;

/**
 * TODO: use the Component pattern. This class is almost the exact same as the Bullet class.
 * 
 * @author matthewriley
 */
public class BladeBullet extends Bullet {
	
	private Hunter hunter;
	
	public BladeBullet(Hunter hunter, int clientID, PhysicsCategory category) {
		super(new Vect2D(0, 0), new Vect2D(0, 0), hunter, clientID, 0.5f);
		this.hunter = hunter;
		float r = hunter.getRadius();
		setRadius(r * 2);
		setPosition(hunter.getPosition().withPolarOffset(hunter.getHeading(), r));
	}
	
	@Override
	public void update(float deltaT) {
		
		// Reposition the bullet
		float r = hunter.getRadius();
		setPosition(hunter.getPosition().withPolarOffset(hunter.getHeading(), r));
		
		if (isLifeDone()) {
			setPlayActive(false);
		}
	}
}
