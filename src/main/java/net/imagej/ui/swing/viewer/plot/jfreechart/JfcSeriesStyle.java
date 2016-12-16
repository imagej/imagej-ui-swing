package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.LineStyle;
import net.imagej.plot.MarkerStyle;
import net.imagej.plot.SeriesStyle;

import java.awt.*;

public class JfcSeriesStyle implements SeriesStyle {
	private Color color;
	private MarkerStyle marker_style;
	private LineStyle line_style;

	JfcSeriesStyle() {
		color = null;
		line_style = null;
		marker_style = null;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setLineStyle(LineStyle style) {
		this.line_style = style;
	}

	public void setMarkerStyle(MarkerStyle style) {
		this.marker_style = style;
	}

	public Color getColor() {
		return color;
	}

	public LineStyle getLineStyle() {
		return line_style;
	}

	public MarkerStyle getMarkerStyle() {
		return marker_style;
	}
}
