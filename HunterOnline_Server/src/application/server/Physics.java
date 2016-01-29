package application.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class Physics {
	
	private Map<Integer, BiConsumer<GameObject, GameObject>> handlerMap;
	
	public Physics() {
		handlerMap = new HashMap<>();
	}
	
	public void setHandler(PhysicsCategory cA, PhysicsCategory cB,
			BiConsumer<GameObject, GameObject> reaction) {
		handlerMap.put(PhysicsCategory.getCollisionId(cA, cB), reaction);
	}
	
	public void computeCollisions(List<GameObject> bodies) {
		
		// Process all game object pairs
		GameObject a, b;
		for (int i = 0; i < bodies.size(); i++) {
			a = bodies.get(i);
			for (int j = i + 1; j < bodies.size(); j++) {
				b = bodies.get(j);
				
				int collisionID = PhysicsCategory.getCollisionId(a.getCategory(), b.getCategory());
				BiConsumer<GameObject, GameObject> handler = handlerMap.get(collisionID);
				if (handler != null) {
					if (isIntersecting(a, b)) {
						handler.accept(a, b);
					}
				}
			}
		}
	}
	
	/**
	 * True if the bounding circle of this object is intersecting with the
	 * bounding circle of the other object, false ow.
	 * 
	 * @param other
	 * @return
	 */
	public static boolean isIntersecting(GameObject a, GameObject b) {
		float radSumSq = (float) Math.pow(a.getRadius() + b.getRadius(), 2);
		return a.getPosition().distanceSq(b.getPosition()) < radSumSq;
	}
}
