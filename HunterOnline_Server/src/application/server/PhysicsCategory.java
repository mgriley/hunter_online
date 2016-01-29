package application.server;

public enum PhysicsCategory {
	HUNTER, BULLET;
	
	private static int[][] idMatrix;
	
	// Create the idMatrix
	static {
		idMatrix = new int[values().length][values().length];
		int idCount = 0;
		for (int i = 0; i < idMatrix.length; i++) {
			for (int j = i; j < idMatrix.length; j++) {
				idMatrix[i][j] = idMatrix[j][i] = idCount++;
			}
		}
	}
	
	/**
	 * Get an ID that is unique to the two given physics category types
	 * 
	 * @param a
	 * @param b
	 * @return
	 * 		an ID that is unique for the collision between the two categories.
	 */
	public static int getCollisionId(PhysicsCategory a, PhysicsCategory b) {
		return idMatrix[a.ordinal()][b.ordinal()];
	}
}
