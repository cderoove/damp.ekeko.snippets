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
import java.awt.Toolkit;
import java.beans.SimpleBeanInfo;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.BeanDescriptor;
/**
*
* @author Ales Novak
*/
public class JavaSettingsBeanInfo extends SimpleBeanInfo {

    /** icon */
    private static Image icon;
    /** icon32 */
    private static Image icon32;

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[] {
                       new PropertyDescriptor (JavaSettings.PROP_REPLACEABLE_STRINGS_TABLE, JavaSettings.class),
                       new PropertyDescriptor (JavaSettings.PROP_AUTO_PARSING_DELAY, JavaSettings.class),
                       new PropertyDescriptor (JavaSettings.PROP_PERFECT_RECOGNITION, JavaSettings.class),
                       new PropertyDescriptor (JavaSettings.PROP_COMPILER, JavaSettings.class),
                       new PropertyDescriptor (JavaSettings.PROP_DEBUGGER, JavaSettings.class),
                       new PropertyDescriptor (JavaSettings.PROP_EXECUTOR, JavaSettings.class)
                   };

            desc[0].setDisplayName (JavaSettings.getString("PROP_REPLACEABLE_STRINGS"));
            desc[0].setShortDescription (JavaSettings.getString("HINT_REPLACEABLE_STRINGS"));
            desc[1].setDisplayName (JavaSettings.getString("PROP_AUTO_PARSING_DELAY"));
            desc[1].setShortDescription (JavaSettings.getString("HINT_AUTO_PARSING_DELAY"));
            desc[2].setDisplayName (JavaSettings.getString("PROP_PERFECT_RECOGNITION"));
            desc[2].setShortDescription (JavaSettings.getString("HINT_PERFECT_RECOGNITION"));
            desc[3].setDisplayName (JavaSettings.getString("PROP_COMPILER"));
            desc[3].setShortDescription (JavaSettings.getString("HINT_COMPILER"));
            desc[4].setDisplayName (JavaSettings.getString("PROP_DEBUGGER"));
            desc[4].setShortDescription (JavaSettings.getString("HINT_DEBUGGER"));
            desc[5].setDisplayName (JavaSettings.getString("PROP_EXECUTOR"));
            desc[5].setShortDescription (JavaSettings.getString("HINT_EXECUTOR"));
        } catch (IntrospectionException ex) {
            throw new InternalError ();
        }
    }

    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor desc = new BeanDescriptor(JavaSettings.class);
        desc.setDisplayName(JavaSettings.getString("CTL_Java_option"));
        /* for POST-FCS desc.setShortDescription(JavaSettings.getString("HINT_Java_option")); */
        return desc;
    }


    /** Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        return desc;
    }

    /**
    * Claim there are no icons available.  You can override
    * this if you want to provide icons for your bean.
    */
    public Image getIcon(int type) {
        if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) || (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
            if (icon == null) {
                icon = Toolkit.getDefaultToolkit().getImage(getClass ().getResource("/org/netbeans/modules/java/resources/class.gif")); // NOI18N
            }
            return icon;
        } else { // 32
            if (icon32 == null) {
                icon32 = Toolkit.getDefaultToolkit().getImage(getClass ().getResource("/org/netbeans/modules/java/resources/class32.gif")); // NOI18N
            }
            return icon32;
        }
    }
}

/*
 * Log
 *  13   Gandalf-post-FCS1.10.1.1    3/16/00  Svatopluk Dedic 
 *  12   Gandalf-post-FCS1.10.1.0    3/15/00  Svatopluk Dedic Fixed hints
 *  11   src-jtulach1.10        1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  10   src-jtulach1.9         12/20/99 Ales Novak      JavaDO has new 
 *       properties - default executor and default debugger
 *  9    src-jtulach1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    src-jtulach1.7         10/8/99  Ales Novak      makeDefaultAction works 
 *       for Java compilers
 *  7    src-jtulach1.6         10/6/99  Ales Novak      #4230
 *  6    src-jtulach1.5         9/29/99  Ales Novak      CompilerType used
 *  5    src-jtulach1.4         8/12/99  Ales Novak      class files could be 
 *       'perfectly'  examined about their source file
 *  4    src-jtulach1.3         4/15/99  Petr Hamernik   parser improvements
 *  3    src-jtulach1.2         4/6/99   Ian Formanek    fixed obtaining 
 *       resources (Object.class.getResource -> getClass ().getResource)
 *  2    src-jtulach1.1         4/1/99   Petr Hamernik   
 *  1    src-jtulach1.0         3/31/99  Ales Novak      
 * $
 */
