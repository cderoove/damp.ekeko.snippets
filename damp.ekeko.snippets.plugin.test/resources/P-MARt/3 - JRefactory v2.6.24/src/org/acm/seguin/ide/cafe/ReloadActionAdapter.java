/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.ide.cafe;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import org.acm.seguin.ide.common.MultipleDirClassDiagramReloader;
import org.acm.seguin.uml.loader.ReloaderSingleton;

/**
 *  Reloads class diagrams for Visual Cafe
 *
 *@author    Chris Seguin
 */
public class ReloadActionAdapter implements ActionListener {
    private static MultipleDirClassDiagramReloader reloader = null;
	/**
	 *  The reload action
	 *
	 *@param  evt  the action that occurred
	 */
	public void actionPerformed(ActionEvent evt) {
		if (ReloadActionAdapter.reloader == null) {
		    reloader = new MultipleDirClassDiagramReloader();
		    ReloaderSingleton.register(reloader);
		}
		
		try {
		    VisualCafe vc = VisualCafe.getVisualCafe();
		    VisualProject[] vps = vc.getProjects();
		    for (int ndx = 0; ndx < vps.length; ndx++) {
		        ReloadActionAdapter.reloader.addRootDirectory(getDirectory(vps[ndx]));
		    }
		}
		catch (Exception exc) {
		    exc.printStackTrace(System.out);
		}
		    
		reloader.setNecessary(true);
		reloader.reload();
	}
	
	private String getDirectory(VisualProject proj) throws MalformedURLException {
		    URL url = proj.getDocumentBase();
		    System.out.println("url:  " + url.toString());
		    String filename = url.getFile().substring(1);
		    System.out.println("filename:  " + filename);
		    int index = filename.lastIndexOf('/');
		    String parent = filename.substring(0, index);
		    System.out.println("Document base:  " + parent);
		    return parent;
	}
}
