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

import java.beans.*;

import org.openide.util.NbBundle;

/** Dummy wrapper class. */
public abstract class FileSystemCapability {

    private FileSystemCapability () {}

    /** BeanInfo for {@link FileSystemCapability.Bean}. */
    public static class BeanBeanInfo extends SimpleBeanInfo {

        public PropertyDescriptor[] getPropertyDescriptors () {
            try {
                PropertyDescriptor compile = new PropertyDescriptor ("compile", org.openide.filesystems.FileSystemCapability.Bean.class); // NOI18N
                compile.setDisplayName (NbBundle.getBundle (FileSystemCapability.class).getString ("PROP_FSCB_compile"));
                compile.setShortDescription (NbBundle.getBundle (FileSystemCapability.class).getString ("HINT_FSCB_compile"));
                PropertyDescriptor debug = new PropertyDescriptor ("debug", org.openide.filesystems.FileSystemCapability.Bean.class); // NOI18N
                debug.setDisplayName (NbBundle.getBundle (FileSystemCapability.class).getString ("PROP_FSCB_debug"));
                debug.setShortDescription (NbBundle.getBundle (FileSystemCapability.class).getString ("HINT_FSCB_debug"));
                PropertyDescriptor doc = new PropertyDescriptor ("doc", org.openide.filesystems.FileSystemCapability.Bean.class); // NOI18N
                doc.setDisplayName (NbBundle.getBundle (FileSystemCapability.class).getString ("PROP_FSCB_doc"));
                doc.setShortDescription (NbBundle.getBundle (FileSystemCapability.class).getString ("HINT_FSCB_doc"));
                PropertyDescriptor execute = new PropertyDescriptor ("execute", org.openide.filesystems.FileSystemCapability.Bean.class); // NOI18N
                execute.setDisplayName (NbBundle.getBundle (FileSystemCapability.class).getString ("PROP_FSCB_execute"));
                execute.setShortDescription (NbBundle.getBundle (FileSystemCapability.class).getString ("HINT_FSCB_execute"));
                return new PropertyDescriptor[] { compile, debug, doc, execute };
            } catch (IntrospectionException ie) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                    ie.printStackTrace ();
                return null;
            }
        }

    }

}

/*
 * Log
 * $
 */
