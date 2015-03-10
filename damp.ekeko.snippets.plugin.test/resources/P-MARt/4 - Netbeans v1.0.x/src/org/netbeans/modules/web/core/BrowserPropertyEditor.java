/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core;

import java.beans.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openide.util.NbBundle;

/** Property editor for host property of HttpServerSettings class
*
* @author Petr Jiricka, Ales Novak
* @version 0.11 May 5, 1999
*/
public class BrowserPropertyEditor extends PropertyEditorSupport {

    private static final java.util.ResourceBundle bundle = NbBundle.getBundle(BrowserPropertyEditor.class);

    /** localized internal browser string*/
    private final static String INTER = bundle.getString("CTL_InternalBrowser");

    /** localized external browser string*/
    private final static String EXTER = bundle.getString("CTL_ExternalBrowser");

    /** array of hosts */
    private static final String[] browserNames = {INTER, EXTER};

    /** @return names of the supported LookAndFeels */
    public String[] getTags() {
        return browserNames;
    }

    /** @return text for the current value */
    public String getAsText () {
        String host = (String) getValue();
        if (host.equals(ServletSettings.INTERNAL_BROWSER)) {
            return INTER;
        }
        else {
            return EXTER;
        }
    }

    /** @param text A text for the current value. */
    public void setAsText (String text) {
        if (text.equals(INTER)) {
            setValue(ServletSettings.INTERNAL_BROWSER);
            return;
        }
        if (text.equals(EXTER)) {
            setValue(ServletSettings.EXTERNAL_BROWSER);
            return;
        }

        throw new IllegalArgumentException ();
    }

    public void setValue(Object value) {
        super.setValue(value);
    }
}

/*
 * Log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         8/3/99   Petr Jiricka    
 * $
 */
