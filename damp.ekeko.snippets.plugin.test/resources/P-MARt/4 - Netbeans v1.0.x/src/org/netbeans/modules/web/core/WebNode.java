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
import org.openide.loaders.ExecSupport;
import org.openide.loaders.MultiDataObject;
import org.openide.src.*;
import org.openide.actions.OpenAction;
import org.openide.src.nodes.SourceChildren;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.execution.Executor;
import org.openide.debugger.DebuggerType;

/** The node representation of <code>WebDataObject</code> for internet files.
*
* @author Petr Jiricka
*/
public class WebNode extends DataNode {

    /** generated Serialized Version UID */
    //static final long serialVersionUID = -7396485743899766258L;

    private static final String EXECUTION_SET_NAME = "Execution"; // NOI18N

    /** extended attribute for attributes */
    private static final String EA_REQPARAMS = WebExecSupport.EA_REQPARAMS;

    private static final String ICON_BASE = "org/netbeans/modules/web/core/resources/"; // NOI18N

    private static final String[] ICONS = {
        "htmlObject", "shtmlObject", "jhtmlObject", "jspObject", "wmObject" // NOI18N
    };

    private static final byte ICON_HTML  = 0;
    private static final byte ICON_SHTML = 1;
    private static final byte ICON_JHTML = 2;
    private static final byte ICON_JSP   = 3;
    private static final byte ICON_WM    = 4;

    public static final String PROP_REQUEST_PARAMS   = "requestparams"; // NOI18N

    private byte currentIcon;

    /** Create a node for the internet data object using the default children.
    * @param jdo the data object to represent
    */
    public WebNode (WebDataObject wdo) {
        super(wdo, Children.LEAF);
        initialize();
    }


    /** Gets an icon identifier on the basis of the primary file extension. */
    private byte getIconId () {
        String ext = getDataObject().getPrimaryFile().getExt();
        if (ext.equals("html") || ext.equals("htm")) // NOI18N
            return 0;
        if (ext.equals("shtml")) // NOI18N
            return 1;
        if (ext.equals("jhtml")) // NOI18N
            return 2;
        if (ext.equals("jsp")) // NOI18N
            return 3;
        if (ext.equals("wm")) // NOI18N
            return 4;
        return 0;
    }

    private void initialize () {
        currentIcon = getIconId ();
        setIconBase(getIconBase() + getIcons()[currentIcon]);
        setDefaultAction (SystemAction.get (OpenAction.class /*ViewAction.class*/));
    }


    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
        is.defaultReadObject();
        initialize();
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

        //    ExecSupport.addProperties(ps, ((MultiDataObject)getDataObject()).getPrimaryEntry());

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
        ps.put(new PropertySupport.ReadWrite (
                   ExecSupport.PROP_EXECUTION,
                   Executor.class,
                   NbBundle.getBundle(WebNode.class).getString("PROP_execution"),
                   NbBundle.getBundle(WebNode.class).getString("HINT_execution")
               ) {
                   public Object getValue() {
                       return ExecSupport.getExecutor(((MultiDataObject)getDataObject()).getPrimaryEntry());
                   }
                   public void setValue (Object val) throws InvocationTargetException {
                       try {
                           ExecSupport.setExecutor(((MultiDataObject)getDataObject()).getPrimaryEntry(), (Executor) val);
                       } catch (IOException ex) {
                           throw new InvocationTargetException (ex);
                       }
                   }
               }
              );

        //
        // debugger type
        /*    ps.put(new PropertySupport.ReadWrite (
                ExecSupport.PROP_DEBUGGER_TYPE,
                DebuggerType.class,
                NbBundle.getBundle(WebNode.class).getString("PROP_debuggerType"),
                NbBundle.getBundle(WebNode.class).getString("HINT_debuggerType")
              ) {
                public Object getValue() {
                  return ExecSupport.getDebuggerType (((MultiDataObject)getDataObject()).getPrimaryEntry());
                }
                public void setValue (Object val) throws InvocationTargetException {
                  try {
                    ExecSupport.setDebuggerType (((MultiDataObject)getDataObject()).getPrimaryEntry(), (DebuggerType) val);
                  } catch (IOException ex) {
                    throw new InvocationTargetException (ex);
                  }
                }
              }
            );    */
        // debugger - deferred to next version

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

    /** Get the icons.
    * This should be a list of bare icon names (i.e. no extension or path) in the icon base.
    * It should contain five icons in order for:
    * <ul>
    * <li>html file
    * <li>shtml file
    * <li>jhtml file
    * <li>jsp file
    * <li>wm file
    * </ul>
    * Subclasses may override this.
    * @return the icons
    * @see #getIconBase
    */
    protected String[] getIcons() {
        return ICONS;
    }


}

/*
 * Log
 */
