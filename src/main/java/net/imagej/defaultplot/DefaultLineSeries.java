package net.imagej.defaultplot;

import net.imagej.plot.LineSeries;
import net.imagej.plot.SeriesStyle;

import java.util.Collection;

/**
 * @author Matthias Arzt
 */
public class DefaultLineSeries extends DefaultCategorySeries implements LineSeries {

	private SeriesStyle style = null;

	DefaultLineSeries(String label, Collection<Double> values) {
		super(label, values);
	}

	public void setStyle(SeriesStyle style) {
		this.style = style;
	}

	public SeriesStyle getStyle() {
		return style;
	}

}
