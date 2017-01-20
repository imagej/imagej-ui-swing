package net.imagej.defaultplot;

import net.imagej.plot.LineStyle;
import net.imagej.plot.MarkerStyle;
import net.imagej.plot.SeriesStyle;
import org.scijava.util.ColorRGB;

/**
 * @author Matthias Arzt
 */
class DefaultSeriesStyle implements SeriesStyle {

	private final ColorRGB color;

	private final MarkerStyle markerStyle;

	private final LineStyle lineStyle;

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

	private static final DefaultSeriesStyle EMPTY_SERIES_STYLE = new DefaultSeriesStyle(null, null, null);

	public static DefaultSeriesStyle emptySeriesStyle() {
		return EMPTY_SERIES_STYLE;
	}

}
