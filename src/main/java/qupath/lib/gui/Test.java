package qupath.lib.gui;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Test extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        final Knob knob = new Knob();
        final Parent root = new Group(knob);
        knob.rotationProperty().addListener((observable, oldValue, newValue) -> System.out.println(newValue.doubleValue()));
        knob.setValue(90);
        final Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
