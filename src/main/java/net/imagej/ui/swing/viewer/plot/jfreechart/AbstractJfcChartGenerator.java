package net.imagej.ui.swing.viewer.plot.jfreechart;

/**
 * @author Matthias Arzt
 */
abstract class AbstractJfcChartGenerator implements JfcPlotGenerator {

	static class SortedLabelFactory {
		private int n;
		SortedLabelFactory() { n = 0; }
		SortedLabel newLabel(String label) { return new SortedLabel(n++, label); }
	}

	static class SortedLabel implements Comparable<SortedLabel> {
		SortedLabel(final int id, final String label) { this.label = label; this.id = id; }
		@Override public String toString() { return label; }
		@Override public int compareTo(SortedLabel o) { return Integer.compare(id, o.id); }
		private String label;
		private int id;
	}

}
