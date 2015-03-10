/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.MessageFormat;
import java.util.*;

import org.openide.cookies.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.actions.OpenAction;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.rmi.settings.*;
import org.netbeans.modules.java.JavaDataObject;

/** Class representing RMI object.
*
* @author Martin Ryzl, Dafe Simonek
*/
public class RMIDataObject extends JavaDataObject {

    /** Serial version UID. */
    static final long serialVersionUID = -8035788991669336965L;

    public static final String EA_PORT = "NetBeansAttrPort"; // NOI18N
    public static final String EA_SERVICE = "NetBeansAttrService"; // NOI18N
    public static final String EA_REGISTRY_PORT = "NetBeansAttrRegistryPort"; // NOI18N

    /** New instance.
    * @param pf primary file object for this data object
    */
    public RMIDataObject(FileObject pf, MultiFileLoader loader)
    throws DataObjectExistsException {
        super(pf, loader);
        init();
    }

    private void readObject(ObjectInputStream in)
    throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init();
    }

    private void init() {
        CookieSet cookies = getCookieSet();

        // replace exec cookie
        Node.Cookie es = getCookie(ExecSupport.class);
        cookies.remove(es);
        es = new RMIExecSupport(getPrimaryEntry());
        cookies.add(es);

        removeCookie(cookies, CompilerCookie.class);
        removeCookie(cookies, CompilerCookie.Compile.class);
        removeCookie(cookies, CompilerCookie.Clean.class);
        removeCookie(cookies, CompilerCookie.Build.class);

        // replace all compile cookies
        cookies.add(new RMICompilerSupport.Compile(getPrimaryEntry()));
        cookies.add(new RMICompilerSupport.Clean(getPrimaryEntry()));
        cookies.add(new RMICompilerSupport.Build(getPrimaryEntry()));
    }

    private static void removeCookie(CookieSet set, Class clazz) {
        Object cookie = set.getCookie(clazz);
        if (cookie != null) {
            set.remove((Node.Cookie)cookie);
        }
    }

    /** Help context for this object.
    * @return help context
    */
    public org.openide.util.HelpCtx getHelpCtx () {
        return org.openide.util.HelpCtx.DEFAULT_HELP;
    }

    /** Provides node that should represent this data object. When a node for representation
    * in a parent is requested by a call to getNode (parent) it is the exact copy of this node
    * with only parent changed. This implementation creates instance
    * <CODE>DataNode</CODE>.
    * <P>
    * This method is called only once.
    *
    * @return the node representation for this data object
    * @see DataNode
    */
    protected Node createNodeDelegate () {

        RMINode node = new RMINode (this);
        node.setDefaultAction (SystemAction.get (OpenAction.class));
        return node;
    }

    /** Getter for port.
    * @return - port
    */
    public int getPort() {
        try {
            Integer port = (Integer) getPrimaryEntry().getFile ().getAttribute (RMIDataObject.EA_PORT);
            if (port != null) {
                return port.intValue();
            }
        } catch (Exception ex) {
            // null pointer or IOException
        }
        return 0;
    }

    /** Setter for port.
    * @param port port
    */
    public void setPort(int port ) {
        try {
            getPrimaryEntry().getFile ().setAttribute (EA_PORT, new Integer(port));
        } catch (java.io.IOException ex) {
        }
    }

    /** Getter for service.
     * @return - name of the service
    */
    public String getService() {
        try {
            String service = (String) getPrimaryEntry().getFile ().getAttribute (RMIDataObject.EA_SERVICE);
            if (service != null) {
                return service;
            }
        } catch (Exception ex) {
            // null pointer or IOException
        }
        return null;
    }

    /** Setter for service.
    * @param service the service
    */
    public void setService(String service) {
        try {
            getPrimaryEntry().getFile ().setAttribute (EA_SERVICE, service);
        } catch (java.io.IOException ex) {
        }
    }

    /** Cancel stub entries.
    */
    public void dropStubs() {
        boolean change = false;
        Set entries = secondaryEntries();
        Iterator it = entries.iterator();
        while (it.hasNext()) {
            MultiDataObject.Entry entry = (MultiDataObject.Entry) it.next();
            // check if is it stub
            String name = entry.getFile().getName();
            int index = name.indexOf(RMIDataLoader.INNER_CLASS_DIVIDER);
            if (index != -1) name = name.substring(0, index);
            if (RMIDataLoader.checkStub(name) != null) {
                removeSecondaryEntry(entry);
                reusableDispose();
            }
        }
    }

    /** Find stubs and cancel their original DataObjects.
    */
    public void aquireStubs() {
        FileObject parent = getPrimaryFile().getParent();
        FileObject clfo, jafo;
        Object[] objs = new Object[1];
        objs[0] = getName();

        RMISettings settings = (RMISettings) RMISettings.findObject(RMISettings.class, true);
        String[] stubFormats = settings.getStubFormats();
        for(int i = 0; i < stubFormats.length; i++) {
            String name = MessageFormat.format(stubFormats[i], objs);
            dropDataObject(parent.getFileObject(name, RMIDataLoader.CLASS_EXTENSION));
            dropDataObject(parent.getFileObject(name, RMIDataLoader.JAVA_EXTENSION));
        }
    }

    /**
    */
    private void dropDataObject(FileObject fo) {
        if (fo == null) return;
        try {
            DataObject dobj = DataObject.find(fo);
            if (dobj != this) dobj.setValid(false);
        } catch (Exception ex) {
            // ignored
        }
    }

    /**
    */
    protected void dispose() {
        super.dispose();
    }

    /** Disposes the object but the same object will be recognized on consequent
    * data loading.
    */
    protected void reusableDispose() {
        final FileObject fo = getPrimaryFile();
        try {
            fo.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                                                   public void run() {
                                                       RMIDataLoader.reusableSet.put(fo, RMIDataObject.this);
                                                       dispose();
                                                   }
                                               }
                                              );
        } catch (IOException ex) {
            // ignore ...
        }
    }

}

/*
 * <<Log>>
 *  29   Gandalf-post-FCS1.24.1.3    3/20/00  Martin Ryzl     localization
 *  28   Gandalf-post-FCS1.24.1.2    3/16/00  Martin Ryzl     calling of dispose() 
 *       fixed
 *  27   Gandalf-post-FCS1.24.1.1    3/16/00  Martin Ryzl     calling of dispose() 
 *       fixed
 *  26   Gandalf-post-FCS1.24.1.0    3/8/00   Martin Ryzl     hide stubs feature
 *  25   src-jtulach1.24        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  24   src-jtulach1.23        10/7/99  Martin Ryzl     
 *  23   src-jtulach1.22        10/7/99  Martin Ryzl     removed settings
 *  22   src-jtulach1.21        10/6/99  Martin Ryzl     compiler type support
 *  21   src-jtulach1.20        8/16/99  Martin Ryzl     method filter in RMI 
 *       Encapsulation Wizard  service URL in RMIDataObject
 *  20   src-jtulach1.19        8/16/99  Martin Ryzl     debug prints were 
 *       removed
 *  19   src-jtulach1.18        7/12/99  Martin Ryzl     large changes  
 *  18   src-jtulach1.17        6/22/99  Ian Formanek    employed DEFAULT_HELP
 *  17   src-jtulach1.16        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  16   src-jtulach1.15        4/23/99  Martin Ryzl     debug info removed
 *  15   src-jtulach1.14        4/21/99  Martin Ryzl     
 *  14   src-jtulach1.13        4/20/99  Martin Ryzl     
 *  13   src-jtulach1.12        4/15/99  Martin Ryzl     
 *  12   src-jtulach1.11        3/29/99  Ian Formanek    removed import of 
 *       modules.compiler
 *  11   src-jtulach1.10        3/23/99  Martin Ryzl     
 *  10   src-jtulach1.9         3/19/99  Martin Ryzl     
 *  9    src-jtulach1.8         3/19/99  Ales Novak      
 *  8    src-jtulach1.7         3/19/99  Karel Gardas    
 *  7    src-jtulach1.6         3/18/99  Karel Gardas    
 *  6    src-jtulach1.5         3/18/99  Karel Gardas    
 *  5    src-jtulach1.4         3/18/99  Karel Gardas    
 *  4    src-jtulach1.3         3/18/99  Karel Gardas    
 *  3    src-jtulach1.2         3/18/99  Karel Gardas    
 *  2    src-jtulach1.1         3/18/99  Karel Gardas    
 *  1    src-jtulach1.0         3/17/99  David Simonek   
 * $
 */





















