package net.imagej.defaultplot;

import net.imagej.plot.BarSeries;
import org.scijava.util.ColorRGB;

import java.util.Collection;

/**
 * Default Implementation of {@link BarSeries}.
 *
 * @author Matthias Arzt
 */
class DefaultBarSeries<C> extends DefaultCategorySeries<C> implements BarSeries<C> {

	private ColorRGB color = null;

	DefaultBarSeries() { super(); }

	// -- BarSeries methods --

	@Override
	public ColorRGB getColor() { return color; }

	@Override
	public void setColor(ColorRGB color) { this.color = color; };

}
