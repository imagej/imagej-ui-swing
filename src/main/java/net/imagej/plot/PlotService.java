package net.imagej.plot;

import net.imagej.ImageJService;
import net.imagej.table.Table;

// TODO: consider extending WrapperService, and making Plot into a WrapperPlugin
public interface PlotService extends ImageJService {
	Plot create(Table<?, ?> data, PlotStyle style);
}
