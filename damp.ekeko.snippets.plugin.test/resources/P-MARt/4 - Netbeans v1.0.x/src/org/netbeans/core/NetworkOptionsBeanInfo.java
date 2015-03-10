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

package org.netbeans.core;

import java.awt.Image;
import java.beans.*;


/**
 * A BeanInfo class for the NetworkOptions JavaBean.
 *
 * @author Ales Novak, Dafe Simonek
 */
public class NetworkOptionsBeanInfo extends SimpleBeanInfo {

    /** Icons for compiler settings objects. */
    static Image icon;
    static Image icon32;

    /** Propertydescriptors */
    static PropertyDescriptor[] descriptors;

    /**
     * Constructs a new BeanInfo class for the NetworkOptions JavaBean.
     */
    public NetworkOptionsBeanInfo () {
    }

    /**
    * This method returns an array of PropertyDescriptors describing the editable properties supported by this bean.
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        if (descriptors == null) initializeDescriptors();
        return descriptors;
    }

    /** Returns the NetworkOptions's icon */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
                (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage("/org/netbeans/core/resources/networkOptions.gif"); // NOI18N
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/core/resources/networkOptions32.gif"); // NOI18N
            return icon32;
        }
    }

    private static void initializeDescriptors () {
        final java.util.ResourceBundle topBundle =
            org.openide.util.NbBundle.
            getBundle(NetworkOptionsBeanInfo.class);
        try {
            descriptors =  new PropertyDescriptor[] {
                               new PropertyDescriptor ("homeURL", org.netbeans.core.NetworkOptions.class), // NOI18N
                               new PropertyDescriptor ("smtpServer", org.netbeans.core.NetworkOptions.class) // NOI18N
                           };
            descriptors[0].setDisplayName(topBundle.getString("PROP_HOME_URL"));
            descriptors[0].setShortDescription(topBundle.getString("HINT_HOME_URL"));
            descriptors[1].setDisplayName(topBundle.getString("PROP_SMTP_SERVER"));
            descriptors[1].setShortDescription(topBundle.getString("HINT_SMTP_SERVER"));
        } catch (IntrospectionException e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) e.printStackTrace();
        }
    }

}


/*
 * Log
 *  6    Gandalf   1.5         1/13/00  Jaroslav Tulach I18N
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         7/24/99  Ian Formanek    Printing stack trace on 
 *       netbeans.debug.exceptions property only
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
