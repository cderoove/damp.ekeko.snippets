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
import java.io.*;
import java.util.ResourceBundle;
import java.util.Properties;

import org.openide.options.ContextSystemOption;
import org.openide.util.NbBundle;
import org.openide.util.io.ReaderInputStream;

import org.netbeans.modules.rmi.wizard.*;

/** Settings for RMI
*
* @author Ales Novak, Petr Hamernik, Martin Ryzl
*/
public class RMISettings extends ContextSystemOption {

    /** Serial version UID. */
    static final long serialVersionUID = -8522143676848697297L;

    /** Name of the property stubFormat. */
    public static final String PROP_STUB_FORMATS = "stubFormats"; // NOI18N

    /** Name of the property detectRemote. */
    public static final String PROP_DETECT_REMOTE = "detectRemote"; // NOI18N

    /** Name of the property confirmConvert. */
    public static final String PROP_CONFIRM_CONVERT = "confirmConvert"; // NOI18N

    /** Name of the property hideStubs. */
    public static final String PROP_HIDE_STUBS = "hideStubs"; // NOI18N

    /** Default value for stubFormats. */
    public static final String[] DEFAULT_STUB_FORMATS = {"{0}_Stub", "{0}_Skel", "_{0}_Tie", "_{0}_Stub"}; // NOI18N

    /** The resource bundle. */
    public static ResourceBundle bundle;

    /** Holds value of property stubFormats. */
    private static String[] stubFormats = DEFAULT_STUB_FORMATS;

    /** Holds value of property detectRemote. */
    private static boolean detectRemote = true;

    /** Holds value of property confirmConvert. */
    private static boolean confirmConvert = true;

    /** Holds value of property hideStubs. */
    private static boolean hideStubs = true;

    public RMISettings() {
        addOption(getRMIRegistrySettings());
        //    addOption((WizardSettings) WizardSettings.findObject(WizardSettings.class, true));
    }

    /** Display name.
    */
    public String displayName () {
        return getString("CTL_RMI_SETTINGS"); // NOI18N
    }

    /** @return localized string */
    static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(RMISettings.class);
        }
        return bundle.getString(s);
    }

    public static RMIRegistrySettings getRMIRegistrySettings() {
        return (RMIRegistrySettings) RMIRegistrySettings.findObject(RMIRegistrySettings.class, true);
    }

    /** Getter for property stubFormats.
     *@return Value of property stubFormats.
     */
    public String[] getStubFormats() {
        return stubFormats;
    }

    /** Setter for property stubFormats.
     *@param stubFormats New value of property stubFormats.
     */
    public void setStubFormats(String[] stubFormats) {
        String[] oldStubFormats = this.stubFormats;
        this.stubFormats = stubFormats;
        firePropertyChange (PROP_STUB_FORMATS , oldStubFormats ,stubFormats);
    }
    /** Getter for property detectRemote.
     *@return Value of property detectRemote.
     */
    public boolean isDetectRemote() {
        return detectRemote;
    }
    /** Setter for property detectRemote.
     *@param detectsRemote New value of property detectRemote.
     */
    public void setDetectRemote(boolean detectRemote) {
        boolean oldDetectRemote = this.detectRemote;
        this.detectRemote = detectRemote;
        firePropertyChange (PROP_DETECT_REMOTE, new Boolean(oldDetectRemote), new Boolean(detectRemote));
    }
    /** Getter for property confirmConvert.
     *@return Value of property confirmConvert.
     */
    public boolean isConfirmConvert() {
        return confirmConvert;
    }
    /** Setter for property confirmConverting.
     *@param confirmConvert New value of property confirmConvert.
     */
    public void setConfirmConvert(boolean confirmConvert) {
        boolean oldConfirmConvert = this.confirmConvert;
        this.confirmConvert = confirmConvert;
        firePropertyChange (PROP_CONFIRM_CONVERT, new Boolean(oldConfirmConvert), new Boolean(confirmConvert));
    }

    /** Getter for property hideStubs.
     * @return Value of property hideStubs.
     */
    public boolean isHideStubs() {
        return hideStubs;
    }
    /** Setter for property hideStubs.
     * @param hideStubs New value of property hideStubs.
     */
    public void setHideStubs(boolean hideStubs) {
        boolean oldHideStubs = this.hideStubs;
        this.hideStubs = hideStubs;
        firePropertyChange (PROP_HIDE_STUBS, new Boolean (oldHideStubs), new Boolean (hideStubs));
    }
}

/*
 * <<Log>>
 */
