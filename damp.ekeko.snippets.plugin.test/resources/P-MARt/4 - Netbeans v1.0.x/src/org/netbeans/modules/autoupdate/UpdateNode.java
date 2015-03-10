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

package org.netbeans.modules.autoupdate;


import java.util.ResourceBundle;

import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.util.NbBundle;

/** The UpdateNode class holds static innerclasses ( Module, Group,
 * Children and wait. This class serves only like a namespace for
 * the innerclasses and defines some usefull constants.
 *
 * @author  Petr Hrebejk
 */
class UpdateNode extends Object {

    private static final ResourceBundle bundle = NbBundle.getBundle( UpdateNode.class );

    /** Iconbase for module Updates with new modules */
    private static final String NEW_MODULE_ICON_BASE = "/org/netbeans/modules/autoupdate/resources/newModule"; // NOI18N
    /** Iconbase for module Updates with module updates */
    private static final String UPDATE_MODULE_ICON_BASE = "/org/netbeans/modules/autoupdate/resources/updateModule"; // NOI18N
    /** Iconbase for module Updates with module updates */
    private static final String MODULE_GROUP_ICON_BASE = "/org/openide/resources/defaultFolder"; // NOI18N
    /** Iconbase for wait node */
    private static final String WAIT_ICON_BASE = "/org/openide/resources/src/wait"; // NOI18N

    /** Private constructor, the class should have no instances */
    private UpdateNode() {
    }

    /** Class for representing module update in the tree */
    static class Module extends AbstractNode {

        private ModuleUpdate moduleUpdate;

        Module ( ModuleUpdate moduleUpdate ) {
            super( org.openide.nodes.Children.LEAF );

            this.moduleUpdate = moduleUpdate;

            setDisplayName( moduleUpdate.getName() );
            setIconBase( moduleUpdate.isNew() ? NEW_MODULE_ICON_BASE : UPDATE_MODULE_ICON_BASE );

            getCookieSet().add( moduleUpdate );
        }
    }

    /** Class for representing module wait node */
    static class Wait extends AbstractNode {

        Wait ( ) {
            super( org.openide.nodes.Children.LEAF );

            setDisplayName( bundle.getString( "CTL_WaitNode" ) );
            setIconBase( WAIT_ICON_BASE );
        }
    }

    /** Class for representing module group in the tree */
    static class Group extends AbstractNode {

        private ModuleGroup group;

        Group( ModuleGroup group ) {
            super( new UpdateNode.Children( group ) );

            this.group = group;

            setDisplayName( group.getName() );
            setIconBase( MODULE_GROUP_ICON_BASE );

            getCookieSet().add( group );
        }

    }


    /** Holds children nodes of the module group */
    static class Children extends org.openide.nodes.Children.Keys {

        protected ModuleGroup moduleGroup;

        // CONSTRUCTORS -----------------------------------------------------------------------

        /** Creates module group children.
         * @param group The group of modules
         */ 

        public Children ( ModuleGroup moduleGroup ) {
            super();
            this.moduleGroup = moduleGroup;
            setKeys( moduleGroup.getItems() );
        }

        /** Called when the preparation of nodes is needed
         */
        protected void addNotify() {
            //refreshAllKeys ();
        }

        /** Called when all children are garbage collected */
        protected void removeNotify() {
            setKeys( java.util.Collections.EMPTY_SET );
        }

        // IMPLEMENTATION of Children.Keys ------------------------------------------

        /** Creates nodes for given key.
        */
        protected Node[] createNodes( final Object key ) {

            if (key instanceof ModuleUpdate) {
                return new Node[] { new UpdateNode.Module( (ModuleUpdate)key ) };
            }
            else if (key instanceof ModuleGroup ) {
                return new Node[] { new UpdateNode.Group( (ModuleGroup)key ) };
            }
            else {
                // Unknown pattern
                return new Node[0];
            }
        }
    }
}
/*
 * Log
 *  4    Gandalf   1.3         1/12/00  Petr Hrebejk    i18n
 *  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         10/11/99 Petr Hrebejk    Last minute fixes
 *  1    Gandalf   1.0         10/7/99  Petr Hrebejk    
 * $
 */
