/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.common;

import java.util.Enumeration;
import java.util.Vector;

import org.acm.seguin.uml.UMLPackage;
import org.acm.seguin.uml.loader.Reloader;
import org.acm.seguin.uml.loader.ReloaderSingleton;

/**
 *  Object that is responsible for reloading the class diagrams
 *
 *@author    Chris Seguin
 */
public abstract class ClassDiagramReloader implements Reloader {
	private Vector umlDiagrams;


	/**
	 *  Constructor for the ClassDiagramReloader object
	 */
	public ClassDiagramReloader() {
		umlDiagrams = new Vector();
		ReloaderSingleton.register(this);
	}


	/**
	 *  Adds a class diagram to the loader
	 *
	 *@param  diagram  the class diagram
	 */
	public void add(UMLPackage diagram) {
		if ((diagram != null) && !umlDiagrams.contains(diagram)) {
			umlDiagrams.add(diagram);
		}
	}


	/**
	 *  Removes a specific class diagram from the registry
	 *
	 *@param  diagram  the class diagram
	 */
	public void remove(UMLPackage diagram) {
		if (diagram != null) {
			umlDiagrams.remove(diagram);
		}
	}


	/**
	 *  Removes all class diagrams from this loader
	 */
	public void clear() {
		umlDiagrams.removeAllElements();
	}


	/**
	 *  Reload the diagrams
	 */
	protected void reloadDiagrams() {
		Enumeration enum_ = umlDiagrams.elements();
		(new RefreshDiagramThread(enum_)).start();
	}

	/**
	 *  Reload the summary information and update the diagrams
	 */
	public abstract void reload();
}
