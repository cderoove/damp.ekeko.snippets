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

package org.netbeans.modules.icebrowser;

import org.openide.options.SystemOption;
import org.openide.actions.GoAction;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

import java.awt.Font;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Enumeration;


/**
* Settings for ICE Browser.
*
* @author Jan Jancura
*/
public class ICEBrowserSettings extends SystemOption {

    // static .....................................................................................

    /** generated Serialized Version UID */
    static final long serialVersionUID = 833203088075073629L;

    /** defaultBackground property name */
    public static final String PROP_DEFAULT_BACKGROUND = "defaultBackground"; // NOI18N
    /** fixedFont property name */
    public static final String PROP_FIXED_FONT = "fixedFont"; // NOI18N
    /** proportionalFont property name */
    public static final String PROP_PROPORTIONAL_FONT = "proportionalFont"; // NOI18N
    /** encoding property name */
    public static final String PROP_ENCODING = "encoding"; // NOI18N


    // init ........................................................................................

    public ICEBrowserSettings () {
        if (getProperty (PROP_DEFAULT_BACKGROUND) == null) {
            ResourceBundle bundle = NbBundle.getBundle (ICEBrowserSettings.class);
            putProperty (PROP_DEFAULT_BACKGROUND, java.awt.Color.white, false);
            putProperty (
                PROP_FIXED_FONT,
                Font.decode (bundle.getString ("DEFAULT_FIXED_FONT")),
                false
            ); // NOI18N
            putProperty (
                PROP_PROPORTIONAL_FONT,
                Font.decode (bundle.getString ("DEFAULT_PROPORTIONAL_FONT")),
                false
            ); // NOI18N
            String s = bundle.getString ("DEFAULT_ENCODING"); // NOI18N
            if ( (s != null) && (s.trim ().length () > 0))
                putProperty (PROP_ENCODING, s, false);
            else {
                /*        s = System.getProperties ().getProperty ("sun.io.unicode.encoding"); // NOI18N
                        if (s != null) 
                          putProperty (PROP_ENCODING, s, false);
                        else    Do not work...*/
                putProperty (PROP_ENCODING, "", false);
            }
        }
    }


    // SystemOption implementation ..................................................................

    /**
    * Returns name of this setings.
    */
    public String displayName () {
        return NbBundle.getBundle (ICEBrowserSettings.class).getString ("CTL_ICE_Browser_settings");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ICEBrowserSettings.class);
    }


    // properties .................................................................................

    /**
    * @returns default color of browser background.
    */
    public java.awt.Color getDefaultBackground () {
        return (java.awt.Color) getProperty (PROP_DEFAULT_BACKGROUND);
    }

    /**
    * Set default color of browser background.
    */
    public void setDefaultBackground (java.awt.Color color) {
        putProperty (PROP_DEFAULT_BACKGROUND, color, true);
    }

    /**
    * @returns default fixed font for HTML browser.
    */
    public Font getFixedFont () {
        return (Font) getProperty (PROP_FIXED_FONT);
    }

    /**
    * Set default fixed font for HTML browser.
    */
    public void setFixedFont (Font font) {
        putProperty (PROP_FIXED_FONT, font, true);
    }

    /**
    * @returns default proportional font for HTML browser.
    */
    public Font getProportionalFont () {
        return (Font) getProperty (PROP_PROPORTIONAL_FONT);
    }

    /**
    * Set default proportional font for HTML browser.
    */
    public void setProportionalFont (Font font) {
        putProperty (PROP_PROPORTIONAL_FONT, font, true);
    }

    /**
    * @returns history for HTML browser.
    */
    public java.util.ArrayList getHistory () {
        return IceBrowserImpl.history;
    }

    /**
    * Set history for HTML browser.
    */
    public void setHistory (java.util.ArrayList history) {
        IceBrowserImpl.history = history;
    }

    /**
    * @returns encoing for browser.
    */
    public String getEncoding () {
        return (String) getProperty (PROP_ENCODING);
    }

    /**
    * Set encoing for browser.
    */
    public void setEncoding (String encoding) {
        putProperty (PROP_ENCODING, encoding, true);
    }
}

/*
 * Log
 *  4    Gandalf-post-FCS1.1.1.1     4/5/00   Jan Jancura     Default values for Fonts
 *       & encoding are in bundles now
 *  3    Gandalf-post-FCS1.1.1.0     4/3/00   Jan Jancura     Encoding support
 *  2    Gandalf   1.1         1/13/00  Ian Formanek    NOI18N
 *  1    Gandalf   1.0         12/23/99 Jan Jancura     
 * $
 */
