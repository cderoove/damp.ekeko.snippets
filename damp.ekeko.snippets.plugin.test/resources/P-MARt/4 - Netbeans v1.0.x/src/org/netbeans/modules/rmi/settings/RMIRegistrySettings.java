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

import java.io.*;
import java.util.*;

import org.openide.execution.*;
import org.openide.options.*;
import org.openide.util.*;

/**
 *
 * @author  mryzl
 */

public class RMIRegistrySettings extends SystemOption {

    /** Serial version UID. */
    static final long serialVersionUID = -5414182127521791783L;

    /** Name of the property refresh time. */
    public static final String PROP_REFRESH_TIME = "refreshTime"; // NOI18N
    /** Name of the property internal registry port. */
    public static final String PROP_INTERNAL_REGISTRY_PORT = "internalRegistryPort"; // NOI18N
    /** Name of the property regs. */
    public static final String PROP_REGS = "regs"; // NOI18N

    /** Default time between thwo registry updates. */
    public static final int DEFAULT_REFRESH_TIME = 60000;
    /** No registry. */
    public static final int REGISTRY_NONE = -1;


    /** Holds value of property regs. */
    private static Set regs;
    /** Holds value of property externalRegistry. */
    private static NbProcessDescriptor externalRegistry;
    /** Holds value of property internalRegistryPort. */
    private static int internalRegistryPort = REGISTRY_NONE;
    /** Holds value of property refreshTIme. */
    private static int refreshTime = DEFAULT_REFRESH_TIME;

    /** Internal registry. */
    private static java.rmi.registry.Registry registry;

    static {
        char separator = java.io.File.separatorChar;
        String process = System.getProperty("java.home") + separator + "bin" + separator + "rmiregistry"; // NOI18N
        externalRegistry = new NbProcessDescriptor(
                               process,
                               "", // NOI18N
                               NbBundle.getBundle(RMIRegistrySettings.class).getString("FMT_ExternalRegistryInfo") // NOI18N
                           );
        regs = new HashSet();
    }

    /** Creates new RMIRegistrySettings. */
    public RMIRegistrySettings() {
        // don't use setXXX here !!!
    }

    /** Get a human presentable name of the action.
    * This may be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String displayName () {
        return NbBundle.getBundle(RMIRegistrySettings.class).getString("PROP_RegistrySettingsName"); // NOI18N
    }

    /** Get a help context for the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(RMIRegistrySettings.class);
    }

    /** Getter for property regs.
     *@return Value of property regs.
     */
    public Set getRegs() {
        return regs;
    }

    /** Setter for property regs.
     *@param regs New value of property regs.
     */
    public void setRegs(Set regs) {
        Set old = this.regs;
        this.regs = regs;
        firePropertyChange(PROP_REGS, old, regs);
    }

    /** Getter for property externalRegistry.
     *@return Value of property externalRegistry.
     */
    public NbProcessDescriptor getExternalRegistry() {
        return externalRegistry;
    }

    /** Setter for property externalRegistry.
     * @param externalRegistry New value of property externalRegistry.
     */
    public void setExternalRegistry(NbProcessDescriptor externalRegistry) {
        this.externalRegistry = externalRegistry;
    }

    /** Getter for property refreshTime.
     * @return Value of property refreshTime.
     */
    public int getRefreshTime() {
        return refreshTime;
    }

    /** Setter for property refreshTime.
     * @param refreshTIme New value of property refreshTime.
     */
    public void setRefreshTime(int refreshTime) {
        int oldRefreshTime = this.refreshTime;
        this.refreshTime = refreshTime;
        firePropertyChange (PROP_REFRESH_TIME, new Integer (oldRefreshTime), new Integer (refreshTime));
    }

    /** Getter for property internalRegistryPort.
     * @return Value of property internalRegistryPort.
     */
    public int getInternalRegistryPort() {
        return internalRegistryPort;
    }

    /** Setter for property internalRegistryPort.
     * @param internalRegistryPort New value of property internalRegistryPort.
     */
    public void setInternalRegistryPort(int internalRegistryPort) {
        int oldInternalRegistryPort = this.internalRegistryPort ;

        if (oldInternalRegistryPort  != internalRegistryPort) {

            // stop existing registry
            try {
                stopRegistry(registry);
            } catch (IOException ex) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
            }

            // clear both values
            registry = null;
            this.internalRegistryPort = REGISTRY_NONE;

            // start registry on new port
            if (internalRegistryPort != REGISTRY_NONE) {
                try {
                    startRegistry(internalRegistryPort);
                    // now the value is properly set and an event is fired => exit
                    return;
                } catch (IOException ex) {
                    if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
                }
            }

            if (oldInternalRegistryPort != this.internalRegistryPort) {
                firePropertyChange(PROP_INTERNAL_REGISTRY_PORT, new Integer(oldInternalRegistryPort), new Integer(this.internalRegistryPort));
            }
        }
    }

    /** Start registry. Don't stop the current version, throw an exception instead.
    * @param internalRegistryPort port value. 
    */
    public void startRegistry(int internalRegistryPort) throws IOException {
        int oldInternalRegistryPort = RMIRegistrySettings.internalRegistryPort;

        if (oldInternalRegistryPort != internalRegistryPort) {
            registry = java.rmi.registry.LocateRegistry.createRegistry(internalRegistryPort);
            RMIRegistrySettings.internalRegistryPort = internalRegistryPort;
            firePropertyChange(PROP_INTERNAL_REGISTRY_PORT, new Integer(oldInternalRegistryPort), new Integer(internalRegistryPort));
        }
    }

    /** Stop registry.
    */
    public static void stopRegistry(java.rmi.registry.Registry registry) throws IOException {
        if (registry != null) {
            sun.rmi.transport.ObjectTable.unexportObject(registry, true);
            registry = null;
        }
    }

    // -- Inner classes. --

    /** Property editor for internalRegistryPort
    */
    public static class IRPPropertyEditor extends java.beans.PropertyEditorSupport implements org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor {

        public static final String REGISTRY_NONE_STRING = NbBundle.getBundle(RMIRegistrySettings.class).getString("LAB_RegistryNone");

        /** Array of tags
        */
        private static String[] tags = {
            REGISTRY_NONE_STRING,
            Integer.toString(java.rmi.registry.Registry.REGISTRY_PORT)
        };

        /** @return text for the current value */
        public String getAsText () {
            if (((Integer) getValue()).intValue() == REGISTRY_NONE)
                return REGISTRY_NONE_STRING;
            else
                return getValue().toString();
        }

        /** @param text A text for the current value. */
        public void setAsText (String text) {
            if (text.equals(REGISTRY_NONE_STRING)) {
                setValue(new Integer(REGISTRY_NONE));
            } else {
                try {
                    setValue(new Integer(text));
                } catch (NumberFormatException ex) {
                    setValue(new Integer(REGISTRY_NONE));
                }
            }
        }

        /**
        * @return true if this PropertyEditor provides a enhanced in-place custom 
        *              property editor, false otherwise
        */
        public boolean hasInPlaceCustomEditor () {
            return true;
        }

        /** In place custom editor.
        */
        public java.awt.Component getInPlaceCustomEditor () {
            final javax.swing.JComboBox eventBox = new javax.swing.JComboBox ();
            eventBox.setEditable(true);
            for (int i = 0; i < tags.length; i++) {
                eventBox.addItem(tags[i]);
            }
            eventBox.setSelectedItem(getAsText ());
            eventBox.addActionListener (new java.awt.event.ActionListener () {
                                            public void actionPerformed (java.awt.event.ActionEvent e) {
                                                setAsText ((String) eventBox.getEditor().getItem());
                                            }
                                        }
                                       );
            return eventBox;
        }

        /**
        * @return true if this property editor provides tagged values and
        * a custom strings in the choice should be accepted too, false otherwise
        */
        public boolean supportsEditingTaggedValues () {
            return false;
        }
    }
}

/*
* <<Log>>
*  8    Gandalf-post-FCS1.4.1.2     3/20/00  Martin Ryzl     localization
*  7    Gandalf-post-FCS1.4.1.1     3/10/00  Martin Ryzl     gc of registry improved
*  6    Gandalf-post-FCS1.4.1.0     3/2/00   Martin Ryzl     local registry control 
*       added
*  5    Gandalf   1.4         10/27/99 Martin Ryzl     bug fix #4393
*  4    Gandalf   1.3         10/25/99 Martin Ryzl     refresh timeout property 
*       added
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         10/12/99 Martin Ryzl     
*  1    Gandalf   1.0         8/31/99  Martin Ryzl     
* $ 
*/ 
