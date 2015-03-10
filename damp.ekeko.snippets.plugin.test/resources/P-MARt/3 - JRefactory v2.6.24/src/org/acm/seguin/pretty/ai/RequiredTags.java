/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.pretty.ai;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.acm.seguin.pretty.JavaDocableImpl;
import org.acm.seguin.util.FileSettings;
import org.acm.seguin.util.MissingSettingsException;

/**
 *  Adds the tags as they are required by the pretty.settings file.
 *
 *@author    Chris Seguin
 */
public class RequiredTags {
	Object[] arguments;
	private static RequiredTags tags = null;


	/**
	 *  Constructor for the RequiredTags object
	 */
	private RequiredTags() {
		arguments = new Object[3];
		arguments[0] = System.getProperty("user.name");
		arguments[1] = DateFormat.getDateInstance(DateFormat.LONG).format(new Date());
	}


	/**
	 *  Adds the tags that are required for this object
	 *
	 *@param  bundle  the pretty printer bundle
	 *@param  key     the key for this object (class for classes and interfaces,
	 *      method for methods and constructors, or field)
	 *@param  name    the name of the object
	 *@param  jdi     the javadoc tag holder
	 */
	public void addTags(FileSettings bundle, String key, String name, JavaDocableImpl jdi) {
		String tags = bundle.getString(key + ".tags");

		StringTokenizer tok = new StringTokenizer(tags, ", \t\n");
		while (tok.hasMoreTokens()) {
			String next = tok.nextToken();
			add(bundle, next, name, jdi);
		}
	}


	/**
	 *  Adds a particular tag
	 *
	 *@param  bundle  the file settings
	 *@param  tag     the tag we are about to add
	 *@param  name    the name of the object
	 *@param  jdi     the java doc holder
	 */
	private void add(FileSettings bundle, String tag, String name, JavaDocableImpl jdi) {
		if (tag.equalsIgnoreCase("param")) {
		}
		else if (tag.equalsIgnoreCase("return")) {
		}
		else if (tag.equalsIgnoreCase("exception")) {
		}
		else if (tag.equalsIgnoreCase("throws")) {
		}
		else if (jdi.contains(tag)) {
		}
		else {
			addNormalTag(bundle, tag, name, jdi);
		}
	}


	/**
	 *  Adds a normal tag to the javadoc comment
	 *
	 *@param  bundle  the pretty.settings bundle
	 *@param  tag     the tag we are adding
	 *@param  name    the name of the object
	 *@param  jdi     the javadoc comment holder
	 */
	private void addNormalTag(FileSettings bundle, String tag, String name, JavaDocableImpl jdi) {
		try {
			String format = bundle.getString(tag + ".descr");
			arguments[2] = name;
			String value = MessageFormat.format(format, arguments);
			jdi.require("@" + tag, value);
		}
		catch (MissingSettingsException mse) {
			//  Not required since there was no tag.descr involved
		}
	}


	/**
	 *  Gets the Tagger attribute of the RequiredTags class
	 *
	 *@return    The Tagger value
	 */
	public static RequiredTags getTagger() {
		if (tags == null) {
			tags = new RequiredTags();
		}

		return tags;
	}
}
