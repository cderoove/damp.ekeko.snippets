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

package org.netbeans.modules.apisupport;

import java.awt.Image;
import java.beans.*;

import org.openide.execution.Executor;

public class InstallModuleExecutorBeanInfo extends SimpleBeanInfo {

    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor bd = new BeanDescriptor (InstallModuleExecutor.class);
        bd.setDisplayName ("Install as Module");
        return bd;
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (Executor.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    private static Image icon;
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon == null)
                icon = loadImage ("resources/InstallModuleExecutorIcon.gif");
            return icon;
        } else {
            return null;
        }
    }

}

/*
 * Log
 *  8    Gandalf   1.7         1/26/00  Jesse Glick     Executor display names 
 *       can just be taken from bean descriptor.
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         10/13/99 Jesse Glick     Various fixes and 
 *       enhancements:    - added a Changes.txt    - fixed handling of 
 *       OpenAPIs.zip on install/uninstall (previously did not correctly unmount
 *       on uninstall, nor check for already-mounted on install)    - added a 
 *       CompilerTypeTester    - display name & icon updates from Tim    - 
 *       removed link to ToDo.txt from docs page    - various BeanInfo's, both 
 *       in templates and in the support itself, did not display superclass 
 *       BeanInfo correctly    - ExecutorTester now permits user to customize 
 *       new executor instance before running it
 *  5    Gandalf   1.4         10/7/99  Jesse Glick     
 *  4    Gandalf   1.3         10/5/99  Jesse Glick     BeanDescriptor's with 
 *       display names.
 *  3    Gandalf   1.2         10/5/99  Jesse Glick     Sundry API changes 
 *       affecting me.
 *  2    Gandalf   1.1         9/30/99  Jesse Glick     Package rename and misc.
 *  1    Gandalf   1.0         9/20/99  Jesse Glick     
 * $
 */
