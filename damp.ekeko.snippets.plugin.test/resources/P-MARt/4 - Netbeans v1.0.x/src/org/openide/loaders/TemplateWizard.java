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

import java.awt.event.ActionEvent;
import java.io.*;
import java.lang.ref.*;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Enumeration;


import org.openide.*;
import org.openide.loaders.*;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.*;

/** Wizard for creation of new objects from a template.
*
* @author Jaroslav Tulach
*/
public class TemplateWizard extends WizardDescriptor {
    /** EA that defines the wizards description */
    private static final String EA_DESCRIPTION = "templateWizardURL"; // NOI18N
    /** EA that defines custom iterator */
    private static final String EA_ITERATOR = "templateWizardIterator"; // NOI18N
    /** EA that defines resource string to the description instead of raw URL */
    private static final String EA_DESC_RESOURCE = "templateWizardDescResource"; // NOI18N


    /** prefered dimmension of the panels */
    static java.awt.Dimension PREF_DIM = new java.awt.Dimension (600, 300);

    /** panel */
    private TemplateWizard1 templateChooser;
    /** panel */
    private TemplateWizard2 targetChooser;

    /** Iterator for the targetChooser */
    private Iterator targetIterator;
    /** whole iterator */
    private TemplateWizardIterImpl iterator;

    /** values for wizards */
    private DataObject template;

    /** class name of object to create */
    private String className = org.openide.util.NbBundle.getBundle(TemplateWizard.class).getString("LAB_TemplateClassName");
    /** package name */
    private String packageName = org.openide.util.NbBundle.getBundle(TemplateWizard.class).getString("LAB_TemplatePackageName");
    /** file system to create object on Reference (FileSystem) */
    private Reference system = new WeakReference (null);

    /** Creates new TemplateWizard */
    public TemplateWizard () {
        this (new TemplateWizard1 (), new TemplateWizard2 ());
    }

    /** Constructor.
    */
    private TemplateWizard (TemplateWizard1 p1, TemplateWizard2 p2) {
        this (new TemplateWizardIterImpl (p1), p1, p2);
    }

    /** Constructor.
    */
    private TemplateWizard (
        TemplateWizardIterImpl it, TemplateWizard1 p1, TemplateWizard2 p2
    ) {
        super (it);
        this.iterator = it;

        templateChooser = p1;
        targetChooser = p2;
        targetIterator = new DefaultIterator (new Panel[] { p2 });

        this.iterator.setIterator (targetIterator, false);

        this.setTitleFormat (new MessageFormat (org.openide.util.NbBundle.getBundle(TemplateWizard.class).getString("CTL_TemplateTitle")));
    }

    /** Getter for current name.
    */
    final String getClassName () {
        return className;
    }

    /** Setter for class name.
    */
    final void setClassName (String name) {
        className = name;
    }

    /** Getter for package name.
    */
    final String getPackageName () {
        return packageName;
    }

    /** Getter for current system to create objects on
    */
    final FileSystem getSystem () {
        FileSystem fs = (FileSystem)system.get ();
        if (fs == null) {
            // search for a file system
            Enumeration en = TopManager.getDefault ().getRepository().fileSystems ();
            while (en.hasMoreElements()) {
                fs = (FileSystem)en.nextElement();
                if (!fs.isHidden()) {
                    // found first non hidden file system
                    system = new WeakReference (fs);
                    return fs;
                }
            }

            // use the default if it is the only one
            fs = TopManager.getDefault ().getRepository().getDefaultFileSystem ();
            system = new WeakReference (fs);
        }
        return fs;
    }

    /** Changes the system & name */
    final void setNameSystem (String packageName, FileSystem system) {
        this.packageName = packageName;
        this.system = new WeakReference (system);
    }

    /** This is method used by TemplateWizard1 to change the template
    */
    final void setTemplateImpl (DataObject obj, boolean notify) {
        DataObject old = template;

        if (template != obj) {
            template = obj;

            templateChooser.readSettings(this);
        }


        if (notify || old != template) {
            Iterator it;
            if (
                obj == null ||
                (it = getIterator (obj)) == null
            ) {
                it = targetIterator;
            }
            this.iterator.setIterator (it, notify);
        }
    }

    /** Getter for template to create object from.
    */
    public DataObject getTemplate () {
        return template;
    }

    /** Sets the template. If under the TopManager.getPlaces ().folders ().
    * templates () directory it will be selected by the dialog.
    *
    * @param obj the template to start with
    */
    public void setTemplate (DataObject obj) {
        if (obj != null) {
            setTemplateImpl (obj, true);
        }
    }

    /** Getter for target folder. If the folder does not
    * exists it is created at this point.
    *
    * @return the target folder
    * @exception IOException if the possible creation of the folder fails
    */
    public DataFolder getTargetFolder () throws IOException {
        DataFolder folder = DataFolder.findFolder (getSystem ().getRoot());

        if (packageName.length () > 0) {
            String f = packageName.replace ('.', '/');
            folder = DataFolder.create (folder, f);
        }

        return folder;
    }

    /** Sets the target folder.
    *
    * @param f the folder
    */
    public void setTargetFolder (DataFolder f) {
        try {
            FileSystem system = f.getPrimaryFile().getFileSystem();

            String newName = f.getPrimaryFile ().getPackageName('.');

            setNameSystem (newName, system);
            targetChooser.readSettings(this);
        } catch (FileStateInvalidException ex) {
        }
    }

    /** Getter for the name of the target template.
    * @return the name
    */
    public String getTargetName () {
        return className;
    }

    /** Setter for the name of the template.
    * @param name name for the new object
    */
    public void setTargetName (String name) {
        className = name;
    }


    /** Panel that is used to choose a template.
    */
    public Panel templateChooser () {
        return templateChooser;
    }

    /** Panel that is used to choose target package and
    * name of the template.
    */
    public Panel targetChooser () {
        return targetChooser;
    }

    /** Chooses the template and instantiates it.
    * @return set of instantiated data objects (DataObject) 
    *   or null if user canceled the dialog
    * @exception IOException I/O error
    */
    public java.util.Set instantiate () throws IOException {
        iterator.first ();
        return instantiateImpl (null, null);
    }

    /** Chooses the template and instantiates it.
    *
    * @param template predefined template that should be instantiated
    * @return set of instantiated data objects (DataObject) 
    *   or null if user canceled the dialog
    * @exception IOException I/O error
    */
    public java.util.Set instantiate (DataObject template) throws IOException {
        return instantiateImpl (template, null);
    }

    /** Chooses the template and instantiates it.
    *
    * @param template predefined template that should be instantiated
    * @param targetFolder the target folder
    *
    * @return set of instantiated data objects (DataObject) 
    *   or null if user canceled the dialog
    * @exception IOException I/O error
    */
    public java.util.Set instantiate (
        DataObject template, DataFolder targetFolder
    ) throws IOException {
        return instantiateImpl (template, targetFolder);
    }

    /** Chooses the template and instantiates it.
    * @param template predefined template or nothing
    * @return set of instantiated data objects (DataObject) 
    *   or null if user canceled the dialog
    * @exception IOException I/O error
    */
    private java.util.Set instantiateImpl (
        DataObject template, DataFolder targetFolder
    ) throws IOException {

        setTemplate (template);


        if (targetFolder != null) {
            setTargetFolder (targetFolder);
        }

        java.awt.Dialog d = TopManager.getDefault().createDialog(this);
        d.show ();
        d.dispose();

        if (getValue () != CANCEL_OPTION && getValue () != CLOSED_OPTION) {
            return iterator.getIterator ().instantiate (this);
        } else {
            return null;
        }
    }

    /** Method to attach a description to a data object.
    * @param obj data object to attach description to
    * @param url the url with description or null if there should be
    *   no description
    * @exception IOException if I/O fails
    */
    public static void setDescription (DataObject obj, URL url) throws IOException {
        obj.getPrimaryFile().setAttribute(EA_DESCRIPTION, url);
    }

    /** Method to get a description for a data object.
    * If the description is not set as a URL but is set as a resource path,
    * that path will be converted to a URL and returned.
    * @param obj data object to attach description to
    * @return the url with description or null
    */
    public static URL getDescription (DataObject obj) {
        URL desc = (URL)obj.getPrimaryFile().getAttribute(EA_DESCRIPTION);
        if (desc != null) return desc;
        String rsrc = getDescriptionAsResource (obj);
        if (rsrc != null) return TopManager.getDefault ().currentClassLoader ().getResource (rsrc);
        return null;
    }

    /** Set a description for a data object by resource path rather than raw URL.
    * @param obj data object to set description for
    * @param rsrc a resource string, e.g. "com/foo/MyPage.html", or <code>null</code> to clear
    */
    public static void setDescriptionAsResource (DataObject obj, String rsrc) throws IOException {
        if (rsrc != null && rsrc.startsWith ("/")) { // NOI18N
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                System.err.println ("Warning: auto-stripping leading slash from resource path in TemplateWizard.setDescriptionAsResource");
            rsrc = rsrc.substring (1);
        }
        obj.getPrimaryFile ().setAttribute (EA_DESC_RESOURCE, rsrc);
    }

    /** Get a description as a resource.
    * @param obj the data object
    * @return the resource path, or <code>null</code> if unset (incl. if only set as a raw URL)
    */
    public static String getDescriptionAsResource (DataObject obj) {
        return (String) obj.getPrimaryFile ().getAttribute (EA_DESC_RESOURCE);
    }

    /** Allows to attach a special Iterator to a template. This allows
    * templates to completelly control the way they are instantiated.
    *
    * @param obj data object
    * @param iter TemplateWizard.Iterator to use for instantiation of this
    *    data object, or <code>null</code> to clear
    * @exception IOException if I/O fails
    */
    public static void setIterator (DataObject obj, Iterator iter)
    throws IOException {
        obj.getPrimaryFile().setAttribute(EA_ITERATOR, iter);
    }

    /** Finds a custom iterator attached to a template that should
    * be used to instantiate the object.
    * @param obj the data object
    * @return custom iterator or null
    */
    public static Iterator getIterator (DataObject obj) {
        return (Iterator)obj.getPrimaryFile ().getAttribute(EA_ITERATOR);
    }


    /** The interface for custom iterator. Enhances to WizardDescriptor.Iterator
    * by serialization and ability to instantiate the object.
    * <P>
    * All Panels provided by this iterator will receive a TemplateWizard
    * as settings object and they are encourage to store its data by the 
    * use of <CODE>putProperty</CODE> method.
    *
    */
    public interface Iterator extends WizardDescriptor.Iterator,
        java.io.Serializable {
        /** Instantiates the template using informations provided by
        * the wizard.
        *
        * @param wiz the wizard
        * @return set of data objects that has been created (should contain
        *   at least one
        * @exception IOException if the instantiation fails
        */
        public java.util.Set instantiate (TemplateWizard wiz)
        throws IOException;
    } // end of Iterator

    /** Implementation of default iterator.
    */
    private static final class DefaultIterator extends ArrayIterator
        implements Iterator {
        /** panel */
        public DefaultIterator (Panel[] arr) {
            super (arr);
        }

        /** Name */
        public String name () {
            return ""; // NOI18N
        }

        /** Instantiates the template using informations provided by
        * the wizard.
        *
        * @param wiz the wizard
        * @return set of data objects that has been created (should contain
        *   at least one) 
        * @exception IOException if the instantiation fails
        */
        public java.util.Set instantiate(TemplateWizard wiz) throws IOException {
            String n = wiz.getTargetName ();
            DataFolder folder = wiz.getTargetFolder ();
            DataObject template = wiz.getTemplate ();
            DataObject obj = template.createFromTemplate (folder, n);

            // run default action (hopefully should be here)
            org.openide.nodes.Node node = obj.getNodeDelegate ();
            org.openide.util.actions.SystemAction sa = node.getDefaultAction ();
            if (sa != null) {
                sa.actionPerformed (new ActionEvent (node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
            }

            return java.util.Collections.singleton(obj);
        }
    }

    /*
      public static void main (String[] args) throws java.lang.Exception {
        TemplateWizard wiz = new TemplateWizard ();
        
        
        FileObject fo = FileSystemCapability.ALL.findResource(
          "Templates/AWTForms/Frame.java"
        );
        DataObject obj = DataObject.find (fo);

        fo = FileSystemCapability.ALL.findResource(
          "test"
        );
        DataFolder f = DataFolder.findFolder(fo);
        
        
        wiz.instantiate();
      }
    */  

}

/*
* Log
*  7    Gandalf   1.6         1/12/00  Ian Formanek    NOI18N
*  6    Gandalf   1.5         1/9/00   Jaroslav Tulach #5205
*  5    Gandalf   1.4         12/17/99 Jesse Glick     Fixed 
*       NullPointerException.
*  4    Gandalf   1.3         12/16/99 Jesse Glick     Ability to set template 
*       descriptions by resource name as well as by URL.
*  3    Gandalf   1.2         12/6/99  Jaroslav Tulach Localized new from 
*       template.
*  2    Gandalf   1.1         12/6/99  Jaroslav Tulach Modified to handle 
*       separatate class & package name.  
*  1    Gandalf   1.0         11/24/99 Jaroslav Tulach 
* $
*/