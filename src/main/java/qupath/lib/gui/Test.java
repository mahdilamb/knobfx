package qupath.lib.gui;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

public class Test extends Application {
    @Override
    public void start(Stage stage) {
        final Knob knob = new Knob();
        final Parent root = new Group(knob);
        knob.rotationProperty().addListener((observable, oldValue, newValue) -> System.out.println(newValue.doubleValue()));
        knob.setTickSpacing(30);
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

        final Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
