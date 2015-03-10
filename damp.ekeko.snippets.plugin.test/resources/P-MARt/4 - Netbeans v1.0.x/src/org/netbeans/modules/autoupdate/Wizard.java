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

import java.awt.Dialog;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ResourceBundle;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.event.EventListenerList;

import org.openide.WizardDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Implements the behavior of AutoUpdate wizard
 *
 * @author  Petr Hrebejk
 * @version 
 */
class Wizard extends Object implements WizardDescriptor.Iterator {

    private static final ResourceBundle bundle = NbBundle.getBundle( Wizard.class );

    /** Structure of the updates */
    private Updates updates;


    /** Panels of the wizard */
    private WizardPanel[][] panels = new WizardPanel[][] {
                                         { new StartPanel(),
                                           new CheckPanel(),
                                           new ConfigPanel(),
                                           new DownloadPanel(),
                                           new LastPanel()
                                         },

                                         { null,
                                           new SelectPanel(),
                                           null,
                                           null,
                                           null
                                         }
                                     };

    /** Current panel */
    private int current = 0;

    private int modulesOK = 0;

    private boolean canceled = false;

    private boolean downloadsPerformed = false;

    /** Which type of wizard should run */
    private int wizardType = 0;

    /** The wizard descriptor */
    private WizardDescriptor wizardDescriptor;

    /** The dialog */
    Dialog dialog;


    Wizard() {
        this( null );
    }

    /** Creates the wizard */
    Wizard( Updates updates ) {


        // Create the wizard

        PropertyChangeListener listener = new PropertyChangeListener() {
                                              public void propertyChange(PropertyChangeEvent event) {
                                                  if (event.getPropertyName().equals(DialogDescriptor.PROP_VALUE)) {
                                                      Object option = event.getNewValue();

                                                      if (option == WizardDescriptor.FINISH_OPTION ||
                                                              option == NotifyDescriptor.CANCEL_OPTION ) {
                                                          //panels[ current ].end( true );
                                                          if ( option == NotifyDescriptor.CANCEL_OPTION )
                                                              canceled = true;
                                                          getCurrent().end( true );
                                                          dialog.setVisible(false);
                                                          dialog.dispose();
                                                      }
                                                  }
                                              }
                                          };


        if ( updates != null ) {
            this.updates = updates;
            current = 2;
            panels[0][2].start( true );
        }

        wizardDescriptor = new WizardDescriptor( this, new Object() );
        wizardDescriptor.setModal( true );
        wizardDescriptor.setTitleFormat (new java.text.MessageFormat ( bundle.getString( "CTL_Wizard" ) + " {1}"));
        wizardDescriptor.setAdditionalOptions (new Object[] { });
        wizardDescriptor.addPropertyChangeListener(listener);
        wizardDescriptor.setOptions (new Object[] {
                                         WizardDescriptor.PREVIOUS_OPTION,
                                         WizardDescriptor.NEXT_OPTION,
                                         WizardDescriptor.FINISH_OPTION,
                                         NotifyDescriptor.CANCEL_OPTION } );

    }

    /** Runs the wizard */
    void go() {

        Autoupdater.setRunning( true );

        dialog = TopManager.getDefault().createDialog( wizardDescriptor );

        /*
        dialog.addWindowListener( new java.awt.event.WindowAdapter() {
          public void windowClosed( java.awt.event.WindowEvent e ) {
            cancel();
          }
    } );
        */

        canceled = false;
        downloadsPerformed = false;

        dialog.show();

        if ( wizardDescriptor.getValue() == WizardDescriptor.FINISH_OPTION
                && modulesOK > 0 ) {

            /*
            NotifyDescriptor.Confirmation nd = new NotifyDescriptor.Confirmation(
                  bundle.getString( "MSG_UpdateConfirmation" ),
                  bundle.getString( "CTL_UpdateConfirmation" ),
                  NotifyDescriptor.YES_NO_OPTION );

            if ( TopManager.getDefault().notify( nd ).equals( NotifyDescriptor.YES_OPTION ) ) {  
            */


            Collection modules = updates.getModules();
            Iterator it = modules.iterator();
            while( it.hasNext() ) {
                ModuleUpdate mu = (ModuleUpdate)it.next();

                if ( !mu.isInstallApproved() ) {
                    Downloader.getNBM( mu ).delete();
                    Downloader.getNBM( mu ).deleteOnExit();
                }
            }
            Autoupdater.restart();
            // }
        }

        Autoupdater.setRunning( false );
    }


    void cancel() {
        canceled = true;
        getCurrent().end( true );
        Autoupdater.setRunning( false );
        //System.out.println(" Canceling wizard " ); // NOI18N
    }


    // Implementation of Iterator --------------------------------------------------------

    public String name() {
        return getCurrent().getName();
    }

    public WizardDescriptor.Panel current() {

        return getCurrent();
        /*
        return panels[ wizardType ][ current ] == null ?
          panels[ 0 ][ current ] : panels[ wizardType ][ current ];
        */
    }

    private WizardPanel getCurrent() {

        return panels[ wizardType ][ current ] == null ?
               panels[ 0 ][ current ] : panels[ wizardType ][ current ];
    }

    public boolean hasNext() {

        if ( current == 1 && getCurrent().nextPanelOffset() == -1 )
            return false;
        else
            return current < panels[wizardType].length - 1;
    }

    public boolean hasPrevious() {

        if ( current == 2 )
            return !downloadsPerformed;
        else
            return current > 0;
    }

    public void nextPanel() {


        if ( current == 4 ) {
            //System.out.println (" asking " ); // NOI18N
        }

        getCurrent().end( true );

        if ( current == 2 && getCurrent().nextPanelOffset() == 2 )
            current = 4;
        //else if ( current == 0 && getCurrent().nextPanelOffset() == 2 )
        //  current = 2;
        else
            current ++;

        getCurrent().start( true );

        if ( current > 2 )
            downloadsPerformed = true;

        //centerDialog();
    }

    public void previousPanel() {

        getCurrent().end( false );

        switch ( current ) {
        case 2:
            if ( wizardType == 0 )
                current = 0;
            else
                current--;
            break;
        case 4:
            current = 2;
            break;
        default:
            current --;
            break;
        }

        getCurrent().start( false );
        //centerDialog();
    }



public synchronized void addChangeListener(javax.swing.event.ChangeListener listener) {}
    public synchronized void removeChangeListener(javax.swing.event.ChangeListener listener) {}

    // Inner classes ----------------------------------------------------------------------

    static interface Validator {

        // Called from component when the next button should be enabled or disbled
        public void setValid( boolean valid );

    }

    abstract class WizardPanel implements WizardDescriptor.Panel, Validator {

        protected Dimension WIZARD_SIZE = new Dimension( 550, 400 );

        protected boolean valid = true;

        /** Utility field used by event firing mechanism. */
        private EventListenerList listenerList = new EventListenerList();

        void start( boolean forward ) {}

        void end( boolean forward ) { }

        /** Called to get offset of the new panel */
        int nextPanelOffset() {
            return 1;
        }

        abstract String getName();

        // Implementation of Validator

        public void setValid(boolean valid) {
            this.valid = valid;
            fireChangeListenerStateChanged( this );
        }

        // Implementation of WizardDescriptor.Panel

        public boolean isValid() {
            return valid;
        }

        public HelpCtx getHelp() {
            return new HelpCtx ( Wizard.class );
        }

        public abstract Component getComponent();

        public void readSettings( Object settings ) {}

        public void storeSettings( Object settings ) {}

        /** Registers ChangeListener to receive events.
         *@param listener The listener to register.
         */
        public synchronized void addChangeListener(javax.swing.event.ChangeListener listener) {
            listenerList.add (javax.swing.event.ChangeListener.class, listener);
        }

        /** Removes ChangeListener from the list of listeners.
         *@param listener The listener to remove.
         */
        public synchronized void removeChangeListener(javax.swing.event.ChangeListener listener) {
            listenerList.remove (javax.swing.event.ChangeListener.class, listener);
        }

        /** Notifies all registered listeners about the event.
         *
         *@param param1 Parameter #1 of the <CODE>ChangeEvent<CODE> constructor.
         */
        protected void fireChangeListenerStateChanged(java.lang.Object param1) {
            javax.swing.event.ChangeEvent e = null;
            Object[] listeners = listenerList.getListenerList ();
            for (int i = listeners.length-2; i>=0; i-=2) {
                if (listeners[i]==javax.swing.event.ChangeListener.class) {
                    if (e == null)
                        e = new javax.swing.event.ChangeEvent (param1);
                    ((javax.swing.event.ChangeListener)listeners[i+1]).stateChanged (e);
                }
            }
        }
    }

    class StartPanel extends WizardPanel {

        FirstPanel firstPanel;

        StartPanel () {
            firstPanel = new FirstPanel( this ) {
                             public Dimension getPreferredSize() {
                                 return WIZARD_SIZE;
                             }
                         };
        }

        String getName() {
            return bundle.getString( "CTL_StartPanel");
        }

        /** In this case set valid is used for setting the right wizard type
        */
        public void setValid( boolean valid ) {
            wizardType = firstPanel.getWizardType();
        }

        public Component getComponent() {
            return firstPanel;
        }

        void end( boolean forward ) {
            //System.out.println("Ending start" ); // NOI18N
            if ( wizardType == 0 )
                Downloader.deleteDownload();

            firstPanel.setRegNum();
        }

        /*
        int nextPanelOffset() {
          if ( wizardType == 1 )
            return 2;
          else
            return 1; 
    }
        */

    }

    class CheckPanel extends WizardPanel {

        private CheckProgressPanel component = null;

        CheckPanel () {
            component = new CheckProgressPanel()
                        {
                            public Dimension getPreferredSize() {
                                return WIZARD_SIZE;
                            }
                        };

        }

        int nextPanelOffset() {
            if ( valid && ( updates.getModules() == null || updates.getModules().size() <= 0 ) ) {
                return -1;
            }
            else
                return 1;
        }

        String getName() {
            return bundle.getString( "CTL_CheckPanel");
        }

        public Component getComponent() {
            return component;
        }

        void start( boolean forward ) {
            valid = false;

            component.setGaugeValue( ProgressDialog.OVERALL_GAUGE, 0 );
            component.setLabelText( ProgressDialog.EXTRA_LABEL, bundle.getString("CheckProgressPanel.jLabel1.text") );
            component.setLabelText( ProgressDialog.OVERALL_LABEL, "" ); // NOI18N
            component.setLabelText( ProgressDialog.PARTIAL_LABEL, "" ); // NOI18N

            component.setDone( false, updates );
            updates = new Updates( Autoupdater.Support.getUpdateURL() );
            updates.checkUpdates( (ProgressDialog)panels[0][1].getComponent(), this );

        }

        public void setValid( boolean valid ) {
            super.setValid( valid );
            component.setDone( true, updates );
            Notification.performNotification( updates );
        }

        void end( boolean forward ) {
            if ( !valid ) {
                //System.out.println("Canceling check" ); // NOI18N
                updates.cancelCheck();
            }
        }
    }

    class ConfigPanel extends WizardPanel {

        private UpdatePanel updatePanel = null;

        ConfigPanel () {
            updatePanel = new UpdatePanel( this, wizardType ) {
                              public Dimension getPreferredSize() {
                                  return WIZARD_SIZE;
                              }
                          };
        }

        public void setValid( boolean valid ) {
            super.setValid( valid );
            if ( dialog != null )
                dialog.repaint( );
        }

        String getName() {
            return bundle.getString( "CTL_ConfigPanel");
        }

        public Component getComponent() {
            return updatePanel;
        }

        public boolean isValid() {
            return valid; //|| downloadsPerformed;
        }

        void start( boolean forward ) {
            valid = false;
            if ( forward )
                Settings.getShared().setLastStamp( updates.getTimeStamp() );
            updatePanel.setUpdates( updates );
        }

        void end( boolean forward ) {
            if ( forward )
                updatePanel.markSelectedModules();
        }

        int nextPanelOffset() {
            if ( updatePanel.modulesToDownload() == 0 )
                return 2;
            else
                return 1;
        }

    }

    class DownloadPanel extends WizardPanel {

        private Downloader downloader;
        private SignVerifier signVerifier;

        private ProgressDialog progressPanel = null;

        private boolean isDownloadFinished;

        DownloadPanel () {

            if ( wizardType == 0 )
                progressPanel =  new DownloadProgressPanel() {
                                 public Dimension getPreferredSize() {
                                     return WIZARD_SIZE;
                                 }
                             };
            else
                progressPanel =  new CopyProgressPanel() {
                                 public Dimension getPreferredSize() {
                                     return WIZARD_SIZE;
                                 }
                             };
        }

        public void setValid( boolean valid ) {

            // First call to setValid means the signVerifier is ready to run


            if ( !isDownloadFinished && valid) {
                isDownloadFinished  = true;
                signVerifier = new SignVerifier( updates, progressPanel, this );
                progressPanel.setLabelText( ProgressDialog.EXTRA_LABEL, bundle.getString("DownloadProgressPanel.jLabel1.securityText") );
                signVerifier.doVerify();
            }
            // This is the real end
            else {
                super.setValid( valid );
                progressPanel.setLabelText( ProgressDialog.EXTRA_LABEL, bundle.getString("DownloadProgressPanel.jLabel1.doneText") );
                // downloadPanel.setDone( true );
            }
        }

        String getName() {
            return bundle.getString( wizardType == 0 ? "CTL_DownloadPanel" : "CTL_CopyPanel" );
        }

        public Component getComponent() {
            return ( Component )progressPanel;
        }

        void start( boolean forward ) {
            valid = false;
            isDownloadFinished = false;
            downloader = new Downloader( updates, progressPanel, this, wizardType == 0 );
            progressPanel.setLabelText( ProgressDialog.EXTRA_LABEL, wizardType == 0 ?
                                        bundle.getString("DownloadProgressPanel.jLabel1.downloadText") :
                                        bundle.getString("CopyProgressPanel.jLabel1.copyText") );
            downloader.doDownload();
            // Sign verifier is called in setValid function
        }

        void end( boolean forward ) {
            if ( !valid ) {
                if ( !isDownloadFinished )
                    downloader.cancelDownload();
                else
                    signVerifier.cancelVerify();
            }
        }

    }

    class LastPanel extends WizardPanel {

        ResultsPanel resultsPanel;

        LastPanel() {
            resultsPanel = new ResultsPanel( this ) {
                               public Dimension getPreferredSize() {
                                   return WIZARD_SIZE;
                               }
                           };
        }

        String getName() {
            return bundle.getString( wizardType == 0 ? "CTL_ResultsPanel" : "CTL_ResultsPanel_1");
        }

        public Component getComponent() {
            return resultsPanel;
        }

        void start( boolean forward ) {
            modulesOK = resultsPanel.generateResults( updates );
        }

    }

    // Panels for installing manualy downloaded modules ---------

    class SelectPanel extends WizardPanel {

        private SelectModulesPanel selectModulesPanel = null;

        SelectPanel () {
            selectModulesPanel = new SelectModulesPanel( this ) {
                                     public Dimension getPreferredSize() {
                                         return WIZARD_SIZE;
                                     }
                                 };
        }

        String getName() {
            return bundle.getString( "CTL_SelectModulesPanel");
        }

        public Component getComponent() {
            return selectModulesPanel;
        }

        /*
        public boolean isValid() {
          return valid || downloadsPerformed;
    }
        */

        void start( boolean forward ) {
            valid = false;
            if ( forward )
                selectModulesPanel.reset();
            else
                setValid( true );
            //updatePanel.setUpdates( updates );
        }

        void end( boolean forward ) {

            if ( forward && !canceled ) {
                updates = new Updates( selectModulesPanel.getFiles() );
                updates.checkDownloadedModules();
            }
        }

    }


    /*
    class CopyPanel extends WizardPanel {
      
      private Downloader copier;
      
      private CopyProgressPanel copyPanel = null;
      
      CopyPanel () {
        copyPanel = new CopyProgressPanel(){
          public Dimension getPreferredSize() {
            return WIZARD_SIZE;
          }
        };
      }
      
      public void setValid( boolean valid ) {
        super.setValid( valid );
        copyPanel.setDone( true );
      }

      String getName() {
        return bundle.getString( "CTL_CopyPanel");
      }
      
      public Component getComponent() {
        return copyPanel;
      }
      
      void start( boolean forward ) { 
        valid = false;
        copier = new Downloader( updates, copyPanel, this, false );
        copier.doDownload();
      }
      
      void end( boolean forward ) {
        if ( !valid ) {
          copier.cancelDownload();
        }
      }
      
}
    */

}
/*
 * Log
 *  17   Gandalf   1.16        2/23/00  Petr Hrebejk    Notifications added into
 *       autoupdate
 *  16   Gandalf   1.15        1/15/00  Petr Hrebejk    Next is enabled when 
 *       returning on second panel (Manualy downloaded modules install)
 *  15   Gandalf   1.14        1/13/00  Petr Hrebejk    Multiuser bugfix
 *  14   Gandalf   1.13        1/13/00  Petr Hrebejk    i18 mk3
 *  13   Gandalf   1.12        1/12/00  Petr Hrebejk    i18n mk2
 *  12   Gandalf   1.11        1/12/00  Petr Hrebejk    i18n
 *  11   Gandalf   1.10        1/10/00  Petr Hrebejk    Bug in setting last 
 *       stamp fixed
 *  10   Gandalf   1.9         1/9/00   Petr Hrebejk    Proxy Config and 
 *       Registration number added
 *  9    Gandalf   1.8         1/3/00   Petr Hrebejk    Various bug fixes - 
 *       5097, 5098, 5110, 5099, 5108
 *  8    Gandalf   1.7         12/21/99 Petr Hrebejk    Various bugfixes
 *  7    Gandalf   1.6         12/20/99 Petr Hrebejk    Autocheck & security 
 *       finished
 *  6    Gandalf   1.5         12/1/99  Petr Hrebejk    Checkin signatures of 
 *       NBM files & automatic autoupdate check added
 *  5    Gandalf   1.4         11/8/99  Petr Hrebejk    Install of downloaded 
 *       modules added, Licenses in XML
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         10/11/99 Petr Hrebejk    Last minute fixes
 *  2    Gandalf   1.1         10/11/99 Petr Hrebejk    Version before Beta 5
 *  1    Gandalf   1.0         10/10/99 Petr Hrebejk    
 * $
 */
