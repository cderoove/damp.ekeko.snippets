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
import java.net.URL;
import java.util.*;
import java.text.SimpleDateFormat;

import org.openide.TopManager;
import org.openide.modules.ModuleDescription;
import org.openide.util.NbBundle;

import org.netbeans.core.UpdateSupport;


/** This class checks for updates in given period
 *
 * @author  Petr Hrebejk
 */
class AutoChecker extends Object
            implements UpdateSupport.UpdateChecker,
    Wizard.Validator {

    /** Resource bundle */
    static ResourceBundle bundle = NbBundle.getBundle(AutoChecker.class);

    /** Empty implementation of progress dialog */
    private static final ProgressDialog NULL_PROGRESS_DIALOG = new NullProgressDialog();

    /** Settings of autoupdate module */
    private Settings settings;

    /** Updates build by check */
    private Updates updates;

    // ==================================================================
    // This part is temporary

    static AutoChecker autoChecker;

    static void doCheck() {
        autoChecker.check();
    }

    // ==================================================================


    /** Creates new AutoChecker */
    AutoChecker() {
        settings = Settings.getShared();
    }

    /** Installs this class into update support in Forte 3.0 implementation */
    void install() {
        Autoupdater.installUpdateChecker( this );

        // ==================================================================
        // This part is temporary
        autoChecker = this;
        // ==================================================================

    }


    // Implementation of UpdateSupport.UpdateChecker

    public void check() {


        if ( !timeToCheck() ) {
            return;
        }

        // Even if the user anwers no we did our work
        settings.setLastCheck( new Date() );

        if ( settings.isAskBefore() ) {

            if ( !AutoCheckInfo.showDialog( bundle.getString( "MSG_AutoCheck_Before" ),
                                            javax.swing.JOptionPane.INFORMATION_MESSAGE, true ) )
                return;
        }

        Autoupdater.setRunning( true );

        updates = new Updates( Autoupdater.Support.getUpdateURL() );
        updates.checkUpdates( NULL_PROGRESS_DIALOG, this );

        TopManager.getDefault().setStatusText( bundle.getString( "CTL_Checking_StatusText" ) );
    }


    void reportResults() {


        Autoupdater.setRunning( false );



        TopManager.getDefault().setStatusText( "" );

        if ( updates.isError() ) {
            return;
        }

        Notification.performNotification( updates );

        // First af all check wether the XML has changed
        if ( settings.getLastStamp() != null && updates.getTimeStamp() != null &&
                !settings.getLastStamp().before( updates.getTimeStamp() ) ) {
            // Report it if necessary
            if ( settings.isNegativeResults() ) {
                AutoCheckInfo.showDialog( bundle.getString( "MSG_AutoCheck_NotFound" ),
                                          javax.swing.JOptionPane.INFORMATION_MESSAGE, false );
            }
            return;
        }


        if ( updates.getModules() != null &&
                updates.getModules().size() > 0 ) {
            // Some modules found

            if ( AutoCheckInfo.showDialog( bundle.getString( "MSG_AutoCheck_Found" ),
                                           javax.swing.JOptionPane.INFORMATION_MESSAGE, true ) ) {
                Wizard wizard = new Wizard( updates );
                settings.setLastStamp( updates.getTimeStamp() );
                wizard.go();
            }
        }
        else if ( settings.isNegativeResults() ) {
            // No modules found and we have to report negative results
            AutoCheckInfo.showDialog( bundle.getString( "MSG_AutoCheck_NotFound" ),
                                      javax.swing.JOptionPane.INFORMATION_MESSAGE, false );
        }

    }

    // Implementation of Wizard.Validator

    /** This method gets the notification that the updates is ready */

    public void setValid(boolean valid) {

        Runnable runnable = new Runnable ( ) {
                                public void run() {
                                    reportResults();
                                }
                            };

        javax.swing.SwingUtilities.invokeLater( runnable );

    }

    // Utility methods --------------------------------------------------------

    /** This method decides whether to perform the check or not
    */
    private boolean timeToCheck() {

        // If this is the first time always check
        if ( settings.getLastCheck() == null )
            return true;

        switch ( settings.getPeriod() ) {
        case Settings.EVERY_STARTUP:
            return true;
        case Settings.EVERY_NEVER:
            return false;
        default:
            Date lastCheck = settings.getLastCheck();
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime( lastCheck );

            /*
            calendar.set( Calendar.HOUR, 0 );
            calendar.set( Calendar.MINUTE, 0 );
            calendar.set( Calendar.SECOND, 0 );
            calendar.set( Calendar.MILLISECOND, 0 );
            */
            calendar.clear( Calendar.HOUR );
            calendar.clear( Calendar.AM_PM );
            calendar.clear( Calendar.MINUTE );
            calendar.clear( Calendar.SECOND );
            calendar.clear( Calendar.MILLISECOND );

            switch ( settings.getPeriod() ) {
            case Settings.EVERY_DAY:
                calendar.add( GregorianCalendar.DATE, 1 );
                break;
            case Settings.EVERY_WEEK:
                calendar.add( GregorianCalendar.WEEK_OF_YEAR, 1 );
                break;
            case Settings.EVERY_2WEEKS:
                calendar.add( GregorianCalendar.WEEK_OF_YEAR, 2 );
                break;
            case Settings.EVERY_MONTH:
                calendar.add( GregorianCalendar.MONTH, 1 );
                break;
            }

            SimpleDateFormat sdf = new SimpleDateFormat();

            return calendar.getTime().before( new Date() );

        }

    }

    /** Innerclass to satisfy the need of Updates class to show proggres of
     * update check. It does nothing
     */
    static class NullProgressDialog implements ProgressDialog {

        private static final String EMPTY_STRING = ""; // NOI18N

        /** Indexed getter for property gaugeValue.
         *@param index Index of the property.
         *@return Value of the property at <CODE>index</CODE>.
         */
        public int getGaugeValue(int gauge) {
            return 0;
        }
        /** Indexed setter for property gaugeValue.
         *@param index Index of the property.
         *@param gaugeValue New value of the property at <CODE>index</CODE>.
         */
        public void setGaugeValue(int gauge,int gaugeValue) {
        }
        /** Indexed setter for property gaugeBounds.
         *@param index Index of the property.
         *@param gaugeBounds New value of the property at <CODE>index</CODE>.
         */
        public void setGaugeBounds(int gauge,int gaugeMin,int gaugeMax) {
        }
        /** Indexed getter for property labelText.
         *@param index Index of the property.
         *@return Value of the property at <CODE>index</CODE>.
         */
        public String getLabelText(int label) {
            return EMPTY_STRING;
        }
        /** Indexed setter for property labelText.
         *@param index Index of the property.
         *@param labelText New value of the property at <CODE>index</CODE>.
         */
        public void setLabelText(int label,String labelText) {
        }
        /** Getter for property title.
         *@return Value of property title.
         */
        public String getTitle() {
            return EMPTY_STRING;
        }
        /** Setter for property title.
         *@param title New value of property title.
         */
        public void setTitle(String title) {
        }
    }

}

/*
 * Log
 *  9    Gandalf   1.8         2/23/00  Petr Hrebejk    Notifications added into
 *       autoupdate
 *  8    Gandalf   1.7         2/7/00   Petr Hrebejk    Status line text added 
 *       while autochecking
 *  7    Gandalf   1.6         1/18/00  Petr Hrebejk    AM-PM bug fixed
 *  6    Gandalf   1.5         1/12/00  Petr Hrebejk    i18n
 *  5    Gandalf   1.4         1/9/00   Petr Hrebejk    Proxy Config and 
 *       Registration number added
 *  4    Gandalf   1.3         1/3/00   Petr Hrebejk    Various bug fixes - 
 *       5097, 5098, 5110, 5099, 5108
 *  3    Gandalf   1.2         12/22/99 Petr Hrebejk    Various bugfixes
 *  2    Gandalf   1.1         12/20/99 Petr Hrebejk    Autocheck & security 
 *       finished
 *  1    Gandalf   1.0         12/1/99  Petr Hrebejk    
 * $
 */
