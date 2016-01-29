package application.server.weapons;

import application.server.Hunter;

public interface Weapon {
	
	enum Type {
		PISTOL, SHOTGUN, KNIFE, SNIPER, GRENADE;
		
		public int id() {
			return this.ordinal();
		}
		
		public static String[] getNamesFromIds(int[] ids) {
			String[] names = new String[ids.length];
			for (int i = 0; i < names.length; i++) {
				int id = ids[i];
				names[i] = Type.values()[id].name();
			}
			return names;
		}
	}
	
	// Use the current weapon
	public void use(Hunter hunter);
	
	// Get the cost of firing this weapon
	public float getUseCost();
	
	// Applies changes to the hunter when this weapon is swapped in
	public void swapIn(Hunter h);
	
	// Retrieve the type of the weapon
	public Type getType();
}
