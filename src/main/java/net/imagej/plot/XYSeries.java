package net.imagej.plot;

import java.util.List;

/**
 * @author
 */
public interface XYSeries extends XYPlotItem {

	void setValues(List<Double> xValues, List<Double> yValues);

	List<Double> getXValues();
	List<Double> getYValues();

	SeriesStyle getStyle();

	void setStyle(SeriesStyle style);

}
