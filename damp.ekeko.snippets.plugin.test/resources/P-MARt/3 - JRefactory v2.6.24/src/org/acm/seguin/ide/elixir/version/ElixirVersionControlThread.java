package org.acm.seguin.ide.elixir.version;

import org.acm.seguin.version.VersionControl;

/**
 *  Version control in separate thread so the window doesn't appear to lock 
 *  up. 
 *
 *@author     Chris Seguin 
 *@created    July 12, 1999 
 */
public class ElixirVersionControlThread extends Thread {
	//  Instance Variables
	private VersionControl delegate = null;
	private String filename;
	private int operation;

	/**
	 *  Description of the Field 
	 */
	public static int CHECK_IN = 1;
	/**
	 *  Description of the Field 
	 */
	public static int CHECK_OUT = 2;
	/**
	 *  Description of the Field 
	 */
	public static int ADD = 3;


	/**
	 *  Constructor for ElixirVersionControl object 
	 *
	 *@param  initDelegate  Description of Parameter 
	 *@param  initFile      Description of Parameter 
	 *@param  initOp        Description of Parameter 
	 */
	public ElixirVersionControlThread(VersionControl initDelegate, String initFile, int initOp) {
		delegate = initDelegate;
		filename = initFile;
		operation = initOp;
	}


	/**
	 *  Program that actually runs 
	 */
	public void run() {
		if (operation == CHECK_IN) {
			delegate.checkIn(filename);
		}
		else if (operation == CHECK_OUT) {
			delegate.checkOut(filename);
		}
		else if (operation == ADD) {
			delegate.add(filename);
		}

		//  Update elixir
		updateElixirFile(filename);
		updateElixirProject();
	}


	/**
	 *  Update Elixir's project window 
	 */
	private void updateElixirProject() {
		//  Repaint the project manager
		com.elixirtech.ide.project.ProjectManager.current().repaint();
	}


	/**
	 *  Update the current file 
	 *
	 *@param  filename  Description of Parameter 
	 */
	private void updateElixirFile(String filename) {
		//  Get the view manager for this file
		IViewSite vs = FrameManager.current().getViewSite();
		ViewManager vm = vs.getViewManager(filename);

		//  If we got one, it is open
		if (vm != null) {
			vm.reload();

			/*
			 * The following code would bring that file to the front.
			 * I don't want to do that yet.
			 */
			//vs.setCurrentViewManager(vm);
		}
	}
}
