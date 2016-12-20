
package net.imagej.plot;

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
