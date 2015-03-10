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

package org.netbeans.modules.beans;


import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.src.ClassElement;
import org.openide.src.nodes.FilterFactory;

/** Adds property pattern node to stansdard Java class node

 @author Petr Hrebejk
*/
class PatternsExplorerFactory extends FilterFactory {

    private boolean writeable;

    PatternsExplorerFactory( boolean writeable ) {
        super();
        this.writeable = writeable;
    }

    public Node createClassNode (ClassElement element) {
        Node node = super.createClassNode( element );

        //n = (ClassElementNode) super.createClassNode(element);
        Children children = node.getChildren();
        PatternChildren patternChildren = new PatternChildren (element, writeable);
        PatternFilter filter = new PatternFilter ();
        filter.setOrder (new int[] {
                             PatternFilter.PROPERTY | PatternFilter.IDXPROPERTY,
                             PatternFilter.EVENT_SET
                         });
        patternChildren.setFilter (filter);
        children.add(new Node[] {
                         new PatternGroupNode(patternChildren, writeable )
                     }

                    );
        return node;
    }

}

/*
 * Log
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         7/26/99  Petr Hrebejk    Better implementation of
 *       patterns resolving
 *  3    Gandalf   1.2         7/9/99   Petr Hrebejk    Factory chaining fix
 *  2    Gandalf   1.1         7/1/99   Jan Jancura     Object Browser support
 *  1    Gandalf   1.0         6/28/99  Petr Hrebejk    
 * $ 
 */ 