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

import java.util.StringTokenizer;
import java.util.HashMap;
import java.lang.ref.WeakReference;
import java.io.Serializable;

import org.openide.util.Utilities;

/** Java types.
*
* @author Petr Hamernik, Ian Formanek
*/
public final class Type extends Object implements Cloneable, Serializable {
    /** Private constants for types */
    private static final int T_BOOLEAN  = 0x0001;
    private static final int T_INT      = 0x0002;
    private static final int T_CHAR     = 0x0003;
    private static final int T_BYTE     = 0x0004;
    private static final int T_SHORT    = 0x0005;
    private static final int T_LONG     = 0x0006;
    private static final int T_FLOAT    = 0x0007;
    private static final int T_DOUBLE   = 0x0008;
    private static final int T_VOID     = 0x0009;

    private static final int T_CLASS    = 0x0010;
    private static final int T_ARRAY    = 0x0020;

    private static final int T_PRIMITIVE= 0x000F;

    /** <code>void</code> primitive type. */
    public static final Type VOID = new Type(T_VOID);
    /** <code>boolean</code> primitive type. */
    public static final Type BOOLEAN = new Type(T_BOOLEAN);
    /** <code>int</code> primitive type. */
    public static final Type INT = new Type(T_INT);
    /** <code>char</code> primitive type. */
    public static final Type CHAR = new Type(T_CHAR);
    /** <code>byte</code> primitive type. */
    public static final Type BYTE = new Type(T_BYTE);
    /** <code>short</code> primitive type. */
    public static final Type SHORT = new Type(T_SHORT);
    /** <code>long</code> primitive type. */
    public static final Type LONG = new Type(T_LONG);
    /** <code>float</code> primitive type. */
    public static final Type FLOAT = new Type(T_FLOAT);
    /** <code>double</code> primitive type. */
    public static final Type DOUBLE = new Type(T_DOUBLE);

    /** Table where strings like "int" are keys and classes like INT are values 
     * @associates Type*/ // NOI18N
    private static HashMap text2type = new HashMap();

    private static final String L_VOID = "void"; // NOI18N
    private static final String L_BOOLEAN = "boolean"; // NOI18N
    private static final String L_INT = "int"; // NOI18N
    private static final String L_CHAR = "char"; // NOI18N
    private static final String L_BYTE = "byte"; // NOI18N
    private static final String L_SHORT = "short"; // NOI18N
    private static final String L_LONG = "long"; // NOI18N
    private static final String L_FLOAT = "float"; // NOI18N
    private static final String L_DOUBLE = "double"; // NOI18N

    private static final String[] PRIMITIVE_NAMES = {
        L_VOID, L_BOOLEAN, L_INT, L_CHAR, L_BYTE, L_SHORT, L_LONG, L_FLOAT, L_DOUBLE
    };

    static {
        text2type.put(L_VOID, VOID);
        text2type.put(L_BOOLEAN, BOOLEAN);
        text2type.put(L_INT, INT);
        text2type.put(L_CHAR, CHAR);
        text2type.put(L_BYTE, BYTE);
        text2type.put(L_SHORT, SHORT);
        text2type.put(L_LONG, LONG);
        text2type.put(L_FLOAT, FLOAT);
        text2type.put(L_DOUBLE, DOUBLE);
    }

    /** Kind of this instance of Type */
    private int kind;

    /** Element type if this type is array */
    private Type elementType = null;

    /** Identifier of the class if this type is ClassType */
    private Identifier classType =  null;

    static final long serialVersionUID =8997425134968958367L;
    /** Constructor for primitive type
    */
    private Type(int kind) {
        this.kind = kind;
    }

    /** Creates array of elements of given type.
    * @param type the element type
    */
    private Type(Type type) {
        this.kind = T_ARRAY;
        elementType = type;
    }

    private Type(Identifier id) {
        this.kind = T_CLASS;
        classType = id;
    }

    private Object readResolve() {
        switch (kind) {
        case T_BOOLEAN: return BOOLEAN;
        case T_INT: return INT;
        case T_CHAR: return CHAR;
        case T_BYTE: return BYTE;
        case T_SHORT: return SHORT;
        case T_LONG: return LONG;
        case T_FLOAT: return FLOAT;
        case T_DOUBLE: return DOUBLE;
        case T_VOID: return VOID;
        case T_CLASS: return createClass(classType);
        case T_ARRAY: return createArray(elementType);
        default: throw new InternalError();
        }
    }

    /** Get the Java names of the primitive types.
    * @return the names
    */
    public static String[] getTypesNames() {
        return PRIMITIVE_NAMES;
    }

    /** Create an array type.
    * @param elementType the element type
    * @return the array type
    */
    public static Type createArray (Type elementType) {
        return new Type(elementType);
    }

    /** Create a class type by name.
    * @param id the class name
    * @return the class type
    */
    public static Type createClass (Identifier id) {
        return new Type(id);
    }

    /** Create a type from an existing class.
    * @param cl the class
    * @return the type
    */
    public static Type createFromClass (Class cl) {
        if (cl.isArray ())
            return createArray (createFromClass (cl.getComponentType ()));
        else if (cl.isPrimitive ()) {
            if (Void.TYPE.equals (cl)) return VOID;
            if (Boolean.TYPE.equals (cl)) return BOOLEAN;
            if (Integer.TYPE.equals (cl)) return INT;
            if (Character.TYPE.equals (cl)) return CHAR;
            if (Byte.TYPE.equals (cl)) return BYTE;
            if (Short.TYPE.equals (cl)) return SHORT;
            if (Long.TYPE.equals (cl)) return LONG;
            if (Float.TYPE.equals (cl)) return FLOAT;
            if (Double.TYPE.equals (cl)) return DOUBLE;
            throw new InternalError (); // Unknown primitive type
        }
        else
            return createClass (Identifier.create (cl.getName ()));
    }

    /** Create a type from its string representation.
    * @param text the string representation, e.g. <code>"int[][]"</code>,
    * <code>"java.awt.Button"</code>, etc.
    * @return the type
    * @exception InvalidArgumentException if the text cannot be parsed
    */
    public static Type parse(String text) throws IllegalArgumentException {
        StringTokenizer tok = new StringTokenizer(text, " []", true); // NOI18N
        Type type = null;
        int status = 0;
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            if (token.equals(" ")) // NOI18N
                continue;

            switch (status) {
            case 0:
                {
                    type = (Type) text2type.get(token);
                    if (type == null) {
                        StringTokenizer tok2 = new StringTokenizer(token, ".", false); // NOI18N
                        while (tok2.hasMoreTokens()) {
                            if (!Utilities.isJavaIdentifier(tok2.nextToken())) {
                                throw new IllegalArgumentException();
                            }
                        }
                        type = createClass(Identifier.create (token));
                    }
                    status = 1;
                    break;
                }
            case 1:
                if (!token.equals("[")) // NOI18N
                    throw new IllegalArgumentException();

                status = 2;
                break;
            case 2:
                if (!token.equals("]")) // NOI18N
                    throw new IllegalArgumentException();

                type = createArray(type);
                status = 1;
                break;
            }
        }
        if (type == null)
            type = VOID;
        return type;
    }

    /** Test if the type is primitive.
    * @return <CODE>true</CODE> if so
    */
    public boolean isPrimitive () {
        return ((kind & T_PRIMITIVE) != 0);
    }

    /** Test if the type is an array.
    * @return <CODE>true</CODE> if so
    */
    public boolean isArray () {
        return (kind == T_ARRAY);
    }

    /** Test if the type is a class or interface.
    * @return <CODE>true</CODE> if so, <code>false</code> if an array or primitive type
    */
    public boolean isClass () {
        return (kind == T_CLASS);
    }

    /** Get the element type of this array type.
    * @return the element type
    * @exception IllegalStateException if this type is not an array type
    */
    public Type getElementType () throws IllegalStateException {
        if (isArray())
            return elementType;
        else
            throw new IllegalStateException();
    }

    /** Get the (fully-qualified) name of this class type.
    * @return the class name
    * @exception IllegalStateException if this type is not a simple class or interface type
    */
    public Identifier getClassName () throws IllegalStateException {
        if (isClass())
            return classType;
        else
            throw new IllegalStateException();
    }

    /** Attempt to get the real class corresponding to this type, using the default class loader.
    * @return the class
    * @exception ClassNotFoundException if the class cannot be found
    */
    public Class toClass() throws ClassNotFoundException {
        return toClass (null);
    }

    /** Attempt to get the real class corresponding to this type.
    * @param loader class loader to use for loading classes
    * @return the class
    * @exception ClassNotFoundException if the class cannot be found
    */
    public Class toClass(ClassLoader loader) throws ClassNotFoundException {
        if (isPrimitive()) {
            switch (kind) {
            case T_BOOLEAN : return Boolean.TYPE;
            case T_INT     : return Integer.TYPE;
            case T_CHAR    : return Character.TYPE;
            case T_BYTE    : return Byte.TYPE;
            case T_SHORT   : return Short.TYPE;
            case T_LONG    : return Long.TYPE;
            case T_FLOAT   : return Float.TYPE;
            case T_DOUBLE  : return Double.TYPE;
            default        : return Void.TYPE; //void
            }
        }

        // if no given class loader then use own
        if (loader == null) {
            loader = getClass ().getClassLoader ();
        }

        if (isClass())
            return Class.forName (classType.getFullName(), true, loader);
        else {
            // construct array
            String name = "["; // NOI18N
            Type t = this;
            while (t.getElementType ().isArray ()) {
                name = name + "["; // NOI18N
                t = t.getElementType ();
            }
            if (t.isClass ()) name = name + t.classType.getFullName ();
            else {
                switch (t.kind) {
                case T_BOOLEAN : name = name + "Z"; break; // NOI18N
                case T_INT     : name = name + "I"; break; // NOI18N
                case T_CHAR    : name = name + "C"; break; // NOI18N
                case T_BYTE    : name = name + "B"; break; // NOI18N
                case T_SHORT   : name = name + "S"; break; // NOI18N
                case T_LONG    : name = name + "J"; break; // NOI18N
                case T_FLOAT   : name = name + "F"; break; // NOI18N
                case T_DOUBLE  : name = name + "D"; break; // NOI18N
                }
            }

            return Class.forName (name, true, loader);
        }
    }

    /** Get this type as the string.
    * @param appendTo The string buffer where to append to
    * @param source true means getSourceName() will be used, otherwise getFullName()
    * @return the same string buffer which was passed into
    */
    StringBuffer getAsString(StringBuffer appendTo, boolean source) {
        if (isPrimitive()) {
            switch (kind) {
            case T_BOOLEAN : return appendTo.append("boolean"); // NOI18N
            case T_INT     : return appendTo.append("int"); // NOI18N
            case T_CHAR    : return appendTo.append("char"); // NOI18N
            case T_BYTE    : return appendTo.append("byte"); // NOI18N
            case T_SHORT   : return appendTo.append("short"); // NOI18N
            case T_LONG    : return appendTo.append("long"); // NOI18N
            case T_FLOAT   : return appendTo.append("float"); // NOI18N
            case T_DOUBLE  : return appendTo.append("double"); // NOI18N
            default        : return appendTo.append("void"); //void // NOI18N
            }
        }
        else {
            if (isClass())
                return appendTo.append(source ?
                                       classType.getSourceName() :
                                       classType.getFullName()
                                      );
            else {
                return elementType.getAsString(appendTo, source).append("[]"); // NOI18N
            }
        }
    }

    /** Get a form of this type usable in Java source.
    * @return the string representation
    */
    public String getSourceString() {
        return getAsString(new StringBuffer(), true).toString();
    }

    /** Get a form of this type usable in Java source.
    * @return the string representation
    */
    public String getFullString() {
        return getAsString(new StringBuffer(), false).toString();
    }

    /** Get a form of this type usable in Java source.
    * @return the string representation
    */
    public String toString() {
        return getSourceString();
    }

    /** Compare the specified Type with this Type for equality.
    * @param type Type to be compared with this
    * @param source Determine if the source name (for class types)
    *       should be also compared.
    *       If <CODE>false</CODE> only fully qualified name is compared.
    * @return <CODE>true</CODE> if the specified object equals to
    *         specified Identifier otherwise <CODE>false</CODE>.
    */
    public boolean compareTo(Type type, boolean source) {
        if (type.kind != kind)
            return false;

        switch (kind) {
        case T_ARRAY:
            return type.getElementType().compareTo(getElementType(), source);
        case T_CLASS:
            return type.getClassName().compareTo(getClassName(), source);
        default:
            return true;
        }
    }

    /** Compare the specified object with this Type for equality.
    * There are tested only full qualified name if the type is Class
    * @param o Object to be compared with this
    * @return <CODE>true</CODE> if the specified object represents the same type
    *         otherwise <CODE>false</CODE>.
    */
    public boolean equals(Object o) {
        return (o instanceof Type) ? compareTo((Type) o, false) : false;
    }

    /** @return the hash code of full name String object.
    */
    public int hashCode() {
        switch (kind) {
        case T_ARRAY:
            return getElementType().hashCode() << 1;
        case T_CLASS:
            return getClassName().hashCode();
        default:
            return System.identityHashCode(this);
        }
    }
}

/*
 * Log
 *  11   src-jtulach1.10        1/13/00  Petr Hamernik   i18n -(2nd round) - 
 *       script bug fixed.
 *  10   src-jtulach1.9         1/12/00  Petr Hamernik   i18n using perl script 
 *       (//NOI18N comments added)
 *  9    src-jtulach1.8         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  8    src-jtulach1.7         8/17/99  Ian Formanek    Generated serial version
 *       UID
 *  7    src-jtulach1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    src-jtulach1.5         5/13/99  Petr Hamernik   
 *  5    src-jtulach1.4         5/12/99  Petr Hamernik   Identifier 
 *       implementation updated
 *  4    src-jtulach1.3         3/30/99  Jesse Glick     [JavaDoc]
 *  3    src-jtulach1.2         3/14/99  Petr Hamernik   
 *  2    src-jtulach1.1         1/20/99  Petr Hamernik   
 *  1    src-jtulach1.0         1/17/99  Jaroslav Tulach 
 * $
 * Beta Change History:
 *  0    Tuborg    0.21        --/--/98 Jan Formanek    added createFromClass, getAsClass methods
 */
