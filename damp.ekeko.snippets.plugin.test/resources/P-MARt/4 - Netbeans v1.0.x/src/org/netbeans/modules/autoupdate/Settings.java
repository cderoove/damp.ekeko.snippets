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

package org.netbeans.modules.autoupdate;

import java.io.File;
import java.beans.PropertyEditorSupport;
import java.util.ResourceBundle;
import java.util.Date;
import java.text.SimpleDateFormat;



import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Options autoupdate module
*
* @author Petr Hrebejk
*/
public class Settings extends SystemOption //implements ViewerConstants
{

    private static final String PROP_ASK_BEFORE = "askBefore"; // NOI18N
    private static final String PROP_NEGATIVE_RESULTS = "negativeResults"; // NOI18N
    private static final String PROP_PERIOD = "period"; // NOI18N
    private static final String PROP_LAST_CHECK = "lastCheck"; // NOI18N
    private static final String PROP_LAST_STAMP = "lastStamp"; // NOI18N
    private static final String PROP_REGISTARTION_NUMBER = "registrationNumber"; // NOI18N

    public static final int EVERY_STARTUP = 0;
    public static final int EVERY_DAY = 1;
    public static final int EVERY_WEEK = 2;
    public static final int EVERY_2WEEKS = 3;
    public static final int EVERY_MONTH = 4;
    public static final int EVERY_NEVER = 5;

    /** serialVersionUID */
    static final long serialVersionUID = 362844553936969452L;

    /** members to show */
    //private static int period = EVERY_STARTUP;

    /** Ask before check */
    // private static boolean askBefore = true;

    /** Show negative results */
    // private static boolean negativeResults = true;

    /** Last time the web was checked automatically checked for updates */
    // private static Date lastCheck = new Date();



    public Settings() {
        // Set default values of properties

        setAskBefore( true );
        setNegativeResults( true );
        setPeriod( EVERY_WEEK );
        // setLastCheck( new Date() );
        setLastCheck( null );
        setRegistrationNumber( "" ); // NOI18N
    }


    static Settings getShared() {
        return (Settings)findObject( Settings.class, true );
    }

    /** @return human presentable name */
    public String displayName() {
        return NbBundle.getBundle( Settings.class).getString("CTL_Settings_Name");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ( Settings.class );
    }

    /** Getter for period
    */
    public int getPeriod() {
        return ((Integer)getProperty( PROP_PERIOD )).intValue();
        //return period;
    }

    /** Setter for period
    */
    public void setPeriod( int period ) {
        putProperty( PROP_PERIOD, new Integer( period ), true );
        //this.period = period;
    }

    /** Getter for askBefore
    */
    public boolean isAskBefore () {
        return ((Boolean)getProperty( PROP_ASK_BEFORE )).booleanValue();
        //return askBefore;
    }

    /** Setter for askBefore
    */
    public void setAskBefore (boolean askBefore) {
        //this.askBefore = askBefore;
        putProperty( PROP_ASK_BEFORE, new Boolean( askBefore ), true );
    }

    /** Getter for negativeResults
    */
    public boolean isNegativeResults () {
        return ((Boolean)getProperty( PROP_NEGATIVE_RESULTS )).booleanValue();
        //return negativeResults;
    }

    /** Setter for negativeResults
    */
    public void setNegativeResults (boolean negativeResults) {
        putProperty( PROP_NEGATIVE_RESULTS, new Boolean( negativeResults ), true );
        //this.negativeResults = negativeResults;
    }

    /** Getter for last check
    */
    public Date getLastCheck() {
        return ((Date)getProperty( PROP_LAST_CHECK ));

    }

    /** Setter for last check
    */
    public void setLastCheck( Date lastCheck ) {
        putProperty( PROP_LAST_CHECK, lastCheck, true );
        //this.lastCheck = lastCheck;
    }

    /** Getter for last stamp
    */
    public Date getLastStamp() {
        return ((Date)getProperty( PROP_LAST_STAMP ));

    }

    /** Setter for last stamp
    */
    public void setLastStamp( Date lastStamp ) {
        putProperty( PROP_LAST_STAMP, lastStamp, true );
        //this.lastCheck = lastCheck;
    }

    /** Getter for registration number
    */
    public String getRegistrationNumber() {
        return ((String)getProperty( PROP_REGISTARTION_NUMBER ));

    }

    /** Setter for registration number
    */
    public void setRegistrationNumber( String registartionNumber ) {
        putProperty( PROP_REGISTARTION_NUMBER, registartionNumber, true );
        //this.lastCheck = lastCheck;
    }


    /** property editor for period property
    */
    public static class PeriodPropertyEditor extends PropertyEditorSupport {

        private static ResourceBundle bundle = NbBundle.getBundle( PeriodPropertyEditor.class );

        /** Array of tags
        */
        private static final String[] tags = {
            bundle.getString( "CTL_PeriodEditor_Startup" ),
            bundle.getString( "CTL_PeriodEditor_Day" ),
            bundle.getString( "CTL_PeriodEditor_Week" ),
            bundle.getString( "CTL_PeriodEditor_2Weeks" ),
            bundle.getString( "CTL_PeriodEditor_Month" ),
            bundle.getString( "CTL_PeriodEditor_Never" ) } ;

        private static final int [] values = {
            Settings.EVERY_STARTUP,
            Settings.EVERY_DAY,
            Settings.EVERY_WEEK,
            Settings.EVERY_2WEEKS,
            Settings.EVERY_MONTH,
            Settings.EVERY_NEVER };

        /** @return names of the supported member Acces types */
        public String[] getTags() {
            return tags;
        }

        /** @return text for the current value */
        public String getAsText () {
            long value = ((Integer)getValue()).intValue();

            for (int i = 0; i < values.length ; i++)
                if (values[i] == value)
                    return tags[i];

            return bundle.getString( "CTL_PeriodEditor_Unsupported" );
        }

        /** @param text A text for the current value. */
        public void setAsText (String text) {
            for (int i = 0; i < tags.length ; i++)
                if (tags[i] == text) {
                    setValue(new Integer(values[i]));
                    return;
                }

            setValue( new Integer(0) );
        }
    }

    /** property editor for last check property
    */
    public static class LastCheckPropertyEditor extends PropertyEditorSupport {

        private static ResourceBundle bundle = NbBundle.getBundle( PeriodPropertyEditor.class );

        private static final SimpleDateFormat sdf = new SimpleDateFormat();


        /** @return text for the current value */
        public String getAsText () {
            return sdf.format( (Date)getValue() );
        }

        /** @param text A text for the current value. */
        public void setAsText (String text) {

            try {
                Date newValue = sdf.parse( text );
                setValue( newValue );
            }
            catch ( java.text.ParseException e ) {
                // leave the old value if the user types some nocense
            }
        }
    }


}

/*
 * Log
 *  4    Gandalf   1.3         1/12/00  Petr Hrebejk    i18n
 *  3    Gandalf   1.2         1/9/00   Petr Hrebejk    Proxy Config and 
 *       Registration number added
 *  2    Gandalf   1.1         12/16/99 Petr Hrebejk    Sign checking added
 *  1    Gandalf   1.0         12/1/99  Petr Hrebejk    
 * $
 */
