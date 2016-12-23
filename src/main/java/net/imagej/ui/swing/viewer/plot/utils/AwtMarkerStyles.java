package net.imagej.ui.swing.viewer.plot.utils;

import net.imagej.plot.MarkerStyle;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Matthias Arzt
 */

public class AwtMarkerStyles {

	final private boolean visible;

	final private boolean filled;

	final private Shape shape;

	private AwtMarkerStyles(boolean visible, boolean filled, Shape shape) {
		this.visible = visible;
		this.filled = filled;
		this.shape = shape;
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean isFilled() {
		return filled;
	}

	public Shape getShape() {
		return shape;
	}

	public static AwtMarkerStyles getInstance(MarkerStyle style) {
		if(style != null)
			switch (style) {
				case NONE:
					return none;
				case PLUS:
					return plus;
				case X:
					return x;
				case STAR:
					return star;
				case SQUARE:
					return square;
				case FILLEDSQUARE:
					return filledSquare;
				case CIRCLE:
					return circle;
				case FILLEDCIRCLE:
					return filledCircle;
			}
		return square;
	}

	// --- Helper Constants ---

	private static AwtMarkerStyles none = new AwtMarkerStyles(false, false, null);

	private static AwtMarkerStyles plus = new AwtMarkerStyles(true, false, Shapes.plus);

	private static AwtMarkerStyles x = new AwtMarkerStyles(true, false, Shapes.x);

	private static AwtMarkerStyles star = new AwtMarkerStyles(true, false, Shapes.star);

	private static AwtMarkerStyles square = new AwtMarkerStyles(true, false, Shapes.square);

	private static AwtMarkerStyles filledSquare = new AwtMarkerStyles(true, true, Shapes.square);

	private static AwtMarkerStyles circle = new AwtMarkerStyles(true, false, Shapes.circle);

	private static AwtMarkerStyles filledCircle = new AwtMarkerStyles(true, true, Shapes.circle);


	static private class Shapes {

		private static Shape x = getAwtXShape();

		private static Shape plus = getAwtPlusShape();

		private static Shape star = getAwtStarShape();

		private static Shape square = new Rectangle2D.Double(-3.0, -3.0, 6.0, 6.0);

		private static Shape circle = new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0);

		private static Shape getAwtXShape() {
			final Path2D p = new Path2D.Double();
			final double s = 3.0;
			p.moveTo(-s, -s);
			p.lineTo(s, s);
			p.moveTo(s, -s);
			p.lineTo(-s, s);
			return p;
		}

		private static Shape getAwtPlusShape() {
			final Path2D p = new Path2D.Double();
			final double t = 4.0;
			p.moveTo(0, -t);
			p.lineTo(0, t);
			p.moveTo(t, 0);
			p.lineTo(-t, 0);
			return p;
		}

		private static Shape getAwtStarShape() {
			final Path2D p = new Path2D.Double();
			final double s = 3.0;
			p.moveTo(-s, -s);
			p.lineTo(s, s);
			p.moveTo(s, -s);
			p.lineTo(-s, s);
			final double t = 4.0;
			p.moveTo(0, -t);
			p.lineTo(0, t);
			p.moveTo(t, 0);
			p.lineTo(-t, 0);
			return p;
		}

	}

}
