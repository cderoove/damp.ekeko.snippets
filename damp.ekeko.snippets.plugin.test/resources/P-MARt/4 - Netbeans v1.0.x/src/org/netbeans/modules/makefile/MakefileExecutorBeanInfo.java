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

package org.netbeans.modules.makefile;

import java.awt.Image;
import java.beans.*;

import org.openide.execution.ProcessExecutor;
import org.openide.util.NbBundle;

/** Bean info for the executor.
 * @author Jesse Glick
 */
public class MakefileExecutorBeanInfo extends SimpleBeanInfo {

    /** Get inherited bean info.
     * @return the super info
     */
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

    /** Get the bean descriptor.
     * @return a localized descriptor
     */
    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor desc = new BeanDescriptor (MakefileExecutor.class);
        desc.setDisplayName (NbBundle.getBundle (MakefileExecutorBeanInfo.class).getString ("LABEL_MakefileExecutor"));
        desc.setShortDescription (NbBundle.getBundle (MakefileExecutorBeanInfo.class).getString ("HINT_MakefileExecutor"));
        return desc;
    }

    /** Get the bean properties.
     * @return the target property, and inherited ones hidden so as to
     * make sure they are not displayed by accident
     */
    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor target = new PropertyDescriptor ("target", MakefileExecutor.class);
            target.setDisplayName (NbBundle.getBundle (MakefileExecutorBeanInfo.class).getString ("PROP_ME_target"));
            target.setShortDescription (NbBundle.getBundle (MakefileExecutorBeanInfo.class).getString ("HINT_ME_target"));
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
            return new PropertyDescriptor[] { target, classPath, bootClassPath, repositoryPath, libraryPath, environmentVariables, workingDirectory };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    /** A cached icon.
     */
    private static Image icon;
    /** Get a bean icon.
     * @param type the style
     * @return the icon
     */
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon == null)
                icon = loadImage ("makefileObject.gif");
            return icon;
        } else {
            return null;
        }
    }

}
