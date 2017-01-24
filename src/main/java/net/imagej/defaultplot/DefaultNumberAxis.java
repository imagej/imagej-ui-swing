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

import net.imagej.plot.NumberAxis;

/**
 * The dafult implementation of the {@link NumberAxis} interface.
 *
 * @author Matthias Arzt
 */

class DefaultNumberAxis extends AbstractLabeled implements NumberAxis {

	private double min;

	private double max;

	private boolean logarithmic;

	private RangeStrategy rangeStrategy;

	DefaultNumberAxis() {
		min = 0;
		max = 0;
		logarithmic = false;
		rangeStrategy = RangeStrategy.AUTO;
	}

	// -- NumberAxis methods --

	@Override
	public void setManualRange(double min, double max) {
		rangeStrategy = RangeStrategy.MANUAL;
		this.min = min;
		this.max = max;
	}

	@Override
	public void setAutoRange() {
		this.rangeStrategy = RangeStrategy.AUTO;
	}

	@Override
	public void setAutoIncludeZeroRange() {
		this.rangeStrategy = RangeStrategy.AUTO_INCLUDE_ZERO;
	}

	@Override
	public RangeStrategy getRangeStrategy() {
		return rangeStrategy;
	}

	@Override
	public double getMin() {
		return min;
	}

	@Override
	public double getMax() {
		return max;
	}

	@Override
	public void setLogarithmic(boolean logarithmic) {
		this.logarithmic = logarithmic;
	}

	@Override
	public boolean isLogarithmic() {
		return logarithmic;
	}

}
