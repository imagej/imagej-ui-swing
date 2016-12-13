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

package net.imagej.ui.swing.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import net.imagej.widget.HistogramBundle;
import net.imagej.widget.HistogramWidget;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.AnnotationChangeListener;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.scijava.plugin.Plugin;
import org.scijava.ui.swing.widget.SwingInputWidget;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;

/**
 * Render a {@link HistogramBundle} in Swing.
 * 
 * @author Barry DeZonia
 */
@Plugin(type = InputWidget.class)
public class SwingHistogramWidget extends SwingInputWidget<HistogramBundle>
	implements HistogramWidget<JPanel>
{

	// -- fields --

	private HistogramBundle bundle;
	private ChartPanel chartPanel;

	// -- HistogramWidget methods --

	@Override
	public HistogramBundle getValue() {
		return bundle;
	}

	@Override
	public void set(final WidgetModel model) {
		super.set(model);
		bundle = (HistogramBundle) model.getValue();
		// TODO: reconcile with Plot?
		chartPanel = makeChartPanel(bundle);
		bundle.setHasChanges(false);
		getComponent().add(chartPanel);
	}

	@Override
	public boolean supports(final WidgetModel model) {
		return model.isType(HistogramBundle.class);
	}

	// -- helpers --

	private ChartPanel makeChartPanel(final HistogramBundle b) {
		final JFreeChart chart = getChart(null, b);
		final ChartPanel panel = new ChartPanel(chart);
		final int xSize = b.getPreferredSizeX();
		final int ySize = b.getPreferredSizeY();
		panel.setPreferredSize(new java.awt.Dimension(xSize, ySize));
		return panel;
	}

	/**
	 * Returns a JFreeChart containing data from the provided histogram.
	 */
	private JFreeChart getChart(final String title, final HistogramBundle bund) {
		List<XYSeries> series = new ArrayList<>();
		for (int h = 0; h < bund.getHistogramCount(); h++) {
			final XYSeries xys = new XYSeries("histo" + h);
			final long total = bund.getHistogram(h).getBinCount();
			for (long i = 0; i < total; i++) {
				xys.add(i, bund.getHistogram(h).frequency(i));
			}
			series.add(xys);
		}
		final JFreeChart chart = createChart(title, series);
		if (bund.getMinBin() != -1) {
			chart.getXYPlot().addDomainMarker(
				new ValueMarker(bund.getMinBin(), Color.black, new BasicStroke(1)));
		}
		if (bund.getMaxBin() != -1) {
			chart.getXYPlot().addDomainMarker(
				new ValueMarker(bund.getMaxBin(), Color.black, new BasicStroke(1)));
		}
		if (displaySlopeLine(bund)) {
			chart.getXYPlot().addAnnotation(slopeLine());
		}
		return chart;
	}

	private JFreeChart
		createChart(final String title, final List<XYSeries> series)
	{
		final XYSeriesCollection data = new XYSeriesCollection();
		for (XYSeries xys : series) {
			data.addSeries(xys);
		}
		final JFreeChart chart =
			ChartFactory.createXYBarChart(title, null, false, null, data,
				PlotOrientation.VERTICAL, false, true, false);
		setTheme(chart);
		// chart.getXYPlot().setForegroundAlpha(0.50f);
		return chart;
	}

	private final void setTheme(final JFreeChart chart) {
		final XYPlot plot = (XYPlot) chart.getPlot();
		final XYBarRenderer r = (XYBarRenderer) plot.getRenderer();
		final StandardXYBarPainter bp = new StandardXYBarPainter();
		r.setBarPainter(bp);
		r.setSeriesOutlinePaint(0, Color.lightGray);
		r.setShadowVisible(false);
		r.setDrawBarOutline(false);
		setBackgroundDefault(chart);
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

		// rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		rangeAxis.setTickLabelsVisible(false);
		rangeAxis.setTickMarksVisible(false);
		final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setTickLabelsVisible(false);
		domainAxis.setTickMarksVisible(false);
	}

	private final void setBackgroundDefault(final JFreeChart chart) {
		final BasicStroke gridStroke =
			new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
				1.0f, new float[] { 2.0f, 1.0f }, 0.0f);
		final XYPlot plot = (XYPlot) chart.getPlot();
		plot.setRangeGridlineStroke(gridStroke);
		plot.setDomainGridlineStroke(gridStroke);
		plot.setBackgroundPaint(new Color(235, 235, 235));
		plot.setRangeGridlinePaint(Color.white);
		plot.setDomainGridlinePaint(Color.white);
		plot.setOutlineVisible(false);
		plot.getDomainAxis().setAxisLineVisible(false);
		plot.getRangeAxis().setAxisLineVisible(false);
		plot.getDomainAxis().setLabelPaint(Color.gray);
		plot.getRangeAxis().setLabelPaint(Color.gray);
		plot.getDomainAxis().setTickLabelPaint(Color.gray);
		plot.getRangeAxis().setTickLabelPaint(Color.gray);
		final TextTitle title = chart.getTitle();
		if (title != null) title.setPaint(Color.black);
	}

	private boolean displaySlopeLine(final HistogramBundle bund) {
		// OLD
		// if (Double.isNaN(bund.getLineIntercept())) return false;
		// if (Double.isNaN(bund.getLineSlope())) return false;
		// CURRENT
		if (Double.isNaN(bund.getDataMin())) return false;
		if (Double.isNaN(bund.getDataMax())) return false;
		if (Double.isNaN(bund.getTheoreticalMin())) return false;
		if (Double.isNaN(bund.getTheoreticalMax())) return false;
		return true;
	}

	private XYAnnotation slopeLine() {
		return new XYAnnotation() {

			private double x1, y1, x2, y2;

			@Override
			public void removeChangeListener(final AnnotationChangeListener listener)
			{
				// ignore
			}

			@Override
			public void addChangeListener(final AnnotationChangeListener listener) {
				// ignore
			}

			@Override
			public void draw(final Graphics2D g2, final XYPlot plot,
				final Rectangle2D dataArea, final ValueAxis domainAxis,
				final ValueAxis rangeAxis, final int rendererIndex,
				final PlotRenderingInfo info)
			{
				calcLineCoords(dataArea);
				drawLine(g2);
			}

			private void drawLine(final Graphics2D g2) {
				final Color origColor = g2.getColor();
				g2.setColor(Color.black);
				g2.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
				g2.setColor(origColor);
			}

			@SuppressWarnings("synthetic-access")
			private void calcLineCoords(final Rectangle2D rect) {
				// adapted from IJ1's ContrastAdjuster plugin
				// calc slope line from min/max ranges
				final double x = rect.getMinX();
				final double y = rect.getMinY();
				final double w = rect.getWidth();
				final double h = rect.getHeight();
				final double min = bundle.getTheoreticalMin();
				final double max = bundle.getTheoreticalMax();
				final double defaultMin = bundle.getDataMin();
				final double defaultMax = bundle.getDataMax();
				final double scale = w / (defaultMax - defaultMin);
				double slope = 0.0;
				if (max != min) slope = h / (max - min);
				if (min >= defaultMin) {
					x1 = scale * (min - defaultMin);
					y1 = h;
				}
				else {
					x1 = 0;
					if (max > min) {
						y1 = h - ((defaultMin - min) * slope);
					}
					else y1 = h;
				}
				if (max <= defaultMax) {
					x2 = (scale * (max - defaultMin));
					y2 = 0;
				}
				else {
					x2 = w;
					if (max > min) {
						y2 = h - ((defaultMax - min) * slope);
					}
					else y2 = 0;
				}
				x1 += x;
				x2 += x;
				y1 += y;
				y2 += y;
				// System.out.println("line coords " + x1 + "," + y1 + " to " + x2 + ","
				// +
				// y2);
			}

			/*
			 * OLD code for calcing line coords from slope/intercept data. Note that
			 * it is not stretched to the correct aspect ratio here yet.
			 * 
			@SuppressWarnings("synthetic-access")
			@Override
			public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
				ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex,
				PlotRenderingInfo info)
			{
			 double slope = bundle.getLineSlope();
			 double intercept = bundle.getLineIntercept();
			 double scaledLeft =
					 dataArea.getHeight() - intercept * dataArea.getHeight();
			 double scaledRight = scaledLeft - slope * dataArea.getWidth();
			 double xLeft = dataArea.getX();
			 double xRight = xLeft + dataArea.getWidth();
			 Color origColor = g2.getColor();
			 g2.setColor(Color.black);
			 g2.drawLine((int) xLeft, (int) scaledLeft, (int) xRight,
				 (int) scaledRight);
			 g2.setColor(origColor);
			}
			 */
		};
	}

	// -- AbstractUIInputWidget methods ---

	@Override
	public void doRefresh() {
		if (bundle.hasChanges()) {
			bundle.setHasChanges(false);
			final ChartPanel newChartPanel = makeChartPanel(bundle);
			final JFreeChart chart = newChartPanel.getChart();
			chartPanel.setChart(chart);
		}
	}
}
