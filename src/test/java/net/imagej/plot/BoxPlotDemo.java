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
		randomData1.put("A", collectionOfRandomNumbers(10));
		randomData1.put("B", collectionOfRandomNumbers(20));
		randomData1.put("C", collectionOfRandomNumbers(30));

		BoxSeries<String> boxSeries1 = chart.addBoxSeries();
		boxSeries1.setLabel("boxes1");
		boxSeries1.setValues(randomData1);
		boxSeries1.setColor(Colors.CYAN);

		Map<String, Collection<Double>> randomData2 = new TreeMap<>();
		randomData2.put("A", collectionOfRandomNumbers(10));
		randomData2.put("B", collectionOfRandomNumbers(20));
		randomData2.put("C", collectionOfRandomNumbers(30));

		BoxSeries<String> boxSeries2 = chart.addBoxSeries();
		boxSeries2.setLabel("boxes2");
		boxSeries2.setValues(randomData2);
		boxSeries2.setColor(Colors.BLACK);

		ui.show(chart);
	}

	private static Collection<Double> collectionOfRandomNumbers(int size) {
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
