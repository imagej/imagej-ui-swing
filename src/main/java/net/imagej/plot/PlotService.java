package net.imagej.plot;

import net.imagej.ImageJService;

/**
 * @author Matthias Arzt
 */
// TODO: consider extending WrapperService, and making JfcPlotGenerator into a WrapperPlugin
public interface PlotService extends ImageJService {

	XYPlot newXYPlot();

	// TODO: BoxPlot createBoxPlot();

	CategoryChart newCategoryChart();

}
