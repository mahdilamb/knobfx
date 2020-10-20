package qupath.lib.gui;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URISyntaxException;

public class Test extends Application {
    @Override
    public void start(Stage stage) throws URISyntaxException {
        final Knob knob = new Knob();
        final BorderPane root = new BorderPane();
        root.setCenter(knob);
        knob.rotationProperty().addListener((observable, oldValue, newValue) -> System.out.println(newValue.doubleValue()));
        knob.setTickSpacing(30);
        knob.setValueFont(new Font(16));
        knob.setShowValue(true);
        knob.setSnapToTicks(false);
        knob.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SHIFT) {
                knob.setSnapToTicks(true);
                knob.setShowTickMarks(true);

            }

        });
        knob.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.SHIFT) {
                knob.setSnapToTicks(false);
                knob.setShowTickMarks(false);
            }
        });

        final Scene scene = new Scene(root,500,400);
        stage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/css/dark.css").toExternalForm());

        stage.show();
    }
}
