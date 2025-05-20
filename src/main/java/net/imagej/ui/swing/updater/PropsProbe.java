package net.imagej.ui.swing.updater;

import java.io.*;
import java.util.*;

/**
 * Simple helper class to print out all system properties. Adapted from
 * https://github.com/apposed/jaunch/blob/a14cf163b5431b3b623628c6c2dba5d5ef3faed2/configs/Props.java
 */
public class PropsProbe {
	public static void main(String[] args) {
		PrintWriter out = null;
		final boolean printToFile = args.length > 0;
		try {
			if (printToFile) {
				out = new PrintWriter(new BufferedWriter(new FileWriter(args[0])));
			}
			else {
				out = new PrintWriter(System.out, true);
			}
			Properties props = System.getProperties();
			for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements(); ) {
				String key = e.nextElement().toString();
				String value = props.getProperty(key);
				out.println(key + "=" + value);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (out != null && printToFile) {
				out.close();
			}
		}
	}
}
