package net.imagej.plot;

import org.scijava.util.Colors;

import java.util.Arrays;
import java.util.List;

/**
 * @author Matthias Arzt
 */
class LineStyleDemo extends ChartDemo {

	public void run() {
		XYPlot plot = plotService.newXYPlot();
		plot.setTitle("Line Styles");
		List<Double> xs = Arrays.asList(0.0,1.0);
		LineStyle[] lineStyles = LineStyle.values();
		for(int i = 0; i < lineStyles.length; i++) {
			double y = i * 1.0;
			XYSeries series = plot.addXYSeries();
			series.setLabel(lineStyles[i].toString());
			series.setValues(xs, Arrays.asList(y,y));
			series.setStyle(plot.newSeriesStyle(Colors.BLACK, lineStyles[i], MarkerStyle.CIRCLE));
		}
		plot.xAxis().setManualRange(-1.0, 2.0);
		plot.yAxis().setManualRange(-1.0, (double) lineStyles.length);

		ui.show(plot);
	}

	public static void main(final String... args) {
		new LineStyleDemo().run();
	}

}
