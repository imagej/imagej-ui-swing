package net.imagej.defaultplot;

import net.imagej.plot.LineStyle;
import net.imagej.plot.MarkerStyle;
import net.imagej.plot.SeriesStyle;
import org.scijava.util.ColorRGB;

/**
 * @author Matthias Arzt
 */
class DefaultSeriesStyle implements SeriesStyle {

	private ColorRGB color;

	private MarkerStyle markerStyle;

	private LineStyle lineStyle;

	DefaultSeriesStyle(ColorRGB color, LineStyle lineStyle, MarkerStyle markerStyle) {
		this.color = color;
		this.lineStyle = lineStyle;
		this.markerStyle = markerStyle;
	}

	public ColorRGB getColor() {
		return color;
	}

	public LineStyle getLineStyle() {
		return lineStyle;
	}

	public MarkerStyle getMarkerStyle() {
		return markerStyle;
	}

}
