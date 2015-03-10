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

package org.netbeans.modules.web.core.jswdk;

import java.awt.Image;
import java.beans.*;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

import org.openide.execution.*;

/** Object that provides beaninfo for {@link JspExecutor}.
*
* @author Petr Jiricka
*/
public class JspExecutorBeanInfo extends SimpleBeanInfo {

    private static Image icon;

    private static BeanDescriptor descr;
    private static PropertyDescriptor[] prop;
    static {
        try {
            descr = new BeanDescriptor (JspExecutor.class);
            ResourceBundle bundle = NbBundle.getBundle(JspExecutorBeanInfo.class);

            descr.setName (bundle.getString("CTL_JspExec_Name"));

            prop = new PropertyDescriptor[4];
            prop[0] = new PropertyDescriptor ("port", ServletJspExecutor.class, "getPort", "setPort"); // NOI18N
            prop[0].setDisplayName(bundle.getString("CTL_PROP_Port"));
            prop[0].setShortDescription(bundle.getString("HINT_Port"));
            prop[1] = new PropertyDescriptor ("MIMETypes", ServletJspExecutor.class, "getMIMETypes", "setMIMETypes"); // NOI18N
            prop[1].setDisplayName(bundle.getString("CTL_PROP_MIMETypes"));
            prop[1].setShortDescription(bundle.getString("HINT_MIMETypes"));
            prop[1].setPropertyEditorClass(PairPropertyEditor.MimeMapEditor.class);
            prop[2] = new PropertyDescriptor ("welcomeFiles", ServletJspExecutor.class, "getWelcomeFiles", "setWelcomeFiles"); // NOI18N
            prop[2].setDisplayName(bundle.getString("CTL_PROP_WelcomeFiles"));
            prop[2].setShortDescription(bundle.getString("HINT_WelcomeFiles"));
            prop[3] = new PropertyDescriptor ("invoker", ServletJspExecutor.class, "isInvoker", "setInvoker"); // NOI18N
            prop[3].setDisplayName(bundle.getString("CTL_PROP_Invoker"));
            prop[3].setShortDescription(bundle.getString("HINT_Invoker"));

        } catch (IntrospectionException ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace ();
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
            return new BeanInfo[] { Introspector.getBeanInfo (ProcessExecutor.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ie.printStackTrace ();
            return null;
        }
    }

    /** @param type Desired type of the icon
    * @return returns the Executor's/Debugger Type's icon
    */
    public Image getIcon(final int type) {
        return getAdditionalBeanInfo()[0].getIcon(type);
        /*if (icon == null)
          icon = loadImage("/org/netbeans/modules/web/core/resources/servletExecution.gif"); // NOI18N
        return icon;*/
    }

}


/*
 * Log
 *  5    Gandalf   1.4         3/8/00   Petr Jiricka    Bugfix 5935 - icons
 *  4    Gandalf   1.3         1/13/00  Petr Jiricka    More i18n
 *  3    Gandalf   1.2         1/12/00  Petr Jiricka    i18n phase 1
 *  2    Gandalf   1.1         1/6/00   Petr Jiricka    Fixed label of JSP 
 *       Executor
 *  1    Gandalf   1.0         12/21/99 Petr Jiricka    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach added hidden property
 */
