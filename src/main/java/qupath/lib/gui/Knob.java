package qupath.lib.gui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.List;

/**
 * A custom JavaFX knob control themed closely to the JavaFX's Moderna
 * <p>
 * Knob supports min and max range as well as snap to ticks. Note that if the min is set to 0 and max is set to 360, then knob is bounded at either end (unlike min and max both at 0)
 */
public class Knob extends Canvas {

    final GraphicsContext gc = getGraphicsContext2D();
    private final double padding = 2;
    private final DoubleProperty rotationProperty = new SimpleDoubleProperty(0);
    private final double radius;
    private final DoubleProperty snapTo = new SimpleDoubleProperty(0);
    private DoubleProperty min = new SimpleDoubleProperty(0);
    private DoubleProperty max = new SimpleDoubleProperty(360);
    private final double[] center;
    private final double[] currentVector = new double[]{0, -1};
    private final List<double[]> snapMarks = new ArrayList<>();

    public Knob(double diameter) {
        this.radius = Math.max(10, diameter * .5);
        center = new double[]{padding + .5 * diameter, padding + .5 * diameter};
        setWidth(diameter + padding + padding);
        setHeight(getWidth());
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

        setOnMousePressed(e -> {
            final double[] vector = new double[]{e.getX() - center[0], e.getY() - center[1]};
            if (!focusedProperty().get() && dot(vector, vector) <= radius * radius) {
                setFocused(true);
            }
        });

        setOnMouseReleased(e -> {
            final double[] vector = new double[]{e.getX() - center[0], e.getY() - center[1]};
            if (focusedProperty().get() && dot(vector, vector) > radius * radius) {
                setFocused(false);
            }

        });
        setOnMouseDragged(e -> updateRotationWithMouseEvent(e));
        setOnScroll(e -> rotationProperty().set(rotationProperty().get() + e.getDeltaY() * (isSnapToTicks() ? snapTo.get() : 1)));
        rotationProperty.addListener((observable, oldValue, newValue) -> checkRotation(newValue.doubleValue()));
        snapTo.addListener((observable, oldValue, newValue) -> updateSnapMarks());
        updateSnapMarks();
    }

    public Knob() {
        this(100);

    }

    //keep a list of snap mark vectors so they don't need to be recalculated; Always calls repaint
    private void updateSnapMarks() {
        snapMarks.clear();
        if (isSnapToTicks()) {
            for (double i = 0; i < Math.PI * 2; i += snapTo.get()) {
                snapMarks.add(new double[]{
                        Math.sin(i), -Math.cos(i)
                });
            }
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

        System.arraycopy(normalize(e.getX() - center[0], e.getY() - center[1]), 0, currentVector, 0, 2);
        final double angle = Math.atan2(currentVector[0], -currentVector[1]);
        rotationProperty.set(Math.toDegrees(angle >= 0 ? angle : Math.PI + Math.PI + angle));// atan2 of dot product and determinant of current vector verses up (0,-1). As x of up vector is 0, can simplify

    }

    /**
     * ensure that the rotation rules are followed (e.g. min, max, snaps, etc.)
     */
    private void checkRotation(double rotation) {
        if (snapTo.get() > 0) {
            final double halfSnap = snapTo.get() * .5;
            final double a = Math.abs(rotation);
            if (a < halfSnap) {
                currentVector[0] = 0;
                currentVector[1] = -1;
                rotationProperty.set(0);

            } else if (a > 180 - halfSnap) {

                currentVector[0] = 0;
                currentVector[1] = 1;
                rotationProperty.set(180);
            } else {
                currentVector[0] = Math.sin(rotation);
                currentVector[1] = -Math.cos(rotation);
                rotationProperty.set(snapTo.get() * (Math.round(rotation / snapTo.get())));
            }

        } else {
            if (max.doubleValue() != min.doubleValue()) {
                if (getValue() < min.get()) {
                    rotationProperty.set(min.get());
                }
                if (getValue() > max.get()) {
                    rotationProperty.set(max.get());
                }
            } else {
                currentVector[0] = Math.sin(rotation);
                currentVector[1] = -Math.cos(rotation);
                rotationProperty.set(rotation);
            }
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
    public final static double dot(double[] a, double[] b) {
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
        return snapTo.get() > 0;
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

        final double x = center[0] + (radius - positionIndicatorRadius - 5) * currentVector[0];
        final double y = center[1] + (radius - positionIndicatorRadius - 5) * currentVector[1];
        gc.setLineWidth(1);

        gc.setStroke(Color.grayRgb(208, opacity));
        gc.strokeOval((padding + positionIndicatorRadius + 5), (padding + positionIndicatorRadius + 5), (diameter - positionIndicatorDiameter - 10), (diameter - positionIndicatorDiameter - 10));

        for (final double[] mark : snapMarks) {
            final double x0 = center[0] + (radius - positionIndicatorRadius - 2) * mark[0];
            final double y0 = center[1] + (radius - positionIndicatorRadius - 2) * mark[1];
            final double x1 = center[0] + (radius - positionIndicatorDiameter) * mark[0];
            final double y1 = center[1] + (radius - positionIndicatorDiameter) * mark[1];
            gc.strokeLine(x0, y0, x1, y1);

        }
        gc.setFill(Color.grayRgb(102, opacity));

        gc.fillOval((x - positionIndicatorRadius), (y - positionIndicatorRadius), (positionIndicatorRadius * 2), (positionIndicatorRadius * 2));
    }
}
