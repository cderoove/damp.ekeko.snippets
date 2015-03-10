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
import java.beans.PropertyChangeListener;
import javax.swing.text.StyledDocument;
import javax.swing.text.Position;

import sun.tools.java.*;
import sun.tools.javac.*;
import sun.tools.tree.*;

import org.openide.TopManager;
import org.openide.src.*;
import org.openide.text.NbDocument;
import org.openide.text.PositionRef;
import org.openide.text.PositionBounds;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.io.NullOutputStream;

/** Parser of Java source code. It generates the hierarchy
* of the implementations of elements.
*
* @author Petr Hamernik
*/
class JavaParser extends BatchParser {

    // ===================== Fields ==============================

    /** Mask which contains all possible modifiers used in the hierarchy.
    * It filters internal modifiers used in parser.
    */
    private static final int MODIF_MASK = M_PUBLIC | M_PRIVATE | M_PROTECTED |
                                          M_STATIC | M_FINAL | M_SYNCHRONIZED | M_VOLATILE | M_TRANSIENT |
                                          M_NATIVE | M_ABSTRACT;

    /** JavaDataObject for which source is this parser created. */
    JavaDataObject jdo;

    /** Appropriate the java editor - used for creating the PositionRefs */
    JavaEditor editor;

    /** The environment for recognizing the errors after parsing. */
    BatchEnvironment environment;

    /** Source element implementation */
    SourceElementImpl sourceElementImpl;

    /** Parsed data. It is created and set to source element impl after
    * parsing finish. If this class is null, the parser doesn't collect
    * the parsed information.
    */
    ParsingResult result;

    /** Stack of the classes (ParsingResult.Class) 
     * @associates Class*/
    Stack classesStack;

    /** Stack of the begin positions for the members (Long) 
     * @associates Long*/
    Stack memberPositionsStack;

    /** The map of the javadoc comments.
    * The keys are javadoc strings and the Range classes of javadoc.
    * @associates Range
    */
    HashMap javadocMap;

    /** The position of the begin of last occured expression. */
    long lastExpressionBegin;

    /** The position of the end of last occured expression. */
    long lastExpressionEnd;

    /** Marks that there is header parsed. */
    boolean headerMark = false;

    /** position of last header */
    long headerEndPosition;

    /** Input stream */
    Util.ParserInputStream input;

    /** Counter of errors which occurs during resolving fully qualified
    * names in parser. If all error occur only during resolving,
    * the parser looks like that trere is no error.
    */
    int errorsInResolving;

    /** Reference to the runnable which has started the parser.
    * It is used in SourceElementImpl.setParsingResult after parser finish.
      @deprecated SourceElementImpl now uses V8-style parser with different binding.
    SourceElementImpl.ParsingRunnable parsingRunnable;
    */

    // ===================== Main methods and constructors ==================

    /** Constructor directly for parsing without compiler.
    * @param fo FileObject where the Java code is stored.
    * @exception IOException if any i/o problem occured during reading
    */
    public JavaParser(FileObject fo) throws IOException {
        this(JavaCompiler.createEnvironment(null), fo);
    }

    /** Constructor directly for parsing without compiler.
    * @param fo FileObject where the Java code is stored.
    * @env environment created by Compiler, which is set to 'environment' field
    * @exception IOException if any i/o problem occured during reading
    */
    private JavaParser(Environment env, FileObject fo) throws IOException {
        this(new Environment(env, new CoronaClassFile(fo)), fo, true);
        if (env instanceof BatchEnvironment) {
            // errors checking - for partially parsed files
            this.environment = (BatchEnvironment) env;
        }
    }

    /** Constructor for compiler.
    * @param environment The environment passed by the compiler or
    * @param fo FileObject where the Java code is stored.
    * @param store if there is required the building and storing the elements
    *              hierarchy
    * @exception IOException if any i/o problem occured during reading
    */
    public JavaParser(Environment environment, FileObject fo, boolean store) throws IOException {
        this(environment, fo, store, createInputStream(fo, store));
    }

    /** Private constructor.
    * @param environment The environment passed by the compiler or
    * @param fo FileObject where the Java code is stored.
    * @param store if there is required the building and storing the elements
    *              hierarchy
    * @param input the input stream where to read.
    * @exception IOException if any i/o problem occured during reading
    */
    private JavaParser(Environment environment, FileObject fo, boolean store, Util.ParserInputStream input) throws IOException {
        super(environment, input);

        this.input = input;

        if (!store)
            return;

        if (environment instanceof BatchEnvironment) {
            // errors checking - for partially parsed files
            this.environment = (BatchEnvironment) environment;
        }

        try {
            jdo = (JavaDataObject) DataObject.find(fo);
            editor = jdo.getJavaEditor();
            sourceElementImpl = jdo.getSourceElementImpl();
            result = new ParsingResult();

            errorsInResolving = 0;

            classesStack = new Stack();
            memberPositionsStack = new Stack();
            if (javadocMap == null)
                javadocMap = new HashMap();
        }
        catch (ClassCastException e) {
            TopManager.getDefault().notifyException(e);
        }
        catch (DataObjectNotFoundException e) {
            TopManager.getDefault().notifyException(e);
        }
    }

    /** Creates new input stream from the file object.
    * Finds the java data object, checks if the document is loaded and
    * and create the stream either from the file object either from the document.
    * @param fo fileobject with the source
    * @param store if there is required the building and storing the elements
    *              hierarchy
    * @exception IOException if any i/o problem occured during reading
    */
    private static Util.ParserInputStream createInputStream(FileObject fo, boolean store) throws IOException {
        return (Util.ParserInputStream)Util.createInputStream(fo, false, true);
    }

    /** Sets the runnable which has started the parser.
    * @param parsingRunnable The reference for the runnable which will be
    *        passed to the SourceElementImpl.setParsingResult after parser finish.
    void setParsingRunnable(SourceElementImpl.ParsingRunnable parsingRunnable) {
      this.parsingRunnable = parsingRunnable;
}
    */

    /** Parses the file - this method starts the parser.
    * After super.parseFile finish, the result is set to
    * the appropriate SourceElementImpl.
    */
    public void parseFile() {
        super.parseFile();
        if (result != null) {
            boolean errors = (environment != null) && (environment.nerrors > errorsInResolving);

            if (result.packageId == null)
                result.packageBounds = createBiasBounds(0, 0); // begin of the file

            /*
              Disabled: sourceElementImpl now uses new V8 parser.
            sourceElementImpl.setParsingResult(result, errors, parsingRunnable);
            */
        }
        closeInput();
    }

    /** This method closes the input stream for parsing, which is always opened
    * in the constructor. Method parseFile calls this method, but if anyone just creates
    * the JavaParser class, is also responsible for closing the stream.
    * Otherwise the stream is closed during finalization.
    */
    public void closeInput() {
        try {
            if (input != null) {
                input.close();
                input = null;
            }
        }
        catch (IOException e) {
        }
    }

    /** Finalize the parser and close the input stream if necessary.
    */
    public void finalize() throws Throwable {
        super.finalize();
        closeInput();
    }

    /** Method for the environment - accessibility */
    protected Vector getClassesProtected() {
        return classes;
    }

    /** Method for the environment - accessibility */
    protected Imports getImportsProtected() {
        return imports;
    }

    // ======================== PARSER METHODS ===============================

    // ----------------------- utilities -----------------------------------

    /** Computes the real offset from the long value representing position
    * in the parser.
    * @return the offset
    */
    static int position(long p) {
        return (int)(p & 0xFFFFFFFFL);
    }

    /** Creates position bounds. For obtaining the real offsets is used
    * previous method position()
    * @param begin The begin in the internal position form.
    * @param end The end in the internal position form.
    * @return the bounds
    */
    PositionBounds createBiasBounds(long begin, long end) {
        PositionRef posBegin = editor.createPositionRef(position(begin), Position.Bias.Forward);
        PositionRef posEnd = editor.createPositionRef(position(end), Position.Bias.Backward);
        return new PositionBounds(posBegin, posEnd);
    }

    /** Converts the object to the full qualified identifier string.
    * @param id As the id is passed either sun.tools.java.Identifier either
    *           IdentifierToken. 
    * @return fully qualified identifier as a string
    */
    String getFullName(Object id) {
        sun.tools.java.Identifier ii = (id instanceof IdentifierToken) ?
                                       ((IdentifierToken)id).getName() :
                                       (sun.tools.java.Identifier) id;

        try {
            String ret = env.resolve(ii).toString();
            return ret;
        }
        catch (ClassNotFound e) {
            return ii.toString();
        }
    }

    /** Converts the object to the src.Identifier (using toString method)
    * @param id As the id is passed either sun.tools.java.Identifier either
    *           IdentifierToken. Both these classes has well implemented
    *           toString method.
    * @param resolve If identifier should be resolved (fully qualified)
    * @return identifier
    */
    org.openide.src.Identifier createId(Object id, boolean resolve) {
        org.openide.src.Identifier retValue = null;
        if (resolve) {
            if (environment != null) {
                int prev = environment.nerrors;
                retValue = org.openide.src.Identifier.create(getIdResolver(id), id.toString());
                errorsInResolving += environment.nerrors - prev;
            }
            else {
                retValue = org.openide.src.Identifier.create(getIdResolver(id), id.toString());
            }
        }
        else {
            retValue = org.openide.src.Identifier.create(id.toString());
        }
        return retValue;
    }

    /** Create new resolver for the identifier.
    * @param id As the id is passed either sun.tools.java.Identifier either
    *           IdentifierToken. Both these classes has well implemented
    *           toString method.
    */
    org.openide.src.Identifier.Resolver getIdResolver(Object id) {
        return new EnvironmentalResolver(env, sourceClass, id);
    }

    /** Resolver implementation - for lazy computing fully
    * qualified identifiers.
    */
    static class EnvironmentalResolver implements org.openide.src.Identifier.Resolver {
        Environment et;
        ClassDefinition cd;
        Object id;

        /** Creates new resolver */
        EnvironmentalResolver(Environment e, ClassDefinition cd, Object id) {
            this.et = e;
            this.id = id;
            this.cd = cd;
        }

        /** Resolve full name */
        public String resolve() {
            sun.tools.java.Identifier ii = (id instanceof IdentifierToken) ?
                                           ((IdentifierToken)id).getName() : (sun.tools.java.Identifier) id;

            if (cd.isInnerClass())
                ii = cd.getOuterClass().resolveName(et, ii);
            else
                ii = et.resolveName(ii);
            String ret = ii.toString();
            // remove space from e.g. 'org.openide.NotifyDescriptor. Exception'
            int space = ret.indexOf(' ');
            if (space != -1)
                ret = ret.substring(0, space) + ret.substring(space + 1);

            return ret;
        }
    }

    /** Converts sun.tools.java.Type to org.openide.src.Type
    * May be called only for primitive types, class type and array type.
    * @param resolve If identifier should be resolved (fully qualified)
    */
    org.openide.src.Type typeToType(sun.tools.java.Type type, boolean resolve) {
        switch (type.getTypeCode()) {
        case TC_BOOLEAN: return org.openide.src.Type.BOOLEAN;
        case TC_BYTE: return org.openide.src.Type.BYTE;
        case TC_CHAR: return org.openide.src.Type.CHAR;
        case TC_SHORT: return org.openide.src.Type.SHORT;
        case TC_INT: return org.openide.src.Type.INT;
        case TC_LONG: return org.openide.src.Type.LONG;
        case TC_FLOAT: return org.openide.src.Type.FLOAT;
        case TC_DOUBLE: return org.openide.src.Type.DOUBLE;
        case TC_VOID: return org.openide.src.Type.VOID;
        case TC_ARRAY: return org.openide.src.Type.createArray(typeToType(((ArrayType) type).getElementType(), resolve));
        case TC_CLASS: return org.openide.src.Type.createClass(createId(((ClassType) type).getClassName(), resolve));

        case TC_NULL:
        case TC_METHOD:
        case TC_ERROR:
        default:
            throw new InternalError();
        }
    }

    /** Object which hold the range in the source stream */
    private class Range {
        /** begin */
        int begin;

        /** end */
        int end;

        /** Constructs new range */
        Range(long begin, long end) {
            this.begin = position(begin);
            this.end = position(end);
        }

        /** Get the javadoc inside the range as the PositionBounds
        * Could be called only when result != null
        */
        PositionBounds getCommentBounds() {
            String doc = input.getString(begin, end);

            //PENDING: not finished - must be improved
            long p1 = begin + doc.indexOf("/**");
            long p2 = end - doc.length() + doc.lastIndexOf("*/") + 2; // NOI18N

            return createBiasBounds(p1, p2);
        }
    }

    /** Get the position bounds for given javadoc.
    * The javadoc must be in the table, otherwise NullPointerException is thrown
    * @return the position bounds.
    */
    private PositionBounds getJavaDocBounds(String doc) {
        return ((Range)javadocMap.get(doc)).getCommentBounds();
    }

    // ----------------- overriden methods -----------------------------------

    /** Package declaration */
    public void packageDeclaration(long l, IdentifierToken identifiertoken) {
        if (result != null) {
            result.packageId = createId(identifiertoken, false);
            result.packageBounds = createBiasBounds(l, prevPos);
        }
        super.packageDeclaration(l, identifiertoken);
    }

    /** Import class declaration */
    public void importClass(long l, IdentifierToken identifiertoken) {
        if (result != null)
            storeImport(l, identifiertoken, Import.CLASS);
        super.importClass(l, identifiertoken);
    }

    /** Import package declaration */
    public void importPackage(long l, IdentifierToken identifiertoken) {
        if (result != null)
            storeImport(l, identifiertoken, Import.PACKAGE);
        super.importPackage(l, identifiertoken);
    }

    /** Stores the given import declaration. */
    private void storeImport(long l, IdentifierToken identifiertoken, boolean what) {
        result.imports.add(new Import(createId(identifiertoken, false), what));
        result.importsBounds.add(createBiasBounds(l, prevPos));
    }

    /** Parses the class. Only store the current position. */
    protected void parseClass() throws SyntaxError, IOException {
        if (result != null) {
            try {
                memberPositionsStack.push(new Long(pos));
                headerEndPosition = pos;
                headerMark = true;
                super.parseClass();
            }
            finally {
                memberPositionsStack.pop();
            }
        }
        else {
            super.parseClass();
        }
    }

    /** Scans one token. Calls super class and marks the javadoc comments or
    * header ends.
    */
    public long scan() throws IOException {
        long p1 = pos;

        long l = super.scan();

        if (result != null) {
            if (headerMark) {
                if ((token == LBRACE) || (token == SEMICOLON) || (token == ASSIGN)) {
                    headerEndPosition = prevPos;
                    headerMark = false;
                }
            }
        }

        // comments must be stored, because the first call of this method
        // is from <init>
        if (docComment != null) {
            if (javadocMap == null)
                javadocMap = new HashMap();

            javadocMap.put(docComment, new Range(p1, pos));
        }
        return l;
    }

    /** Parse the expression and store its bounds */
    public Expression parseExpression() throws SyntaxError, IOException {
        Expression exp = super.parseExpression();
        lastExpressionBegin = exp.getWhere();
        lastExpressionEnd = pos;
        return exp;
    }

    /** Parses the field. Only store the current position. */
    protected void parseField() throws SyntaxError, IOException {
        if (result != null) {
            try {
                memberPositionsStack.push(new Long(pos));
                headerEndPosition = pos;
                headerMark = true;
                super.parseField();
            }
            finally {
                memberPositionsStack.pop();
            }
        }
        else {
            super.parseField();
        }
    }

    /** Define one field (method, field, constructor, intializer) */
    public void defineField(long where, ClassDefinition c,
                            String doc, int mod, sun.tools.java.Type t,
                            IdentifierToken name, IdentifierToken[] args,
                            IdentifierToken[] exp, Node val) {
        super.defineField(where, c, doc, mod, t, name, args, exp, val);

        if (result == null)
            return;

        if ((sourceClass.getModifiers() & M_ANONYMOUS) != 0) {
            // skip elements of anonymous inner class
            return;
        }

        ParsingResult.Class declaringClass = (ParsingResult.Class) classesStack.peek();
        ElementImpl elementImpl;

        if (t.getTypeCode() == Constants.TC_METHOD) {
            if (name.getName().equals(Constants.idClassInit)) {
                // Initializer
                InitializerElementImpl impl = new InitializerElementImpl();
                elementImpl = impl;
                impl.stat = ((mod & M_STATIC) != 0);
                impl.javadoc = new JavaDocImpl(doc, impl);
                declaringClass.initializers.add(impl);
            }
            else {
                // Constructor & Method
                ConstructorElementImpl impl = (name.getName().equals(Constants.idInit)) ?
                                              new ConstructorElementImpl() : new MethodElementImpl();
                elementImpl = impl;

                impl.mod = mod & MODIF_MASK;

                impl.parameters = new MethodParameter[args.length];
                sun.tools.java.Type[] methodArguments = ((MethodType) t).getArgumentTypes();
                for (int i = 0; i < args.length; i++) {
                    impl.parameters[i] = new MethodParameter(
                                             args[i].toString(), typeToType(methodArguments[i], true),
                                             ((args[i].getModifiers() & M_FINAL) != 0)
                                         );
                }

                if (exp == null) {
                    impl.exceptions = new org.openide.src.Identifier[0];
                }
                else {
                    impl.exceptions = new org.openide.src.Identifier[exp.length];
                    for (int i = 0; i < exp.length; i++) {
                        impl.exceptions[i] = createId(exp[i], true);
                    }
                }

                impl.javadoc = new JavaDocImpl.Method(doc, impl);

                if (name.getName().equals(Constants.idInit)) {
                    // Constructor
                    impl.name = createId(declaringClass.impl.getName().getName(), false);
                    declaringClass.constructors.add(impl);
                }
                else {
                    // Method
                    ((MethodElementImpl)impl).type = typeToType(((MethodType) t).getReturnType(), true);
                    impl.name = createId(name, false);
                    declaringClass.methods.add(impl);
                }
            }
            elementImpl.bodyBounds = (val == null) ?
                                     null :
                                     createBiasBounds(val.getWhere() + 1, prevPos - 1);
        }
        else {
            // Field
            FieldElementImpl impl = new FieldElementImpl();
            elementImpl = impl;

            impl.name = createId(name, false);
            impl.mod = mod & MODIF_MASK;
            impl.type = typeToType(t, true);
            impl.initValue = (val == null) ? "" : // NOI18N
                             input.getString(position(lastExpressionBegin), position(lastExpressionEnd));
            impl.javadoc = new JavaDocImpl.Field(doc, impl);
            declaringClass.fields.add(impl);

            impl.bodyBounds = (val == null) ?
                              null :
                              createBiasBounds(val.getWhere(), prevPos);
        }

        long beginField = ((Long) memberPositionsStack.peek()).longValue();
        long endField = prevPos;
        if (elementImpl instanceof FieldElementImpl) {
            String tmp = input.getString(position(prevPos));
            int firstColon = tmp.indexOf(";"); // NOI18N
            endField += firstColon + 1;
        }

        if (doc == null) { // field without javadoc comment
            elementImpl.docBounds = null;
            elementImpl.bounds = createBiasBounds(beginField, endField);
        }
        else { // field with javadoc comment
            elementImpl.docBounds = getJavaDocBounds(doc);
            long docBeginPosition = elementImpl.docBounds.getBegin().getOffset();
            elementImpl.bounds = createBiasBounds(docBeginPosition, endField);
        }
        elementImpl.headerBounds = createBiasBounds(beginField, headerEndPosition);

        headerMark = false;
    }

    /** Begin of the class */
    public ClassDefinition beginClass(long where, String doc, int mod, IdentifierToken t,
                                      IdentifierToken sup, IdentifierToken[] interfaces) {
        ClassDefinition clazz = super.beginClass(where, doc, mod, t, sup, interfaces);

        if (result == null)
            return clazz;

        if ((mod & M_ANONYMOUS) != 0) //skip anonymous inner class
            return clazz;

        headerMark = false;
        long beginClass = ((Long) memberPositionsStack.peek()).longValue();

        ClassElementImpl impl = new ClassElementImpl();
        impl.headerBounds = createBiasBounds(beginClass, headerEndPosition);
        impl.name = createId(t, true);
        impl.mod = mod & MODIF_MASK;
        impl.isClass = ((mod & M_INTERFACE) == 0);
        impl.superclass = (sup == null) ? null : createId(sup, true);
        impl.interfaces = new org.openide.src.Identifier[interfaces.length];
        for (int i = 0; i < interfaces.length; i++)
            impl.interfaces[i] = createId(interfaces[i], true);

        impl.javadoc = new JavaDocImpl.Class(doc, impl);

        try {
            ParsingResult.Class outerClass = (ParsingResult.Class) classesStack.peek();
            ParsingResult.Class thisClass =  new ParsingResult.Class(impl);
            outerClass.classes.add(thisClass);
            classesStack.push(thisClass);
        }
        catch (EmptyStackException e) {
            ParsingResult.Class thisClass =  new ParsingResult.Class(impl);
            result.classes.add(thisClass);
            classesStack.push(thisClass);
        }

        return clazz;
    }

    /** End of the class */
    public void endClass(long where, ClassDefinition clazz) {
        super.endClass(where, clazz);

        if (result == null)
            return;

        if ((clazz.getModifiers() & M_ANONYMOUS) != 0) //skip anonymous inner class
            return;

        ParsingResult.Class thisClass = (ParsingResult.Class) classesStack.pop();

        long beginClass = ((Long) memberPositionsStack.peek()).longValue();
        if (clazz.getDocumentation() == null) { // class without javadoc comment
            thisClass.impl.docBounds = null;
            thisClass.impl.bounds = createBiasBounds(beginClass, where);
        }
        else { // field with javadoc comment
            thisClass.impl.docBounds = getJavaDocBounds(clazz.getDocumentation());
            long docBeginPosition = thisClass.impl.docBounds.getBegin().getOffset();
            thisClass.impl.bounds = createBiasBounds(docBeginPosition, where);
        }
    }

    // ==================== The parser input stream ==========================

    /** The input stream which holds all data which are read in the StringBuffer.
    */
    static class ParserInputStream extends InputStream {
        /** The underlaying stream. */
        private InputStream stream;

        /** Whole text */
        private String text;

        /** The string buffer which collect the data. */
        private StringBuffer buffer;

        /** This flag determines if there is used the text field or buffer field.
        * The constructor set it
        */
        private boolean mode;

        /** The counter of read chars */
        private int counter;

        /** Creates the stream from the text. */
        ParserInputStream(String text) {
            stream = new StringBufferInputStream(text);
            this.text = text;
            mode = false;
            counter = 0;
        }

        /** Creates the stream from the another stream. */
        ParserInputStream(InputStream stream) {
            this.stream = stream;
            buffer = new StringBuffer();
            mode = true;
        }

        /** Gets the part of the text which was already read.
        * @param begin the begin index
        * @param end the end index
        */
        public String getString(long begin, long end) {
            int p1 = position(begin);
            int p2 = position(end);
            return mode ? buffer.substring(p1, p2) : text.substring(p1, p2);
        }

        /** Gets the part of the text which was already read.
        * End is last position which was already read.
        * @param begin the begin index
        */
        public String getString(long begin) {
            int p1 = position(begin);

            if (mode) {
                return buffer.substring(p1);
            }
            else {
                int p2 = Math.min(counter - 1, text.length());
                return text.substring(p1, p2);
            }
        }

        /** Read one character from the stream. */
        public int read() throws IOException {
            int x = stream.read();
            if (mode && (x != -1))
                buffer.append((char)x);
            counter++;
            return x;
        }

        /** Closes the stream */
        public void close() throws IOException {
            stream.close();
        }
    }
}

/*
 * Log: 
 *  31   src-jtulach1.30        01/12/00 Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  30   src-jtulach1.29        11/05/99 Ales Novak      #2206
 *  29   src-jtulach1.28        11/03/99 Petr Hamernik   fixed #3473 and other
 *       problems with resolving Identifiers
 *  28   src-jtulach1.27        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems copyright in file comment
 *  27   src-jtulach1.26        10/08/99 Petr Hamernik   fixed bug - close input
 *       stream
 *  26   src-jtulach1.25        10/06/99 Petr Hamernik   Streams and Readers
 *       correction.
 *  25   src-jtulach1.24        09/10/99 Petr Hamernik   comments
 *  24   src-jtulach1.23        09/09/99 Petr Hamernik   correct closing streams
 *       after parsing
 *  23   src-jtulach1.22        08/18/99 Petr Hamernik   fixed bug #2113
 *  22   src-jtulach1.21        08/18/99 Petr Hamernik   fixed bug #3173
 *  21   src-jtulach1.20        07/23/99 Petr Hamernik   global parsing listener
 *  20   src-jtulach1.19        06/08/99 Ian Formanek    ---- Package Change To
 *       org.openide ----
 *  19   src-jtulach1.18        06/02/99 Petr Hamernik   connections of java
 *       sources
 *  18   src-jtulach1.17        05/28/99 Ales Novak      lazy resolving of
 *       identifiers
 *  17   src-jtulach1.16        05/26/99 Petr Hamernik   compilation bug ffixed
 *  16   src-jtulach1.15        05/21/99 Petr Hamernik   parsing info garbage
 *       collected too fast - fixed
 *  15   src-jtulach1.14        05/15/99 Petr Hamernik   Identifiers resolving
 *       temporary disabled
 *  14   src-jtulach1.13        05/14/99 Petr Hamernik   parsing errors during
 *       resolving Identifiers are ignored
 *  13   src-jtulach1.12        05/12/99 Petr Hamernik   ide.src.Identifier changed
 *  12   src-jtulach1.11        05/12/99 Petr Hamernik   javadoc parsing bugfix
 *  11   src-jtulach1.10        05/11/99 Petr Hamernik   emptyStackExecption bug
 *       fixed
 *  10   src-jtulach1.9         05/10/99 Petr Hamernik   
 *  9    src-jtulach1.8         04/30/99 Petr Hamernik   bounds bugfix
 *  8    src-jtulach1.7         04/21/99 Petr Hamernik   Java module updated
 *  7    src-jtulach1.6         04/15/99 Petr Hamernik   parser improvements
 *  6    src-jtulach1.5         04/14/99 Ian Formanek    Fixes problem with
 *       programmatic usage of imports
 *  5    src-jtulach1.4         04/02/99 Petr Hamernik   
 *  4    src-jtulach1.3         04/01/99 Petr Hamernik   
 *  3    src-jtulach1.2         03/29/99 Petr Hamernik   
 *  2    src-jtulach1.1         03/29/99 Petr Hamernik   
 *  1    src-jtulach1.0         03/28/99 Ales Novak      
 *
 *  Log
 *   32   Gandalf-post-FCS1.30.1.0    2/24/00  Ian Formanek    Post FCS changes
 *   31   src-jtulach1.30        1/12/00  Petr Hamernik   i18n: perl script used 
 *        ( //NOI18N comments added )
 *   30   src-jtulach1.29        11/5/99  Ales Novak      #2206
 *   29   src-jtulach1.28        11/3/99  Petr Hamernik   fixed #3473 and other 
 *        problems with resolving Identifiers
 *   28   src-jtulach1.27        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - 
 *        Sun Microsystems copyright in file comment
 *   27   src-jtulach1.26        10/8/99  Petr Hamernik   fixed bug - close input
 *        stream
 *   26   src-jtulach1.25        10/6/99  Petr Hamernik   Streams and Readers 
 *        correction.
 *   25   src-jtulach1.24        9/10/99  Petr Hamernik   comments
 *   24   src-jtulach1.23        9/9/99   Petr Hamernik   correct closing streams
 *        after parsing
 *   23   src-jtulach1.22        8/18/99  Petr Hamernik   fixed bug #2113
 *   22   src-jtulach1.21        8/18/99  Petr Hamernik   fixed bug #3173
 *   21   src-jtulach1.20        7/23/99  Petr Hamernik   global parsing listener
 *   20   src-jtulach1.19        6/9/99   Ian Formanek    ---- Package Change To 
 *        org.openide ----
 *   19   src-jtulach1.18        6/2/99   Petr Hamernik   connections of java 
 *        sources
 *   18   src-jtulach1.17        5/28/99  Ales Novak      lazy resolving of 
 *        identifiers
 *   17   src-jtulach1.16        5/26/99  Petr Hamernik   compilation bug ffixed
 *   16   src-jtulach1.15        5/21/99  Petr Hamernik   parsing info garbage 
 *        collected too fast - fixed
 *   15   src-jtulach1.14        5/15/99  Petr Hamernik   Identifiers resolving 
 *        temporary disabled
 *   14   src-jtulach1.13        5/14/99  Petr Hamernik   parsing errors during 
 *        resolving Identifiers are ignored
 *   13   src-jtulach1.12        5/12/99  Petr Hamernik   ide.src.Identifier 
 *        changed
 *   12   src-jtulach1.11        5/12/99  Petr Hamernik   javadoc parsing bugfix
 *   11   src-jtulach1.10        5/11/99  Petr Hamernik   emptyStackExecption bug
 *        fixed
 *   10   src-jtulach1.9         5/10/99  Petr Hamernik   
 *   9    src-jtulach1.8         4/30/99  Petr Hamernik   bounds bugfix
 *   8    src-jtulach1.7         4/21/99  Petr Hamernik   Java module updated
 *   7    src-jtulach1.6         4/15/99  Petr Hamernik   parser improvements
 *   6    src-jtulach1.5         4/14/99  Ian Formanek    Fixes problem with 
 *        programmatic usage of imports
 *   5    src-jtulach1.4         4/2/99   Petr Hamernik   
 *   4    src-jtulach1.3         4/1/99   Petr Hamernik   
 *   3    src-jtulach1.2         3/29/99  Petr Hamernik   
 *   2    src-jtulach1.1         3/29/99  Petr Hamernik   
 *   1    src-jtulach1.0         3/28/99  Ales Novak      
 *  $
 */
