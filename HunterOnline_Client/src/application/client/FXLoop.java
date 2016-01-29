package application.client;

import java.util.function.Consumer;

import application.shared.Timer;
import javafx.animation.AnimationTimer;

public class FXLoop extends AnimationTimer {
	
	// For debugging
	public boolean DEBUGGING = false;
	private int upsCount = 0;
	private Timer secTimer;
	
	private long lastUpdate; // time since the last update, in nano-secs
	
	// The update function to be called, where the float is the # of secs elapsed since last update
	private Consumer<Float> update;
	
	public FXLoop(Consumer<Float> update) {
		this.update = update;
		secTimer = new Timer(1.0f);
	}
	
	/**
	 * Handle is called at 60fps by default
	 * now is in nanosecs
	 */
	@Override
	public void handle(long now) {
		
		// Find the elapsed time since the last update, in seconds
		float elapsed = (now - lastUpdate) / 1e9f;
		
		update.accept(elapsed);
		lastUpdate = now;
		upsCount++;
		
		if (DEBUGGING && secTimer.isDone()) {
			System.out.println("UPS count: " + upsCount);
			upsCount = 0;
			secTimer.start();
		}
	}
}
