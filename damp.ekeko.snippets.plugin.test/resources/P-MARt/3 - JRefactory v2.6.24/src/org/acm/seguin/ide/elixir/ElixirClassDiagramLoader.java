package org.acm.seguin.ide.elixir;

import org.acm.seguin.ide.common.SingleDirClassDiagramReloader;

/**
 *  Object responsible for loading the UML class diagrams for
 *  the Elixir IDE.
 *
 *@author    Chris Seguin
 */
public class ElixirClassDiagramLoader {
	private static SingleDirClassDiagramReloader singleton;


	/**
	 *  Constructor for the ElixirClassDiagramLoader object
	 */
	ElixirClassDiagramLoader() {
	}


	/**
	 *  Order from the user to reload the diagrams
	 */
	public static void reload() {
		singleton.setNecessary(true);
		singleton.reload();
	}


	/**
	 *  Registers a reloader with Elixir IDE
	 *
	 *@param  init  the reloader
	 */
	public static void register(SingleDirClassDiagramReloader init) {
		singleton = init;
	}
}
