
package net.imagej.defaultplot;

import net.imagej.plot.CategoryChart;
import net.imagej.plot.PlotService;
import net.imagej.plot.XYPlot;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * @author Matthias Arzt
 */

@Plugin(type = Service.class)
public class DefaultPlotService extends AbstractService implements PlotService {

	@Override
	public XYPlot createXYPlot() {
		return new DefaultXYPlot();
	}

	@Override
	public CategoryChart createCategoryChart() {
		return new DefaultCategoryChart();
	}

}
