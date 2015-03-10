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

package org.netbeans.modules.applet;

import java.awt.Image;
import java.beans.*;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;
import org.openide.execution.Executor;


/** Object that provides beaninfo for {@link AppletExecutor}.
*
* @author Petr Jiricka
*/
public class AppletExecutorBeanInfo extends SimpleBeanInfo {

    private static Image icon;

    private static BeanDescriptor descr;
    private static PropertyDescriptor[] prop;
    static {
        try {
            descr = new BeanDescriptor (AppletExecutor.class);
            ResourceBundle bundle = NbBundle.getBundle(AppletExecutorBeanInfo.class);

            descr.setName (bundle.getString("CTL_Exec_Name"));

            prop = new PropertyDescriptor[1];
            prop[0] = new PropertyDescriptor("externalExecutor", AppletExecutor.class); // 0
            prop[0].setDisplayName(bundle.getString("PROP_External_path"));
            prop[0].setShortDescription(bundle.getString("HINT_External_path"));
        } catch (IntrospectionException ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) {
                ex.printStackTrace ();
            }
        }
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            // Do not pick up general stuff from ProcessExecutor:
            return new BeanInfo[] { Introspector.getBeanInfo (org.openide.execution.Executor.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    public BeanDescriptor getBeanDescriptor () {
        return descr;
    }

    public PropertyDescriptor[] getPropertyDescriptors () {
        return prop;
    }

    /** @param type Desired type of the icon
    * @return returns the Executor's/Debugger Type's icon
    */
    public Image getIcon(final int type) {
        return getAdditionalBeanInfo()[0].getIcon(type);
        /*if (icon == null)
          icon = loadImage("/org/netbeans/modules/applet/appletExecution.gif"); // NOI18N
        return icon;*/
    }
}


/*
 * Log
 *  11   Gandalf   1.10        3/8/00   Petr Jiricka    Bugfix 5935 - icons
 *  10   Gandalf   1.9         1/17/00  Petr Jiricka    Debug output removed
 *  9    Gandalf   1.8         1/16/00  Jesse Glick     
 *  8    Gandalf   1.7         1/13/00  Petr Jiricka    More i18n
 *  7    Gandalf   1.6         1/12/00  Petr Jiricka    i18n
 *  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         10/7/99  Petr Jiricka    Specified icon
 *  4    Gandalf   1.3         7/12/99  Petr Jiricka    Type of "External 
 *       Viewer" property changed to NbProcessDescriptor
 *  3    Gandalf   1.2         6/30/99  Ian Formanek    Reflecting package 
 *       changes of some property editors
 *  2    Gandalf   1.1         6/16/99  Petr Jiricka    New property - external 
 *       viewer
 *  1    Gandalf   1.0         6/9/99   Petr Jiricka    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach added hidden property
 */
