package net.imagej.plot;

import java.util.*;

/**
 * @author Matthias Arzt
 */
class CategoryChartDemo extends ChartDemo{

	public void run() {

		CategoryChart<String> chart = plotService.newCategoryChart(String.class);
		chart.categoryAxis().setManualCategories(Arrays.asList("one wheel", "bicycle", "car"));

		Map<String, Double> wheelsData = new TreeMap<>();
		wheelsData.put("one wheel", 1.0);
		wheelsData.put("bicycle", 2.0);
		wheelsData.put("car", 4.0);

		LineSeries<String> lineSeries = chart.addLineSeries();
		lineSeries.setLabel("wheels");
		lineSeries.setValues(wheelsData);

		Map<String, Double> speedData = new TreeMap<>();
		speedData.put("one wheel", 10.0);
		speedData.put("bicycle", 30.0);
		speedData.put("car", 200.0);

		BarSeries<String> barSeries = chart.addBarSeries();
		barSeries.setLabel("speed");
		barSeries.setValues(speedData);

		ui.show(chart);
	}

	public static void main(final String... args) {
		new CategoryChartDemo().run();
	}
}
