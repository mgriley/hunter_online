package application.shared;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.ArcType;

/**
 * A 2D vector class containing basic vector manipulations.
 * A 2D point vector is used to represent the positions of all game entities.
 * Contains useful conversions from polar to cartesian coordinates.
 * 
 * Vect2D is immutable, so that vector operations can easily be reasoned about.
 * 
 * @author matthewriley
 * 		
 */
public class Vect2D {
	
	private final float x, y;
	
	public Vect2D() {
		x = 0;
		y = 0;
	}
	
	public Vect2D(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public Vect2D(double x, double y) {
		this.x = (float) x;
		this.y = (float) y;
	}
	
	public Vect2D(Vect2D other) {
		x = other.x;
		y = other.y;
	}
	
	/**
	 * Get a vector representing the cartesian coordinate converted from
	 * the given polar coordinate.
	 * 
	 * @param angle (in radians)
	 * @param radius
	 * @return a point-vector of the corresponding cartesian coordinate
	 */
	public static Vect2D getFromPolarCoords(float angle, float radius) {
		float x = radius * (float) Math.cos(angle);
		float y = radius * (float) Math.sin(angle);
		return new Vect2D(x, y);
	}
	
	/**
	 * Convert the given polar coordinate to cartesian coordinates, with this vector as
	 * the origin of the polar coordinate space.
	 */
	public Vect2D withPolarOffset(float angle, float radius) {
		return Vect2D.getFromPolarCoords(angle, radius).add(this);
	}
	
	/**
	 * Covert this vector to an angle.
	 * 
	 * @return angle, in radians
	 */
	public float getAngle() {
		return (float) Math.atan2(y, x);
	}
	
	public Vect2D add(Vect2D other) {
		return new Vect2D(this.x + other.x, this.y + other.y);
	}
	
	public Vect2D subtract(Vect2D other) {
		return new Vect2D(this.x - other.x, this.y - other.y);
	}
	
	public Vect2D multiply(float factor) {
		return multiply(factor, factor);
	}
	
	public Vect2D multiply(float xfact, float yfact) {
		return new Vect2D(this.x * xfact, this.y * yfact);
	}
	
	public float distance(Vect2D other) {
		return (float) Math.sqrt(distanceSq(other));
	}
	
	public float distanceSq(Vect2D other) {
		float sqX = (float) Math.pow(x - other.x, 2);
		float sqY = (float) Math.pow(y - other.y, 2);
		return sqX + sqY;
	}
	
	public boolean isDistLessThan(Vect2D other, float dist) {
		return distanceSq(other) < dist * dist;
	}
	
	public Vect2D withX(float newX) {
		return new Vect2D(newX, y);
	}
	
	public Vect2D withY(float newY) {
		return new Vect2D(x, newY);
	}
	
	public float magnitude() {
		return (float) Math.sqrt(x * x + y * y);
	}
	
	// Drawing Utilities
	
	public void fillCircle(GraphicsContext gc, float r) {
		gc.fillOval(intX() - r, intY() - r, r * 2, r * 2);
	}
	
	public void strokeArc(GraphicsContext gc, int r, float startAngle, float arcExtent) {
		gc.strokeArc(x - r, y - r, r * 2, r * 2, startAngle, arcExtent, ArcType.OPEN);
	}
	
	public void drawLineTo(GraphicsContext gc, Vect2D other) {
		gc.strokeLine(x, y, other.x, other.y);
	}
	
	// Getters
	
	public float x() {
		return x;
	}
	
	public float y() {
		return y;
	}
	
	public int intX() {
		return Math.round(x);
	}
	
	public int intY() {
		return Math.round(y);
	}
	
	// Utilities
	
	public static float PI() {
		return (float) Math.PI;
	}
	
	public static float PI2() {
		return 2 * PI();
	}
	
	@Override
	public String toString() {
		return String.format("x: %5f y: %5f", x, y);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vect2D other = (Vect2D) obj;
		if (Math.abs(x - other.x) > 0.001)
			return false;
		if (Math.abs(y - other.y) > 0.001)
			return false;
		return true;
	}
}
