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

package org.openide;

import java.awt.event.*;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Implements a basic "wizard" GUI system.
* A list of <em>wizard panels</em> may be specified and these
* may be traversed at the proper times using the "Previous"
* and "Next" buttons (or "Finish" on the last one).
*
* @author Ian Formanek, Jaroslav Tulach
*/
public class WizardDescriptor extends DialogDescriptor {
    /** "Next" button option.
    * @see #setOptions */
    public static final Object NEXT_OPTION = new Object ();
    /** "Finish" button option.
    * @see #setOptions */
    public static final Object FINISH_OPTION = OK_OPTION;
    /** "Previous" button option.
    * @see #setOptions */
    public static final Object PREVIOUS_OPTION = new Object ();

    /** real buttons to be placed instead of the options */
    private final JButton nextButton = new JButton ();
    private final JButton finishButton = new JButton ();
    private final JButton cancelButton = new JButton ();
    private final JButton previousButton = new JButton ();

    private static final ActionListener CLOSE_PREVENTER = new ActionListener () {
                public void actionPerformed (ActionEvent evt) {
                }
            };

    {
        // button init
        ResourceBundle b = NbBundle.getBundle ("org.openide.Bundle"); // NOI18N
        nextButton.setText (b.getString ("CTL_NEXT"));
        previousButton.setText (b.getString ("CTL_PREVIOUS"));
        finishButton.setText (b.getString ("CTL_FINISH"));
        cancelButton.setText (b.getString ("CTL_CANCEL"));

        finishButton.setDefaultCapable (true);
        nextButton.setDefaultCapable (true);
        previousButton.setDefaultCapable (false);
        cancelButton.setDefaultCapable (false);
    }

    /** Iterator between panels in the wizard */
    private Iterator panels;

    /** Change listener that invokes method update state */
    private Listener listener;

    /** current panel */
    private Panel current;

    /** settings to be used for the panels */
    private Object settings;

    /** message format to create title of the document */
    private MessageFormat titleFormat;

    /** hashtable with additional settings that is usually used
    * by Panels to store their data
    * @associates Object
    */
    private Map properties;

    /** Create a new wizard from a fixed list of panels, passing some settings to the panels.
    * @param wizardPanels the panels to use
    * @param settings the settings to pass to panels, or <code>null</code>
    * @see #WizardDescriptor(WizardDescriptor.Iterator, Object)
    */
    public WizardDescriptor (Panel[] wizardPanels, Object settings) {
        this (new ArrayIterator (wizardPanels), settings);
    }

    /** Create a new wizard from a fixed list of panels with settings
    * defaulted to <CODE>this</CODE>.
    *
    * @param wizardPanels the panels to use
    * @see #WizardDescriptor(WizardDescriptor.Iterator, Object)
    */
    public WizardDescriptor (Panel[] wizardPanels) {
        // passing CLOSE_PREVENTER which is treated especially
        this (wizardPanels, CLOSE_PREVENTER);
    }

    /** Create wizard for a sequence of panels, passing some settings to the panels.
    * @param panels iterator over all {@link WizardDescriptor.Panel}s that can appear in the wizard
    * @param settings the settings to provide to the panels (may be any data understood by them)
    * @see WizardDescriptor.Panel#readSettings
    * @see WizardDescriptor.Panel#storeSettings
    */
    public WizardDescriptor (Iterator panels, Object settings) {
        super ("", "", true, DEFAULT_OPTION, null, CLOSE_PREVENTER); // NOI18N
        this.settings = settings == CLOSE_PREVENTER ? this : settings;

        listener = new Listener ();

        nextButton.addActionListener (listener);
        previousButton.addActionListener (listener);
        finishButton.addActionListener (listener);
        cancelButton.addActionListener (listener);

        super.setOptions (new Object[] { previousButton, nextButton, finishButton, cancelButton });
        super.setClosingOptions (new Object[] { finishButton, cancelButton });

        setPanels (panels);
    }

    /** Create wizard for a sequence of panels, with settings
    * defaulted to <CODE>this</CODE>.
    *
    * @param panels iterator over all {@link WizardDescriptor.Panel}s that can appear in the wizard
    */
    public WizardDescriptor (Iterator panels) {
        // passing CLOSE_PREVENTER which is treated especially
        this (panels, CLOSE_PREVENTER);
    }

    /** Set a different list of panels.
    * Correctly updates the buttons.
    * @param panels the new list of {@link WizardDescriptor.Panel}s 
    */
    public final synchronized void setPanels (Iterator panels) {
        if (panels != null) {
            panels.removeChangeListener (listener);
        }
        this.panels = panels;
        panels.addChangeListener (listener);

        updateState ();
    }

    /** Set options permitted by the wizard considered as a <code>DialogDescriptor</code>.
    * Substitutes tokens such as {@link #NEXT_OPTION} with the actual button.
    *
    * @param options the options to set
    */
    public void setOptions (Object[] options) {
        super.setOptions (convertOptions (options));
    }

    /**
    * @param options the options to set
    */
    public void setAdditionalOptions (Object[] options) {
        super.setAdditionalOptions (convertOptions (options));
    }

    /**
    * @param options the options to set
    */
    public void setClosingOptions (Object[] options) {
        super.setClosingOptions (convertOptions (options));
    }

    /** Converts some options.
    */
    private Object[] convertOptions (Object[] options) {
        options = (Object[])options.clone ();
        for (int i = options.length - 1; i >= 0; i--) {
            if (options[i] == NEXT_OPTION) options[i] = nextButton;
            if (options[i] == PREVIOUS_OPTION) options[i] = previousButton;
            if (options[i] == FINISH_OPTION) options[i] = finishButton;
            if (options[i] == CANCEL_OPTION) options[i] = cancelButton;
        }
        return options;
    }

    /** Sets the message format to create title of the wizard.
    * The format can take two parameters. The name of the
    * current component and the name returned by the iterator that
    * defines the order of panels. The default value is something
    * like
    * <PRE>
    *   {0} wizard {1}
    * </PRE>
    * That can be expanded to something like this
    * <PRE>
    *   EJB wizard (1 of 8)
    * </PRE>
    * This method allows anybody to provide own title format.
    * 
    * @param format message format to the title
    */
    public void setTitleFormat (MessageFormat format) {
        titleFormat = format;
        updateState ();
    }

    /** Getter for current format to be used to format title.
    * @return the format
    * @see #setTitleFormat
    */
    public MessageFormat getTitleFormat () {
        if (titleFormat == null) {
            synchronized (this) {
                if (titleFormat == null) {
                    // ok, initialize the default one
                    titleFormat = new MessageFormat (NbBundle.getBundle (
                                                         WizardDescriptor.class
                                                     ).getString ("CTL_WizardName"));
                }
            }
        }
        return titleFormat;
    }

    /** Allows Panels that use WizardDescriptor as settings object to
    * store additional settings into it.
    *
    * @param name name of the property
    * @param value value of property
    */
    public synchronized void putProperty (String name, Object value) {
        if (properties == null) {
            properties = new HashMap (7);
        }
        properties.put (name, value);
    }

    /** Getter for stored property.
    * @param name name of the property
    * @return the value
    */
    public synchronized Object getProperty (String name) {
        return properties == null ? null : properties.get (name);
    }

    /** Updates buttons to reflect the current state of the panels.
    * Can be overridden by subclasses
    * to change the options to special values. In such a case use:
    * <p><code><PRE>
    *   super.updateState ();
    *   setOptions (...);
    * </PRE></code>
    */
    protected synchronized void updateState () {
        boolean next = panels.hasNext ();
        boolean prev = panels.hasPrevious ();
        Panel p = panels.current ();
        // listeners on the panel
        if (current != p) {
            if (current != null) {
                // remove
                current.removeChangeListener (listener);
                current.storeSettings (settings);
            }

            // add to new
            p.addChangeListener (listener);

            current = p;
            current.readSettings (settings);
        }

        boolean valid = p.isValid ();

        nextButton.setEnabled (next && valid);
        previousButton.setEnabled (prev);
        finishButton.setEnabled (
            valid &&
            (!next || (current instanceof FinishPanel))
        );

        //    nextButton.setVisible (next);
        //    finishButton.setVisible (!next || (current instanceof FinishPanel));

        if (next) {
            setValue (nextButton);
        } else {
            setValue (finishButton);
        }

        setHelpCtx (p.getHelp ());

        java.awt.Component c = p.getComponent ();
        String panelName = c.getName ();
        if (panelName == null) {
            panelName = ""; // NOI18N
        }

        if (c != getMessage ()) {
            setMessage (c);
        }

        Object[] args = {
            panelName,
            panels.name ()
        };
        MessageFormat mf = getTitleFormat ();
        setTitle (mf.format (args));
    }

    /** Iterator on the sequence of panels.
    * @see WizardDescriptor.Panel
    */
    public interface Iterator {
        /** Get the current panel.
        * @return the panel
        */
        public Panel current ();

        /** Get the name of the current panel.
        * @return the name
        */
        public String name ();

        /** Test whether there is a next panel.
        * @return <code>true</code> if so
        */
        public boolean hasNext ();

        /** Test whether there is a previous panel.
        * @return <code>true</code> if so
        */
        public boolean hasPrevious ();

        /** Move to the next panel.
        * I.e. increment its index, need not actually change any GUI itself.
        * @exception NoSuchElementException if the panel does not exist
        */
        public void nextPanel ();

        /** Move to the previous panel.
        * I.e. decrement its index, need not actually change any GUI itself.
        * @exception NoSuchElementException if the panel does not exist
        */
        public void previousPanel ();

        /** Add a listener to changes of the current panel.
        * The listener is notified when the possibility to move forward/backward changes.
        * @param l the listener to add
        */
        public void addChangeListener (ChangeListener l);

        /** Remove a listener to changes of the current panel.
        * @param l the listener to remove
        */
        public void removeChangeListener (ChangeListener l);
    }

    /** One wizard panel with a component on it. */
    public interface Panel {
        /** Get the component displayed in this panel.
        * @return the component
        */
        public java.awt.Component getComponent ();

        /** Help for this panel.
        * When the panel is active, this is used as the help for the wizard dialog.
        * @return the help or <code>null</code> if no help is supplied
        */
        public HelpCtx getHelp ();

        /** Provides the wizard panel with the current data--either
        * the default data or already-modified settings, if the user used the previous and/or next buttons.
        * This method can be called multiple times on one instance of <code>WizardDescriptor.Panel</code>.
        * @param settings the object representing wizard panel state, as originally supplied to {@link WizardDescriptor#WizardDescriptor(WizardDescriptor.Iterator,Object)}
        */
        public void readSettings (Object settings);

        /** Provides the wizard panel with the opportunity to update the
        * settings with its current customized state.
        * Rather than updating its settings with every change in the GUI, it should collect them,
        * and then only save them when requested to by this method.
        * Also, the original settings passed to {@link #readSettings} should not be modified (mutated);
        * rather, the (copy) passed in here should be mutated according to the collected changes.
        * This method can be called multiple times on one instance of <code>WizardDescriptor.Panel</code>.
        * @param settings the object representing a settings of the wizard
        */
        public void storeSettings (Object settings);

        /** Test whether the panel is finished and it is safe to proceed to the next one.
        * If the panel is valid, the "Next" (or "Finish") button will be enabled.
        * @return <code>true</code> if the user has entered satisfactory information
        */
        public boolean isValid ();

        /** Add a listener to changes of the panel's validity.
        * @param l the listener to add
        * @see #isValid
        */
        public void addChangeListener (ChangeListener l);

        /** Remove a listener to changes of the panel's validity.
        * @param l the listener to remove
        */
        public void removeChangeListener (ChangeListener l);
    }

    /** A special interface for panels in middle of the
    * iterators path that would like to have the finish button
    * enabled. So both Next and Finish are enabled on panel
    * implementing this interface.
    */
    public interface FinishPanel extends Panel {
    }

    /** Special iterator that works on an array of <code>Panel</code>s.
    */
    public static class ArrayIterator extends Object implements Iterator {
        /** Array of items.
        */
        private Panel[] panels;

        /** Index into the array
        */
        private int index;

        /** Construct an iterator.
        * @param array the list of panels to use
        */
        public ArrayIterator (Panel[] array) {
            panels = array;
            index = 0;
        }

        /* The current panel.
        */
        public Panel current () {
            return panels[index];
        }

        /* Current name of the panel */
        public String name () {
            Object[] args = {
                new Integer (index + 1),
                new Integer (panels.length)
            };
            MessageFormat mf = new MessageFormat (NbBundle.getBundle (WizardDescriptor.class).getString ("CTL_ArrayIteratorName"));
            return mf.format (args);
        }

        /* Is there a next panel?
        * @return true if so
        */
        public boolean hasNext () {
            return index < panels.length - 1;
        }

        /* Is there a previous panel?
        * @return true if so
        */
        public boolean hasPrevious () {
            return index > 0;
        }

        /* Moves to the next panel.
        * @exception NoSuchElementException if the panel does not exist
        */
        public synchronized void nextPanel () {
            if (index + 1 == panels.length) throw new java.util.NoSuchElementException ();
            index++;
        }

        /* Moves to previous panel.
        * @exception NoSuchElementException if the panel does not exist
        */
        public synchronized void previousPanel () {
            if (index == 0) throw new java.util.NoSuchElementException ();
            index--;
        }

        /* Ignores the listener, there are no changes in order of panels.
        */
        public void addChangeListener (ChangeListener l) {
        }

        /* Ignored.
        */
        public void removeChangeListener (ChangeListener l) {
        }
    }

    /** Listener to changes in the iterator and panels.
    */
    private final class Listener extends Object
        implements ChangeListener, ActionListener {
        /** Change in the observed objects */
        public void stateChanged (ChangeEvent ev) {
            updateState ();
        }
        /** Action listener */
        public void actionPerformed (ActionEvent ev) {
            if (ev.getSource () == nextButton) {
                panels.nextPanel ();
                updateState ();
            }

            if (ev.getSource () == previousButton) {
                panels.previousPanel ();
                updateState ();
            }

            if (ev.getSource () == finishButton) {
                current.storeSettings (settings);
                setValue (OK_OPTION);
            }

            if (ev.getSource () == cancelButton) {
                current.storeSettings (settings);
                setValue (CANCEL_OPTION);
            }
        }
    }
}

/*
* Log
*  19   src-jtulach1.18        1/13/00  Ian Formanek    NOI18N
*  18   src-jtulach1.17        1/8/00   Jaroslav Tulach 
*  17   src-jtulach1.16        1/8/00   Jaroslav Tulach 
*  16   src-jtulach1.15        12/15/99 Jesse Glick     There is a now a Help 
*       button automatically added to any dialog (though not notification 
*       dialogs) which provides help either explicitly or on its inner 
*       component.
*  15   src-jtulach1.14        12/9/99  Jaroslav Tulach Can be used more than 
*       once.
*  14   src-jtulach1.13        11/25/99 Jaroslav Tulach Both Next & Finish are 
*       always visible.
*  13   src-jtulach1.12        11/24/99 Jaroslav Tulach New "New From Template" 
*       Dialog
*  12   src-jtulach1.11        11/4/99  Jaroslav Tulach DialogDescriptor.setClosingOptions
*        
*  11   src-jtulach1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  10   src-jtulach1.9         8/10/99  Martin Ryzl     isValid and readSettings 
*       order in updateState changed
*  9    src-jtulach1.8         6/24/99  Jesse Glick     Gosh-honest HelpID's.
*  8    src-jtulach1.7         6/11/99  Slavek Psenicka storeSettings was not 
*       called at last pane
*  7    src-jtulach1.6         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  6    src-jtulach1.5         6/8/99   Ian Formanek    oved here from package 
*       awt
*  5    src-jtulach1.4         6/8/99   Ian Formanek    Close on button press 
*       prevented
*  4    src-jtulach1.3         6/4/99   Jaroslav Tulach Improved NbDialog.
*  3    src-jtulach1.2         6/3/99   Ian Formanek    
*  2    src-jtulach1.1         5/15/99  Jesse Glick     [JavaDoc]
*  1    src-jtulach1.0         3/20/99  Jaroslav Tulach 
* $
*/
