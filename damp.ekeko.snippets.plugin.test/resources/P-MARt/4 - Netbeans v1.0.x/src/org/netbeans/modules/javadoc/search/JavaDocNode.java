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

package org.netbeans.modules.javadoc.search;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ResourceBundle;

import org.openide.src.SourceException;
import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.util.datatransfer.NewType;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.actions.NewAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.TopManager;


/** Subnodes of this node are nodes representing source code patterns i.e.
 * PropertyPatternNode or EventSetPatternNode.
 *
 * @author Petr Hrebejk
 */
public class  JavaDocNode extends AbstractNode {

    private static final ResourceBundle bundle = NbBundle.getBundle( JavaDocNode.class );

    /** Array of the actions of the java methods, constructors and fields. */
    private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {

                SystemAction.get(AddJavaDocFSAction.class),
                SystemAction.get(AddJavaDocJarAction.class),
                null,
                SystemAction.get(ToolsAction.class),
                SystemAction.get(PropertiesAction.class),
            };

    public static final String ICON_BASE =
        "/org/netbeans/modules/javadoc/resources/searchDoc"; // NOI18N

    public JavaDocNode() {
        super( new JavaDocChildren() );
        setName( bundle.getString( "CTL_NodeJavadoc" ) );
        setIconBase( ICON_BASE );
        setActions(DEFAULT_ACTIONS);
    }

    /*
    public JavaDocNode( Children children ) {
      super( (Children)children );
      setName( PatternNode.bundle.getString( "Patterns" ) );
      setIconBase( ICON_BASE );
      setActions(DEFAULT_ACTIONS);    

      CookieSet cs = getCookieSet();
      cs.add( children.getPatternAnalyser() );
}

    /*
    public Node cloneNode() {
      return new PatternGroupNode( ((PatternChildren) getChildren()).cloneChildren() );
}
    */

    public HelpCtx getHelpCtx () {
        return new HelpCtx ( JavaDocNode.class );
    }

    /** Set all actions for this node.
    * @param actions new list of actions
    */
    public void setActions(SystemAction[] actions) {
        systemActions = actions;
    }

    /** Serialization */
    public Node.Handle getHandle () {
        return new JavaDocHandle();
    }

    /** Handle for this node, it is serialized instead of node */
    static final class JavaDocHandle implements Node.Handle {
        static final long serialVersionUID =-3836731604791683300L;
        public Node getNode () {
            return new JavaDocNode();
        }
    }

}

/*
 * Log
 *  4    Gandalf   1.3         1/12/00  Petr Hrebejk    i18n
 *  3    Gandalf   1.2         11/27/99 Patrik Knakal   
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         8/13/99  Petr Hrebejk    
 * $ 
 */ 
