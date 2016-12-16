package net.imagej.plot;

import org.jfree.chart.JFreeChart;

import java.util.Collection;

/**
 * Created by arzt on 13/12/2016.
 */
public class BoxPlot implements Plot {

	@Override
	public void setTitle(String title) { }

	//setCategoryAxis(String label, Collection<Object>);
	//addLineSeries(String label, Collection<Double>, linestyle, markerstyle, color);
	//addBarSeries(String label, Collection<Double>, color);
	//addBoxSeries(String label, Collection<Collection<Double>>, color);
	@Override
	public JFreeChart getJFreeChart() {
		return null;
	}
}
