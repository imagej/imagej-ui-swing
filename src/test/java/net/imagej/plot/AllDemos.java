package net.imagej.plot;

/**
 * @author Matthias Arzt
 */
public class AllDemos {

	public static void main(final String... args) {
		new BoxPlotDemo().run();
		new CategoryChartDemo().run();
		new LineStyleDemo().run();
		new LogarithmicAxisDemo().run();
		new MarkerStyleDemo().run();
		new SortingCategoriesDemo().run();
		new XYPlotDemo().run();
	}

}
