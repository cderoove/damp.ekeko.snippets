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

package org.netbeans.modules.jarpackager;

import java.io.*;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import org.openide.filesystems.FileObject;

/** File object filter which filters file objects using specified
* regular expression.
*
* @author Dafe Simonek
*/
public class REFilter implements FileObjectFilter {

    /** asociated regular expression matcher which does all the work
    * for us */
    protected transient RE matcher;
    /** Pattern string */
    protected String pattern;
    /** true if regular expression should be treated negatively,
    * false otherwise */
    protected boolean negative;

    static final long serialVersionUID =-6746916584711803738L;
    /** Creates new REFilter */
    public REFilter (String pattern) throws RESyntaxException {
        matcher = new RE(pattern);
        this.pattern = pattern;
    }

    /** Accepts or refuses given file object. File object is accepted if
    * its name matches current regular expression.
    * @param fo File object to accept of refuse
    * @return true if given file object can be accepted, false otherwise.
    */
    public boolean accept (FileObject fo) {
        // construct the name of the file object
        StringBuffer buf = new StringBuffer();
        buf.append(fo.getName());
        buf.append("."); // NOI18N
        buf.append(fo.getExt());
        boolean result = matcher.match(buf.toString());
        return negative ? !result : result;
    }

    /* @return true if negative processing of regular expression
    * is turned on, false otherwise */
    public boolean isNegative () {
        return negative;
    }

    /** Sets type of how regular expression should be treated,
    * positively or negatively */
    public void setNegative (boolean negative) {
        this.negative = negative;
    }

    private void writeObject (ObjectOutputStream out)
    throws IOException {
        out.defaultWriteObject();
    }

    /** Deserialization - we must recreate regular expression matcher,
    * because it's not serializable */
    private void readObject (ObjectInputStream in)
    throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // recreate matcher
        try {
            matcher = new RE(pattern);
        } catch (RESyntaxException exc) {
            // turn around into IOException...
            throw new IOException();
        }
    }

    /** @return RE pattern as textual description of this filter */
    public String toString () {
        return pattern;
    }

    /** Utility method, check validity of given regular expression
    * pattern.
    * @return true if given pattern was compiled ok, false otherwise
    */
    public static boolean checkRegExp (String expPattern) {
        try {
            RE matcher = new RE(expPattern);
        } catch (RESyntaxException exc) {
            // some syntax error, return false
            return false;
        }
        return true;
    }

}

/*
* <<Log>>
*  7    Gandalf   1.6         1/16/00  David Simonek   i18n
*  6    Gandalf   1.5         11/27/99 Patrik Knakal   
*  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         10/4/99  David Simonek   
*  3    Gandalf   1.2         9/16/99  David Simonek   a lot of bugfixes (RE 
*       filters, empty jar content etc)  added templates
*  2    Gandalf   1.1         9/13/99  David Simonek   modified for sun's RE
*  1    Gandalf   1.0         6/22/99  David Simonek   
* $
*/