package net.imagej.plot;

import java.util.List;

/**
 * A data series of a {@link XYPlot} to be displayed as line or points.
 *
 * @author Matthias Arzt
 */
public interface XYSeries extends XYPlotItem {

	void setValues(List<Double> xValues, List<Double> yValues);

	List<Double> getXValues();

	List<Double> getYValues();

	SeriesStyle getStyle();

	void setStyle(SeriesStyle style);

}
