package net.imagej.ui.swing.viewer.plot.jfreechart;

import net.imagej.plot.CategoryChart;
import org.jfree.chart.JFreeChart;
import org.scijava.Priority;
import org.scijava.convert.AbstractConverter;
import org.scijava.convert.Converter;
import org.scijava.plugin.Plugin;

/**
 * @author Matthias Arzt
 */
@Plugin(type = Converter.class, priority = Priority.NORMAL_PRIORITY)
public class CategoryChartToJfcCpnverter extends AbstractConverter<CategoryChart, JFreeChart> {
	@SuppressWarnings("unchecked")
	@Override
	public <T> T convert(Object o, Class<T> aClass) {
		return (T) new JfcCategoryChartGenerator((CategoryChart) o).getJFreeChart();
	}

	@Override
	public Class<JFreeChart> getOutputType() {
		return JFreeChart.class;
	}

	@Override
	public Class<CategoryChart> getInputType() {
		return CategoryChart.class;
	}
}
