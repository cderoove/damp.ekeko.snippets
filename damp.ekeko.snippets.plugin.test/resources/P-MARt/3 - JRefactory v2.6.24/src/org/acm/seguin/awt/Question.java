package org.acm.seguin.awt;

import javax.swing.JOptionPane;

/**
 *  Asks the user a yes no question.  Has the capability of
 *  automatically answering yes for junit tests.
 *
 *@author    Chris Seguin
 */
public class Question {
	private static boolean autoYes = false;


	/**
	 *  Determines if we should always answer yes to any question
	 *
	 *@param  way  true if we should always answer yes
	 */
	public static void setAlwaysYes(boolean way) {
		autoYes = way;
	}


	/**
	 *  Asks the user a question and returns true if the answer is yes
	 *
	 *@param  title     The title of the message displayed to the user
	 *@param  question  The question that we asked of the user
	 *@return           true if they answered yes
	 */
	public static boolean isYes(String title, String question) {
		if (autoYes) {
			return true;
		}

		int result = JOptionPane.showConfirmDialog(null, question, title,
				JOptionPane.YES_NO_OPTION);

		return result == JOptionPane.YES_OPTION;
	}
}
