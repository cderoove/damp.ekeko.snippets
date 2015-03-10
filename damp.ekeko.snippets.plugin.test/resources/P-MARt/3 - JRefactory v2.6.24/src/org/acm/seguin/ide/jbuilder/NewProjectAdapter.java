/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder;

import java.util.Iterator;
import java.util.LinkedList;

import org.acm.seguin.ide.common.MultipleDirClassDiagramReloader;

/**
 *  New project adapter that adds source directories to the current reloader.
 *
 *@author    Chris Seguin
 */
public class NewProjectAdapter extends BrowserAdapter {
	private LinkedList list;


	/**
	 *  Constructor for the NewProjectAdapter object
	 */
	public NewProjectAdapter() {
		list = new LinkedList();
	}


	/**
	 *  A particular project was activated
	 *
	 *@param  browser  The browser that it was activated in
	 *@param  project  The project
	 */
	public void browserProjectActivated(Browser browser, Project project) {
		if (!list.contains(project)) {
			list.add(project);

			if (project instanceof JBProject) {
				MultipleDirClassDiagramReloader reloader =
						UMLNodeViewerFactory.getFactory().getReloader();
				JBProject jbProject = (JBProject) project;
				registerProject(reloader, jbProject);
				reloader.reload();
			}
		}
	}


	/**
	 *  A project was closed in a particular browser
	 *
	 *@param  browser  the browser
	 *@param  project  the project
	 */
	public void browserProjectClosed(Browser browser, Project project) {
		list.remove(project);

		MultipleDirClassDiagramReloader reloader =
				UMLNodeViewerFactory.getFactory().getReloader();
		reloader.clear();

		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			JBProject jbProject = (JBProject) iter.next();
			registerProject(reloader, jbProject);
		}
	}


	/**
	 *  Registers a project with the directory reloader
	 *
	 *@param  reloader   the reloader
	 *@param  jbProject  the JBuilder project
	 */
	private void registerProject(MultipleDirClassDiagramReloader reloader, JBProject jbProject) {
		ProjectPathSet paths = jbProject.getPaths();
		Url[] dirs = paths.getSourcePath();
		for (int ndx = 0; ndx < dirs.length; ndx++) {
			String directory = dirs[ndx].getFile();
			reloader.addRootDirectory(directory);
		}
	}
}
