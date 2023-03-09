/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2023 ImageJ developers.
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

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.display.DatasetView;
import net.imagej.display.ImageCanvas;
import net.imagej.display.ImageDisplay;
import net.imagej.display.ImageDisplayService;
import net.imagej.display.event.AxisPositionEvent;
import net.imagej.display.event.DelayedPositionEvent;
import net.imagej.display.event.LUTsChangedEvent;
import net.imagej.ui.swing.SwingColorBar;
import net.imagej.ui.viewer.image.ImageDisplayPanel;
import net.imglib2.display.ColorTable;
import net.miginfocom.swing.MigLayout;

import org.scijava.event.EventHandler;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.awt.AWTInputEventDispatcher;
import org.scijava.ui.swing.StaticSwingUtils;
import org.scijava.ui.viewer.DisplayWindow;

/**
 * Swing implementation of image display panel. Contains a label, a graphics
 * pane containing an {@link ImageCanvas}, and panel containing dimensional
 * controllers. This panel is added to a top-level {@link DisplayWindow} display
 * container.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 * @author Barry DeZonia
 */
public class SwingImageDisplayPanel extends JPanel implements ImageDisplayPanel {

	private final SwingImageDisplayViewer displayViewer;
	private final ImageDisplay display;
	private final JLabel imageLabel;
	private final JPanel imagePane;
	private final SwingColorBar colorBar;
	private final JPanel sliderPanel;
	private final DisplayWindow window;
	private boolean initialScaleCalculated = false;

	private final Map<AxisType, JScrollBar> axisSliders =
		new ConcurrentHashMap<>();

	private final Map<AxisType, JLabel> axisLabels =
		new HashMap<>();

	@Parameter
	private ImageDisplayService imageDisplayService;

	@Parameter
	private EventService eventService;

	// -- constructors --

	public SwingImageDisplayPanel(final SwingImageDisplayViewer displayViewer,
		final DisplayWindow window)
	{
		this.displayViewer = displayViewer;
		this.window = window;

		display = displayViewer.getDisplay();

		imageLabel = new JLabel(" ");
		final int prefHeight = imageLabel.getPreferredSize().height;
		imageLabel.setPreferredSize(new Dimension(0, prefHeight));

		imagePane = new JPanel();
		imagePane.setLayout(new MigLayout("ins 0,wrap 1", "fill,grow",
			"[fill,grow|]"));
		imagePane.add(displayViewer.getCanvas());

		final int colorBarHeight = 8;
		colorBar = new SwingColorBar(colorBarHeight);
		colorBar.setPreferredSize(new Dimension(0, colorBarHeight));
		colorBar.setBorder(new LineBorder(Color.black));
		imagePane.add(colorBar);

		sliderPanel = new JPanel();
		sliderPanel.setLayout(new MigLayout("fillx,wrap 2", "[right|fill,grow]"));

		setLayout(new BorderLayout());
		setBorder(new EmptyBorder(3, 3, 3, 3));

		add(imageLabel, BorderLayout.NORTH);
		add(imagePane, BorderLayout.CENTER);
		add(sliderPanel, BorderLayout.SOUTH);

		window.setContent(this);

		display.getContext().inject(this);
	}

	// -- SwingDisplayPanel methods --

	public void addEventDispatcher(final AWTInputEventDispatcher dispatcher) {
		dispatcher.register(this, true, false);
	}

	// -- ImageDisplayPanel methods --

	@Override
	public ImageDisplay getDisplay() {
		return display;
	}

	// -- DisplayPanel methods --

	@Override
	public DisplayWindow getWindow() {
		return window;
	}

	@Override
	public void redoLayout() {
		final int oldSliderHeight = sliderPanel.getPreferredSize().height;

		// rebuild display panel UI
		createSliders();
		updateColorBar(0);
		sliderPanel.setVisible(sliderPanel.getComponentCount() > 0);
		doInitialSizing();
		displayViewer.getCanvas().rebuild();
		revalidate();

		final int newSliderHeight = sliderPanel.getPreferredSize().height;

		// HACK: Grow DisplayWindow height to match slider panel height increase.
		final int heightIncrease = newSliderHeight - oldSliderHeight;
		if (heightIncrease > 0) {
			final Component c = (Component) getWindow();
			final Dimension size = c.getSize();
			size.height += heightIncrease;
			c.setSize(size);
		}

		repaint();
	}

	@Override
	public void setLabel(final String s) {
		imageLabel.setText(s);
	}

	@Override
	public void redraw() {
		final DatasetView view = imageDisplayService.getActiveDatasetView(display);
		if (view == null || view.getProjector() == null) return; // no active dataset
		view.getProjector().map();
		displayViewer.getCanvas().update();
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final AxisPositionEvent event) {
		if (event.getDisplay() != getDisplay()) return;
		final AxisType axis = event.getAxis();
		updateAxis(axis);
		eventService.publish(new DelayedPositionEvent(display, axis));
	}

	@EventHandler
	protected void onEvent(LUTsChangedEvent event) {
		if (!getDisplay().contains(event.getView())) return;
		final int value = (int) display.getLongPosition(Axes.CHANNEL);
		updateColorBar(value);
	}

	// -- Helper methods --

	private void createSliders() {
		// remove obsolete sliders
		for (final AxisType axis : axisSliders.keySet()) {
			if (display.dimensionIndex(axis) >= 0) continue; // axis still active
			sliderPanel.remove(axisSliders.get(axis));
			sliderPanel.remove(axisLabels.get(axis));
			axisSliders.remove(axis);
			axisLabels.remove(axis);
		}

		// configure sliders to match axes and extents
		for (int i = 0; i < display.numDimensions(); i++) {
			final AxisType axis = display.axis(i).type();
			if (axis.isXY()) continue; // skip spatial axes
			final int min = (int) display.min(i);
			final int max = (int) display.max(i) + 1;
			// TODO this may not be the best place to prune length 1 axes.
			// Probably should happen when generating an ImgPlus or in SCIFIO
			// Metadata itself.
			if (max - min <= 1) continue; // skip length 1 axes
			final int value = (int) display.getLongPosition(axis);

			final JScrollBar axisSlider = axisSliders.get(axis);
			if (axisSlider == null) {
				// create new slider

				final JLabel label = new JLabel(axis.getLabel());
				label.setHorizontalAlignment(SwingConstants.RIGHT);
				axisLabels.put(axis, label);

				final JScrollBar slider =
					new JScrollBar(Adjustable.HORIZONTAL, value, 1, min, max);
				slider.addAdjustmentListener(new AdjustmentListener() {

					@Override
					public void adjustmentValueChanged(final AdjustmentEvent e) {
						display.setPosition(slider.getValue(), axis);
					}
				});
				axisSliders.put(axis, slider);

				// add slider to panel
				// TODO - ensure sliders are always ordered the same (alphabetical?)
				sliderPanel.add(label);
				sliderPanel.add(slider);
			}
			else {
				// update slider extents and value
				if (axisSlider.getMinimum() != min || axisSlider.getMaximum() != max) {
					axisSlider.setValues(value, 1, min, max);
				}
				else if (axisSlider.getValue() != value) axisSlider.setValue(value);
			}
		}
	}

	private void updateColorBar(final int c) {
		final DatasetView view = imageDisplayService.getActiveDatasetView(display);
		if (view == null) return; // no active dataset
		List<ColorTable> colorTables = view.getColorTables();
		if (c >= colorTables.size()) return;
		final ColorTable lut = colorTables.get(c);
		colorBar.setColorTable(lut);
		colorBar.repaint();
	}

	private void doInitialSizing() {
		final double scale = findFullyVisibleScale();
		final double zoomLevel = display.getCanvas().getBestZoomLevel(scale);
		final ImageCanvas canvas = displayViewer.getDisplay().getCanvas();
		canvas.setZoomAndCenter(zoomLevel);
		if (!initialScaleCalculated) {
			canvas.setInitialScale(canvas.getZoomFactor());
			initialScaleCalculated = true;
		}
	}

	// NB - BDZ would like to streamline this to avoid extra display updates.
	// However testing scroll bar position versus current display's position
	// fails as the display position always matches already. So we can't avoid
	// calling display.update() by testing such. We need to make the display
	// update mechanism smarter if possible. Perhaps by giving it hints about
	// the changes being made.
	
	private void updateAxis(final AxisType axis) {
		final int value = (int) display.getLongPosition(axis);
		if (axis == Axes.CHANNEL) updateColorBar(value);
		final JScrollBar scrollBar = axisSliders.get(axis);
		if (scrollBar != null) scrollBar.setValue(value);
		getDisplay().update();
	}

	private double findFullyVisibleScale() {
		final JHotDrawImageCanvas canvas = displayViewer.getCanvas();
		final Dimension canvasSize = canvas.getPreferredSize();
		final Rectangle deskBounds = StaticSwingUtils.getWorkSpaceBounds();

		// calc height variables
		final int labelHeight = imageLabel.getPreferredSize().height;
		final int sliderHeight = sliderPanel.getPreferredSize().height;

		// NB - extraSpace used to be 64. But this caused some images to come in at
		// an inappropriate scale. I think extraSpace was just a hopeful fudge
		// factor. I am eliminating it for now but leaving machinery in place in
		// case we want to restore such code. This fixes bug #1472.
		final int extraSpace = 0;

		// determine largest viewable panel sizes
		final int maxViewHeight =
			deskBounds.height - labelHeight - sliderHeight - extraSpace;
		final int maxViewWidth = deskBounds.width - extraSpace;

		// is canvas bigger than largest viewable panel?
		if ((canvasSize.width > maxViewWidth) ||
			(canvasSize.height > maxViewHeight))
		{
			// yes it is
			// so calc best scale that brings whole image into max viewable panel size

			final double canvasAspect = 1.0 * canvasSize.width / canvasSize.height;
			final double viewAspect = 1.0 * maxViewWidth / maxViewHeight;
			if (canvasAspect < viewAspect) {
				// image height the issue
				return 1.0 * maxViewHeight / canvasSize.height;
			}
			// else image width the issue
			return 1.0 * maxViewWidth / canvasSize.width;
		}

		// else canvas fits on screen as is
		return 1;
	}

}
