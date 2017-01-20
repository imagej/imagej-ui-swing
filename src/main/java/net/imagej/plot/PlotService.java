package net.imagej.plot;

import net.imagej.ImageJService;

/**
 * An ImageJService that provides factory methods for supported {@link AbstractPlot}s,
 * e.g. {@link XYPlot} and {@link CategoryChart}.
 *
 * @author Matthias Arzt
 */
public interface PlotService extends ImageJService {

	XYPlot newXYPlot();

	<C> CategoryChart<C> newCategoryChart(Class<C> categoryType);

}
