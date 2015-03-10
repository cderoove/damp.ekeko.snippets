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

package org.netbeans.modules.makefile;

import org.openide.actions.OpenAction;
import org.openide.compiler.CompilerType;
import org.openide.execution.Executor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.text.EditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/** Object that provides main functionality for Makefile data loader.
*
* @author Libor Kramolis, Jesse Glick
*/
public class MakefileDataObject extends MultiDataObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 2096503267128764035L;

    /** Create a new makefile object.
     * @param obj the makefile
     * @param loader the makefile loader
     * @throws DataObjectExistsException standard exception
     */
    public MakefileDataObject (final FileObject obj, final UniFileLoader loader)
    throws DataObjectExistsException {
        super (obj, loader);
        EditorSupport ed = new EditorSupport (getPrimaryEntry ());
        ed.setMIMEType ("text/plain");
        getCookieSet().add (ed);
        getCookieSet ().add (new CompilerSupport.Compile (getPrimaryEntry ()) {
                                 protected CompilerType defaultCompilerType () {
                                     return CompilerType.find (MakefileCompilerType.class);
                                 }
                             });
        getCookieSet ().add (new CompilerSupport.Build (getPrimaryEntry ()) {
                                 protected CompilerType defaultCompilerType () {
                                     return CompilerType.find (MakefileCompilerType.class);
                                 }
                             });
        getCookieSet ().add (new CompilerSupport.Clean (getPrimaryEntry ()) {
                                 protected CompilerType defaultCompilerType () {
                                     return CompilerType.find (MakefileCompilerType.class);
                                 }
                             });
        getCookieSet ().add (new ExecSupport (getPrimaryEntry ()) {
                                 protected Executor defaultExecutor () {
                                     return Executor.find (MakefileExecutor.class);
                                 }
                             });
    }

    /** Create a makefile node.
     * @return the node
     */
    protected Node createNodeDelegate () {
        return new MakefileNode (this);
    }

    /** Get context help for the object/node.
     * @return general help
     */
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.makefile.overview");
    }

    /*
    public String getName () {
      FileObject fo = getPrimaryFile();
      String name = fo.getName();
      String ext = fo.getExt();
      if (ext.length() == 0)
        return name;
      return name + "." + ext;
}
    */

    /** Makefile Node implementation.
    * Leaf node, default action opens editor or instantiates template.
    * Icons redefined.
    */
    public static class MakefileNode extends DataNode {
        /** Icon base for the MakefileNode node */
        private static final String MAKEFILE_ICON_BASE =
            "org/netbeans/modules/makefile/makefileObject";

        /** Create the node.
         * @param dob a data object
         */
        public MakefileNode (DataObject dob) {
            this (dob, Children.LEAF);
        }

        /** Create the node with special children.
         * @param dataObject the data object
         * @param ch children to use
         */
        public MakefileNode (DataObject dataObject, Children ch) {
            super (dataObject, ch);
            setIconBase (MAKEFILE_ICON_BASE);
            setDefaultAction (SystemAction.get (OpenAction.class));
        }

        /** Create the initial property sheet.
         * Adds an execution tab to the <CODE>DataNode</CODE> standard.
         * @return the custom sheet
         */
        protected Sheet createSheet () {
            Sheet sheet = super.createSheet ();
            Sheet.Set set = sheet.get (ExecSupport.PROP_EXECUTION);
            if (set == null) {
                set = new Sheet.Set ();
                set.setName (ExecSupport.PROP_EXECUTION);
                set.setDisplayName (NbBundle.getBundle (MakefileDataObject.class).getString ("LBL_ExecutionSheet"));
                set.setShortDescription (NbBundle.getBundle (MakefileDataObject.class).getString ("HINT_ExecutionSheet"));
            }
            ((ExecSupport) getCookie (ExecSupport.class)).addProperties (set);
            set.remove (ExecSupport.PROP_DEBUGGER_TYPE);
            ((CompilerSupport) getCookie (CompilerSupport.class)).addProperties (set);
            sheet.put (set);
            return sheet;
        }

    }

}
