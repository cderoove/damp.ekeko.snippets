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

package org.netbeans.modules.web.core;

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
import org.openide.text.EditorSupport;
import org.openide.loaders.OpenSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.CloneableTopComponent;


/**
* Loader for internet DataObjects, namely HTM, HTML, JHTML, SHTML, JSP.
*
* @author Petr Jiricka
*/
public class WebLoader extends UniFileLoader {

    public static final String JSP_MIME_TYPE  = "text/x-jsp"; // NOI18N
    public static final String HTML_MIME_TYPE = "text/html"; // NOI18N

    public WebLoader() {
        super (MultiDataObject.class);
    }

    protected void initialize() {
        super.initialize();
        setDisplayName(NbBundle.getBundle(WebLoader.class).
                       getString("PROP_WebLoader_Name"));
        getExtensions ().addExtension ("html"); // NOI18N
        getExtensions ().addExtension ("htm"); // NOI18N
        getExtensions ().addExtension ("shtml"); // NOI18N
        getExtensions ().addExtension ("jhtml"); // NOI18N
        //getExtensions ().addExtension ("jsp"); // NOI18N
        setActions (new SystemAction[] {
                        SystemAction.get (ViewAction.class),
                        SystemAction.get (OpenAction.class),
                        SystemAction.get (FileSystemAction.class),
                        null,
                        SystemAction.get (ExecuteAction.class),
                        SystemAction.get (ExecuteAction.class),
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


    protected MultiDataObject createMultiObject (final FileObject primaryFile)
    throws DataObjectExistsException, IOException {

        MultiDataObject obj = new WebDataObject (primaryFile, this);
        EditorSupport es = new EditorSupport (obj.getPrimaryEntry ());
        String ext = obj.getPrimaryFile().getExt();
        if (ext.equals("jsp")) // NOI18N
            es.setMIMEType (JSP_MIME_TYPE);
        else
            es.setMIMEType (HTML_MIME_TYPE);
        obj.getCookieSet ().add (es);
        obj.getCookieSet ().add (new ViewCookie () {
                                     public void view () {
                                         try {
                                             TopManager.getDefault ().showUrl (primaryFile.getURL ());
                                         } catch (FileStateInvalidException e) {
                                         }
                                     }
                                 });
        obj.getCookieSet ().add (new WebExecSupport(obj.getPrimaryEntry ()));
        return obj;
    }
}

/*
* Log
*  13   Gandalf   1.12        1/13/00  Petr Jiricka    More i18n
*  12   Gandalf   1.11        1/12/00  Petr Jiricka    i18n phase 1
*  11   Gandalf   1.10        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  10   Gandalf   1.9         10/1/99  Petr Jiricka    Changes in  initialize()
*  9    Gandalf   1.8         9/22/99  Petr Jiricka    JSP objects not loaded by
*       this loader
*  8    Gandalf   1.7         8/30/99  Pavel Buzek     
*  7    Gandalf   1.6         8/27/99  Petr Jiricka    
*  6    Gandalf   1.5         8/4/99   Petr Jiricka    
*  5    Gandalf   1.4         8/4/99   Petr Jiricka    Corrected content type 
*       for JSP files
*  4    Gandalf   1.3         7/24/99  Petr Jiricka    
*  3    Gandalf   1.2         7/3/99   Petr Jiricka    
*  2    Gandalf   1.1         7/3/99   Petr Jiricka    
*  1    Gandalf   1.0         6/30/99  Petr Jiricka    
* $
*/
