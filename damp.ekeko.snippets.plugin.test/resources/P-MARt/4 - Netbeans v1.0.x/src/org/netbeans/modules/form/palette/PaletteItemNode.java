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

package org.netbeans.modules.form.palette;

import org.openide.actions.*;
import org.openide.cookies.InstanceCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.*;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.form.BeanSupport;

/** The PaletteItemNode is a Node representing the ComponentPaletteItem
* in the tree under Environment.
*
* @author   Ian Formanek
*/
public class PaletteItemNode extends FilterNode {
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = -2098259549820241091L;

    /** Name of the template property. */
    public static final String PROP_IS_CONTAINER = "isContainer"; // NOI18N



    // -----------------------------------------------------------------------------
    // Static variables

    private static SystemAction[] staticActions;

    // -----------------------------------------------------------------------------
    // Constructors

    /** Creates a new palette node */
    public PaletteItemNode (Node original) {
        super (original, Children.LEAF);
    }

    // -----------------------------------------------------------------------------
    // Other methods

    /** Creates properties for this node */
    public Node.PropertySet[] getPropertySets () {
        java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle(PaletteItemNode.class);

        // default sheet with "properties" property set // NOI18N
        Sheet sheet = Sheet.createDefault();
        if (canBeContainer ()) {
            final DataObject obj = (DataObject)getCookie (DataObject.class);
            sheet.get(Sheet.PROPERTIES).put(
                new PropertySupport.ReadWrite (
                    PROP_IS_CONTAINER,
                    Boolean.TYPE,
                    bundle.getString("PROP_isContainer"),
                    bundle.getString("HINT_isContainer")
                ) {
                    public Object getValue () {
                        Object val = obj.getPrimaryFile ().getAttribute(PaletteItem.ATTR_IS_CONTAINER);
                        if (val instanceof Boolean) {
                            return val;
                        } else {
                            return Boolean.TRUE;
                        }
                    }

                    public void setValue (Object val) throws IllegalAccessException,
                        IllegalArgumentException, java.lang.reflect.InvocationTargetException {
                        if (!(val instanceof Boolean))
                            throw new IllegalArgumentException();
                        try {
                            if (!((Boolean)val).booleanValue ()) {
                                obj.getPrimaryFile ().setAttribute(PaletteItem.ATTR_IS_CONTAINER, val);
                            } else { // true is default
                                obj.getPrimaryFile ().setAttribute(PaletteItem.ATTR_IS_CONTAINER, null);
                            }
                        } catch (java.io.IOException e) {
                            // silently ignore - the property is just not changed
                        }
                    }

                }
            );
        }
        return sheet.toArray();
    }

    private boolean canBeContainer () {
        InstanceCookie ic = (InstanceCookie)getCookie (InstanceCookie.class);
        if (ic != null) {
            try {
                Class instClass = ic.instanceClass ();
                if (java.awt.Container.class.isAssignableFrom (instClass)) {
                    Object isContainerValue = BeanSupport.createBeanInfo (instClass).getBeanDescriptor().getValue("isContainer"); // NOI18N
                    if (isContainerValue == null) return true; // containers without the isCOntainer special flag are real containers
                    if (isContainerValue instanceof Boolean) {
                        return ((Boolean)isContainerValue).booleanValue();
                    }
                }
            } catch (Exception e) {
                // in such case return false
                return false;
            }
        }
        return false;
    }

    /** Actions.
    * @return array of actions for this node
    */
    public SystemAction[] getActions () {
        if (staticActions == null)
            staticActions = new SystemAction [] {
                                SystemAction.get(CustomizeBeanAction.class),
                                null,
                                SystemAction.get(MoveUpAction.class),
                                SystemAction.get(MoveDownAction.class),
                                null,
                                SystemAction.get(CutAction.class),
                                SystemAction.get(CopyAction.class),
                                null,
                                SystemAction.get(DeleteAction.class),
                                null,
                                SystemAction.get(ToolsAction.class),
                                SystemAction.get(PropertiesAction.class),
                            };
        return staticActions;
    }

}

/*
 * Log
 *  7    Gandalf   1.6         1/13/00  Ian Formanek    NOI18N #2
 *  6    Gandalf   1.5         1/5/00   Ian Formanek    NOI18N
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         8/18/99  Ian Formanek    Fixed bug 2608 - There 
 *       is no delete action in popup menu of Global Settings | Component 
 *       Palette but Delete key works.
 *  3    Gandalf   1.2         7/23/99  Ian Formanek    isContainer property of 
 *       Palette items
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         6/7/99   Ian Formanek    
 * $
 */
