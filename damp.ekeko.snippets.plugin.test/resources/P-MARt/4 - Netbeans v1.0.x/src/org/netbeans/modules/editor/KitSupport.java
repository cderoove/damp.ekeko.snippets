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

package org.netbeans.modules.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Map;
import java.util.HashMap;
import javax.swing.JEditorPane;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.JTextComponent;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.JumpList;
import org.netbeans.editor.view.ViewSettings;
import org.openide.loaders.DataObject;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.actions.UndoAction;
import org.openide.actions.RedoAction;
import org.openide.actions.CutAction;
import org.openide.actions.CopyAction;
import org.openide.actions.PasteAction;
import org.openide.actions.DeleteAction;
import org.openide.actions.FindAction;
import org.openide.actions.ReplaceAction;
import org.openide.actions.GotoAction;
import org.openide.actions.SaveAction;
import org.openide.actions.CompileAction;
import org.openide.actions.ExecuteAction;
import org.openide.actions.ToolsAction;
import org.openide.actions.NewAction;
import org.openide.actions.PropertiesAction;
import org.openide.text.Line;
import org.openide.cookies.LineCookie;
import org.openide.cookies.EditorCookie;
import org.openide.windows.TopComponent;

/**
* Support for common kit actions and for translating
* editor actions to nb actions and back
*
* @author Miloslav Metelka
* @version 1.00
*/

public class KitSupport {

    public static final String systemActionSave = "system-action-save"; // NOI18N

    public static final String systemActionCompile = "system-action-compile"; // NOI18N

    public static final String systemActionExecute = "system-action-execute"; // NOI18N

    public static final String systemActionTools = "system-action-tools"; // NOI18N

    public static final String systemActionNew = "system-action-new"; // NOI18N

    public static final String systemActionProperties = "system-action-properties"; // NOI18N

    /** Editor action name to NB action class mapping 
     * @associates Class*/
    static Map ed2nb = new HashMap();

    /** NB action class to Editor action name mapping 
     * @associates String*/
    static Map nb2ed = new HashMap();

    static void init() {
        //    addMapping(BaseKit.undoAction, UndoAction.class);
        //    addMapping(BaseKit.redoAction, RedoAction.class);

        addMapping(BaseKit.cutAction, CutAction.class);
        addMapping(BaseKit.copyAction, CopyAction.class);
        addMapping(BaseKit.pasteAction, PasteAction.class);
        addMapping(BaseKit.removeSelectionAction, DeleteAction.class);

        addMapping(BaseKit.findAction, FindAction.class);
        addMapping(BaseKit.replaceAction, ReplaceAction.class);
        addMapping(BaseKit.gotoAction, GotoAction.class);

        addMapping(systemActionSave, SaveAction.class);
        addMapping(systemActionCompile, CompileAction.class);
        addMapping(systemActionExecute, ExecuteAction.class);
        addMapping(systemActionNew, NewAction.class);
        addMapping(systemActionTools, ToolsAction.class);
        addMapping(systemActionProperties, PropertiesAction.class);
    }

    public static void addMapping(String editorActionName, Class nbActionClass) {
        ed2nb.put(editorActionName, nbActionClass);
        nb2ed.put(nbActionClass, editorActionName);
    }

    public static Class getNbActionClass(String editorActionName) {
        return (Class)ed2nb.get(editorActionName);
    }

    public static SystemAction getNbAction(String editorActionName) {
        Class ac = getNbActionClass(editorActionName);
        return (ac != null) ? SystemAction.get(ac) : null;
    }

    public static String getEditorActionName(Class nbActionClass) {
        return (String)nb2ed.get(nbActionClass);
    }

    public static Action getEditorAction(Class nbActionClass, Class kitClass) {
        String an = getEditorActionName(nbActionClass);
        if (an != null) {
            return BaseKit.getKit(kitClass).getActionByName(an);
        }
        return null;
    }

    public static void updateSystemActionPerformer(final JTextComponent c,
            String editorActionName) {
        BaseKit kit = Utilities.getKit(c);
        if (kit != null) {
            final Action ea = kit.getActionByName(editorActionName);
            if (ea != null) {
                final SystemAction sa = getNbAction(editorActionName);
                if (sa instanceof CallbackSystemAction) {
                    ((CallbackSystemAction)sa).setActionPerformer(
                        new ActionPerformer() {
                            public void performAction(SystemAction action) {
                                SwingUtilities.invokeLater(
                                    new Runnable() {
                                        public void run() {
                                            ea.actionPerformed(new ActionEvent(c, 0, "")); // NOI18N
                                        }
                                    }
                                );
                            }
                        }
                    );
                }
            }
        }
    }

    public static PropertyChangeListener syncEnabling(JTextComponent c,
            String editorActionName) {
        PropertyChangeListener l = null;
        BaseKit kit = Utilities.getKit(c);
        if (kit != null) {
            final Action ea = kit.getActionByName(editorActionName);
            if (ea != null) {
                final SystemAction sa = getNbAction(editorActionName);
                if (sa instanceof CallbackSystemAction) {
                    ea.addPropertyChangeListener(
                        l = new PropertyChangeListener() {
                                public void propertyChange(PropertyChangeEvent evt) {
                                    if ("enabled".equals(evt.getPropertyName())) { // NOI18N
                                        sa.setEnabled(((Boolean)evt.getNewValue()).booleanValue());
                                    }
                                }
                            }
                    );
                    sa.setEnabled(ea.isEnabled());
                }
            }
        }
        return l;
    }

    public static void unsyncEnabling(JTextComponent c,
                                      String editorActionName, PropertyChangeListener l) {
        if (l != null) {
            BaseKit kit = Utilities.getKit(c);
            if (kit != null) {
                final Action ea = kit.getActionByName(editorActionName);
                if (ea != null) {
                    ea.removePropertyChangeListener(l);
                }
            }
        }
    }

    public static void updateActions(final JTextComponent c) {
        c.addFocusListener(
            new FocusListener() {

                PropertyChangeListener removeSelectionL;

                public void focusGained(FocusEvent evt) {
                    updateSystemActionPerformer(c, BaseKit.findAction);
                    updateSystemActionPerformer(c, BaseKit.replaceAction);
                    updateSystemActionPerformer(c, BaseKit.gotoAction);
                    updateSystemActionPerformer(c, BaseKit.removeSelectionAction);

                    removeSelectionL = syncEnabling(c, BaseKit.removeSelectionAction);

                    Document doc = c.getDocument();
                    if (doc != null) {
                        DataObject dob = getDataObject(doc);
                        if (dob != null) {
                            org.openide.filesystems.FileObject fo = dob.getPrimaryFile();
                            if (fo != null) {
                                fo.refresh();
                            }
                        }
                    }
                }

                public void focusLost(FocusEvent evt) {
                    if (removeSelectionL != null) {
                        unsyncEnabling(c, BaseKit.removeSelectionAction, removeSelectionL);
                    }
                }
            }
        );

    }

    /** Get the dataobject from the document */
    public static DataObject getDataObject(Document doc) {
        return (DataObject)doc.getProperty(Document.StreamDescriptionProperty);
    }

    /** This method is a composition of <tt>Utilities.getIdentifierBlock()</tt>
    * and <tt>SyntaxSupport.getFunctionBlock()</tt>.
    * @return null if there's no identifier at the given position.
    *   identifier block if there's identifier but it's not a function call.
    *   three member array for the case that there is an identifier followed
    *   by the function call character. The first two members are members
    *   of the identifier block and the third member is the second member
    *   of the function block.
    */
    public static int[] getIdentifierAndMethodBlock(BaseDocument doc, int pos)
    throws BadLocationException {
        int[] idBlk = Utilities.getIdentifierBlock(doc, pos);
        if (idBlk != null) {
            int[] funBlk = doc.getSyntaxSupport().getFunctionBlock(idBlk);
            if (funBlk != null) {
                return new int[] { idBlk[0], idBlk[1], funBlk[1] };
            }
        }
        return idBlk;
    }

    public static Line getLine(BaseDocument doc, int pos) {
        DataObject dob = KitSupport.getDataObject(doc);
        if (dob != null) {
            LineCookie lc = (LineCookie)dob.getCookie(LineCookie.class);
            if (lc != null) {
                Line.Set lineSet = lc.getLineSet();
                if (lineSet != null) {
                    try {
                        int lineOffset = Utilities.getLineOffset(doc, pos);
                        return lineSet.getOriginal(lineOffset);
                    } catch (BadLocationException e) {
                    }

                }
            }
        }
        return null;
    }

    public static Line getLine(JTextComponent target) {
        return getLine((BaseDocument)target.getDocument(), target.getCaret().getDot());
    }

    public static TopComponent getTopComponent(JTextComponent target) {
        return (TopComponent)SwingUtilities.getAncestorOfClass(TopComponent.class, target);
    }

    public static void addJumpListEntry(DataObject dob) {
        final EditorCookie ec = (EditorCookie)dob.getCookie(EditorCookie.class);
        if (ec != null) {
            final Timer timer = new Timer(500, null);
            timer.addActionListener(
                new ActionListener() {

                    private int countDown = 10;

                    public void actionPerformed(ActionEvent evt) {
                        SwingUtilities.invokeLater(
                            new Runnable() {
                                public void run() {
                                    if (--countDown >= 0) {
                                        JEditorPane[] panes = ec.getOpenedPanes();
                                        if (panes != null && panes.length > 0) {
                                            JumpList.checkAddEntry(panes[0]);
                                            timer.stop();
                                        }
                                    } else {
                                        timer.stop();
                                    }
                                }
                            }
                        );
                    }
                }
            );
            timer.start();
        }
    }

}

/*
 * Log
 *  19   Gandalf-post-FCS1.17.1.0    4/5/00   Miloslav Metelka undo/redo updating 
 *       removed
 *  18   Gandalf   1.17        2/15/00  Miloslav Metelka wrong performers 
 *       updating
 *  17   Gandalf   1.16        1/18/00  Miloslav Metelka 
 *  16   Gandalf   1.15        1/18/00  Miloslav Metelka 
 *  15   Gandalf   1.14        1/16/00  Miloslav Metelka 
 *  14   Gandalf   1.13        1/13/00  Miloslav Metelka Localization
 *  13   Gandalf   1.12        1/10/00  Miloslav Metelka 
 *  12   Gandalf   1.11        1/7/00   Miloslav Metelka 
 *  11   Gandalf   1.10        1/4/00   Miloslav Metelka 
 *  10   Gandalf   1.9         12/28/99 Miloslav Metelka 
 *  9    Gandalf   1.8         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    Gandalf   1.7         8/19/99  Miloslav Metelka DeleteAction handling
 *  7    Gandalf   1.6         8/18/99  Miloslav Metelka 
 *  6    Gandalf   1.5         8/5/99   Jaroslav Tulach Tools & New action in 
 *       editor.
 *  5    Gandalf   1.4         7/22/99  Miloslav Metelka Safe casting
 *  4    Gandalf   1.3         7/20/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/9/99   Miloslav Metelka 
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         5/17/99  Miloslav Metelka 
 * $
 */

