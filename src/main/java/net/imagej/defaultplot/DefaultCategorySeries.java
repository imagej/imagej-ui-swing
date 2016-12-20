package net.imagej.defaultplot;

import net.imagej.plot.CategorySeries;
import net.imagej.plot.LineSeries;
import net.imagej.plot.SeriesStyle;

import java.util.Collection;

/**
 * @author Matthias Arzt
 */

abstract public class DefaultCategorySeries implements CategorySeries {

	private Collection<Double> values;

	private String label;

	private SeriesStyle style;

	DefaultCategorySeries(String label, Collection<Double> values) {
		this.label = label;
		this.values = values;
		this.style = null;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public void setValues(Collection<Double> Values) {
		this.values = values;
	}

	@Override
	public Collection<Double> getValues() {
		return values;
	}

	@Override
	public void setStyle(SeriesStyle style) {
		this.style = style;
	}

	@Override
	public SeriesStyle getStyle() {
		return style;
	}

}
