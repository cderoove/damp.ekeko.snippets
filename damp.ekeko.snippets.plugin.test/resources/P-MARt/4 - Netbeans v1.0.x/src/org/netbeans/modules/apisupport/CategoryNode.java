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

import java.awt.datatransfer.*;
import java.io.*;
import java.util.*;
import java.util.jar.*;
import javax.swing.SwingUtilities;
import javax.swing.event.*;

import org.openide.*;
import org.openide.actions.*;
import org.openide.cookies.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.ModuleDescription;
import org.openide.nodes.*;
import org.openide.src.*;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListener;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.*;

public class CategoryNode extends AbstractNode {
    /**
     * @associates String 
     */
    private static final Map categorySupertypes = new HashMap ();
    static {
        categorySupertypes.put (ModuleDescription.TAG_MAIN, "org.openide.modules.ModuleInstall");
        categorySupertypes.put (ModuleDescription.SECTION_ACTION, "org.openide.util.actions.SystemAction");
        categorySupertypes.put (ModuleDescription.SECTION_CLIPBOARD_CONVERTOR, "org.openide.util.datatransfer.ExClipboard$Convertor");
        categorySupertypes.put (ModuleDescription.SECTION_DEBUGGER, "org.openide.debugger.Debugger");
        categorySupertypes.put (ModuleDescription.SECTION_FILESYSTEM, "org.openide.filesystems.FileSystem");
        categorySupertypes.put (ModuleDescription.SECTION_LOADER, "org.openide.loaders.DataLoader");
        categorySupertypes.put (ModuleDescription.SECTION_NODE, "org.openide.nodes.Node");
        categorySupertypes.put (ModuleDescription.SECTION_OPTION, "org.openide.options.SystemOption");
        categorySupertypes.put (ModuleDescription.SECTION_SERVICE, "org.openide.ServiceType");
        //categorySupertypes.put (ModuleDescription.SECTION_EXECUTOR, "org.openide.execution.ExecutorType");
    }

    private ManifestProvider provider;
    private Object type;
    private int style;

    static final int STYLE_MAIN = 0;
    static final int STYLE_SECTION = 1;
    static final int STYLE_DEP = 2;

    public CategoryNode (ManifestProvider provider, Object type) {
        super (new CategoryChildren (provider, type));
        this.provider = provider;
        this.type = type;
        if (type.equals (ModuleDescription.TAG_MAIN) ||
                type.equals (ModuleDescription.TAG_DESCRIPTION))
            style = STYLE_MAIN;
        else if (type.equals (ModuleDescription.TAG_MODULE_DEPENDENCIES) ||
                 type.equals (ModuleDescription.TAG_PACKAGE_DEPENDENCIES) ||
                 type.equals (ModuleDescription.TAG_JAVA_DEPENDENCIES) ||
                 type.equals (ModuleDescription.TAG_IDE_DEPENDENCIES))
            style = STYLE_DEP;
        else
            style = STYLE_SECTION;
        getCategoryChildren ().style = style;
        setName (type.toString ());
        // XXX nice to have shortDescription giving a brief explanation of what this category is for
        setIconBase ("/org/netbeans/modules/apisupport/resources/CategoryNodeIcon");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.modules");
    }

    private CategoryChildren getCategoryChildren () {
        return (CategoryChildren) getChildren ();
    }

    protected SystemAction[] createActions () {
        return new SystemAction[] {
                   SystemAction.get (PasteAction.class),
                   null,
                   SystemAction.get (ToolsAction.class),
                   SystemAction.get (PropertiesAction.class),
               };
    }

    // XXX new types (for DepNode's)

    /** Test whether the indicated node can serve as the main thing to be pasted to this manifest section.
    * I.e., either a class assignable to the desired section class, or a data node for a .hs file.
    * @param n the node
    * @return the section name if it is, else null
    */
    private String isAcceptablePrimaryPaster (Node n) {
        //System.err.println("Testing for paster: " + n.getDisplayName ());
        if (type.equals (ModuleDescription.TAG_DESCRIPTION)) {
            DataObject dob = (DataObject) n.getCookie (DataObject.class);
            if (dob != null && dob.getPrimaryFile ().hasExt ("hs"))
                return dob.getPrimaryFile ().getPackageName ('.');
            else
                return null;
        } else {
            InstanceCookie inst = (InstanceCookie) n.getCookie (InstanceCookie.class);
            if (inst == null) return null;
            try {
                Class clazz = inst.instanceClass ();
                String supeName = (String) categorySupertypes.get (type);
                Class supe = Class.forName (supeName);
                if (supe.isAssignableFrom (clazz))
                    return clazz.getName ().replace ('.', '/') + ".class";
                else
                    return null;
            } catch (Exception e) {
                e.printStackTrace ();
                return null;
            }
        }
    }

    protected void createPasteTypes (Transferable t, List l) {
        super.createPasteTypes (t, l);
        if (style == STYLE_MAIN && ! getCategoryChildren ().keys.isEmpty ()) {
            //System.err.println("will not accept >1 main instance");
            return; // cannot have more than one
        }
        try {
            //System.err.println("transferable: " + t);
            // XXX this could be simplified with NodeTransfer.nodes...
            if (t.isDataFlavorSupported (ExTransferable.multiFlavor)) {
                //System.err.println("multi flavor");
                // Look for a multi-node copy.
                String text = null;
                Set dobs = new HashSet ();
                MultiTransferObject mto = (MultiTransferObject) t.getTransferData (ExTransferable.multiFlavor);
                int count = mto.getCount ();
                for (int i = 0; i < count; i++) {
                    Transferable ti = mto.getTransferableAt (i);
                    Node n = NodeTransfer.node (ti, NodeTransfer.COPY);
                    if (n != null) {
                        String maybeText = isAcceptablePrimaryPaster (n);
                        if (maybeText != null) {
                            if (text != null) {
                                //System.err.println(">1 primaries");
                                return; // tried to paste >1 primary at once
                            }
                            text = maybeText;
                        }
                        DataObject obj = (DataObject) n.getCookie (DataObject.class);
                        if (obj != null) dobs.add (obj);
                        if (maybeText == null && obj == null) {
                            //System.err.println("useless node: " + n.getDisplayName ());
                            return; // useless node
                        }
                    } else {
                        //System.err.println("not a node being pasted from " + ti);
                        return; // not a node being pasted
                    }
                }
                if (text != null) {
                    // Got exactly one primary, all set.
                    reallyMakePasteType (l, text, dobs);
                } else {
                    //System.err.println("no primary found");
                }
            } else {
                // Try a single-node copy.
                Node n = NodeTransfer.node (t, NodeTransfer.COPY);
                if (n != null) {
                    //System.err.println("single-node flavor");
                    String text = isAcceptablePrimaryPaster (n);
                    if (text != null) {
                        DataObject dob = (DataObject) n.getCookie (DataObject.class);
                        reallyMakePasteType (l, text, dob == null ? Collections.EMPTY_SET : Collections.singleton (dob));
                    } else {
                        //System.err.println("not a primary");
                    }
                } else {
                    //System.err.println("not a node flavor");
                }
            }
        } catch (Exception e) {
            TopManager.getDefault ().notifyException (e);
        }
    }

    /** Attributes indicating files associated with an instance entry in the manifest.
    * The value of such an attribute should be a hex encoding of the serialized form
    * of a FileObject[].
    */
    static final Attributes.Name ATTR_FILEOBJS_HELPSET = new Attributes.Name ("X-APIWizard-HelpSet-AssociatedFiles");
    static final Attributes.Name ATTR_FILEOBJS_INSTALL = new Attributes.Name ("X-APIWizard-Install-AssociatedFiles");
    static final Attributes.Name ATTR_FILEOBJS_SECTION = new Attributes.Name ("X-APIWizard-Section-AssociatedFiles");

    private static class HexBufferOutputStream extends OutputStream {
        private StringBuffer buf;
        private static final String digits = "0123456789ABCDEF";
        public HexBufferOutputStream () {
            buf = new StringBuffer ();
        }
        public final void write (int b) throws IOException {
            buf.append (digits.charAt ((b & 0xF0) >> 4));
            buf.append (digits.charAt (b & 0x0F));
        }
        public String toString () {
            return buf.toString ();
        }
        public void close () throws IOException {
            buf = null;
        }
    }

    /** Actually add a paste type to the list.
    * @param l list of paste types to add to
    * @param text section name
    * @param dobs set of DataObject's to add contents of to JAR
    */
    private void reallyMakePasteType (List l, final String text, final Set dobs) {
        //System.err.println("really making a paste type now for " + text);
        l.add (new PasteType () {
                   public String getName () {
                       return "Add " + text + " to " + (style == STYLE_SECTION ? "section" : "module");
                   }
                   public Transferable paste () throws IOException {
                       // First, serialize list of files, maybe.
                       String associatedFiles;
                       Set files;
                       if (dobs.isEmpty ()) {
                           associatedFiles = null;
                           files = Collections.EMPTY_SET;
                       } else {
                           HexBufferOutputStream hex = new HexBufferOutputStream ();
                           files = new HashSet ();
                           Iterator it = dobs.iterator ();
                           while (it.hasNext ()) {
                               DataObject dob = (DataObject) it.next ();
                               // XXX should maybe exclude any FileObject's which were already in the content
                               files.add (dob.getPrimaryFile ());
                           }
                           ObjectOutputStream oos = new ObjectOutputStream (hex);
                           oos.writeObject (files.toArray (new FileObject[files.size ()]));
                           oos.flush ();
                           associatedFiles = hex.toString ();
                           oos.close ();
                           hex.close ();
                       }
                       // Now, write manifest.
                       Manifest mani = provider.getManifest ();
                       if (style == STYLE_MAIN) {
                           mani.getMainAttributes ().put (type, text);
                           if (associatedFiles != null) {
                               if (type.equals (ModuleDescription.TAG_MAIN)) {
                                   mani.getMainAttributes ().put (ATTR_FILEOBJS_INSTALL, associatedFiles);
                               } else {
                                   // TAG_DESCRIPTION
                                   mani.getMainAttributes ().put (ATTR_FILEOBJS_HELPSET, associatedFiles);
                               }
                           }
                       } else {
                           // STYLE_SECTION
                           Attributes nue = new Attributes ();
                           nue.put (ModuleDescription.TAG_SECTION_CLASS, type);
                           if (associatedFiles != null) nue.put (ATTR_FILEOBJS_SECTION, associatedFiles);
                           mani.getEntries ().put (text, nue);
                       }
                       provider.setManifest (mani);
                       provider.addFiles (files);
                       return ExTransferable.EMPTY;
                   }
               });
    }

    public boolean canCopy () {
        return false;
    }
    public boolean canCut () {
        return false;
    }
    public boolean canRename () {
        return false;
    }
    public boolean canDestroy () {
        return false;
    }

    public static class CategoryChildren extends Children.Keys {

        ManifestProvider provider;
        Object type;
        int style;
        Collection keys = Collections.EMPTY_SET;
        private ChangeListener list;

        public CategoryChildren (ManifestProvider provider, Object type) {
            this.provider = provider;
            this.type = type;
            // style initialized afterwards
            provider.addChangeListener (WeakListener.change (list = new ChangeListener () {
                                            public void stateChanged (ChangeEvent ev) {
                                                updateKeys ();
                                            }
                                        }, provider));
        }

        protected void addNotify () {
            updateKeys ();
        }

        void updateKeys () {
            Manifest mani;
            try {
                mani = provider.getManifest ();
            } catch (IOException ioe) {
                TopManager.getDefault ().notifyException (ioe);
                removeNotify ();
                return;
            }
            if (style == STYLE_MAIN) {
                String val = mani.getMainAttributes ().getValue ((Attributes.Name) type);
                if (val != null)
                    setKeys (new Object[] { val });
                else
                    setKeys (new Object[] { });
            } else if (style == STYLE_SECTION) {
                List l = new LinkedList ();
                java.util.Map entries = mani.getEntries ();
                Iterator it = entries.keySet ().iterator ();
                while (it.hasNext ()) {
                    String name = (String) it.next ();
                    Attributes attr = (Attributes) entries.get (name);
                    String secclass = attr.getValue (ModuleDescription.TAG_SECTION_CLASS);
                    if (secclass != null && secclass.equalsIgnoreCase ((String) type)) {
                        l.add (name);
                    }
                }
                setKeys (keys = l);
            } else {
                // XXX STYLE_DEP implement
                setKeys (keys = Collections.EMPTY_SET);
            }
        }

        protected void removeNotify () {
            setKeys (keys = Collections.EMPTY_SET);
        }

        protected Node[] createNodes (final Object key) {
            if (style == STYLE_MAIN || style == STYLE_SECTION) {
                String val = (String) key;
                if (type.equals (ModuleDescription.TAG_DESCRIPTION))
                    val = val.replace ('.', '/') + ".hs";
                Node baseNode = null;
                FileObject fo = TopManager.getDefault ().getRepository ().findResource (val);
                if (fo != null) {
                    try {
                        DataObject dob = DataObject.find (fo);
                        baseNode = dob.getNodeDelegate ();
                    } catch (DataObjectNotFoundException donfe) {
                    }
                }
                if (baseNode == null) {
                    baseNode = new AbstractNode (Children.LEAF);
                    baseNode.setName (val);
                    baseNode.setDisplayName (val + " <missing>");
                    baseNode.setShortDescription ("Could not find the object " + val + " in the Repository.");
                }
                return new Node[] { new InstanceNode (provider, type, style, val, baseNode) };
            } else {
                // XXX STYLE_DEP implement
                return new Node[] { };
            }
        }

    }

}

/*
 * Log
 *  22   Gandalf   1.21        1/26/00  Jesse Glick     Manifest handling 
 *       changed--now more dynamic, synched properly with open document as for 
 *       real file types.
 *  21   Gandalf   1.20        1/22/00  Jesse Glick     Manifest files can now 
 *       be recognized, not just JARs.
 *  20   Gandalf   1.19        12/17/99 Jesse Glick     JavaHelpDataObject was 
 *       removed.
 *  19   Gandalf   1.18        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  18   Gandalf   1.17        10/7/99  Jesse Glick     File associations 
 *       mechanism changed by the JAR Packager.
 *  17   Gandalf   1.16        10/6/99  Jesse Glick     Added table of contents,
 *       anchored context help.
 *  16   Gandalf   1.15        10/6/99  Jesse Glick     JAR packager content 
 *       model change.
 *  15   Gandalf   1.14        10/5/99  Jesse Glick     
 *  14   Gandalf   1.13        9/30/99  Jesse Glick     Package rename and misc.
 *  13   Gandalf   1.12        9/21/99  Jesse Glick     Small bugfixes.
 *  12   Gandalf   1.11        9/20/99  Jesse Glick     Slick group pasting 
 *       (required fix in ExplorerActions).
 *  11   Gandalf   1.10        9/20/99  Jesse Glick     New resources package.
 *  10   Gandalf   1.9         9/16/99  Jesse Glick     Package change.
 *  9    Gandalf   1.8         9/16/99  Jesse Glick     Changed to work in 
 *       recent builds again; filter node deletion fix.
 *  8    Gandalf   1.7         9/14/99  Jesse Glick     Mostly debugged pasting.
 *  7    Gandalf   1.6         9/13/99  Jesse Glick     Pasting implemented but 
 *       untested (compiles at least).
 *  6    Gandalf   1.5         9/13/99  Jesse Glick     Got deletion to work.
 *  5    Gandalf   1.4         9/13/99  Jesse Glick     Minor fixes.
 *  4    Gandalf   1.3         9/13/99  Jesse Glick     Sections should be 
 *       case-insenitive.
 *  3    Gandalf   1.2         9/13/99  Jesse Glick     Compiles.
 *  2    Gandalf   1.1         9/13/99  Jesse Glick     
 *  1    Gandalf   1.0         9/13/99  Jesse Glick     
 * $
 */
