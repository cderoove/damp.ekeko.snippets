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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** A dialog description.
 * It may be built later using {@link TopManager#createDialog}.
* It extends <code>NotifyDescriptor</code>'s capabilities by allowing specification of the
* modal/nonmodal state of the dialog, option alignment, help, and
* a listener on button presses.
* Anyone who want to display some kind of dialog with standard
* behavior should use this class to describe it and
* use <code>createDialog(...)</code> to build it.
* <p>The property <code>innerPane</code> (message in <code>NotifyDescriptor</code>) is primarily used here 
* to specify the inner component of the dialog, in contrast to <code>NotifyDescriptor</code>, though
* a String message can be used.
*
* @author Dafe Simonek
*/
public class DialogDescriptor extends NotifyDescriptor {
    // Property constants

    /** Name of property for alignment of options. */
    public static final String PROP_OPTIONS_ALIGN   = "optionsAlign"; // NOI18N
    /** Name of property for modality of dialog. */
    public static final String PROP_MODAL           = "modal"; // NOI18N
    /** Name of property for the help context. */
    public static final String PROP_HELP_CTX        = "helpCtx"; // NOI18N
    /** Name of property for the button listener. */
    public static final String PROP_BUTTON_LISTENER = "buttonListener"; // NOI18N
    /** Name of property for list of closing options. */
    public static final String PROP_CLOSING_OPTIONS   = "closingOptions"; // NOI18N

    // Constants

    /** Constant for message type property */
    /** Alignment to put options in the bottom part. */
    public static final int BOTTOM_ALIGN = 0;
    /** Alignment to place options vertically
    * in the right part. */
    public static final int RIGHT_ALIGN = 1;
    /** Alignment to place options in the default manner. */
    public static final int DEFAULT_ALIGN = BOTTOM_ALIGN;

    /** default closing options */
    private static final Object[] DEFAULT_CLOSING_OPTIONS = new Object[] {
                YES_OPTION, NO_OPTION, CANCEL_OPTION, OK_OPTION
            };

    // Properties

    /** RW property specifying modal status of the dialog */
    private boolean modal;
    /** RW property specifying options alignment,
    * possible values today are BOTTOM_ALIGN, RIGHT_ALIGN, DEFAULT_ALIGN */
    private int optionsAlign;
    /** RW property which specifies help context for the dialog */
    private HelpCtx helpCtx;
    /** RW property which specifies button listener for notifying
    * clients about button presses */
    private ActionListener buttonListener;
    /** array of options that close the dialog when pressed */
    private Object[] closingOptions = DEFAULT_CLOSING_OPTIONS;

    /** Create modal dialog descriptor with given title and inner part,
    * with ok/cancel buttons with default alignment,
    * no help available. All buttons will close the dialog and the getValue ()
    * will provide the pressed option.
    * @param innerPane inner component of the dialog, or String message
    * @param title title of the dialog
    */
    public DialogDescriptor(final Object innerPane, final String title) {
        this(innerPane, title, true, OK_CANCEL_OPTION, OK_OPTION,
             DEFAULT_ALIGN, null, null);
    }

    /** Create dialog descriptor with given title, inner part and modal status,
    * with ok/cancel buttons displayed with default alignment, no help available.
    * If <code>bl</code> is not <code>null</code>, then it will receive notifications when the user
    * presses the buttons. (If no listener is specified, it's still possible
    * to retrieve the user-selected button using {@link NotifyDescriptor#getValue}.)
    *
    * @param innerPane inner component of the dialog, or String message
    * @param title title of the dialog
    * @param modal modal status
    * @param bl listener for user's button presses
    */
    public DialogDescriptor(final Object innerPane, final String title,
                            final boolean isModal,
                            final ActionListener bl) {
        this(innerPane, title, isModal, OK_CANCEL_OPTION, OK_OPTION,
             DEFAULT_ALIGN, null, bl);
    }

    /** Create dialog descriptor with given title, inner part, modal status,
    * option type and default option. Options have default alignment, no help available.
    * If the action listener is null, all option buttons will close the dialog and the 
    * getValue () will provide the pressed option.
    * @param innerPane inner component of the dialog, or String message
    * @param title title of the dialog
    * @param modal modal status
    * @param optionType one of the standard options (<code>OK_CANCEL_OPTION</code>, ...)
    * @param initialValue default option (default button)
    * @param bl listener for the user's button presses or null for default close action on all options
    */
    public DialogDescriptor(final Object innerPane, final String title,
                            final boolean isModal, final int optionType,
                            final Object initialValue,
                            final ActionListener bl) {
        this(innerPane, title, isModal, optionType, initialValue,
             DEFAULT_ALIGN, null, bl);
    }

    /** Create dialog descriptor; possibility of specifying custom
    * array of options and their alignment.  If the action listener is null, 
    * all option buttons will close the dialog and the getValue ()
    * will provide the pressed option.
    * When custom optionsnset is provided, if any of the standard options
    * (OK_OPTION, CLOSE_OPTION or CANCEL_OPTION) is used, the dialog will close when
    * the button for this option is pressed, for custom options, closing the dialog is left 
    * for the ActionListener.
    * @param innerPane inner component of the dialog, or String message
    * @param title title of the dialog
    * @param modal modal status
    * @param options array of custom options (<code>null</code> means no options at all)
    * @param initialValue default option from custom option array
    * @param optionsAlign specifies where to place
    *   options in the dialog
    * @param helpCtx help context specifying help page
    * @param bl listener for the user's button presses or null for default close action on all options
    */
    public DialogDescriptor(final Object innerPane, final String title,
                            final boolean modal, final Object[] options,
                            final Object initialValue, final int optionsAlign,
                            final HelpCtx helpCtx,
                            final ActionListener bl
                           ) {
        super(
            innerPane, title, DEFAULT_OPTION, PLAIN_MESSAGE, options, initialValue
        );
        this.modal = modal;
        this.optionsAlign = optionsAlign;
        this.helpCtx = helpCtx == null ? HelpCtx.DEFAULT_HELP : helpCtx;
        this.buttonListener = bl;
    }

    /** Create dialog descriptor.
    * If the action listener is null, all option buttons will close the dialog and the 
    * getValue () will provide the pressed option.
    *
    * @param innerPane inner component of the dialog, or String message
    * @param title title of the dialog
    * @param modal modal status
    * @param optionType one of the standard options (<code>OK_CANCEL_OPTION</code>, ...)
    * @param initialValue default option (default button)
    * @param optionsAlign specifies where to place
    *   options in the dialog
    * @param helpCtx help context specifying help page
    * @param bl listener for the user's button presses or null for default close action on all options
    */
    public DialogDescriptor(final Object innerPane, final String title,
                            final boolean isModal, final int optionType,
                            final Object initialValue, final int optionsAlign,
                            final HelpCtx helpCtx,
                            final ActionListener bl) {
        super(innerPane, title, optionType, PLAIN_MESSAGE,
              null, initialValue
             );
        this.modal = isModal;
        this.optionsAlign = optionsAlign;
        this.helpCtx = helpCtx == null ? HelpCtx.DEFAULT_HELP : helpCtx;
        this.buttonListener = bl;
    }

    /** Get current option alignment.
    * @return current option alignment
    * @see #setOptionsAlign
    */
    public int getOptionsAlign () {
        return optionsAlign;
    }

    /** Set new option alignment. See aligment constants for
    * possible values.
    * Fires property change event if successful.
    *
    * @param optionsAlign new options alignment
    * @throws IllegalArgumentException when unknown alignment is given
    * @see #DEFAULT_ALIGN
    */
    public void setOptionsAlign (final int optionsAlign) {
        if ((optionsAlign != BOTTOM_ALIGN) && (optionsAlign != RIGHT_ALIGN))
            throw new IllegalArgumentException(
                NbBundle.getBundle (DialogDescriptor.class).getString("EXC_OptionsAlign")
            );
        if (this.optionsAlign == optionsAlign)
            return;
        int oldValue = this.optionsAlign;
        this.optionsAlign = optionsAlign;
        firePropertyChange(PROP_OPTIONS_ALIGN, new Integer(oldValue),
                           new Integer(optionsAlign));
    }

    /** Get modal status.
    * @return modal status
    * @see #setModal
    */
    public boolean isModal () {
        return modal;
    }

    /** Set new modal status.
    * Fires property change event if successful.
    *
    * @param modal new modal status
    * @see #isModal
    */
    public void setModal (final boolean modal) {
        if (this.modal == modal)
            return;
        boolean oldModal = this.modal;
        this.modal = modal;
        firePropertyChange(PROP_MODAL, new Boolean(oldModal),
                           new Boolean(modal));
    }

    /** Setter for list of options that close the dialog.
    * Special values are:
    * <UL>
    *   <LI>null - all options will close the dialog
    *   <LI>empty array - no option will close the dialog
    * </UL>
    * @param arr array of options that should close the dialog when pressed
    *    if null then all options close the dialog
    */
    public void setClosingOptions (Object[] arr) {
        Object[] old = closingOptions;
        closingOptions = arr;

        firePropertyChange (PROP_CLOSING_OPTIONS, old, arr);
    }

    /** Getter for list of closing options.
    * @return array of options or null
    */
    public Object[] getClosingOptions () {
        return closingOptions;
    }

    /** Get current help context asociated with this dialog
    * descriptor.
    * @return current help context
    * @see #setHelpCtx
    */
    public HelpCtx getHelpCtx () {
        return helpCtx;
    }

    /** Set new help context for this dialog descriptor.
    * Fires property change event if successful.
    * <p>The implementation should automatically display a help
    * button among the secondary options, without your needing to
    * specify it, if the help context on the descriptor is neither
    * <code>null</code> nor {@link HelpCtx#DEFAULT_HELP}. If the
    * descriptor is <code>null</code>, this feature will be disabled
    * (you can still add your own help button manually if you wish,
    * of course). If <code>DEFAULT_HELP</code> (the default), normally the button
    * will also be disabled, however if the inner pane is a component
    * and this component has an {@link HelpCtx#findHelp associated}
    * help ID, that will be used automatically. So most users should never
    * need to manually add a help button: call this method with the correct
    * context, or associate an ID with the displayed component. Note that to
    * set it to <code>null</code> you must explicitly call this method; passing
    * <code>null</code> in the constructor actually sets it to <code>DEFAULT_HELP</code>.
    *
    * @param helpCtx new help context, can be <code>null</code> (no help)
    * @see #getHelpCtx
    */
    public void setHelpCtx (final HelpCtx helpCtx) {
        if ((this.helpCtx != null) && (this.helpCtx.equals(helpCtx)))
            return;
        HelpCtx oldHelpCtx = this.helpCtx;
        this.helpCtx = helpCtx;
        firePropertyChange(PROP_HELP_CTX, oldHelpCtx, helpCtx);
    }

    /** Get button listener which listens for the user's button presses.
    * @return current button listener instance or null
    * @see #setButtonListener
    */
    public ActionListener getButtonListener () {
        return buttonListener;
    }

    /** Set new button listener instance for this dialog descriptor.
    * Fires property change event if successful.
    *
    * @param l new button listener. It may be <code>null</code>, in which case listening is cancelled.
    * @see #getButtonListener
    */
    public void setButtonListener (final ActionListener l) {
        if (this.buttonListener == l)
            return;
        ActionListener oldButtonListener = this.buttonListener;
        this.buttonListener = l;
        firePropertyChange(PROP_BUTTON_LISTENER, oldButtonListener, l);
    }
}

/*
* Log
*  18   Gandalf   1.17        1/12/00  Ian Formanek    NOI18N
*  17   Gandalf   1.16        1/5/00   Jaroslav Tulach Deleted all 
*       NotifyDescriptior constructors that take Icon as argument.
*  16   Gandalf   1.15        12/15/99 Jesse Glick     There is a now a Help 
*       button automatically added to any dialog (though not notification 
*       dialogs) which provides help either explicitly or on its inner 
*       component.
*  15   Gandalf   1.14        11/4/99  Jaroslav Tulach DialogDescriptor.setClosingOptions
*        
*  14   Gandalf   1.13        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  13   Gandalf   1.12        6/24/99  Jesse Glick     Gosh-honest HelpID's.
*  12   Gandalf   1.11        6/9/99   Ian Formanek    Clarification of custom 
*       options usage
*  11   Gandalf   1.10        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  10   Gandalf   1.9         6/7/99   Ian Formanek    Clarified semantics of 
*       ActionListener
*  9    Gandalf   1.8         3/30/99  Jesse Glick     [JavaDoc]
*  8    Gandalf   1.7         3/29/99  Ian Formanek    Added property names 
*       constants
*  7    Gandalf   1.6         3/26/99  Ian Formanek    Fixed use of obsoleted 
*       NbBundle.getBundle (this)
*  6    Gandalf   1.5         3/20/99  Jaroslav Tulach WizardDescriptor changes
*  5    Gandalf   1.4         3/19/99  Ian Formanek    Fixed modal dialogs
*  4    Gandalf   1.3         3/18/99  Ian Formanek    
*  3    Gandalf   1.2         3/18/99  Ian Formanek    Modified comments
*  2    Gandalf   1.1         3/3/99   Jesse Glick     [JavaDoc]
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
