package net.imagej.plot;

import net.imagej.ImageJService;

/**
 * @author Matthias Arzt
 */
// TODO: consider extending WrapperService, and making JfcPlotGenerator into a WrapperPlugin
public interface PlotService extends ImageJService {

	XYPlot createXYPlot();

	// TODO: BoxPlot createBoxPlot();

	// TODO: CategoryChart createCategoryChart();

}
