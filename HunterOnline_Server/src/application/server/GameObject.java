package application.server;

import java.util.concurrent.atomic.AtomicBoolean;

import application.shared.Vect2D;
import application.shared.states.EntityState;
import javafx.scene.canvas.GraphicsContext;

public abstract class GameObject {
	
	// The ID of the client to which this game object is associated
	private int clientID;
	
	// The (x, y) pixel position of the center of the GameObject
	private Vect2D position;
	
	// The speed, in pixels/sec
	private float speed;
	
	// The heading of the object, in radians
	private float heading;
	
	// The radius, in pixels, of the bounding circle of the object
	private float radius;
	
	// True if the GameObject is alive (i.e. should be updated and drawn), false ow
	private AtomicBoolean playActive;
	
	// The physics category from which this hails
	private final PhysicsCategory category;
	
	public GameObject(int clientID, PhysicsCategory category) {
		this.clientID = clientID;
		this.category = category;
		position = new Vect2D();
		speed = 0;
		heading = 0;
		radius = 0;
		playActive = new AtomicBoolean(true);
	}
	
	public GameObject(int clientID, PhysicsCategory category, float posX, float posY) {
		this(clientID, category);
		setPosition(posX, posY);
	}
	
	/**
	 * Update the GameObject's state.
	 * deltaT is in seconds
	 */
	public abstract void update(float deltaT);
	
	/**
	 * Draw the Game object to the given graphics context.
	 * 
	 * @param g2d
	 */
	public abstract void draw(GraphicsContext gc);
	
	/**
	 * @return the state of the entity that is required to model the object.
	 */
	public abstract EntityState getState();
	
	/**
	 * Move the object by its 'speed' pixels in the direction of its heading.
	 * deltaT is in seconds.
	 */
	public void move(float deltaT) {
		move(deltaT, heading);
	}
	
	public void move(float deltaT, float direction) {
		// Compute the velocity in px/sec. Remember, speed is in px/sec.
		Vect2D velocity = Vect2D.getFromPolarCoords(direction, speed * deltaT);
		position = position.add(velocity);
	}
	
	// Getters and setters
	
	public int getClientID() {
		return clientID;
	}
	
	public Vect2D getPosition() {
		return position;
	}
	
	public void setPosition(Vect2D newPos) {
		position = newPos;
	}
	
	public void setPosition(float newX, float newY) {
		position = new Vect2D(newX, newY);
	}
	
	public float getSpeed() {
		return speed;
	}
	
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	public float getHeading() {
		return heading;
	}
	
	public void setHeading(float heading) {
		this.heading = heading;
	}
	
	public float getRadius() {
		return radius;
	}
	
	public PhysicsCategory getCategory() {
		return category;
	}
	
	public void setRadius(float radius) {
		this.radius = radius;
	}
	
	public boolean isPlayActive() {
		return playActive.get();
	}
	
	public void setPlayActive(boolean active) {
		this.playActive.set(active);
	}
}