package net.imagej.defaultplot;

import java.util.Objects;

/**
 * @author Matthias Arzt
 */
class AbstractChartItem extends AbstractLabeled {

	private boolean legendVisible = true;

	public void setLegendVisible(boolean visible) {
		legendVisible = visible;
	}

	public boolean getLegendVisible() {
		return legendVisible;
	}
}
