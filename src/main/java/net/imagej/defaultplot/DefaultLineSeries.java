package net.imagej.defaultplot;

import net.imagej.plot.LineSeries;
import net.imagej.plot.SeriesStyle;

import java.util.Collection;

/**
 * @author Matthias Arzt
 */
class DefaultLineSeries<C> extends DefaultCategorySeries<C> implements LineSeries<C> {

	private SeriesStyle style = null;

	DefaultLineSeries() {
		super();
	}

	public void setStyle(SeriesStyle style) {
		this.style = style;
	}

	public SeriesStyle getStyle() {
		return style;
	}

}
