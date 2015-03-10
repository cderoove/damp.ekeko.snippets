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

import java.awt.Image;
import java.awt.Toolkit;
import java.beans.*;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;


/**
* A BeanInfo for ICEBrowserSettings.
*
* @author Jan Jancura
*/
public class ICEBrowserSettingsBeanInfo extends SimpleBeanInfo {

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    /** Icons */
    static Image icon;
    static Image icon32;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor [] {
                       new PropertyDescriptor (
                           ICEBrowserSettings.PROP_DEFAULT_BACKGROUND, ICEBrowserSettings.class,
                           "getDefaultBackground", "setDefaultBackground"), // 0 // NOI18N
                       new PropertyDescriptor (
                           ICEBrowserSettings.PROP_FIXED_FONT, ICEBrowserSettings.class,
                           "getFixedFont", "setFixedFont"), // 1 // NOI18N
                       new PropertyDescriptor (
                           ICEBrowserSettings.PROP_PROPORTIONAL_FONT, ICEBrowserSettings.class,
                           "getProportionalFont", "setProportionalFont"), // 2 // NOI18N
                       new PropertyDescriptor (
                           "history", ICEBrowserSettings.class, // NOI18N
                           "getHistory", "setHistory"), // 3 // NOI18N
                       new PropertyDescriptor (
                           "encoding", ICEBrowserSettings.class, // NOI18N
                           "getEncoding", "setEncoding"), // 4 // NOI18N
                   };
            ResourceBundle bundle = NbBundle.getBundle (ICEBrowserSettingsBeanInfo.class);
            desc[0].setDisplayName (bundle.getString ("PROP_DEFAULT_BACKGROUND"));
            desc[0].setShortDescription (bundle.getString ("HINT_DEFAULT_BACKGROUND"));
            desc[1].setDisplayName (bundle.getString ("PROP_FIXED_FONT"));
            desc[1].setShortDescription (bundle.getString ("HINT_FIXED_FONT"));
            desc[2].setDisplayName (bundle.getString ("PROP_PROPORTIONAL_FONT"));
            desc[2].setShortDescription (bundle.getString ("HINT_PROPORTIONAL_FONT"));
            desc[3].setHidden (true);
            desc[4].setDisplayName (bundle.getString ("PROP_ENCODING"));
            desc[4].setShortDescription (bundle.getString ("HINT_ENCODING"));
            desc[4].setPropertyEditorClass (EncodingEditor.class);
        } catch (IntrospectionException ex) {
            //throw new InternalError ();
            ex.printStackTrace (); //[PENDINGbeta comment out]
        }
    }


    /** Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        return desc;
    }

    /**
    * Returns the ICEBrowserSettings' icon. 
    */
    public Image getIcon (int type) {
        return loadImage("/org/openide/resources/html/htmlView.gif"); // NOI18N
    }
}


/*
 * Log
 *  5    Gandalf-post-FCS1.2.1.1     4/5/00   Jan Jancura     Encoding editor added
 *  4    Gandalf-post-FCS1.2.1.0     4/3/00   Jan Jancura     Encoding support
 *  3    Gandalf   1.2         1/13/00  Ian Formanek    NOI18N #2
 *  2    Gandalf   1.1         1/13/00  Ian Formanek    NOI18N
 *  1    Gandalf   1.0         12/23/99 Jan Jancura     
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jan Formanek    property display names changed
 */
