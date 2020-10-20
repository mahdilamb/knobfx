package qupath.lib.gui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom JavaFX knob control themed closely to the JavaFX's Moderna
 * <p>
 */
public class Knob extends Region {
    final Canvas canvas = new Canvas();
    final GraphicsContext gc = canvas.getGraphicsContext2D();
    private final double padding = 2;
    private final DoubleProperty rotationProperty = new SimpleDoubleProperty(0);
    private final double radius;
    private final BooleanProperty snapEnabled = new SimpleBooleanProperty(false);
    private final BooleanProperty tickMarksVisible = new SimpleBooleanProperty(false);

    private final DoubleProperty tickSpacing = new SimpleDoubleProperty(10);
    private final double[] center;
    private final List<double[]> snapMarks = new ArrayList<>();

    public Knob(double diameter) {
        getChildren().add(canvas);
        setFocusTraversable(true);
        setFocused(true);
        this.radius = Math.max(10, diameter * .5);
        center = new double[]{padding + .5 * diameter, padding + .5 * diameter};
        canvas.setWidth(diameter + padding + padding);
        canvas.setHeight(canvas.getWidth());
        disabledProperty().addListener((observable, oldValue, newValue) -> repaint());
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && !isDisabled()) {
                //just adding a thick line - can draw over the current control
                gc.setStroke(Color.web("#039ED3"));
                gc.setLineWidth(2);
                gc.strokeOval(padding, padding, diameter, diameter);
            } else {
                //force repaint
                repaint();
            }
        });

        addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> updateRotationWithMouseEvent(e));
        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case UP -> rotationProperty().set(rotationProperty().get() - (isSnapToTicks() ? tickSpacing.get() : 1));
                case DOWN -> rotationProperty().set(rotationProperty().get() + (isSnapToTicks() ? tickSpacing.get() : 1));

            }
        });
        addEventHandler(ScrollEvent.ANY, e -> rotationProperty().set(rotationProperty().get() + (e.isShiftDown() ? e.getDeltaX() : e.getDeltaY()) * (isSnapToTicks() ? tickSpacing.get() : 1)));


        rotationProperty.addListener((observable, oldValue, newValue) -> checkRotation());
        tickSpacing.addListener((observable, oldValue, newValue) -> updateSnapMarks());
        snapEnabled.addListener((observable, oldValue, newValue) -> repaint());
        tickMarksVisible.addListener((observable, oldValue, newValue) -> repaint());

        updateSnapMarks();
    }

    public Knob() {
        this(100);

    }

    //keep a list of snap mark vectors so they don't need to be recalculated; Always calls repaint
    private void updateSnapMarks() {
        snapMarks.clear();
        final double radians = Math.toRadians(tickSpacing.get());
        for (double i = 0; i < Math.PI * 2; i += radians) {
            snapMarks.add(new double[]{
                    Math.sin(i), -Math.cos(i)
            });
        }

        repaint();
    }

    /**
     * @param e calculate the rotation based on the mouse event
     */
    private void updateRotationWithMouseEvent(MouseEvent e) {
        if (isDisabled()) {
            return;
        }

        final double[] currentVector = normalize(e.getX() - center[0], e.getY() - center[1]);
        final double angle = Math.atan2(currentVector[0], -currentVector[1]);
        rotationProperty.set(Math.toDegrees(angle >= 0 ? angle : Math.PI + Math.PI + angle));// atan2 of dot product and determinant of current vector verses up (0,-1). As x of up vector is 0, can simplify

    }

    /**
     * ensure that the rotation rules are followed (e.g. min, max, snaps, etc.)
     */
    private void checkRotation() {
        if (getValue() < 0) {
            rotationProperty.set(360 + (getValue() % 360));
        }
        if (getValue() > 360) {
            rotationProperty.set(getValue() % 360);
        }
        if (isSnapToTicks()) {

            final double halfSnap = tickSpacing.get() * .5;

            if (getValue() < halfSnap || getValue() > 360 - halfSnap) {
                rotationProperty.set(0);
            } else {

                rotationProperty.set(tickSpacing.get() * (Math.round(getValue() / tickSpacing.get())));
            }
            rotationProperty.set(tickSpacing.get() * (Math.round(getValue() / tickSpacing.get())));

        }


        repaint();
    }

    /**
     * calculate the dot product of two vectors
     *
     * @param a vector a
     * @param b vector b
     * @return dot product of both vectors
     */
    public static double dot(double[] a, double[] b) {
        double product = 0;
        final int n = Math.min(a.length, b.length);
        for (int i = 0; i < n; i++) {
            product = Math.fma(a[i], b[i], product);
        }

        return product;
    }

    /**
     * calculate the length/magnitude of a vector
     *
     * @param vector vector whose magnitude to calculate
     * @return vector length
     */
    public static double length(double... vector) {
        return Math.sqrt(dot(vector, vector));
    }

    /**
     * normalize a vector to a unit vector
     *
     * @param vector input vector
     * @return unit vector
     */
    public static double[] normalize(double... vector) {
        final double[] result = new double[vector.length];
        final double length = length(vector);
        for (int i = 0; i < result.length; i++) {
            result[i] = vector[i] / length;
        }
        return result;
    }

    /**
     * @return the rotation property of this control
     */
    public DoubleProperty rotationProperty() {
        return rotationProperty;
    }

    /**
     * @return the current rotation in degrees
     */
    public double getValue() {
        return rotationProperty.get();
    }

    public void setValue(double rotation) {
        this.rotationProperty.set(rotation);
    }


    /**
     * @return if the rotation is snapped to ticks
     */
    public boolean isSnapToTicks() {
        return snapEnabled.get();
    }

    protected void repaint() {
        final double opacity = isDisabled() ? 0.4 : 1;
        gc.clearRect(0, 0, getWidth(), getHeight());
        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(1, Color.grayRgb(207, opacity)),
                new Stop(0, Color.grayRgb(237, opacity))));
        final double diameter = 2 * radius;
        double positionIndicatorRadius = 5;
        final double positionIndicatorDiameter = 2 * positionIndicatorRadius;
        gc.fillOval(padding, padding, diameter, diameter);
        if (isFocused() && !isDisabled()) {
            gc.setStroke(Color.web("#039ED3", opacity));
            gc.setLineWidth(2);
        } else {
            gc.setStroke(Color.grayRgb(208, opacity));
            gc.setLineWidth(1);

        }

        gc.strokeOval(padding, padding, diameter, diameter);
        final double radians = Math.toRadians(rotationProperty.get());
        final double[] currentVector = new double[]{
                Math.sin(radians),
                -Math.cos(radians)
        };
        final double x = center[0] + (radius - positionIndicatorRadius - 5) * currentVector[0];
        final double y = center[1] + (radius - positionIndicatorRadius - 5) * currentVector[1];
        gc.setLineWidth(1);
        if (tickMarksVisible.get()) {
            gc.setStroke(Color.grayRgb(153, opacity));

            for (final double[] mark : snapMarks) {
                final double x0 = center[0] + (radius - positionIndicatorRadius - 2) * mark[0];
                final double y0 = center[1] + (radius - positionIndicatorRadius - 2) * mark[1];
                final double x1 = center[0] + (radius - positionIndicatorDiameter) * mark[0];
                final double y1 = center[1] + (radius - positionIndicatorDiameter) * mark[1];
                gc.strokeLine(x0, y0, x1, y1);

            }
        }

        gc.setFill(Color.grayRgb(102, opacity));

        gc.fillOval((x - positionIndicatorRadius), (y - positionIndicatorRadius), (positionIndicatorRadius * 2), (positionIndicatorRadius * 2));
    }

    /**
     * set the number of degrees between tick marks
     *
     * @param spacing the degrees between tick marks
     */
    public void setTickSpacing(double spacing) {
        tickSpacing.set(spacing);
    }

    /**
     * set whether to snap to ticks
     *
     * @param enabled whether to snap to ticks
     */
    public void setSnapToTicks(boolean enabled) {
        snapEnabled.set(enabled);
    }

    /**
     * set whether to show tick marks
     *
     * @param visible whether to show tick marks
     */
    public void setShowTickMarks(boolean visible) {
        tickMarksVisible.set(visible);
    }
}
