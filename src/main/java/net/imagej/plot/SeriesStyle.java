package net.imagej.plot;

import org.scijava.util.ColorRGB;

/**
 * An interface to describe the style of a line with markers.
 * {@link CategoryChart} and {@link XYPlot} provide factory methods to create such object.
 *
 * @author Matthias Arzt
 */
public interface SeriesStyle {

	ColorRGB getColor();

	LineStyle getLineStyle();

	MarkerStyle getMarkerStyle();

}
