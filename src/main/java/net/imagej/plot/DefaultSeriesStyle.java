package net.imagej.plot;

import java.awt.*;

/**
 * @author Matthias Arzt
 */
class DefaultSeriesStyle implements SeriesStyle {

	private Color color;

	private MarkerStyle markerStyle;

	private LineStyle lineStyle;

	DefaultSeriesStyle(Color color, LineStyle lineStyle, MarkerStyle markerStyle) {
		this.color = color;
		this.lineStyle = lineStyle;
		this.markerStyle = markerStyle;
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
