package net.imagej.plot;

import net.imagej.table.DoubleColumn;
import net.imagej.table.GenericColumn;
import net.imagej.table.GenericTable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.Iterator;

import static com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table;

/**
 * Created by arzt on 13/12/2016.
 */
// FIXME make ScatterPlot an interface and implement the JFreeChart in JFreeChartScatterPlot
public class ScatterPlot implements Plot{

	private String chart_title;
	private GenericTable table;

	//public addSeries(x_list, y_list, color, linestyle, markerstyle, title);
	//public xAxis(min, max, steps, substeps, logarithmic, fixedrange/autorange);
	//public yAxis(min, max, steps, substeps, logarithmic, fixedrange/autorange);

	@Override
	public void setTitle(String title) {
		this.chart_title = title;
	}

	// FIXME implement an appropriate interface do set up chart data
	void setData(GenericTable data) {
		this.table = data;
	}

	@Override
	public JFreeChart getJFreeChart() {
		final XYSeries series = new XYSeries("series");
		DoubleColumn xcol = (DoubleColumn) table.get(0);
		DoubleColumn ycol = (DoubleColumn) table.get(1);
		Iterator<Double> xiter = xcol.iterator();
		Iterator<Double> yiter = ycol.iterator();
		while(xiter.hasNext() && yiter.hasNext()) {
			double x = xiter.next();
			double y = yiter.next();
			series.add(x, y);
		}
		final XYSeriesCollection chartDataset = new XYSeriesCollection( );
		chartDataset.addSeries( series );
		final String xlabel = xcol.getHeader();
		final String ylabel = ycol.getHeader();
		return ChartFactory.createScatterPlot(chart_title, xlabel, ylabel, chartDataset);
	}

}
