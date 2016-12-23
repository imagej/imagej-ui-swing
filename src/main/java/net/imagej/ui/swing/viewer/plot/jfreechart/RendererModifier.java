package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.LineStyle;
import net.imagej.plot.MarkerStyle;
import net.imagej.plot.SeriesStyle;
import net.imagej.ui.swing.viewer.plot.utils.AwtLineStyles;
import net.imagej.ui.swing.viewer.plot.utils.AwtMarkerStyles;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.scijava.ui.awt.AWTColors;
import org.scijava.util.ColorRGB;

/**
 * @author Matthias Arzt
 */
class RendererModifier {

	AbstractRenderer renderer;

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

	private void setSeriesColor(int index, ColorRGB color) {
		if (color == null)
			return;
		renderer.setSeriesPaint(index, AWTColors.getColor(color));
	}

	private void setSeriesLineStyle(int index, LineStyle style) {
		AwtLineStyles line = AwtLineStyles.getInstance(style);
		setSeriesLinesVisible(index, line.isVisible());
		renderer.setSeriesStroke(index, line.getStroke());
	}

	private void setSeriesMarkerStyle(int index, MarkerStyle style) {
		AwtMarkerStyles marker = AwtMarkerStyles.getInstance(style);
		setSeriesShapesVisible(index, marker.isVisible());
		setSeriesShapesFilled(index, marker.isFilled());
		renderer.setSeriesShape(index, marker.getShape());
	}

	void setSeriesLinesVisible(int index, boolean visible) {
		if(renderer instanceof LineAndShapeRenderer)
			((LineAndShapeRenderer) renderer).setSeriesLinesVisible(index, visible);
		if(renderer instanceof XYLineAndShapeRenderer)
			((XYLineAndShapeRenderer) renderer).setSeriesLinesVisible(index, visible);
	}

	void setSeriesShapesVisible(int index, boolean visible) {
		if(renderer instanceof LineAndShapeRenderer)
			((LineAndShapeRenderer) renderer).setSeriesShapesVisible(index, visible);
		if(renderer instanceof XYLineAndShapeRenderer)
			((XYLineAndShapeRenderer) renderer).setSeriesShapesVisible(index, visible);
	}

	void setSeriesShapesFilled(int index, boolean filled) {
		if(renderer instanceof LineAndShapeRenderer)
			((LineAndShapeRenderer) renderer).setSeriesShapesFilled(index, filled);
		if(renderer instanceof XYLineAndShapeRenderer)
			((XYLineAndShapeRenderer) renderer).setSeriesShapesFilled(index, filled);
	}

}
