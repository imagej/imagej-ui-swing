package net.imagej.plot;

import net.imagej.ImageJService;

/**
 * @author Matthias Arzt
 */
public interface PlotService extends ImageJService {

	XYPlot newXYPlot();

	CategoryChart newCategoryChart();

}
