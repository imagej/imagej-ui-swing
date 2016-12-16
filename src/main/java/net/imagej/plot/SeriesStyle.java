package net.imagej.plot;

import org.jfree.data.xy.XYSeries;

import java.awt.Color;

/**
 * Created by arzt on 14/12/2016.
 */

public class SeriesStyle {
	private Color color;
	private MarkerStyle marker_style;
	private LineStyle line_style;

	SeriesStyle() {
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
