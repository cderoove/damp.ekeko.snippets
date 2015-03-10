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

package org.netbeans.examples.modules.minicomposer;
import java.awt.Image;
import java.beans.*;
import org.openide.execution.ProcessExecutor;
import org.openide.util.NbBundle;
public class ExternalPlayerBeanInfo extends SimpleBeanInfo {
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] {
                       Introspector.getBeanInfo (ProcessExecutor.class)
                   };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }
    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor desc = new BeanDescriptor (ExternalPlayer.class);
        desc.setDisplayName (NbBundle.getBundle (ExternalPlayerBeanInfo.class).getString ("LBL_ExternalPlayer"));
        desc.setShortDescription (NbBundle.getBundle (ExternalPlayerBeanInfo.class).getString ("HINT_ExternalPlayer"));
        return desc;
    }
    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor classPath = new PropertyDescriptor ("classPath", ProcessExecutor.class);
            classPath.setHidden (true);
            PropertyDescriptor bootClassPath = new PropertyDescriptor ("bootClassPath", ProcessExecutor.class);
            bootClassPath.setHidden (true);
            PropertyDescriptor repositoryPath = new PropertyDescriptor ("repositoryPath", ProcessExecutor.class, "getRepositoryPath", null);
            repositoryPath.setHidden (true);
            PropertyDescriptor libraryPath = new PropertyDescriptor ("libraryPath", ProcessExecutor.class, "getLibraryPath", null);
            libraryPath.setHidden (true);
            PropertyDescriptor environmentVariables = new PropertyDescriptor ("environmentVariables", ProcessExecutor.class);
            environmentVariables.setHidden (true);
            PropertyDescriptor workingDirectory = new PropertyDescriptor ("workingDirectory", ProcessExecutor.class);
            workingDirectory.setHidden (true);
            return new PropertyDescriptor[] { classPath, bootClassPath, repositoryPath, libraryPath, environmentVariables, workingDirectory };
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
                icon = loadImage ("ExternalPlayerIcon.gif");
            return icon;
        } else {
            return null;
        }
    }
}
