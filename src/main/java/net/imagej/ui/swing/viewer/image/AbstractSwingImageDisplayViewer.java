/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2020 ImageJ developers.
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

package net.imagej.ui.swing.viewer.image;

import net.imagej.Dataset;
import net.imagej.ui.viewer.image.AbstractImageDisplayViewer;

import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.options.event.OptionsEvent;
import org.scijava.plugin.Parameter;
import org.scijava.ui.awt.AWTDropTargetEventDispatcher;
import org.scijava.ui.awt.AWTInputEventDispatcher;
import org.scijava.ui.viewer.DisplayWindow;

/**
 * A Swing image display viewer, which displays 2D planes in grayscale or
 * composite color. Intended to be subclassed by a concrete implementation that
 * provides a {@link DisplayWindow} in which the display should be housed.
 * 
 * @author Curtis Rueden
 * @author Lee Kamentsky
 * @author Grant Harris
 * @author Barry DeZonia
 */
public abstract class AbstractSwingImageDisplayViewer extends
	AbstractImageDisplayViewer implements SwingImageDisplayViewer
{

	protected AWTInputEventDispatcher dispatcher;

	@Parameter
	private EventService eventService;

	private JHotDrawImageCanvas imgCanvas;
	private SwingImageDisplayPanel imgPanel;

	// -- SwingImageDisplayViewer methods --

	@Override
	public JHotDrawImageCanvas getCanvas() {
		return imgCanvas;
	}

	// -- DisplayViewer methods --

	@Override
	public void view(final DisplayWindow w, final Display<?> d) {
		super.view(w, d);
		
		// NB: resolve the racing condition when other consumer are looking up the 
		// active display
		getContext().service(DisplayService.class).setActiveDisplay(getDisplay());

		dispatcher = new AWTInputEventDispatcher(getDisplay(), eventService);

		// broadcast input events (keyboard and mouse)
		imgCanvas = new JHotDrawImageCanvas(this);
		imgCanvas.addEventDispatcher(dispatcher);

		// broadcast drag-and-drop events
		final AWTDropTargetEventDispatcher dropDispatcher =
			new AWTDropTargetEventDispatcher(getDisplay(), eventService);
		imgCanvas.addEventDispatcher(dropDispatcher);

		imgPanel = new SwingImageDisplayPanel(this, getWindow());
		setPanel(imgPanel);

		updateTitle();
	}

	@Override
	public SwingImageDisplayPanel getPanel() {
		return imgPanel;
	}

	@Override
	public Dataset capture() {
		return getCanvas().capture();
	}

	// -- Disposable methods --
	
	/**
	 * NB: a reference to the imgCanvas is held, ultimately, by a finalizable
	 * parent of a javax.swing.JViewport. This means that the entire resource
	 * stack is held until finalize executes. This can be troublesome when
	 * resources held by the imgCanvas themselves react to finalization or
	 * reference queueing (e.g. of PhantomReferences). At the point dispose is
	 * called, we know we're trying to release resources associated with this
	 * object, so it makes sense to do as much as we can up front. By clearing the
	 * imgCanvas, we break the strong reference link from Finalizer to the
	 * imgCanvas's resources, allowing them to be garbage collected, etc... and
	 * working around the limitation of swing classes overriding finalize.
	 */
	@Override
	public void dispose() {
		super.dispose();

		if (imgCanvas != null) {
			imgCanvas.dispose();
			imgCanvas = null;
		}
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(@SuppressWarnings("unused") final OptionsEvent e) {
		updateLabel();
	}

}
