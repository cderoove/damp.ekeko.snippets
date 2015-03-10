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

package org.openide.loaders;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.Enumeration;

import org.openide.*;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.actions.OpenAction;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.nodes.*;

/** A data object whose only purpose is to supply <code>InstanceCookie</code>.
* The instances are created by default instantiation; the name of the class
* to instantiate is stored on disk, typically right in the file name.
* <p>This data object is generally used to configure menus and toolbars,
* though it could be used in any situation requiring instances to be present in
* a folder; for example, anything using {@link FolderInstance}.
* <p>Use {@link #create} and {@link #remove} to make the objects.
*
* @author Ian Formanek
*/
public class InstanceDataObject extends MultiDataObject implements InstanceCookie {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -6134784731744777123L;

    private static final String INSTANCE_ICON_BASE =
        "/org/openide/resources/instanceObject"; // NOI18N

    /** opening symbol */
    private static final char OPEN = '[';
    /** closing symbol */
    private static final char CLOSE = ']';

    /** File extension for instance data objects. */
    public static final String INSTANCE = "instance"; // NOI18N

    /** Create a new instance.
    * Do not use this to make instances; use {@link #create}.
    * @param pf primary file object for this data object
    * @param loader the loader
    * @throws DataObjectExistsException if it already exists
    */
    public InstanceDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super(pf, loader);
    }

    /** Finds instance of specified name in a given folder.
    * @param folder the folder to create the instance data object in
    * @param name the name to give to the object (can be <code>null</code> if no special name besides the class name is needed)
    * @param className the name of the class the new object should provide an instance of
    * @return the found instance data object or null if it does not exist
    */
    public static InstanceDataObject find (DataFolder folder, String name, String className) {
        FileObject fo = folder.getPrimaryFile ();
        String fileName;
        if (name == null) {
            fileName = className.replace ('.', '-');
        } else {
            fileName = escape (name) + OPEN + className.replace ('.', '-') + CLOSE;
        }

        FileObject file = fo.getFileObject (fileName, INSTANCE);
        if (file != null) {
            try {
                return (InstanceDataObject)DataObject.find (file);
            } catch (DataObjectNotFoundException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /** Finds instance of specified name in a given folder.
    * @param folder the folder to create the instance data object in
    * @param name the name to give to the object (can be <code>null</code> if no special name besides the class name is needed)
    * @param clazz the class to create instance for
    * @return the found instance data object or null if it does not exist
    */
    public static InstanceDataObject find (DataFolder folder, String name, Class clazz) {
        return find (folder, name, clazz.getName ());
    }

    /** Create a new <code>InstanceDataObject</code> in a given folder. If object with specified name already exists, it is returned.
    * You should specify the name if there is a chance another file of the same
    * instance class already exists in the folder; or just to provide a more
    * descriptive name, which will appear in the Explorer for example.
    * @param folder the folder to create the instance data object in
    * @param name the name to give to the object (can be <code>null</code> if no special name besides the class name is needed)
    * @param className the name of the class the new object should provide an instance of
    * @return the newly created or eisting instance data object
    * @exception IOException if the file cannot be created  
    */
    public static InstanceDataObject create (DataFolder folder, String name, String className) throws IOException {
        FileObject fo = folder.getPrimaryFile ();
        String fileName;
        if (name == null) {
            fileName = className.replace ('.', '-');
        } else {
            fileName = escape (name) + OPEN + className.replace ('.', '-') + CLOSE;
        }

        FileObject newFile = fo.getFileObject (fileName, INSTANCE);
        if (newFile == null) newFile = fo.createData (fileName, INSTANCE);

        return (InstanceDataObject)DataObject.find (newFile);
    }

    /** Create a new <code>InstanceDataObject</code> in a given folder. If object with specified name already exists, it is returned.
    * You should specify the name if there is a chance another file of the same
    * instance class already exists in the folder; or just to provide a more
    * descriptive name, which will appear in the Explorer for example.
    * @param folder the folder to create the instance data object in
    * @param name the name to give to the object (can be <code>null</code> if no special name besides the class name is needed)
    * @param clazz the class to create instance for
    * @return the newly created or eisting instance data object
    * @exception IOException if the file cannot be created  
    */
    public static InstanceDataObject create (DataFolder folder, String name, Class clazz) throws IOException {
        return create (folder, name, clazz.getName ());
    }

    /** Remove an existing instance data object.
    * If you have the exact file name, just call {@link DataObject#delete};
    * this method lets you delete an instance you do not have an exact record
    * of the file name for, based on the same information used to create it.
    * @param folder the folder to remove the file from
    * @param name the name of the instance (can be <code>null</code>)
    * @param className the name of class the object referred to
    * @return <code>true</code> if the instance was succesfully removed, <code>false</code> if not
    */
    public static boolean remove (DataFolder folder, String name,
                                  String className) {
        FileLock lock = null;
        try {
            String fileName;
            if (name == null) {
                fileName = className.replace ('.', '-');
            } else {
                fileName = escape (name) + OPEN + className.replace ('.', '-') + CLOSE;
            }
            FileObject fileToRemove =
                folder.getPrimaryFile().getFileObject (fileName, INSTANCE);
            if (fileToRemove == null) // file not found
                return false;
            lock = fileToRemove.lock();
            fileToRemove.delete(lock);
        } catch (IOException exc) {
            // something is bad, instance wasn't removed
            return false;
        } finally {
            if (lock != null)
                lock.releaseLock();
        }
        return true;
    }

    /** Remove an existing instance data object.
    * If you have the exact file name, just call {@link DataObject#delete};
    * this method lets you delete an instance you do not have an exact record
    * of the file name for, based on the same information used to create it.
    * @param folder the folder to remove the file from
    * @param name the name of the instance (can be <code>null</code>)
    * @param className the name of class the object referred to
    * @return <code>true</code> if the instance was succesfully removed, <code>false</code> if not
    */
    public static boolean remove (DataFolder folder, String name, Class clazz) {
        return remove (folder, name, clazz.getName ());
    }

    /* Help context for this object.
    * @return help context
    */
    public HelpCtx getHelpCtx () {
        HelpCtx test = InstanceSupport.findHelp (this);
        if (test != null)
            return test;
        else
            return new HelpCtx (InstanceDataObject.class);
    }

    /* Provides node that should represent this data object. When a node for representation
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
        return new InstanceNode ();
    }

    /* The name of the bean for this file or null if the class name is not encoded
    * in the file name and rather the CLASS_NAME property from the file content should be used.
    *
    * @return the name for the instance or null if the class name is not defined in the name
    */
    public String instanceName () {
        String name = getPrimaryFile ().getName ();

        int first = name.indexOf (OPEN) + 1;

        int last = name.indexOf (CLOSE);
        if (last < 0) {
            last = name.length ();
        }

        // take only a part of the string
        if (first < last) {
            name = name.substring (first, last);
        }

        name = name.replace ('-', '.');
        //System.out.println ("Original: " + getPrimaryFile ().getName () + " new one: " + name); // NOI18N
        name = org.openide.util.Utilities.translate(name);
        return name;
    }

    /* The class of the instance represented by this cookie.
    * Can be used to test whether the instance is of valid
    * class before it is created.
    *
    * @return the class of the instance
    * @exception IOException an I/O error occured
    * @exception ClassNotFoundException the class has not been found
    */
    public Class instanceClass ()
    throws java.io.IOException, ClassNotFoundException {
        String className = instanceName ();

        return Class.forName (
                   className, true, TopManager.getDefault ().currentClassLoader ()
               );
    }

    /*
    * @return an object to work with
    * @exception IOException an I/O error occured
    * @exception ClassNotFoundException the class has not been found
    */
    public Object instanceCreate ()
    throws java.io.IOException, ClassNotFoundException {
        try {
            Class c = instanceClass ();
            if (SharedClassObject.class.isAssignableFrom (c)) {
                // special support
                return SharedClassObject.findObject (c, true);
            } else {
                // create new instance
                return c.newInstance ();
            }
        } catch (ClassNotFoundException ex) {
            throw ex;
        } catch (Throwable ex) {
            if (ex instanceof ThreadDeath) {
                throw (ThreadDeath)ex;
            }
            if (System.getProperty ("netbeans.debug.exceptions") != null) ex.printStackTrace ();
            throw new ClassNotFoundException (ex.getMessage ());
        }
    }

    /* Overriden to return only first part till the bracket */
    public String getName () {
        String superName = super.getName();
        int bracket = superName.indexOf (OPEN);
        return unescape ((bracket < 0) ? superName : superName.substring (0, bracket));
    }

    /** Hex-escapes anything potentially nasty in some text. */
    private static String escape (String text) {
        int len = text.length ();
        StringBuffer escaped = new StringBuffer (len);
        for (int i = 0; i < len; i++) {
            char c = text.charAt (i);
            // For some reason Windoze throws IOException if angle brackets in filename...
            if (c == '/' || c == ':' || c == '\\' || c == OPEN || c == CLOSE || c == '<' || c == '>' ||
                    c == '.' || c == '"' || c < '\u0020' || c > '\u007E' || c == '#') {
                // Hex escape.
                escaped.append ('#');
                String hex = Integer.toString (c, 16).toUpperCase ();
                if (hex.length () < 4) escaped.append ('0');
                if (hex.length () < 3) escaped.append ('0');
                if (hex.length () < 2) escaped.append ('0');
                escaped.append (hex);
            } else {
                escaped.append (c);
            }
        }
        return escaped.toString ();
    }

    /** Removes hex escapes and regenerates displayable Unicode. */
    private static String unescape (String text) {
        int len = text.length ();
        StringBuffer unesc = new StringBuffer (len);
        for (int i = 0; i < len; i++) {
            char c = text.charAt (i);
            if (c == '#') {
                if (i + 4 >= len) {
                    if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                        System.err.println("trailing garbage in instance name: " + text); // NOI18N
                    break;
                }
                try {
                    char[] hex = new char[4];
                    text.getChars (i + 1, i + 5, hex, 0);
                    unesc.append ((char) Integer.parseInt (new String (hex), 16));
                } catch (NumberFormatException nfe) {
                    if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                        nfe.printStackTrace ();
                }
                i += 4;
            } else {
                unesc.append (c);
            }
        }
        return unesc.toString ();
    }

    //
    // Temporary properties methods
    //
    /** Provides access to properties defined inside the instance file.
    * @param  key name of the key for which the value is to be acquired
    * @return the value for specified key or null if the key was not found
    */

    private Properties instanceProperties;

    String getProperty (String key) {
        return getInstanceProperties ().getProperty (key);
    }


    /** Lazy initialization of instance Properties object */
    private Properties getInstanceProperties () {
        if (instanceProperties == null) {
            instanceProperties = new Properties ();

            if (getPrimaryFile ().getSize () == 0) {
                // empty file need not be loaded
                return instanceProperties;
            }

            try {
                java.io.InputStream is = getPrimaryFile ().getInputStream ();
                try {
                    instanceProperties.load (is);
                } finally {
                    is.close ();
                }
            } catch (java.io.FileNotFoundException e) {
                // OK, empty properties
            } catch (java.io.IOException e) {
                // OK, empty properties
            }
        }
        return instanceProperties;
    }

    static final String ICON_NAME = "icon"; // NOI18N

    static final String translateIcon(String name) {
        final int namelen = name.length();
        final int ext = name.lastIndexOf('.');
        String tmp = name.substring(1, ext);
        tmp = tmp.replace('/', '.');
        tmp = org.openide.util.Utilities.translate(tmp);
        tmp = tmp.replace('.', '/');
        tmp = '/' + tmp + name.substring(ext);
        return tmp;
    }

    /** Node that uses special ways to obtain icon.
    */
    private final class InstanceNode extends DataNode
        implements Runnable {
        /** icon from bean info */
        private Image beanInfoIcon;

        /** Constructor */
        public InstanceNode () {
            super (InstanceDataObject.this, Children.LEAF);
            setIconBase(INSTANCE_ICON_BASE);
            setDefaultAction (SystemAction.get (OpenAction.class));

            RequestProcessor.postRequest (this);
        }

        /** Find an icon for this node (in the closed state).
        * @param type constant from {@link java.beans.BeanInfo}
        * @return icon to use to represent the node
        */
        public Image getIcon (int type) {
            if (beanInfoIcon != null) {
                return beanInfoIcon;
            } else {
                return super.getIcon (type);
            }
        }

        /** Find an icon for this node (in the open state).
        * This icon is used when the node may have children and is expanded.
        *
        * @param type constant from {@link java.beans.BeanInfo}
        * @return icon to use to represent the node when open
        */
        public Image getOpenedIcon (int type) {
            return getIcon (type);
        }

        /** Runs the get for the icon */
        public void run () {
            String expliciteIcon = getProperty (ICON_NAME);
            if (expliciteIcon != null) {
                expliciteIcon = translateIcon(expliciteIcon);
                // create the icon from the resource
                java.net.URL url = TopManager.getDefault ().currentClassLoader ().getResource (expliciteIcon);
                if (url != null) {
                    javax.swing.ImageIcon imic = new javax.swing.ImageIcon (url);

                    // only take the icon if it is correctly loaded
                    if (imic.getImageLoadStatus() == java.awt.MediaTracker.COMPLETE) {
                        beanInfoIcon = imic.getImage ();
                    }
                }
            } else {
                try {
                    BeanInfo bi = Utilities.getBeanInfo (instanceClass ());
                    if (bi != null) {
                        beanInfoIcon = bi.getIcon (BeanInfo.ICON_COLOR_16x16);
                    }
                } catch (java.io.IOException e) {
                    // Problem ==>> use default icon
                } catch (ClassNotFoundException e2) {
                    // Problem ==>> use default icon
                } catch (java.beans.IntrospectionException e3) {
                    // Problem ==>> use default icon
                }
            }

            if (beanInfoIcon != null) {
                // fire change
                fireIconChange ();
            }

        }

    }
}


/*
 * Log
 *  26   Gandalf   1.25        1/17/00  Jesse Glick     Angle brackets must be 
 *       escaped on Windows.
 *  25   Gandalf   1.24        1/15/00  Jesse Glick     Zero-padding.
 *  24   Gandalf   1.23        1/15/00  Jesse Glick     IDO now privately 
 *       manages all filename escaping for safety.
 *  23   Gandalf   1.22        1/15/00  Jesse Glick     Recognizing hex escapes 
 *       in instance names.
 *  22   Gandalf   1.21        1/13/00  Ian Formanek    NOI18N
 *  21   Gandalf   1.20        1/12/00  Ian Formanek    NOI18N
 *  20   Gandalf   1.19        1/11/00  Jesse Glick     Was not creating the 
 *       correct InstanceCookie for its own instances.
 *  19   Gandalf   1.18        12/29/99 Jaroslav Tulach Special handling for 
 *       SharedClassObject.
 *  18   Gandalf   1.17        11/3/99  Jaroslav Tulach Runs iconization later.
 *  17   Gandalf   1.16        10/25/99 Jaroslav Tulach Runs instantiation 
 *       later.
 *  16   Gandalf   1.15        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  15   Gandalf   1.14        8/17/99  Ian Formanek    Undone last change
 *  14   Gandalf   1.13        8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  13   Gandalf   1.12        8/9/99   Ian Formanek    Survives more problems 
 *       when creating instance
 *  12   Gandalf   1.11        7/25/99  Ian Formanek    Exceptions printed to 
 *       console only on "netbeans.debug.exceptions" flag
 *  11   Gandalf   1.10        7/15/99  Ian Formanek    create methods return 
 *       survive if instance of specified name already exists (and return it), 
 *       added find methods
 *  10   Gandalf   1.9         7/11/99  David Simonek   window system change...
 *  9    Gandalf   1.8         6/25/99  Jesse Glick     Instances can have 
 *       sensible help contexts.
 *  8    Gandalf   1.7         6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  7    Gandalf   1.6         6/10/99  Jaroslav Tulach 
 *  6    Gandalf   1.5         6/10/99  Jesse Glick     [JavaDoc]
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         6/1/99   David Simonek   method remove(...) added
 *  2    Gandalf   1.1         5/14/99  Jaroslav Tulach Pallete works again.
 *  1    Gandalf   1.0         5/11/99  Jaroslav Tulach 
 * $
 */
