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

package org.netbeans.modules.jarpackager;

import java.awt.BorderLayout;
import java.io.*;

import javax.swing.ImageIcon;

import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.util.NbBundle;

import org.netbeans.modules.jarpackager.util.VersionSerializator;
import org.netbeans.modules.jarpackager.options.JarPackagerOption;

/** Top component that manages packaging into jar files.
*
* @author Dafe Simonek
*/
public final class PackagingView extends TopComponent {

    // Attributes

    /** The only instance of the packaging view in the system */
    static PackagingView defaultView;

    /** Manager for the serialization */
    private static VersionSerializator serializationManager;

    /** true if we have our gui (content) fully initialized, false otherwise */
    private boolean guiInitialized;

    /** Content (gui) of this view (composition) */
    PackagingPanel content;

    /** Content of currently managed archive */
    JarContent jc;

    /** Creates new PackagingView. Singletonb pattern.
    * Use getPackagingView() method to obtain singleton instance. */
    private PackagingView () {
        super();
        guiInitialized = false;
        initialize();
    }

    /** Peforms initialization; name and icon */
    private void initialize () {
        jc = new JarContent();
        setName(NbBundle.getBundle(PackagingView.class).
                getString("CTL_PackagerActionTitle"));
        setIcon(new ImageIcon(getClass().getResource(
                                  "/org/netbeans/modules/jarpackager/resources/jarObject.gif")). // NOI18N
                getImage());
        setLayout(new BorderLayout());
    }

    /** Initializes the Gui, the content */
    private void initializeGui () {
        //System.out.println("initing gui of packaging view..."); // NOI18N
        content = new PackagingPanel();
        content.initContentType();
        add(content, BorderLayout.CENTER);
        content.setJarContent(jc);
    }

    /** Returns the only instance of packaging view in the system.
    */
    public static PackagingView getPackagingView () {
        //clearPackagingView();
        if (defaultView == null) {
            synchronized (PackagingView.class) {
                if (defaultView == null) {
                    defaultView = new PackagingView();
                }
            }
        }
        return defaultView;
    }

    /** Clears singleton instance of packaging view. */
    static void clearPackagingView () {
        synchronized (PackagingView.class) {
            defaultView = null;
        }
    }

    /** Overrides parent method. Forces right editor kit to be
    * used for manifest file editing */
    public void open (Workspace workspace) {
        if (!guiInitialized) {
            initializeGui();
            guiInitialized = true;
        }
        super.open(workspace);
    }

    /** @return Current jar content of packaging top component */
    public JarContent getJarContent () {
        if (guiInitialized) {
            jc = content.getJarContent();
        }
        return jc;
    }

    /** Sets given jar content to this top component and asociates
    * it with given target file. Top component
    * will update its visual state according to the new jar content
    * desription.
    * @param targetFile destination file for the archive
    * (can be null, equivalent to setJarContent(JarContent) call).
    * @param jc Jar content describing the content of the archive
    * @deprecated use combination of JarContent.setTargetFile(..) and
    * setJarContent(JarContent) instead.
    * @see #setJarContent
    */  
    public void setJarContent (File targetFile, JarContent jc) {
        // modify jar content if needed
        if (targetFile != null) {
            jc.setTargetFile(targetFile);
        }
        setJarContent(jc);
    }

    /** Sets given jar content to this top component. Top component
    * will update its visual state according to the new jar content
    * desription.
    * @param jc Jar content describing the content of the archive
    */  
    public void setJarContent (JarContent jc) {
        this.jc = jc;
        if (guiInitialized) {
            content.setJarContent(jc);
        }
    }

    /** Accessor to the versioned serialization manager */
    private static VersionSerializator serializationManager () {
        if (serializationManager == null) {
            serializationManager = new VersionSerializator();
            serializationManager.putVersion(new Version1());
        }
        return serializationManager;
    }

    /** Serializes jar packager -> writes Replacer object which
    * holds jar content. */
    protected Object writeReplace ()
    throws java.io.ObjectStreamException {
        // update target file field first
        JarContent jc = getJarContent();
        String targetPath = content.normalizedTargetPath();
        File targetFile = jc.getTargetFile();
        if ((targetPath != null) &&
                ((targetFile == null) ||
                 (!targetPath.equals(targetFile.getPath())))) {
            jc.setTargetFile(new File(targetPath));
        }
        return new Replacer();
    }

    /** The class which is serialized instead of PackagingView.
    * It holds all information which should be serialized for PackagingView
    * and on deserialization, it deserializes back to singleton instance
    * of the PackagingView */
    private static final class Replacer implements java.io.Serializable {
        /** serial version UID */
        static final long serialVersionUID = 2230042447556712133L;

        private void readObject (ObjectInputStream ois)
        throws IOException, ClassNotFoundException {
            serializationManager().readVersion(ois);
        }

        private void writeObject (ObjectOutputStream oos)
        throws IOException {
            serializationManager().writeLastVersion(oos);
        }

        private Object readResolve ()
        throws java.io.ObjectStreamException {
            // return singleton instance
            return getPackagingView();
        }

    } // end of inner Replacer inner class

    /** Basic version of persistence state for jar packager view */
    private static final class Version1 implements VersionSerializator.Versionable {

        /** Identification of the version */
        public String getName () {
            return "Version_1.0"; // NOI18N
        }

        /** read the data of the version from given input */
        public void readData (ObjectInput in)
        throws IOException, ClassNotFoundException {
            JarContent jc = new JarContent();
            jc.readContent(in);
            getPackagingView().setJarContent(jc);
        }

        /** write the data of the version to given output */
        public void writeData (ObjectOutput out)
        throws IOException {
            getPackagingView().getJarContent().writeContent(out);
        }

    }


}

/*
* <<Log>>
*  19   Gandalf   1.18        1/26/00  David Simonek   Minor changes concerning 
*       correct action installation / removal
*  18   Gandalf   1.17        1/25/00  David Simonek   Various bugfixes and i18n
*  17   Gandalf   1.16        1/16/00  David Simonek   i18n
*  16   Gandalf   1.15        12/7/99  David Simonek   
*  15   Gandalf   1.14        11/11/99 David Simonek   add to jar action failure
*       repaired
*  14   Gandalf   1.13        11/10/99 David Simonek   testing code removed
*  13   Gandalf   1.12        11/6/99  David Simonek   L&F bugfixes
*  12   Gandalf   1.11        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems copyright in file comment
*  11   Gandalf   1.10        9/16/99  David Simonek   a lot of bugfixes (RE 
*       filters, empty jar content etc)  added templates
*  10   Gandalf   1.9         9/8/99   David Simonek   new version of jar 
*       packager
*  9    Gandalf   1.8         8/19/99  Ian Formanek    Better UI (border around)
*  8    Gandalf   1.7         8/19/99  Ian Formanek    Proper texts 
*       capitalization
*  7    Gandalf   1.6         8/17/99  David Simonek   installations of actions,
*       icon changing
*  6    Gandalf   1.5         6/10/99  David Simonek   progress dialog now 
*       functional
*  5    Gandalf   1.4         6/9/99   David Simonek   bugfixes, progress 
*       dialog, compiling progress..
*  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  3    Gandalf   1.2         6/8/99   David Simonek   
*  2    Gandalf   1.1         6/8/99   David Simonek   bugfixes....
*  1    Gandalf   1.0         6/3/99   David Simonek   
* $
*/