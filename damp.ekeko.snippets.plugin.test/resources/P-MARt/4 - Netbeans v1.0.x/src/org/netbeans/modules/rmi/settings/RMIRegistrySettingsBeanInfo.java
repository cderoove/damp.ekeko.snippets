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

package org.netbeans.modules.rmi.settings;

import java.beans.*;

public class RMIRegistrySettingsBeanInfo extends SimpleBeanInfo {

    // "" - Localized.

    /** Resource bundle. */
    public static final java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle(RMIRegistrySettingsBeanInfo.class);

    // Property identifiers //GEN-FIRST:Properties
    private static final int PROPERTY_externalRegistry = 0;
    private static final int PROPERTY_regs = 1;
    private static final int PROPERTY_internalRegistryPort = 2;
    private static final int PROPERTY_refreshTime = 3;

    // Property array
    private static PropertyDescriptor[] properties = new PropertyDescriptor[4];

    static {
        try {
            properties[PROPERTY_externalRegistry] = new PropertyDescriptor ( "externalRegistry", RMIRegistrySettings.class, "getExternalRegistry", "setExternalRegistry" );
            properties[PROPERTY_externalRegistry].setHidden ( true );
            properties[PROPERTY_regs] = new PropertyDescriptor ( "regs", RMIRegistrySettings.class, "getRegs", "setRegs" );
            properties[PROPERTY_regs].setHidden ( true );
            properties[PROPERTY_internalRegistryPort] = new PropertyDescriptor ( "internalRegistryPort", RMIRegistrySettings.class, "getInternalRegistryPort", "setInternalRegistryPort" );
            properties[PROPERTY_internalRegistryPort].setDisplayName ( bundle.getString("PROP_internaRegistryPort") );
            properties[PROPERTY_internalRegistryPort].setShortDescription ( bundle.getString("HINT_internaRegistryPort") );
            properties[PROPERTY_internalRegistryPort].setPropertyEditorClass(RMIRegistrySettings.IRPPropertyEditor.class);
            properties[PROPERTY_refreshTime] = new PropertyDescriptor ( "refreshTime", RMIRegistrySettings.class, "getRefreshTime", "setRefreshTime" );
            properties[PROPERTY_refreshTime].setDisplayName ( bundle.getString("PROP_REFRESH_TIME") );
            properties[PROPERTY_refreshTime].setShortDescription ( bundle.getString("HINT_REFRESH_TIME") );
        }
        catch( IntrospectionException e) {}//GEN-HEADEREND:Properties

        // Here you can add code for customizing the properties array.

    }//GEN-LAST:Properties

    // EventSet identifiers//GEN-FIRST:Events
    private static final int EVENT_propertyChangeListener = 0;

    // EventSet array
    private static EventSetDescriptor[] eventSets = new EventSetDescriptor[1];

    static {
        try {
            eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( RMIRegistrySettings.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[0], "addPropertyChangeListener", "removePropertyChangeListener" );
        }
        catch( IntrospectionException e) {}//GEN-HEADEREND:Events

        // Here you can add code for customizing the event sets array.

    }//GEN-LAST:Events

    private static java.awt.Image iconColor16 = null; //GEN-BEGIN:IconsDef
    private static java.awt.Image iconColor32 = null;
    private static java.awt.Image iconMono16 = null;
    private static java.awt.Image iconMono32 = null; //GEN-END:IconsDef
    private static String iconNameC16 = "/org/netbeans/modules/rmi/resources/rmiSettings.gif";//GEN-BEGIN:Icons
    private static String iconNameC32 = "/org/netbeans/modules/rmi/resources/rmiSettings32.gif";
    private static String iconNameM16 = null;
    private static String iconNameM32 = null;//GEN-END:Icons

    private static int defaultPropertyIndex = -1;//GEN-BEGIN:Idx
    private static int defaultEventIndex = -1;//GEN-END:Idx


    /**
     * Gets the beans <code>PropertyDescriptor</code>s.
     * 
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by this bean.  May return null if the
     * information should be obtained by automatic analysis.
     * <p>
     * If a property is indexed, then its entry in the result array will
     * belong to the IndexedPropertyDescriptor subclass of PropertyDescriptor.
     * A client of getPropertyDescriptors can use "instanceof" to check
     * if a given PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        return properties;
    }

    /**
     * Gets the beans <code>EventSetDescriptor</code>s.
     * 
     * @return  An array of EventSetDescriptors describing the kinds of 
     * events fired by this bean.  May return null if the information
     * should be obtained by automatic analysis.
     */
    public EventSetDescriptor[] getEventSetDescriptors() {
        return eventSets;
    }

    /**
     * A bean may have a "default" property that is the property that will
     * mostly commonly be initially chosen for update by human's who are 
     * customizing the bean.
     * @return  Index of default property in the PropertyDescriptor array
     * 		returned by getPropertyDescriptors.
     * <P>	Returns -1 if there is no default property.
     */
    public int getDefaultPropertyIndex() {
        return defaultPropertyIndex;
    }

    /**
     * A bean may have a "default" event that is the event that will
     * mostly commonly be used by human's when using the bean. 
     * @return Index of default event in the EventSetDescriptor array
     *		returned by getEventSetDescriptors.
     * <P>	Returns -1 if there is no default event.
     */
    public int getDefaultEventIndex() {
        return defaultPropertyIndex;
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
        switch ( iconKind ) {
        case ICON_COLOR_16x16:
            if ( iconNameC16 == null )
                return null;
            else {
                if( iconColor16 == null )
                    iconColor16 = loadImage( iconNameC16 );
                return iconColor16;
            }
        case ICON_COLOR_32x32:
            if ( iconNameC32 == null )
                return null;
            else {
                if( iconColor32 == null )
                    iconColor32 = loadImage( iconNameC32 );
                return iconColor32;
            }
        case ICON_MONO_16x16:
            if ( iconNameM16 == null )
                return null;
            else {
                if( iconMono16 == null )
                    iconMono16 = loadImage( iconNameM16 );
                return iconMono16;
            }
        case ICON_MONO_32x32:
            if ( iconNameM32 == null )
                return null;
            else {
                if( iconNameM32 == null )
                    iconMono32 = loadImage( iconNameM32 );
                return iconMono32;
            }
        }
        return null;
    }
}

/*
 * <<Log>>
 *  8    Gandalf-post-FCS1.5.1.1     3/20/00  Martin Ryzl     localization
 *  7    Gandalf-post-FCS1.5.1.0     3/2/00   Martin Ryzl     local registry control 
 *       added
 *  6    Gandalf   1.5         10/27/99 Martin Ryzl     bug fix #4393
 *  5    Gandalf   1.4         10/25/99 Martin Ryzl     refresh timeout property
 *       added
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         10/12/99 Martin Ryzl     
 *  2    Gandalf   1.1         10/7/99  Martin Ryzl     completely rewritten
 *  1    Gandalf   1.0         8/31/99  Martin Ryzl     
 * $
 */
