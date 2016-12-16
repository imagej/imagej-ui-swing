package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.MarkerStyle;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Matthias Arzt
 */

public class JfcMarkerStyles {

	static void modifyRenderer(XYLineAndShapeRenderer renderer, int series, MarkerStyle style) {
		if(style == null)
			return;
		renderer.setSeriesShapesVisible(series, getVisiable(style));
		renderer.setSeriesShapesFilled(series, getFilled(style));
		renderer.setSeriesShape(series, getAwtShape(style));
	}

	// --- Helper Constants ---

	private static Shape none = null;
	private static Shape x = getAwtXShape();
	private static Shape plus = getAwtPlusShape();
	private static Shape star = getAwtStarShape();
	private static Shape square = new Rectangle2D.Double(-3.0, -3.0, 6.0, 6.0);
	private static Shape circle = new Ellipse2D.Double(-3.0,-3.0,6.0,6.0);

	// --- Helper Functions ---

	private static Boolean getFilled(MarkerStyle style) {
		return style == MarkerStyle.FILLEDSQUARE || style == MarkerStyle.FILLEDCIRCLE;
	}

	private static Boolean getVisiable(MarkerStyle style) {
		return style != MarkerStyle.NONE;
	}

	private static Shape getAwtShape(MarkerStyle style) {
		switch (style) {
			case NONE:
				return none;
			case X:
				return x;
			case PLUS:
				return plus;
			case STAR:
				return star;
			case SQUARE:
				return square;
			case FILLEDSQUARE:
				return square;
			case CIRCLE:
				return circle;
			case FILLEDCIRCLE:
				return circle;
		}
		return null;
	}

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
