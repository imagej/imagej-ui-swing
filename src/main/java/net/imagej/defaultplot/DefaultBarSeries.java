package net.imagej.defaultplot;

import net.imagej.plot.BarSeries;
import org.scijava.util.ColorRGB;

import java.util.Collection;

/**
 * @author Matthias Arzt
 */
public class DefaultBarSeries<C> extends DefaultCategorySeries<C> implements BarSeries<C> {

	ColorRGB color = null;

	DefaultBarSeries() { super(); }

	@Override
	public ColorRGB getColor() { return color; }

	@Override
	public void setColor(ColorRGB color) { this.color = color; };

}
