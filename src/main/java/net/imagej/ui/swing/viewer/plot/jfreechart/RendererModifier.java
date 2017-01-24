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

package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.LineStyle;
import net.imagej.plot.MarkerStyle;
import net.imagej.plot.SeriesStyle;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.scijava.ui.awt.AWTColors;
import org.scijava.util.ColorRGB;

/**
 * @author Matthias Arzt
 */
class RendererModifier {

	final AbstractRenderer renderer;

	private RendererModifier(AbstractRenderer renderer) {
		this.renderer = renderer;
	}

	static public RendererModifier wrap(AbstractRenderer renderer) {
		return new RendererModifier(renderer);
	}

	public void setSeriesStyle(int index, SeriesStyle style) {
		if(style == null)
			return;
		setSeriesColor(index, style.getColor());
		setSeriesLineStyle(index, style.getLineStyle());
		setSeriesMarkerStyle(index, style.getMarkerStyle());
	}

	public void setSeriesColor(int index, ColorRGB color) {
		if (color == null)
			return;
		renderer.setSeriesPaint(index, AWTColors.getColor(color));
	}

	public void setSeriesLineStyle(int index, LineStyle style) {
		AwtLineStyles line = AwtLineStyles.getInstance(style);
		setSeriesLinesVisible(index, line.isVisible());
		renderer.setSeriesStroke(index, line.getStroke());
	}

	public void setSeriesMarkerStyle(int index, MarkerStyle style) {
		AwtMarkerStyles marker = AwtMarkerStyles.getInstance(style);
		setSeriesShapesVisible(index, marker.isVisible());
		setSeriesShapesFilled(index, marker.isFilled());
		renderer.setSeriesShape(index, marker.getShape());
	}

	private void setSeriesLinesVisible(int index, boolean visible) {
		if(renderer instanceof LineAndShapeRenderer)
			((LineAndShapeRenderer) renderer).setSeriesLinesVisible(index, visible);
		if(renderer instanceof XYLineAndShapeRenderer)
			((XYLineAndShapeRenderer) renderer).setSeriesLinesVisible(index, visible);
	}

	private void setSeriesShapesVisible(int index, boolean visible) {
		if(renderer instanceof LineAndShapeRenderer)
			((LineAndShapeRenderer) renderer).setSeriesShapesVisible(index, visible);
		if(renderer instanceof XYLineAndShapeRenderer)
			((XYLineAndShapeRenderer) renderer).setSeriesShapesVisible(index, visible);
	}

	private void setSeriesShapesFilled(int index, boolean filled) {
		if(renderer instanceof LineAndShapeRenderer)
			((LineAndShapeRenderer) renderer).setSeriesShapesFilled(index, filled);
		if(renderer instanceof XYLineAndShapeRenderer)
			((XYLineAndShapeRenderer) renderer).setSeriesShapesFilled(index, filled);
	}

}
