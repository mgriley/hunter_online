package application.client;

import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;

/**
 * The built-in JavaFX canvas does not resize.
 * This canvas resizes when its parent resizes
 *
 * Based On:
 * https://dlemmermann.wordpress.com/2014/04/10/javafx-tip-1-resizable-canvas/
 *
 * For the parent change listener:
 * http://stackoverflow.com/questions/31375422/javafx-detect-if-shape-has-been-assigned-to-a-region-
 * parent-node
 *
 * @author matthewriley
 * 		
 */
public class ResizableCanvas extends Canvas {
	
	// Set to true when the canvas is resized, and the user must set to false
	// once he / she has redrawn the canvas to account for the size change
	private boolean sizeChanged = false;
	
	public ResizableCanvas() {
		
		// Let the canvas manage its own size (allows for resizing)
		setManaged(false);
		
		// When the canvas is assigned a parent Pane, bind its size properties
		// to the pane's size.
		parentProperty().addListener((obs, oldParent, newParent) -> {
			
			if (newParent != null && newParent instanceof Pane) {
				
				// Bind the canvas's dimensions to its parent's dimensions,
				// so that they resize together
				Pane parentPane = (Pane) newParent;
				widthProperty().bind(parentPane.widthProperty());
				heightProperty().bind(parentPane.heightProperty());
			}
		});
		
		// Redraw the canvas when its size changes
		widthProperty().addListener(evt -> sizeChanged = true);
		heightProperty().addListener(evt -> sizeChanged = true);
	}
	
	@Override
	public boolean isResizable() {
		return true;
	}
	
	@Override
	public double prefHeight(double width) {
		return getHeight();
	}
	
	@Override
	public double prefWidth(double height) {
		return getWidth();
	}
	
	public boolean didSizeChange() {
		return sizeChanged;
	}
	
	public void accountedForSizeChange() {
		sizeChanged = true;
	}
}
