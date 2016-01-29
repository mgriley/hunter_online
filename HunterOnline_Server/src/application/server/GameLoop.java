package application.server;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import application.shared.Timer;

public class GameLoop implements Runnable {
	
	private AtomicBoolean running = new AtomicBoolean(true);
	private Timer updateTimer;
	private Timer secTimer;
	private Consumer<Float> update; // the update function
	
	// For debugging
	public boolean DEBUGGING = false;
	private int updateCount = 0;
	
	public GameLoop(int targetUPS, Consumer<Float> update) {
		this.update = update;
		updateTimer = new Timer(1.0f / targetUPS);
		secTimer = new Timer(1);
	}
	
	@Override
	public void run() {
		
		updateTimer.start();
		secTimer.start();
		
		while (running.get()) {
			
			// Check if the loop should called update
			float deltaT = updateTimer.secsElapsed();
			if (deltaT > updateTimer.secDuration()) {
				updateTimer.start();
				update.accept(deltaT);
				updateCount++;
			}
			
			// Print debugging info every second
			if (DEBUGGING && secTimer.isDone()) {
				secTimer.start();
				int targetUPS = Math.round(1 / updateTimer.secDuration());
				System.out.format("Target UPS: %3d, Achieved UPS: %3d%n", targetUPS, updateCount);
				updateCount = 0;
			}
		}
	}
	
	public void stopRunning() {
		running.set(false);
	}
}