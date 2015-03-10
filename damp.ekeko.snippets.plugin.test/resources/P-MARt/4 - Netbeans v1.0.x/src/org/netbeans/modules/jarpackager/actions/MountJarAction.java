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

import java.io.File;
import java.io.IOException;
import java.beans.PropertyVetoException;

import org.openide.util.actions.NodeAction;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.execution.NbClassPath;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.jarpackager.JarDataObject;
import org.netbeans.modules.jarpackager.util.JarUtils;

/** An action that mounts selected jar into the repository.
*
* @author Dafe Simonek
*/
public class MountJarAction extends NodeAction {

    static final long serialVersionUID =-2074858203953636843L;
    /** Creates new MountJarAction. */
    public MountJarAction() {
    }

    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName () {
        return NbBundle.getBundle(MountJarAction.class).
               getString ("CTL_MountJarAction");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx () {
        return new HelpCtx(MountJarAction.class);
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    /*protected String iconResource () {
      return "/org/netbeans/modules/jarpackager/resources/jarObject.gif";
}*/

    /** Mounts all activated jar files to the repository */
    protected void performAction (Node[] activatedNodes) {
        JarDataObject jdo = null;
        FileObject jarFo = null;
        File jarFile = null;
        for (int i = 0; i < activatedNodes.length; i++) {
            jdo = (JarDataObject)activatedNodes[i].getCookie(JarDataObject.class);
            if (jdo != null) {
                jarFo = jdo.findArchiveFile();
                if (jarFo != null) {
                    jarFile = NbClassPath.toFile(jarFo);
                    if (jarFile != null) {
                        try {
                            JarUtils.addJarFSToRepository(jarFile, false);
                        } catch (IOException exc) {
                            notifyMountError(exc);
                        } catch (PropertyVetoException exc) {
                            notifyMountError(exc);
                        }
                    }
                }
            }
        }
    }

    private static void notifyMountError (Exception exc) {
        if (System.getProperty("netbeans.debug.exceptions") != null) {
            exc.printStackTrace();
        }
        TopManager.getDefault().notify(new NotifyDescriptor.Exception(
                                           exc,
                                           NbBundle.getBundle(MountJarAction.class).getString("MSG_MountFail")
                                       ));
    }

    /** Enables this action only if selected jar is not mounted
    * in repository */
    protected boolean enable (Node[] activatedNodes) {
        // enable only when not mounted already
        JarDataObject jdo = null;
        FileObject jarFo = null;
        File jarFile = null;
        for (int i = 0; i < activatedNodes.length; i++) {
            jdo = (JarDataObject)activatedNodes[i].getCookie(JarDataObject.class);
            if (jdo == null) {
                return false;
            }
            jarFo = jdo.findArchiveFile();
            if (jarFo == null) {
                return false;
            }
            jarFile = NbClassPath.toFile(jarFo);
            if ((jarFile == null) || (JarUtils.getMountedJarFS(jarFile) != null)) {
                return false;
            }
        }
        return true;
    }

}

/*
* <<Log>>
*  2    Gandalf   1.1         11/27/99 Patrik Knakal   
*  1    Gandalf   1.0         11/9/99  David Simonek   
* $ 
*/ 
