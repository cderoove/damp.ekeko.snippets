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

import java.text.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.Modifier;

import org.openide.src.*;
import org.openide.util.NbBundle;

/** A format used to print members of the source hierarchy.
* It is sometimes used for code generation of elements, and also
* for formatting the display names of the nodes representing
* the hierarchy.
* <P>
*
* This format is similar to {@link MessageFormat}.
* It also uses special characters in the pattern and replaces them with strings,
* depending on the code.
* <P>
* For example:
* <p><CODE><PRE>
* ElementFormat fmt = new ElementFormat ("{m} {r} {n} ({p})");
* MethodElement method = getMethodSomewhere ();
* System.out.println (fmt.format (method));
* </PRE></CODE>
* <p>...should print something like this: <code>"public int method(int,char)"</code>
* 
* <p>The substitution codes are:
* <UL>
* <LI> <code>{m}</code> Modifiers
* <LI> <code>{n}</code> Name
* <LI> <code>{C}</code> Name of class (with all outerclasses)
* <LI> <code>{f}</code> Full name of element with package
* <LI> <code>{t}</code> Type
* <LI> <code>{r}</code> Return type
* <LI> <code>{s}</code> Superclass
* <LI> <code>{c}</code> Static (for initializers)
* <LI> <code>{p}</code> Parameters with types but not variable names (e.g. <code>"int,char"</code>).
* <LI> <code>{a}</code> Parameters with types and names (e.g. <code>"int x,char c"</code>).
* <LI> <code>{i}</code> Interfaces
* <LI> <code>{e}</code> Exceptions
* </UL>
* 
* <P>
* The following table shows which codes may be used
* to format which kinds of element. An asterisk means
* the code may be used, a hyphen means it cannot:
* 
* <p><CODE><PRE>
* character   | m  n  f  C  t  r  s  c  p  a  i  e
* ----------------------------------------------------
* Initializer | -  -  -  -  -  -  -  *  -  -  -  -
* Field       | *  *  *  -  *  -  -  -  -  -  -  -
* Constructor | *  *  *  -  -  -  -  -  *  *  -  *
* Method      | *  *  *  -  -  *  -  -  *  *  -  *
* Class       | *  *  *  *  -  -  *  -  -  -  *  -
* Interface   | *  *  *  *  -  -  -  -  -  -  *  -
* </PRE></CODE>
*
* <p>The grammar for expressions:
*
* <p><code><pre>
* messageFormatPattern := string ( "{" messageFormatElement "}" string )*
*
* messageFormatElement := simple_argument { "," prefix "," suffix }
*
* messageFormatElement := array_argument { "," prefix "," suffix { "," delim } }
*
* simple_argument := "m" | "n" | "f" | "C" | "t" | "r" | "s" | "c"
*
* array_argument := "p" | "a" | "i" | "e"
*
* prefix := string
*
* suffix := string
*
* delim  := string
*
* </pre></code>
*
* <p>Comments on the previous grammar:
* <UL>
* <LI> <code>simple_argument</code> - arguments which are replaced by a single string
* <LI> <code>array_argument</code> - arguments for arrays (parameters, ...)
* <LI> <code>prefix</code> - prefix before the format element if nonempty
* <LI> <code>suffix</code> - suffix after the format element if nonempty
* <LI> <code>delim</code> - delimiter between the members of the array
* <LI> <code>string</code> - a bare string, or enclosed in double quotes if necessary
* (e.g. if it contains a comma)
* </UL>
*
* <P>
* Example formats:
* <UL>
* <LI> For a method which doesn't throw any exceptions: <code>{e,throws ,}</code> => <code>""</code>
* <LI> For a method which throws <code>IOException</code>: <code>{e,throws ,</code>} => <code>"throws IOException"</code>
* <LI> Method parameters #1: <code>{p,,,-}</code> => <code>"int-int-int"</code>
* <LI> Method parameters #2: <code>{p,(,),", "}</code> => <code>"(int, int, int)"</code>
* </UL>
* <p>The default delimiter is a comma.
* <p>This class <em>currently</em> has a default property editor
* in the property editor search path for the IDE.
*
* @author Petr Hamernik
*/
public final class ElementFormat extends Format {

    // ============== Static part =================================

    /** Serial UID */
    static final long serialVersionUID = 3775521938640169753L;

    /** Resource bundle for this class. */
    static final ResourceBundle bundle = NbBundle.getBundle(ElementFormat.class);

    /** Messages localized strings */
    private static final String MSG_BAD_PATTERN = bundle.getString("MSG_badPattern");
    private static final String MSG_BAD_ARGUMENT = bundle.getString("MSG_badArgument");
    private static final String MSG_NO_PARSING = bundle.getString("MSG_noParsing");

    /** Magic characters for all kinds of the formating tags.
    * The position of the characters is used as index to the following array.
    */
    private static final String PROPERTIES_NAMES_INDEX = "mnfCtrscpaie"; // NOI18N

    /** Array of names of all kinds properties which could be included
    * in the pattern string.
    */
    private static final String[] PROPERTIES_NAMES = {
        ElementProperties.PROP_MODIFIERS,     //m
        ElementProperties.PROP_NAME,          //n
        ElementProperties.PROP_NAME,          //f
        ElementProperties.PROP_NAME,          //C
        ElementProperties.PROP_TYPE,          //t
        ElementProperties.PROP_RETURN,        //r
        ElementProperties.PROP_SUPERCLASS,    //s
        ElementProperties.PROP_STATIC,        //c
        ElementProperties.PROP_PARAMETERS,    //p
        ElementProperties.PROP_PARAMETERS,    //a
        ElementProperties.PROP_INTERFACES,    //i
        ElementProperties.PROP_EXCEPTIONS     //e
    };

    /** Status constants for the parser. */
    private static final byte STATUS_OUTSIDE = 0;
    private static final byte STATUS_INSIDE = 1;
    private static final byte STATUS_RBRACE = 2;

    // ================ Non-static part ===============================

    /** Pattern - the string which is given in the constructor. */
    private String pattern;

    /** The current value of "source" property */ // NOI18N
    private boolean source;

    /** List of parts of the formated string. Elements of this list are
    * either String objects either Tag.
    */
    private transient LinkedList list;

    // ================ Public methods =================================

    /** Create a new format.
    * See documentation for the class for the syntax of the format argument.
    * @param pattern the pattern describing the format
    */
    public ElementFormat(String pattern) {
        applyPattern(pattern);
        source = true;
    }

    /** Set whether the formating is used for code generation.
    * Default value is <CODE>true</CODE>.
    * @param source <CODE>true</CODE> means that all Identifier and Type objects
    *               used in formating are evaluated by <CODE>getSourceName</CODE>
    *               method. Otherwise (<CODE>false</CODE>) the getFullName() is
    *               called.
    */
    public void setSourceFormat(boolean source) {
        this.source = source;
    }

    /** Test if this format generate strings in source format or fully
    * qualified format.
    * @return the source flag
    */
    public boolean isSourceFormat() {
        return source;
    }

    /** Get the pattern.
    * @return the current pattern
    */
    public String getPattern() {
        return pattern;
    }

    /** Format an object.
    * @param o should be an {@link Element}
    * @param toAppendTo the string buffer to format to
    * @param pos currently ignored
    * @return the same string buffer it was passed (for convenient chaining)
    * @throws IllegalArgumentException if the object was not really an <code>Element</code>
    */
    public StringBuffer format(Object o, StringBuffer toAppendTo, FieldPosition pos) {
        try {
            Element element = (Element) o;
            Iterator it = list.iterator();
            while (it.hasNext()) {
                Object obj = it.next();
                if (obj instanceof String) {
                    toAppendTo.append((String)obj);
                }
                else {
                    ((Tag)obj).format(element, toAppendTo);
                }
            }
            return toAppendTo;
        }
        catch (ClassCastException e) {
            throw new IllegalArgumentException(MSG_BAD_ARGUMENT);
        }
    }

    /** Formats an element.
    * @param element the element to be printed
    * @return the formatted string using the pattern
    */
    public String format(Element element) {
        return format(element, new StringBuffer(), null).toString();
    }

    /** Test whether a property could affect the formatting.
    * I.e., if that property would be read due to one of the control codes in the pattern.
    * @param prop the property name from {@link ElementProperties}
    * @return <code>true</code> if so
    */
    public boolean dependsOnProperty(String prop) {
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof Tag) {
                int index = PROPERTIES_NAMES_INDEX.indexOf(((Tag)obj).kind);
                if (PROPERTIES_NAMES[index].equals(prop))
                    return true;
            }
        }
        return false;
    }

    /** Don't parse objects.
    * @param source ignored
    * @param status ignored
    * @return <code>null</code> in the default implementation
    */
    public Object parseObject (String source, ParsePosition status) {
        return null;
    }

    /** Reads the object and initialize fields. */
    private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        applyPattern(pattern);
    }

    // ====================== Private part ===================================

    /** Parse the pattern. */
    private void applyPattern(String pattern) {
        this.pattern = pattern;
        list = new LinkedList();

        byte status = STATUS_OUTSIDE;
        StringTokenizer tokenizer = new StringTokenizer(pattern, "{}", true); // NOI18N
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            switch (status) {

            case STATUS_OUTSIDE:
                if (token.equals("}")) // NOI18N
                    throw new IllegalArgumentException(MSG_BAD_PATTERN);
                if (token.equals("{")) // NOI18N
                    status = STATUS_INSIDE;
                else
                    list.add(token);
                break;

            case STATUS_INSIDE:
                if ((token.equals("{")) || (token.equals("}"))) // NOI18N
                    throw new IllegalArgumentException(MSG_BAD_PATTERN);
                list.add(createTag(token));
                status = STATUS_RBRACE;
                break;

            case STATUS_RBRACE:
                if (!token.equals("}")) // NOI18N
                    throw new IllegalArgumentException(MSG_BAD_PATTERN);
                status = STATUS_OUTSIDE;
                break;

            }
        }
    }

    /** Creates the appropriate tag for the given String.
    * @param s The string which is between the brackets in the pattern.
    * @return the tag object.
    */
    private Tag createTag(String s) {
        if (s.length() > 0) {
            char c = s.charAt(0);
            String[] params = new String[0];

            if (s.length() > 1) {
                if ((s.length() < 2) || (s.charAt(1) != ','))
                    throw new IllegalArgumentException(MSG_BAD_PATTERN);
                params = parseParams(s.substring(2));
            }

            if ("mnfCtrsc".indexOf(c) != -1) { // NOI18N
                switch (params.length) {
                case 0: return new Tag(c, "", ""); // NOI18N
                case 2: return new Tag(c, params[0], params[1]);
                }
            }
            else if ("paie".indexOf(c) != -1) { // NOI18N
                switch (params.length) {
                case 0: return new ArrayTag(c, "", "", ", "); // NOI18N
                case 2: return new ArrayTag(c, params[0], params[1], ", "); // NOI18N
                case 3: return new ArrayTag(c, params[0], params[1], params[2]);
                }
            }
        }
        throw new IllegalArgumentException(MSG_BAD_PATTERN);
    }

    /** Parse the parameters of the tag.
    * @param string of the params delimited by commas
    * @return the array of the params.
    */
    private String[] parseParams(String s) {
        StringTokenizer tokenizer = new StringTokenizer(s, ",", true); // NOI18N
        ArrayList list = new ArrayList();
        StringBuffer token = new StringBuffer();
        boolean comma = false;
        boolean inString = false;

        while (tokenizer.hasMoreTokens()) {
            String t = tokenizer.nextToken();

            if (inString) {
                token.append(t);
                if (t.endsWith("\"")) { // NOI18N
                    if (token.length() > 1)
                        token.setLength(token.length() - 1);
                    list.add(token.toString());
                    token.setLength(0);
                    inString = false;
                    comma = true;
                }
                continue;
            }

            if (t.equals(",")) { // NOI18N
                if (comma)
                    comma = false;
                else
                    list.add(""); // NOI18N
                continue;
            }

            if (comma)
                throw new IllegalArgumentException(MSG_BAD_PATTERN);

            String stringToAdd = t;

            if (t.startsWith("\"")) { // NOI18N
                if ((t.endsWith("\"")) && (t.length() > 1)) { // NOI18N
                    stringToAdd = (t.length() <= 2) ? "" : t.substring(1, t.length() - 1); // NOI18N
                }
                else {
                    token.append(t.substring(1));
                    inString = true;
                    continue;
                }
            }

            list.add(stringToAdd);
            comma = true;
            token.setLength(0);
        }
        if (!comma)
            list.add(""); // NOI18N

        String[] ret = new String[list.size()];
        list.toArray(ret);
        return ret;
    }

    private static void resolveClassName (StringBuffer sb, ClassElement element) {
        ClassElement c = element.getDeclaringClass ();
        if (c == null) {
            sb.append (element.getName ().getName ());
            return;
        }
        resolveClassName (sb, c);
        sb.append ('.').append (element.getName ().getName ());
    }

    /** Convert the Indentifier to string depending on "source" flag.
    */
    String identifierToString(Identifier id) {
        return source ? id.getSourceName() : id.getFullName();
    }

    /** Convert the Type to String depending on "source" flag.
    */
    String typeToString(Type id) {
        return source ? id.getSourceString() : id.getFullString();
    }

    /** Tag for simple types - m,n,t,r,s,c */
    private class Tag extends Object implements Serializable {
        /** Tag character */
        char kind;

        /** Prefix of the tag */
        String prefix;

        /** Suffix of the tag */
        String suffix;

        static final long serialVersionUID =4946774706959011193L;
        /** Creates the tag. */
        Tag(char kind, String prefix, String suffix) {
            this.kind = kind;
            this.prefix = prefix;
            this.suffix = suffix;
        }

        /** Formats this tag for the given element.
        * @param element Element to be formated.
        * @param buf StringBuffer where to add the formated string.
        */
        void format(Element element, StringBuffer buf) {
            try {
                int mark = buf.length();
                buf.append(prefix);

                switch (kind) {
                case 'm':
                    buf.append(Modifier.toString(((MemberElement)element).getModifiers()));
                    break;

                case 'n':
                    buf.append(identifierToString(((MemberElement)element).getName()));
                    break;

                case 'f':
                    buf.append(identifierToString(((MemberElement)element).getName()));
                    break;

                case 'C':
                    resolveClassName (buf, (ClassElement)element);
                    break;

                case 't':
                    buf.append(typeToString(((FieldElement)element).getType()));
                    break;

                case 'r':
                    buf.append(typeToString(((MethodElement)element).getReturn()));
                    break;

                case 's':
                    Identifier id = ((ClassElement)element).getSuperclass();
                    if (id != null)
                        buf.append(identifierToString(id));
                    break;

                case 'c':
                    if (((InitializerElement)element).isStatic())
                        buf.append(Modifier.toString(Modifier.STATIC));
                    break;
                }

                if (buf.length() > mark + prefix.length()) {
                    buf.append(suffix);
                }
                else {
                    buf.setLength(mark);
                }
            }
            catch (ClassCastException e) {
                throw new IllegalArgumentException(MSG_BAD_PATTERN);
            }
        }
    }

    /** Tag for arrays - params, exceptions, interfaces.
    */
    private class ArrayTag extends Tag {
        /** Delimiter */
        String delim;

        static final long serialVersionUID =2060398944304753010L;
        /** Creates new array tag. */
        ArrayTag(char kind, String prefix, String suffix, String delim) {
            super(kind, prefix, suffix);
            this.delim = delim;
        }

        /** Formats this tag for the given element.
        * @param element Element to be formated.
        * @param buf StringBuffer where to add the formated string.
        */
        void format(Element element, StringBuffer buf) {
            try {
                int mark = buf.length();
                buf.append(prefix);

                switch (kind) {
                case 'e':
                    Identifier[] ids = ((ConstructorElement)element).getExceptions();
                    for (int i = 0; i < ids.length; i++) {
                        if (i > 0)
                            buf.append(delim);
                        buf.append(identifierToString(ids[i]));
                    }
                    break;

                case 'p':
                case 'a':
                    MethodParameter[] args = ((ConstructorElement)element).getParameters();
                    for (int i = 0; i < args.length; i++) {
                        if (i > 0)
                            buf.append(delim);
                        if (kind == 'a') {
                            args[i].getAsString(buf, source);
                        }
                        else {
                            args[i].getType().getAsString(buf, source);
                        }
                    }
                    break;

                case 'i':
                    ids = ((ClassElement)element).getInterfaces();
                    for (int i = 0; i < ids.length; i++) {
                        if (i > 0)
                            buf.append(delim);
                        buf.append(identifierToString(ids[i]));
                    }
                    break;
                }

                if (buf.length() > mark + prefix.length()) {
                    buf.append(suffix);
                }
                else {
                    buf.setLength(mark);
                }
            }
            catch (ClassCastException e) {
                throw new IllegalArgumentException(MSG_BAD_PATTERN);
            }
        }
    }
}


/*
* Log
*  14   src-jtulach1.13        1/12/00  Petr Hamernik   i18n using perl script 
*       (//NOI18N comments added)
*  13   src-jtulach1.12        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  12   src-jtulach1.11        8/17/99  Ian Formanek    Generated serial version 
*       UID
*  11   src-jtulach1.10        7/13/99  Petr Hamernik   
*  10   src-jtulach1.9         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  9    src-jtulach1.8         5/13/99  Petr Hamernik   bugfix in printing method
*       params
*  8    src-jtulach1.7         5/12/99  Petr Hamernik   Identifier implementation
*       updated
*  7    src-jtulach1.6         4/26/99  Jesse Glick     [JavaDoc]
*  6    src-jtulach1.5         4/7/99   Jesse Glick     [JavaDoc]
*  5    src-jtulach1.4         4/2/99   Jesse Glick     [JavaDoc]
*  4    src-jtulach1.3         3/30/99  Jesse Glick     [JavaDoc]
*  3    src-jtulach1.2         3/30/99  Jan Jancura     C & f tags added
*  2    src-jtulach1.1         3/23/99  Petr Hamernik   
*  1    src-jtulach1.0         3/22/99  Petr Hamernik   
* $
*/
