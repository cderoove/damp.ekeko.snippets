package org.acm.seguin.metrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Summarize {
	private File dir;
	public Summarize(String dirName) {
		dir = new File(dirName);
	}

	public void run() {
		File[] list = dir.listFiles();
		for (int ndx = 0; ndx < list.length; ndx++) {
			extractTotals(list[ndx]);
		}
	}

	private void extractTotals(File input) {
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(input));
			String name = input.getName();
			name = name.substring(0, name.indexOf(".csv"));

			String line = bufferedReader.readLine();
			while (line != null) {
				if (line.indexOf("---,---,---") >= 0) {
					System.out.println(name + "," + line);
				}
				line = bufferedReader.readLine();
			}

			bufferedReader.close();
		}
		catch (IOException ioe) {
		}
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Syntax:  org.acm.seguin.metrics.Summarize <dir>");
		}
		else {
			(new Summarize(args[0])).run();
		}
	}
}
