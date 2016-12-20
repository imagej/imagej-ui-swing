package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.LineStyle;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import java.awt.*;

/**
 * @author Matthias Arzt
 */

public class JfcLineStyles {

	static void modifyRenderer(AbstractRenderer renderer, int seriesIndex, LineStyle style) {
		if(style == null)
			return;
		if(renderer instanceof XYLineAndShapeRenderer)
			((XYLineAndShapeRenderer) renderer).setSeriesLinesVisible(seriesIndex, style != LineStyle.NONE);
		if(renderer instanceof LineAndShapeRenderer)
			((LineAndShapeRenderer) renderer).setSeriesLinesVisible(seriesIndex, style != LineStyle.NONE);
		renderer.setSeriesStroke(seriesIndex, getAwtBasicStroke(style));
	}

	// --- Helper Constants ---

	private static BasicStroke solid = new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND);

	private static BasicStroke dash = new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,
			.0f,new float[]{6.0f,6.0f},0.0f);

	private static BasicStroke dot = new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,
			.0f,new float[]{0.6f,4.0f},0.0f);

	private static BasicStroke none = new BasicStroke(1,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,
			.0f,new float[]{0.0f,100.0f},0.0f);

	// --- Helper Finctions ---

	private static BasicStroke getAwtBasicStroke(LineStyle style) {
		switch (style) {
			case SOLID:
				return solid;
			case NONE:
				return none;
			case DASH:
				return dash;
			case DOT:
				return dot;
		}
		return null;
	}

}
