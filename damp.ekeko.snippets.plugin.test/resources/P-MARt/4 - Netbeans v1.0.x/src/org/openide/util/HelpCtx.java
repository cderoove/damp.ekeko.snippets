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

package org.openide.util;

import java.net.URL;
import javax.swing.JComponent;

import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/** Provides help for any window or other feature in the system.
* It is designed to be JavaHelp-compatible and to use the same tactics when
* assigning help to {@link JComponent} instances.
*
* @author Petr Hamernik, Jaroslav Tulach
*/
public final class HelpCtx extends Object {

    // JST: I do not want to have every class deprecated!
    //     * @deprecated Please give a specific help page instead.

    /** Default help page.
    * This (hopefully) points to a note explaining to the user that no help is available.
    * Precisely, the Help ID is set to <code>org.openide.util.HelpCtx.DEFAULT_HELP</code>.
    */
    public final static HelpCtx DEFAULT_HELP = new HelpCtx (HelpCtx.class.getName () + ".DEFAULT_HELP"); // NOI18N

    /** URL of the help page */
    private URL helpCtx;

    /** JavaHelp ID for the help */
    private String helpID;

    /** Create a help context by URL.
    * @param helpCtx URL to point help to
    */
    public HelpCtx(URL helpCtx) {
        this.helpCtx = helpCtx;
    }

    /** Create a help context by tag (preferred technique).
    * You must provide an ID of the
    * desired help for the item. The ID should refer to an
    * already installed help; this can be easily installed by specifying
    * a JavaHelp HelpSet file in the module manifest tag <code>OpenIDE-Module-Help</code>.
    *
    * @param helpID the JavaHelp ID of the help
    */
    public HelpCtx(String helpID) {
        this.helpID = helpID;
    }

    /** Create a help context by class.
    * Assigns the name of a class as
    * the ID.
    *
    * @param clazz the class to take the name from
    */
    public HelpCtx (Class clazz) {
        this (clazz.getName ());
    }

    /** Get a URL to the help page, if applicable.
    * @return a URL to the page, or <code>null</code> if the target was specified by ID
    */
    public URL getHelp () {
        return helpCtx;
    }

    /** Get the ID of the help page, if applicable.
    * @return the JavaHelp ID string, or <code>null</code> if specified by URL
    */
    public String getHelpID () {
        return helpID;
    }

    // object identity

    public int hashCode () {
        int base = HelpCtx.class.hashCode ();
        if (helpCtx != null) base ^= helpCtx.hashCode ();
        if (helpID != null) base ^= helpID.hashCode ();
        return base;
    }

    public boolean equals (Object o) {
        if (o == null || ! (o instanceof HelpCtx))
            return false;
        HelpCtx oo = (HelpCtx) o;
        return Utilities.compareObjects (helpCtx, oo.helpCtx) && Utilities.compareObjects (helpID, oo.helpID);
    }

    public String toString () {
        return "org.openide.util.HelpCtx[helpID=" + helpID + ",helpCtx=" + helpCtx + "]"; // NOI18N
    }

    /** Set the help ID for a component.
    * @param comp the visual component to associate help to
    * @param helpID help ID, or <code>null</code> if the help ID should be removed
    */
    public static void setHelpIDString (JComponent comp, String helpID) {
        comp.putClientProperty("HelpID", helpID); // NOI18N
    }

    /** Find the help ID for a component.
    * If the component is a {@link TopComponent}, then {@link TopComponent#getHelp} is called.
    * If the component has help attached by {@link #setHelpIDString}, it returns that.
    * Otherwise it checks the parent component recursively.
    *
    * @param comp the component to find help for
    * @return the help for that component (never <code>null</code>)
    */
    public static HelpCtx findHelp (java.awt.Component comp) {
        while (comp != null) {
            // System.err.println ("Considering component " + comp + " " + comp.getName ());
            if (comp instanceof TopComponent) {
                HelpCtx help = ((TopComponent) comp).getHelpCtx ();
                if (help != null) {
                    // System.err.println ("TC help: " + help);
                    return help;
                }
            } else if (comp instanceof JComponent) {
                String hid = (String) ((JComponent) comp).getClientProperty("HelpID"); // NOI18N
                if (hid != null) {
                    // System.err.println ("JC help: " + hid);
                    return new HelpCtx (hid);
                }
            }

            comp = comp.getParent ();
        }
        // System.err.println ("None found...");
        return DEFAULT_HELP;
    }

}

/*
 * Log
 *  15   Gandalf   1.14        1/12/00  Pavel Buzek     I18N
 *  14   Gandalf   1.13        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  13   Gandalf   1.12        7/11/99  David Simonek   window system change...
 *  12   Gandalf   1.11        7/2/99   Jesse Glick     Bugfix--findHelp did not
 *       always work.
 *  11   Gandalf   1.10        6/24/99  Jesse Glick     toString() added, and 
 *       fixed bug in findHelp().
 *  10   Gandalf   1.9         6/24/99  Jesse Glick     Object identity stuff.
 *  9    Gandalf   1.8         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  8    Gandalf   1.7         6/9/99   Ian Formanek    manifest tags changed to
 *       NetBeans-
 *  7    Gandalf   1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         5/31/99  Jaroslav Tulach External Execution & 
 *       Compilation
 *  5    Gandalf   1.4         4/27/99  Jesse Glick     new HelpCtx () -> 
 *       HelpCtx.DEFAULT_HELP.
 *  4    Gandalf   1.3         4/26/99  Jesse Glick     [JavaDoc]
 *  3    Gandalf   1.2         3/16/99  Jaroslav Tulach 
 *  2    Gandalf   1.1         3/16/99  Jaroslav Tulach JavaHelp like help 
 *       system
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.35        --/--/98 Jan Formanek    default URL changed to pending.html
 */
