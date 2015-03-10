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

package org.netbeans.editor;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.JLayeredPane;
import javax.swing.Timer;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;

/**
* Support for custom tooltips
*
* @author Miloslav Metelka
* @version 1.00
*/

public class ToolTipSupport extends MouseAdapter
            implements MouseMotionListener, ActionListener, PropertyChangeListener,
    SettingsChangeListener {

    private static final String NO_ROOT_PANE_LOCALE = "no_root_pane"; // NOI18N
    private static final String NO_ROOT_PANE_DEFAULT
    = "Editor component has no RootPane! Cannot handle tool tips."; // NOI18N

    private static final String UI_PREFIX = "ToolTip"; // NOI18N

    public static final int INITIAL_DELAY = 1000;

    public static final int DISMISS_DELAY = 5000;

    ExtUI extUI;

    JLabel toolTip;

    Timer enterTimer;

    Timer exitTimer;

    boolean enabled;

    protected int lastMouseX;

    protected int lastMouseY;

    private ComponentAdapter componentL;



    public ToolTipSupport(ExtUI extUI) {
        this.extUI = extUI;

        componentL = new ComponentAdapter() {
                         public void componentHidden(ComponentEvent evt) {
                             checkRemoveFromPane();
                         }
                     };

        enterTimer = new Timer(INITIAL_DELAY, this);
        enterTimer.setRepeats(false);
        exitTimer = new Timer(DISMISS_DELAY, this);
        exitTimer.setRepeats(false);

        enabled = true;

        Settings.addSettingsChangeListener(this);
        extUI.addPropertyChangeListener(this);
    }

    public final JLabel getToolTip() {
        return toolTip;
    }

    public void settingsChange(SettingsChangeEvent evt) {
        Class kitClass = Utilities.getKitClass(extUI.getComponent());
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if (ExtUI.COMPONENT_PROPERTY.equals(propName)) {
            if (evt.getNewValue() != null) { // just installed
                JTextComponent c = extUI.getComponent();

                c.addMouseListener(this);
                c.addMouseMotionListener(this);
                c.addComponentListener(componentL);

            } else { // just deinstalled
                JTextComponent c = (JTextComponent)evt.getOldValue();

                c.removeMouseListener(this);
                c.removeMouseMotionListener(this);
                c.removeComponentListener(componentL);
            }
        }
    }

    private void checkRemoveFromPane() {
        if (toolTip != null) {
            JRootPane rp = extUI.getComponent().getRootPane();
            if (rp == null) {
                return;
            }
        }
    }

    private void checkAddToPane() {
        if (toolTip != null) {
            JRootPane rp = extUI.getComponent().getRootPane();
            if (rp == null) {
                System.err.println(LocaleSupport.getString(
                                       NO_ROOT_PANE_LOCALE, NO_ROOT_PANE_DEFAULT));
                return;
            }
            // Possibly deinstall the old component from layered pane
            JRootPane ttrp = toolTip.getRootPane();
            if (ttrp != rp) {
                if (ttrp != null) {
                    ttrp.getLayeredPane().remove(toolTip);
                }
                rp.getLayeredPane().add(toolTip, JLayeredPane.POPUP_LAYER, 0);
            }
        }
    }

    public void setToolTip(JLabel tt) {
        if (toolTip == tt) {
            return;
        }

        checkRemoveFromPane();
        toolTip = tt;
    }

    protected JLabel createDefaultToolTip() {
        JLabel tt = new JLabel();

        Font font = UIManager.getFont(UI_PREFIX + ".font"); // NOI18N
        Color backColor = UIManager.getColor(UI_PREFIX + ".background"); // NOI18N
        Color foreColor = UIManager.getColor(UI_PREFIX + ".foreground"); // NOI18N

        if (font != null) {
            tt.setFont(font);
        }
        if (foreColor != null) {
            tt.setForeground(foreColor);
        }
        if (backColor != null) {
            tt.setBackground(backColor);
        }

        tt.setOpaque(true);
        tt.setBorder(BorderFactory.createCompoundBorder(
                         BorderFactory.createLineBorder(tt.getForeground()),
                         BorderFactory.createEmptyBorder(0, 3, 0, 3)
                     ));

        return tt;
    }

    protected void updateToolTip() {
        BaseKit kit = Utilities.getKit(extUI.getComponent());
        if (kit != null) {
            Action a = kit.getActionByName(BaseKit.buildToolTipAction);
            if (a != null) {
                a.actionPerformed(new ActionEvent(extUI.getComponent(), 0, "")); // NOI18N
            }
        }
    }

    protected void updateToolTipBounds() {
        Dimension prefSize = toolTip.getPreferredSize();
        Rectangle extBounds = extUI.getExtentBounds();
        int x = Math.min(lastMouseX - prefSize.width / 2,
                         extBounds.x + extBounds.width - prefSize.width);
        x = Math.max(x, extBounds.x);
        int charHeight = extUI.charHeight;
        int y = lastMouseY - 2 * charHeight;
        if (y - extBounds.y < charHeight) {
            y = lastMouseY + charHeight;
        }

        toolTip.setBounds(x - extBounds.x, y - extBounds.y, prefSize.width, prefSize.height);
    }

    protected void setToolTipVisible(boolean visible) {
        if (visible) {
            updateToolTip();
            if (toolTip != null && toolTip.getText() != null
                    && toolTip.getText().length() > 0
               ) {
                checkAddToPane();
                updateToolTipBounds();
                toolTip.setVisible(true);
            }
            exitTimer.start();

        } else { // hide tip

            enterTimer.stop();
            exitTimer.stop();
            if (toolTip != null && toolTip.isVisible()) {
                toolTip.setVisible(false);
            }

        }
    }

    public boolean isToolTipVisible() {
        return (toolTip != null && toolTip.isVisible());
    }

    public void setToolTipText(String text) {
        JLabel tt = getToolTip();
        if (tt == null) {
            tt = createDefaultToolTip();
            setToolTip(tt);
        }
        tt.setText(text);
        updateToolTipBounds();
    }

    public String getIdentifierUnderCursor() {
        String word = null;
        try {
            JTextComponent component = extUI.getComponent();
            BaseTextUI ui = (BaseTextUI)component.getUI();
            int pos = ui.viewToModel(component, new Point(lastMouseX, lastMouseY));
            if (pos >= 0) {
                BaseDocument doc = (BaseDocument)component.getDocument();
                int eolPos = Utilities.getRowEnd(doc, pos);
                Rectangle eolRect = ui.modelToView(component, eolPos);
                int charHeight = Utilities.getExtUI(component).charHeight;
                if (lastMouseX <= eolRect.x && lastMouseY <= eolRect.y + charHeight) {
                    word = Utilities.getIdentifier(doc, pos);
                }
            }
        } catch (BadLocationException e) {
            // word will be null
        }

        return word;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getInitialDelay() {
        return enterTimer.getDelay();
    }

    public void setInitialDelay(int delay) {
        enterTimer.setDelay(delay);
    }

    public int getDismissDelay() {
        return exitTimer.getDelay();
    }

    public void setDismissDelay(int delay) {
        exitTimer.setDelay(delay);
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == enterTimer) {
            if (toolTip != null && toolTip.isVisible()) { // already displayed
                return;
            }
            setToolTipVisible(true);
        } else if (evt.getSource() == exitTimer) {
            setToolTipVisible(false);
        }
    }

    public void mouseClicked(MouseEvent evt) {
        setToolTipVisible(false);
    }

    public void mousePressed(MouseEvent evt) {
        setToolTipVisible(false);
    }

    public void mouseReleased(MouseEvent evt) {
        setToolTipVisible(false);
    }

    public void mouseEntered(MouseEvent evt) {
        //    enabled = true;
    }

    public void mouseExited(MouseEvent evt) {
        //    enabled = false;
    }

    public void mouseDragged(MouseEvent evt) {
        setToolTipVisible(false);
    }

    public void mouseMoved(MouseEvent evt) {
        setToolTipVisible(false);
        if (enabled) {
            enterTimer.restart();
        }
        lastMouseX = evt.getX();
        lastMouseY = evt.getY();
    }

}

/*
 * Log
 *  8    Gandalf-post-FCS1.6.1.0     3/8/00   Miloslav Metelka 
 *  7    Gandalf   1.6         1/16/00  Miloslav Metelka 
 *  6    Gandalf   1.5         1/13/00  Miloslav Metelka 
 *  5    Gandalf   1.4         11/14/99 Miloslav Metelka 
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         8/17/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/29/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/26/99  Miloslav Metelka 
 * $
 */

