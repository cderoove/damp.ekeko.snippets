/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jini.settings;

import java.util.*;

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;

import org.netbeans.modules.jini.*;

/** Options for something or other.
 *
 * @author  pkuzel
 */
public class JiniSettings extends SystemOption {

    static final long serialVersionUID = 1;

    // No constructor please!

    // Properties must obey SystemOptions rule: be static

    /** Where to browse? 
     * @associates Object*/
    private static HashSet targets = new HashSet();

    /** Holds value of property eventLimit. */
    private static int eventLimit = 20;

    public String displayName () {
        return "Jini Settings";
    }

    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
        // If you provide context help then use:
        // return new HelpCtx (JiniSettingsSettings.class);
    }

    /** Default instance of this system option, for the convenience of associated classes. */
    static final public JiniSettings DEFAULT = (JiniSettings) findObject (JiniSettings.class, true);

    /** Getter for property groups.
     * @return Value of property groups.
     */
    public HashSet getTargets() {
        return targets;
    }

    /** Setter for property groups.
     * @param groups New value of property groups.
     */
    public void setTargets(HashSet targets) {
        HashSet old = this.targets;
        this.targets = targets;
        firePropertyChange ("targets", old, targets);
    }

    public void addTarget(Object key) {
        targets.add(key);
        firePropertyChange ("targets", null, targets);
    }

    public void removeTarget(Object key) {
        targets.remove(key);
        firePropertyChange ("targets", null, targets);
    }

    /** Getter for property eventLimit.
     * @return Value of property eventLimit.
     */
    public int getEventLimit() {
        return eventLimit;
    }

    /** Setter for property eventLimit.
     * @param eventLimit New value of property eventLimit.
     */
    public void setEventLimit(int eventLimit) {
        int oldEventLimit = this.eventLimit;
        if (eventLimit < 0)
            eventLimit = 0 - eventLimit;
        this.eventLimit = eventLimit;
        firePropertyChange ("eventLimit", new Integer (oldEventLimit), new Integer (eventLimit));
    }
}


/*
* <<Log>>
*  2    Gandalf   1.1         2/14/00  Petr Kuzel      
*  1    Gandalf   1.0         2/2/00   Petr Kuzel      
* $ 
*/ 

