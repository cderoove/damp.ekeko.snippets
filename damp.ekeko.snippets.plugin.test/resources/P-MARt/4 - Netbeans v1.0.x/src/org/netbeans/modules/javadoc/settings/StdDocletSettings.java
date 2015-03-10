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
import org.openide.explorer.propertysheet.editors.StringArrayEditor;

/** Options for invoking internal Javadoc
*
* @author Petr Hrebejk
*/
public class StdDocletSettings extends SystemOption //implements ViewerConstants
{
    /** generated Serialized Version UID */
    //static final long serialVersionUID = 605615362662343329L;

    /** destination directory*/
    private static File directory;

    static {
        String fileSep = System.getProperty ("file.separator");
        try {
            directory = new File (System.getProperty ("netbeans.user") + fileSep + "javadoc").getCanonicalFile();
        }
        catch ( java.io.IOException e ) {
            directory = new File (System.getProperty ("netbeans.user") + fileSep + "javadoc").getAbsoluteFile();
        }
    }

    /** use option */
    private static boolean use;

    /** version option */
    private static boolean version;

    /** author option */
    private static boolean author;

    /** splitindex option */
    private static boolean splitindex;

    /** window title option */
    private static String windowtitle = ""; // NOI18N

    /** doctitle title option */
    private static String doctitle = ""; // NOI18N

    /** header title option */
    private static String header = ""; // NOI18N

    /** footer title option */
    private static String footer = ""; // NOI18N

    /** bottom title option */
    private static String bottom = ""; // NOI18N

    /** link option */
    private static String link = ""; // NOI18N

    /** group option */
    private static String[] group = { "" }; // NOI18N

    /** nodeprecated option */
    private static boolean nodeprecated;

    /** nodeprecatedlist option */
    private static boolean nodeprecatedlist;

    /** notree option */
    private static boolean notree;

    /** noindex option */
    private static boolean noindex;

    /** nohelp option */
    private static boolean nohelp;

    /** nonavbar option */
    private static boolean nonavbar;

    /** helpfile option */
    private static File helpfile = null;

    /** stylesheetfile option */
    private static File stylesheetfile = null;

    /** document charset option */
    private static String charset = ""; // NOI18N


    static final long serialVersionUID =8476913303755577009L;
    /** @return human presentable name */
    public String displayName() {
        return NbBundle.getBundle(StdDocletSettings.class).getString("CTL_StdDoclet_settings");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (StdDocletSettings.class);
    }

    /** Getter for destination directory
    */
    public File getDirectory () {
        return directory;
    }

    /** Setter for destination directory
    */
    public void setDirectory (File s) {
        directory = s;
    }

    /** Getter for use option
     */
    public boolean isUse () {
        return use;
    }

    /** Setter for use option
     */
    public void setUse (boolean b) {
        use = b;
    }

    /** Getter for version option
     */
    public boolean isVersion () {
        return version;
    }

    /** Setter for version option
     */
    public void setVersion (boolean b) {
        version = b;
    }

    /** Getter for author option
     */
    public boolean isAuthor () {
        return author;
    }

    /** Setter for autho option
     */
    public void setAuthor (boolean b) {
        author = b;
    }

    /** Getter for splitindex option
     */
    public boolean isSplitindex () {
        return splitindex;
    }

    /** Setter for splitindex option
     */
    public void setSplitindex (boolean b) {
        splitindex = b;
    }

    /** Getter for windowtitle option
     */
    public String getWindowtitle () {
        return windowtitle;
    }

    /** Setter for windowtitle option
     */
    public void setWindowtitle (String s) {
        windowtitle = s;
    }

    /** Getter for doctitle option
     */
    public String getDoctitle () {
        return doctitle;
    }

    /** Setter for doctitle option
     */
    public void setDoctitle (String s) {
        doctitle = s;
    }

    /** Getter for header option
     */
    public String getHeader () {
        return header;
    }

    /** Setter for header option
     */
    public void setHeader (String s) {
        header = s;
    }

    /** Getter for footer option
     */
    public String getFooter () {
        return footer;
    }

    /** Setter for footer option
     */
    public void setFooter (String s) {
        footer = s;
    }

    /** Getter for bottom option
     */
    public String getBottom () {
        return bottom;
    }

    /** Setter for bottom option
     */
    public void setBottom (String s) {
        bottom = s;
    }

    /** Getter for link option
     */
    public String getLink () {
        return link;
    }

    /** Setter for link option
     */
    public void setLink (String s) {
        link = s;
    }

    /** Getter for group option
     */
    public String[] getGroup () {
        return group;
    }

    /** Setter for group option
     */
    public void setGroup (String[] s) {
        group = s;
    }

    /** Getter for nodeprecated option
     */
    public boolean isNodeprecated () {
        return nodeprecated;
    }

    /** Setter for nodeprecated option
     */
    public void setNodeprecated (boolean b) {
        nodeprecated = b;
    }

    /** Getter for nodeprecatedlist option
     */
    public boolean isNodeprecatedlist () {
        return nodeprecatedlist;
    }

    /** Setter for nodeprecatedlist option
     */
    public void setNodeprecatedlist (boolean b) {
        nodeprecatedlist = b;
    }

    /** Getter for notree option
     */
    public boolean isNotree () {
        return notree;
    }

    /** Setter for notree option
     */
    public void setNotree (boolean b) {
        notree = b;
    }

    /** Getter for noindex option
     */
    public boolean isNoindex () {
        return noindex;
    }

    /** Setter for noindex option
     */
    public void setNoindex (boolean b) {
        noindex = b;
    }

    /** Getter for nohlep option
     */
    public boolean isNohelp () {
        return nohelp;
    }

    /** Setter for nohelp option
     */
    public void setNohelp (boolean b) {
        nohelp = b;
    }

    /** Getter for nonavbar option
     */
    public boolean isNonavbar () {
        return nonavbar;
    }

    /** Setter for nonavbar option
     */
    public void setNonavbar (boolean b) {
        nonavbar = b;
    }

    /** Getter for helpfile option
     */
    public File getHelpfile () {
        return helpfile;
    }

    /** Setter for helpfile option
     */
    public void setHelpfile (File f) {
        helpfile = f;
    }

    /** Getter for stylesheetfile option
     */
    public File getStylesheetfile () {
        return stylesheetfile;
    }

    /** Setter for stylesheetfile option
     */
    public void setStylesheetfile (File f) {
        stylesheetfile = f;
    }

    /** Getter for docencoding option
     */
    public String getCharset () {
        return charset;
    }

    /** Setter for docencoding option
     */
    public void setCharset (String s) {
        charset = s;
    }

    static class GroupEditor extends StringArrayEditor {

        public String getAsText() {
            return null;
        }
    }

}

/*
 * Log
 *  11   Gandalf   1.10        2/8/00   Petr Hrebejk    Problem with mounting 
 *       Javadoc output directory in multiuser installation fix
 *  10   Gandalf   1.9         1/12/00  Petr Hrebejk    i18n
 *  9    Gandalf   1.8         1/3/00   Petr Hrebejk    Bugfix 4747
 *  8    Gandalf   1.7         11/27/99 Patrik Knakal   
 *  7    Gandalf   1.6         11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         9/15/99  Petr Hrebejk    Option -docencoding 
 *       changed to -charset
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         4/28/99  Petr Hrebejk    
 *  2    Gandalf   1.1         4/23/99  Petr Hrebejk    
 *  1    Gandalf   1.0         4/23/99  Petr Hrebejk    
 * $
 */
