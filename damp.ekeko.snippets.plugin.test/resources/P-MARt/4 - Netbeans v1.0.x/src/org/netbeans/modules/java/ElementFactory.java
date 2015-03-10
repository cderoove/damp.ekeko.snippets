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

import org.openide.src.*;

/** Builder interface for creating parse results.
 * This inteface allows a parser engine to create some items of implementation
 * unknown to the engine. The engine is provided with opaque Item reference
 * and can communicate with the factory using those references.
 * The ElementFactory is focused on creating and connecting Java Source
 * elements and provides the only way how to access JavaLoader's internals from
 * the parser engine.
 */
public interface ElementFactory {
    /* ======================= Item creator methods ========================== */
    public Item createClass(boolean isInterface, int modifiers, Identifier name, Identifier superclass, Identifier[] interfaces);
    public Item createMethod(int modifiers, Identifier name, Type returnType, MethodParameter[] params, Identifier[] exceptions);
    public Item createField(int modifiers, Identifier name, Type type, String initializer);
    public Item createConstructor(int modifiers, Identifier id, MethodParameter[] params, Identifier[] exceptions);
    public Item createInitializer(int modifiers);
    public void	createImport(Import im, int begin, int end);
    public void	createPackage(Identifier name, int begin, int end);
    
    public void setParent(Item child, Item parent);
    
    /** Sets bounds for the whole element. Begin is offset of first character of the element,
    end is the offset of the last one.
    */
    public void setBounds(Item item, int begin, int end);

    /** Sets bounds for the body of the element.
    */
    public void setBodyBounds(Item item, int begin, int end);

    public void setHeaderBounds(Item item, int begin, int end);

    /** Sets a documentation for the element.
    @param begin offset of doc comment start
    @param end offset of doc comment end
    @param text documentation comment content
    */
    public void setDocumentation(Item item, int begin, int end, String text);
    
    /** Sets name of the field that precedes this one in a declaration statement.
    */
    public void setPrecedingField(Item item, Item previous);

    /** Sets bounds for the identifier only.
    */
    public void setFieldTypeBounds(Item item, int begin, int end);
    
    public void markError(Item item);

    /** Only marker interface
    */
    public interface Item {
    }
}
