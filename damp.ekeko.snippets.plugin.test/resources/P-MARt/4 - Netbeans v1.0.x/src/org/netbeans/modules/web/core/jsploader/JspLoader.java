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

import java.util.*;
import java.io.IOException;
import java.net.URL;
import java.awt.BorderLayout;

import org.openide.TopManager;
import org.openide.actions.*;
import org.openide.awt.HtmlBrowser;
import org.openide.cookies.ViewCookie;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.execution.NbClassPath;
import org.openide.text.EditorSupport;
import org.openide.loaders.OpenSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.CloneableTopComponent;

import org.netbeans.modules.web.core.WebExecSupport;
import org.netbeans.modules.web.core.WebNode;
import org.netbeans.modules.web.core.QueryStringCookie;

import org.netbeans.modules.web.core.jswdk.Execute2Action;

/**
* Loader for JSPs.
*
* @author Petr Jiricka
*/
public class JspLoader extends UniFileLoader {
    /** serialVersionUID */
    private static final long serialVersionUID = 1549250022027438942L;

    static final String JSP_EXTENSION = "jsp"; // NOI18N
    public static final String JSP_MIME_TYPE  = "text/x-jsp"; // NOI18N

    protected void initialize () {
        super.initialize();
        setDisplayName(NbBundle.getBundle(JspLoader.class).
                       getString("PROP_JspLoader_Name"));
        ExtensionList ext = new ExtensionList();
        ext.addExtension(JSP_EXTENSION);
        setExtensions(ext);

        setActions (new SystemAction[] {
                        SystemAction.get (OpenAction.class),
                        SystemAction.get (EditServletAction.class),
                        SystemAction.get (FileSystemAction.class),
                        null,
                        SystemAction.get (ExecuteAction.class),
                        SystemAction.get (Execute2Action.class),
                        SystemAction.get (CompileAction.class),
                        null,
                        SystemAction.get (CutAction.class),
                        SystemAction.get (CopyAction.class),
                        SystemAction.get (PasteAction.class),
                        null,
                        SystemAction.get (DeleteAction.class),
                        SystemAction.get (RenameAction.class),
                        null,
                        SystemAction.get (SaveAsTemplateAction.class),
                        null,
                        SystemAction.get (ToolsAction.class),
                        SystemAction.get (PropertiesAction.class),
                    });

    }

    public JspLoader() {
        super (JspDataObject.class);
    }


    protected MultiDataObject createMultiObject (final FileObject primaryFile)
    throws DataObjectExistsException, IOException {

        java.io.File f = NbClassPath.toFile(primaryFile);
        boolean isFileBased = ((f != null) && f.exists());

        JspDataObject obj = new JspDataObject (primaryFile, this);
        EditorSupport es = new EditorSupport (obj.getPrimaryEntry ());
        es.setMIMEType (JSP_MIME_TYPE);
        obj.getCookieSet ().add (es);
        obj.getCookieSet ().add (new ViewCookie () {
                                     public void view () {
                                         try {
                                             TopManager.getDefault ().showUrl (primaryFile.getURL ());
                                         } catch (FileStateInvalidException e) {
                                         }
                                     }
                                 });

        if (isFileBased) {
            obj.getCookieSet ().add (new WebExecSupport(obj.getPrimaryEntry ()));
            obj.getCookieSet ().add (new QueryStringCookie() {});

            try {
                // check that we can conver this JSP to file
                JspCompileUtil.getFileObjectFileName(obj.getPrimaryFile());
                obj.getCookieSet ().add(new JspCompilerSupport.Compile(obj));
                obj.getCookieSet ().add(new JspCompilerSupport.Build(obj));
                obj.getCookieSet ().add(new JspCompilerSupport.Clean(obj));
            }
            catch (FileStateInvalidException e) {
                // compile cookies not added
            }
        }

        return obj;
    }
}

/*
* Log
*  12   Gandalf   1.11        2/4/00   Petr Jiricka    Fixed exception on 
*       compilation in Jar files
*  11   Gandalf   1.10        1/15/00  Petr Jiricka    Ensuring correct compiler
*       implementation - hashCode and equals
*  10   Gandalf   1.9         1/12/00  Petr Jiricka    i18n phase 1
*  9    Gandalf   1.8         1/4/00   Petr Jiricka    Removed ViewAction
*  8    Gandalf   1.7         12/29/99 Petr Jiricka    Various compilation fixes
*  7    Gandalf   1.6         12/4/99  Petr Jiricka    Changed extension 
*       handling. Might fix the bug with disappearing files.
*  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    Gandalf   1.4         10/8/99  Petr Jiricka    Adds QueryStringCookie to
*       the JSP page
*  4    Gandalf   1.3         10/1/99  Petr Jiricka    Changes in  initialize()
*  3    Gandalf   1.2         10/1/99  Jaroslav Tulach Loaders extends 
*       SharedClassObject
*  2    Gandalf   1.1         9/22/99  Petr Jiricka    
*  1    Gandalf   1.0         9/22/99  Petr Jiricka    
* $
*/
