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
import org.openide.debugger.DebuggerType;
import org.openide.explorer.propertysheet.editors.FileOnlyEditor;

import org.netbeans.modules.debugger.delegator.DefaultDebuggerType;

/** Object that provides beaninfo for {@link AppletDebuggerType}.
*
* @author Petr Jiricka
*/
public class AppletDebuggerTypeBeanInfo extends SimpleBeanInfo {

    private static Image icon;

    private static BeanDescriptor descr;
    private static PropertyDescriptor[] prop;
    static {
        //try {
        descr = new BeanDescriptor (AppletDebuggerType.class);
        ResourceBundle bundle = NbBundle.getBundle(AppletDebuggerTypeBeanInfo.class);

        descr.setName (bundle.getString("CTL_Debug_Name"));

        /*
        prop = new PropertyDescriptor [1];
        prop[0] = new PropertyDescriptor ("name", AppletDebuggerType.class, "getName", "setName"); // 0 // NOI18N
        prop[0].setDisplayName(bundle.getString("PROP_DebugName"));
        prop[0].setShortDescription(bundle.getString("HINT_DebugName"));

    } catch (IntrospectionException ex) {
        ex.printStackTrace ();
    }
        */
    }

    public final BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (DefaultDebuggerType.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
        //return new BeanInfo[] {new ProcessDebuggerTypeBeanInfo ()};
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
          icon = loadImage("/org/netbeans/modules/applet/appletDebugging.gif"); // NOI18N
        return icon;*/
    }

}


/*
 * Log
 *  13   Gandalf-post-FCS1.11.1.0    4/18/00  Jan Jancura     New "default" debugger 
 *       type
 *  12   Gandalf   1.11        3/8/00   Petr Jiricka    Bugfix 5935 - icons
 *  11   Gandalf   1.10        1/15/00  Daniel Prusa    'Identify Name' insted 
 *       of 'name' in property sheet.
 *  10   Gandalf   1.9         1/13/00  Petr Jiricka    More i18n
 *  9    Gandalf   1.8         1/12/00  Petr Jiricka    i18n
 *  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/7/99  Petr Jiricka    Specified icon
 *  6    Gandalf   1.5         8/13/99  Petr Jiricka    Property name changed to
 *        read/write
 *  5    Gandalf   1.4         8/9/99   Petr Jiricka    Fixed bad name property
 *  4    Gandalf   1.3         8/9/99   Petr Jiricka    Change of debugger API -
 *       dependence on debugger module
 *  3    Gandalf   1.2         6/30/99  Ian Formanek    Reflecting package 
 *       changes of some property editors
 *  2    Gandalf   1.1         6/29/99  Petr Jiricka    
 *  1    Gandalf   1.0         6/28/99  Petr Jiricka    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach added hidden property
 */
