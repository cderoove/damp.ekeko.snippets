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
import java.io.File;
import java.util.ResourceBundle;

import org.openide.execution.*;
import org.openide.explorer.propertysheet.editors.*;
import org.openide.util.NbBundle;

/** Object that provides beaninfo for {@link ProcessExecutor}.
*
* @author Jaroslav Tulach
*/
public class ProcessExecutorBeanInfo extends SimpleBeanInfo {
    private static BeanDescriptor descr;
    private static PropertyDescriptor[] prop;
    private static Image icon;
    private static Image icon32;


    static {
        try {
            descr = new BeanDescriptor (ProcessExecutor.class);
            ResourceBundle bundle = NbBundle.getBundle(ProcessExecutorBeanInfo.class);

            descr.setName (bundle.getString("LAB_ProcessExecutor"));

            prop = new PropertyDescriptor[7];
            prop[0] = new PropertyDescriptor ("externalExecutor", ProcessExecutor.class, "getExternalExecutor", "setExternalExecutor"); // NOI18N
            prop[0].setDisplayName (bundle.getString ("PROP_PEBI_externalExecutor"));
            prop[0].setShortDescription (bundle.getString ("HINT_PEBI_externalExecutor"));
            prop[1] = new PropertyDescriptor ("classPath", ProcessExecutor.class, "getClassPath", "setClassPath"); // NOI18N
            prop[1].setDisplayName (bundle.getString ("PROP_PEBI_classPath"));
            prop[1].setShortDescription (bundle.getString ("HINT_PEBI_classPath"));
            prop[1].setExpert (true);
            prop[2] = new PropertyDescriptor ("bootClassPath", ProcessExecutor.class, "getBootClassPath", "setBootClassPath"); // NOI18N
            prop[2].setDisplayName (bundle.getString ("PROP_PEBI_bootClassPath"));
            prop[2].setShortDescription (bundle.getString ("HINT_PEBI_bootClassPath"));
            prop[2].setExpert (true);
            prop[3] = new PropertyDescriptor ("repositoryPath", ProcessExecutor.class, "getRepositoryPath", null); // NOI18N
            prop[3].setDisplayName (bundle.getString ("PROP_PEBI_repositoryPath"));
            prop[3].setShortDescription (bundle.getString ("HINT_PEBI_repositoryPath"));
            prop[3].setExpert (true);
            prop[4] = new PropertyDescriptor ("libraryPath", ProcessExecutor.class, "getLibraryPath", null); // NOI18N
            prop[4].setDisplayName (bundle.getString ("PROP_PEBI_libraryPath"));
            prop[4].setShortDescription (bundle.getString ("HINT_PEBI_libraryPath"));
            prop[4].setExpert (true);
            prop[5] = new PropertyDescriptor ("environmentVariables", ProcessExecutor.class); // NOI18N
            prop[5].setDisplayName (bundle.getString ("PROP_PEBI_environmentVariables"));
            prop[5].setShortDescription (bundle.getString ("HINT_PEBI_environmentVariables"));
            prop[5].setExpert (true);
            prop[5].setPropertyEditorClass (EnvVarEd.class);
            try {
                Runtime.class.getMethod ("exec", new Class[] { String[].class, String[].class, File.class }); // NOI18N
                prop[6] = new PropertyDescriptor ("workingDirectory", ProcessExecutor.class); // NOI18N
                prop[6].setShortDescription (bundle.getString ("HINT_PEBI_workingDirectory_RW"));
            } catch (NoSuchMethodException nsme) {
                prop[6] = new PropertyDescriptor ("workingDirectory", ProcessExecutor.class, "getWorkingDirectory", null); // NOI18N
                prop[6].setShortDescription (bundle.getString ("HINT_PEBI_workingDirectory_RO"));
            }
            prop[6].setDisplayName (bundle.getString ("PROP_PEBI_workingDirectory"));
            prop[6].setExpert (true);
            prop[6].setPropertyEditorClass (WorkDirEd.class);
        } catch (IntrospectionException ex) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) ex.printStackTrace();
        }
    }

    public BeanDescriptor getBeanDescriptor () {
        descr.setName(ThreadExecutorBeanInfo.getString("CTL_ProcessExecutor"));
        return descr;
    }

    public PropertyDescriptor[] getPropertyDescriptors () {
        return prop;
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
            icon = loadImage("/org/netbeans/core/resources/processExecutor.gif"); // NOI18N
            icon32 = loadImage("/org/netbeans/core/resources/processExecutor32.gif"); // NOI18N
        }

        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return icon;
        else
            return icon32;
    }

    public static class EnvVarEd extends StringArrayEditor {

        public String getAsText () {
            if (getValue () == null)
                return "null"; // NOI18N
            else
                return super.getAsText ();
        }

        public void setAsText (String text) {
            if ("null".equals (text)) // NOI18N
                setValue (null);
            else
                super.setAsText (text);
        }

    }

    public static class WorkDirEd extends DirectoryOnlyEditor {

        public String getAsText () {
            if (getValue () == null)
                return "null"; // NOI18N
            else
                return super.getAsText ();
        }

        public void setAsText (String text) {
            if ("null".equals (text)) // NOI18N
                setValue (null);
            else
                super.setAsText (text);
        }

    }

}


/*
 * Log
 *  11   Gandalf   1.10        1/13/00  Jaroslav Tulach I18N
 *  10   Gandalf   1.9         12/21/99 Jesse Glick     External executors can 
 *       set envvars and (on 1.3) cwd.
 *  9    Gandalf   1.8         11/11/99 Jesse Glick     Display miscellany.
 *  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         10/7/99  Ales Novak      new names for execution 
 *       types
 *  6    Gandalf   1.5         10/1/99  Jesse Glick     Cleanup of service type 
 *       name presentation.
 *  5    Gandalf   1.4         9/10/99  Jaroslav Tulach Bean infos for services.
 *  4    Gandalf   1.3         7/24/99  Ian Formanek    Printing stack trace on 
 *       netbeans.debug.exceptions property only
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/30/99  Jaroslav Tulach ClassPath settings 
 *       marked as expert.
 *  1    Gandalf   1.0         5/27/99  Jaroslav Tulach 
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach added hidden property
 */
