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

package org.netbeans.modules.java;

import java.beans.*;
import java.util.Collection;
import java.util.LinkedList;

import org.openide.actions.*;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.ElementCookie;
import org.openide.cookies.FilterCookie;
import org.openide.nodes.*;
import org.openide.src.*;
import org.openide.src.nodes.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/*
import org.netbeans.modules.java.patterns.PatternGroupNode;
import org.netbeans.modules.java.patterns.PatternChildren;
*/

/** The implementation of hierarchy nodes factory for the java loader.
*
* @author Petr Hamernik
*/
class JavaElementNodeFactory extends DefaultFactory {
    /** Default instance of this factory. */
    public static final DefaultFactory DEFAULT = new JavaElementNodeFactory();

    /** Array of the actions of the java methods, constructors and fields. */
    private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {
                SystemAction.get(OpenAction.class),
                null,
                SystemAction.get(CutAction.class),
                SystemAction.get(CopyAction.class),
                null,
                SystemAction.get(DeleteAction.class),
                SystemAction.get(RenameAction.class),
                null,
                SystemAction.get(ToolsAction.class),
                SystemAction.get(PropertiesAction.class)
            };

    /** Array of the actions of the java intializers. */
    private static final SystemAction[] INITIALIZER_ACTIONS = new SystemAction[] {
                SystemAction.get(OpenAction.class),
                null,
                SystemAction.get(CutAction.class),
                SystemAction.get(CopyAction.class),
                null,
                SystemAction.get(DeleteAction.class),
                null,
                SystemAction.get(ToolsAction.class),
                SystemAction.get(PropertiesAction.class)
            };

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

    /** This node can return current element factory as cookie */
    private final Node FACTORY_GETTER_NODE = new FactoryGetterNode();


    /** Create nodes for tree */
    private boolean tree = false;

    /** Creates new factory. */
    public JavaElementNodeFactory() {
        super(true);
    }

    /** If true generate nodes for tree.
    */
    public void setGenerateForTree (boolean tree) {
        this.tree = tree;
    }

    /** Returns true if generate nodes for tree.
    * @returns true if generate nodes for tree.
    */
    public boolean getGenerateForTree () {
        return tree;
    }

    /** Returns the node asociated with specified element.
    * @return ElementNode
    */
    public Node createMethodNode(MethodElement element) {
        MethodElementNode n = new MethodElementNode(element, true);
        n.setDefaultAction(SystemAction.get(OpenAction.class));
        n.setActions(DEFAULT_ACTIONS);
        return n;
    }

    /** Returns the node asociated with specified element.
    * @return ElementNode
    */
    public Node createConstructorNode(ConstructorElement element) {
        ConstructorElementNode n = new ConstructorElementNode(element, true);
        n.setDefaultAction(SystemAction.get(OpenAction.class));
        n.setActions(DEFAULT_ACTIONS);
        return n;
    }

    /** Returns the node asociated with specified element.
    * @return ElementNode
    */
    public Node createFieldNode(FieldElement element) {
        FieldElementNode n = new FieldElementNode(element, true);
        n.setDefaultAction(SystemAction.get(OpenAction.class));
        n.setActions(DEFAULT_ACTIONS);
        return n;
    }

    /** Returns the node asociated with specified element.
    * @return ElementNode
    */
    public Node createInitializerNode(InitializerElement element) {
        InitializerElementNode n = new InitializerElementNode(element, true);
        n.setDefaultAction(SystemAction.get(OpenAction.class));
        n.setActions(INITIALIZER_ACTIONS);
        return n;
    }

    /** Returns the node asociated with specified element.
    * @return ElementNode
    */
    public Node createClassNode(final ClassElement element) {

        if ( element == null ) {
            return FACTORY_GETTER_NODE;
        }


        ClassElementNode n;
        if (tree) {
            ClassChildren children = new ClassChildren( JavaDataObject.getBrowserFactory(), element);
            ClassElementFilter filter = new ClassElementFilter();
            n = new ClassElementNode(element, children ,true);

            CookieSet css = n.getCookieSet ();
            css.add ((FilterCookie) n.getChildren ());
            n.setElementFormat(new ElementFormat (
                                   NbBundle.getBundle (JavaElementNodeFactory.class).getString("CTL_Class_name_format")
                               ));

            // filter out inner classes
            filter.setOrder (new int[] {
                                 ClassElementFilter.CONSTRUCTOR + ClassElementFilter.METHOD,
                                 ClassElementFilter.FIELD,
                             });
            children.setFilter (filter);
        }
        else {
            n = (ClassElementNode) super.createClassNode(element);
        }
        n.setDefaultAction(SystemAction.get(OpenAction.class));
        n.setActions(CLASS_ACTIONS);
        return n;
    }

    protected Children createClassChildren( ClassElement element ) {
        return createClassChildren( element, JavaDataObject.getExplorerFactory() );
    }


    /** This is an unusuall use of Node and FilterCookie */

    private class FactoryGetterNode extends AbstractNode implements FilterCookie {

        FactoryGetterNode( ) {
            super ( Children.LEAF );
        }

        public synchronized Node.Cookie getCookie( Class clazz ) {
            if ( clazz == FilterFactory.class )
                return this;
            else
                return super.getCookie( clazz );
        }

        public Class getFilterClass() {
            return null;
        }

        public void setFilter( Object filter ) {}

        public Object getFilter( ) {
            if ( tree )
                return JavaDataObject.getBrowserFactory();
            else
                return JavaDataObject.getExplorerFactory();
        }

    }

}

/*
* Log
*  14   src-jtulach1.13        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  13   src-jtulach1.12        7/9/99   Petr Hrebejk    JavaDoc comment support 
*       moved out of module
*  12   src-jtulach1.11        6/28/99  Petr Hrebejk    Multiple node factories 
*       added
*  11   src-jtulach1.10        6/28/99  Petr Hamernik   new hierarchy under 
*       ClassChildren
*  10   src-jtulach1.9         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  9    src-jtulach1.8         5/26/99  Petr Hrebejk    
*  8    src-jtulach1.7         5/16/99  Jaroslav Tulach New hiearchy.
*  7    src-jtulach1.6         5/7/99   Petr Hrebejk    
*  6    src-jtulach1.5         5/7/99   Petr Hamernik   package private again
*  5    src-jtulach1.4         4/13/99  Petr Hamernik   public
*  4    src-jtulach1.3         4/2/99   Jan Jancura     ObjectBrowser support II.
*  3    src-jtulach1.2         4/1/99   Ian Formanek    Rollback to make it 
*       compilable
*  2    src-jtulach1.1         4/1/99   Jan Jancura     Object Browser support
*  1    src-jtulach1.0         3/18/99  Petr Hamernik   
* $
*/
