package net.imagej.plot;

import org.scijava.util.ColorRGB;

/**
 * @author Matthias Arzt
 */
public interface SeriesStyle {

	ColorRGB getColor();

	LineStyle getLineStyle();

	MarkerStyle getMarkerStyle();

}
