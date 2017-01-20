package net.imagej.defaultplot;

import net.imagej.plot.LineSeries;
import net.imagej.plot.SeriesStyle;

import java.util.Collection;
import java.util.Objects;

/**
 * @author Matthias Arzt
 */
class DefaultLineSeries<C> extends DefaultCategorySeries<C> implements LineSeries<C> {

	private SeriesStyle style = DefaultSeriesStyle.emptySeriesStyle();

	DefaultLineSeries() {
		super();
	}

	public void setStyle(SeriesStyle style) {
		this.style = Objects.requireNonNull(style);
	}

	public SeriesStyle getStyle() {
		return style;
	}

}
