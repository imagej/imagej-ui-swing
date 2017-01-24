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

package net.imagej.defaultplot;

import net.imagej.plot.SeriesStyle;
import net.imagej.plot.XYSeries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The default implementation of the {@link XYSeries} interface.
 *
 * @author Matthias Arzt
 */
class DefaultXYSeries extends AbstractChartItem implements XYSeries {

	private List<Double> xValues = Collections.emptyList();

	private List<Double> yValues = Collections.emptyList();

	private SeriesStyle style = DefaultSeriesStyle.emptySeriesStyle();

	DefaultXYSeries() { }

	// -- XYSeries methods --

	@Override
	public void setValues(List<Double> xValues, List<Double> yValues) {
		if(xValues.size() != yValues.size())
			throw new IllegalArgumentException();
		this.xValues = Collections.unmodifiableList(xValues);
		this.yValues = Collections.unmodifiableList(yValues);
	}

	@Override
	public List<Double> getXValues() {
		return xValues;
	}

	@Override
	public List<Double> getYValues() {
		return yValues;
	}

	@Override
	public SeriesStyle getStyle() {
		return style;
	}

	@Override
	public void setStyle(SeriesStyle style) {
		this.style = style;
	}

}
