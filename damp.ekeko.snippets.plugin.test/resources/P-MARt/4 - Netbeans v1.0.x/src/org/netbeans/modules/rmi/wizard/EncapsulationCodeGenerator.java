/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.rmi.wizard;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.*;
import java.text.*;

import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.src.*;

/**
 *
 * @author  mryzl
 */

public class EncapsulationCodeGenerator extends DefaultCodeGenerator{

    public static final MessageFormat METHOD_DELEGATION = new MessageFormat("\n{0}.{1}({2});\n");
    public static final MessageFormat METHOD_RETURN_DELEGATION = new MessageFormat("\nreturn {0}.{1}({2});\n");
    public static final Identifier INNER = Identifier.create("inner");

    /** Creates new DefaultCodeGenerator. */
    public EncapsulationCodeGenerator() {
    }

    /** Creates implementation class.
    * @param ce - class element. If null, it will be created.
    * @return class element
    */
    public ClassElement getImpl(ClassElement ce) throws SourceException {
        ce = super.getImpl(ce);
        // add inner and constructor and method for setting inner
        ce.setFields(getInnerFields());
        return ce;
    }

    /** Set body for implementation methods.
    * @param me - method
    * @return properly set method
    */
    protected MethodElement setImplMethodBody(MethodElement me) throws SourceException {
        // delegate it
        Object[] objs = new Object[] {INNER, me.getName(), SrcSupport.getParameterNames(me.getParameters())};
        if (me.getReturn().equals(Type.VOID)) {
            me.setBody(METHOD_DELEGATION.format(objs));
        } else {
            me.setBody(METHOD_RETURN_DELEGATION.format(objs));
        }
        return me;
    }

    /** Set comment for implementation methods.
    * @param me - method
    * @return properly set method
    */
    protected MethodElement setImplMethodComment(MethodElement me) throws SourceException {
        SrcSupport.commentMethod(me, null);
        return me;
    }

    /** Create variables for encapsulated Objects
    */
    protected FieldElement[] getInnerFields() throws SourceException {
        FieldElement[] fes = new FieldElement[1];
        fes[0] = new FieldElement();
        fes[0].setName(INNER);
        fes[0].setModifiers(Modifier.PROTECTED);
        fes[0].setType(Type.parse(data.sourceName));
        return fes;
    }

    /** Creates constructors for implementation.
     *
     * @return constructors
     */
    protected ConstructorElement[] getImplConstructors() throws SourceException {
        ConstructorElement[] ces = new ConstructorElement[] {
                                       SrcSupport.getRMIConstructorElement(data.implName, "", "", false),
                                       SrcSupport.getRMIConstructorElement(data.implName, data.sourceName + " inner", "", false),
                                   };
        ces[0].setBody("\nthrow new InstantiationError(\"User should implement this constructor.\");\n");
        ces[1].setBody("\nsuper();\nthis.inner = inner;\n");
        for(int i = 0; i < ces.length; i++) {
            SrcSupport.commentMethod(ces[i], "Creates new instance.\n");
        }
        return ces;
    }
}

/*
* <<Log>>
*  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         8/18/99  Martin Ryzl     
*  3    Gandalf   1.2         7/29/99  Martin Ryzl     executor selection is 
*       working
*  2    Gandalf   1.1         7/28/99  Martin Ryzl     added selection of 
*       executor
*  1    Gandalf   1.0         7/28/99  Martin Ryzl     
* $ 
*/ 
