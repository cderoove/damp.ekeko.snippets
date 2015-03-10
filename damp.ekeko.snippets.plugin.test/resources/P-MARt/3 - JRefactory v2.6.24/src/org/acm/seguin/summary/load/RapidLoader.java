package org.acm.seguin.summary.load;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.SummaryTraversal;
import org.acm.seguin.util.FileSettings;

/**
 *  This code is responsible for speeding the loading and saving of the meta
 *  data about the different classes.
 *
 *@author    Chris Seguin
 */
public class RapidLoader {
	/**
	 *  This will save the classes
	 */
	public void save() {
		(new SaveThread()).start();
	}


	/**
	 *  This will load the classes
	 */
	public void load() {
		try {
			System.out.println("RapidLoader.load()");
			String filename = FileSettings.getSettingsRoot() + File.separator + ".refactory" + File.separator + "data.sof";
			FileInputStream fileInput = new FileInputStream(filename);
			BufferedInputStream bufferInput = new BufferedInputStream(fileInput);
			ObjectInputStream in = new ObjectInputStream(bufferInput);
			PackageSummary.loadAll(in);
			in.close();

			SummaryTraversal.setFrameworkLoader(new FlashLoader());
		}
		catch (IOException ioe) {
			ioe.printStackTrace(System.out);
		}
	}


	/**
	 *  Separate thread to save the data to the serialized object file
	 *
	 *@author    Chris Seguin
	 */
	public class SaveThread extends Thread {
		/**
		 *  Main processing method for the SaveThread object
		 */
		public void run() {
			try {
				String filename = FileSettings.getSettingsRoot() + File.separator + ".refactory" + File.separator + "data.sof";
				FileOutputStream fileOutput = new FileOutputStream(filename);
				BufferedOutputStream bufferOutput = new BufferedOutputStream(fileOutput);
				ObjectOutputStream out = new ObjectOutputStream(bufferOutput);
				PackageSummary.saveAll(out);
				out.close();
			}
			catch (IOException ioe) {
				ioe.printStackTrace(System.out);
			}
		}
	}
}
