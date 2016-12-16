
package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.PlotService;
import net.imagej.plot.ScatterPlot;
import net.imagej.ui.swing.viewer.plot.jfreechart.JfcScatterPlot;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

// TODO: consider extending WrapperService, and making JfcPlot into a WrapperPlugin
@Plugin(type = Service.class)
public class DefaultPlotService extends AbstractService implements PlotService {

	@Override
	public ScatterPlot createScatterPlot() {
		return new JfcScatterPlot();
	}

}
