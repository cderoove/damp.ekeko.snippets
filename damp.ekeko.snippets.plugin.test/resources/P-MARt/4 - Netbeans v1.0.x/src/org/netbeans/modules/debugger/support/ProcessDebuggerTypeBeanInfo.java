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

package org.netbeans.modules.debugger.support;

import java.awt.Image;
import java.awt.Toolkit;
import java.beans.*;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;
import org.openide.debugger.DebuggerType;


/** A BeanInfor for ProcessDebuggerType.
*
* @author Jan Jancura
* @version 0.11, May 16, 1998
*/
public class ProcessDebuggerTypeBeanInfo extends SimpleBeanInfo {

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor (ProcessDebuggerType.PROP_DEBUGGER_PROCESS, ProcessDebuggerType.class,
                                               "getDebuggerProcess", "setDebuggerProcess"), // 0 // NOI18N
                       new PropertyDescriptor (ProcessDebuggerType.PROP_CLASSIC, ProcessDebuggerType.class,
                                               "isClassic", "setClassic"), // 1 // NOI18N
                       new PropertyDescriptor (ProcessDebuggerType.PROP_REPOSITORY, ProcessDebuggerType.class,
                                               "getRepositoryPath", null), // 2 // NOI18N
                       new PropertyDescriptor (ProcessDebuggerType.PROP_LIBRARY, ProcessDebuggerType.class,
                                               "getLibraryPath", null), // 3 // NOI18N
                       new PropertyDescriptor (ProcessDebuggerType.PROP_CLASSPATH, ProcessDebuggerType.class,
                                               "getClassPath", null), // 4 // NOI18N
                       new PropertyDescriptor (ProcessDebuggerType.PROP_BOOT_CLASSPATH, ProcessDebuggerType.class,
                                               "getBootClassPath", null), // 5 // NOI18N
                   };
            ResourceBundle bundle = NbBundle.getBundle (ProcessDebuggerTypeBeanInfo.class);
            desc[0].setDisplayName (bundle.getString ("PROP_DEBUGGER_PROCESS"));
            desc[0].setShortDescription (bundle.getString ("HINT_DEBUGGER_PROCESS"));
            desc[1].setDisplayName (bundle.getString ("PROP_Classic"));
            desc[1].setShortDescription (bundle.getString ("HINT_Classic"));
            desc[2].setDisplayName (bundle.getString ("PROP_RepositoryPath"));
            desc[2].setShortDescription (bundle.getString ("HINT_RepositoryPath"));
            desc[2].setExpert (true);
            desc[3].setDisplayName (bundle.getString ("PROP_LibraryPath"));
            desc[3].setShortDescription (bundle.getString ("HINT_LibraryPath"));
            desc[3].setExpert (true);
            desc[4].setDisplayName (bundle.getString ("PROP_ClassPath"));
            desc[4].setShortDescription (bundle.getString ("HINT_ClassPath"));
            desc[4].setExpert (true);
            desc[5].setDisplayName (bundle.getString ("PROP_BootClassPath"));
            desc[5].setShortDescription (bundle.getString ("HINT_BootClassPath"));
            desc[5].setExpert (true);
        } catch (IntrospectionException ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ex.printStackTrace ();
        }
    }


    /** Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        return desc;
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (DebuggerType.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    /** @param type Desired type of the icon
    * @return returns the Executor's/Debugger Type's icon
    */
    public Image getIcon(final int type) {
        return getAdditionalBeanInfo()[0].getIcon(type);
    }

}

/*
 * Log
 *  8    Gandalf   1.7         3/8/00   Petr Jiricka    Bugfix 5935 - icons
 *  7    Gandalf   1.6         1/16/00  Jesse Glick     
 *  6    Gandalf   1.5         1/14/00  Daniel Prusa    NOI18N
 *  5    Gandalf   1.4         11/29/99 Jan Jancura     
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         9/28/99  Jan Jancura     
 *  2    Gandalf   1.1         8/18/99  Jan Jancura     Localization & Current 
 *       thread & Current session
 *  1    Gandalf   1.0         8/9/99   Jan Jancura     
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jan Formanek    property display names changed
 */
