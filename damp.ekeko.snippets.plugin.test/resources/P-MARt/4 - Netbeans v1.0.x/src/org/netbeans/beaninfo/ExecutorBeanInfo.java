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

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Object that provides beaninfo for {@link Executor}.
*
* @author Petr Jiricka
*/
public class ExecutorBeanInfo extends SimpleBeanInfo {
    private static BeanDescriptor descr;
    private static PropertyDescriptor[] prop;
    private static Image icon;
    private static Image icon32;

    static {
        descr = new BeanDescriptor (org.openide.execution.Executor.class);
        ResourceBundle bundle = NbBundle.getBundle(ExecutorBeanInfo.class);
        descr.setDisplayName (bundle.getString ("LAB_ExecutorType"));
        descr.setShortDescription (bundle.getString ("HINT_ExecutorType"));
        descr.setValue ("helpID", org.openide.execution.Executor.class.getName ()); // NOI18N
    }

    public BeanDescriptor getBeanDescriptor () {
        return descr;
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (org.openide.ServiceType.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }

    /* Provides the JarFileSystem's icon */
    public Image getIcon(int type) {
        if (icon == null) {
            icon = loadImage("/org/netbeans/core/resources/executionTypes.gif"); // NOI18N
            icon32 = loadImage("/org/netbeans/core/resources/executionTypes32.gif"); // NOI18N
        }

        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return icon;
        else
            return icon32;
    }

}


/*
 * Log
 *  7    Gandalf   1.6         1/13/00  Jaroslav Tulach I18N
 *  6    Gandalf   1.5         11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         10/1/99  Jesse Glick     Cleanup of service type 
 *       name presentation.
 *  3    Gandalf   1.2         9/10/99  Jaroslav Tulach Bean infos for services.
 *  2    Gandalf   1.1         7/24/99  Ian Formanek    Printing stack trace on 
 *       netbeans.debug.exceptions property only
 *  1    Gandalf   1.0         6/9/99   Petr Jiricka    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach added hidden property
 */
