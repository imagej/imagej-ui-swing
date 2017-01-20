package net.imagej.plot;

import org.scijava.util.Colors;

import java.util.Arrays;
import java.util.List;

/**
 * @author Matthias Arzt
 */
class LineStyleDemo extends ChartDemo {


	public void run() {
		LineStyle[] lineStyles = LineStyle.values();

		XYPlot plot = plotService.newXYPlot();
		plot.setTitle("Line Styles");
		plot.xAxis().setManualRange(-1.0, 2.0);
		plot.yAxis().setManualRange(-1.0, (double) lineStyles.length);

		for(int i = 0; i < lineStyles.length; i++)
			addSeries(plot, i, lineStyles[i]);

		ui.show(plot);
	}

	private void addSeries(XYPlot plot, double y, LineStyle lineStyle) {
		XYSeries series = plot.addXYSeries();
		series.setLabel(lineStyle.toString());
		series.setValues(Arrays.asList(0.0,1.0), Arrays.asList(y,y));
		series.setStyle(plot.newSeriesStyle(Colors.BLACK, lineStyle, MarkerStyle.CIRCLE));
	}

	public static void main(final String... args) {
		new LineStyleDemo().run();
	}

}
