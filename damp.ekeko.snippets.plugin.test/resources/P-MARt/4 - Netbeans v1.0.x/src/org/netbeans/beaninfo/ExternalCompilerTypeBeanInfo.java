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

import org.openide.compiler.*;
import org.openide.util.NbBundle;

/** Object that provides beaninfo for {@link ExternalCompilerType}.
*
* @author Jaroslav Tulach
*/
public class ExternalCompilerTypeBeanInfo extends SimpleBeanInfo {
    private static BeanDescriptor descr;
    private static PropertyDescriptor[] prop;
    private static Image icon;
    private static Image icon32;


    static {
        try {
            descr = new BeanDescriptor (ExternalCompilerType.class);
            ResourceBundle bundle = NbBundle.getBundle(ProcessExecutorBeanInfo.class);

            descr.setName (bundle.getString("LAB_ExternalCompilerType"));

            prop = new PropertyDescriptor[2];
            prop[0] = new PropertyDescriptor ("externalCompiler", ExternalCompilerType.class, "getExternalCompiler", "setExternalCompiler"); // 0 // NOI18N
            prop[1] = new PropertyDescriptor ("errorExpression", ExternalCompilerType.class, "getErrorExpression", "setErrorExpression"); // 1 // NOI18N
        } catch (IntrospectionException ex) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) ex.printStackTrace();
        }
    }

    public BeanDescriptor getBeanDescriptor () {
        return descr;
    }

    public PropertyDescriptor[] getPropertyDescriptors () {
        return prop;
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (CompilerType.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }

    /* Provides the JarFileSystem's icon */
    public Image getIcon(int type) {
        if (icon == null) {
            icon = loadImage("/org/netbeans/core/resources/externalCompilerType.gif"); // NOI18N
            icon32 = loadImage("/org/netbeans/core/resources/externalCompilerType32.gif"); // NOI18N
        }

        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return icon;
        else
            return icon32;
    }

}


/*
 * Log
 *  5    Gandalf   1.4         1/13/00  Jaroslav Tulach I18N
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         10/1/99  Jesse Glick     Cleanup of service type 
 *       name presentation.
 *  2    Gandalf   1.1         9/14/99  Jaroslav Tulach Error expressions.
 *  1    Gandalf   1.0         9/10/99  Jaroslav Tulach 
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach added hidden property
 */
