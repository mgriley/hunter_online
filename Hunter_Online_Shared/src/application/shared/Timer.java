package application.shared;

public class Timer {
	
	private long start;
	private final float duration; // in secs
	
	public Timer(float duration) {
		if (duration == 0) {
			throw new IllegalArgumentException("Duration cannnot be 0");
		}
		this.duration = duration;
		start();
	}
	
	public void start() {
		start = System.currentTimeMillis();
	}
	
	public float secsElapsed() {
		return (System.currentTimeMillis() - start) / 1000.0f;
	}
	
	public float fracElapsed() {
		return secsElapsed() / duration;
	}
	
	public float secDuration() {
		return duration;
	}
	
	public boolean isDone() {
		return (System.currentTimeMillis() - start) / 1000.0f > duration;
	}
}
