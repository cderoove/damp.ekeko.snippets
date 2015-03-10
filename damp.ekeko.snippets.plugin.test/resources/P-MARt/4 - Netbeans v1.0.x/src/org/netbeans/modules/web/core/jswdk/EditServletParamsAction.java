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

package org.netbeans.modules.web.core.jswdk;

import java.util.ResourceBundle;
import java.io.IOException;
import java.io.File;
import java.awt.Dialog;

import java.util.Properties;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map;
import java.util.TreeMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;
import java.net.MalformedURLException;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.execution.Executor;
import org.openide.execution.ExecInfo;
import org.openide.execution.NbClassPath;
import org.openide.debugger.DebuggerType;
import org.openide.cookies.ExecCookie;
import org.openide.nodes.Node;
import org.openide.loaders.ExecSupport;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.web.core.jsploader.JspDataObject;
import org.netbeans.modules.web.core.WebDataObject;
import org.netbeans.modules.web.core.WebExecSupport;
import org.netbeans.modules.web.core.WebExecInfo;
import org.netbeans.modules.web.core.jsploader.JspCompileUtil;

/**
*
* @author Petr Jiricka
*/
public class EditServletParamsAction extends CookieAction {

    /** extended attribute for servlet execution parameters */
    private static final String EA_SERVLET_PARAMS = "NetBeansAttrServletExecParams"; // NOI18N

    static final long serialVersionUID =5523492367349099493L;
    /** Actually performs the SwitchOn action.
    * @param activatedNodes Currently activated nodes.
    */
    public void performAction (final Node[] activatedNodes) {
        MultiDataObject dObj = (MultiDataObject)(activatedNodes[0]).getCookie(DataObject.class);

        ServletParamsPanel spPanel = new ServletParamsPanel(getServletExecParams(dObj));

        DialogDescriptor dd = new DialogDescriptor( spPanel,
                              NbBundle.getBundle(EditServletParamsAction.class).getString("CTL_TITLE_ServletParams"),     // Title
                              true,                                                 // Modal
                              NotifyDescriptor.OK_CANCEL_OPTION,                    // Option list
                              NotifyDescriptor.OK_OPTION,                           // Default
                              DialogDescriptor.BOTTOM_ALIGN,                        // Align
                              new HelpCtx (ServletParamsPanel.class), // Help
                              null );

        Dialog spDialog = TopManager.getDefault().createDialog( dd );
        spDialog.show ();

        if (dd.getValue().equals( NotifyDescriptor.OK_OPTION ) ) {
            try {
                setServletExecParams(dObj, spPanel.getServletExecParams());
            }
            catch (IOException e) {
                TopManager.getDefault().notifyException(e);
            }
        }
    }

    /**
    * Returns MODE_EXACTLY_ONE.
    */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    /** Adds test of executor for JavaDataObjects */
    protected boolean enable (Node[] activatedNodes) {
        if (super.enable(activatedNodes)) {
            Node.Cookie c = (activatedNodes[0]).getCookie(ServletParamsCookie.class);
            if (c != null)
                return true;
            DataObject dObj = (DataObject)(activatedNodes[0]).getCookie(DataObject.class);
            if (dObj instanceof MultiDataObject && /* PENDING - very UGLY */
                    !(dObj instanceof JspDataObject) && !(dObj instanceof WebDataObject)) {
                Executor exec = ExecSupport.getExecutor(((MultiDataObject)dObj).getPrimaryEntry());
                if (exec == null) {
                    WebExecSupport wes = (WebExecSupport)dObj.getCookie(WebExecSupport.class);
                    if (wes != null)
                        exec = wes.defaultExecutor();
                }
                if ((exec != null) && (exec instanceof ServletParamsCookie)) return true;
                DebuggerType debug = ExecSupport.getDebuggerType(((MultiDataObject)dObj).getPrimaryEntry());
                if (debug == null) {
                    WebExecSupport wes = (WebExecSupport)dObj.getCookie(WebExecSupport.class);
                    if (wes != null)
                        debug = wes.defaultDebuggerType();
                }
                if ((debug != null) && (debug instanceof ServletParamsCookie)) return true;
                return false;
            }
            else
                return false;
        }
        else
            return false;
    }

    /**
    * Returns ThreadCookie
    */
    protected Class[] cookieClasses () {
        return new Class [] {
                   DataObject.class
               };
    }

    /** @return the action's icon */
    public String getName() {
        return NbBundle.getBundle (EditServletParamsAction.class).getString ("LBL_EditServletParams");
    }

    /** @return the action's help context */
    // PENDING - helpctx
    public HelpCtx getHelpCtx() {
        return null;
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "/org/netbeans/modules/web/core/resources/EditServletParams.gif"; // NOI18N
    }

    /** Set servlet execution parameters for a given entry.
    * @param entry the entry
    * @param args array of arguments
    * @exception IOException if arguments cannot be set
    */
    public static void setServletExecParams (MultiDataObject dObj, ServletExecParams args) throws IOException {
        MultiDataObject.Entry entry = dObj.getPrimaryEntry();
        entry.getFile ().setAttribute (EA_SERVLET_PARAMS, args);
        // PENDING - set in ordinary params
        setInServletProperties(args, dObj);
        setInMappingProperties(args);
    }

    /** Get the arguments associated with a given entry.
    * @param entry the entry
    * @return the arguments, or an empty array if no arguments are specified
    */
    public static ServletExecParams getServletExecParams(MultiDataObject dObj) {
        MultiDataObject.Entry entry = dObj.getPrimaryEntry();
        ServletExecParams args = null;
        try {
            args = (ServletExecParams)entry.getFile ().getAttribute (EA_SERVLET_PARAMS);
        } catch (Exception ex) {
            // null pointer or IOException
        }
        if (args == null)
            args = new ServletExecParams(dObj);

        // propagate ordinary params into servlet params
        // PENDING
        setInServletProperties(args, dObj);
        setInMappingProperties(args);

        return args;
    }

    /** Sets in the servlets.properties file in the system directory, not in the context's dir */
    private static void setInServletProperties(ServletExecParams args, DataObject dObj) {
        String resName = "/servlet/servlets.properties"; // NOI18N
        FileSystem fs = TopManager.getDefault().getRepository().getDefaultFileSystem();
        Properties props = PropertiesUtil.loadProperties(fs, resName);

        if (!"".equals(args.getName())) { // NOI18N
            // code property
            props.put(args.getName() + ".code", dObj.getPrimaryFile().getPackageName('.')); // NOI18N

            // initparams property
            StringBuffer params = new StringBuffer ();
            Vector rows = args.getInitParams();
            for (int i = 0; i < rows.size(); i++) {
                Vector oneRow = (Vector)rows.elementAt(i);
                String param = (String)oneRow.elementAt(0);
                String val = (String)oneRow.elementAt(1);
                // pending escape it
                params.append(param + "=" + val); // NOI18N
                if (i < rows.size() - 1)
                    params.append(","); // NOI18N
            }
            props.put(args.getName() + ".initparams", params.toString()); // NOI18N
        }

        try {
            PropertiesUtil.saveProperties(props, fs, resName);
        }
        catch (IOException e) {
            TopManager.getDefault().notifyException(e);
        }
    }


    /** Sets in the mappings.properties file in the system directory, not in the context's dir */
    private static void setInMappingProperties(ServletExecParams args) {
        String resName = "/servlet/mappings.properties"; // NOI18N
        FileSystem fs = TopManager.getDefault().getRepository().getDefaultFileSystem();
        Properties props = PropertiesUtil.loadProperties(fs, resName);

        // add or remove the mapping
        if ("".equals(args.getName())) // NOI18N
            props.remove(args.getMapping());
        else
            if (!"".equals(args.getMapping())) // NOI18N
                props.put(args.getMapping(), args.getName());

        try {
            PropertiesUtil.saveProperties(props, fs, resName);
        }
        catch (IOException e) {
            TopManager.getDefault().notifyException(e);
        }
    }

    /** Puts the properties files to the WEB-INF directory so the context can pick them up
    *  Used by both executor and debugger, so it's declared here.
    *  PENDING : restriction - right now the contextRoot must be a root of a filesystem
    */
    public static void deployWebAppDescriptor(FileObject contextRoot,
            Map mimeMap, String welcomeFiles, boolean invoker) throws IOException {

        FileSystem defaultFs = TopManager.getDefault().getRepository().getDefaultFileSystem();
        FileSystem targetFs = contextRoot.getFileSystem();
        Properties props;

        // create servlets.properties
        props = PropertiesUtil.loadProperties(defaultFs, "/servlet/servlets.properties"); // NOI18N
        File contRoot = NbClassPath.toFile(JspCompileUtil.getContextOutputRoot(contextRoot));
        props.setProperty("jsp.code", "com.sun.jsp.runtime.JspServlet"); // NOI18N
        props.setProperty("jsp.initparams", (contRoot == null) ? null : ("scratchdir=" + contRoot.getAbsolutePath())); // NOI18N
        PropertiesUtil.saveProperties(props, targetFs, "/WEB-INF/servlets.properties"); // NOI18N

        // create mappings.properties
        props = PropertiesUtil.loadProperties(defaultFs, "/servlet/mappings.properties"); // NOI18N
        props.setProperty(".jsp", "jsp"); // NOI18N
        PropertiesUtil.saveProperties(props, targetFs, "/WEB-INF/mappings.properties"); // NOI18N

        // create mime.properties
        props = map2Properties(mimeMap);
        PropertiesUtil.saveProperties(props, targetFs, "/WEB-INF/mime.properties"); // NOI18N

        // create webapp.properties
        props = new Properties();
        props.setProperty("welcomefiles", welcomeFiles); // NOI18N
        props.setProperty("invoker", "" + invoker); // NOI18N
        PropertiesUtil.saveProperties(props, targetFs, "/WEB-INF/webapp.properties"); // NOI18N
    }

    // PENDING - context root path
    static URL constructServletURL(DataObject servlet, int port, boolean invoker) throws MalformedURLException, IOException {
        ServletExecParams sep = getServletExecParams((MultiDataObject)servlet);
        String queryString = sep.getQueryString();

        if ((queryString.length() > 0) && (!queryString.startsWith("?"))) // NOI18N
            queryString = "?" + queryString; // NOI18N

        String mapping = sep.getMapping();
        if (mapping.equals("") || mapping.startsWith(".")) { // NOI18N
            if (invoker)
                mapping = "/servlet/" + servlet.getPrimaryFile().getPackageName('.'); // NOI18N
            else
                throw new IOException(NbBundle.getBundle(EditServletParamsAction.class).getString("EXC_NoSuitableMapping"));
        }

        URL url = new URL("http", WebExecSupport.getLocalHost(), port, mapping + queryString); // NOI18N
        return url;
    }

    // PENDING - context root path
    static URL constructJspURL(DataObject jspDo, int port) throws MalformedURLException, IOException {
        String queryString = WebExecSupport.getQueryString(jspDo.getPrimaryFile());

        if ((queryString.length() > 0) && (!queryString.startsWith("?"))) // NOI18N
            queryString = "?" + queryString; // NOI18N

        URL url = new URL("http", WebExecSupport.getLocalHost(), port, "/" + // NOI18N
                          jspDo.getPrimaryFile().getPackageNameExt('/', '.') + queryString);
        return url;
    }

    static Map properties2Map(Properties properties) {
        Map map = new TreeMap();
        for (Enumeration e = properties.keys(); e.hasMoreElements(); ) {
            String prop = (String)e.nextElement();
            map.put(prop, properties.getProperty(prop));
        }
        return map;
    }

    static Properties map2Properties(Map map) {
        Properties properties = new Properties();
        for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
            String key = (String)it.next();
            properties.setProperty(key, (String)map.get(key));
        }
        return properties;
    }

    static Map getDefaultMimeMap() {
        Properties props = new Properties();
        try {
            props.load(EditServletParamsAction.class.getResourceAsStream("/org/netbeans/modules/web/core/resources/DefaultMimeTypes.properties")); // NOI18N
        } catch (IOException e) {}
        return EditServletParamsAction.properties2Map(props);
    }

    /** Resolves document root. Returns null if the root is invalid, otherwise returns the document root.
    * The rules are as follows: For servlets, if a non-default root is specified, it is used. Otherwise 
    * the servlet's filesystem's root is used, but only if this is a java.io.File-based filesystem. 
    * For JSPs, if no 
    */
    private static FileObject getDocRoot(ExecInfo info, DataObject executedObj,
                                         FileSystem userRootFs, boolean isServlet) throws IOException {
        FileObject fo = executedObj.getPrimaryFile().getFileSystem().getRoot();
        File f = NbClassPath.toFile(fo);
        boolean residesOnOkFS = (f != null && f.isDirectory());

        if (isServlet) {
            if (userRootFs != null)
                return userRootFs.getRoot();
            if (residesOnOkFS) return fo;
            else throw new IOException(NbBundle.getBundle(EditServletParamsAction.class).
                                           getString("EXC_RootError1"));
        }
        else {
            if (userRootFs == null) {
                if (residesOnOkFS) return fo;
                else throw new IOException(NbBundle.getBundle(EditServletParamsAction.class).
                                               getString("EXC_RootError2"));
            }
            if (userRootFs.getRoot().equals(fo))
                return fo;
            throw new IOException(NbBundle.getBundle(EditServletParamsAction.class).
                                  getString("EXC_RootError3"));
        }
    }

    /** Never returns null */
    static DataObject findDataObject(ExecInfo info) throws IOException {
        if (!isServlet(info)) {
            return ((WebExecInfo)info).getDataObject();
        }
        else {
            String servlet = info.getClassName();
            DataObject dObj = WebExecSupport.classToDataObject(servlet);
            if (dObj == null)
                throw new IOException(org.openide.util.NbBundle.getBundle(EditServletParamsAction.class).getString("CTL_BadCompilerType"));
            return dObj;
        }
    }

    static boolean isServlet(ExecInfo info) {
        return !(info instanceof WebExecInfo); // pending - should check whether it actually is a servlet
    }

    static boolean isJsp(ExecInfo info) {
        return (info instanceof WebExecInfo);
    }

    static FileObject resolveDocRoot(ExecInfo info, FileSystem userRootFs) throws IOException {
        return getDocRoot(info, findDataObject(info), userRootFs, isServlet(info));
    }

    static String getWorkDir(FileObject docRoot) throws IOException {
        return NbClassPath.toFile(JspCompileUtil.getContextOutputRoot(docRoot)).
               getAbsolutePath();
    }


}

/*
 * Log
 *  19   Gandalf   1.18        1/27/00  Petr Jiricka    WEB-INF capitalized
 *  18   Gandalf   1.17        1/17/00  Petr Jiricka    WebExecSupport - related
 *       changes.
 *  17   Gandalf   1.16        1/16/00  Petr Jiricka    isJsp() method
 *  16   Gandalf   1.15        1/12/00  Petr Jiricka    Fully I18n-ed
 *  15   Gandalf   1.14        1/12/00  Petr Jiricka    i18n phase 1
 *  14   Gandalf   1.13        1/3/00   Petr Jiricka    Changed RootFileObject 
 *       to RootFileSystem
 *  13   Gandalf   1.12        12/21/99 Petr Jiricka    getDocRoot parameter 
 *       changed DataObject -> FileObject
 *  12   Gandalf   1.11        12/20/99 Petr Jiricka    Checking in changes made
 *       in the U.S.
 *  11   Gandalf   1.10        11/27/99 Patrik Knakal   
 *  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         10/12/99 Petr Jiricka    Removed debug messages
 *  8    Gandalf   1.7         10/12/99 Petr Jiricka    JSPServlet scratch dir 
 *       changed
 *  7    Gandalf   1.6         10/10/99 Petr Jiricka    Compilation changes
 *  6    Gandalf   1.5         10/9/99  Petr Jiricka    More static methods
 *  5    Gandalf   1.4         10/8/99  Petr Jiricka    Static methods added
 *  4    Gandalf   1.3         10/8/99  Petr Jiricka    
 *  3    Gandalf   1.2         10/7/99  Petr Jiricka    
 *  2    Gandalf   1.1         10/7/99  Petr Jiricka    
 *  1    Gandalf   1.0         10/7/99  Petr Jiricka    
 * $
 */
