/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.properties;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import javax.swing.text.Keymap;
import javax.swing.*;

import org.openide.modules.ModuleInstall;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.Utilities;
import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.InstanceDataObject;

/** Installation class for the properties module
*
* @author Petr Jiricka
*/
public class PropertiesModule extends ModuleInstall {

    private static PropertiesDataObject lastBundleUsed;

    private static String lastBundleName;

    /* Constructor */
    static final long serialVersionUID =4119556963177537363L;
    public PropertiesModule() {
        // A public default constructor is required!
        // Of course, Java makes one by default for a public class too.
    }

    public void installed() {
        // -----------------------------------------------------------------------------
        // 1. copy Templates
        copyTemplates ();

        // This module has been installed for the first time! Notify authors.
        // Handle setup within this session too:
        restored();
    }

    /** Creates a new multitab mode for the properties files */
    public void restored() {
        installColoring();
        assign("A-I", InsertLocalStringAction.class);
        /*java.awt.Toolkit.getDefaultToolkit ().getImage (
          EditorSupport.class.getResource ("/org/openide/resources/editorMode.gif")
        ) */     
    }

    private void installColoring() {
        try {
            Class settings = Class.forName
                             ("org.netbeans.editor.Settings",
                              false, this.getClass().getClassLoader()); // only test for editor module

            Class restore = Class.forName
                            ("org.netbeans.modules.properties.syntax.RestoreColoring",
                             false, this.getClass().getClassLoader());
            Method restoreMethod = restore.getMethod ("restore", null);
            restoreMethod.invoke (restore.newInstance(), null);
        } catch (ClassNotFoundException e) {
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e) {
        } catch (IllegalAccessException e) {
        } catch (InstantiationException e) {
        }
    }

    public static PropertiesDataObject getLastBundleUsed() {
        if (lastBundleUsed == null && lastBundleName != null) {
            FileObject fo = TopManager.getDefault().getRepository().findResource(lastBundleName);
            if (fo != null)
                try {
                    DataObject dObj = TopManager.getDefault().getLoaderPool().findDataObject(fo);
                    if (dObj instanceof PropertiesDataObject)
                        lastBundleUsed = (PropertiesDataObject)dObj;
                }
            catch (IOException e) {}
        }
        return lastBundleUsed;
    }

    public static void setLastBundleUsed(PropertiesDataObject newLastBundleUsed) {
        if (newLastBundleUsed != null) {
            lastBundleUsed = newLastBundleUsed;
            lastBundleName = lastBundleUsed.getPrimaryFile().getPackageNameExt('/', '.');
        }
    }

    /** Writes data
    * @param out ObjectOutputStream
    */
    public void writeExternal(ObjectOutput out) throws IOException {
        if (lastBundleUsed != null) out.writeObject(lastBundleName);
        else                        out.writeObject(null);
        out.writeObject(ResourceBundleStringEditor.javaStringFormat);
    }

    /** Reads data
    * @param in ObjectInputStream
    */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException	{
        lastBundleName = null;
        Object obj = in.readObject();
        if (obj instanceof String) {
            lastBundleName = (String)obj;
        }
        ResourceBundleStringEditor.javaStringFormat = (String)in.readObject();
    }
    // -----------------------------------------------------------------------------
    // Private methods

    private static void assign (String key, Class actionClass) {
        KeyStroke str = Utilities.stringToKey (key);
        if (str == null) return;

        // create instance of the action
        SystemAction a = SystemAction.get (actionClass);

        TopManager.getDefault ().getGlobalKeymap ().addActionForKeyStroke (str, a);
    }

    private void copyTemplates () {
        try {
            org.openide.filesystems.FileUtil.extractJar (
                org.openide.TopManager.getDefault ().getPlaces ().folders().templates ().getPrimaryFile (),
                getClass ().getClassLoader ().getResourceAsStream ("org/netbeans/modules/properties/templates.jar")
            );
        } catch (java.io.IOException e) {
            org.openide.TopManager.getDefault ().notifyException (e);
        }
    }


}

/*
 * <<Log>>
 */
