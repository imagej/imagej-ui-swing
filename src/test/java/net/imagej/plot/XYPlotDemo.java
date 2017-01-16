package net.imagej.plot;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthias Arzt
 */
class XYPlotDemo extends ChartDemo{

	public void run() {
		XYPlot plot = plotService.newXYPlot();
		plot.setTitle("A series forming a circle.");

		List<Double> xs = new ArrayList<>();
		List<Double> ys = new ArrayList<>();
		for(double t = 0; t < 2 * Math.PI; t += 0.1) {
			xs.add(Math.sin(t));
			ys.add(Math.cos(t));
		}

		XYSeries series = plot.addXYSeries();
		series.setLabel("circle");
		series.setValues(xs, ys);

		plot.xAxis().setAutoRange();
		plot.yAxis().setAutoRange();
		plot.setPreferredSize(400, 400);
		ui.show(plot);
	}

	public static void main(final String... args) {
		new XYPlotDemo().run();
	}
}
