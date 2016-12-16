package net.imagej.plot;

import java.awt.Color;

public interface SeriesStyle {

	void setColor(Color color);

	void setLineStyle(LineStyle style);

	void setMarkerStyle(MarkerStyle style);

	Color getColor();

	LineStyle getLineStyle();

	MarkerStyle getMarkerStyle();
}
