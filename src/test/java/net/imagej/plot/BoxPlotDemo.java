package net.imagej.plot;

import org.scijava.util.Colors;

import java.util.*;

/**
 * @author Matthias Arzt
 */
class BoxPlotDemo extends ChartDemo{

	public void run() {
		CategoryChart<String> chart = plotService.newCategoryChart(String.class);

		Map<String, Collection<Double>> randomData1 = new TreeMap<>();
		randomData1.put("A", randomCollection(10));
		randomData1.put("B", randomCollection(20));
		randomData1.put("C", randomCollection(30));

		BoxSeries<String> random1 = chart.addBoxSeries();
		random1.setLabel("boxes1");
		random1.setValues(randomData1);
		random1.setColor(Colors.CYAN);

		Map<String, Collection<Double>> randomData2 = new TreeMap<>();
		randomData2.put("A", randomCollection(10));
		randomData2.put("B", randomCollection(20));
		randomData2.put("C", randomCollection(30));

		BoxSeries<String> random2 = chart.addBoxSeries();
		random2.setLabel("boxes2");
		random2.setValues(randomData2);
		random2.setColor(Colors.BLACK);

		ui.show(chart);
	}

	private static Collection<Double> randomCollection(int size) {
		Random rand = new Random();
		Vector<Double> result = new Vector<>(size);
		for(int i = 0; i < size; i++)
			result.add(rand.nextGaussian()*20);
		return result;
	}

	public static void main(final String... args) {
		new BoxPlotDemo().run();
	}
}
