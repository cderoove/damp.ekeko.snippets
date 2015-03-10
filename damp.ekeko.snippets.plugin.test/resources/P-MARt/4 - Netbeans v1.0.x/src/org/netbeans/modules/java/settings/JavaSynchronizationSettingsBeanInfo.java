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

package org.netbeans.modules.java.settings;

import java.awt.Image;
import java.beans.*;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** BeanInfo for class JavaSynchronizationSettings.
* It describes three properties one with special editor.
*
* @author Petr Hamernik
*/
public class JavaSynchronizationSettingsBeanInfo extends SimpleBeanInfo {

    /** Icons for java synchronization settings objects. */
    Image icon;
    Image icon32;

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor(JavaSynchronizationSettings.PROP_GENERATE_RETURN,
                                              JavaSynchronizationSettings.class, "getGenerateReturn", "setGenerateReturn"), // NOI18N
                       new PropertyDescriptor(JavaSynchronizationSettings.PROP_ENABLED,
                                              JavaSynchronizationSettings.class, "isEnabled", "setEnabled") // NOI18N
                   };
            ResourceBundle bundle = NbBundle.getBundle(JavaSynchronizationSettingsBeanInfo.class);

            desc[0].setDisplayName(bundle.getString("PROP_GENERATE_RETURN"));
            desc[0].setShortDescription(bundle.getString("HINT_GENERATE_RETURN"));
            desc[0].setPropertyEditorClass(RetGenEditor.class);

            desc[1].setDisplayName(bundle.getString("PROP_ENABLED"));
            desc[1].setShortDescription(bundle.getString("HINT_ENABLED"));
        }
        catch (IntrospectionException ex) {
            ex.printStackTrace ();
        }
    }

    /**
    * loads icons
    */
    public JavaSynchronizationSettingsBeanInfo() {
    }

    /** Returns the ExternalCompilerSettings' icon */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null)
                icon = loadImage("/org/netbeans/modules/java/settings/javaSynchronizationSettings.gif"); // NOI18N
            return icon;
        } else {
            if (icon32 == null)
                icon32 = loadImage ("/org/netbeans/modules/java/settings/javaSynchronizationSettings32.gif"); // NOI18N
            return icon32;
        }
    }

    /** Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        return desc;
    }

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor desc = new BeanDescriptor(JavaSettings.class);
        desc.setDisplayName(JavaSettings.getString("CTL_JavaSynchronization_Settings"));
        return desc;
    }

    /** Simple property editor for two-item pulldown. */
    public static class RetGenEditor extends PropertyEditorSupport {

        private static final String[] tags;

        static {
            tags = new String[3];
            ResourceBundle bundle = NbBundle.getBundle(JavaSynchronizationSettingsBeanInfo.class);
            tags[JavaSynchronizationSettings.RETURN_GEN_NOTHING] = bundle.getString("CTL_RETURN_GEN_NOTHING");
            tags[JavaSynchronizationSettings.RETURN_GEN_EXCEPTION] = bundle.getString("CTL_RETURN_GEN_EXCEPTION");
            tags[JavaSynchronizationSettings.RETURN_GEN_NULL] = bundle.getString("CTL_RETURN_GEN_NULL");
        }

        public String[] getTags () {
            return tags;
        }

        public String getAsText () {
            return tags[((Integer) getValue ()).intValue ()];
        }

        public void setAsText (String text) throws IllegalArgumentException {
            for (int i = 0; i < tags.length; i++) {
                if (tags[i].equals (text)) {
                    setValue (new Integer (i));
                    return;
                }
            }
            throw new IllegalArgumentException ();
        }
    }

}

/*
 * Log
 *  7    Gandalf-post-FCS1.5.1.0     3/15/00  Svatopluk Dedic Fixed hints
 *  6    Gandalf   1.5         1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/10/99  Petr Hamernik   redundant properties 
 *       removed.
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         6/2/99   Petr Hamernik   connections of java 
 *       sources
 *  1    Gandalf   1.0         6/1/99   Petr Hamernik   
 * $
 */

