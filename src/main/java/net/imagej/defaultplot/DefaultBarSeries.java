package net.imagej.defaultplot;

import net.imagej.plot.BarSeries;
import org.scijava.util.ColorRGB;

import java.util.Collection;

/**
 * @author Matthias Arzt
 */
public class DefaultBarSeries extends DefaultCategorySeries implements BarSeries {

	ColorRGB color = null;

	DefaultBarSeries(String label, Collection<Double> values) {
		super(label, values);
	}

	@Override
	public ColorRGB getColor() { return color; }

	@Override
	public void setColor(ColorRGB color) { this.color = color; };

}
