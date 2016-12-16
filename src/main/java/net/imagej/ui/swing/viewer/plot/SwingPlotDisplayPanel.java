/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.ui.swing.viewer.plot;

import java.awt.BorderLayout;
import javax.swing.JPanel;

import net.imagej.ui.swing.viewer.plot.jfreechart.JfcPlot;
import net.imagej.ui.swing.viewer.plot.jfreechart.PlotDisplay;
import net.imagej.ui.swing.viewer.plot.jfreechart.PlotDisplayPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.scijava.ui.viewer.DisplayWindow;

/**
 * A JFreeChart-driven display panel for {@link JfcPlot}s.
 * 
 * @author Curtis Rueden
 */
public class SwingPlotDisplayPanel extends JPanel implements PlotDisplayPanel {

	// -- instance variables --

	private final DisplayWindow window;
	private final PlotDisplay display;

	// -- constructor --

	public SwingPlotDisplayPanel(final PlotDisplay display,
		final DisplayWindow window)
	{
		this.display = display;
		this.window = window;
		setLayout(new BorderLayout());
		JFreeChart chart = makeChart();
		ChartPanel panel = new ChartPanel(chart);
		add(panel);
		window.setContent(this);
	}

	// -- PlotDisplayPanel methods --

	@Override
	public PlotDisplay getDisplay() {
		return display;
	}

	// -- DisplayPanel methods --

	@Override
	public DisplayWindow getWindow() {
		return window;
	}

	@Override
	public void redoLayout() { }

	@Override
	public void setLabel(final String s) { }

	@Override
	public void redraw() { }

	// -- Helper methods --

	private JFreeChart makeChart() {
		final JfcPlot plot = display.get(0);
		return plot.getJFreeChart();
	}

	// 	static private JFreeChart makePieChart(final String chart_title, final Table<?,?> table) {
	// 		final DefaultPieDataset dataset = generatePieDataset(table);
	//		return ChartFactory.createPieChart( chart_title, dataset, true, true, false );
	//	}
	//
	// static private DefaultPieDataset generatePieDataset(Table<?, ?> table) {
	// 	// FIXME frow an exception it table has not the correct format.
	// 	final Column<?> key_column = table.get(0);
	// 	final DoubleColumn value_column = (DoubleColumn) table.get(1);
	// 	final DefaultPieDataset dataset = new DefaultPieDataset();
	// 	Iterator<?> key_iterator = key_column.iterator();
	// 	Iterator<Double> value_iterator = value_column.iterator();
	// 	while(key_iterator.hasNext() && value_iterator.hasNext())
	// 		dataset.setValue(key_iterator.next().toString(), value_iterator.next());
	// 	return dataset;
	// }

	// static private JFreeChart makeScatterChart(final String chart_title, final Table<?,?> table) {
	// 	final XYSeries series = new XYSeries("series");
	// 	DoubleColumn xcol = (DoubleColumn) table.get(0);
	// 	DoubleColumn ycol = (DoubleColumn) table.get(1);
	// 	Iterator<Double> xiter = xcol.iterator();
	// 	Iterator<Double> yiter = ycol.iterator();
	// 	while(xiter.hasNext() && yiter.hasNext()) {
	// 		double x = xiter.next();
	// 		double y = yiter.next();
	// 		series.add(x, y);
	// 	}
	// 	final XYSeriesCollection chartDataset = new XYSeriesCollection( );
	// 	chartDataset.addSeries( series );
	// 	final String xlabel = xcol.getHeader();
	// 	final String ylabel = ycol.getHeader();
	// 	return ChartFactory.createScatterPlot(chart_title, xlabel, ylabel, chartDataset);
	// }

	// static private JFreeChart makeBoxChart(final String chart_title, final Table<?,?> table) {
	// 	final DefaultBoxAndWhiskerCategoryDataset chartDataset = generateBoxplotDataset(table);
	// 	final CategoryAxis xAxis = new CategoryAxis("Type");
	// 	final NumberAxis yAxis = new NumberAxis("Value");
	// 	final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
	// 	renderer.setFillBox(false);
	// 	final CategoryPlot plot = new CategoryPlot(chartDataset, xAxis, yAxis, renderer);
	// 	final Font font = new Font("SansSerif", Font.BOLD, 14);
	// 	return new JFreeChart(chart_title ,font , plot, true );
	// }

	// static private DefaultBoxAndWhiskerCategoryDataset generateBoxplotDataset(Table<?,?> table) {
	// 	DefaultBoxAndWhiskerCategoryDataset chartDataset = new DefaultBoxAndWhiskerCategoryDataset();
	// 	Column<?> key_column = table.get(0);
	// 	for(int i = 1; i < table.size(); i++) {
	// 		DoubleColumn value_column = (DoubleColumn) table.get(i);
	// 		String column_title = value_column.getHeader();
	// 		MyMultiMap<?, Double> map = new MyMultiMap<>(key_column, value_column);
	// 		for (Map.Entry<?, List<Double>> entry : map.entrySet())
	// 			chartDataset.add(entry.getValue(), entry.getKey().toString(), column_title);
	// 	}
	// 	return chartDataset;
	// }

	// static private class MyMultiMap<K,V> {

	// 	private Map<K, List<V>> map;

	// 	MyMultiMap(Collection<K> keys, Collection<V> values) {
	// 		map = new HashMap<>();
	// 		Iterator<K> kIterator = keys.iterator();
	// 		Iterator<V> vIterator = values.iterator();
	// 		while(kIterator.hasNext() && vIterator.hasNext())
	// 			add(kIterator.next(), vIterator.next());
	// 	}

	// 	void add(K k, V v) { get(k).add(v); }

	// 	List<V> get(K k) {
	// 		List<V> list = map.get(k);
	//			if(list == null) {
	//				list = new ArrayList<>();
	//				map.put(k, list);
	//			}
	//			return list;
	//		}
	//
	//		Set<Map.Entry<K, List<V>>> entrySet() { return map.entrySet(); }
	//}
}
