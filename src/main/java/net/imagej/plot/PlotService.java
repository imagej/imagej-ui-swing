package net.imagej.plot;

import net.imagej.ImageJService;

/**
 * @author Matthias Arzt
 */
public interface PlotService extends ImageJService {

	XYPlot newXYPlot();

	<C> CategoryChart<C> newCategoryChart(Class<C> categoryType);

}
