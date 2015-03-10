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
import java.util.ResourceBundle;

import javax.swing.*;

import org.openide.util.NbBundle;

/** The ButtonBarButton represents a single button on a ButtonBar.
* @author   Ian Formanek, Petr Hamernik
* @version  0.23, May 29, 1998
*/
public class ButtonBarButton extends JButton {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -4251349097612749034L;
    /** Bundle - i18n */
    static ResourceBundle bundle;

    /** Default color of the text of the button when the user moves a mouse over it */
    public Color DEFAULT_HIGHLIGHTED = new Color(128, 128, 255);
    /** Default color of the text of the button when it is disabled */
    public Color DEFAULT_DISABLED = Color.lightGray;

    /** Constructs a new ButtonBarButton with label "Button". */ // NOI18N
    public ButtonBarButton() {
        this(getString("CTL_Button"));
    }

    /** Constructs a new ButtonBarButton with specified label.
    * @param aLabel the label for the button
    */
    public ButtonBarButton(String aLabel) {
        super (aLabel);
        label = aLabel;
        //    enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        //    enableEvents(AWTEvent.KEY_EVENT_MASK);
    }

    /** Called by a ButtonBar to notify this ButtonBarButton that
    * it has been added to the ButtonBar.
    * @param bat The ButtonBar, this Button has been added to
    */
    void attachButton(ButtonBar bar) {
        buttonBar = bar;
        addActionListener (new ActionListener () {
                               public void actionPerformed (ActionEvent e) {
                                   buttonBar.buttonPressed(ButtonBarButton.this, e.getModifiers());
                               }
                           }
                          );
    }

    /** Getter method for the Label property.
    * @return Current Label value
    */
    public String getText() {
        return label;
    }

    /** Setter method for the Label property.
    * @param value New Label value
    */
    public void setText(String value) {
        label = value;
        super.setText (value);
        //    repaint();
    }

    /** Getter method for the HighlightedColor property.
    * @return Current HighlightedColor value
    */
    public Color getHighlightedColor() {
        return highlightedColor;
    }

    /** Setter method for the HighlightedColor property.
    * @param value New HighlightedColor value
    */
    public void setHighlightedColor(Color value) {
        highlightedColor = value;
        if (highlighted)
            repaint();
    }

    /** Getter method for the DisabledColor property.
    * @return Current DisabledColor value
    */
    public Color getDisabledColor() {
        return disabledColor;
    }

    /** Setter method for the DisabledColor property.
    * @param value New DisabledColor value
    */
    public void setDisabledColor(Color value) {
        disabledColor = value;
        if (!enabled)
            repaint();
    }

    /** Sets this button to be default. It's included only
    * for compatibility. The <CODE>setDefaultButton</CODE> should
    * be used instead.
    */
    public void setDefault(boolean b) {
        buttonBar.setDefaultButton(b ? this : null);
    }

    /** i18n */
    static String getString(String x) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(ButtonBarButton.class);
        }
        return bundle.getString(x);
    }

    /** Paints the button */
    /*  public void paint(Graphics g) {
        int descent = getToolkit().getFontMetrics(getFont()).getDescent();
        Dimension d = getSize();
        if (!enabled)
          g.setColor(getDisabledColor());
        else if (highlighted)
          g.setColor(getHighlightedColor());
        else if (def)
          g.setColor(getDefaultColor());
        else
          g.setColor(getForeground());
        g.setFont(getFont());
        g.drawString(label, 0, d.height - descent - 1);
      } */

    /** Returns a width of the button
    * @return a width of the button
    */
    /*
    public int getWidth() {
      return getPreferredSize ().width; // getToolkit().getFontMetrics(getFont()).stringWidth(label);
}     */

    /** Returns a height of the button
    * @return a height of the button
    */  /*
    public int getHeight() {
      return getPreferredSize ().height; //     return getToolkit().getFontMetrics(getFont()).getHeight();
} */

    /** Returns a preferred size of the button
    * @return a preferred size of the button
    */
    /*  public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getHeight());
      } */

    /*  public void processMouseEvent(MouseEvent e) {
        switch (e.getID()) {
          case MouseEvent.MOUSE_PRESSED:
            if (highlighted) {
              pressed = true;
              if (enabled) repaint();
            }
            break;
          case MouseEvent.MOUSE_RELEASED:
            if (pressed) {
              if (enabled && (buttonBar != null))
                buttonBar.buttonPressed(this, e.getModifiers());
              pressed = false;
              repaint();
            }
            break;
          case MouseEvent.MOUSE_ENTERED:
            highlighted = true;
            if (enabled) repaint();
            break;
          case MouseEvent.MOUSE_EXITED:
            if (highlighted) {
              highlighted = false;
              repaint();
            }
            if (pressed) {
              pressed = false;
              repaint();
            }
            break;
        }
        super.processMouseEvent(e);
      } */

    /** Returns a string representation of the button
    * @return a string representation of the button
    */
    //  public String toString() {
    //    return "ButtonBarButton ["+label+"]"; // NOI18N
    //  }

    /** default color of highlighted button's text */
    private Color highlightedColor = DEFAULT_HIGHLIGHTED;
    /** default color of disabled button's text */
    private Color disabledColor = DEFAULT_DISABLED;

    /** a Label of the button */
    private String label;

    private boolean enabled = true;
    private boolean pressed = false;
    private boolean highlighted = false;

    private ButtonBar buttonBar;
}

/*
 * Log
 *  4    src-jtulach1.3         1/13/00  Jaroslav Tulach I18N
 *  3    src-jtulach1.2         1/12/00  Ales Novak      i18n
 *  2    src-jtulach1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    src-jtulach1.0         3/9/99   Jaroslav Tulach 
 * $
 * Beta Change History:
 *  0    Tuborg    0.21        --/--/98 Petr Hamernik   default state added
 *  0    Tuborg    0.23        --/--/98 Petr Hamernik   getHeight, getWidth commented
 */
