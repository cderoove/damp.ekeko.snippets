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

package org.netbeans.modules.jarpackager.actions;

import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.beans.PropertyVetoException;

import org.openide.util.actions.NodeAction;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.nodes.Node;
import org.openide.execution.NbClassPath;

import org.netbeans.modules.jarpackager.JarDataObject;
import org.netbeans.modules.jarpackager.JarContent;
import org.netbeans.modules.jarpackager.PackagingView;
import org.netbeans.modules.jarpackager.util.ProgressListener;
import org.netbeans.modules.jarpackager.util.ProgressDialog;
import org.netbeans.modules.jarpackager.util.JarUtils;
import org.netbeans.modules.jarpackager.util.JarInspector;
import org.netbeans.modules.jarpackager.options.JarPackagerOption;

/** This actions opens managing jar packager view on given
* selected jar archive node.
* If additional information needed to manage archive cannot be found,
* action tries to inspect the archive and compute additional information
* about the archive.
*
* @author Dafe Simonek
*/
public class ManageJarAction extends NodeAction {

    static final long serialVersionUID =3994272970224624862L;
    /** Actually performs the action of opening the
    * jar content manager view of selected node in a case jar
    * content informations are available.
    * @param activatedNodes The nodes on which to perform the action.
    */
    protected void performAction (Node[] activatedNodes) {
        JarDataObject jdo =
            (JarDataObject)activatedNodes[0].getCookie(JarDataObject.class);
        JarContent jc = jdo.getJarContent();
        // testing ...
        /*JarContent content = new JarContent();
        content.setMainAttributes(false);
        content.setManifestFileList(true);
        java.util.jar.Manifest manifest = new java.util.jar.Manifest();
        java.util.Map entries = manifest.getEntries();
        java.util.jar.Attributes attr = new java.util.jar.Attributes();
        attr.putValue("Enterprise-Bean", "True");
        entries.put("debile/nazdar/necum.txt", attr);
        content.setManifest(manifest);
        content.putFile(jdo.getPrimaryFile().getParent());
        try {
          jdo.setJarContent(content);
    } catch (IOException exc) {
          exc.printStackTrace();
    }
        jc = jdo.getJarContent();
        System.out.println("MAIN ATTRS: " + jc.isMainAttributes());
        */
        // end of testing

        if (jc != null) {
            // open packaging view
            PackagingView pv = PackagingView.getPackagingView();
            pv.setJarContent(jc);
            pv.open();
        }
    }

    /** Enables this action only if jar content
    * information is available */
    protected boolean enable (Node[] activatedNodes) {
        // only for one node
        if (activatedNodes.length != 1) {
            return false;
        }
        // enable only when jarContent exists
        return JarUtils.jarContentFromNode(activatedNodes[0]) != null;
    }

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return NbBundle.getBundle (JarPackagerAction.class).
               getString ("CTL_ManageJar");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(ManageJarAction.class);
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "/org/netbeans/modules/jarpackager/resources/manageJar.gif"; // NOI18N
    }

    /** Inspects given jar file and produces jar content.
    * Implemented to enable running in separate thread. */
    private static final class Inspector implements Runnable, ProgressListener {
        File jarFile;
        DialogUpdater dialogUpdater;
        JarDataObject jdo;
        ProgressDialog progressDlg;

        Inspector (File jarFile, JarDataObject jdo) {
            this.jarFile = jarFile;
            this.jdo = jdo;
        }

        public void run () {
            // show progress dialog
            progressDlg = new ProgressDialog(
                              NbBundle.getBundle(ManageJarAction.class).getString("CTL_InspectingTitle"), // title
                              0, // min value
                              100 // max value
                          );
            SwingUtilities.invokeLater(new Runnable() {
                                           public void run () {
                                               progressDlg.show();
                                           }
                                       });
            // inspect and produce jarcontent
            JarInspector ji = new JarInspector(jarFile);
            ji.addProgressListener(this);
            JarContent result = null;
            try {
                result = ji.createContent();
                jdo.setJarContent(result);
            } catch (IOException exc) {
                // PENDING
            } catch (PropertyVetoException exc) {
                // PENDING - tady jsem skoncil
            } finally {
                ji.removeProgressListener(this);
                // close progress dialog
                SwingUtilities.invokeLater(new Runnable() {
                                               public void run () {
                                                   progressDlg.dispose();
                                               }
                                           });
            }
            // open packaging view
            PackagingView pv = PackagingView.getPackagingView();
            pv.setJarContent(result);
            pv.open();
        }

        /** update the values in the progress dialog */
        public void progress (int percent, String description) {
            if (dialogUpdater == null) {
                dialogUpdater = new DialogUpdater(progressDlg);
            }
            dialogUpdater.setValues(percent, description);
            SwingUtilities.invokeLater(dialogUpdater);
        }

    } // end of Inspector inner class

    /** Update progress dialog. Should be runned in the AWT thread.
    * It exists here to prevent from creating of new instance in
    * every Inspector.progress() call. */
    private static final class DialogUpdater implements Runnable {
        int percent;
        String description;
        ProgressDialog progressDlg;

        public DialogUpdater (ProgressDialog progressDlg) {
            this.progressDlg = progressDlg;
        }

        /** Sets new values to update the dialog with */
        public void setValues (int percent, String description) {
            this.percent = percent;
            this.description = description;
        }

        /** updates the dialog (should be runned in AWT thread ) */
        public void run () {
            progressDlg.setValue(percent);
            progressDlg.setLabel(description);
        }

    } // end of DialogUpdater inner cladd

}

/*
* <<Log>>
*  13   Gandalf   1.12        1/16/00  David Simonek   i18n
*  12   Gandalf   1.11        11/27/99 Patrik Knakal   
*  11   Gandalf   1.10        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems copyright in file comment
*  10   Gandalf   1.9         10/13/99 David Simonek   various bugfixes 
*       concerning primarily manifest
*  9    Gandalf   1.8         10/13/99 David Simonek   jar content now primary 
*       file, other small changes
*  8    Gandalf   1.7         10/4/99  David Simonek   
*  7    Gandalf   1.6         8/18/99  David Simonek   stupid bugs fixes
*  6    Gandalf   1.5         8/17/99  David Simonek   installations of actions,
*       icon changing
*  5    Gandalf   1.4         7/11/99  David Simonek   
*  4    Gandalf   1.3         6/9/99   David Simonek   bugfixes, progress 
*       dialog, compiling progress..
*  3    Gandalf   1.2         6/9/99   Ian Formanek    Fixed resources for 
*       package change
*  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         6/8/99   David Simonek   
* $
*/