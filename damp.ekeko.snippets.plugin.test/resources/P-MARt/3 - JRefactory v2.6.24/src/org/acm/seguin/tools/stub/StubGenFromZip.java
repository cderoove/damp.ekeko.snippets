/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.tools.stub;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 *  Generates a stub set from a file
 *
 *@author    Chris Seguin
 */
class StubGenFromZip
{
	private String filename;
	private StubFile sf;


	/**
	 *  Constructor for the StubGenFromZip object
	 *
	 *@param  name     The name of the zip file
	 *@param  stubKey  Description of Parameter
	 *@param  file     Description of Parameter
	 */
	public StubGenFromZip(String name, String stubKey, File file)
	{
		filename = name;
		sf = new StubFile(stubKey, file);
	}


	/**
	 *  Main processing method for the StubGenFromZip object
	 */
	public void run()
	{
		try
		{
			ZipFile zipfile = new ZipFile(filename);

			Enumeration entryEnum = zipfile.entries();
			while (entryEnum.hasMoreElements())
			{
				ZipEntry entry = (ZipEntry) entryEnum.nextElement();
				if (applies(entry))
				{
					InputStream input = zipfile.getInputStream(entry);
					generateStub(input, entry.getName());
					input.close();
				}
			}

			sf.done();
		}
		catch (Throwable thrown)
		{
			thrown.printStackTrace(System.out);
		}
	}


	/**
	 *  Does this algorithm apply to this entry
	 *
	 *@param  entry  the entry
	 *@return        true if we should generate a stub from it
	 */
	private boolean applies(ZipEntry entry)
	{
		return !entry.isDirectory() && entry.getName().endsWith(".java");
	}


	/**
	 *  Generates a stub
	 *
	 *@param  input     the input stream
	 *@param  filename  the filename
	 */
	private void generateStub(InputStream input, String filename)
	{
		System.out.println("Generating a stub for:  " + filename);
		sf.apply(input, filename);
	}
}
