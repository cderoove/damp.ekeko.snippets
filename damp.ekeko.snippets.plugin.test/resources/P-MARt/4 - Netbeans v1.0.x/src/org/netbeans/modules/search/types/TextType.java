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

package org.netbeans.modules.search.types;

import java.io.*;

import org.openide.loaders.*;
import org.openide.filesystems.*;

import org.apache.regexp.*;

/**
 * Abstract text test.
 *
 * <p>There are mutually exclusive criteria: substring and re.
 * One of substring or re must be null.
 *
 * <p>Internally uses null as wildcard. It is presented as WILDCARD string.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public abstract class TextType extends DataObjectType {

    public static final long serialVersionUID = 3L;

    public final String PROP_SUBSTRING = "Substring"; // NOI18N
    public final String PROP_RE = "RE"; // NOI18N

    /** Holds value of subtring criterion. */
    protected  String matchString;

    /** Holds value of re criterion. */
    protected transient RE re;
    protected  String reString;

    /** Creates new FullTextType */
    public TextType() {
    }

    /** @return string desribing current state.
    */
    public String toString() {
        return "TextType: substring:" + matchString + " REstring:" + reString + " re:" + re; // NOI18N
    }

    //
    // Match methods
    //

    protected boolean match (String text) {
        return matchString(text) && matchRE(text);
    }

    private boolean matchRE (String line) {
        if ( re == null ) return true;
        return re.match(line);
    }

    private boolean matchString (String line) {
        if ( matchString == null ) return true;
        return (line.indexOf(matchString) >= 0);
    }



    //
    // Be bound bean
    //


    /** Getter for property matchWord.
     *@return Value of property matchWord.
     */
    public String getMatchString() {

        if (matchString == null)
            return WILDCARD;
        else
            return matchString;
    }

    /** Setter for property matchWord.
     * @param matchString New value of criterion substring.
     */
    public void setMatchString(String matchString) {

        if (matchString == null) {
            setValid(false);
            throw new IllegalArgumentException();
        }

        if (matchString.equals(WILDCARD))
            matchString = null;

        String old = this.matchString;
        this.matchString = matchString;

        re = null;
        reString = null;
        firePropertyChange(PROP_SUBSTRING, old, matchString);
        firePropertyChange(PROP_RE, null, null);
        setValid(true);
    }


    /** Getter for property re1.
     *@return Value of property re1.
     */
    public String getRe() {

        if (reString == null)
            return WILDCARD;
        else
            return reString;
    }

    /** Setter for property re1.
     *@param exp New value of criterion re.
     *@throw IllegalArgumentException if not valid regexp or null. 
     */
    public void setRe(String re) {
        try {
            setReImpl(re);
            setValid(true);
        } catch (IllegalArgumentException ex) {
            setValid(false);
            throw ex;
        }
    }

    private void setReImpl(String exp) {

        if (exp == null) throw new IllegalArgumentException();

        String old = reString;

        if (exp.equals(WILDCARD)) {
            reString = null;
            exp = null;
            matchString=null;  //MUX
            firePropertyChange(PROP_RE, old, reString);
            firePropertyChange(PROP_SUBSTRING, null, null);
            return;
        }


        try {
            re = new RE(exp);
            reString = exp;
            matchString = null; //MUX
            firePropertyChange(PROP_RE, old, reString);
            firePropertyChange(PROP_SUBSTRING, null, null);

        } catch (RESyntaxException ex) {
            throw new IllegalArgumentException();
        }
    }

}


/*
* Log
*  4    Gandalf   1.3         1/13/00  Radko Najman    I18N
*  3    Gandalf   1.2         1/10/00  Petr Kuzel      "valid" fired.
*  2    Gandalf   1.1         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  1    Gandalf   1.0         12/23/99 Petr Kuzel      
* $ 
*/ 

