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

package org.netbeans.modules.apisupport;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.jar.*;
import javax.swing.event.*;

import org.openide.*;
import org.openide.filesystems.FileObject;
import org.openide.modules.ModuleDescription;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListener;

public class InstanceNode extends FilterNode {

    private ManifestProvider provider;
    private Object type;
    private int style;
    private String val;
    private ChangeListener list;

    public InstanceNode (ManifestProvider provider, Object type, int style, String val, Node baseNode) {
        super (baseNode);
        this.provider = provider;
        this.type = type;
        this.style = style;
        this.val = val;
        provider.addChangeListener (WeakListener.change (list = new ChangeListener () {
                                        public void stateChanged (ChangeEvent ev) {
                                            fPC ();
                                        }
                                    }, provider));
    }

    private void fPC () {
        firePropertyChange (null, null, null);
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.modules");
    }

    public boolean canCopy () {
        return false; // maybe later
    }
    public boolean canCut () {
        return false; // maybe later
    }
    public boolean canRename () {
        return false;
    }
    public boolean canDestroy () {
        return true;
    }

    private static class HexBufferInputStream extends InputStream {
        private String buf;
        private int len;
        private int pos;
        private static final String digits = "0123456789ABCDEF";
        public HexBufferInputStream (String contents) {
            buf = contents;
            len = buf.length ();
            pos = 0;
        }
        public void close () throws IOException {
            buf = null;
        }
        public int read () throws IOException {
            if (pos == len) return -1;
            if (pos == len - 1) throw new IOException ("odd # of chars in hex string!");
            char hi = buf.charAt (pos++);
            int numHi = digits.indexOf (hi);
            if (numHi == -1) throw new IOException ("bad hex digit: " + hi);
            char lo = buf.charAt (pos++);
            int numLo = digits.indexOf (lo);
            if (numLo == -1) throw new IOException ("bad hex digit: " + lo);
            return numHi * 16 + numLo;
        }
    }

    public void destroy () throws IOException {
        Manifest mani = provider.getManifest ();
        String associatedFiles = null;
        // Remove manifest attributes.
        if (style == CategoryNode.STYLE_MAIN) {
            Attributes attr = mani.getMainAttributes ();
            attr.remove (type);
            if (type.equals (ModuleDescription.TAG_MAIN) &&
                    attr.containsKey (CategoryNode.ATTR_FILEOBJS_INSTALL)) {
                associatedFiles = attr.getValue (CategoryNode.ATTR_FILEOBJS_INSTALL);
                attr.remove (CategoryNode.ATTR_FILEOBJS_INSTALL);
            } else if (type.equals (ModuleDescription.TAG_DESCRIPTION) &&
                       attr.containsKey (CategoryNode.ATTR_FILEOBJS_HELPSET)) {
                associatedFiles = attr.getValue (CategoryNode.ATTR_FILEOBJS_HELPSET);
                attr.remove (CategoryNode.ATTR_FILEOBJS_HELPSET);
            }
        } else if (style == CategoryNode.STYLE_SECTION) {
            // May still be null after this, OK:
            associatedFiles = mani.getAttributes (val).getValue (CategoryNode.ATTR_FILEOBJS_SECTION);
            mani.getEntries ().remove (val);
        } else {
            // XXX STYLE_DEP implement
            return;
        }
        provider.setManifest (mani);
        // Possibly remove any associated files with this instance, if such were included
        // at the time the instance was added via a paste.
        if (associatedFiles != null) {
            // hello, imports?!
            java.io.InputStream is = new HexBufferInputStream (associatedFiles.trim ());
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream (is);
            FileObject[] files;
            try {
                files = (FileObject[]) ois.readObject ();
            } catch (ClassNotFoundException cnfe) {
                throw new IOException (cnfe.getMessage ());
            }
            ois.close ();
            is.close ();
            provider.removeFiles (new HashSet (Arrays.asList (files)));
        }
    }

    public Node.PropertySet[] getPropertySets () {
        Node.PropertySet[] orig = super.getPropertySets ();
        Node.PropertySet[] nue = new Node.PropertySet[orig.length + 1];
        nue[0] = getExtraSet ();
        for (int i = 0; i < orig.length; i++)
            nue[i + 1] = orig[i];
        return nue;
    }

    private Sheet.Set xtra = null;
    private Sheet.Set getExtraSet () {
        if (xtra == null) {
            xtra = new Sheet.Set ();
            xtra.setName ("manifestprops");
            xtra.setDisplayName ("OpenIDE Manifest");
            xtra.setShortDescription ("Configure how this object will appear in a module manifest.");
            class NameProp extends PropertySupport.ReadOnly {
                public NameProp () {
                    super ("name", String.class, "Resource Path", "Path to the object within the module.");
                }
                public Object getValue () {
                    return val;
                }
            }
            xtra.put (new NameProp ());
            class ExistsProp extends PropertySupport.ReadOnly {
                public ExistsProp () {
                    super ("exists", Boolean.TYPE, "Exists in JAR",
                           "Whether or not the named resource is actually included in the JAR.");
                }
                public Object getValue () throws InvocationTargetException {
                    try {
                        Iterator it = provider.getFiles ().iterator ();
                        while (it.hasNext ()) {
                            FileObject fo = (FileObject) it.next ();
                            if (fo.getPackageNameExt ('/', '.').equals (val))
                                return Boolean.TRUE;
                        }
                        return Boolean.FALSE;
                    } catch (IOException ioe) {
                        throw new InvocationTargetException (ioe);
                    }
                }
            }
            xtra.put (new ExistsProp ());
            if (style == CategoryNode.STYLE_SECTION) {
                class SectionProp extends PropertySupport.ReadWrite {
                    Attributes.Name tag;
                    public SectionProp (Attributes.Name tag, String displayName, String shortDescription) {
                        super (tag.toString (), String.class, displayName, shortDescription);
                        this.tag = tag;
                    }
                    // XXX handle jarcontent == null etc.
                    public Object getValue () throws InvocationTargetException {
                        try {
                            String dispname = provider.getManifest ().getAttributes (val).getValue (tag);
                            return (dispname == null) ? "" : dispname;
                        } catch (IOException ioe) {
                            throw new InvocationTargetException (ioe);
                        }
                    }
                    public void setValue (Object nue) throws IllegalArgumentException, InvocationTargetException {
                        if (! (nue instanceof String)) throw new IllegalArgumentException ();
                        try {
                            Manifest mani = provider.getManifest ();
                            if (nue.equals (""))
                                mani.getAttributes (val).remove (tag);
                            else
                                mani.getAttributes (val).put (tag, nue);
                            provider.setManifest (mani);
                        } catch (IOException ioe) {
                            throw new InvocationTargetException (ioe);
                        }
                    }
                    public void restoreDefaultValue () throws IllegalArgumentException, InvocationTargetException {
                        setValue ("");
                    }
                    public boolean supportsDefaultValue () {
                        return true;
                    }
                }
                if (((String) type).equalsIgnoreCase (ModuleDescription.SECTION_FILESYSTEM)) {
                    xtra.put (new SectionProp (ModuleDescription.TAG_FILESYSTEM_NAME, "Display Name",
                                               "Display name for this file system (as will be displayed on Repository Settings popups)."));
                    // XXX proped to browse to *.hs file:
                    xtra.put (new SectionProp (ModuleDescription.TAG_FILESYSTEM_HELP, "Help Set",
                                               "Path to a HelpSet.hs file for this file system, in the format com.mycom.HelpSet (like a class name)."));
                } else if (((String) type).equalsIgnoreCase (ModuleDescription.SECTION_LOADER)) {
                    // XXX propeds to browse to *.class file whose source cookie says that it is a subclass of DataObject
                    xtra.put (new SectionProp (ModuleDescription.TAG_INSTALL_AFTER, "Install After",
                                               "DataObject representation class(es) of loader(s) to install this one after (comma-separated class names)."));
                    xtra.put (new SectionProp (ModuleDescription.TAG_INSTALL_BEFORE, "Install Before",
                                               "DataObject representation class(es) of loader(s) to install this one before (comma-separated class names)."));
                } else if (((String) type).equalsIgnoreCase (ModuleDescription.SECTION_SERVICE)) {
                    // XXX proped
                    xtra.put (new SectionProp (ModuleDescription.TAG_SERVICE_DEFAULT, "Is Default",
                                               "Whether or not this class of service is the default for its category (True or False)."));
                } else if (((String) type).equalsIgnoreCase (ModuleDescription.SECTION_NODE)) {
                    // XXX proped
                    xtra.put (new SectionProp (ModuleDescription.TAG_NODE_TYPE, "Node Type",
                                               "Where to install this node to."));
                }
            } // STYLE_SECTION
        }
        return xtra;
    }

}

/*
 * Log
 *  17   Gandalf   1.16        1/26/00  Jesse Glick     Manifest handling 
 *       changed--now more dynamic, synched properly with open document as for 
 *       real file types.
 *  16   Gandalf   1.15        1/22/00  Jesse Glick     Manifest files can now 
 *       be recognized, not just JARs.
 *  15   Gandalf   1.14        11/25/99 Jesse Glick     
 *  14   Gandalf   1.13        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  13   Gandalf   1.12        10/6/99  Jesse Glick     Added table of contents,
 *       anchored context help.
 *  12   Gandalf   1.11        10/5/99  Jesse Glick     Sundry API changes 
 *       affecting me.
 *  11   Gandalf   1.10        9/30/99  Jesse Glick     Package rename and misc.
 *  10   Gandalf   1.9         9/20/99  Jesse Glick     Slick group pasting 
 *       (required fix in ExplorerActions).
 *  9    Gandalf   1.8         9/16/99  Jesse Glick     Package change.
 *  8    Gandalf   1.7         9/16/99  Jesse Glick     Changed to work in 
 *       recent builds again; filter node deletion fix.
 *  7    Gandalf   1.6         9/14/99  Jesse Glick     Default values.
 *  6    Gandalf   1.5         9/14/99  Jesse Glick     Added existence prop, 
 *       commented-out props for service types. Also, deletion warns about 
 *       content not being removed. But, deletion no longer updates the view. 
 *       And sometimes filter nodes are not created properly after a paste. Very
 *       mysterious.
 *  5    Gandalf   1.4         9/13/99  Jesse Glick     Got deletion to work.
 *  4    Gandalf   1.3         9/13/99  Jesse Glick     Minor fixes.
 *  3    Gandalf   1.2         9/13/99  Jesse Glick     Some bugfixes, all 
 *       section props implemented.
 *  2    Gandalf   1.1         9/13/99  Jesse Glick     Starting to add 
 *       properties.
 *  1    Gandalf   1.0         9/13/99  Jesse Glick     
 * $
 */
