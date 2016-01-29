package application.server;

import application.shared.Constants;
import application.shared.Timer;
import application.shared.Vect2D;
import application.shared.states.BulletState;
import application.shared.states.EntityState;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Bullet extends GameObject {
	
	private Hunter firer; // the hunter that fired this bullet
	private Timer lifeSpan;
	private int clientID;
	
	public Bullet(Vect2D source, Vect2D destination, Hunter firer, int clientID, float secLifeSpan) {
		super(clientID, PhysicsCategory.BULLET);
		setSpeed(Constants.BULLET_BASE_SPEED);
		setPosition(source);
		Vect2D offset = destination.subtract(source);
		setHeading(offset.getAngle());
		setRadius(Constants.BULLET_R);
		this.firer = firer;
		this.clientID = clientID;
		lifeSpan = new Timer(secLifeSpan);
	}
	
	@Override
	public void update(float deltaT) {
		move(deltaT);
		
		if (lifeSpan.isDone()) {
			setPlayActive(false);
		}
	}
	
	public boolean isLifeDone() {
		return lifeSpan.isDone();
	}
	
	public float secsElapsed() {
		return lifeSpan.secsElapsed();
	}
	
	@Override
	public void draw(GraphicsContext gc) {
		gc.setFill(Color.BLACK);
		getPosition().fillCircle(gc, getRadius());
	}
	
	@Override
	public EntityState getState() {
		return new BulletState(getPosition());
	}
	
	public Hunter getFirer() {
		return firer;
	}
	
	public int getClientID() {
		return clientID;
	}
	
	public void setFracSpeed(float frac) {
		setSpeed(Constants.BULLET_BASE_SPEED * frac);
	}
}
