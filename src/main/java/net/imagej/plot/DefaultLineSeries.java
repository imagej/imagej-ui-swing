package net.imagej.plot;

import java.util.Collection;

/**
 * @author Matthias Arzt
 */

public class DefaultLineSeries implements LineSeries {

	private Collection<Double> values;

	private String label;

	private SeriesStyle style;

	DefaultLineSeries(String label, Collection<Double> values) {
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
