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

package org.netbeans.modules.text;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;

import org.openide.*;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.windows.*;
import org.openide.actions.OpenAction;
import org.openide.text.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
//import org.netbeans.modules.editor.EditorBase;


/** Object that provides main functionality for txt data loader.
* This class is final only for performance reasons,
* can be unfinaled if desired.
*
* @author Petr Hamernik
*/
public final class TXTDataObject extends MultiDataObject {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4795737295255253334L;


    public TXTDataObject (final FileObject obj, final MultiFileLoader loader)
    throws DataObjectExistsException {
        super (obj, loader);
        // use editor support
        EditorSupport es = new EditorSupport(getPrimaryEntry());
        es.setMIMEType ("text/plain"); // NOI18N
        getCookieSet().add(es);
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
        return new TXTNode(this);
    }

    /** Help context for this object.
    * @return help context
    */
    public org.openide.util.HelpCtx getHelpCtx () {
        return new org.openide.util.HelpCtx (TXTDataObject.class);
    }


    /** @return action controller
    * (if it hadn't created yet this function creates it) */
    /*private ActionController getActionController() {
      if (actionController == null) {
        actionController = new ActionController() {
          public void allowInsert(AllowChangeEvent e) throws NotAllowedException {
            commonImpl(this);
          }
          public void allowDelete(AllowChangeEvent e) throws NotAllowedException {
            commonImpl(this);
          }
          public void allowMultiDelete(AllowChangeEvent e) throws NotAllowedException {
            commonImpl(this);
          }
        };
      }
      return actionController;
}*/

    /** Try to lock primary file and remove given ActionController from the Document.
    * @param controller calling controller itself
    */
    /*void commonImpl(ActionController controller) throws NotAllowedException {
      try {
        editorLock = takePrimaryFileLock();
        doc.removeActionController(controller);
        setModified(true);
        TopManager.getDefault().setStatusText("");
      }
      catch (IOException e) {
        throw new NotAllowedException (
          NbBundle.getBundle(this).getString("MSG_DocReadOnly"));
      }
}*/

    /** Deals with deleting of the object.
    * @exception IOException if an error occures
    */
    /*protected void handleDelete () throws IOException {
      setModified(false);
      if (editorLock != null) {
        editorLock.releaseLock();
        editorLock = null;
      }
      Enumeration en = editors.getComponents();
      while (en.hasMoreElements()) {
        TXTEditor comp = (TXTEditor)en.nextElement();
        comp.close();
        comp.removeFromFrame();
      }
      discard();
      super.handleDelete();
}*/

    /** Discards the loaded text. Called from the TXTEditor.closeLast() */
    /*void discard() {
      if (doc != null) {
        doc.removeActionController(actionController);
        try {
          doc.delete(this);
        }
        catch (NotAllowedException e) {
        }
      }
      setModified(false);
      doc = null;
}*/

    /** TXT Node implementation.
    * Leaf node, default action opens editor or instantiates template.
    * Icons redefined.
    */
    public static final class TXTNode extends DataNode {
        /** Icon base for the TXTNode node */
        private static final String TXT_ICON_BASE =
            "org/netbeans/modules/text/txtObject"; // NOI18N

        /** Default constructor, constructs node */
        public TXTNode (final DataObject dataObject) {
            super(dataObject, Children.LEAF);
            setIconBase(TXT_ICON_BASE);
        }

        /** Overrides default action from DataNode.
        * Instantiate a template, if isTemplate() returns true.
        * Opens otherwise.
        */
        public SystemAction getDefaultAction () {
            SystemAction result = super.getDefaultAction();
            return result == null ? SystemAction.get(OpenAction.class) : result;
        }
    } // end of TXTNode inner class

}

/*
 * Log
 *  16   Gandalf   1.15        1/5/00   Ian Formanek    NOI18N
 *  15   Gandalf   1.14        12/7/99  Jaroslav Tulach Deleted old commented 
 *       out code.
 *  14   Gandalf   1.13        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  13   Gandalf   1.12        7/15/99  Jesse Glick     Forcing text/plain for 
 *       the editor on text-like files. Otherwise adding strange extensions to 
 *       the Text loader results in their being opened with default editor kit 
 *       (perhaps).
 *  12   Gandalf   1.11        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  11   Gandalf   1.10        6/22/99  Ian Formanek    employed DEFAULT_HELP
 *  10   Gandalf   1.9         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  9    Gandalf   1.8         5/12/99  Ales Novak      PrintSupport removed
 *  8    Gandalf   1.7         5/6/99   Ales Novak      print
 *  7    Gandalf   1.6         3/27/99  Ian Formanek    Removed obsoleted import
 *  6    Gandalf   1.5         3/9/99   Ian Formanek    images moved to this 
 *       package
 *  5    Gandalf   1.4         1/15/99  David Simonek   
 *  4    Gandalf   1.3         1/15/99  Petr Hamernik   
 *  3    Gandalf   1.2         1/13/99  David Simonek   
 *  2    Gandalf   1.1         1/7/99   David Simonek   
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.54        --/--/98 Jan Formanek    reflecting changes in cookies
 *  0    Tuborg    0.55        --/--/98 Jan Formanek    templates
 *  0    Tuborg    0.56        --/--/98 Petr Hamernik   discarding doc.
 *  0    Tuborg    0.57        --/--/98 Petr Hamernik   locking changed
 */
