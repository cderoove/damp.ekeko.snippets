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

package org.netbeans.modules.javadoc;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.nodes.Node;
import org.openide.TopManager;
import org.openide.cookies.CompilerCookie;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.windows.InputOutput;
import org.openide.util.RequestProcessor;
import org.openide.execution.ExecutionEngine;
import org.openide.execution.ExecutorTask;
import org.openide.TopManager;

import org.netbeans.modules.javadoc.settings.JavadocSettings;
import org.netbeans.modules.javadoc.settings.StdDocletSettings;
import org.netbeans.modules.java.JavaDataObject;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.io.*;
import java.lang.reflect.*;
import javax.swing.JFileChooser;

import com.sun.javadoc.*;
import sun.tools.util.ModifierFilter;
import sun.tools.java.ClassDeclaration;

/** Generate Javadoc - Tools action
 *
 * @author   Petr Hrebejk
 */
public class GenerateDocAction extends CookieAction {

    /** We have to hold refwerence on ExecutorTask in order not to los ioTab */
    private static ExecutorTask et = null;

    private static final ResourceBundle bundle = NbBundle.getBundle( GenerateDocAction.class );

    static final long serialVersionUID =-7617405431087800775L;
    /** Creates and starts a thread for generating documentation
     */
    protected void performAction(Node[] activatedNodes) {

        ExecutionEngine ee = TopManager.getDefault().getExecutionEngine();

        StdDocletSettings options= new StdDocletSettings();

        File dir = DestinationPanel.showDialog( options.getDirectory() );

        if ( dir != null ) {
            options.setDirectory( dir );
            JavadocInvoker ji = new JavadocInvoker( activatedNodes );
            TopManager.getDefault().setStatusText( bundle.getString( "MSG_StartingJavadoc" ) );

            et = ee.execute( "Javadoc generation", ji, JavadocInvoker.getIO() ); //NOI18N

            //RequestProcessor.postRequest( ji );
        }
        else {
            return;
        }
    }

    /** Cookie classes contains one class returned by cookie () method.
     */
    protected final Class[] cookieClasses () {
        return new Class[] { DataFolder.class, JavaDataObject.class };
    }

    /** If javadoc already running disable the actions.
     * Otherways let decide overriden method
     */
    protected boolean enable( Node[] activatedNodes ) {
        if (JavadocInvoker.isRunning())
            return false;
        else
            return super.enable (activatedNodes);
    }

    /** Get the requested cookie class.
     * @return the class, e.g. {@link CompilerCookie.Compile}
     */
    protected Class cookie () {
        return DataFolder.class;
    };

    /** All must be DataFolders or JavaDataObjects
     */
    protected int mode () {
        return MODE_ALL;
    }

    /* Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (GenerateDocAction.class);
    }

    /* Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        return bundle.getString("CTL_ActionGenerate");
    }

}

/*
 * Log
 */
