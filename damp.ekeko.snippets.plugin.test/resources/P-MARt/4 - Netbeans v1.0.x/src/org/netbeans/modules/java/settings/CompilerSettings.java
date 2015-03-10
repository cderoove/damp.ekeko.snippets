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

import java.util.ResourceBundle;
import sun.tools.java.Constants;

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Serves as a source of settings for compiler.
*
* @author Ales Novak
* @version 0.17, May 16, 1998
*/
public class CompilerSettings extends SystemOption implements Constants {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -1426504702124944362L;

    /** bundle to obtain text information from */
    static final ResourceBundle bundle = NbBundle.getBundle(CompilerSettings.class);

    /** compiler flags */
    private static int flag = F_WARNINGS | F_DEBUG_VARS | F_DEBUG_LINES | F_DEBUG_SOURCE;

    /** character encoding */
    private static String charEncoding;

    public String displayName () {
        return bundle.getString ("CTL_Compiler_settings");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (CompilerSettings.class);
    }

    public int flag() {
        return flag;
    }

    private void orFlag(int f) {
        int oldflag = flag;
        flag |= f;
        firePropertyChange("flag", new Integer(oldflag), new Integer(flag)); // NOI18N
    }

    private void andFlag(int f) {
        int oldflag = flag;
        flag &= f;
        firePropertyChange("flag", new Integer(oldflag), new Integer(flag)); // NOI18N
    }

    public void setWarnings(boolean x) {
        boolean old = (flag & F_WARNINGS) != 0 ;
        if (x) {
            if (!old) orFlag(F_WARNINGS);
        }
        else if (old) andFlag(~F_WARNINGS);
        if (x != old)
            firePropertyChange("warnings", new Boolean(old), new Boolean(x)); // NOI18N
    }

    public boolean getWarnings() {
        return (flag & F_WARNINGS) != 0;
    }

    public void setDebug(boolean x) {
        boolean old = (flag & (F_DEBUG_VARS | F_DEBUG_LINES | F_DEBUG_SOURCE)) != 0;
        if (x) {
            if (!old) orFlag((F_DEBUG_VARS | F_DEBUG_LINES | F_DEBUG_SOURCE));
        }
        else if (old) andFlag(~(F_DEBUG_VARS | F_DEBUG_LINES | F_DEBUG_SOURCE));
        if (x != old)
            firePropertyChange("debug", new Boolean(old), new Boolean(x)); // NOI18N
    }

    public boolean  getDebug() {
        return (flag & (F_DEBUG_VARS | F_DEBUG_LINES | F_DEBUG_SOURCE)) != 0;
    }

    public void setOptimize(boolean x) {
        boolean old = (flag & (F_OPT | F_OPT_INTERCLASS)) != 0;
        if (x) {
            if (!old) orFlag((F_OPT | F_OPT_INTERCLASS));
        }
        else if (old) andFlag(~(F_OPT | F_OPT_INTERCLASS));
        if (x != old)
            firePropertyChange("optimize", new Boolean(old), new Boolean(x)); // NOI18N
    }

    public boolean getOptimize () {
        return (flag & (F_OPT | F_OPT_INTERCLASS)) != 0;
    }

    public void setDeprecation(boolean x) {
        boolean old = (flag & F_DEPRECATION) != 0;
        if (x) {
            if (!old) orFlag(F_DEPRECATION);
        }
        else if (old) andFlag(~F_DEPRECATION);
        if (x != old)
            firePropertyChange("deprecation", new Boolean(old), new Boolean(x)); // NOI18N
    }

    public boolean getDeprecation () {
        return  (flag & F_DEPRECATION) != 0;
    }

    public void setDependencies(boolean x) {
        boolean old = (flag & F_DEPENDENCIES) != 0;
        if (x) {
            if (!old) orFlag(F_DEPENDENCIES);
        }
        else if (old) andFlag(~F_DEPENDENCIES);
        if (x != old)
            firePropertyChange("dependencies", new Boolean(old), new Boolean(x)); // NOI18N
    }

    public boolean getDependencies () {
        return  (flag & F_DEPENDENCIES) != 0;
    }

    /** sets new character encoding
    * @param enc is a new encoding
    */
    public void setCharEncoding(String enc) {
        String old = charEncoding;
        charEncoding = enc;
        firePropertyChange("encoding", old, charEncoding); // NOI18N
    }

    /** returns character encoding
    * @return encoding, null is possible encoding
    */
    public String getCharEncoding() {
        return charEncoding;
    }
}

/*
 * Log
 *  6    src-jtulach1.5         1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  5    src-jtulach1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems copyright in file comment
 *  4    src-jtulach1.3         7/2/99   Jesse Glick     More help IDs.
 *  3    src-jtulach1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    src-jtulach1.1         5/6/99   Ales Novak      debug flag added
 *  1    src-jtulach1.0         3/28/99  Ales Novak      
 * $
 * Beta Change History:
 *  0    Tuborg    0.16        --/--/98 Jan Formanek    option name fro CompilerBundle
 *  0    Tuborg    0.17        --/--/98 Jan Formanek    removed implements Serializable
 */
