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

package org.openide.explorer.propertysheet;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyEditor;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.FocusManager;


/**
* A lightweight component creating a labelled button. 
* Can be "plastic", which means that when a mouse
* enters the button, the area of the button is stubbed. <code>SheetButton</code> can contain one inner component.
*
* @author   Jan Jancura
* @version  0.18, May 6, 1997
*/
final class SheetButton extends JPanel {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -5681433767155127558L;


    // variables .................................................................

    /** There are all listeners stored. 
     * @associates SheetButtonListener*/
    private transient Vector        listeners = new Vector (1,5);
    /** There are action command string stored. */
    private String                  actionCommand = "click"; // NOI18N
    /** Label of this button. */
    private String                  label;
    /** Preferred size of this button. */
    private Dimension               preferredSize = null;
    /** State of button. */
    private boolean                 pressed = false;
    /** State of focus. */
    private boolean                 hasFocus = false;
    /** True if focus is received from mouse. */
    private boolean                 hasFocusFromMouse = false;
    /** True if button can receive focus. */
    private boolean                 focusTransferable = false;
    /** True is is in "plastic" mode. */
    private boolean                 isPlastic = false;
    /** Listens on focus/key/mouse events. */
    private InnerListener           innerListener;
    /** SheetButton can contains one inner component. */
    private JComponent              innerComponent = null;
    /** Is true if mouseEntered and mouseExited actions should be propagated. */
    private boolean                 plasticNotify = false;
    /** Foreground color. */
    private Color                   foreground = SystemColor.controlText;
    /** Inactive foreground color. */
    private Color                   inForeground = SystemColor.textInactiveText;


    // init .................................................................

    /**
    * Construct a button with empty label.
    */
    public SheetButton () {
        this ("", false, false); // NOI18N
    }

    /**
    * Construct a button with label and standard appearance (not plastic).
    *
    * @param aLabel the label
    */
    public SheetButton (String aLabel) {
        this (aLabel, false, false);
    }

    /**
    * Construct a button with label.
    *
    * @param aLabel the label
    * @param boolean <code>true</code> if should use plastic appearance
    */
    public SheetButton (String aLabel, boolean isPlasticButton) {
        this (aLabel, isPlasticButton, false);
    }

    /**
    * Construct a button with label and possibly a plastic listener.
    * @param label the label
    * @param isPlasticButton <code>true</code> if should be plastic
    * @param plasticActionNotify <code>true</code> if mouse enter/exit events should be propagated
    *
    */
    public SheetButton (String aLabel, boolean isPlasticButton, boolean plasticActionNotify) {
        setDoubleBuffered (false);
        setOpaque (true);
        label = aLabel == null ? "" : aLabel; // NOI18N
        isPlastic = isPlasticButton;
        plasticNotify = plasticActionNotify;
        innerListener = new InnerListener ();
        addMouseListener (innerListener);
        addKeyListener (innerListener);
    }


    // variables .................................................................

    /**
    * Return whether focus should be traversable.
    */
    public boolean isFocusTraversable () {
        return focusTransferable;
    }

    /**
    * Set whether focus should be traversable.
    * @param ft <code>true</code> if so
    * @see #isFocusTraversable
    */
    public void setFocusTransferable (boolean ft) {
        if (isFocusTraversable () == ft) return;
        focusTransferable = ft;
        if (ft)
            addFocusListener (innerListener);
        else
            removeFocusListener (innerListener);
    }

    /**
    * Sets tooltip test.
    */
    public void setToolTipText (String str) {
        if (innerComponent != null) innerComponent.setToolTipText (str);
        super.setToolTipText (str);
    }

    /*
    * @return Standart method returned preferredSize (depends on font size only).
    */
    public Dimension getPreferredSize () {
        if (preferredSize == null) updatePreferredSize ();
        return preferredSize == null ? new Dimension (10,10) : preferredSize;
    }

    /*
    * Sets the font of this component.
    * @param aFont The font to become this component's font.
    */
    public void setFont (Font aFont) {
        super.setFont (aFont);
        updatePreferredSize ();
    }

    /**
    * Get the active foreground color.
    * @return the color
    */
    public Color getActiveForeground () {
        return foreground;
    }

    /**
    * Set the active foreground color.
    * @param color the color
    */
    public void setActiveForeground (Color color) {
        foreground = color;
    }

    /**
    * Get the inactive foreground color.
    * @return the color
    */
    public Color getInactiveForeground () {
        return inForeground;
    }

    /**
    * Set the inactive foreground color.
    * @param color the color
    */
    public void setInactiveForeground (Color color) {
        inForeground = color;
    }

    /*
    * Gets the label of this button.
    * @return the button's label.
    */
    public String getLabel () {
        return label;
    }

    /*
    * Sets the button's label to be the specified string.
    * @param label   the new label.
    */
    public void setLabel (String aLabel) {
        label = aLabel;
        updatePreferredSize ();
        repaint ();
    }

    /**
    * Set whether button should appear stubbed.
    * @param aPressed <code>true</code> if so
    */
    public void setPressed (boolean aPressed) {
        if (pressed == aPressed) return;
        pressed = aPressed;
        if (pressed)
            setBorder (new EmptyBorder (2, 2, 0, 0));
        else
            setBorder (new EmptyBorder (1, 1, 1, 1));
        repaint ();
    }

    /**
    * Test whether button is pressed.
    * @return <code>true</code> if so
    */
    public boolean isPressed () {
        return pressed;
    }

    /**
    * Set whether button is plastic.
    * @param plastic <code>true</code> if so
    */
    public void setPlastic (boolean plastic) {
        this.isPlastic = plastic;
        repaint ();
    }

    /**
    * Test whether button is plastic.
    * @return <code>true</code> if so
    */
    public boolean isPlastic () {
        return isPlastic;
    }

    /**
    * Set the action command.
    * @param command the new command, e.g. <code>"click"</code>
    */
    public void setActionCommand (String command) {
        actionCommand = command;
    }

    /**
    * Attaches component for this button.
    */
    public Component add (Component innerComponent) {
        setLayout (new BorderLayout ());
        setBorder (new EmptyBorder (1, 1, 1, 1));
        add ("Center", innerComponent); // NOI18N
        removeMouseListener (innerListener);
        innerComponent.addMouseListener (innerListener);
        return this.innerComponent = (JComponent) innerComponent;
    }

    /**
    * Recalculates preferred size.
    */
    private void updatePreferredSize () {
        Graphics g = getGraphics ();
        if (g == null) return;
        Font font = null;
        if ((font = getFont ()) == null) return;
        FontMetrics fontMetrics = g.getFontMetrics (font);
        preferredSize = new Dimension (
                            fontMetrics.stringWidth (label) + 10,
                            fontMetrics.getHeight () + 6
                        );
    }

    /**
    * Standart methods painting SheetButton.
    */
    public void paint (Graphics g) {
        super.paint (g);

        Dimension size = getSize ();
        Color color = g.getColor ();
        Font theFont = g.getFont ();

        g.setFont (getFont ());
        FontMetrics fontMetrics = g.getFontMetrics ();
        if (pressed || hasFocus) {
            if (innerComponent == null) {
                g.setColor (isEnabled () ? getActiveForeground () : getInactiveForeground ());
                g.drawString (
                    label,
                    6,
                    (size.height - fontMetrics.getHeight ()) / 2 + 1 + fontMetrics.getMaxAscent ()
                );
            }
        } else {
            g.setColor (SystemColor.controlLtHighlight);
            g.drawLine (0, 0, size.width - 1, 0);
            g.drawLine (0, 0, 0, size.height - 1);

            g.setColor (SystemColor.controlDkShadow);
            g.drawLine (size.width - 1, 0, size.width - 1, size.height - 1);
            g.drawLine (0, size.height - 1, size.width - 1, size.height - 1);

            if (innerComponent == null) {
                g.setColor (isEnabled () ? getActiveForeground () : getInactiveForeground ());
                g.drawString (
                    label,
                    5,
                    (size.height - fontMetrics.getHeight ()) / 2 + fontMetrics.getMaxAscent ()
                );
            }
        }
        g.setFont (theFont);
        g.setColor (color);
    }

    /*
    * Returns string representation of this class.
    * @return <CODE>String</CODE> Representation of this class.
    */
    public String toString () {
        return getClass ().getName () + "[ \"" + label + "\" ]"; // NOI18N
    }


    // SheetButtonListener support ......................................................

    /**
    * Adds Listener.
    */
    void addSheetButtonListener (SheetButtonListener sheetButtonListener) {
        listeners.addElement (sheetButtonListener);
    }

    /**
    * Removes Listener.
    */
    void removeSheetButtonListener (SheetButtonListener sheetButtonListener) {
        listeners.removeElement (sheetButtonListener);
    }

    public void notifySheetButtonListenersAboutClick (ActionEvent e) {
        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            ((SheetButtonListener) l.elementAt (i)).sheetButtonClicked (e);
    }

    public void notifySheetButtonListenersAboutEntered (ActionEvent e) {
        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            ((SheetButtonListener) l.elementAt (i)).sheetButtonEntered (e);
    }

    public void notifySheetButtonListenersAboutExited (ActionEvent e) {
        Vector l = (Vector) listeners.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            ((SheetButtonListener) l.elementAt (i)).sheetButtonExited (e);
    }


    // innerclasses ..........................................................................

    private class InnerListener extends MouseMotionAdapter implements
        MouseListener, KeyListener, FocusListener {

        /*
        * Standart helper method.
        */
        public void mouseClicked (MouseEvent e) {
        }

        /*
        * Standart helper method.
        */
        public void mousePressed (MouseEvent e) {
            if (isFocusTraversable ()) {
                hasFocusFromMouse = true;
                requestFocus ();
            }
            if (!isPlastic) {
                pressed = true;
                setBorder (new EmptyBorder (2, 2, 0, 0));
                notifySheetButtonListenersAboutEntered (
                    new ActionEvent (
                        SheetButton.this,
                        ActionEvent.ACTION_FIRST,
                        actionCommand
                    )
                );
                if (innerComponent != null) {
                    //          setBorder (new EmptyBorder (2, 2, 0, 0));
                    invalidate ();
                    getParent ().validate ();
                    innerComponent.addMouseMotionListener (innerListener);
                } else
                    addMouseMotionListener (innerListener);
                repaint ();
            }
        }

        /*
        * Standart helper method.
        */
        public void mouseReleased (MouseEvent e) {
            if (!isPlastic) {
                if (innerComponent != null) {
                    innerComponent.removeMouseMotionListener (innerListener);
                    //          setBorder (new EmptyBorder (1, 1, 1, 1));
                    invalidate ();
                    getParent ().validate ();
                } else
                    removeMouseMotionListener (innerListener);
                repaint ();
                if (isEnabled () && pressed) {
                    notifySheetButtonListenersAboutClick (
                        new ActionEvent (
                            SheetButton.this,
                            ((e.getClickCount () % 2) == 1) ?
                            (ActionEvent.ACTION_FIRST + 1) :
                            ActionEvent.ACTION_FIRST,
                            actionCommand
                        )
                    );
                    notifySheetButtonListenersAboutExited (
                        new ActionEvent (
                            SheetButton.this,
                            ActionEvent.ACTION_FIRST,
                            actionCommand
                        )
                    );
                }
                pressed = false;
                setBorder (new EmptyBorder (1, 1, 1, 1));
            } else {
                if (isEnabled ()) notifySheetButtonListenersAboutClick (
                        new ActionEvent (
                            SheetButton.this,
                            ActionEvent.ACTION_FIRST,
                            actionCommand
                        )
                    );
            }
        }

        /*
        * Standart helper method.
        */
        public void mouseEntered (MouseEvent e) {
            if (isPlastic) {
                pressed = true;
                setBorder (new EmptyBorder (2, 2, 0, 0));
                repaint ();
                if (plasticNotify)
                    notifySheetButtonListenersAboutEntered (
                        new ActionEvent (
                            SheetButton.this,
                            ActionEvent.ACTION_FIRST,
                            actionCommand
                        )
                    );
            }
        }

        /*
        * Standart helper method.
        */
        public void mouseExited (MouseEvent e) {
            if (isPlastic) {
                pressed = false;
                setBorder (new EmptyBorder (1, 1, 1, 1));
                repaint ();
                if (plasticNotify)
                    notifySheetButtonListenersAboutExited (
                        new ActionEvent (
                            SheetButton.this,
                            ActionEvent.ACTION_FIRST,
                            actionCommand
                        )
                    );
            }
        }

        public void mouseDragged (MouseEvent e) {
            if (new Rectangle (
                        SheetButton.this.getSize ()
                    ).contains (e.getPoint ())
                    ==
                    pressed
               ) return;
            if (pressed)
                notifySheetButtonListenersAboutExited (
                    new ActionEvent (
                        SheetButton.this,
                        ActionEvent.ACTION_FIRST,
                        actionCommand
                    ));
            else
                notifySheetButtonListenersAboutEntered (
                    new ActionEvent (
                        SheetButton.this,
                        ActionEvent.ACTION_FIRST,
                        actionCommand
                    ));
            pressed = !pressed;
            if (pressed)
                setBorder (new EmptyBorder (2, 2, 0, 0));
            else
                setBorder (new EmptyBorder (1, 1, 1, 1));
            if (innerComponent != null) {
                invalidate ();
                getParent ().validate ();
            }
            repaint ();
        }

        public void keyTyped (KeyEvent e) {
            if ((e.getKeyChar () == ' ') || (e.getKeyChar () == '\n'))
                notifySheetButtonListenersAboutClick (
                    new ActionEvent (
                        SheetButton.this,
                        ActionEvent.ACTION_FIRST + 1,
                        actionCommand
                    )
                );
        }

        public void keyPressed (KeyEvent e) {
            if (e.getKeyCode () == KeyEvent.VK_DOWN) {
                FocusManager fm = FocusManager.getCurrentManager ();
                fm.focusNextComponent (SheetButton.this);
            }
            else
                if (e.getKeyCode () == KeyEvent.VK_UP) {
                    FocusManager fm = FocusManager.getCurrentManager ();
                    fm.focusPreviousComponent (SheetButton.this);
                }
        }

        public void keyReleased (KeyEvent e) {
        }

        public void focusGained (FocusEvent fe) {
            if (hasFocusFromMouse) hasFocusFromMouse = false;
            else hasFocus = true;
            notifySheetButtonListenersAboutEntered (
                new ActionEvent (
                    SheetButton.this,
                    ActionEvent.ACTION_FIRST + 1,
                    actionCommand
                )
            );
            repaint ();
        }

        public void focusLost (FocusEvent fe) {
            hasFocus = false;
            notifySheetButtonListenersAboutExited (
                new ActionEvent (
                    SheetButton.this,
                    ActionEvent.ACTION_FIRST + 1,
                    actionCommand
                )
            );
            repaint ();
        }
    }
}

/*
 * Log
 *  8    Gandalf   1.7         1/19/00  Jan Jancura     Cycling while repainting
 *       solved.
 *  7    Gandalf   1.6         1/17/00  Jan Jancura     Button Updated
 *  6    Gandalf   1.5         1/12/00  Ian Formanek    NOI18N
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/15/99  Jaroslav Tulach More private things & 
 *       support for default property.
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         3/20/99  Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
