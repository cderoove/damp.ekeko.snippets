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

package org.netbeans.core.awt;

import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.*;

import org.openide.util.NbBundle;

/** The ButtonBar is a bar that contains buttons.
* The button can be oriented both horizontally (default) or vertically.
* There are two groups of buttons - the left(bottom) group is aligned to the
* left(bottom) edge of the ButtonBar and the right(top) group is aligned to the
* right(top) edge (you would probably guess so, would not you?).
* The buttons are currently identified by strings - the two groups
* are passed to the ButtonBar in the constructor and the ButtonBar fires
* a ActionEvent with the name of the button as the ActionCommand.
*
* @author   Ian Formanek, Petr Hamernik
* @version  0.35, May 31, 1998
*/
public class ButtonBar extends JPanel {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -5909692079369563652L;
    /** Constant for a preset configuration with no buttons */
    public static final int EMPTY         = 0;
    /** Constant for a preset configuration with OK and Canel buttons */
    public static final int OK_CANCEL     = 1;
    /** Constant for a preset configuration with Close button */
    public static final int CLOSE         = 2;
    /** Constant for a preset configuration with Yes and No buttons */
    public static final int YES_NO        = 3;
    /** Constant for a preset configuration with Yes, No and Cancel buttons */
    public static final int YES_NO_CANCEL = 4;
    /** Constant for a preset configuration with Details and Close buttons */
    public static final int DETAILS_CLOSE = 5;

    /** Default color of the text of the buttons */
    public static final Color DEFAULT_FOREGROUND = new Color(200, 200, 255);
    /** Default color of the button bar background */
    public static final Color DEFAULT_BACKGROUND = Color.blue.darker().darker();

    /** Default font of the text of the button */
    public static final Font DEFAULT_FONT  = new Font("SansSerif", Font.PLAIN, 18); // NOI18N

    /** Constant for Single-side button mode - aligned to the right (top) */
    public static final int SINGLE_RIGHT = 0;
    /** Constant for Single-side button mode - aligned to the left (bottom) */
    public static final int SINGLE_LEFT = 1;
    /** Constant for Single-side button mode - centered */
    public static final int SINGLE_CENTER = 2;

    /** Default border between buttons */
    public static final int BUTTONS_GAP = 6;
    public static final int SECTIONS_GAP  = 40;

    /** Orientation constants */
    public static final int HORIZONTAL = 0; // left to right orientation
    public static final int VERTICAL = 1; // up to bottom orientation

    /** Default button in the button bar */
    private ButtonBarButton defaultButton;

    /** Does the peer exist? It's modified in add/removeNotify */
    private boolean peerExist;

    /** Orientation of the button bar */
    private int orientation;

    /** Constructs an empty button bar. */
    public ButtonBar() {
        this(EMPTY);
    }

    /** Constructs a new button bar in given preset configuration.
    * @param preset desired preset configuration (OK_CANCEL, CLOSE, ...)
    */
    public ButtonBar(int preset) {
        this(preset, HORIZONTAL);
    }

    /** Constructs a new button bar in given preset configuration.
    * @param preset desired preset configuration (OK_CANCEL, CLOSE, ...)
    * @param orientation buttons orientation (HORIZONTAL, VERTICAL)
    */
    public ButtonBar(int preset, int orientation) {
        this.orientation = orientation;
        String[] left = null;
        String[] right = null;
        String def = ""; // NOI18N
        ResourceBundle bundle = NbBundle.getBundle(ButtonBar.class);

        switch (preset) {
        case OK_CANCEL:
            right = new String[2];
            right[0] = bundle.getString("OKButton");
            right[1] = bundle.getString("CancelButton");
            def = right[0];
            break;
        case CLOSE:
            right = new String[1];
            right[0] = bundle.getString("CloseButton");
            def = right[0];
            break;
        case YES_NO:
            right = new String[2];
            right[0] = bundle.getString("YesButton");
            right[1] = bundle.getString("NoButton");
            def = right[0];
            break;
        case YES_NO_CANCEL:
            right = new String[3];
            right[0] = bundle.getString("YesButton");
            right[1] = bundle.getString("NoButton");
            right[2] = bundle.getString("CancelButton");
            def = right[0];
            break;
        case DETAILS_CLOSE:
            left = new String[1];
            left[0] = bundle.getString("DetailsButton");
            right = new String[1];
            right[0] = bundle.getString("CloseButton");
            def = right[0];
            break;
        default:
        case EMPTY: break;
        }
        initButtons(left, right, def);
    }

    /** Constructs a new button bar with buttons with specified labels.
    * @param left an array of labels for the buttons to be aligned to the left (bottom)
    * @param right an array of labels for the buttons to be aligned to the right (top)
    */
    public ButtonBar(String[] left, String[] right) {
        this(left, right, "", HORIZONTAL); // NOI18N
    }

    /** Constructs a new button bar with buttons with specified labels.
    * @param left an array of labels for the buttons to be aligned to the left (bottom)
    * @param right an array of labels for the buttons to be aligned to the right (top)
    * @param defaultButton Label of the button which should be default.
    */
    public ButtonBar(String[] left, String[] right, String defaultButton) {
        this(left, right, defaultButton, HORIZONTAL);
    }

    /** Constructs a new button bar with buttons with specified labels.
    * @param left an array of labels for the buttons to be aligned to the left (bottom)
    * @param right an array of labels for the buttons to be aligned to the right (top)
    * @param defaultButton Label of the button which should be default.
    * @param orientation buttons orientation (HORIZONTAL, VERTICAL)
    */
    public ButtonBar(String[] left, String[] right, String defaultButton, int orientation) {

        this.orientation = orientation;
        initButtons(left, right, defaultButton);
    }

    /** Constructs a new button bar with specified buttons.
    * @param left an array of buttons to be aligned to the left (bottom)
    * @param right an array of buttons to be aligned to the right (top)
    */
    public ButtonBar(ButtonBarButton[] left, ButtonBarButton[] right) {
        this(left, right, HORIZONTAL);
    }

    /** Constructs a new button bar with specified buttons and orientation.
    * @param left an array of buttons to be aligned to the left (bottom)
    * @param right an array of buttons to be aligned to the right (top)
    * @param orientation buttons orientation (HORIZONTAL, VERTICAL)
    */
    public ButtonBar(ButtonBarButton[] left, ButtonBarButton[] right, int orientation) {

        this.orientation = orientation;
        initButtons(left, right);
    }

    /** Constructs a new button bar with specified buttons aligned to the right. (top)
    * @param right an array of buttons to be aligned to the right (top)
    */
    public ButtonBar(ButtonBarButton[] right) {
        this(null, right, HORIZONTAL);
    }

    /** Inistializes the buttons - called from the string constructors.
    * @param left an array of labels for the buttons to be aligned to the left (bottom)
    *             or null if buttons should be placed to the right (top)
    * @param right an array of labels for the buttons to be aligned to the right (top)
    *             or null if buttons should be placed to the left (bottom)
    * @param defaultButton Label of the button which should be default.
    */
    private void initButtons(String[] left, String[] right, String defaultButton) {

        ButtonBarButton[] lefts = null;
        ButtonBarButton[] rights = null;

        if (left != null) {
            lefts = new ButtonBarButton[left.length];
            for (int i=0; i<left.length; i++) {
                lefts[i] = new ButtonBarButton(left[i]);
                if (left[i].equals(defaultButton))
                    setDefaultButton(lefts[i]);
            }
        }

        if (right != null) {
            rights = new ButtonBarButton[right.length];
            for (int i=0; i<right.length; i++) {
                rights[i] = new ButtonBarButton(right[i]);
                if (right[i].equals(defaultButton))
                    setDefaultButton(rights[i]);
            }
        }

        initButtons(lefts, rights);
    }

    /** Initializes the buttons - called from the constructor that takes the buttons
    * and from the initButtons(String, String).
    * @param left an array of buttons to be aligned to the left (bottom) or null if no
    *             buttons should be placed to the left (bottom)
    * @param right an array of buttons to be aligned to the right (top) or null if no
    *             buttons should be placed to the right (top)
    */
    private void initButtons(ButtonBarButton[] left, ButtonBarButton[] right) {

        //    setFont(DEFAULT_FONT);
        //    setForeground(DEFAULT_FOREGROUND);
        //    setBackground(DEFAULT_BACKGROUND);

        setBorder(new EmptyBorder(6, 7, 6, 7));
        setLayout(new BorderLayout());

        JPanel inside = new JPanel();
        inside.setLayout(new ButtonBarLayout());

        if ((left == null) || (left.length == 0))
            leftButtons = new ButtonBarButton[0];
        else {
            leftButtons = new ButtonBarButton[left.length];
            for (int i=0; i<left.length; i++) {
                leftButtons[i] = left[i];
                leftButtons[i].attachButton(this);
                inside.add(leftButtons[i]);
            }
        }

        if ((right == null) || (right.length == 0))
            rightButtons = new ButtonBarButton[0];
        else {
            rightButtons = new ButtonBarButton[right.length];
            for (int i=0; i<right.length; i++) {
                rightButtons[i] = right[i];
                rightButtons[i].attachButton(this);
                inside.add(rightButtons[i]);
            }
        }

        add(inside, "Center"); // NOI18N
    }

    /** Sets the buttons of the ButtonBar.
    * @param left The buttons to be placed on the left
    * @param right The buttons to be placed on the right
    */
    public void setButtons(ButtonBarButton[] left, ButtonBarButton[] right) {
        setButtons(left, right, HORIZONTAL);
    }

    /** Sets the buttons of the ButtonBar.
    * @param left The buttons to be placed on the left
    * @param right The buttons to be placed on the right
    * @param orientation buttons orientation (HORIZONTAL, VERTICAL)
    */
    public void setButtons(ButtonBarButton[] left, ButtonBarButton[] right,
                           int orientation) {

        this.orientation = orientation;
        initButtons(left, right);
        invalidate();
        repaint();
    }

    /** Sets the buttons of the ButtonBar.
    * @param left The String captions of the buttons to be placed on the left
    * @param right The String captions of the  buttons to be placed on the right
    */
    public void setButtons(String[] left, String[] right) {
        setButtons(left, right, HORIZONTAL);
    }

    /** Sets the buttons of the ButtonBar.
    * @param left The String captions of the buttons to be placed on the left
    * @param right The String captions of the  buttons to be placed on the right
    * @param orientation buttons orientation (HORIZONTAL, VERTICAL)
    */
    public void setButtons(String[] left, String[] right, int orientation) {
        this.orientation = orientation;
        initButtons(left, right, ""); // NOI18N
        invalidate();
        repaint();
    }

    /** Adds the specified ButtonBar listener to receive ButtonBar events from
    * this ButtonBar. ButtonBar events occur when a user clicks on one
    * of the buttons on the ButtonBar.
    * @param l the ButtonBar listener.
    * @see #removeActionListener
    */
    public synchronized void addButtonBarListener(ButtonBarListener l) {
        listeners.addElement(l);
    }

    /** Removes the specified ButtonBar listener to receive ButtonBar events from
    * this ButtonBar. ButtonBar events occur when a user clicks on one
    * of the buttons on the ButtonBar.
    * @param l the ButtonBar listener.
    * @see #removeActionListener
    */
    public synchronized void removeButtonBarListener(ButtonBarListener l) {
        listeners.removeElement(l);
    }

    /** paints the button bar */
    public void paint(Graphics g) {
        Dimension d = getSize();
        g.setColor(getBackground());
        g.fillRect(0, 0, d.width, d.height);
        super.paint(g); // because of the lightweight components
    }

    /** Returns a specified button's index. It is the index of the button from
    * left to right. I.e. the leftmost button has index 0, and the rightmost
    * has index <number of left buttons> + <number of right buttons> - 1
    * @return a specified button's index
    */
    public int getButtonIndex(ButtonBarButton button) {
        if (button == null)
            return -1;

        for (int i=0; i<leftButtons.length; i++)
            if (leftButtons[i].equals(button)) return i;
        for (int i=0; i<rightButtons.length; i++)
            if (rightButtons[i].equals(button)) return leftButtons.length + i;

        return -1;
    }

    /** Get the button by its index */
    public ButtonBarButton getButton(int index) {
        if (index < 0 || index >= leftButtons.length + rightButtons.length)
            return null;
        return (index >= leftButtons.length) ?
               rightButtons[index - leftButtons.length] : leftButtons[index];
    }

    /** Sets the default button by index */
    public void setDefaultButton(int index) {
        setDefaultButton(getButton(index));
    }

    /** Sets the default button in the ButtonBar. When 'Enter' key is pressed this
    * button behaves the same way as it was pressed by mouse.
    */
    public void setDefaultButton(final ButtonBarButton button) {
        // if the peer doesn't exist just remember the default button
        if (!peerExist) {
            defaultButton = button;
            return;
        }

        if (defaultButton != button) {
            JButton oldDefault = defaultButton;
            defaultButton = button;

            Component c = getParent();
            while(c != null) {
                if (c instanceof JRootPane) {
                    /* now set the default button to root pane. However it
                    * will not work properly in text fields, so the next part
                    * patches the behavior.
                    */
                    ((JRootPane)c).setDefaultButton(button);
                    break;
                }
                c = c.getParent();
            }

            /* Because of the default button behavior we need to reregister
            * action for VK_ENTER again using registerKeyboardAction() in root
            * pane. If the root pane is not available we will register it for
            * button bar directly. However in this case the button will not have
            * the special border.
            */
            if (c == null) { // if no root pane found
                c = this; // register action for button bar
            }

            if (oldDefault != null) {
                ((JComponent)c).unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true));
                ((JComponent)c).unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false));
            }
            if (button != null) {
                // common array for the two following registerKeyboardAction() calls
                // acceptKeys[0] == true // start accepting keys
                final boolean acceptKeys[] = new boolean[1];
                acceptKeys[0] = true;

                /* [PENDING] Patch for situation when the dialog with button bar and default button
                * gets opened and the Enter key stays pressed. Since the registerKeyboardAction catches
                * only release of Enter key, holding the Enter key would in this case cause
                * default button action on Enter key release.
                * /
                Timer tm = new Timer(1000,
                  new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                      acceptKeys[0] = true;
                    }
                  }
                );
                tm.setRepeats(false);
                tm.start();
                 */

                ((JComponent)c).registerKeyboardAction(
                    new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            if (button.isEnabled() && acceptKeys[0]) {
                                buttonPressed(button, 0);
                            }
                        }
                    },
                    KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true),
                    WHEN_IN_FOCUSED_WINDOW
                );
            }
            repaint();
        }
    }

    /** This method notifies the actionListeners about that the give button has been pressed */
    protected void buttonPressed(ButtonBarButton button, int modifiers) {
        ButtonBarEvent evt = new ButtonBarEvent(this, button,
                                                ActionEvent.ACTION_PERFORMED, button.getText(), modifiers);
        Vector listeners2;
        synchronized (this) {
            listeners2 = (Vector)listeners.clone();
        }
        int length = listeners2.size ();
        for (int i = 0; i < length; i++)
            ((ButtonBarListener)(listeners2.elementAt(i))).buttonPressed(evt);
    }

    public void addNotify() {
        super.addNotify();
        peerExist = true;
        // now really set the default button
        if (defaultButton != null) {
            ButtonBarButton b = defaultButton;
            defaultButton = null; // set function tests equality
            setDefaultButton(b);
        }
    }

    public void removeNotify() {
        // disable default button
        if (defaultButton != null) {
            ButtonBarButton oldDefault = defaultButton;
            setDefaultButton(null);
            defaultButton = oldDefault;
        }
        super.removeNotify();
        peerExist = false;
    }

    public final int getOrientation() {
        return orientation;
    }

    /** The ButtonBarLayout class is a layout that lays the ButtonBarButtons according to their preferred size.
    * The buttons in the left group are placed according to their preferred width from the left edge from first to
    * the last and the buttons in the right group are placed according to their preferred size from the right edge
    * from the last to the first.
    */
    class ButtonBarLayout implements LayoutManager {
        public void addLayoutComponent (String name, Component comp) {
        }

        public void removeLayoutComponent (Component comp) {
        }

        public Dimension preferredLayoutSize (Container parent) {
            Dimension d = new Dimension(0, 0);
            if ((leftButtons.length == 0) && (rightButtons.length == 0))
                return d;
            else {
                Dimension maxButton = countMaxSize(leftButtons, rightButtons);
                if (orientation == HORIZONTAL) {
                    d.height = maxButton.height;
                    d.width += maxButton.width * (leftButtons.length + rightButtons.length);

                    if ((leftButtons.length > 0) && (rightButtons.length > 0)) {
                        d.width += SECTIONS_GAP;
                    }

                    if (leftButtons.length > 1)
                        d.width += BUTTONS_GAP * (leftButtons.length - 1);
                    if (rightButtons.length > 1)
                        d.width += BUTTONS_GAP * (rightButtons.length - 1);
                } else { // vertical orientation
                    d.height = maxButton.height * (leftButtons.length + rightButtons.length);
                    d.width += maxButton.width;

                    if ((leftButtons.length > 0) && (rightButtons.length > 0)) {
                        d.height += SECTIONS_GAP;
                    }

                    if (rightButtons.length > 1)
                        d.height += BUTTONS_GAP * (rightButtons.length - 1);
                    if (leftButtons.length > 1)
                        d.height += BUTTONS_GAP * (leftButtons.length - 1);
                }

                return d;
            }
        }

        public Dimension minimumLayoutSize (Container parent) {
            return preferredLayoutSize(parent);
        }

        public void layoutContainer (Container parent) {
            if ((leftButtons.length == 0) && (rightButtons.length == 0))
                return;

            final Dimension size = parent.getSize();
            final Dimension maxButton = countMaxSize(leftButtons, rightButtons);
            int buttonsCount = leftButtons.length + rightButtons.length;

            if (orientation == HORIZONTAL) {
                if ((maxButton.width * buttonsCount) + (BUTTONS_GAP * (buttonsCount - 1)) > size.width)
                    maxButton.width = (size.width - (BUTTONS_GAP * (buttonsCount - 1))) / buttonsCount;
            } else {
                if ((maxButton.height * buttonsCount) + (BUTTONS_GAP * (buttonsCount - 1)) > size.height)
                    maxButton.height = (size.height - (BUTTONS_GAP * (buttonsCount - 1))) / buttonsCount;
            }

            final int butDelta = BUTTONS_GAP
                                 + ((orientation == HORIZONTAL) ? maxButton.width : maxButton.height);

            if ((leftButtons.length > 0) && (rightButtons.length > 0)) { // both are not empty
                if (orientation == HORIZONTAL) {
                    int x = 0;
                    for (int i = 0; i < leftButtons.length; i++) {
                        leftButtons[i].setBounds(new Rectangle(x, 0, maxButton.width, maxButton.height));
                        leftButtons[i].setSize(maxButton);
                        x += butDelta;
                    }

                    x = size.width - maxButton.width;
                    for (int i = rightButtons.length - 1; i >= 0; i--) {
                        rightButtons[i].setBounds(new Rectangle(x, 0, maxButton.width, maxButton.height));
                        rightButtons[i].setSize(maxButton);
                        x -= butDelta;
                    }
                } else {
                    int y = 0;
                    for (int i = 0; i < rightButtons.length; i++) {
                        rightButtons[i].setBounds(new Rectangle(0, y, maxButton.width, maxButton.height));
                        rightButtons[i].setSize(maxButton);
                        y += butDelta;
                    }

                    y = size.height - maxButton.height;
                    for (int i = leftButtons.length - 1; i >= 0; i--) {
                        leftButtons[i].setBounds(new Rectangle(0, y, maxButton.width, maxButton.height));
                        leftButtons[i].setSize(maxButton);
                        y -= butDelta;
                    }
                }
            } else {
                ButtonBarButton[] buts = (leftButtons.length > 0) ? leftButtons : rightButtons;

                if (singleMode == SINGLE_CENTER) { // single centered
                    if (orientation == HORIZONTAL) { // horizontal orientation
                        int x = (size.width - (butDelta * buts.length - BUTTONS_GAP)) / 2;
                        for (int i = 0; i < buts.length; i++) {
                            buts[i].setBounds(new Rectangle(x, 0, maxButton.width, maxButton.height));
                            x += butDelta;
                        }
                    } else { // vertical orientation
                        int y = (size.height - (butDelta * buts.length - BUTTONS_GAP)) / 2;
                        for (int i = 0; i < buts.length; i++) {
                            buts[i].setBounds(new Rectangle(0, y, maxButton.width, maxButton.height));
                            y += butDelta;
                        }
                    }
                } else if (singleMode == SINGLE_LEFT) { // single left
                    if (orientation == HORIZONTAL) { // horizontal orientation
                        int x = 0;
                        for (int i = 0; i < buts.length; i++) {
                            buts[i].setBounds(new Rectangle(x, 0, maxButton.width, maxButton.height));
                            buts[i].setSize(maxButton);
                            x += butDelta;
                        }
                    } else { // vertical orientation
                        int y = size.height - maxButton.height;
                        for (int i = buts.length - 1; i >= 0; i--) {
                            buts[i].setBounds(new Rectangle(0, y, maxButton.width, maxButton.height));
                            buts[i].setSize(maxButton);
                            y -= butDelta;
                        }
                    }
                } else { // SINGLE_RIGHT
                    if (orientation == HORIZONTAL) { // horizontal orientation
                        int x = size.width - maxButton.width;
                        for (int i = buts.length - 1; i >= 0; i--) {
                            buts[i].setBounds(new Rectangle(x, 0, maxButton.width, maxButton.height));
                            buts[i].setSize(maxButton);
                            x -= butDelta;
                        }
                    } else { // vertical orientation
                        int y = 0;
                        for (int i = 0; i < buts.length; i++) {
                            buts[i].setBounds(new Rectangle(0, y, maxButton.width, maxButton.height));
                            buts[i].setSize(maxButton);
                            y += butDelta;
                        }
                    }
                }
            }
        }

        private Dimension countMaxSize(ButtonBarButton[] left, ButtonBarButton[] right) {
            Dimension d1 = countMaxSize(left);
            Dimension d2 = countMaxSize(right);
            return new Dimension(Math.max(d1.width, d2.width), Math.max(d1.height, d2.height));
        }

        private Dimension countMaxSize(ButtonBarButton[] buttons) {
            Dimension d = new Dimension(0, 0);
            for (int i = 0; i < buttons.length; i++) {
                Dimension d2 = buttons[i].getPreferredSize();
                d.width = Math.max(d.width, d2.width);
                d.height = Math.max(d.height, d2.height);
            }
            return d;
        }
    }

    public static interface ButtonBarListener {
        public void buttonPressed(ButtonBarEvent evt);
    }

    /** The ButtonBar event */
    public static class ButtonBarEvent extends ActionEvent {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 5361326502523278662L;

        /** The button on which the event originated */
        private ButtonBarButton sourceButton;

        /** Constructs a ButtonBarEvent object with the specified source object
        * (the ButtonBar) and sourceButton (ButtonBarButton).
        * @param source the object where the event originated
        * @param id the type of event
        * @param command the command string for this action event
         * @param modifiers the modifiers held down during this action
        */
        public ButtonBarEvent(ButtonBar source, ButtonBarButton button, int id,
                              String command, int modifiers) {
            super(source, id, command, modifiers);
            sourceButton = button;
        }

        /** @return The button on which the event originated */
        public ButtonBarButton getButton() {
            return sourceButton;
        }
    }

    /** the buttons */
    private ButtonBarButton[] rightButtons, leftButtons;

    /** the mode for single-size button alignment */
    private int singleMode = SINGLE_RIGHT;

    /** ButtonBar listeners 
     * @associates ButtonBarListener*/
    private Vector listeners = new Vector();
}

/*
 * Log
 *  5    src-jtulach1.4         1/12/00  Ales Novak      i18n
 *  4    src-jtulach1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    src-jtulach1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    src-jtulach1.1         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  1    src-jtulach1.0         3/9/99   Jaroslav Tulach 
 * $
 * Beta Change History:
 *  0    Tuborg    0.15        --/--/98 Jan Formanek    ButtonBarButton class moved out into separate class file
 *  0    Tuborg    0.15        --/--/98 Jan Formanek    Buttons identified by indexes
 *  0    Tuborg    0.15        --/--/98 Jan Formanek    Localization
 *  0    Tuborg    0.16        --/--/98 Jan Formanek    getPressedButton added
 *  0    Tuborg    0.21        --/--/98 Jan Formanek    empty button bar preset, default constuctor creates an
 *  0    Tuborg    0.21        --/--/98 Jan Formanek    empty button bar
 *  0    Tuborg    0.30        --/--/98 Jan Formanek    huge changes
 *  0    Tuborg    0.32        --/--/98 Jan Formanek    added setButtons(String[], String[])
 *  0    Tuborg    0.33        --/--/98 Petr Hamernik   default button in the predefined sets added.
 *  0    Tuborg    0.34        --/--/98 Petr Hamernik   layout rewritten
 *  0    Tuborg    0.35        --/--/98 Jan Formanek    added single-side alignment modes
 */
