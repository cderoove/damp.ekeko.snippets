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

package org.netbeans.modules.vcs.cmdline;
import java.beans.*;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;
import org.openide.filesystems.*;
import org.netbeans.modules.vcs.util.*;

/** BeanInfo for CommandLineVcsFileSystem.
 * 
 * @author Pavel Buzek
 */

//-------------------------------------------
public class CvsFileSystemBeanInfo extends SimpleBeanInfo {
    private static Debug E=new Debug("CvsFileSystemBeanInfo", true); // NOI18N
    private static Debug D=E;

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    /** Icon for VCS filesystem. */
    private static java.awt.Image icon;
    private static java.awt.Image icon32;

    static {
        PropertyDescriptor rootDirectory=null;
        PropertyDescriptor debug=null;
        PropertyDescriptor lock=null;
        PropertyDescriptor lockPrompt=null;

        try {
            rootDirectory=new PropertyDescriptor
                          ("rootDirectory", CvsFileSystem.class, "getRootDirectory", "setRootDirectory"); // NOI18N
            debug=new PropertyDescriptor
                  ("debug",CvsFileSystem.class,"getDebug","setDebug"); // NOI18N
            lock=new PropertyDescriptor
                 ("lock",CvsFileSystem.class,"isLockFilesOn","setLockFilesOn"); // NOI18N
            lockPrompt=new PropertyDescriptor
                       ("lockPrompt",CvsFileSystem.class,"isPromptForLockOn","setPromptForLockOn"); // NOI18N


            desc = new PropertyDescriptor[] {
                       rootDirectory, debug, lock, lockPrompt
                   };

            ResourceBundle bundle = NbBundle.getBundle
                                    ("org.netbeans.modules.vcs.cmdline.Bundle"); // NOI18N
            rootDirectory.setDisplayName      (bundle.getString("PROP_rootDirectory"));
            rootDirectory.setShortDescription (bundle.getString("HINT_rootDirectory"));
            debug.setDisplayName              (bundle.getString("PROP_debug"));
            debug.setShortDescription         (bundle.getString("HINT_debug"));
            lock.setDisplayName               (bundle.getString("PROP_lock"));
            lock.setShortDescription          (bundle.getString("HINT_lock"));
            lockPrompt.setDisplayName         (bundle.getString("PROP_lockPrompt"));
            lockPrompt.setShortDescription    (bundle.getString("HINT_lockPrompt"));

        } catch (IntrospectionException ex) {
            ex.printStackTrace ();
        }
    }

    /* Provides the VCSFileSystem's icon */
    public java.awt.Image getIcon(int type) {
        if (icon == null) {
            icon = loadImage("/org/netbeans/modules/vcs/cmdline/cvs.gif"); // NOI18N
            icon32 = icon;
        }
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16))
            return icon;
        else
            return icon32;
    }

    /* Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        return desc;
    }


    public BeanDescriptor getBeanDescriptor(){
        D.deb("getBeanDescriptor()"); // NOI18N
        return new BeanDescriptor(CvsFileSystem.class, org.netbeans.modules.vcs.cmdline.CvsCustomizer.class);
    }

}

/*
* Log
*  8    Gandalf-post-FCS1.6.2.0     3/23/00  Martin Entlicher Added possibility to set 
*       locking of files.
*  7    Gandalf   1.6         1/6/00   Martin Entlicher 
*  6    Gandalf   1.5         10/25/99 Pavel Buzek     
*  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         10/12/99 Pavel Buzek     
*  3    Gandalf   1.2         10/8/99  Pavel Buzek     icon changed
*  2    Gandalf   1.1         10/5/99  Pavel Buzek     VCS at least can be 
*       mounted
*  1    Gandalf   1.0         9/30/99  Pavel Buzek     
* $
*/
