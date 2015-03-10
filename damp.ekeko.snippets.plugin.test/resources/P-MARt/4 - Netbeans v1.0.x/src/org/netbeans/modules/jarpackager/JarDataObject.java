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

import java.awt.Image;
import java.awt.Toolkit;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;

import org.openide.*;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.CompilerCookie;
import org.openide.cookies.ExecCookie;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerJob;
import org.openide.execution.Executor;
import org.openide.execution.ExecInfo;
import org.openide.execution.NbClassPath;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.windows.*;
import org.openide.actions.OpenAction;
import org.openide.text.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.Children;

import org.netbeans.modules.jarpackager.actions.ManageJarAction;
import org.netbeans.modules.jarpackager.options.JarPackagerOption;
import org.netbeans.modules.jarpackager.util.JarUtils;

/** Object representing one JAR file that can be updated.
*
* @author Dafe Simonek, Jaroslav Tulach
*/
public class JarDataObject extends MultiDataObject
            implements OpenCookie,
            CompilerCookie.Compile,
    CompilerCookie.Build {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 2983457824351249776L;

    static final String EA_JAR_CONTENT = "NetBeans-JarContent"; // NOI18N

    /** Constructs new JarDataObject.
    */
    public JarDataObject(final FileObject obj,final MultiFileLoader loader) throws DataObjectExistsException {
        super (obj, loader);
        getCookieSet().add(new ExecSupport(getPrimaryEntry()));
    }

    /** Opens the Jar file. (Implementation of the open cookie)
    */
    public void open () {
        // PENDING - FileObject ---> File --> if exists ---> add to repository
    }

    /** Accepts depth one.
    * @return <code>true</code> if this cookie supports depth one
    */
    public boolean isDepthSupported (Compiler.Depth depth) {
        return Compiler.DEPTH_ONE.equals(depth);
    }

    /** Creates jar compiler with given job.
    */
    public void addToJob (CompilerJob job, Compiler.Depth depth) {
        // obtain file object of archive (create if not exist)
        FileObject contentFo = findContentFile();
        String archiveExt = ((JarDataLoader)getLoader()).getArchiveExt();
        FileObject fo = FileUtil.findBrother(contentFo, archiveExt);
        if (fo == null) {
            try {
                fo = contentFo.getParent().createData(contentFo.getName(), archiveExt);
            } catch (IOException exc) {
                TopManager.getDefault().notify(new NotifyDescriptor.Exception(
                                                   exc,
                                                   NbBundle.getBundle(JarDataObject.class).
                                                   getString("MSG_CreateArchiveError"))
                                              );
                return;
            }
        }
        JarContent jc = getJarContent();
        // do not compile if jar content is not valid
        if (jc == null) {
            return;
        }
        // update manifest and target file
        JarUtils.updateManifest(jc);
        if (jc.getTargetFile() == null) {
            jc.setTargetFile(NbClassPath.toFile(fo));
        }
        try {
            setJarContent(jc);
        } catch (IOException exc) {
            TopManager.getDefault().notify(new NotifyDescriptor.Exception(
                                               exc,
                                               NbBundle.getBundle(JarDataObject.class).
                                               getString("MSG_CreateArchiveError"))
                                          );
            return;
        }
        // add compiler which manages compilation
        job.add(new JarCompiler(fo, jc));
    }

    /** Sets the description of the jar content to given filedata object.
    * Jar content description will be stored in the extended attributes,
    * allowing to repackage archive whenever needed.
    */   
    public void setJarContent (JarContent jarContent) throws IOException {
        FileObject jcFile = findContentFile();
        // write jar content
        FileLock lock = null;
        try {
            if (jcFile == null) {
                // creste jar content information file
                //System.out.println("Name: " + pfo.getPackageName('/')); // NOI18N
                FileObject pfo = getPrimaryFile();
                jcFile = pfo.getParent().createData(
                             pfo.getName(),
                             JarPackagerOption.singleton().getContentExt()
                         );
            }
            lock = jcFile.lock();
            ObjectOutputStream oos =
                new ObjectOutputStream(jcFile.getOutputStream(lock));
            try {
                jarContent.writeContent(oos);
            } finally {
                oos.close();
            }
        } finally {
            if (lock != null)
                lock.releaseLock();
        }
    }

    /** @return new instance of current description of the jar content.
    * or null if jar content cannot be found for some reason.
    * Note that if you wish to modify jar content, you will have to call
    * setJarContent() after modification */
    public JarContent getJarContent () {
        FileObject jcFile = findContentFile();
        if (jcFile == null) {
            //System.out.println("Nenaseeeeel..."); // NOI18N
            return null;
        }
        JarContent jc = new JarContent();
        try {
            // read jar content
            ObjectInputStream ois = new ObjectInputStream(jcFile.getInputStream());
            try {
                jc.readContent(ois);
            } finally {
                ois.close();
            }
        } catch (IOException exc) {
            return null;
        } catch (ClassNotFoundException exc) {
            return null;
        }
        return jc;
    }

    /** @return file object of jar content file describing archive
    * or null if no content file can be found.
    * Subclasses can override this to provide their own
    * content file */
    protected FileObject findContentFile () {
        return getPrimaryFile();
    }

    /** Utility method, returns file object of the archive or
    * null if no archive exist */
    public FileObject findArchiveFile () {
        FileObject contentFo = findContentFile();
        return FileUtil.findBrother(
                   contentFo,
                   ((JarDataLoader)getLoader()).getArchiveExt()
               );
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
        return new JarNode(this);
    }

    /** Help context for this object.
    * @return help context
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(JarDataObject.class);
    }


    /** Jar data Node implementation.
    * Leaf node, icons and name redefined.
    */
    public static class JarNode extends DataNode {
        /** Icon base for the JarNode node */
        static final String JAR_ICON_BASE =
            "org/netbeans/modules/jarpackager/resources/jarObject"; // NOI18N

        /** Default constructor, constructs node */
        public JarNode (DataObject dataObject) {
            this(dataObject, Children.LEAF);
        }

        /** Constructs node with specified data object and with given
        * children */
        public JarNode (DataObject dataObject, Children children) {
            super(dataObject, children);
            setIconBase (JAR_ICON_BASE);
            setDefaultAction (SystemAction.get(ManageJarAction.class));
        }

        /** Create the property sheet for this node.
        * @return the sheet
        */
        protected Sheet createSheet () {
            Sheet sheet = super.createSheet();
            // create execution set
            Sheet.Set ps = new Sheet.Set ();
            ps.setName(
                NbBundle.getBundle(JarDataObject.class).getString("CTL_ExecutionSet")
            );
            ps.setDisplayName(NbBundle.getBundle(JarDataObject.JarNode.class).
                              getString("PROP_executionSetName"));
            ps.setShortDescription(NbBundle.getBundle(JarDataObject.JarNode.class).
                                   getString("HINT_executionSetName"));
            // fill execution properties
            ((ExecSupport) getCookie (ExecSupport.class)).addProperties (ps);
            ps.remove(ExecSupport.PROP_FILE_PARAMS);
            ps.remove(ExecSupport.PROP_DEBUGGER_TYPE);
            sheet.put(ps);
            return sheet;
        }


    } // end of JarNode inner class
}

/*
 * <<Log>>
 *  24   Gandalf   1.23        1/25/00  David Simonek   Various bugfixes and 
 *       i18n
 *  23   Gandalf   1.22        1/16/00  David Simonek   i18n
 *  22   Gandalf   1.21        11/9/99  David Simonek   addToJob bug fixed 
 *       (concerning null jar content)
 *  21   Gandalf   1.20        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems copyright in file comment
 *  20   Gandalf   1.19        10/14/99 David Simonek   manifest updating 
 *       bugfixes
 *  19   Gandalf   1.18        10/13/99 David Simonek   jar content now primary 
 *       file, other small changes
 *  18   Gandalf   1.17        10/7/99  Jesse Glick     Fixed problem with 
 *       execution sheet being blank.
 *  17   Gandalf   1.16        10/4/99  David Simonek   
 *  16   Gandalf   1.15        9/16/99  David Simonek   a lot of bugfixes (RE 
 *       filters, empty jar content etc)  added templates
 *  15   Gandalf   1.14        9/13/99  David Simonek   bugfixes, compressed 
 *       on/off support fixed
 *  14   Gandalf   1.13        9/8/99   David Simonek   new version of jar 
 *       packager
 *  13   Gandalf   1.12        8/17/99  David Simonek   installations of 
 *       actions, icon changing
 *  12   Gandalf   1.11        8/1/99   David Simonek   automatic file list 
 *       generation to the manifest added
 *  11   Gandalf   1.10        7/11/99  David Simonek   
 *  10   Gandalf   1.9         6/10/99  David Simonek   progress indocator + 
 *       minor bugfixes....
 *  9    Gandalf   1.8         6/10/99  David Simonek   progress dialog now 
 *       functional
 *  8    Gandalf   1.7         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  7    Gandalf   1.6         6/8/99   David Simonek   
 *  6    Gandalf   1.5         6/8/99   David Simonek   bugfixes....
 *  5    Gandalf   1.4         6/4/99   Petr Hamernik   temporary version
 *  4    Gandalf   1.3         6/4/99   David Simonek   executor properties set 
 *       added
 *  3    Gandalf   1.2         6/4/99   David Simonek   
 *  2    Gandalf   1.1         6/4/99   David Simonek   compile action on jar 
 *       data object
 *  1    Gandalf   1.0         6/4/99   Jaroslav Tulach 
 * $
 * Beta Change History:
 *  0    Tuborg    0.54        --/--/98 Jan Formanek    reflecting changes in cookies
 *  0    Tuborg    0.55        --/--/98 Jan Formanek    templates
 *  0    Tuborg    0.56        --/--/98 Petr Hamernik   discarding doc.
 *  0    Tuborg    0.57        --/--/98 Petr Hamernik   locking changed
 */
