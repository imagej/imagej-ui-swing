package net.imagej.defaultplot;

import net.imagej.plot.SeriesStyle;
import net.imagej.plot.XYSeries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Matthias Arzt
 */
class DefaultXYSeries extends AbstractChartItem implements XYSeries {

	private List<Double> xValues = Collections.emptyList();

	private List<Double> yValues = Collections.emptyList();

	private SeriesStyle style = DefaultSeriesStyle.emptySeriesStyle();

	DefaultXYSeries() { }

	@Override
	public void setValues(List<Double> xValues, List<Double> yValues) {
		this.xValues = Collections.unmodifiableList(xValues);
		this.yValues = Collections.unmodifiableList(yValues);
	}

	@Override
	public List<Double> getXValues() {
		return xValues;
	}

	@Override
	public List<Double> getYValues() {
		return yValues;
	}

	@Override
	public SeriesStyle getStyle() {
		return style;
	}

	@Override
	public void setStyle(SeriesStyle style) {
		this.style = style;
	}

}
