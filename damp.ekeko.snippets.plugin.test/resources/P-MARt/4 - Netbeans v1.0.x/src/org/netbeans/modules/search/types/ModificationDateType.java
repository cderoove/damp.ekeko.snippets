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
import java.util.*;
import java.text.*;

import org.openide.util.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;

import org.apache.regexp.*;

import org.netbeans.modules.search.res.*;

/**
 * Test DataObject primaryFile for modificaion date.
 * <p>There are mutually exclusive criteria: between and days.
 * Between represents absolute date interval. Days represents
 * relative interval related to today.
 *
 * <p>Internally uses null as wildcard. It is presented as WILDCARD string.
 * One of between or days must be null.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class ModificationDateType extends DataObjectType {

    public static final long serialVersionUID = 4L;

    public static final String PROP_DAYS = "days"; // NOI18N
    public static final String PROP_BEFORE = "before"; // NOI18N
    public static final String PROP_AFTER = "after"; // NOI18N

    private transient boolean TRACE = false;

    /** Holds value of property matchBefore. */
    private Date matchBefore;

    /** Holds value of property matchAfter. */
    private Date matchAfter;

    /** Holds value of property day. */
    private Short days;

    /** Creates new FullTextType */
    public ModificationDateType() {
    }

    /**
    */
    public String toString() {
        return "ModificationDateType: days:" + days + " after:" + matchAfter + " before:" + matchBefore ; // NOI18N
    }

    /**
    * Does the DataObject pass this criterion?
    * @param dataobject to be examined
    * @return true if pass
    */
    public boolean test (DataObject dobj) {

        FileObject fo = dobj.getPrimaryFile();

        // it is strange
        if (fo == null) return false;

        // Primary File Modification Date
        Date date = fo.lastModified();

        t("file:" + fo + " modified:" + date); // NOI18N
        boolean hit = testDays(date) && testAfter(date) && testBefore(date);

        if (hit) addDetail(Res.text("DETAIL_DATE") + new FormattedDate(date).toString()); // NOI18N
        return hit;
    }

    /** Is within range? */
    private boolean testAfter(Date date) {
        if (matchAfter == null) return true;
        return date.compareTo(matchAfter)>=0;
    }

    /** Is within range? */
    private boolean testBefore(Date date) {
        if (matchBefore == null) return true;
        return date.compareTo(matchBefore)<=0;
    }

    /** Is within 24 hours range? */
    private boolean testDays(Date date) {
        if (days == null) return true;
        return (System.currentTimeMillis() - date.getTime()) < days.shortValue()*1000L*60L*60L*24L;
    }



    /** ???
    */
    public String getDisplayName() {
        return Res.text("DATE_CRITERION"); // NOI18N
    }


    // -------------- before -------------------

    /** Getter for property matchBefore.
     *@return Value of property matchBefore.
     */
    public Date getMatchBeforeAsDate() {
        return new FormattedDate(matchBefore);
    }

    public String getMatchBefore() {

        if (matchBefore == null)
            return WILDCARD;
        else
            return getMatchBeforeAsDate().toString();
    }

    /** Setter for property.
    * @param String to be parsed 
    * @throw IllegalArgumentException if null passed    
    */  
    public void setMatchBefore(String before) {
        try {
            setMatchBeforeImpl(before);
            setValid(true);
        } catch (IllegalArgumentException ex) {
            setValid(false);
            throw ex;
        }
    }

    private void setMatchBeforeImpl(String matchBefore) {

        if (matchBefore == null) throw new IllegalArgumentException();

        if (matchBefore.equals(WILDCARD)) {
            setMatchBeforeByDate(null);
            return;
        }

        try {
            setMatchBeforeByDate(new FormattedDate(matchBefore));

        } catch (ParseException ex) {
            throw new IllegalArgumentException();
        }

    }

    /** Setter for property matchBefore.
     *@param matchBefore New value of property matchBefore.
     */
    public void setMatchBeforeByDate(Date matchBefore) {

        //let date represent one milisecond before midnight

        if (matchBefore != null) {

            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(matchBefore);

            cal.set(cal.HOUR_OF_DAY, cal.getActualMaximum(cal.HOUR_OF_DAY));
            cal.set(cal.MINUTE, cal.getActualMaximum(cal.MINUTE));
            cal.set(cal.SECOND, cal.getActualMaximum(cal.SECOND));
            cal.set(cal.MILLISECOND, cal.getActualMaximum(cal.MILLISECOND));

            matchBefore = cal.getTime();
        }

        //do it

        Date old = this.matchBefore;
        this.matchBefore = matchBefore;
        days = null;
        firePropertyChange(PROP_BEFORE, old, matchBefore);
        firePropertyChange(PROP_DAYS, null, null);

    }


    // --------------- after ---------------


    /** Getter for property matchAfter.
     *@return Value of property matchAfter.
     */
    public Date getMatchAfterAsDate() {
        return new FormattedDate(matchAfter);
    }

    public String getMatchAfter() {

        if (matchAfter == null)
            return WILDCARD;
        else
            return getMatchAfterAsDate().toString();
    }

    /** Setter for property.
    * @param String to be parsed 
    * @throw IllegalArgumentException if null passed
    */
    public void setMatchAfter(String after) {
        try {
            setMatchAfterImpl(after);
            setValid(true);
        } catch (IllegalArgumentException ex) {
            setValid(false);
            throw ex;
        }
    }


    private void setMatchAfterImpl(String matchAfter) {

        if (matchAfter == null) throw new IllegalArgumentException();

        if (matchAfter.equals(WILDCARD)) {
            setMatchAfterByDate(null);
            return;
        }

        try {
            setMatchAfterByDate(new FormattedDate(matchAfter));

        } catch (ParseException ex) {
            throw new IllegalArgumentException();
        }

    }

    /** Setter for property matchAfter.
     *@param matchAfter New value of property matchAfter.
     */
    public void setMatchAfterByDate(Date matchAfter) {

        //let date represent midnight

        if (matchAfter != null) {

            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(matchAfter);

            cal.set(cal.HOUR_OF_DAY, cal.getActualMinimum(cal.HOUR_OF_DAY));
            cal.set(cal.MINUTE, cal.getActualMinimum(cal.MINUTE));
            cal.set(cal.SECOND, cal.getActualMinimum(cal.SECOND));
            cal.set(cal.MILLISECOND, cal.getActualMinimum(cal.MILLISECOND));

            matchAfter = cal.getTime();
        }

        //do it

        Date old = this.matchAfter;
        this.matchAfter = matchAfter;
        days = null;
        firePropertyChange(PROP_AFTER, old, matchAfter);
        firePropertyChange(PROP_DAYS, null, null);

    }


    //  ------------ days handling ----------------------


    /** Getter for property day.
     *@return Value of property day.
     */
    public Short getDaysAsShort() {
        return days;
    }

    public String getDays() {
        if (days == null) return WILDCARD;
        else return days.toString();
    }

    /** Setter for property day.
    * @param String to be parsed as short
    */
    public void setDays(String days) {
        try {
            setDaysImpl(days);
            setValid(true);
        } catch (IllegalArgumentException ex) {
            setValid(false);
            throw ex;
        }
    }

    public void setDaysImpl(String days) {

        if (days.equals(WILDCARD)) {
            setDaysByShort(null);
            return;
        }

        try {
            DecimalFormat format = new DecimalFormat();
            setDaysByShort(new Short(format.parse(days).shortValue()));

        } catch (ParseException ex) {
            throw new IllegalArgumentException();
        }

    }

    /** Setter for property day.
     *@param day New value of property day.
     */
    private void setDaysByShort(Short days) {

        Short old = this.days;
        this.days = days;

        matchAfter = null;
        matchBefore = null;

        firePropertyChange(PROP_DAYS, old, days);
        firePropertyChange(PROP_AFTER, null, null);
        firePropertyChange(PROP_BEFORE, null, null);

    }


    // --------------- INNER ---------------------------


    /**
    * Date using default locale formatting.
    * Provide overridden constructor and toString() method.
    */
    static class FormattedDate extends Date {

        /** Default locale formattor. */
        private static DateFormat format;
        private transient boolean isNull = true;

        /** Init static fields.
        */
        static {
            format = new SimpleDateFormat().getDateInstance();
            // bugfix accepts dates like Dec -1, 1999 = Nov 29
            //    format.setLenient(true);
        }

        /** Create new object.
        * @param date null is ambiguous -> today.
        */
        public FormattedDate(Date date) {
            super(date == null ? new Date().getTime() : date.getTime() );
            isNull = date == null;
        }

        /**
        * Create new object.
        * @param Use default locale formatting while parsing date. 
        */    
        public FormattedDate(String date) throws ParseException {
            super( format.parse(date).getTime() );
            isNull = date == null;
        }

        /** Use defalt locale formatting. */
        public String toString() {
            return format.format(this);
        }

        /**
        * Extra handling of equals(null). Return true if
        * this Date represents wildcard value.
        */
        public boolean equals(Object obj){
            if (obj == null && isNull) return true;
            else return super.equals(obj);
        }
    }

    private void t(String msg) {
        if (TRACE)
            System.err.println("CriteriaM: " + msg);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (ModificationDateType.class);
    }

    public String getTabText() {
        return Res.text("DATE_CRITERION"); // NOI18N
    }

}


/*
* Log
*  15   Gandalf-post-FCS1.13.1.0    4/4/00   Petr Kuzel      Comments + output window 
*       fix
*  14   Gandalf   1.13        1/18/00  Jesse Glick     Context help.
*  13   Gandalf   1.12        1/13/00  Radko Najman    I18N
*  12   Gandalf   1.11        1/11/00  Petr Kuzel      Result details added.
*  11   Gandalf   1.10        1/10/00  Petr Kuzel      "valid" fired.
*  10   Gandalf   1.9         1/7/00   Petr Kuzel      Lenient date parsing 
*       fixed.
*  9    Gandalf   1.8         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  8    Gandalf   1.7         1/4/00   Petr Kuzel      Bug hunting.
*  7    Gandalf   1.6         12/23/99 Petr Kuzel      Architecture improved.
*  6    Gandalf   1.5         12/20/99 Petr Kuzel      L&F fixes.
*  5    Gandalf   1.4         12/17/99 Petr Kuzel      Bundling.
*  4    Gandalf   1.3         12/16/99 Petr Kuzel      
*  3    Gandalf   1.2         12/15/99 Petr Kuzel      
*  2    Gandalf   1.1         12/15/99 Martin Balin    Fixed package name
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 

