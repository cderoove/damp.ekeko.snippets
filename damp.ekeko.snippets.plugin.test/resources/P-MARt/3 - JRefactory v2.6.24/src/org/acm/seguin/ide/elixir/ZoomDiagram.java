package org.acm.seguin.ide.elixir;


/**
 *  Handles all the diagram zooming for Elixir IDE
 *
 *@author    Chris Seguin
 */
public class ZoomDiagram {
	/**
	 *  Scales to 10%
	 */
	public static void tenPercent() {
		work(0.1);
	}


	/**
	 *  Scales to 25%
	 */
	public static void twentyfivePercent() {
		work(0.25);
	}


	/**
	 *  Scales to 50%
	 */
	public static void fiftyPercent() {
		work(0.5);
	}


	/**
	 *  Scales to 100%
	 */
	public static void fullSize() {
		work(1.0);
	}


	/**
	 *  Gets the Manager attribute of the ZoomDiagram class
	 *
	 *@return    The Manager value
	 */
	private static ViewManager getManager() {
		FrameManager fm = FrameManager.current();
		if (fm == null) {
			return null;
		}
		if (fm.getViewSite() == null) {
			return null;
		}
		return (ViewManager) fm.getViewSite().getCurrentViewManager();
	}


	/**
	 *  Scales the diagram
	 *
	 *@param  manager      the manager
	 *@param  scaleFactor  the amount to scale
	 */
	private static void scale(UMLViewManager manager, double scaleFactor) {
		manager.getDiagram().scale(scaleFactor);
		manager.getDiagram().repaint();
	}


	/**
	 *  actually performs the scaling if it is appropriate
	 *
	 *@param  scaleFactor  The amount to scale the diagram by
	 */
	private static void work(double scaleFactor) {
		ViewManager bvm = getManager();
		if ((bvm != null) && (bvm instanceof UMLViewManager)) {
			scale((UMLViewManager) bvm, scaleFactor);
		}
	}
}
