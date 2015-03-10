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

import org.openide.nodes.Node;
import org.openide.src.*;

/** A factory used to create
* instances of hierarchy node implementations.
* Loaders that use the element hierarchy
* should implement this factory
* so as to provide their own implementations of hierarchy element nodes.
* @see ClassChildren
* @see SourceChildren
*
* @author Dafe Simonek, Jaroslav Tulach
*/
public interface ElementNodeFactory {

    /** Make a node representing a constructor.
    * @param element the constructor
    * @return a constructor node instance
    */
    public Node createConstructorNode (ConstructorElement element);

    /** Make a node representing a method.
    * @param element the method
    * @return a method node instance
    */
    public Node createMethodNode (MethodElement element);

    /** Make a node representing a field.
    * @param element the field
    * @return a field node instance
    */
    public Node createFieldNode (FieldElement element);

    /** Make a node representing an initializer.
    * @param element the initializer
    * @return an initializer node instance
    */
    public Node createInitializerNode (InitializerElement element);

    /** Make a node representing a class.
    * @param element the class
    * @return a class node instance
    */
    public Node createClassNode (ClassElement element);

    /** Make a node indicating that the creation of children
    * is still under way.
    * It should be used when the process is slow.
    * @return a wait node
    */
    public Node createWaitNode ();

    /** Make a node indicating that there was an error creating
    * the element children.
    * @return the error node
    */
    public Node createErrorNode ();

}

/*
* Log
*  6    src-jtulach1.5         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  5    src-jtulach1.4         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  4    src-jtulach1.3         4/2/99   Jesse Glick     [JavaDoc]
*  3    src-jtulach1.2         2/10/99  Petr Hamernik   
*  2    src-jtulach1.1         2/9/99   David Simonek   
*  1    src-jtulach1.0         1/29/99  David Simonek   
* $
*/
