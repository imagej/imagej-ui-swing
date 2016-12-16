package net.imagej.plot;

import net.imagej.ImageJService;

/**
 * @author Matthias Arzt
 */
// TODO: consider extending WrapperService, and making JfcPlot into a WrapperPlugin
public interface PlotService extends ImageJService {

	ScatterPlot createScatterPlot();

	// TODO: BoxPlot createBoxPlot();

	// TODO: CategoryChart createCategoryChart();

}
