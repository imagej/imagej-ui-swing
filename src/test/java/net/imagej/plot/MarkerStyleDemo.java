package net.imagej.plot;

import java.util.Arrays;
import java.util.List;

/**
 * @author Matthias Arzt
 */
class MarkerStyleDemo extends ChartDemo {

	public void run() {
		XYPlot plot = plotService.newXYPlot();
		plot.setTitle("Marker Styles");
		List<Double> xs = Arrays.asList(0.0, 1.0);
		MarkerStyle[] markerStyles = MarkerStyle.values();
		for(int i = 0; i < markerStyles.length; i++) {
			double y = i * 1.0;
			XYSeries series = plot.addXYSeries();
			series.setLabel(markerStyles[i].toString());
			series.setValues(xs, Arrays.asList(y,y));
			series.setStyle(plot.newSeriesStyle(null, null, markerStyles[i]));
		}
		plot.xAxis().setManualRange(-1.0, 2.0);
		plot.yAxis().setManualRange(-1.0, (double) markerStyles.length);
		ui.show(plot);
	}

	public static void main(final String... args) {
		new MarkerStyleDemo().run();
	}

}

