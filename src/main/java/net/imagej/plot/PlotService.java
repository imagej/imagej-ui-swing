package net.imagej.plot;

import net.imagej.ImageJService;
import net.imagej.ui.swing.viewer.plot.jfreechart.JfcScatterPlot;

// TODO: consider extending WrapperService, and making JfcPlot into a WrapperPlugin
public interface PlotService extends ImageJService {

	ScatterPlot createScatterPlot();

}
