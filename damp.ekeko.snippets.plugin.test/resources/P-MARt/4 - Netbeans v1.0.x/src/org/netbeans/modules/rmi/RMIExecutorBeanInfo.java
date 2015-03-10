/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.rmi;

import java.beans.*;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

import org.openide.execution.*;

/** Object that provides beaninfo for {@link RMIExecutor}.
*
* @author Jaroslav Tulach, Martin Ryzl
*/
public class RMIExecutorBeanInfo extends SimpleBeanInfo {

    /** Bean descriptor. */
    private static BeanDescriptor descr;

    /** Property descriptors. */
    private static PropertyDescriptor[] prop;

    static {
        try {
            descr = new BeanDescriptor (RMIExecutor.class);
            ResourceBundle bundle = NbBundle.getBundle(RMIExecutorBeanInfo.class);

            descr.setName (bundle.getString("LAB_RMIExecutor")); // NOI18N

            prop = new PropertyDescriptor[] {
                       new PropertyDescriptor("hostname", RMIExecutor.class, "getHostname", null), // NOI18N
                       new PropertyDescriptor("hostIP", RMIExecutor.class, "getHostIP", null), // NOI18N
                       new PropertyDescriptor("internalHttpPort", RMIExecutor.class, "getInternalHttpPort", null), // NOI18N
                       new PropertyDescriptor("repositoryURL", RMIExecutor.class, "getRepositoryURL", null), // NOI18N
                       new PropertyDescriptor("repositoryIPURL", RMIExecutor.class, "getRepositoryIPURL", null), // NOI18N
                   };
            prop[0].setDisplayName(getString("PROP_hostname")); // NOI18N
            prop[0].setShortDescription(getString("HINT_hostname")); // NOI18N
            prop[1].setDisplayName(getString("PROP_hostIP")); // NOI18N
            prop[1].setShortDescription(getString("HINT_hostIP")); // NOI18N
            prop[2].setDisplayName(getString("PROP_internalHttpPort")); // NOI18N
            prop[2].setShortDescription(getString("HINT_internalHttpPort")); // NOI18N
            prop[3].setDisplayName(getString("PROP_repositoryURL")); // NOI18N
            prop[3].setShortDescription(getString("HINT_repositoryURL")); // NOI18N
            prop[4].setDisplayName(getString("PROP_repositoryIPURL")); // NOI18N
            prop[4].setShortDescription(getString("HINT_repositoryIPURL")); // NOI18N

            for(int i = 0; i < prop.length; i++) {
                prop[i].setExpert(true);
            }
        } catch (IntrospectionException ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
        }
    }

    /** Get bean descriptor.
    * @return a bean descriptor
    */
    public BeanDescriptor getBeanDescriptor () {
        return descr;
    }

    /** Get property descriptors.
    * @return an array of property descriptors.
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        return prop;
    }

    /** Get additional bean info.
    * @return an array of BeanInfos
    */
    public BeanInfo[] getAdditionalBeanInfo() {
        try {
            return new BeanInfo[] {
                       Introspector.getBeanInfo(ProcessExecutor.class)
                   };
        } catch (IntrospectionException ex) {
            return new BeanInfo[] {};
        }
    }

    /**
     * This method returns an image object that can be used to
     * represent the bean in toolboxes, toolbars, etc.   Icon images
     * will typically be GIFs, but may in future include other formats.
     * <p>
     * Beans aren't required to provide icons and may return null from
     * this method.
     * <p>
     * There are four possible flavors of icons (16x16 color,
     * 32x32 color, 16x16 mono, 32x32 mono).  If a bean choses to only
     * support a single icon we recommend supporting 16x16 color.
     * <p>
     * We recommend that icons have a "transparent" background
     * so they can be rendered onto an existing background.
     *
     * @param  iconKind  The kind of icon requested.  This should be
     *    one of the constant values ICON_COLOR_16x16, ICON_COLOR_32x32, 
     *    ICON_MONO_16x16, or ICON_MONO_32x32.
     * @return  An image object representing the requested icon.  May
     *    return null if no suitable icon is available.
     */
    public java.awt.Image getIcon(int iconKind) {
        return loadImage("/org/netbeans/modules/rmi/resources/RMIExecutor.gif"); // NOI18N
    }

    /** Get localized string.
    * @param key a key that should be localized.
    * @return a localized string.
    */
    private static String getString(String key) {
        return NbBundle.getBundle(RMIExecutorBeanInfo.class).getString(key);
    }
}


/*
 * <<Log>>
 *  6    Gandalf-post-FCS1.4.1.0     3/20/00  Martin Ryzl     localization
 *  5    Gandalf   1.4         1/21/00  Martin Ryzl     repository changed to 
 *       filesystems
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         10/7/99  Martin Ryzl     icon added
 *  2    Gandalf   1.1         8/18/99  Martin Ryzl     corrected localization
 *  1    Gandalf   1.0         7/12/99  Martin Ryzl     
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach added hidden property
 */
