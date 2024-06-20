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

import net.imagej.display.ImageDisplay;
import net.imagej.display.OverlayView;
import net.imagej.display.event.DataViewEvent;

import org.jhotdraw.draw.Figure;


/**
 * An event that reports the creation of a JHotDraw {@link Figure}, linked to an
 * ImageJ {@link OverlayView} in a particular {@link ImageDisplay}.
 * 
 * @author Lee Kamentsky
 * @author Curtis Rueden
 */
public class FigureCreatedEvent extends DataViewEvent {

	private final OverlayView view;
	private final Figure figure;
	private final ImageDisplay display;

	public FigureCreatedEvent(final OverlayView view, final Figure figure,
		final ImageDisplay display)
	{
		super(view);
		this.view = view;
		this.figure = figure;
		this.display = display;
	}

	/** Gets the newly created {@link Figure}. */
	public Figure getFigure() {
		return figure;
	}

	/** Gets the associated {@link ImageDisplay}. */
	public ImageDisplay getDisplay() {
		return display;
	}

	// -- DataViewEvent methods --

	@Override
	public OverlayView getView() {
		return view;
	}

}
