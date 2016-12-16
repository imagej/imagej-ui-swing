package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.LineStyle;
import net.imagej.plot.MarkerStyle;
import net.imagej.plot.SeriesStyle;

import java.awt.*;

/**
 * @author Matthias Arzt
 */
public class JfcSeriesStyle implements SeriesStyle {
	private Color color;
	private MarkerStyle markerStyle;
	private LineStyle lineStyle;

	JfcSeriesStyle() {
		color = null;
		lineStyle = null;
		markerStyle = null;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setLineStyle(LineStyle style) {
		this.lineStyle = style;
	}

	public void setMarkerStyle(MarkerStyle style) {
		this.markerStyle = style;
	}

	public Color getColor() {
		return color;
	}

	public LineStyle getLineStyle() {
		return lineStyle;
	}

	public MarkerStyle getMarkerStyle() {
		return markerStyle;
	}
}
