package net.imagej.plot;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matthias Arzt
 */
class LogarithmicAxisDemo extends ChartDemo {

	public void run() {

		XYPlot plot = plotService.newXYPlot();
		plot.setTitle("Logarithmic");
		plot.xAxis().setAutoRange();
		plot.yAxis().setAutoRange();
		plot.yAxis().setLogarithmic(true);

		List<Double> xs = new ArrayList<>();
		List<Double> ys = new ArrayList<>();
		for(double x = 0; x < 10; x += 0.1) {
			xs.add(x);
			ys.add(Math.exp(Math.sin(x)));
		}

		XYSeries series = plot.addXYSeries();
		series.setLabel("exp(sin(x))");
		series.setValues(xs, ys);

		ui.show(plot);
	}

	public static void main(final String... args) {
		new LogarithmicAxisDemo().run();
	}
}
