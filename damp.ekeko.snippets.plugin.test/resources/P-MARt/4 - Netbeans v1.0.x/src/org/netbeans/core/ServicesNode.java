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

package org.netbeans.core;

import java.awt.datatransfer.Transferable;
import java.awt.Image;
import java.beans.*;
import java.util.*;
import java.text.MessageFormat;

import org.openide.nodes.*;
import org.openide.ServiceType;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.InstanceSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.io.NbMarshalledObject;

//import org.netbeans.core.execution.*;

/** Node that represents the executors.
*
* @author Jaroslav Tulach
*/
final class ServicesNode extends AbstractNode {
    /** instance */
    private static final ServicesNode INSTANCE = new ServicesNode ();
    /** default icons for SubLevel (Debugger Types, Executor Types, etc.) */
    private static final String ICON_SUB_LEVEL = "org/netbeans/core/resources/subLevel"; // NOI18N

    private static final String ICON_TYPE_LEVEL = "org/netbeans/core/resources/typeLevel"; // NOI18N

    /** Creates new ServicesNode */
    private ServicesNode() {
        super (Services.FIRST);
        getCookieSet ().add (new InstanceSupport.Instance (
                                 Services.getDefault ()
                             ));
    }

    /** Default node. BeanNode
    */
    public static Node getDefault () {
        return INSTANCE;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ServicesNode.class);
    }

    /** Node that displayes informations about one class.
    */
    static class ClassNode extends AbstractNode {
        protected Class clazz;
        protected BeanInfo bi;

        public ClassNode (Children ch, Class clazz) throws InstantiationException {
            super (ch);

            this.clazz = clazz;

            try {
                bi = Introspector.getBeanInfo (clazz);
            } catch (IntrospectionException ex) {
                throw new InstantiationException (ex.getMessage ());
            }

            setName (clazz.getName ());
            setDisplayName (bi.getBeanDescriptor ().getDisplayName ());
            setShortDescription (bi.getBeanDescriptor ().getShortDescription ());
        }

        public Image getIcon (int type) {
            Image i = bi.getIcon (type);
            if (i == null) {
                i = super.getIcon (type);
            }
            return i;
        }

        public Image getOpenedIcon (int type) {
            return getIcon (type);
        }

        public boolean canCopy () { return false; }
        public boolean canCut () { return false; }
        public boolean canRename () { return false; }
        public boolean canDestroy () { return false; }

    }

    /** Node that represents one direct subclass of a ServiceType
    */
    static final class SubLevel extends ClassNode {
        public SubLevel (Class clazz) throws InstantiationException {
            super (new Services.TypeLevel (), clazz);

            // icon for types that do not define own
            setIconBase (ICON_SUB_LEVEL);

            getCookieSet ().add ((Index)getChildren ());
        }

        /** Finds help associated with the variety class acc. to the BeanDescriptor. */
        public HelpCtx getHelpCtx () {
            return InstanceSupport.findHelp(new InstanceCookie () {
                                                public String instanceName () {
                                                    return clazz.getName ();
                                                }
                                                public Class instanceClass () {
                                                    return clazz;
                                                }
                                                public Object instanceCreate () {
                                                    return null;
                                                }
                                            });
        }

        /** Actions.
        */
        protected SystemAction[] createActions () {
            return new SystemAction[] {
                       SystemAction.get (org.openide.actions.ReorderAction.class),
                       null,
                       SystemAction.get (org.openide.actions.ToolsAction.class),
                       SystemAction.get (org.openide.actions.PropertiesAction.class)
                   };
        }

    }

    /** Node that represents type (subclass of a ServiceType
    */
    static final class TypeLevel extends ClassNode {
        public TypeLevel (Class clazz) throws InstantiationException {
            super (new Services.InstanceLevel (), clazz);

            // icon for types that do not define own
            setIconBase (ICON_TYPE_LEVEL);

            getCookieSet ().add (((Services.InstanceLevel) getChildren ()).getIndex ());
        }

        /** Normally, gets help from the default instance. */
        public HelpCtx getHelpCtx () {
            Node[] children = getChildren ().getNodes ();
            if (children.length > 0) return children[0].getHelpCtx ();
            Node parent = getParentNode ();
            if (parent != null)
                return parent.getHelpCtx ();
            else
                return HelpCtx.DEFAULT_HELP;
        }

        /** New types.
        */
        public NewType[] getNewTypes () {
            return new NewType[] { new NewType () {
                                       public String getName () {
                                           return Main.getString ("LAB_NewExecutor_Instantiate", getDisplayName ());
                                       }
                                       public HelpCtx getHelpCtx () {
                                           return new HelpCtx (TypeLevel.class.getName () + ".newFresh"); // NOI18N
                                       }
                                       public void create () throws java.io.IOException {
                                           try {
                                               Services.InstanceLevel si = (Services.InstanceLevel)getChildren ();
                                               si.create ();
                                           } catch (Exception ex) {
                                               throw new java.io.IOException (ex.getMessage ());
                                           }
                                       }
                                   }};
        }

        /** Paste types.
        */
        protected void createPasteTypes (Transferable t, List l) {
            super.createPasteTypes (t, l);
            final InstanceCookie ic = (InstanceCookie)NodeTransfer.cookie (
                                          t, NodeTransfer.COPY, InstanceCookie.class
                                      );
            try {
                if (ic != null && ic.instanceClass () == clazz) {
                    l.add (new PasteType () {
                               public String getName () {
                                   return Main.getString ("LAB_PasteExecutor", getDisplayName ());
                               }

                               public Transferable paste () throws java.io.IOException {
                                   try {
                                       ServiceType s = (ServiceType)ic.instanceCreate ();

                                       Services.InstanceLevel i = (Services.InstanceLevel)getChildren ();
                                       i.add (Services.InstanceLevel.uniquify (s));
                                   } catch (Exception ex) {
                                       throw new java.io.IOException (ex.getMessage ());
                                   }
                                   return null;
                               }
                           });
                }
            } catch (ClassNotFoundException ex) {
                // consume exceptions
            } catch (java.io.IOException ex) {
                // consume exceptions
            }
        }

        /** Actions.
        */
        protected SystemAction[] createActions () {
            return new SystemAction[] {
                       SystemAction.get (org.openide.actions.MoveUpAction.class),
                       SystemAction.get (org.openide.actions.MoveDownAction.class),
                       SystemAction.get (org.openide.actions.ReorderAction.class),
                       null,
                       SystemAction.get (org.openide.actions.PasteAction.class),
                       null,
                       SystemAction.get (org.openide.actions.NewAction.class),
                       null,
                       SystemAction.get (org.openide.actions.ToolsAction.class),
                       SystemAction.get (org.openide.actions.PropertiesAction.class)
                   };
        }

    }

    /** Node that represents one service type.
    */
    static final class InstanceLevel extends BeanNode {
        /** is this node default node or not */
        private boolean def;
        /** for default nodes, listens to name changes */
        private PropertyChangeListener propl = null;

        public InstanceLevel (final ServiceType s) throws IntrospectionException {
            super (s);
        }

        ServiceType getService () {
            return (ServiceType) getBean ();
        }

        Services.InstanceLevel getContainer () {
            return (Services.InstanceLevel) getParentNode ().getChildren ();
        }

        /** @return false
        */
        public boolean canCopy () {
            return true;
        }

        public boolean canCut () {
            return true;
        }

        public boolean canRename () {
            return true;
        }

        public boolean canDestroy () {
            return true;
        }

        public Transferable clipboardCut () throws java.io.IOException {
            destroy ();
            return clipboardCopy ();
        }

        /** Destroys the node.
        */
        public void destroy () {
            getContainer ().destroy (getService ());
        }

        /** Actions.
        */
        protected SystemAction[] createActions () {
            return new SystemAction[] {
                       SystemAction.get (org.openide.actions.MoveUpAction.class),
                       SystemAction.get (org.openide.actions.MoveDownAction.class),
                       null,
                       SystemAction.get (org.openide.actions.CustomizeBeanAction.class),
                       null,
                       SystemAction.get (org.openide.actions.CopyAction.class),
                       SystemAction.get (org.openide.actions.CutAction.class),
                       null,
                       SystemAction.get (org.openide.actions.DeleteAction.class),
                       null,
                       SystemAction.get (org.openide.actions.RenameAction.class),
                       null,
                       SystemAction.get (org.openide.actions.ToolsAction.class),
                       SystemAction.get (org.openide.actions.PropertiesAction.class)
                   };
        }

    }

}

/*
* Log
*  11   Gandalf   1.10        1/13/00  Jaroslav Tulach I18N
*  10   Gandalf   1.9         12/20/99 Jesse Glick     No more "default 
*       services", all are freely reorderable.
*  9    Gandalf   1.8         11/26/99 Patrik Knakal   
*  8    Gandalf   1.7         11/5/99  Jesse Glick     Context help jumbo patch.
*  7    Gandalf   1.6         11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  6    Gandalf   1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    Gandalf   1.4         10/4/99  Jesse Glick     Make Default action on 
*       service types.
*  4    Gandalf   1.3         10/1/99  Jesse Glick     Cleanup of service type 
*       name presentation.
*  3    Gandalf   1.2         9/23/99  Jaroslav Tulach #3976
*  2    Gandalf   1.1         9/17/99  Jaroslav Tulach Reorder of nodes works.
*  1    Gandalf   1.0         9/10/99  Jaroslav Tulach 
* $
*/
