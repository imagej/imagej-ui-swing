package net.imagej.plot;

import java.util.Arrays;
import java.util.List;

/**
 * @author Matthias Arzt
 */
class MarkerStyleDemo extends ChartDemo {

	public void run() {
		MarkerStyle[] markerStyles = MarkerStyle.values();

		XYPlot plot = plotService.newXYPlot();
		plot.setTitle("Marker Styles");
		plot.xAxis().setManualRange(-1.0, 2.0);
		plot.yAxis().setManualRange(-1.0, (double) markerStyles.length);

		for(int i = 0; i < markerStyles.length; i++)
			addSeries(plot, i, markerStyles[i]);

		ui.show(plot);
	}

	private void addSeries(XYPlot plot, double y, MarkerStyle markerStyle) {
		XYSeries series = plot.addXYSeries();
		series.setLabel(markerStyle.toString());
		series.setValues(Arrays.asList(0.0, 1.0), Arrays.asList(y,y));
		series.setStyle(plot.newSeriesStyle(null, null, markerStyle));
	}

	public static void main(final String... args) {
		new MarkerStyleDemo().run();
	}

}

