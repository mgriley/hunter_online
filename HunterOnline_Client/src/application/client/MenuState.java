package application.client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class MenuState extends State {
	
	public MenuState() {
		
		BorderPane bp = new BorderPane();
		
		Label titleLabel = new Label("Hunter");
		titleLabel.setPadding(new Insets(20));
		titleLabel.setFont(Font.font(40.0f));
		BorderPane.setAlignment(titleLabel, Pos.CENTER);
		bp.setTop(titleLabel);
		
		Label nameLabel = new Label("Enter a 3-letter name  then press ENTER");
		TextField nameField = new TextField();
		nameField.setMaxWidth(200);
		nameField.setPromptText("FOO");
		nameField.setOnAction(evt -> {
			// Validate the string
			String s = nameField.getText();
			if (s.length() != 3) {
				nameLabel.setText("The name must be exactly -3- letters long.");
				return;
			}
			char[] arr = s.toCharArray();
			for (int i = 0; i < arr.length; i++) {
				if (!Character.isAlphabetic(arr[i])) {
					nameLabel.setText("Must be exactly 3 -letters-.");
					return;
				}
			}
			
			// If the name is valid, transition to play state
			State.transitionState(new PlayState(s));
		});
		VBox vBox = new VBox();
		vBox.setAlignment(Pos.TOP_CENTER);
		vBox.setSpacing(15);
		vBox.getChildren().addAll(nameLabel, nameField);
		
		bp.setCenter(vBox);
		
		getChildren().add(bp);
	}
	
	@Override
	public void transitionIn() {}
	
	@Override
	public void close() {
		System.out.println("Closing from Menu");
	}
}
