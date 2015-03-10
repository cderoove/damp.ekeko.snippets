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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FontMetrics;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.Timer;
import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.text.JTextComponent;
import javax.swing.text.Caret;

/**
* Status bar support
*
* @author Miloslav Metelka
* @version 1.00
*/

public class StatusBar implements PropertyChangeListener, SettingsChangeListener {

    public static final String CELL_MAIN = "main"; // NOI18N

    public static final String CELL_POSITION = "position"; // NOI18N

    public static final String CELL_TYPING_MODE = "typing-mode"; // NOI18N

    public static final String INSERT_LOCALE = "status-bar-insert"; // NOI18N
    public static final String INSERT_DEFAULT = "INS"; // NOI18N

    public static final String OVERWRITE_LOCALE = "status-bar-overwrite"; // NOI18N
    public static final String OVERWRITE_DEFAULT = "OVR"; // NOI18N

    private static final String[] POS_MAX_STRINGS = new String[] { "99999:999" }; // NOI18N

    private static final Insets NULL_INSETS = new Insets(0, 0, 0, 0);

    static final Border CELL_BORDER
    = BorderFactory.createCompoundBorder(
          BorderFactory.createBevelBorder(BevelBorder.LOWERED),
          BorderFactory.createEmptyBorder(0, 2, 0, 2)
      );

    protected ExtUI extUI;

    /** The status bar panel into which the cells are added. */
    private JPanel panel;

    private boolean visible;

    private Coloring coloring;

    private Coloring boldColoring;

    private List cellList = new ArrayList();

    private CaretListener caretL;

    private int caretDelay;

    private boolean overwriteModeDisplayed;

    private String insText;

    private String ovrText;

    static final long serialVersionUID =-6266183959929157349L;

    public StatusBar(ExtUI extUI) {
        this.extUI = extUI;

        caretDelay = 500;
        caretL = new CaretListener(caretDelay);
        insText = LocaleSupport.getString(INSERT_LOCALE, INSERT_DEFAULT);
        ovrText = LocaleSupport.getString(OVERWRITE_LOCALE, OVERWRITE_DEFAULT);

        // Add caret listener
        JTextComponent c = extUI.getComponent();
        if (c != null) {
            Caret caret = c.getCaret();
            if (caret != null) {
                caret.addChangeListener(caretL);
            }
        }

        Settings.addSettingsChangeListener(this);
        extUI.addPropertyChangeListener(this);
        settingsChange(null);
    }

    public void settingsChange(SettingsChangeEvent evt) {
        Class kitClass = Utilities.getKitClass(extUI.getComponent());
        String settingName = (evt != null) ? evt.getSettingName() : null;
        if (kitClass != null) {
            Coloring dc = extUI.getDefaultColoring();
            coloring = extUI.getColoring(Settings.STATUS_BAR_COLORING);
            boldColoring = extUI.getColoring(Settings.STATUS_BAR_BOLD_COLORING);

            // assign coloring
            if (coloring != null) {
                coloring = coloring.apply(dc);
            } else {
                coloring = dc;
            }

            // assign bold coloring
            if (boldColoring != null) {
                boldColoring = boldColoring.apply(dc);
            } else {
                boldColoring = dc;
            }

            // apply coloring to the status bar components
            refreshPanel();

            if (settingName == null || Settings.STATUS_BAR_CARET_DELAY.equals(settingName)) {
                caretDelay = SettingsUtil.getInteger(kitClass, Settings.STATUS_BAR_CARET_DELAY,
                                                     DefaultSettings.defaultStatusBarCaretDelay);
                if (caretL != null) {
                    caretL.setDelay(caretDelay);
                }
            }

            if (settingName == null || Settings.STATUS_BAR_VISIBLE.equals(settingName)) {
                boolean wantVisible = SettingsUtil.getBoolean(kitClass,
                                      Settings.STATUS_BAR_VISIBLE, false);
                setVisible(wantVisible);
            }

        }
    }

    protected JPanel createPanel() {
        return new JPanel(new GridBagLayout());
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean v) {
        if (v != visible) {
            visible = v;

            if (panel != null || visible) {
                if (visible) { // need to refresh first
                    refreshPanel();
                }
                getPanel().setVisible(visible);
            }
        }
    }

    public final JPanel getPanel() {
        if (panel == null) {
            panel = createPanel();
            initPanel();
        }
        return panel;
    }

    protected void initPanel() {
        addCell(CELL_POSITION, POS_MAX_STRINGS).setHorizontalAlignment(
            SwingConstants.CENTER);
        addCell(CELL_TYPING_MODE, new String[] { insText, ovrText }).setHorizontalAlignment(
            SwingConstants.CENTER);
        setText(CELL_TYPING_MODE, insText);
        addCell(CELL_MAIN, null);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();

        if (ExtUI.COMPONENT_PROPERTY.equals(propName)) {
            if (evt.getNewValue() != null) { // just installed
                JTextComponent c = extUI.getComponent();

                c.addPropertyChangeListener(this);

                Caret caret = c.getCaret();
                if (caret != null) {
                    caret.addChangeListener(caretL);
                }

                settingsChange(null);
                refreshPanel();

            } else { // just deinstalled
                JTextComponent c = (JTextComponent)evt.getOldValue();

                c.removePropertyChangeListener(this);

                Caret caret = c.getCaret();
                if (caret != null) {
                    caret.removeChangeListener(caretL);
                }

            }

        } else if ("caret".equals(propName)) {
            Caret oldCaret = (Caret)evt.getOldValue();
            Caret newCaret = (Caret)evt.getNewValue();
            if (oldCaret != null) {
                oldCaret.removeChangeListener(caretL);
            }
            if (newCaret != null) {
                newCaret.addChangeListener(caretL);
            }
        }
    }

    public int getCellCount() {
        return cellList.size();
    }

    public JLabel addCell(String name, String[] widestStrings) {
        return addCell(-1, name, widestStrings);
    }

    public JLabel addCell(int i, String name, String[] widestStrings) {
        Cell c = new Cell(name, widestStrings);
        addCellImpl(i, c);
        return c;
    }

    public void addCustomCell(int i, JLabel c) {
        addCellImpl(i, c);
    }

    private void addCellImpl(int i, JLabel c) {
        synchronized (cellList) {
            ArrayList newCellList = new ArrayList(cellList); // copy because of iterators
            int cnt = newCellList.size();
            if (i < 0 || i > cnt) {
                i = cnt;
            }
            newCellList.add(i, c);

            cellList = newCellList;
        }

        refreshPanel();
    }

    public JLabel getCellByName(String name) {
        Iterator i = cellList.iterator();
        while (i.hasNext()) {
            JLabel c = (JLabel)i.next();
            if (name.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }

    public String getText(String cellName) {
        JLabel cell = getCellByName(cellName);
        return (cell != null) ? cell.getText() : null;
    }

    public void setText(String cellName, String text) {
        setText(cellName, text, null);
    }

    public void setBoldText(String cellName, String text) {
        setText(cellName, text, boldColoring);
    }

    public void setText(String cellName, String text,
                        Coloring extraColoring) {
        // Should use invokeLater()?
        JLabel cell = getCellByName(cellName);
        if (cell != null) {
            Coloring c = coloring;
            if (extraColoring != null) {
                c = extraColoring.apply(c);
            }
            c.apply(cell);
            cell.setText(text);
        }
    }

    /* Refresh the whole panel by removing all the components
    * and adding only those that appear in the cell-list.
    */
    private void refreshPanel() {
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    if (isVisible()) { // refresh only if visible
                        // apply the coloring
                        Iterator it = cellList.iterator();
                        while (it.hasNext()) {
                            JLabel c = (JLabel)it.next();
                            if (c instanceof Cell) {
                                coloring.apply(c);
                            }
                        }

                        // layout the cells
                        GridBagConstraints gc = new GridBagConstraints();
                        gc.gridx = GridBagConstraints.RELATIVE;
                        gc.gridwidth = 1;
                        gc.gridheight = 1;
                        Iterator i = cellList.iterator();
                        while (i.hasNext()) {
                            JLabel c = (JLabel)i.next();
                            boolean main = CELL_MAIN.equals(c.getName());
                            if (main) {
                                gc.fill = GridBagConstraints.HORIZONTAL;
                                gc.weightx = 1.0;
                            }
                            getPanel().add(c, gc);
                            if (main) {
                                gc.fill = GridBagConstraints.NONE;
                                gc.weightx = 0;
                            }
                        }
                    }
                }
            }
        );
    }

    class CaretListener implements ChangeListener, ActionListener {

        Timer timer;

        CaretListener(int delay) {
            timer = new Timer(delay, this);
            timer.setRepeats(false);
        }

        void setDelay(int delay) {
            timer.setDelay(delay);
            timer.setInitialDelay(delay);
        }

        public void stateChanged(ChangeEvent evt) {
            timer.restart();
        }

        public void actionPerformed(ActionEvent evt) {
            JTextComponent component = extUI.getComponent();
            if (component != null) {
                Caret caret = component.getCaret();
                BaseDocument doc = Utilities.getDocument(extUI.getComponent());
                if (caret != null && doc != null) {
                    int pos = caret.getDot();
                    String s = Utilities.debugPosition(doc, pos);
                    setText(CELL_POSITION, s);

                    Boolean b = (Boolean)extUI.getProperty(ExtUI.OVERWRITE_MODE_PROPERTY);
                    boolean om = (b != null && b.booleanValue());
                    if (om != overwriteModeDisplayed) {
                        overwriteModeDisplayed = om;
                        setText(CELL_TYPING_MODE, overwriteModeDisplayed ? ovrText : insText);
                    }
                }
            }
        }

    }

    static class Cell extends JLabel {

        Dimension maxDimension;

        String[] widestStrings;

        static final long serialVersionUID =-2554600362177165648L;

        Cell(String name, String[] widestStrings) {
            setName(name);
            setBorder(CELL_BORDER);
            setOpaque(true);
            this.widestStrings = widestStrings;
        }

        private void updateSize() {
            Font f = getFont();
            if (maxDimension == null) {
                maxDimension = new Dimension();
            }
            if (f != null) {
                Border b = getBorder();
                Insets ins = (b != null) ? b.getBorderInsets(this) : NULL_INSETS;
                FontMetrics fm = getFontMetrics(f);
                int mw = fm.stringWidth(this.getText());
                maxDimension.height = fm.getHeight() + ins.top + ins.bottom;
                if (widestStrings != null) {
                    for (int i = 0; i < widestStrings.length; i++) {
                        mw = Math.max(mw, fm.stringWidth(widestStrings[i]));
                    }
                }
                maxDimension.width = mw + ins.left + ins.right;
            }
        }

        public Dimension getPreferredSize() {
            if (maxDimension == null) {
                maxDimension = new Dimension();
            }
            return new Dimension(maxDimension);
        }

        public void setFont(Font f) {
            super.setFont(f);
            updateSize();
        }

    }

}

/*
 * Log
 *  18   Gandalf-post-FCS1.14.1.2    4/5/00   Miloslav Metelka status bar patch2
 *  17   Gandalf-post-FCS1.14.1.1    3/16/00  Miloslav Metelka fixed #5999
 *  16   Gandalf-post-FCS1.14.1.0    3/8/00   Miloslav Metelka 
 *  15   Gandalf   1.14        3/8/00   Miloslav Metelka project creating dedlok 
 *       fix
 *  14   Gandalf   1.13        1/13/00  Miloslav Metelka 
 *  13   Gandalf   1.12        1/4/00   Miloslav Metelka 
 *  12   Gandalf   1.11        12/28/99 Miloslav Metelka 
 *  11   Gandalf   1.10        11/14/99 Miloslav Metelka 
 *  10   Gandalf   1.9         11/8/99  Miloslav Metelka 
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         8/27/99  Miloslav Metelka 
 *  7    Gandalf   1.6         8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  6    Gandalf   1.5         8/17/99  Miloslav Metelka 
 *  5    Gandalf   1.4         7/26/99  Miloslav Metelka 
 *  4    Gandalf   1.3         7/22/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/21/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/20/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/9/99   Miloslav Metelka 
 * $
 */

