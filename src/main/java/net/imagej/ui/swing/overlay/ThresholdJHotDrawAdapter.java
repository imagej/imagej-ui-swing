/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2024 ImageJ developers.
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

package net.imagej.ui.swing.overlay;

import java.awt.Shape;

import net.imagej.Dataset;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.overlay.Overlay;
import net.imagej.overlay.ThresholdOverlay;
import net.imagej.threshold.ThresholdService;
import net.imagej.ui.swing.tools.SwingPolygonTool;

import org.jhotdraw.draw.Figure;
import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.tool.Tool;

/**
 * JHotDraw adapter for threshold overlays.
 *
 * @author Barry DeZonia
 * @see SwingPolygonTool
 */
@Plugin(type = JHotDrawAdapter.class, priority = Priority.HIGH_PRIORITY)
public class ThresholdJHotDrawAdapter extends
	AbstractJHotDrawAdapter<ThresholdOverlay, ThresholdFigure>
{

	@Parameter
	private ImageDisplayService imageDisplayService;

	@Parameter
	private ThresholdService thresholdService;

	// -- JHotDrawAdapter methods --

	@Override
	public boolean supports(Tool tool) {
		return false; // there is no threshold tool
	}

	@Override
	public boolean supports(Overlay overlay, Figure figure) {
		if (!(overlay instanceof ThresholdOverlay)) return false;
		return figure == null || figure instanceof ThresholdFigure;
	}

	@Override
	public Overlay createNewOverlay() {
		ImageDisplay display = imageDisplayService.getActiveImageDisplay();
		if (display == null) return null;
		return thresholdService.getThreshold(display);
	}

	@Override
	public Figure createDefaultFigure() {
		ImageDisplay display = imageDisplayService.getActiveImageDisplay();
		if (display == null) return null;
		Dataset dataset = imageDisplayService.getActiveDataset();
		if (dataset == null) return null;
		ThresholdOverlay overlay = thresholdService.getThreshold(display);
		return new ThresholdFigure(display, dataset, overlay);
	}

	@Override
	public JHotDrawTool getCreationTool(ImageDisplay display) {
		return new IJCreationTool<>(display, this);
	}

	@Override
	public Shape toShape(ThresholdFigure figure) {
		throw new UnsupportedOperationException("to be implemented"); // TODO
	}

}
