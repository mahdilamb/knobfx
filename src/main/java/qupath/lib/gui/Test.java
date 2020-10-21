package qupath.lib.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Test extends Application {
    @Override
    public void start(Stage stage) {
        final Knob knob = new Knob();
        final BorderPane root = new BorderPane();
        root.setCenter(knob);
        root.setTop(new Slider());
        knob.rotationProperty().addListener((observable, oldValue, newValue) -> System.out.println(newValue.doubleValue()));
        knob.setTickSpacing(30);
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

        final Scene scene = new Scene(root, 500, 400);
        stage.setScene(scene);
        scene.getStylesheets().add(getClass().getResource("/css/dark.css").toExternalForm());

        stage.show();
    }
}
