/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.properties;

import java.io.*;
import java.util.ResourceBundle;
import java.util.Properties;

import org.openide.options.ContextSystemOption;
import org.openide.util.NbBundle;
import org.openide.util.io.ReaderInputStream;

/** Settings for properties data loader and properties source parser
*
* @author Petr Jiricka
*/
public class PropertiesSettings extends ContextSystemOption {
    /** serial uid */
    //static final long serialVersionUID = -8522143676848697297L;

    public static final String PROP_AUTO_PARSING_DELAY = "autoParsingDelay";

    /** The resource bundle for the form editor */
    public static ResourceBundle bundle;

    /** property value */
    //  private static String table = "USER="+System.getProperty("user.name")+"\n";

    /** auto parsing delay */
    private static int autoParsingDelay = 2000;

    /** If true then external execution is used */
    public String displayName () {
        return getString("CTL_Properties_option");
    }

    static final long serialVersionUID =435101794077184415L;
    public PropertiesSettings() {
    }

    /** Gets the delay time for the start of the parsing.
    * @return The time in milis
    */
    public int getAutoParsingDelay() {
        return autoParsingDelay;
    }

    /** Sets the delay time for the start of the parsing.
    * @param delay The time in milis
    */
    public void setAutoParsingDelay(int delay) {
        if (delay < 0)
            throw new IllegalArgumentException();
        autoParsingDelay = delay;
    }

    /** @return localized string */
    static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(PropertiesSettings.class);
        }
        return bundle.getString(s);
    }

}

/*
 * <<Log>>
 */
