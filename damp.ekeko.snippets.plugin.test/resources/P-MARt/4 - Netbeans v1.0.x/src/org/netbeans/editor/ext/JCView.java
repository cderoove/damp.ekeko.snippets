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

package org.netbeans.editor.ext;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.AbstractListModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.EditorKit;
import javax.swing.text.Document;
import org.netbeans.editor.*;

/**
* Java completion display formatting and services
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JCView extends JScrollPane
    implements ActionListener, SettingsChangeListener {

    private static final Dimension MAX_SIZE = new Dimension(400, 300);

    private static final Dimension MIN_SIZE = new Dimension(100, 50);

    private static final Dimension PLUS_SIZE = new Dimension(20, 20);

    public static final String JCOMPLETION_VIEW_PROP = "jcompletion-view"; // NOI18N

    static final long serialVersionUID =2254850878595695671L;

    private JList list;

    protected JTextComponent component;

    private JCQuery query;

    private JCQuery.QueryResult queryResult;

    JLabel topLabel;

    Timer refreshTimer;

    private DocumentListener docL;

    private PropertyChangeListener docChangeL;

    /** Reserved space around the caret */
    static final int CARET_THRESHOLD = 5;

    private static JCView getFromExtUI(JTextComponent c) {
        return (JCView)Utilities.getExtUI(c).getProperty(JCOMPLETION_VIEW_PROP);
    }

    public static JCView getView(JTextComponent c) {
        JCView view = getFromExtUI(c);
        if (view == null) {
            view = new JCView(c);
            Utilities.getExtUI(c).putProperty(JCOMPLETION_VIEW_PROP, view);
        }
        return view;
    }

    public static boolean isViewVisible(JTextComponent c) {
        JCView view = getFromExtUI(c);
        boolean v = false;
        if (view != null) {
            v = view.isVisible();
        }
        return v;
    }

    public static void setViewVisible(JTextComponent c, boolean visible) {
        if (visible) {
            JCView view = getView(c);
            if (view != null) {
                view.showHelp(false);
            }
        } else {
            if (isViewVisible(c)) {
                JCView view = getView(c);
                view.setVisible(false);
            }
        }
    }

    public static void refreshView(JTextComponent c, boolean post) {
        if (c != null) {
            JCView view = getFromExtUI(c);
            if (view != null && view.isVisible()) { // visible
                view.showHelp(post);
            }
        }
    }

    public static Object getSelectedValue(JTextComponent c) {
        if (c != null) {
            JCView view = getFromExtUI(c);
            if (view != null && view.isVisible()) {
                return view.getSelectedValue();
            }
        }
        return null;
    }

    public static Object getFirstResultItem(JTextComponent c, int pos) {
        JCView view = getView(c);
        if (view != null) {
            JCQuery query = view.getQuery();
            if (query != null) {
                JCQuery.QueryResult qr = query.getHelp(c, pos, true);
                if (qr != null) {
                    List data = qr.getData();
                    if (data != null && data.size() > 0) {
                        return data.get(0);
                    }
                }
            }
        }
        return null;
    }

    /** Construct new view */
    public JCView(JTextComponent component) {
        super();

        // Prevent the bug with displaying without the scrollbar
        getViewport().setMinimumSize(new Dimension(4,4));
        this.component = component;

        super.setVisible(false);
        installInnerComponents();
        init();

        putClientProperty ("HelpID", JCView.class.getName ()); // NOI18N
    }

    private void init() {
        BaseKit kit = Utilities.getKit(component);
        if (kit != null) {
            getList().registerKeyboardAction(kit.getActionByName(JavaKit.jCompletionHideAction),
                                             KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                             JComponent.WHEN_FOCUSED
                                            );
        }

        component.addFocusListener(
            new FocusAdapter() {
                public void focusLost(FocusEvent evt) {
                    SwingUtilities.invokeLater(
                        new Runnable() {
                            public void run() {
                                if (isVisible()) {
                                    Component focusOwner = SwingUtilities.windowForComponent(
                                                               component).getFocusOwner();
                                    if (!(focusOwner != null
                                            && (focusOwner == component || focusOwner == JCView.this || focusOwner == getList())
                                         )) {
                                        setVisible(false); // both JC and component don't own the focus
                                    }
                                }
                            }
                        }
                    );
                }
            }
        );

        // Create timer; delay will be changed later
        refreshTimer = new Timer(500, this);
        refreshTimer.setRepeats(false);

        Settings.addSettingsChangeListener(this);
        settingsChange(null);

        // Create document listener
        docL = new DocumentListener() {
                   public void insertUpdate(DocumentEvent e) {
                       if (e.getLength() > 0) {
                           invalidateQueryResult();
                       }
                   }

                   public void removeUpdate(DocumentEvent e) {
                       if (e.getLength() > 0) {
                           invalidateQueryResult();
                       }
                   }

                   public void changedUpdate(DocumentEvent e) {
                   }
               };

        // Document change listener
        docChangeL = new PropertyChangeListener() {
                         public void propertyChange(PropertyChangeEvent evt) {
                             if ("document".equals(evt.getPropertyName())) {
                                 Document oldDoc = (Document)evt.getOldValue();
                                 Document newDoc = (Document)evt.getNewValue();
                                 oldDoc.removeDocumentListener(docL);
                                 newDoc.addDocumentListener(docL);
                             }
                         }
                     };

        Document doc = component.getDocument();
        doc.addDocumentListener(docL);
        component.addPropertyChangeListener(docChangeL);
    }

    public void settingsChange(SettingsChangeEvent evt) {
        Class kitClass = Utilities.getKitClass(component);
        int delay = SettingsUtil.getInteger(kitClass,
                                            ExtSettings.JCOMPLETION_REFRESH_DELAY, ExtSettings.defaultJCRefreshDelay);
        refreshTimer.setDelay(delay);
    }

    private void checkAddToPane() {
        // Install into layered pane
        JRootPane rp = component.getRootPane();
        if (rp == null) { // null RootPane!
            System.err.println("Editor component has no RootPane! Cannot install Java Completion JList"); // NOI18N
        }
        JRootPane thisRP = getRootPane();
        if (thisRP != rp) {
            if (thisRP != null) {
                thisRP.remove(this);
            }
            rp.getLayeredPane().add(this, JLayeredPane.POPUP_LAYER, 0);
        }
    }

    public final JCQuery getQuery() {
        if (query == null) {
            query = createQuery();
        }
        return query;
    }

    protected JCQuery createQuery() {
        return new JCQuery();
    }

    protected void installInnerComponents() {
        list = createList();
        setViewportView(list);
        //    getViewport().setMinimumSize(new Dimension(4,4));

        topLabel = createTopLabel();
        setColumnHeaderView(topLabel);
    }

    public JLabel getTopLabel() {
        return topLabel;
    }

    protected ListCellRenderer createCellRenderer() {
        return (ListCellRenderer)SettingsUtil.getValue(Utilities.getKitClass(component),
                ExtSettings.JCOMPLETION_CELL_RENDERER, new JCCellRenderer());
    }

    protected JList createList() {
        JList l = new JList();
        l.setCellRenderer(createCellRenderer());
        l.addMouseListener(new JCMouseListener());
        return l;
    }

    protected JLabel createTopLabel() {
        JLabel l = new JLabel();
        l.setForeground(Color.blue);
        l.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        return l;
    }

    public JList getList() {
        return list;
    }

    /** Populate the completion list with the results of the query.
    */
    synchronized void populate(JCQuery.QueryResult queryResult) {
        this.queryResult = queryResult; // will be used when updating the text

        if (queryResult != null) {
            // Set the top title
            getTopLabel().setText(queryResult.getTitle());

            // Set the class display offset
            ListCellRenderer r = getList().getCellRenderer();
            if (r instanceof JCCellRenderer) {
                ((JCCellRenderer)r).setClassDisplayOffset(queryResult.getClassDisplayOffset());
            }

            // Set the result data
            List data = queryResult.getData();
            if (data != null) {
                getList().setModel(new JCListModel(data));
                if (data.size() > 0) {
                    getList().setSelectedIndex(0);
                }
            }

            setVisible(true);
        }
    }

    public synchronized boolean updateCommonText() {
        return getQuery().updateCommonText(component, queryResult);
    }

    public synchronized boolean updateText() {
        Object replacement = getList().getSelectedValue();
        List data = ((JCListModel)getList().getModel()).getData();
        return getQuery().updateText(component, data, replacement, queryResult);
    }

    private JList getNonEmptyList() {
        JList l = getList();
        if (l.getModel().getSize() <= 0) {
            l = null;
        }
        return l;
    }

    public void moveDown() {
        JList l = getNonEmptyList();
        if (l != null) {
            l.setSelectedIndex(Math.min(l.getSelectedIndex() + 1,
                                        l.getModel().getSize() - 1));
            l.ensureIndexIsVisible(l.getSelectedIndex());
        }
    }

    public void moveUp() {
        JList l = getNonEmptyList();
        if (l != null) {
            l.setSelectedIndex(l.getSelectedIndex() - 1);
            l.ensureIndexIsVisible(l.getSelectedIndex());
        }
    }

    public void movePageDown() {
        JList l = getNonEmptyList();
        if (l != null) {
            int pageSize = Math.max(l.getLastVisibleIndex() - l.getFirstVisibleIndex(), 0);
            int lastInd = Math.max(Math.min(l.getLastVisibleIndex() + pageSize,
                                            l.getModel().getSize() - 1), 0);
            int ind = Math.max(Math.min(l.getSelectedIndex() + pageSize, lastInd), 0);

            l.ensureIndexIsVisible(lastInd);
            l.setSelectedIndex(ind);
            l.ensureIndexIsVisible(ind);
        }
    }

    public void movePageUp() {
        JList l = getNonEmptyList();
        if (l != null) {
            int pageSize = Math.max(l.getLastVisibleIndex() - l.getFirstVisibleIndex(), 0);
            int firstInd = Math.max(l.getFirstVisibleIndex() - pageSize, 0);
            int ind = Math.max(l.getSelectedIndex() - pageSize, firstInd);

            l.ensureIndexIsVisible(firstInd);
            l.setSelectedIndex(ind);
            l.ensureIndexIsVisible(ind);
        }
    }

    public void moveBegin() {
        JList l = getNonEmptyList();
        if (l != null) {
            l.setSelectedIndex(0);
            l.ensureIndexIsVisible(0);
        }
    }

    public void moveEnd() {
        JList l = getNonEmptyList();
        if (l != null) {
            int ind = l.getModel().getSize() - 1;
            l.setSelectedIndex(ind);
            l.ensureIndexIsVisible(ind);
        }
    }

    public Object getSelectedValue() {
        return getList().getSelectedValue();
    }

    public synchronized void setVisible(final boolean visible) {
        if (visible) {
            checkAddToPane();
        } else {
            refreshTimer.stop();
        }

        super.setVisible(visible);

        if (visible) {
            getList().setSelectedIndex(0);
            getList().ensureIndexIsVisible(getList().getSelectedIndex());
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        Rectangle bounds = getPreferredBounds();
                        setBounds(bounds);
                        revalidate();
                    }
                }
            );
        } else { // making invisible
            component.requestFocus();
        }
    }

    protected Rectangle getPreferredBounds() {
        Rectangle r = new Rectangle(getList().getPreferredSize());
        r.width += PLUS_SIZE.width;
        r.height += PLUS_SIZE.height;

        // update by top label width
        Dimension tld = topLabel.getPreferredSize();
        Insets i = getInsets();
        if (i != null) {
            tld.width += i.left + i.right;
        }
        r.width = Math.max(r.width, tld.width);

        r.width = Math.min(Math.max(r.width, MIN_SIZE.width), MAX_SIZE.width);
        r.height = Math.min(Math.max(r.height, MIN_SIZE.height), MAX_SIZE.height);

        ExtUI extUI = Utilities.getExtUI(component);
        Rectangle extBounds = extUI.getExtentBounds();
        Rectangle caretRect = (Rectangle)component.getCaret();

        // Compute y coord. under the caret relative to the beggining of window
        int underCaretStartRelY = caretRect.y + caretRect.height + CARET_THRESHOLD - extBounds.y;
        // Compute y coord. above the caret
        int aboveCaretEndRelY = (caretRect.y - extBounds.y) - CARET_THRESHOLD;

        // Update y and height
        if (extBounds.height - underCaretStartRelY >= r.height) { // enough space under caret?
            r.y = underCaretStartRelY;
        } else if (extBounds.height - underCaretStartRelY >= aboveCaretEndRelY) {
            r.y = underCaretStartRelY; // more space under caret than up from it
            r.height = extBounds.height - underCaretStartRelY;
        } else { // more space up from top of window till the caret position
            r.height = Math.min(aboveCaretEndRelY, r.height);
            r.y = aboveCaretEndRelY - r.height;
        }

        // Update x and width
        if (r.width < extBounds.width) {
            r.x = Math.min(caretRect.x - extBounds.x, extBounds.width - r.width);
        } else { // width too big
            r.x = 0;
        }

        return r;
    }

    /** Show the code completion help.
    * @param post post the request so the timer is started and
    *   the code completion window is refreshed when the timer expires.
    */
    public synchronized void showHelp(boolean post) {
        if (isVisible() && post) { // view already visible
            refreshTimer.start();
        } else { // not yet visible, display immediately
            refreshTimer.stop();
            actionPerformed(null);
        }
    }

    public synchronized void invalidateQueryResult() {
        queryResult = null;
    }

    /** It's called to notify that the refresh timer was triggered. */
    public void actionPerformed(ActionEvent evt) {
        populate(getQuery().getHelp(component));
    }

    public static class JCListModel extends AbstractListModel {

        List data;

        static final long serialVersionUID =3292276783870598274L;

        public JCListModel(List data) {
            this.data = data;
        }

        public int getSize() {
            return data.size();
        }

        public Object getElementAt(int index) {
            return (index >= 0 && index < data.size()) ? data.get(index) : null;
        }

        List getData() {
            return data;
        }
    }

    static class JCMouseListener extends MouseAdapter {

        public void mouseClicked(MouseEvent evt) {
            if (SwingUtilities.isLeftMouseButton(evt)
                    && evt.getClickCount() == 2
               ) {
                Object src = evt.getSource();
                JTextComponent target = null;
                if (src instanceof Component) {
                    JCView v = (JCView)SwingUtilities.getAncestorOfClass(JCView.class, (Component)src);
                    target = v.component;
                }

                if (target != null) {
                    BaseKit kit = Utilities.getKit(target);
                    if (kit != null) {
                        Action a = kit.getActionByName(JavaKit.insertBreakAction);
                        if (a != null) {
                            a.actionPerformed(new ActionEvent(target, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                        }
                    }
                }
            }
        }

    }
}

/*
 * Log
 *  28   Gandalf-post-FCS1.26.1.0    3/8/00   Miloslav Metelka 
 *  27   Gandalf   1.26        1/16/00  Miloslav Metelka 
 *  26   Gandalf   1.25        1/15/00  Miloslav Metelka #5270
 *  25   Gandalf   1.24        1/13/00  Miloslav Metelka Localization
 *  24   Gandalf   1.23        1/11/00  Jesse Glick     Context help.
 *  23   Gandalf   1.22        1/10/00  Miloslav Metelka 
 *  22   Gandalf   1.21        11/14/99 Miloslav Metelka 
 *  21   Gandalf   1.20        11/9/99  Miloslav Metelka 
 *  20   Gandalf   1.19        11/8/99  Miloslav Metelka 
 *  19   Gandalf   1.18        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  18   Gandalf   1.17        10/11/99 Miloslav Metelka fixed focus problems
 *  17   Gandalf   1.16        10/10/99 Miloslav Metelka 
 *  16   Gandalf   1.15        10/4/99  Miloslav Metelka 
 *  15   Gandalf   1.14        8/18/99  Miloslav Metelka 
 *  14   Gandalf   1.13        8/18/99  Miloslav Metelka 
 *  13   Gandalf   1.12        8/17/99  Miloslav Metelka 
 *  12   Gandalf   1.11        8/10/99  Miloslav Metelka no gray edges
 *  11   Gandalf   1.10        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  10   Gandalf   1.9         7/30/99  Miloslav Metelka 
 *  9    Gandalf   1.8         7/29/99  Miloslav Metelka 
 *  8    Gandalf   1.7         7/26/99  Miloslav Metelka 
 *  7    Gandalf   1.6         7/21/99  Miloslav Metelka 
 *  6    Gandalf   1.5         7/21/99  Miloslav Metelka 
 *  5    Gandalf   1.4         7/20/99  Miloslav Metelka 
 *  4    Gandalf   1.3         6/25/99  Miloslav Metelka from floats back to ints
 *  3    Gandalf   1.2         6/10/99  Miloslav Metelka 
 *  2    Gandalf   1.1         6/10/99  Miloslav Metelka 
 *  1    Gandalf   1.0         6/8/99   Miloslav Metelka 
 * $
 */

