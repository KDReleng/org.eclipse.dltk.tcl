package org.eclipse.dltk.tcl.internal.core.packages;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class PackageUtils {
	public static String packagesToKey(Set packages) {
		Set sorted = new TreeSet();
		sorted.addAll(packages);
		StringBuffer buffer = new StringBuffer();
		for (Iterator iterator = sorted.iterator(); iterator.hasNext();) {
			String object = (String) iterator.next();
			buffer.append("_").append(object);
		}
		return buffer.toString().replaceAll(":", "_");

	}

	static String packageToPath(String packageName, String packageVersion) {
		String result = packageName.replaceAll(":", "_");
		if (packageVersion != null) {
			result += packageVersion.replaceAll("\\.", "_");
		}
		return result;
	}
}
