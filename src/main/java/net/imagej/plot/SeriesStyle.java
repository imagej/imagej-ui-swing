package net.imagej.plot;

import java.awt.Color;

/**
 * @author Matthias Arzt
 */
public interface SeriesStyle {

	Color getColor();

	LineStyle getLineStyle();

	MarkerStyle getMarkerStyle();

}
