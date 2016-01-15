package net.imagej.ui.swing.ops;

import net.imagej.Dataset;
import net.imglib2.img.Img;

import org.scijava.Priority;
import org.scijava.convert.AbstractConverter;
import org.scijava.convert.Converter;
import org.scijava.plugin.Plugin;

@SuppressWarnings("rawtypes")
@Plugin(type = Converter.class, priority = Priority.LOW_PRIORITY)
public class DatasetImgConverter extends AbstractConverter<Dataset, Img> {

	@SuppressWarnings("unchecked")
	@Override
	public <T> T convert(Object src, Class<T> dest) {
		Dataset data = (Dataset)src;
		return (T) data.getImgPlus().getImg();
	}

	@Override
	public Class<Img> getOutputType() {
		return Img.class;
	}

	@Override
	public Class<Dataset> getInputType() {
		return Dataset.class;
	}
}
