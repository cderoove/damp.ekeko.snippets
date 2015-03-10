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

import java.io.*;
import java.beans.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import javax.swing.SwingUtilities;

import org.openide.*;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.loaders.DataNode;
import org.openide.loaders.CompilerSupport;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.src.*;
import org.openide.actions.OpenAction;
import org.openide.src.nodes.SourceChildren;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.execution.Executor;
import org.openide.debugger.DebuggerType;

import org.netbeans.modules.web.core.WebExecSupport;
import org.netbeans.modules.web.core.WebNode;

/** The node representation of <code>JspDataObject</code> for internet files.
*
* @author Petr Jiricka
*/
public class JspNode extends DataNode {

    private static final String EXECUTION_SET_NAME = "Execution"; // NOI18N

    /** extended attribute for attributes */
    private static final String EA_REQPARAMS = WebExecSupport.EA_REQPARAMS;

    private static final String ICON_BASE = "org/netbeans/modules/web/core/resources/jspObject"; // NOI18N

    public static final String PROP_REQUEST_PARAMS   = "requestparams"; // NOI18N

    /** Create a node for the internet data object using the default children.
    * @param jdo the data object to represent
    */
    public JspNode (JspDataObject jdo) {
        super(jdo, Children.LEAF);
        initialize();
    }

    private void initialize () {
        setIconBase(ICON_BASE);
        setDefaultAction (SystemAction.get (OpenAction.class));
    }


    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
        is.defaultReadObject();
        initialize();
    }

    public DataObject getDataObject() {
        return super.getDataObject();
    }

    /** Create the property sheet.
    * Subclasses may want to override this and add additional properties.
    * @return the sheet
    */
    protected Sheet createSheet () {
        Sheet.Set ps;

        Sheet sheet = super.createSheet();

        ps = new Sheet.Set ();
        ps.setName(EXECUTION_SET_NAME);
        ps.setDisplayName(NbBundle.getBundle(WebNode.class).getString("PROP_executionSetName"));
        ps.setShortDescription(NbBundle.getBundle(WebNode.class).getString("HINT_executionSetName"));

        ps.put(new PropertySupport.ReadWrite (
                   PROP_REQUEST_PARAMS,
                   String.class,
                   NbBundle.getBundle(WebNode.class).getString("PROP_requestParams"),
                   NbBundle.getBundle(WebNode.class).getString("HINT_requestParams")
               ) {
                   public Object getValue() {
                       return getRequestParams(((MultiDataObject)getDataObject()).getPrimaryEntry());
                   }
                   public void setValue (Object val) throws InvocationTargetException {
                       if (val instanceof String) {
                           try {
                               setRequestParams(((MultiDataObject)getDataObject()).getPrimaryEntry(), (String)val);
                           } catch(IOException e) {
                               throw new InvocationTargetException (e);
                           }
                       }
                       else {
                           throw new IllegalArgumentException();
                       }
                   }
               }
              );

        // add execution/debugger properties
        WebExecSupport wes = (WebExecSupport)getDataObject().getCookie(WebExecSupport.class);
        if (wes != null)
            wes.addProperties(ps);

        // remove the params property
        //ps.remove(ExecSupport.PROP_FILE_PARAMS);
        // remove the debugger type property
        //ps.remove(ExecSupport.PROP_DEBUGGER_TYPE);

        // add compilation properties
        JspCompilerSupport.Compile comp = (JspCompilerSupport.Compile)getDataObject().getCookie(JspCompilerSupport.Compile.class);
        if (comp != null) comp.addProperties(ps);

        // change the display name and hint
        Node.Property compProp = ps.get(JspCompilerSupport.PROP_COMPILER_TYPE);
        if (compProp != null) {
            compProp.setDisplayName(NbBundle.getBundle(JspNode.class).getString("CTL_ServletCompilerPropertyName"));
            compProp.setShortDescription(NbBundle.getBundle(JspNode.class).getString("CTL_ServletCompilerPropertyHint"));
        }

        sheet.put(ps);

        return sheet;
    }


    /** Set request parameters for a given entry.
    * @param entry the entry
    * @param args array of arguments
    * @exception IOException if arguments cannot be set
    */
    static void setRequestParams(MultiDataObject.Entry entry, String params) throws IOException {
        WebExecSupport.setQueryString(entry.getFile (), params);
    }

    /** Get the request parameters associated with a given entry.
    * @param entry the entry
    * @return the arguments, or an empty string if no arguments are specified
    */
    static String getRequestParams(MultiDataObject.Entry entry) {
        return WebExecSupport.getQueryString(entry.getFile ());
    }

    /** Get the icon base.
    * This should be a resource path, e.g. <code>/some/path/</code>,
    * where icons are held. Subclasses may override this.
    * @return the icon base
    * @see #getIcons
    */
    protected String getIconBase() {
        return ICON_BASE;
    }

}

/*
 * Log
 */
