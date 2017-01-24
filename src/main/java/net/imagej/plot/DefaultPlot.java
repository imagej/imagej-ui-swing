
package net.imagej.plot;

import net.imagej.table.Table;

/**
 * Default implementation of {@link Plot}.
 *
 * @author Curtis Rueden
 */
public class DefaultPlot implements Plot {

	private final Table<?, ?> data;
	private final PlotStyle style;

	public DefaultPlot(final Table<?, ?> data, final PlotStyle style) {
		this.data = data;
		this.style = style;
	}

	@Override
	public Table<?, ?> getData() {
		return data;
	}

	@Override
	public PlotStyle getStyle() {
		return style;
	}

}
