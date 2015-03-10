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

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

import org.openide.*;
import org.openide.loaders.DataNode;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.*;
import org.openide.util.HelpCtx;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.src.*;
import org.openide.src.nodes.SourceChildren;

/** The node representation of RMIDataObject.
*
* @author Martin Ryzl
*/
public class RMINode extends org.netbeans.modules.java.JavaNode {

    /** Serial version UID. */
    static final long serialVersionUID = -1396485743899766258L;

    // static ........................................................................................................

    private static final String EXECUTION_SET_NAME = "Execution"; // NOI18N
    private static final String SOURCE_SET_NAME = "Source"; // NOI18N

    private static final String ICON_BASE = "org/netbeans/modules/rmi/resources/"; // NOI18N
    private static final String[] ICONS = {"rmi", "rmiMain", "rmiError", "bean", "beanMain"}; // NOI18N
    private static final String ICON_RMI = "rmi"; // NOI18N
    private static final String ICON_RMI_MAIN = "rmiMain"; // NOI18N
    private static final String ICON_RMI_ERROR = "rmiError"; // NOI18N
    private static final String ICON_BEAN = "bean"; // NOI18N
    private static final String ICON_BEAN_MAIN = "beanMain"; // NOI18N

    // init ........................................................................................................

    /** Get localized string.
    * @param key
    * @return the localized string.
    */
    private String getString0(String key) {
        return NbBundle.getBundle(RMINode.class).getString(key);
    }

    /** Constructs a new RMIDataObject for specified primary file.
    */
    public RMINode (RMIDataObject rdo) {
        super (rdo);
        initialize();
    }

    /** Get path of the folder with icons.
    * @return a path
    */
    protected String getIconBase() {
        return ICON_BASE;
    }

    /** Get list of possible icons.
    * @return an array of icon names.
    */
    protected String[] getIcons() {
        return ICONS;
    }

    /** Initialize the object.
    */
    private void initialize () {
        setIconBase(ICON_BASE + ICON_RMI);
    }

    /**
    */
    private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
        is.defaultReadObject();
    }

    /** Create a property sheet.
    * @return properly initialized property sheet.
    */
    protected Sheet createSheet() {
        Sheet s = super.createSheet();
        Sheet.Set ss = new Sheet.Set();

        ss.setName(getString0("PROP_RMIExport")); // NOI18N
        s.put(ss);

        Node.Property p;

        RMIDataObject obj = (RMIDataObject) getCookie(RMIDataObject.class);

        if (obj == null) return s;

        try {
            p = new PropertySupport.Reflection (obj, Integer.TYPE, "getPort", "setPort"); // NOI18N
            p.setName("port"); // NOI18N
            p.setDisplayName(getString0("PROP_port")); // NOI18N
            p.setShortDescription(getString0("HINT_port")); // NOI18N
            ss.put(p);

            p = new PropertySupport.Reflection (obj, String.class, "getService", "setService"); // NOI18N
            p.setName("service"); // NOI18N
            p.setDisplayName(getString0("PROP_service_name")); // NOI18N
            p.setShortDescription(getString0("HINT_service_name")); // NOI18N
            ss.put(p);

        } catch (Exception ex) {
            throw new InternalError();
        }

        if ((ss = s.get(EXECUTION_SET_NAME)) != null) {
            RMICompilerSupport supp = (RMICompilerSupport) obj.getCookie (RMICompilerSupport.class);
            if (supp != null) RMICompilerSupport.addProperties (ss, supp);
        }

        return s;
    }

    // other methods ........................................................................................

    /** Support function, because getDataObject is protected and not accessable from
    * "this.innerclasses"
    */
    protected RMIDataObject getRMIDataObject() {
        return (RMIDataObject) getDataObject();
    }
}

/*
 * <<Log>>
 *  13   Gandalf-post-FCS1.11.1.0    3/20/00  Martin Ryzl     localization
 *  12   src-jtulach1.11        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   src-jtulach1.10        10/6/99  Martin Ryzl     compiler type support
 *  10   src-jtulach1.9         8/18/99  Martin Ryzl     corrected localization
 *  9    src-jtulach1.8         8/16/99  Martin Ryzl     
 *  8    src-jtulach1.7         7/12/99  Martin Ryzl     large changes  
 *  7    src-jtulach1.6         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    src-jtulach1.5         5/10/99  Jaroslav Tulach DataNode.canRename
 *  5    src-jtulach1.4         4/15/99  Martin Ryzl     
 *  4    src-jtulach1.3         4/15/99  Martin Ryzl     
 *  3    src-jtulach1.2         3/23/99  Martin Ryzl     
 *  2    src-jtulach1.1         3/19/99  Martin Ryzl     
 *  1    src-jtulach1.0         3/19/99  Martin Ryzl     
 * $
 */














