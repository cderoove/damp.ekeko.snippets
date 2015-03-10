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

package org.netbeans.beaninfo;

import java.awt.Image;
import java.beans.*;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

import org.openide.execution.*;

/** Object that provides beaninfo for {@link ThreadExecutor}.
*
* @author Jaroslav Tulach
*/
public class ThreadExecutorBeanInfo extends SimpleBeanInfo {
    private static BeanDescriptor descr;
    private static Image icon;
    private static Image icon32;
    private static ResourceBundle bundle;

    static {
        descr = new BeanDescriptor (ThreadExecutor.class);
    }

    public BeanDescriptor getBeanDescriptor () {
        descr.setName(getString("CTL_ThreadExecutor"));
        return descr;
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (Executor.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }

    /* Provides the JarFileSystem's icon */
    public Image getIcon(int type) {
        if (icon == null) {
            icon = loadImage("/org/netbeans/core/resources/threadExecutor.gif"); // NOI18N
            icon32 = loadImage("/org/netbeans/core/resources/threadExecutor32.gif"); // NOI18N
        }

        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return icon;
        else
            return icon32;
    }

    /** @return localized String */
    static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(ThreadExecutorBeanInfo.class);
        }
        return bundle.getString(s);
    }
}


/*
 * Log
 *  9    Gandalf   1.8         1/13/00  Jaroslav Tulach I18N
 *  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/7/99  Ales Novak      new names for execution 
 *       types
 *  6    Gandalf   1.5         10/1/99  Jesse Glick     Cleanup of service type 
 *       name presentation.
 *  5    Gandalf   1.4         9/10/99  Jaroslav Tulach Bean infos for services.
 *  4    Gandalf   1.3         7/24/99  Ian Formanek    Printing stack trace on 
 *       netbeans.debug.exceptions property only
 *  3    Gandalf   1.2         6/9/99   Petr Jiricka    
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         5/27/99  Jaroslav Tulach 
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach added hidden property
 */
