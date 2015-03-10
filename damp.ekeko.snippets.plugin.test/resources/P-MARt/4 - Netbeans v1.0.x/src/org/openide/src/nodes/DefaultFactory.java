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

package org.openide.src.nodes;

import java.beans.*;

import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.src.*;
import org.openide.src.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;

/** The default implementation of the hierarchy nodes factory.
* Uses the standard node implementations in this package.
* @author Petr Hamernik
*/
public class DefaultFactory extends Object implements ElementNodeFactory, IconStrings {
    /** Default instance of the factory with read-write properties. */
    public static final DefaultFactory READ_WRITE = new DefaultFactory(true);

    /** Default instance of the factory with read-only properties. */
    public static final DefaultFactory READ_ONLY = new DefaultFactory(false);

    /** Should be the element nodes read-only or writeable
    * (properties, clipboard operations,...)
    */
    private boolean writeable;

    /** Create a new factory.
    * @param writeable <code>true</code> if the produced nodes
    * should have writable properties
    * @see ElementNode#writeable
    */
    public DefaultFactory(boolean writeable) {
        this.writeable = writeable;
    }

    /* Test whether this factory produces writeable nodes.
    * @return <code>true</code> if so
    */
    public boolean isWriteable() {
        return writeable;
    }

    /* Returns the node asociated with specified element.
    * @return ElementNode
    */
    public Node createMethodNode (final MethodElement element) {
        return new MethodElementNode(element, writeable);
    }

    /* Returns the node asociated with specified element.
    * @return ElementNode
    */
    public Node createConstructorNode (final ConstructorElement element) {
        return new ConstructorElementNode(element, writeable);
    }

    /* Returns the node asociated with specified element.
    * @return ElementNode
    */
    public Node createFieldNode (final FieldElement element) {
        return new FieldElementNode(element, writeable);
    }

    /* Returns the node asociated with specified element.
    * @return ElementNode
    */
    public Node createInitializerNode (final InitializerElement element) {
        return new InitializerElementNode(element, writeable);
    }

    /* Returns the node asociated with specified element.
    * @return ElementNode
    */
    public Node createClassNode (ClassElement element) {
        return new ClassElementNode(element, createClassChildren(element), writeable);
    }

    /** Create children for a class node.
    * Could be subclassed to customize, e.g., the ordering of children.
    * The default implementation used {@link ClassChildren}.
    * @param element a class element
    * @return children for the class element
    */
    protected Children createClassChildren(ClassElement element) {
        return createClassChildren( element, writeable ? READ_WRITE : READ_ONLY );
    }

    /** Create children for a class node, with specified factory.
    * The default implementation used {@link ClassChildren}.
    * @param element a class element
    * @param factory the factory which will be used to create children
    * @return children for the class element
    */
    final protected Children createClassChildren(ClassElement element, ElementNodeFactory factory ) {

        if (ElementNode.sourceOptions.getCategoriesUsage()) {
            ClassChildren children = new ClassChildren( factory, element);
            ClassElementFilter filter = new ClassElementFilter();
            filter.setOrder(new int[] { SourceElementFilter.CLASS, SourceElementFilter.INTERFACE });
            children.setFilter(filter);
            if (element.isInterface()) {
                children.add(new Node[] {
                                 new ElementCategoryNode(0, factory, element, writeable),
                                 new ElementCategoryNode(2, factory, element, writeable),
                             });
            }
            else {
                children.add(new Node[] {
                                 new ElementCategoryNode(0, factory, element, writeable),
                                 new ElementCategoryNode(1, factory, element, writeable),
                                 new ElementCategoryNode(2, factory, element, writeable),
                             });
            }
            return children;
        }
        else {
            return new ClassChildren(factory, element);
        }
    }

    /* Creates and returns the instance of the node
    * representing the status 'WAIT' of the DataNode.
    * It is used when it spent more time to create elements hierarchy.
    * @return the wait node.
    */
    public Node createWaitNode () {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(ElementNode.bundle.getString("Wait"));
        n.setIconBase(WAIT);
        return n;
    }

    /* Creates and returns the instance of the node
    * representing the status 'ERROR' of the DataNode
    * @return the error node.
    */
    public Node createErrorNode () {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setName(ElementNode.bundle.getString("Error"));
        n.setIconBase(ERROR);
        return n;
    }

    /** Array of the actions of the category nodes. */
    private static final SystemAction[] CATEGORY_ACTIONS = new SystemAction[] {
                SystemAction.get(CopyAction.class),
                SystemAction.get(PasteAction.class),
                null,
                SystemAction.get(NewAction.class),
                null,
                SystemAction.get(ToolsAction.class),
                SystemAction.get(PropertiesAction.class)
            };

    /** Filters under each category node */
    static final int[][] FILTERS = new int[][] {
                                       { ClassElementFilter.FIELD },
                                       { ClassElementFilter.CONSTRUCTOR },
                                       { ClassElementFilter.METHOD },
                                   };

    /** The names of the category nodes */
    static final String[] NAMES = new String[] {
                                      ElementNode.bundle.getString("Fields"),
                                      ElementNode.bundle.getString("Constructors"),
                                      ElementNode.bundle.getString("Methods"),
                                  };

    /** The short descriptions of the category nodes */
    static final String[] SHORTDESCRS = new String[] {
                                            ElementNode.bundle.getString("Fields_HINT"),
                                            ElementNode.bundle.getString("Constructors_HINT"),
                                            ElementNode.bundle.getString("Methods_HINT"),
                                        };

    /** Array of the icons used for category nodes */
    static final String[] CATEGORY_ICONS = new String[] {
                                               FIELDS_CATEGORY, CONSTRUCTORS_CATEGORY, METHODS_CATEGORY
                                           };

    /**
    * Category node - represents one section under class element node - fields,
    * constructors, methods.
    */
    static class ElementCategoryNode extends AbstractNode {

        /** The class element for this node */
        ClassElement element;

        /** The type of the category node - for new types. */
        int newTypeIndex;

        /** Create new element category node for the specific category.
        * @param index The index of type (0=fields, 1=constructors, 2=methods)
        * @param factory The factory which is passed down to the class children object
        * @param element the class element which this node is created for
        */
        ElementCategoryNode(int index, ElementNodeFactory factory, ClassElement element, boolean writeable) {
            this(index, new ClassChildren(factory, element));
            this.element = element;
            newTypeIndex = writeable ? index : -1;
            switch (index) {
            case 0: setName("Fields"); break; // NOI18N
            case 1: setName("Constructors"); break; // NOI18N
            case 2: setName("Methods"); break; // NOI18N
            }
        }

        /** Create new element node.
        * @param index The index of type (0=fields, 1=constructors, 2=methods)
        * @param children the class children of this node
        */
        private ElementCategoryNode(int index, ClassChildren children) {
            super(children);
            setDisplayName(NAMES[index]);
            setShortDescription (SHORTDESCRS[index]);
            ClassElementFilter filter = new ClassElementFilter();
            filter.setOrder(FILTERS[index]);
            children.setFilter(filter);
            systemActions = CATEGORY_ACTIONS;
            setIconBase(CATEGORY_ICONS[index]);
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (ElementCategoryNode.class);
        }

        /* Get the new types that can be created in this node.
        * @return array of new type operations that are allowed
        */
        public NewType[] getNewTypes() {
            switch (newTypeIndex) {
            case 0:
                return new NewType[] {
                           new SourceEditSupport.ElementNewType(element, (byte) 1)
                       };
            case 1:
                return new NewType[] {
                           new SourceEditSupport.ElementNewType(element, (byte) 0),
                           new SourceEditSupport.ElementNewType(element, (byte) 2)
                       };
            case 2:
                return new NewType[] {
                           new SourceEditSupport.ElementNewType(element, (byte) 3)
                       };
            default:
                return super.getNewTypes();
            }
        }
    }

}

/*
* Log
*  21   src-jtulach1.20        1/12/00  Petr Hamernik   i18n using perl script 
*       (//NOI18N comments added)
*  20   src-jtulach1.19        11/11/99 Jesse Glick     Display miscellany.
*  19   src-jtulach1.18        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  18   src-jtulach1.17        9/28/99  Petr Hamernik   fixed bug #1074
*  17   src-jtulach1.16        9/13/99  Petr Hamernik   runAsUser implemented and
*       used
*  16   src-jtulach1.15        8/18/99  Petr Hamernik   name is set for category 
*       nodes
*  15   src-jtulach1.14        7/19/99  Petr Hamernik   interfaces also listed 
*       under nodes
*  14   src-jtulach1.13        7/10/99  Jesse Glick     Context help.
*  13   src-jtulach1.12        7/9/99   Petr Hrebejk    Method for creating 
*       ClassChildren with explicitly specified factory added
*  12   src-jtulach1.11        6/28/99  Petr Hamernik   new hierarchy under 
*       ClassChildren
*  11   src-jtulach1.10        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  10   src-jtulach1.9         5/9/99   Ian Formanek    setDisplayName -> setName
*       as recommended for AbstractNode
*  9    src-jtulach1.8         4/2/99   Jesse Glick     [JavaDoc]
*  8    src-jtulach1.7         3/18/99  Petr Hamernik   
*  7    src-jtulach1.6         3/16/99  Petr Hamernik   renaming static fields
*  6    src-jtulach1.5         3/15/99  Petr Hamernik   
*  5    src-jtulach1.4         3/4/99   Petr Hamernik   
*  4    src-jtulach1.3         2/12/99  Petr Hamernik   
*  3    src-jtulach1.2         2/10/99  Petr Hamernik   
*  2    src-jtulach1.1         2/9/99   David Simonek   
*  1    src-jtulach1.0         1/29/99  David Simonek   
* $
*/
