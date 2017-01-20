package net.imagej.defaultplot;

import net.imagej.plot.LineSeries;
import net.imagej.plot.SeriesStyle;

import java.util.Collection;
import java.util.Objects;

/**
 * The default implementation of {@link LineSeries}.
 *
 * @author Matthias Arzt
 */
class DefaultLineSeries<C> extends DefaultCategorySeries<C> implements LineSeries<C> {

	private SeriesStyle style = DefaultSeriesStyle.emptySeriesStyle();

	DefaultLineSeries() {
		super();
	}

	// -- LineSerties methods --

	@Override
	public void setStyle(SeriesStyle style) {
		this.style = Objects.requireNonNull(style);
	}

	@Override
	public SeriesStyle getStyle() {
		return style;
	}

}
