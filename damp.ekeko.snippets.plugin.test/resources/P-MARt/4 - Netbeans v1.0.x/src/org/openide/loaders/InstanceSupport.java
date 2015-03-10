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

import java.lang.reflect.*;
import java.io.*;

import org.openide.TopManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;

// imports for findHelp:
import java.beans.*;
import javax.swing.JComponent;
import org.openide.windows.TopComponent;
import org.openide.nodes.Node;
import org.openide.ServiceType;
import org.openide.loaders.DataObject;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;
import org.openide.DialogDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.actions.SystemAction;
import org.openide.modules.ManifestSection;
import org.openide.options.SystemOption;
import org.openide.util.Utilities;
import org.openide.util.SharedClassObject;

// Encapsulates working with classes and optimize it.
/** An instance cookie implementation that works with files or entries.
*
* @author   Jan Jancura, Jaroslav Tulach
*/
public class InstanceSupport extends Object implements InstanceCookie {
    /** entry to work with */
    private MultiDataObject.Entry entry;

    /** throw exception during loading of the class */
    private Throwable clazzException;

    /** the class of the instance */
    private Class clazz;

    /** the class is applet */
    private Boolean applet;
    /** the class is bean */
    private Boolean bean;

    /** New support for given entry. The file is taken from the
    * entry and is updated if the entry moves or renames itself.
    * @param entry entry to create instance from
    */
    public InstanceSupport(MultiDataObject.Entry entry) {
        this.entry = entry;
    }

    // main methods .........................................................................................................

    /* The bean name for the instance.
    * @return the name for the instance
    */
    public String instanceName () {
        return instanceOrigin ().getPackageName ('.');
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
        if (clazzException != null) {
            if (clazzException instanceof IOException)
                throw (IOException)clazzException;
            else if (clazzException instanceof ClassNotFoundException)
                throw (ClassNotFoundException)clazzException;
            else if (clazzException instanceof RuntimeException)
                throw (RuntimeException)clazzException;
            else
                throw (ThreadDeath)clazzException;
        }
        if (clazz != null) return clazz;
        //System.out.println ("getClass " + fileName ); // NOI18N
        try {
            if (instanceOrigin ().getExt ().equals ("ser")) { // NOI18N
                // read class from ser file
                InputStream is = instanceOrigin ().getInputStream ();
                try {
                    clazz = readClass (is);
                    return clazz;
                } finally {
                    is.close ();
                }
            } else {
                // find class by class loader
                clazz = findClass (instanceName ());
                if (clazz == null) throw new ClassNotFoundException ();
                return clazz;
            }
        } catch (IOException ex) {
            clazzException = ex;
            throw ex;
        } catch (ClassNotFoundException ex) {
            clazzException = ex;
            throw ex;
        } catch (RuntimeException ex) {
            clazzException = ex;
            throw ex;
        } catch (ThreadDeath t) {
            clazzException = t;
            throw t;
        } catch (Throwable t) {
            // turn other throwables into class not found ex.
            throw (ClassNotFoundException)
            (clazzException = new ClassNotFoundException (t.getMessage ()));
        }
    }

    /** Returns the origin of the instance.
     * @see InstanceCookie.Origin#instanceOrigin
    * @return the origin
    */
    public FileObject instanceOrigin () {
        //    return getEntry ().getFile ();
        return entry.getFile ();
    }

    /*
    * @return an object to work with
    * @exception IOException an I/O error occured
    * @exception ClassNotFoundException the class has not been found
    */
    public Object instanceCreate ()
    throws java.io.IOException, ClassNotFoundException {
        try {
            if (instanceOrigin ().getExt ().equals ("ser")) { // NOI18N
                // create from ser file
                Object o = java.beans.Beans.instantiate (
                               TopManager.getDefault ().currentClassLoader (),
                               instanceName ()
                           );
                return o;
            } else {
                Class c = instanceClass ();
                if (SharedClassObject.class.isAssignableFrom (c)) {
                    // special support
                    return SharedClassObject.findObject (c, true);
                } else {
                    // create new instance
                    return c.newInstance ();
                }
            }
        } catch (IOException ex) {
            throw ex;
        } catch (ClassNotFoundException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (ThreadDeath t) {
            throw t;
        } catch (Throwable t) {
            // turn other throwables into class not found ex.
            throw (ClassNotFoundException)
            (clazzException = new ClassNotFoundException (t.getMessage ()));
        }
    }

    /** Is this an applet?
    * @return <code>true</code> if this class is an {@link Applet}
    */
    public boolean isApplet () {
        if (applet != null) return applet.booleanValue ();
        boolean b = false;
        try {
            b = java.applet.Applet.class.isAssignableFrom (instanceClass ());
        } catch (RuntimeException ex) {
            throw ex;
        } catch (ThreadDeath t) {
            throw t;
        } catch (Throwable t) {
            // false when other errors occur (NoClassDefFoundError etc...)
            applet = Boolean.FALSE;
            return false;
        }
        applet = new Boolean (b);
        return b;
    }

    /** Is this a standalone executable?
    * @return <code>true</code> if this class has main method
    * (e.g., <code>public static void main (String[] arguments)</code>).
    */
    public boolean isExecutable () {
        try {
            Method main = instanceClass ().getDeclaredMethod ("main", new Class[] { // NOI18N
                              String[].class
                          });

            int m = main.getModifiers ();
            return Modifier.isPublic (m) && Modifier.isStatic (m) && Void.TYPE.equals (
                       main.getReturnType ()
                   );
        } catch (RuntimeException ex) {
            throw ex;
        } catch (ThreadDeath t) {
            throw t;
        } catch (Throwable t) {
            // false when other errors occur (NoClassDefFoundError etc...)
            // [PENDING] ThreadDeath (ditto for other methods)
            return false;
        }
    }

    /** Is this a JavaBean?
    * @return <code>true</code> if this class represents JavaBean (is public and has a public default constructor).
    */
    public boolean isJavaBean () {
        if (bean != null) return bean.booleanValue ();
        // try to find out...
        try {
            int modif = instanceClass ().getModifiers ();
            if (!Modifier.isPublic (modif) || Modifier.isAbstract (modif)) {
                bean = Boolean.FALSE;
                return false;
            }
            Constructor c;
            try {
                c = instanceClass ().getConstructor (new Class [0]);
            } catch (NoSuchMethodException e) {
                bean = Boolean.FALSE;
                return false;
            }
            if ((c == null) || !Modifier.isPublic (c.getModifiers ())) {
                bean = Boolean.FALSE;
                return false;
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (ThreadDeath t) {
            throw t;
        } catch (Throwable t) {
            // false when other errors occur (NoClassDefFoundError etc...)
            bean = Boolean.FALSE;
            return false;
        }
        // okay, this is bean...
        //    return isBean = java.io.Serializable.class.isAssignableFrom (clazz);
        bean = Boolean.TRUE;
        return true;
    }

    /** Is this an interface?
    * @return <code>true</code> if the class is an interface
    */
    public boolean isInterface () {
        try {
            return instanceClass ().isInterface ();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (ThreadDeath t) {
            throw t;
        } catch (Throwable t) {
            // false when other errors occur (NoClassDefFoundError etc...)
            return false;
        }
    }

    public String toString () {
        return instanceName ();
    }

    /** Find context help for some instance.
    * Helper method useful in nodes or data objects that provide an instance cookie;
    * they may choose to supply their own help context based on this.
    * All API classes which can provide help contexts will be tested for
    * (including <code>HelpCtx</code> instances themselves).
    * <code>JComponent</code>s are checked for an attached help ID property,
    * as with {@link HelpCtx#findHelp} (but not traversing parents).
    * <p>Also, partial compliance with the JavaHelp section on JavaBeans help is implemented--i.e.,
    * if a Bean in its <code>BeanInfo</code> provides a <code>BeanDescriptor</code> which
    * has the attribute <code>helpID</code>, this will be returned. The value is not
    * defaulted (because it would usually be nonsense and would mask a useful default
    * help for the instance container), nor is the help set specification checked,
    * since someone should have installed the proper help set anyway, and the APIs
    * cannot add a new reference to a help set automatically.
    * See <code>javax.help.HelpUtilities.getIDStringFromBean</code> for details.
    * <p>Special IDs are added, corresponding to the class name, for all standard visual components.
    * @param instance the instance to check for help (it is permissible for the {@link InstanceCookie#instanceCreate} to return <code>null</code>)
    * @return the help context found on the instance or inferred from a Bean,
    * or <code>null</code> if none was found (or it was {@link HelpCtx#DEFAULT_HELP})
    */
    public static HelpCtx findHelp (InstanceCookie instance) {
        Class clazz = null;
        try {
            clazz = instance.instanceClass ();
            // First try known API classes.
            if (TopComponent.class.isAssignableFrom (clazz) ||
                    Node.class.isAssignableFrom (clazz) ||
                    DataObject.class.isAssignableFrom (clazz) ||
                    NewType.class.isAssignableFrom (clazz) ||
                    PasteType.class.isAssignableFrom (clazz) ||
                    DialogDescriptor.class.isAssignableFrom (clazz) ||
                    WizardDescriptor.Panel.class.isAssignableFrom (clazz) ||
                    SystemAction.class.isAssignableFrom (clazz) ||
                    ManifestSection.FileSystemSection.class.isAssignableFrom (clazz) ||
                    SystemOption.class.isAssignableFrom (clazz) ||
                    ServiceType.class.isAssignableFrom (clazz) ||
                    HelpCtx.class.isAssignableFrom (clazz)) {
                HelpCtx test;
                Object obj = instance.instanceCreate ();
                if (obj instanceof TopComponent)
                    test = ((TopComponent) obj).getHelpCtx ();
                else if (obj instanceof Node)
                    test = ((Node) obj).getHelpCtx ();
                else if (obj instanceof DataObject)
                    test = ((DataObject) obj).getHelpCtx ();
                else if (obj instanceof NewType)
                    test = ((NewType) obj).getHelpCtx ();
                else if (obj instanceof PasteType)
                    test = ((PasteType) obj).getHelpCtx ();
                else if (obj instanceof DialogDescriptor)
                    test = ((DialogDescriptor) obj).getHelpCtx ();
                else if (obj instanceof WizardDescriptor.Panel)
                    test = ((WizardDescriptor.Panel) obj).getHelp ();
                else if (obj instanceof SystemAction)
                    test = ((SystemAction) obj).getHelpCtx ();
                else if (obj instanceof ManifestSection.FileSystemSection)
                    test = ((ManifestSection.FileSystemSection) obj).getHelpCtx ();
                else if (obj instanceof SystemOption)
                    test = ((SystemOption) obj).getHelpCtx ();
                else if (obj instanceof ServiceType)
                    test = ((ServiceType) obj).getHelpCtx ();
                else if (obj instanceof HelpCtx)
                    test = (HelpCtx) obj;
                else
                    test = null;          // obj==null or bad cookie
                if (test != null && ! test.equals (HelpCtx.DEFAULT_HELP))
                    return test;
            }
            // If a component, look for attached help.
            if (JComponent.class.isAssignableFrom (clazz)) {
                JComponent comp = (JComponent) instance.instanceCreate ();
                if (comp != null) {
                    String hid = (String) comp.getClientProperty ("HelpID"); // NOI18N
                    if (hid != null)
                        return new HelpCtx (hid);
                }
            }
            // Look for Bean help. Also works on components not found above.
            BeanDescriptor desc = Utilities.getBeanInfo (clazz).getBeanDescriptor ();
            if (desc != null) {
                // [PENDING] ideally would also look for a help set and add that to the system
                // set if found, but there is no API for this at the moment
                String val = (String) desc.getValue ("helpID"); // NOI18N
                if (val != null) return new HelpCtx (val);
            }
            // Help on some standard components. Note that borders/layout managers do not really work here.
            if (java.awt.Component.class.isAssignableFrom (clazz) || java.awt.MenuComponent.class.isAssignableFrom (clazz)) {
                String name = clazz.getName ();
                String[] pkgs = new String[] { "java.awt.", "javax.swing.", "javax.swing.border." }; // NOI18N
                for (int i = 0; i < pkgs.length; i++) {
                    if (name.startsWith (pkgs[i]) && name.substring (pkgs[i].length ()).indexOf ('.') == -1)
                        return new HelpCtx (name);
                }
            }
            // All failed.
            return null;
        } catch (Exception e) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) { // NOI18N
                System.err.println ("During inspection of " + instance + " (class " + clazz + "):");
                e.printStackTrace ();
            }
            return null;
        }
    }

    /** Reads a class from input stream. Expects a serialized object to be stored
    * in the stream and reads only a class from it.
    * @param is input stream to read from
    * @return the class of that stream
    * @exception IOException if something fails
    */
    private Class readClass (InputStream is) throws IOException, ClassNotFoundException {
        /** object input stream */
        class OIS extends ObjectInputStream {
            public OIS (InputStream iss) throws IOException {
                super (iss);
            }

            /** Throws exception to signal the kind of class found.
            */
            public Class resolveClass (ObjectStreamClass osc)
            throws IOException, ClassNotFoundException {
                throw new ClassEx (findClass (osc.getName ()));
            }
        };

        ObjectInputStream ois = new OIS (new BufferedInputStream (is));

        try {
            ois.readObject ();
            // should not happen
            throw new ClassNotFoundException ();
        } catch (ClassEx ex) {
            // good, we found the class
            return ex.clazz;
        }
    }

    /** Finds a class for given name.
    * @param name name of the class
    * @return the class for the name
    * @exception ClassNotFoundException if the class cannot be found
    */
    private Class findClass (String name) throws ClassNotFoundException {
        try {
            return TopManager.getDefault ().currentClassLoader ().loadClass (name);
        } catch (ClassNotFoundException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (ThreadDeath t) {
            throw t;
        } catch (Throwable t) {
            // turn other throwables into class not found ex.
            throw new ClassNotFoundException (t.getMessage ());
        }
    }

    /** Enhanced instance cookie support that also knows the file it
    * has been created from and can be serialized back to.
    * Note that <code>InstanceSupport</code> already does; this class
    * only declares the interface.
    */
    public static class Origin extends InstanceSupport
        implements InstanceCookie.Origin {
        /** New support for a given entry. The file is taken from the
        * entry and is updated if the entry moves or renames itself.
        * @param entry entry to create instance from
        */
        public Origin (MultiDataObject.Entry entry) {
            super (entry);
        }
    }

    /** Trivial supporting instance cookie for already-existing objects.
    */
    public static class Instance extends Object implements InstanceCookie {
        /** the object to represent */
        private Object obj;


        /** Create a new instance cookie.
         * @param obj the object to represent in this cookie
        */
        public Instance (Object obj) {
            this.obj = obj;
        }

        /* The bean name for the instance.
        * @return the name for the instance
        */
        public String instanceName () {
            return obj.getClass ().getName ();
        }

        /* The class of the instance represented by this cookie.
        * Can be used to test whether the instance is of valid
        * class before it is created.
        *
        * @return the class of the instance
        */
        public Class instanceClass () {
            return obj.getClass ();
        }

        /*
        * @return an object to work with
        */
        public Object instanceCreate () {
            return obj;
        }
    }

    /** The exception to use to signal succesful find of a class.
    * Used in method readClass.
    */
    private class ClassEx extends IOException {
        /** founded class */
        public Class clazz;

        static final long serialVersionUID =4810039297880922426L;
        /** @param c the class
        */
        public ClassEx (Class c) {
            clazz = c;
        }
    }

}



/*
 * Log
 *  23   Gandalf   1.22        1/13/00  Ian Formanek    NOI18N
 *  22   Gandalf   1.21        1/12/00  Ian Formanek    NOI18N
 *  21   Gandalf   1.20        12/29/99 Jaroslav Tulach Special handling for 
 *       SharedClassObject.
 *  20   Gandalf   1.19        11/5/99  Jesse Glick     Help context on visual 
 *       component classes.
 *  19   Gandalf   1.18        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  18   Gandalf   1.17        10/5/99  Jesse Glick     Only conditionally 
 *       reporting exceptions in findHelp.
 *  17   Gandalf   1.16        9/10/99  Jaroslav Tulach Changes in services 
 *       APIs.
 *  16   Gandalf   1.15        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  15   Gandalf   1.14        7/21/99  Jesse Glick     Bugfix (TopComponent's 
 *       in Component Palette).
 *  14   Gandalf   1.13        7/20/99  Jesse Glick     findHelp bugfixes.
 *  13   Gandalf   1.12        7/11/99  David Simonek   window system change...
 *  12   Gandalf   1.11        7/7/99   Jesse Glick     System options & 
 *       executor types have help contexts.
 *  11   Gandalf   1.10        6/25/99  Jesse Glick     Instances can have 
 *       sensible help contexts.
 *  10   Gandalf   1.9         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  9    Gandalf   1.8         4/16/99  Libor Martinek  
 *  8    Gandalf   1.7         3/26/99  Jaroslav Tulach 
 *  7    Gandalf   1.6         3/14/99  Jaroslav Tulach Change of 
 *       MultiDataObject.Entry.
 *  6    Gandalf   1.5         3/10/99  Jesse Glick     [JavaDoc]
 *  5    Gandalf   1.4         1/20/99  David Simonek   
 *  4    Gandalf   1.3         1/20/99  David Simonek   rework of class DO
 *  3    Gandalf   1.2         1/17/99  Jaroslav Tulach 
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
