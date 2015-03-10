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

package org.netbeans.modules.web.core.jsploader;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.EditorKit;
import javax.swing.text.BadLocationException;

import org.openide.text.NbDocument;
import org.openide.text.EditorSupport;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObject;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.Workspace;
import org.openide.windows.Mode;
import org.openide.actions.ToggleBreakpointAction;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.SystemAction;
import org.openide.TopManager;

import org.netbeans.modules.java.JavaEditor;

/*
/** Editor for servlet files generated from JSP files. Main features:
* <ul>
* <li>All text is read-only (guarded) </li>
* </ul>
*
* @author Petr Jiricka
*/
public class JspServletEditor extends JavaEditor /*implements SourceCookie.Editor*/ {


    /** Create a new Editor support for the given Java source.
    * @param entry the (primary) file entry representing the Java source file
    */
    public JspServletEditor(MultiDataObject.Entry entry) {
        super(entry);
    }

    protected void loadFromStreamToKit (StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
        super.loadFromStreamToKit (doc, stream, kit);
        //NbDocument.markGuarded(doc, 0, doc.getLength());
    }

    public boolean isOpen() {
        java.util.Enumeration en = allEditors.getComponents ();
        return en.hasMoreElements ();
    }


    protected CloneableTopComponent createCloneableTopComponent () {
        // initializes the document if not initialized
        prepareDocument ();

        return myCreateJavaEditorComponent();
    }

    /** Method for creation of the java editor component
    * - accessible from the innerclass.
    */
    JavaEditorComponent myCreateJavaEditorComponent() {
        DataObject obj = findDataObject ();
        JavaEditorComponent editor = new JspServletEditorComponent(obj);

        // dock into editor mode if possible
        Workspace current = TopManager.getDefault().getWindowManager().getCurrentWorkspace();
        Mode editorMode = current.findMode(EDITOR_MODE);
        if (editorMode != null)
            editorMode.dockInto(editor);

        return editor;
    }


    public static class JspServletEditorComponent extends JavaEditor.JavaEditorComponent {

        /** The support, subclass of EditorSupport */
        JspServletEditor jspSupport;

        public JspServletEditorComponent() {
            super();
        }

        public JspServletEditorComponent(DataObject obj) {
            super(obj);
            init();
        }

        private void init() {
            pane.setEditable(false);
            jspSupport = (JspServletEditor)obj.getCookie(JspServletEditor.class);
        }

        /** Deserialize this top component.
        * @param in the stream to deserialize from
        */
        public void readExternal (ObjectInput in)
        throws IOException, ClassNotFoundException {
            super.readExternal(in);
            init();
        }

        protected CloneableTopComponent createClonedObject () {
            return jspSupport.myCreateJavaEditorComponent();
        }

        /* This method is called when parent window of this component has focus,
        * and this component is preferred one in it. This implementation adds 
        * performer to the ToggleBreakpointAction.
        */
        protected void componentActivated () {
            super.componentActivated ();
            ((ToggleBreakpointAction) SystemAction.get (ToggleBreakpointAction.class)).
            setActionPerformer (new ActionPerformer () {
                                    public void performAction (SystemAction a) {
                                        // breakpoints not allowed, do nothing
                                    }
                                });
        }

    } // JspServletEditorComponent
}

/*
 * Log
 *  4    Gandalf   1.3         1/17/00  Petr Jiricka    Setting a breakpoint 
 *       disabled.
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/10/99 Petr Jiricka    Read-only attempts
 *  1    Gandalf   1.0         9/22/99  Petr Jiricka    
 * $
 */
