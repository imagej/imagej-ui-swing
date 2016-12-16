package net.imagej.plot;

import java.util.Collection;

/**
 * @author
 */
public interface XYSeries {
	String getLabel();

	void setLabel(String label);

	Collection<Double> getXValues();

	void setXValues(Collection<Double> xValues);

	Collection<Double> getYValues();

	void setYValues(Collection<Double> yValues);

	SeriesStyle getStyle();

	void setStyle(SeriesStyle style);

}
