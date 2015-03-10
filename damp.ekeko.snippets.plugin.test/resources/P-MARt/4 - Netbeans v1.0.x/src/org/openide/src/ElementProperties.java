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

package org.openide.src;

/** Names of properties of elements.
*
*
* @author Jaroslav Tulach
*/
public interface ElementProperties {
    /** Name of {@link FieldElement#getType type} property for {@link FieldElement field elements}.
    */
    public static final String PROP_TYPE = "type"; // NOI18N

    /** Name of {@link FieldElement#getInitValue initial value} property for {@link FieldElement field elements}.
    */
    public static final String PROP_INIT_VALUE = "initValue"; // NOI18N

    /** Name of {@link ConstructorElement#getParameters parameters} property  for {@link MethodElement methods} and {@link ConstructorElement constructors}.
    */
    public static final String PROP_PARAMETERS = "parameters"; // NOI18N

    /** Name of {@link ConstructorElement#getExceptions exceptions} property for {@link ConstructorElement constructors} and {@link MethodElement methods}.
    */
    public static final String PROP_EXCEPTIONS = "exceptions"; // NOI18N

    /** Name of {@link InitializerElement#isStatic static} property for {@link InitializerElement initializers}.
    */
    public static final String PROP_STATIC = "static"; // NOI18N

    /** Name of {@link ConstructorElement#getBody body} property for {@link InitializerElement#getBody initializers}, {@link ConstructorElement constructors} and {@link MethodElement methods}.
    */
    public static final String PROP_BODY = "body"; // NOI18N

    /** Name of {@link MemberElement#getModifiers modifiers} property for {@link ClassElement classes}, {@link ConstructorElement constructors}, {@link MethodElement methods}, and {@link FieldElement fields}.
    */
    public static final String PROP_MODIFIERS = "modifiers"; // NOI18N

    /** Name of {@link MemberElement#getName name} property for {@link ClassElement classes}, {@link ConstructorElement constructors}, {@link MethodElement methods}, and {@link FieldElement fields}.
    */
    public static final String PROP_NAME = "name"; // NOI18N

    /** Name of {@link MethodElement#getReturn return value type} property for {@link MethodElement methods}.
    */
    public static final String PROP_RETURN = "return"; // NOI18N

    /** Name of {@link SourceElement#getPackage package} property for {@link SourceElement source elements}.
    */
    public static final String PROP_PACKAGE = "package"; // NOI18N

    /** Name of {@link SourceElement#getImports imports} property for {@link SourceElement source elements}.
    */
    public static final String PROP_IMPORTS = "imports"; // NOI18N

    /** Name of classes property for {@link SourceElement#getClasses source elements} and {@link ClassElement#getClasses classes}.
    */
    public static final String PROP_CLASSES = "classes"; // NOI18N

    /** Name of {@link SourceElement#getAllClasses all classes} property for {@link SourceElement source elements}.
    */
    public static final String PROP_ALL_CLASSES = "allClasses"; // NOI18N

    /** Name of {@link ClassElement#getInitializers initializers} property for {@link ClassElement classes}.
    */
    public static final String PROP_INITIALIZERS = "initializers"; // NOI18N

    /** Name of {@link ClassElement#getMethods methods} property for {@link ClassElement classes}.
    */
    public static final String PROP_METHODS = "methods"; // NOI18N

    /** Name of {@link ClassElement#getFields fields} property for {@link ClassElement classes}.
    */
    public static final String PROP_FIELDS = "fields"; // NOI18N

    /** Name of {@link ClassElement#getConstructors constructors} property for {@link ClassElement classes}.
    */
    public static final String PROP_CONSTRUCTORS = "constructors"; // NOI18N

    /** Name of {@link ClassElement#getSuperclass super class} property for {@link ClassElement classes}.
    */
    public static final String PROP_SUPERCLASS = "superclass"; // NOI18N

    /** Name of {@link ClassElement#getInterfaces interfaces} property for {@link ClassElement classes}.
    */
    public static final String PROP_INTERFACES = "interfaces"; // NOI18N

    /** Name of {@link SourceElement#getStatus status} property for {@link SourceElement source elements}.
    */
    public static final String PROP_STATUS = "status"; // NOI18N

    /** Name of {@link ClassElement#isClassOrInterface is class or interface} property for {@link ClassElement classes}.
    */
    public static final String PROP_CLASS_OR_INTERFACE = "classOrInterface"; // NOI18N
}

/*
* Log
*  8    src-jtulach1.7         1/12/00  Petr Hamernik   i18n using perl script 
*       (//NOI18N comments added)
*  7    src-jtulach1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  6    src-jtulach1.5         7/8/99   Jesse Glick     Removing 
*       ElementProperties.PROP_{CLASS,INTERFACE} (redundant).
*  5    src-jtulach1.4         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  4    src-jtulach1.3         3/30/99  Jesse Glick     [JavaDoc]
*  3    src-jtulach1.2         2/10/99  Jaroslav Tulach 
*  2    src-jtulach1.1         1/19/99  Jaroslav Tulach 
*  1    src-jtulach1.0         1/19/99  Jaroslav Tulach 
* $
*/
