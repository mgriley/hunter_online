package application.client;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public abstract class State extends StackPane {
	
	// Reference to the app's primary stage
	private static Stage primaryStage;
	
	private static State currentState;
	
	/**
	 * Called immediately -after- this state is set as the scene of the primaryStage
	 */
	public abstract void transitionIn();
	
	/**
	 * Called before the application exists
	 */
	public abstract void close();
	
	/**
	 * Call this at the beginning of the application.
	 * 
	 * @param primary
	 */
	public static void init(Stage primary, int startW, int startH) {
		primaryStage = primary;
		
		// Use a dummy scene to prime the width and height of the stage
		primaryStage.setScene(new Scene(new BorderPane(), startW, startH));
	}
	
	public static void transitionState(State newState) {
		
		double w = primaryStage.getScene().getWidth();
		double h = primaryStage.getScene().getHeight();
		primaryStage.setScene(new Scene(newState, w, h));
		
		newState.transitionIn();
		currentState = newState;
	}
	
	public static void exit() {
		if (currentState != null) {
			currentState.close();
		}
	}
}
