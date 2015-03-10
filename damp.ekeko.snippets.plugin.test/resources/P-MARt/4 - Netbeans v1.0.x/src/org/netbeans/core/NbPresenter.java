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

package org.netbeans.core;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.*;

import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Default implementation of Dialog created from NotifyDescriptor.
*
* @author Ian Formanek, Jaroslav Tulach
*/
class NbPresenter extends JDialog
    implements PropertyChangeListener, WindowListener {
    /** variable holding current modal dialog in the system */
    public static NbPresenter currentModalDialog;

    protected NotifyDescriptor descriptor;

    private final JButton stdYesButton = new JButton (NbBundle.getBundle (NbPresenter.class).getString ("YES_OPTION_CAPTION"));
    private final JButton stdNoButton = new JButton (NbBundle.getBundle (NbPresenter.class).getString ("NO_OPTION_CAPTION"));
    private final JButton stdOKButton = new JButton (NbBundle.getBundle (NbPresenter.class).getString ("OK_OPTION_CAPTION"));
    private final JButton stdCancelButton = new JButton (NbBundle.getBundle (NbPresenter.class).getString ("CANCEL_OPTION_CAPTION"));
    private final JButton stdHelpButton = new JButton (NbBundle.getBundle (NbPresenter.class).getString ("HELP_OPTION_CAPTION"));
    private final JButton stdDetailButton = new JButton (NbBundle.getBundle (NbPresenter.class).getString ("HELP_OPTION_CAPTION"));
    {
        stdYesButton.setDefaultCapable (true);
        stdOKButton.setDefaultCapable (true);
        stdNoButton.setDefaultCapable (false);
        stdCancelButton.setDefaultCapable (false);
        stdHelpButton.setDefaultCapable (false);
        stdDetailButton.setDefaultCapable (false);
    }
    private final static String CANCEL_COMMAND = "Cancel"; // NOI18N

    private Component currentMessage;
    private JPanel currentButtonsPanel;
    private Component[] currentPrimaryButtons;
    private Component[] currentSecondaryButtons;

    /** useful only for DialogDescriptor */
    private int currentAlign;

    private ButtonListener buttonListener;
    /** Help context to actually associate with the dialog, as it is currently known. */
    private transient HelpCtx currentHelp = null;
    /** Used to prevent updateHelp from calling initializeButtons too many times. */
    private transient boolean haveCalledInitializeButtons = false;

    static final long serialVersionUID =-4508637164126678997L;

    /** Creates a new Dialog from specified NotifyDescriptor,
    * with given frame owner.
    * @param d The NotifyDescriptor to create the dialog from
    */
    public NbPresenter (NotifyDescriptor d, Frame owner, boolean modal) {
        super(owner, d.getTitle(), modal); // modal
        initialize(d);
    }

    /** Creates a new Dialog from specified NotifyDescriptor,
    * with given dialog owner.
    * @param d The NotifyDescriptor to create the dialog from
    */
    public NbPresenter (NotifyDescriptor d, Dialog owner, boolean modal) {
        super(owner, d.getTitle(), modal); // modal
        initialize(d);
    }

    private void initialize (NotifyDescriptor d) {
        descriptor = d;
        setDefaultCloseOperation (JDialog.DO_NOTHING_ON_CLOSE);
        buttonListener = new ButtonListener ();

        getRootPane().registerKeyboardAction (
            buttonListener,
            CANCEL_COMMAND,
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        initializeMessage ();

        updateHelp ();

        initializeButtons ();
        haveCalledInitializeButtons = true;

        descriptor.addPropertyChangeListener (this);
        addWindowListener (this);

        pack ();
        setLocationRelativeTo (null); // center on screen
    }

    private void initializeMessage () {
        if (currentMessage != null) {
            getContentPane ().remove (currentMessage);
        }

        Object message = descriptor.getMessage ();
        if (
            descriptor.getMessageType () == NotifyDescriptor.PLAIN_MESSAGE &&
            (message instanceof Component)
        ) {
            // if plain message => use directly the component
            currentMessage = (Component)message;
            getContentPane ().add (currentMessage, BorderLayout.CENTER);
        } else {
            currentMessage = createOptionPane ();
            getContentPane ().add (currentMessage, BorderLayout.CENTER);
        }

        currentMessage.requestFocus();
    }

    /** Creates option pane message.
    */
    private JOptionPane createOptionPane () {
        Object msg = descriptor.getMessage();
        if (msg instanceof String) {
            msg = org.openide.util.Utilities.replaceString ((String)msg, "\t", "    "); // NOI18N
            msg = org.openide.util.Utilities.replaceString ((String)msg, "\r", ""); // NOI18N
        }
        // initilize component
        JOptionPane optionPane = new JOptionPane(
                                     msg,
                                     descriptor.getMessageType(),
                                     0, // options type
                                     null, // icon
                                     new Object[0], // options
                                     null // value
                                 );
        optionPane.setWantsInput (false);

        return optionPane;
    }

    protected final void initializeButtons () {
        // -----------------------------------------------------------------------------
        // If there were any buttons previously, remove them and removeActionListener from them

        if (currentButtonsPanel != null) {
            if (currentPrimaryButtons != null) {
                for (int i = 0; i < currentPrimaryButtons.length; i++) {
                    modifyListener (currentPrimaryButtons[i], buttonListener, false);
                }
            }
            if (currentSecondaryButtons != null) {
                for (int i = 0; i < currentSecondaryButtons.length; i++) {
                    modifyListener (currentSecondaryButtons[i], buttonListener, false);
                }
            }

            getContentPane ().remove (currentButtonsPanel);

        }

        Object[] primaryOptions = descriptor.getOptions ();
        Object[] secondaryOptions = descriptor.getAdditionalOptions ();
        currentAlign = getOptionsAlign ();

        // -----------------------------------------------------------------------------
        // Obtain main (primary) and additional (secondary) buttons

        currentPrimaryButtons = null;
        currentSecondaryButtons = null;

        // explicitly provided options (AKA buttons)
        // JST: The following line causes only problems,
        //      I hope that my change will not cause additional ones ;-)
        //    if (descriptor.getOptionType () == NotifyDescriptor.DEFAULT_OPTION) {
        if (primaryOptions != null) {
            currentPrimaryButtons = new Component [primaryOptions.length];
            for (int i = 0; i < primaryOptions.length; i++) {
                if (primaryOptions[i] == NotifyDescriptor.YES_OPTION) {
                    currentPrimaryButtons[i] = stdYesButton;
                } else if (primaryOptions[i] == NotifyDescriptor.NO_OPTION) {
                    currentPrimaryButtons[i] = stdNoButton;
                } else if (primaryOptions[i] == NotifyDescriptor.OK_OPTION) {
                    currentPrimaryButtons[i] = stdOKButton;
                } else if (primaryOptions[i] == NotifyDescriptor.CANCEL_OPTION) {
                    currentPrimaryButtons[i] = stdCancelButton;
                } else if (primaryOptions[i] instanceof Component) {
                    currentPrimaryButtons[i] = (Component) primaryOptions [i];
                } else if (primaryOptions [i] instanceof Icon) {
                    JButton button = new JButton ((Icon)primaryOptions [i]);
                    currentPrimaryButtons[i] = button;
                } else {
                    JButton button = new JButton (primaryOptions[i].toString ());
                    currentPrimaryButtons[i] = button;
                }
            }
        } else { // predefined option types
            switch (descriptor.getOptionType ()) {
            case NotifyDescriptor.YES_NO_OPTION:
                currentPrimaryButtons = new Component[2];
                currentPrimaryButtons[0] = stdYesButton;
                currentPrimaryButtons[1] = stdNoButton;
                break;
            case NotifyDescriptor.YES_NO_CANCEL_OPTION:
                currentPrimaryButtons = new Component[3];
                currentPrimaryButtons[0] = stdYesButton;
                currentPrimaryButtons[1] = stdNoButton;
                currentPrimaryButtons[2] = stdCancelButton;
                break;
            case NotifyDescriptor.OK_CANCEL_OPTION:
            default:
                currentPrimaryButtons = new Component[2];
                currentPrimaryButtons[0] = stdOKButton;
                currentPrimaryButtons[1] = stdCancelButton;
                break;
            }
        }

        // Automatically add a help button if needed.
        if (currentHelp != null) {
            if (currentPrimaryButtons == null) currentPrimaryButtons = new Component[] { };
            Component[] cPB2 = new Component[currentPrimaryButtons.length + 1];
            System.arraycopy (currentPrimaryButtons, 0, cPB2, 0, currentPrimaryButtons.length);
            cPB2[currentPrimaryButtons.length] = stdHelpButton;
            currentPrimaryButtons = cPB2;
        }

        if ((secondaryOptions != null) && (secondaryOptions.length != 0)) {
            currentSecondaryButtons = new Component [secondaryOptions.length];
            for (int i = 0; i < secondaryOptions.length; i++) {
                if (secondaryOptions[i] == NotifyDescriptor.YES_OPTION) {
                    currentSecondaryButtons[i] = stdYesButton;
                } else if (secondaryOptions[i] == NotifyDescriptor.NO_OPTION) {
                    currentSecondaryButtons[i] = stdNoButton;
                } else if (secondaryOptions[i] == NotifyDescriptor.OK_OPTION) {
                    currentSecondaryButtons[i] = stdOKButton;
                } else if (secondaryOptions[i] == NotifyDescriptor.CANCEL_OPTION) {
                    currentSecondaryButtons[i] = stdCancelButton;
                } else if (secondaryOptions[i] instanceof Component) {
                    currentSecondaryButtons[i] = (Component) secondaryOptions [i];
                } else if (secondaryOptions [i] instanceof Icon) {
                    JButton button = new JButton ((Icon)secondaryOptions [i]);
                    currentSecondaryButtons[i] = button;
                } else {
                    JButton button = new JButton (secondaryOptions[i].toString ());
                    currentSecondaryButtons[i] = button;
                }
            }
        }

        // -----------------------------------------------------------------------------
        // Create panels for main (primary) and additional (secondary) buttons and add to content pane

        if (currentAlign == DialogDescriptor.BOTTOM_ALIGN || currentAlign == -1) {

            JPanel panelForPrimary = null;
            JPanel panelForSecondary = null;

            if (currentPrimaryButtons != null) {
                panelForPrimary = new JPanel ();

                if (currentAlign == -1) {
                    panelForPrimary.setLayout (new org.openide.awt.EqualFlowLayout ());
                } else {
                    panelForPrimary.setLayout (new org.openide.awt.EqualFlowLayout (FlowLayout.RIGHT));
                }
                for (int i = 0; i < currentPrimaryButtons.length; i++) {
                    modifyListener (currentPrimaryButtons[i], buttonListener, true); // add button listener
                    panelForPrimary.add (currentPrimaryButtons[i]);
                }
            }

            if (currentSecondaryButtons != null) {
                panelForSecondary = new JPanel ();
                panelForSecondary.setLayout (new org.openide.awt.EqualFlowLayout (FlowLayout.LEFT));
                for (int i = 0; i < currentSecondaryButtons.length; i++) {
                    modifyListener (currentSecondaryButtons[i], buttonListener, true); // add button listener
                    panelForSecondary.add (currentSecondaryButtons[i]);
                }
            }

            // both primary and secondary buttons are used
            if ((panelForPrimary != null) && (panelForSecondary != null)) {
                currentButtonsPanel = new JPanel ();
                currentButtonsPanel.setLayout (new BorderLayout ());
                currentButtonsPanel.add (panelForPrimary, BorderLayout.EAST);
                currentButtonsPanel.add (panelForSecondary, BorderLayout.WEST);
            } else if (panelForPrimary != null) {
                currentButtonsPanel = panelForPrimary;
            } else {
                currentButtonsPanel = panelForSecondary;
            }

            // add final button panel to the dialog
            if (currentButtonsPanel != null) {
                getContentPane ().add (currentButtonsPanel, BorderLayout.SOUTH);
            }

        } else if (currentAlign == DialogDescriptor.RIGHT_ALIGN) {
            currentButtonsPanel = new JPanel ();
            currentButtonsPanel.setLayout (new GridBagLayout ());
            GridBagConstraints gbc = new GridBagConstraints ();
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.weightx = 1.0f;
            gbc.insets = new Insets (5, 4, 2, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            if (currentPrimaryButtons != null) {
                for (int i = 0; i < currentPrimaryButtons.length; i++) {
                    modifyListener (currentPrimaryButtons[i], buttonListener, true); // add button listener
                    currentButtonsPanel.add (currentPrimaryButtons[i], gbc);
                }
            }

            GridBagConstraints padding = new GridBagConstraints ();
            padding.gridwidth = GridBagConstraints.REMAINDER;
            padding.weightx = 1.0f;
            padding.weighty = 1.0f;
            padding.fill = GridBagConstraints.BOTH;
            currentButtonsPanel.add (new JPanel (), padding);

            gbc.insets = new Insets (2, 4, 5, 5);
            if (currentSecondaryButtons != null) {
                for (int i = 0; i < currentSecondaryButtons.length; i++) {
                    modifyListener (currentSecondaryButtons[i], buttonListener, true); // add button listener
                    currentButtonsPanel.add (currentSecondaryButtons[i], gbc);
                }
            }

            // add final button panel to the dialog
            if (currentButtonsPanel != null) {
                getContentPane ().add (currentButtonsPanel, BorderLayout.EAST);
            }

        }
        updateDefaultButton ();
    }

    /** Checks default button and updates it
    */
    private void updateDefaultButton () {
        if (currentPrimaryButtons != null) {
            // finds default button
            for (int i = 0; i < currentPrimaryButtons.length; i++) {
                if (currentPrimaryButtons[i] instanceof JButton) {
                    JButton b = (JButton)currentPrimaryButtons[i];
                    if (b.isVisible() && b.isEnabled () && b.isDefaultCapable()) {
                        getRootPane ().setDefaultButton(b);
                        return;
                    }
                }
            }
        }
        getRootPane ().setDefaultButton (null);
    }

    private void modifyListener (Component comp, ButtonListener l, boolean add) {
        // on JButtons attach simply by method call
        if (comp instanceof JButton) {
            JButton b = (JButton)comp;
            if (add) {
                b.addActionListener (l);
                b.addComponentListener(l);
                b.addPropertyChangeListener(l);
            } else {
                b.removeActionListener (l);
                b.removeComponentListener(l);
                b.removePropertyChangeListener(l);
            }
            return;
        } else {
            // we will have to use dynamic method invocation to add the action listener
            // to generic component (and we succeed only if it has the addActionListener method)
            java.lang.reflect.Method m = null;
            try {
                m = comp.getClass ().getMethod (add ? "addActionListener" : "removeActionListener", new Class[] { ActionListener.class });// NOI18N
            } catch (NoSuchMethodException e) {
                m = null; // no jo, we cannot attach ActionListener to this Component
            } catch (SecurityException e2) {
                m = null; // no jo, we cannot attach ActionListener to this Component
            }
            if (m != null) {
                try {
                    m.invoke (comp, new Object[] { l });
                } catch (Exception e) {
                    // not succeeded, so give up
                }
            }
        }
    }

    public void show () {
        NbPresenter prev = currentModalDialog;
        currentModalDialog = this;
        super.show ();
        currentModalDialog = prev;
    }


    public void propertyChange(final java.beans.PropertyChangeEvent evt) {
        boolean update = false;

        if (DialogDescriptor.PROP_OPTIONS.equals (evt.getPropertyName ())) {
            initializeButtons ();
            update = true;
        } else if (DialogDescriptor.PROP_OPTION_TYPE.equals (evt.getPropertyName ())) {
            initializeButtons ();
            update = true;
        } else if (DialogDescriptor.PROP_OPTIONS_ALIGN.equals (evt.getPropertyName ())) {
            initializeButtons ();
            update = true;
        } else if (DialogDescriptor.PROP_MESSAGE.equals (evt.getPropertyName ())) {
            initializeMessage ();
            // In case change of help ID on component message:
            updateHelp ();
            update = true;
        } else if (DialogDescriptor.PROP_MESSAGE_TYPE.equals (evt.getPropertyName ())) {
            initializeMessage ();
            update = true;
        } else if (DialogDescriptor.PROP_TITLE.equals (evt.getPropertyName ())) {
            setTitle (descriptor.getTitle ());
        } else if (DialogDescriptor.PROP_HELP_CTX.equals (evt.getPropertyName ())) {
            updateHelp ();
            // In case buttons have changed: //just buttons!!
            currentButtonsPanel.revalidate();
            currentButtonsPanel.repaint();
        }

        if (update) {
            getContentPane ().repaint ();
            getContentPane ().validate ();
            pack ();

            setLocationRelativeTo (null);
        }
    }

    public Dimension getMinimumSize () {
        return new Dimension (400, 200);
    }

    private void updateHelp () {
        //System.err.println ("Updating help for NbDialog...");
        HelpCtx help = getHelpCtx ();
        // Handle help from the inner component automatically (see docs
        // in DialogDescriptor):
        if (HelpCtx.DEFAULT_HELP.equals (help)) {
            Object msg = descriptor.getMessage ();
            if (msg instanceof Component) {
                help = HelpCtx.findHelp ((Component) msg);
            }
            if (HelpCtx.DEFAULT_HELP.equals (help)) help = null;
        }
        if (! Utilities.compareObjects (currentHelp, help)) {
            currentHelp = help;
            if (help != null && help.getHelpID () != null) {
                //System.err.println ("New help ID for root pane: " + help.getHelpID ());
                HelpCtx.setHelpIDString (getRootPane (), help.getHelpID ());
            }
            // Refresh button list if it had already been created.
            if (haveCalledInitializeButtons) initializeButtons ();
        }
    }

    /** Options align.
    */
    protected int getOptionsAlign () {
        return -1;
    }

    /** Getter for button listener or null
    */
    protected ActionListener getButtonListener () {
        return null;
    }

    /** Closing options.
    */
    protected Object[] getClosingOptions () {
        return null;
    }

    /** Updates help.
    */
    protected HelpCtx getHelpCtx () {
        return null;
    }


    public void windowDeactivated(final java.awt.event.WindowEvent p1) {
    }
    public void windowClosed(final java.awt.event.WindowEvent p1) {
    }
    public void windowDeiconified(final java.awt.event.WindowEvent p1) {
    }
    public void windowOpened(final java.awt.event.WindowEvent p1) {
    }
    public void windowIconified(final java.awt.event.WindowEvent p1) {
    }
    public void windowClosing(final java.awt.event.WindowEvent p1) {
        descriptor.setValue (NotifyDescriptor.CLOSED_OPTION);
        dispose ();
    }
    public void windowActivated(final java.awt.event.WindowEvent p1) {
    }

    /** Button listener
    */
    private class ButtonListener extends Object
                implements ActionListener, java.awt.event.ComponentListener,
        java.beans.PropertyChangeListener {
        public void actionPerformed (ActionEvent evt) {
            Object pressedOption = evt.getSource ();
            // handle ESCAPE
            if (evt.getActionCommand() == CANCEL_COMMAND) {
                pressedOption = NotifyDescriptor.CLOSED_OPTION;
            } else {
                if (evt.getSource () == stdHelpButton) {
                    String sysprop = System.getProperty ("org.openide.actions.HelpAction.DEBUG"); // NOI18N
                    if ("true".equals (sysprop) || "full".equals (sysprop)) // NOI18N
                        System.err.println ("Help button showing: " + currentHelp); // NOI18N, please do not comment out
                    TopManager.getDefault ().showHelp (currentHelp);
                    return;
                }

                Object[] options = descriptor.getOptions ();
                if (
                    options != null &&
                    currentPrimaryButtons != null &&
                    options.length == currentPrimaryButtons.length
                ) {
                    for (int i = 0; i < currentPrimaryButtons.length; i++) {
                        if (evt.getSource () == currentPrimaryButtons[i]) {
                            pressedOption = options[i];
                        }
                    }
                }

                options = descriptor.getAdditionalOptions ();
                if (
                    options != null &&
                    currentSecondaryButtons != null &&
                    options.length == currentSecondaryButtons.length
                ) {
                    for (int i = 0; i < currentSecondaryButtons.length; i++) {
                        if (evt.getSource () == currentSecondaryButtons[i]) {
                            pressedOption = options[i];
                        }
                    }
                }

                if (evt.getSource () == stdYesButton) {
                    pressedOption = NotifyDescriptor.YES_OPTION;
                } else if (evt.getSource () == stdNoButton) {
                    pressedOption = NotifyDescriptor.NO_OPTION;
                } else if (evt.getSource () == stdCancelButton) {
                    pressedOption = NotifyDescriptor.CANCEL_OPTION;
                } else if (evt.getSource () == stdOKButton) {
                    pressedOption = NotifyDescriptor.OK_OPTION;
                }
            }
            descriptor.setValue (pressedOption);

            ActionListener al = getButtonListener ();
            if (al != null) {

                if (pressedOption == evt.getSource ()) {
                    al.actionPerformed (evt);
                } else {
                    al.actionPerformed (new ActionEvent (
                                            pressedOption, evt.getID (), evt.getActionCommand (), evt.getModifiers ()
                                        ));
                }
            }

            Object[] arr = getClosingOptions ();
            if (arr == null || pressedOption == NotifyDescriptor.CLOSED_OPTION) {
                // all options should close
                setVisible (false);
            } else {
                java.util.List l = java.util.Arrays.asList (arr);

                if (l.contains (pressedOption)) {
                    setVisible (false);
                }
            }
        }
        public void componentShown(final java.awt.event.ComponentEvent p1) {
            updateDefaultButton ();
        }
        public void componentResized(final java.awt.event.ComponentEvent p1) {
        }

        public void componentHidden(final java.awt.event.ComponentEvent p1) {
            updateDefaultButton ();
        }

        public void componentMoved(final java.awt.event.ComponentEvent p1) {
        }

        public void propertyChange(final java.beans.PropertyChangeEvent p1) {
            if ("enabled".equals (p1.getPropertyName())) {
                updateDefaultButton ();
            }
        }
    }

}

/*
* Log
*  12   Gandalf   1.11        1/18/00  Jaroslav Tulach Solves deadlock on 
*       Solaris 1.2.1
*  11   Gandalf   1.10        1/17/00  Petr Kuzel      Buttons revalidating 
*       fixed.
*  10   Gandalf   1.9         1/14/00  Jaroslav Tulach #5308
*  9    Gandalf   1.8         1/13/00  Jaroslav Tulach I18N
*  8    Gandalf   1.7         1/9/00   Jaroslav Tulach Again and better.
*  7    Gandalf   1.6         1/9/00   Jaroslav Tulach Stupid bug in finding 
*       pressed option.
*  6    Gandalf   1.5         1/7/00   David Simonek   better settings of dialog
*       owners
*  5    Gandalf   1.4         1/7/00   Jesse Glick     Debugging for Patrick.
*  4    Gandalf   1.3         1/5/00   Jaroslav Tulach Supports custom options
*  3    Gandalf   1.2         1/5/00   Jesse Glick     Fixed bug with Help 
*       button replacing rather than adding to default OK/Cancel.
*  2    Gandalf   1.1         1/4/00   Jesse Glick     Help button on right of 
*       dialogs, not left.
*  1    Gandalf   1.0         12/30/99 Jaroslav Tulach 
* $
* Log
*  12   Gandalf   1.11        1/18/00  Jaroslav Tulach Solves deadlock on 
*       Solaris 1.2.1
*  11   Gandalf   1.10        1/17/00  Petr Kuzel      Buttons revalidating 
*       fixed.
*  10   Gandalf   1.9         1/14/00  Jaroslav Tulach #5308
*  9    Gandalf   1.8         1/13/00  Jaroslav Tulach I18N
*  8    Gandalf   1.7         1/9/00   Jaroslav Tulach Again and better.
*  7    Gandalf   1.6         1/9/00   Jaroslav Tulach Stupid bug in finding 
*       pressed option.
*  6    Gandalf   1.5         1/7/00   David Simonek   better settings of dialog
*       owners
*  5    Gandalf   1.4         1/7/00   Jesse Glick     Debugging for Patrick.
*  4    Gandalf   1.3         1/5/00   Jaroslav Tulach Supports custom options
*  3    Gandalf   1.2         1/5/00   Jesse Glick     Fixed bug with Help 
*       button replacing rather than adding to default OK/Cancel.
*  2    Gandalf   1.1         1/4/00   Jesse Glick     Help button on right of 
*       dialogs, not left.
*  1    Gandalf   1.0         12/30/99 Jaroslav Tulach 
* $
*/
