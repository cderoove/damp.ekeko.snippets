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

package org.netbeans.modules.editor.java;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.Action;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.JavaKit;
import org.netbeans.editor.ext.JCView;
import org.netbeans.editor.ext.JCQuery;
import org.netbeans.editor.ext.JCPackage;
import org.netbeans.editor.ext.JCField;
import org.netbeans.editor.ext.JCMethod;
import org.netbeans.editor.ext.ExtActionFactory;
import org.netbeans.editor.view.DefaultBuildToolTipAction;
import org.netbeans.editor.view.DefaultBuildPopupMenuAction;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.KitSupport;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.debugger.Debugger;
import org.openide.debugger.Watch;
import org.openide.debugger.Breakpoint;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.openide.actions.UndoAction;
import org.openide.actions.RedoAction;

/**
* Java editor kit with appropriate document
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorJavaKit extends JavaKit {

    public static final String gotoDeclarationAction = ExtActionFactory.gotoDeclarationAction;

    public static final String gotoSourceAction = "goto-source"; // NOI18N

    public static final String gotoHelpAction = "goto-help"; // NOI18N

    public static final String addWatchAction = "add-watch"; // NOI18N

    public static final String toggleBreakpointAction = "toggle-breakpoint"; // NOI18N

    static final long serialVersionUID =-5445829962533684922L;

    public Document createDefaultDocument() {
        BaseDocument doc = new NbEditorDocument(this.getClass());
        // Force '\n' as write line separator
        doc.putProperty(BaseDocument.WRITE_LINE_SEPARATOR_PROP, BaseDocument.LS_LF);
        return doc;
    }

    public void install(JEditorPane c) {
        super.install(c);
        KitSupport.updateActions(c);
    }

    /** Create syntax support */
    public SyntaxSupport createSyntaxSupport(BaseDocument doc) {
        return new NbJavaSyntaxSupport(doc);
    }

    public Class getFocusableComponentClass(JTextComponent c) {
        return TopComponent.class;
    }

    protected Action[] createActions() {
        Action[] javaActions = new Action[] {
                                   new BuildPopupMenuAction(),
                                   new BuildToolTipAction(),
                                   new ToggleBreakpointAction(),
                                   new AddWatchAction(),
                                   new GotoHelpAction(),
                                   new GotoSourceAction(),
                                   new GotoDeclarationAction(),
                                   new NbUndoAction(),
                                   new NbRedoAction(),
                               };
        return TextAction.augmentList(super.createActions(), javaActions);
    }


    public static class BuildPopupMenuAction extends DefaultBuildPopupMenuAction {

        static final long serialVersionUID =-8623762627678464181L;

        protected JMenuItem getItem(JTextComponent target, String actionName) {
            JMenuItem item = null;
            SystemAction sa = KitSupport.getNbAction(actionName);
            if (sa instanceof Presenter.Popup) {
                item = ((Presenter.Popup)sa).getPopupPresenter();
            } else { // editor action
                item = super.getItem(target, actionName);
            }

            return item;
        }

    }

    public static class BuildToolTipAction extends DefaultBuildToolTipAction {

        static final long serialVersionUID =-2009277037915948909L;

        public BuildToolTipAction() {
        }

        protected String buildText(JTextComponent target) {
            String text = null;
            NbJavaSyntaxSupport sup = (NbJavaSyntaxSupport)Utilities.getSyntaxSupport(target);
            Debugger debugger = sup.getDebugger();
            if (debugger != null) {
                int state = debugger.getState();
                if (state == Debugger.DEBUGGER_RUNNING || state == Debugger.DEBUGGER_STOPPED) {
                    String word = Utilities.getExtUI(target).getToolTipSupport().getIdentifierUnderCursor();
                    if (word != null) {
                        Watch watch = debugger.createWatch(word, true);
                        if (watch != null) {
                            String asText = watch.getAsText();
                            if (asText != null) {
                                text = word + '=' + asText;
                            }
                        }
                    }
                }
            }
            return text;
        }

    }

    public static class ToggleBreakpointAction extends BaseAction {

        public ToggleBreakpointAction() {
            super(toggleBreakpointAction);
            putValue ("helpID", ToggleBreakpointAction.class.getName ());
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                NbJavaSyntaxSupport sup = (NbJavaSyntaxSupport)Utilities.getSyntaxSupport(target);
                int dotPos = target.getCaret().getDot();
                Breakpoint b = sup.getBreakpoint(dotPos);
                if (b == null) {
                    sup.createBreakpoint(dotPos);
                } else {
                    b.remove();
                }
            }
        }

    }

    public static class AddWatchAction extends BaseAction {

        static final long serialVersionUID =4253425227297112737L;

        public AddWatchAction() {
            super(addWatchAction);
            putValue ("helpID", AddWatchAction.class.getName ());
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                int dotPos = target.getCaret().getDot();
                try {
                    String text = Utilities.getSelectionOrIdentifier(target, dotPos);

                    ResourceBundle bundle = NbBundle.getBundle (NbEditorJavaKit.class);
                    NotifyDescriptor.InputLine il =
                        new NotifyDescriptor.InputLine(bundle.getString ("CTL_Watch_Name"), // NOI18N
                                                       bundle.getString ("CTL_Watch_Title")); // NOI18N
                    il.setInputText (text);
                    Object r = TopManager.getDefault().notify(il);
                    NbJavaSyntaxSupport sup = (NbJavaSyntaxSupport)Utilities.getSyntaxSupport(target);
                    if (r == NotifyDescriptor.OK_OPTION) {
                        text = il.getInputText();
                        if (text != null) {
                            Debugger debugger = sup.getDebugger();
                            if (debugger != null) {
                                debugger.createWatch(text, false);
                            }
                        }
                    }
                } catch (BadLocationException e) {
                    // do nothing
                }
            }
        }

    }

    public static class GotoDeclarationAction extends ExtActionFactory.GotoDeclarationAction {

        public GotoDeclarationAction () {
            putValue ("helpID", GotoDeclarationAction.class.getName ());
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                if (JCView.isViewVisible(target)) { // completion window visible
                    Object item = JCView.getSelectedValue(target);
                    if (item != null) {
                        ((NbJavaSyntaxSupport)Utilities.getSyntaxSupport(target)).openSource(item, true, false);
                    }
                } else {
                    boolean found = false;
                    int dotPos = target.getCaret().getDot();
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    try {
                        int[] idFunBlk = KitSupport.getIdentifierAndMethodBlock(doc, dotPos);
                        if (idFunBlk != null && idFunBlk.length == 2) { // id but not function
                            int pos = Utilities.getFirstNonWhiteBwd(doc, idFunBlk[0]);
                            if (pos < 0 || doc.getChars(pos, 1)[0] != '.') { // because 'this.var' could search for local var
                                found = gotoDeclaration(target);
                            }
                        }

                        if (!found) {
                            if (idFunBlk == null) {
                                idFunBlk = new int[] { dotPos, dotPos };
                            }

                            for (int ind = idFunBlk.length - 1; ind >= 1; ind--) {
                                Object item = JCView.getFirstResultItem(target, idFunBlk[ind]);
                                if (item != null) {
                                    ((NbJavaSyntaxSupport)Utilities.getSyntaxSupport(target)).openSource(item, true, false);
                                    break;
                                }
                            }
                        }
                    } catch (BadLocationException e) {
                    }
                }
            }
        }

    }

    public static class GotoSourceAction extends BaseAction {

        public GotoSourceAction() {
            super(gotoSourceAction, SAVE_POSITION);
            putValue ("helpID", GotoSourceAction.class.getName ());
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                String msg = openSource(target, false);
                if (msg != null) { // not found
                    TopManager.getDefault().notify(new NotifyDescriptor.Message(msg));
                }
            }
        }

        public String openSource(JTextComponent target, boolean simulate) {
            Object item = null;
            String itemDesc = null;
            if (JCView.isViewVisible(target)) { // completion window visible
                item = JCView.getSelectedValue(target);
                if (item != null) {
                    itemDesc = ((NbJavaSyntaxSupport)Utilities.getSyntaxSupport(target)).openSource(item, false, simulate);
                }
            } else {
                try {
                    int dotPos = target.getCaret().getDot();
                    BaseDocument doc = (BaseDocument)target.getDocument();
                    int[] idFunBlk = KitSupport.getIdentifierAndMethodBlock(doc, dotPos);
                    if (idFunBlk == null) {
                        idFunBlk = new int[] { dotPos, dotPos };
                    }

                    for (int ind = idFunBlk.length - 1; ind >= 1; ind--) {
                        item = JCView.getFirstResultItem(target, idFunBlk[ind]);
                        if (item != null) {
                            itemDesc = ((NbJavaSyntaxSupport)Utilities.getSyntaxSupport(target)).openSource(item, false, simulate);
                            break;
                        }
                    }
                } catch (BadLocationException e) {
                }
            }

            // Complete the messages
            String msg = null;
            if (itemDesc != null) {
                boolean isPkg = (item instanceof JCPackage);
                msg = NbBundle.getBundle(NbEditorJavaKit.class).getString(
                          simulate ? (isPkg ? "goto_source_explore_package" : "goto_source_open_source") // NOI18N
          : (isPkg ? "goto_source_package_not_found" : "goto_source_source_not_found") // NOI18N
                      );
                msg = MessageFormat.format(msg, new Object [] { itemDesc } );
            }

            return msg;
        }

        public String getPopupMenuText(JTextComponent target) {
            return openSource(target, true); // simulate open
        }

    }

    public static class GotoHelpAction extends BaseAction {

        public GotoHelpAction() {
            super(gotoHelpAction, SAVE_POSITION);
            putValue ("helpID", GotoHelpAction.class.getName ());
        }

        public URL[] getJavaDocURLs(JTextComponent target) {
            if (JCView.isViewVisible(target)) { // completion window visible
                Object item = JCView.getSelectedValue(target);
                if (item != null) {
                    return ((NbJavaSyntaxSupport)Utilities.getSyntaxSupport(target)).getJavaDocURLs(item);
                }
            } else { // completion window not visible
                int dotPos = target.getCaret().getDot();
                BaseDocument doc = (BaseDocument)target.getDocument();
                try {
                    int[] idFunBlk = KitSupport.getIdentifierAndMethodBlock(doc, dotPos);
                    if (idFunBlk == null) {
                        idFunBlk = new int[] { dotPos, dotPos };
                    }

                    for (int ind = idFunBlk.length - 1; ind >= 1; ind--) {
                        Object item = JCView.getFirstResultItem(target, idFunBlk[ind]);
                        if (item != null) {
                            return ((NbJavaSyntaxSupport)Utilities.getSyntaxSupport(target)).getJavaDocURLs(item);
                        }
                    }
                } catch (BadLocationException e) {
                }
            }
            return null;
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                URL[] urls = getJavaDocURLs(target);
                if (urls != null && urls.length > 0) {
                    TopManager.getDefault().showUrl(urls[0]); // show first URL
                }
            }
        }

        public String getPopupMenuText(JTextComponent target) {
            return NbBundle.getBundle(NbEditorJavaKit.class).getString("show_javadoc"); // NOI18N
        }


    }

    public static class NbUndoAction extends org.netbeans.editor.ActionFactory.UndoAction {

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            UndoAction ua = (UndoAction)SystemAction.get(UndoAction.class);
            if (ua != null && ua.isEnabled()) {
                ua.actionPerformed(evt);
            }
        }

    }

    public static class NbRedoAction extends org.netbeans.editor.ActionFactory.RedoAction {

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            RedoAction ua = (RedoAction)SystemAction.get(RedoAction.class);
            if (ua != null && ua.isEnabled()) {
                ua.actionPerformed(evt);
            }
        }

    }

}

/*
 * Log
 *  7    Gandalf-post-FCS1.5.1.0     4/6/00   Miloslav Metelka undo action
 *  6    Gandalf   1.5         1/19/00  Jesse Glick     Context help.
 *  5    Gandalf   1.4         1/13/00  Miloslav Metelka Localization
 *  4    Gandalf   1.3         1/10/00  Miloslav Metelka 
 *  3    Gandalf   1.2         1/7/00   Miloslav Metelka 
 *  2    Gandalf   1.1         1/6/00   Miloslav Metelka fixed #4584
 *  1    Gandalf   1.0         1/4/00   Miloslav Metelka 
 * $
 */

