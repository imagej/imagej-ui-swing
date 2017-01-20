package net.imagej.plot;

/**
 * Interface that is extended by all data series of {@link XYPlot}.
 *
 * @author Matthias Arzt
 */
public interface XYPlotItem extends Labeled {

	boolean getLegendVisible();

	void setLegendVisible(boolean visible);

}
