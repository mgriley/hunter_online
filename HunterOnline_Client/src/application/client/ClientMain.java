package application.client;

import javafx.application.Application;
import javafx.stage.Stage;

public class ClientMain extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			// Set the first state of the application
			State.init(primaryStage, 700, 700);
			MenuState menuState = new MenuState();
			State.transitionState(menuState);
			
			primaryStage.setResizable(true);
			primaryStage.setTitle("Hunter");
			primaryStage.show();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop() {
		State.exit();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
