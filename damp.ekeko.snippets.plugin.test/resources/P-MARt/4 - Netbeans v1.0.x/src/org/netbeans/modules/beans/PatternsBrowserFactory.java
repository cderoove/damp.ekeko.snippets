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


import org.openide.actions.*;
import org.openide.cookies.FilterCookie;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.src.nodes.ElementNodeFactory;
import org.openide.src.ClassElement;
import org.openide.src.ElementFormat;
import org.openide.src.nodes.FilterFactory;
import org.openide.src.nodes.ClassElementNode;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/** Adds properties & events to the ClassElementNode
*
* @author Jan Jancura
*/
class PatternsBrowserFactory extends FilterFactory {


    /** Array of the actions of the java classes. */
    private static final SystemAction[] CLASS_ACTIONS = new SystemAction[] {
                SystemAction.get(OpenAction.class),
                null,
                SystemAction.get(CutAction.class),
                SystemAction.get(CopyAction.class),
                SystemAction.get(PasteAction.class),
                null,
                SystemAction.get(DeleteAction.class),
                SystemAction.get(RenameAction.class),
                null,
                SystemAction.get(NewAction.class),
                null,
                SystemAction.get(ToolsAction.class),
                SystemAction.get(PropertiesAction.class)
            };

    private boolean writeable;

    PatternsBrowserFactory( boolean writeable ) {
        super();
        this.writeable = writeable;
    }

    public Node createClassNode (ClassElement element) {

        Node factoryGetterNode = super.createClassNode( null );
        FilterCookie factoryGetter = (FilterCookie) factoryGetterNode.getCookie( FilterFactory.class );
        ElementNodeFactory childrenFactory = (ElementNodeFactory)factoryGetter.getFilter();

        PatternChildren children = new PatternChildren ( childrenFactory, element, writeable );
        //System.out.println("Writeable : " + writeable + " : " + element.getName().getName() );
        ClassElementNode n = new ClassElementNode (element, children , writeable);


        Node np = super.createClassNode( element );

        CookieSet css = n.getCookieSet ();
        css.add ((FilterCookie) n.getChildren ());
        n.setElementFormat (new ElementFormat (
                                NbBundle.getBundle (PatternsBrowserFactory.class).getString ("CTL_Class_name_format")
                            ));

        // filter out inner classes
        PatternFilter filter = new PatternFilter ();
        filter.setOrder (new int[] {
                             PatternFilter.FIELD,
                             PatternFilter.CONSTRUCTOR + PatternFilter.METHOD,
                             //      PatternFilter.PROPERTY | PatternFilter.IDXPROPERTY,
                             //      PatternFilter.EVENT_SET
                         });
        children.setFilter (filter);

        n.setActions ( np.getActions() );
        n.setDefaultAction ( np.getDefaultAction() );
        /*
        if ( !element.isInterface () ) { 
          n.setIconBase ( "/org/netbeans/modules/clazz/resources/classBr" );
    }
        */
        //n.setIconBase( np.getIconBase() );


        //if ( writeable ) {

        //  n.setDefaultAction (SystemAction.get (OpenAction.class));
        //  n.setActions (CLASS_ACTIONS);
        //}

        return n;
    }
}

/*
* Log
*  6    Gandalf   1.5         2/16/00  Petr Hrebejk    Element from clazz module
*       in ObjectBrowser are no loger mofifiable
*  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         7/27/99  Jan Jancura     Popup menu in Object 
*       Browser repaired
*  3    Gandalf   1.2         7/26/99  Petr Hrebejk    Better implementation of 
*       patterns resolving
*  2    Gandalf   1.1         7/9/99   Petr Hrebejk    Factory chaining fix
*  1    Gandalf   1.0         7/1/99   Jan Jancura     
* $ 
*/ 
