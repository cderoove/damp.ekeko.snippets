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
import java.util.*;
import java.util.jar.Manifest;
import javax.swing.SwingUtilities;
import javax.swing.event.*;

import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;

import org.netbeans.modules.jarpackager.*;

public class ModuleDataObject extends JarDataObject {

    private static final long serialVersionUID =4724305481753911436L;
    public ModuleDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException {
        super (pf, loader);
        CookieSet cookies = getCookieSet ();
        cookies.add (new ManifestProviderSupport ());
        cookies.add (new ManifestProvider.ModuleExecSupport (getPrimaryEntry ()));
        // Necessary because JarCreator just writes out new content, does not
        // use setJarContent. Need to actually listen to the file itself.
        findContentFile ().addFileChangeListener (new FileChangeAdapter () {
                    public void fileChanged (FileEvent ev) {
                        ((ManifestProviderSupport) getCookie (ManifestProviderSupport.class)).fireStateChange ();
                    }
                });
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.apisupport.modules");
    }

    protected org.openide.nodes.Node createNodeDelegate () {
        return new ModuleDataNode (this);
    }

    private class ManifestProviderSupport implements ManifestProvider {
        /**
         * @associates ChangeListener 
         */
        private Set listeners = new HashSet (); // Set<ChangeListener>

        private JarContent jc () throws IOException {
            JarContent jarC = getJarContent ();
            if (jarC != null)
                return jarC;
            else
                throw new IOException ("No JAR content found for " + getPrimaryFile ().getPackageNameExt ('/', '.'));
        }

        public synchronized Manifest getManifest () throws IOException {
            return jc ().getManifest ();
        }

        public synchronized void setManifest (Manifest m) throws IOException {
            JarContent content = jc ();
            content.setManifest (m);
            setJarContent (content);
        }

        public synchronized void addFiles (Set files) throws IOException {
            JarContent content = jc ();
            content.putFiles (files);
            setJarContent (content);
        }

        public synchronized void removeFiles (Set files) throws IOException {
            JarContent content = jc ();
            content.removeFiles ((FileObject[]) files.toArray (new FileObject[files.size ()]));
            setJarContent (content);
        }

        public synchronized Set getFiles () throws IOException {
            Enumeration e = jc ().fullContent ();
            Set s = new HashSet ();
            while (e.hasMoreElements ()) s.add (e.nextElement ());
            return s;
        }

        public synchronized void addChangeListener (ChangeListener list) {
            listeners.add (list);
        }

        public synchronized void removeChangeListener (ChangeListener list) {
            listeners.remove (list);
        }

        synchronized void fireStateChange () {
            SwingUtilities.invokeLater (new Runnable () {
                                            public void run () {
                                                ChangeEvent ev = new ChangeEvent (this);
                                                Set _listeners = new HashSet (listeners);
                                                Iterator it = _listeners.iterator ();
                                                while (it.hasNext ())
                                                    ((ChangeListener) it.next ()).stateChanged (ev);
                                            }
                                        });
        }

        public boolean isValid () {
            return true;
        }

        public Exception getParseException () {
            return null;
        }

        public File getManifestAsFile () {
            return null;
        }

    }

}

/*
 * Log
 *  12   Gandalf-post-FCS1.9.2.1     3/28/00  Jesse Glick     More robust module 
 *       install executor.
 *  11   Gandalf-post-FCS1.9.2.0     3/9/00   Jesse Glick     Backport of 1.9.1.0 from
 *       Jaga.
 *  10   Gandalf   1.9         1/26/00  Jesse Glick     Live manifest parsing.
 *  9    Gandalf   1.8         1/26/00  Jesse Glick     Manifest handling 
 *       changed--now more dynamic, synched properly with open document as for 
 *       real file types.
 *  8    Gandalf   1.7         1/22/00  Jesse Glick     Manifest files can now 
 *       be recognized, not just JARs.
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         10/7/99  Jesse Glick     Inexplicable compile 
 *       errors--org.openide.nodes.Node import does not work.
 *  5    Gandalf   1.4         10/6/99  Jesse Glick     Added table of contents,
 *       anchored context help.
 *  4    Gandalf   1.3         9/30/99  Jesse Glick     Package rename and misc.
 *  3    Gandalf   1.2         9/30/99  Jesse Glick     
 *  2    Gandalf   1.1         9/22/99  Jesse Glick     Using regular 
 *       .jarContent extension, and recognizing modules by magic tag.
 *  1    Gandalf   1.0         9/17/99  Jesse Glick     
 * $
 */
