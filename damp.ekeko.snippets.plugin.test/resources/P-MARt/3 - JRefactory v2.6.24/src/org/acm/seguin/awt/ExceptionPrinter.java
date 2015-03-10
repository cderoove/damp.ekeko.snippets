package org.acm.seguin.awt;


/**
 *  Prints exceptions
 *
 *@author    Chris Seguin
 */
public class ExceptionPrinter {
	/**
	 *  Prints exceptions
	 *
	 *@param  exc  the exception to be printed
	 */
	public static void print(Throwable exc) {
		exc.printStackTrace(System.out);
	}


	/**
	 *  Prints exceptions
	 *
	 *@param  exc  the exception to be printed
	 */
	public static void dialog(Throwable exc) {
		(new ExceptionDialog(exc)).setVisible(true);
	}
}
