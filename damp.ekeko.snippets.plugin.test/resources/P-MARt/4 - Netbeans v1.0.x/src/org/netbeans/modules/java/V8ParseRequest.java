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

import java.io.*;
import java.util.*;
import java.lang.reflect.Modifier;

import javax.swing.text.Position;

import org.openide.src.*;
import org.openide.text.*;
import org.netbeans.modules.java.ElementFactory.Item;

/** Miscellaneous utilities for Java data loader.
*
* @author Petr Hamernik, Ales Novak
*/
class V8ParseRequest extends Object implements ParseObjectRequest, ElementFactory {
    JavaDataObject jdo;
    int errorCount = 0;
	ParsingResult result;
    EditorSupport editor;

    /**
     * @associates Class 
     */
    HashMap classMap = new HashMap(13);

    public V8ParseRequest(JavaDataObject jdo) {
        this.jdo = jdo;
    }

    public JavaDataObject getDataObject() {
        return jdo;
    }

    public InputStream modifyInputStream(InputStream is) {
        return is;
    }

    public void setSyntaxErrors(int errors) {
        errorCount = errors;
    }
    
    public void setSemanticErrors(int errors) {
        // currently no use for this information.
    }

    public int getSyntaxErrors() {
        return errorCount;
    }

	public ElementFactory getFactory() {
		this.result = new ParsingResult();
        this.editor = (EditorSupport)jdo.getCookie(EditorSupport.class);
		return this;
	}

    private void checkFlags(int flags) {
        switch (flags & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE)) {
        case Modifier.PUBLIC:
        case Modifier.PROTECTED:
        case Modifier.PRIVATE:
        case 0:
            // that's OK
            break;
        default:
            errorCount++;
        }
        if (Modifier.isFinal(flags) && Modifier.isAbstract(flags)) {
            errorCount++;
        }
    }
    
    public void markError(Item item) {
        errorCount++;
    }

    /* ========================= BUILDER FACTORY METHODS ======================== */
	public void createImport(Import imp, int begin, int end) {
		result.imports.add(imp);
		result.importsBounds.add(createBiasBounds(begin, end));
	}
    
    public void createPackage(Identifier id, int begin, int end) {
        result.packageId = id;
        result.packageBounds = createBiasBounds(begin, end);
    }
    
    public void setBounds(Item item, int begin, int end) {
        ((ElementImpl)item).bounds = createBiasBounds(begin, end);
    }
    
    public void setBodyBounds(Item item, int begin, int end) {
        ((ElementImpl)item).setBodyBounds(createBiasBounds(begin, end));
    }
    
    public void setHeaderBounds(Item item, int begin, int end) {
        ElementImpl el = (ElementImpl)item;
        
        el.headerBounds = createBiasBounds(begin, end);
    }
    
    public void setFieldTypeBounds(Item item, int begin, int end) {
        FieldElementImpl el = (FieldElementImpl)item;
        el.typeBounds = createBiasBounds(begin, end);
    }
    
    public void setPrecedingField(Item item, Item prev) {
        FieldElementImpl fld1 = (FieldElementImpl)item;
        FieldElementImpl fld2 = (FieldElementImpl)prev;
        
        fld1.setPreviousField(fld2);
        fld1.docBounds = fld2.docBounds;
        fld1.typeBounds = fld2.typeBounds;
		fld1.javadoc = fld2.javadoc;
    }
    
    public void setParent(Item el, Item parent) {
        if (parent == null) {
            ParsingResult.Class c = new ParsingResult.Class((ClassElementImpl)el);
            classMap.put(el, c);
            result.classes.add(c);
            return;
        }
        ParsingResult.Class clazz = lookupClass(parent);
        if (el instanceof ClassElementImpl) {
            ParsingResult.Class c = new ParsingResult.Class((ClassElementImpl)el);
            clazz.classes.add(c);
            classMap.put(el, c);
        } else if (el instanceof InitializerElementImpl) {
            clazz.initializers.add(el);
        } else if (el instanceof FieldElementImpl) {
            clazz.fields.add(el);
        } else if (el instanceof MethodElementImpl) {
            clazz.methods.add(el);
        } else if (el instanceof ConstructorElementImpl) {
            clazz.constructors.add(el);
        } else {
            throw new IllegalArgumentException("Got " + el.getClass().getName());
        }
    }
    
    public void setDocumentation(Item item, int begin, int end, String text) {
        ElementImpl el = (ElementImpl)item;
        JavaDocImpl jdoc;
        
        if (item instanceof ClassElementImpl) {
            jdoc = new JavaDocImpl.Class(text, (ClassElementImpl)el);
        } else if (item instanceof FieldElementImpl) {
            jdoc = new JavaDocImpl.Field(text, (FieldElementImpl)el);
        } else if (item instanceof MethodElementImpl) {
            jdoc = new JavaDocImpl.Method(text, (MethodElementImpl)el);
        } else if (item instanceof ConstructorElementImpl) {
            jdoc = new JavaDocImpl.Method(text, (ConstructorElementImpl)el);
	} else if (item instanceof InitializerElementImpl) {
	    jdoc = new JavaDocImpl(text, (InitializerElementImpl)el);
        } else {
            throw new IllegalArgumentException("Got " + el.getClass().getName());
		}
		el.javadoc = jdoc;
        if (begin == -1 || end == -1) {
            el.docBounds = null;
        } else {
            el.docBounds = createBiasBounds(begin, end);
        }
    }
    
    public Item createClass(boolean isInterface,int modifiers,Identifier name,Identifier superclass,Identifier[] interfaces) {
        ClassElementImpl impl = new ClassElementImpl();
        impl.name = name;
        impl.mod = modifiers;
        checkFlags(impl.mod);
        impl.isClass = !isInterface;
        impl.superclass = superclass;
        impl.interfaces = interfaces;
        return impl;
    }
    
    public Item createMethod(int modifiers, Identifier name, Type returnType, MethodParameter[] params, Identifier[] exceptions) {
        MethodElementImpl impl = new MethodElementImpl();
        impl.name = name;
        impl.mod = modifiers;
        impl.type = returnType;
        impl.parameters = params;
        impl.exceptions = exceptions;
        checkFlags(modifiers);
        return impl;
    }
    
    public Item createField(int modifiers, Identifier name, Type type, String initializer) {
        FieldElementImpl impl = new FieldElementImpl();
        impl.mod = modifiers;
        impl.name = name;
        impl.type = type;
        impl.initValue = initializer;
        return impl;
    }
    
    public Item createConstructor(int modifiers, Identifier id, MethodParameter[] params, Identifier[] exceptions) {
        ConstructorElementImpl impl = new ConstructorElementImpl();
        impl.name = id;
        impl.mod = modifiers;
        impl.parameters = params;
        impl.exceptions = exceptions;
        checkFlags(modifiers);
        return impl;
    }
    
    public Item createInitializer(int modifiers) {
        InitializerElementImpl impl = new InitializerElementImpl();
        impl.stat = Modifier.isStatic(modifiers);
        return impl;
    }
    
    /** Creates position bounds. For obtaining the real offsets is used
    * previous method position()
    * @param begin The begin in the internal position form.
    * @param end The end in the internal position form.
    * @return the bounds
    */
    private PositionBounds createBiasBounds(int beginOffset, int endOffset) {
        PositionRef posBegin = editor.createPositionRef(beginOffset, Position.Bias.Forward);
        PositionRef posEnd = editor.createPositionRef(endOffset, Position.Bias.Backward);
        return new PositionBounds(posBegin, posEnd);
    }

    private ParsingResult.Class lookupClass(Object impl) {
        return (ParsingResult.Class)classMap.get(impl);
    }
}

/*
 * Log
 *  10   Gandalf-post-FCS1.3.2.5     3/13/00  Svatopluk Dedic 
 *  9    Gandalf-post-FCS1.3.2.4     3/10/00  Svatopluk Dedic Fixed field initializer 
 *       fetching
 *  8    Gandalf-post-FCS1.3.2.3     3/9/00   Svatopluk Dedic Stores line number info;
 *       doesn't replace input stream
 *  7    Gandalf-post-FCS1.3.2.2     3/8/00   Svatopluk Dedic Added setter for 
 *       errorCount
 *  6    Gandalf-post-FCS1.3.2.1     3/6/00   Svatopluk Dedic Removed TokenListener 
 *       related code
 *  5    Gandalf-post-FCS1.3.2.0     2/24/00  Ian Formanek    Post FCS changes
 *  4    Gandalf   1.3         1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  3    Gandalf   1.2         1/6/00   Petr Hamernik   update - not used
 *  2    Gandalf   1.1         12/23/99 Petr Hamernik   update - still not used
 *  1    Gandalf   1.0         12/22/99 Petr Hamernik   
 * $
 */
