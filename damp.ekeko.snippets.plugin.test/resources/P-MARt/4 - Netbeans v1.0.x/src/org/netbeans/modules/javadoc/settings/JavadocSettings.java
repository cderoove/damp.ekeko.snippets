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

package org.netbeans.modules.javadoc.settings;

import java.io.File;

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Options for invoking internal Javadoc
*
* @author Petr Hrebejk
* @version 0.1, Apr 15, 1999
*/
public class JavadocSettings extends SystemOption //implements ViewerConstants
{
    /** generated Serialized Version UID */
    //static final long serialVersionUID = 605615362662343329L;


    /** path to overview file */
    private static File overview;

    /** members to show */
    private static long members = MemberConstants.PROTECTED;

    /** override standard bootclasspath */
    private static String bootclasspath;

    /** extension directories */
    private static String extdirs = ""; // NOI18N

    /** generate JDK 1.1 style documentation*/
    private static boolean style1_1;

    /** detail messages */
    private static boolean verbose;

    /** encoding */
    private static String encoding = ""; // NOI18N

    /** locale */
    private static String locale = ""; // NOI18N

    static final long serialVersionUID =5671560473265010369L;
    /** @return human presentable name */
    public String displayName() {
        return NbBundle.getBundle(JavadocSettings.class).getString("CTL_IntJavadoc_settings");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (JavadocSettings.class);
    }


    /** Getter for members
    */
    public long getMembers() {
        return members;
    }


    /** Setter for members
    */
    public void setMembers( long l ) {
        members = l;
    }

    /** Getter for path to overview file.
    */
    public File getOverview () {
        return overview;
    }

    /** Setter for path to overview file.
    */
    public void setOverview (File s) {
        overview = s;
    }

    /** Getter for bootclasspath
    */
    public String getBootclasspath () {
        return bootclasspath;
    }
    /** Setter for bootclasspath
    */
    public void setBootclasspath (String s) {
        bootclasspath = s;
    }

    /** Getter for extension directories
    */
    public String getExtdirs () {
        return extdirs;
    }
    /** Setter extension directories
    */
    public void setExtdirs (String s) {
        extdirs = s;
    }

    /** Getter for JDK 1.1 Style
    */
    public boolean isStyle1_1 () {
        return style1_1;
    }

    /** Setter for JDK 1.1 Style
    */
    public void setStyle1_1 (boolean b) {
        style1_1 = b;
    }

    /** Getter for verbose mode
    */
    public boolean isVerbose () {
        return verbose;
    }

    /** Setter for verbose mode
    */
    public void setVerbose (boolean b) {
        verbose = b;
    }

    /** Getter for encoding
    */
    public String getEncoding () {
        return encoding;
    }
    /** Setter for encoding
    */
    public void setEncoding (String s) {
        encoding = s;
    }

    /** Getter for locale
    */
    public String getLocale () {
        return locale;
    }
    /** Setter locale
    */
    public void setLocale (String s) {
        locale = s;
    }
}

/*
 * Log
 *  6    Gandalf   1.5         1/12/00  Petr Hrebejk    i18n
 *  5    Gandalf   1.4         11/27/99 Patrik Knakal   
 *  4    Gandalf   1.3         11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         4/23/99  Petr Hrebejk    
 * $
 */
