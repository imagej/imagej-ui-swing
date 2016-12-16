
package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.PlotService;
import net.imagej.plot.ScatterPlot;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * @author Matthias Arzt
 */

@Plugin(type = Service.class)
public class DefaultPlotService extends AbstractService implements PlotService {

	@Override
	public ScatterPlot createScatterPlot() {
		return new JfcScatterPlot();
	}

}
