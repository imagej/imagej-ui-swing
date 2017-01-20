package net.imagej.defaultplot;

import java.util.Objects;

/**
 * An abstract class with default behavior for {@link CategoryCharItem} and {XYPlotItem} interfaces.
 *
 * @author Matthias Arzt
 */
abstract class AbstractChartItem extends AbstractLabeled {

	private boolean legendVisible = true;

	public void setLegendVisible(boolean visible) {
		legendVisible = visible;
	}

	public boolean getLegendVisible() {
		return legendVisible;
	}
}
