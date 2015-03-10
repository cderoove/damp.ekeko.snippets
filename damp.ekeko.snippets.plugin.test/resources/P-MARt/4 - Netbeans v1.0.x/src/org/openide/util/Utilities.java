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

package org.openide.util;

import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
import java.lang.reflect.*;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import javax.swing.KeyStroke;

import java.awt.*;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Vector;

import org.openide.TopManager;

/** Otherwise uncategorized useful static methods.
*
* @author Jan Palka, Ian Formanek, Jaroslav Tulach
*/
final public class Utilities {
    private Utilities () {}

    /** Operating system is Windows NT. */
    public static final int OS_WINNT = 1;
    /** Operating system is Windows 95. */
    public static final int OS_WIN95 = 2;
    /** Operating system is Windows 98. */
    public static final int OS_WIN98 = 4;
    /** Operating system is Solaris. */
    public static final int OS_SOLARIS = 8;
    /** Operating system is Linux. */
    public static final int OS_LINUX = 16;
    /** Operating system is HP-UX. */
    public static final int OS_HP = 32;
    /** Operating system is IBM AIX. */
    public static final int OS_AIX = 64;
    /** Operating system is SGI IRIX. */
    public static final int OS_IRIX = 128;
    /** Operating system is Sun OS. */
    public static final int OS_SUNOS = 256;
    /** Operating system is DEC (Digital Unix). */
    public static final int OS_DEC = 512;
    /** Operating system is OS/2. */
    public static final int OS_OS2 = 1024;
    /** Operating system is Mac. */
    public static final int OS_MAC = 2048;
    /** Operating system is Windows 2000. */
    public static final int OS_WIN2000 = 4096;
    /** Operating system is unknown. */
    public static final int OS_OTHER = 65536;

    /** A mask for Windows platforms. */
    public static final int OS_WINDOWS_MASK = OS_WINNT | OS_WIN95 | OS_WIN98 | OS_WIN2000;
    /** A mask for Unix platforms. */
    public static final int OS_UNIX_MASK = OS_SOLARIS | OS_LINUX | OS_HP | OS_AIX | OS_IRIX | OS_SUNOS | OS_DEC;


    /** hashtable that maps allowed key names to their values (String, Integer) 
     * @associates Integer*/
    private static Map names;
    /** hashtable for mapping of values to their names 
     * @associates String*/
    private static Map values;

    static {
        initialize ();
    }

    /** Get the operating system on which the IDE is running.
    * @return one of the <code>OS_*</code> constants (such as {@link #OS_WINNT})
    */
    public static final int getOperatingSystem () {
        if (operatingSystem == -1) {
            String osName = System.getProperty ("os.name");
            if ("Windows NT".equals (osName)) // NOI18N
                operatingSystem = OS_WINNT;
            else if ("Windows 95".equals (osName)) // NOI18N
                operatingSystem = OS_WIN95;
            else if ("Windows 98".equals (osName)) // NOI18N
                operatingSystem = OS_WIN98;
            else if ("Windows 2000".equals (osName)) // NOI18N
                operatingSystem = OS_WIN2000;
            else if ("Solaris".equals (osName)) // NOI18N
                operatingSystem = OS_SOLARIS;
            else if (osName.startsWith ("SunOS")) // NOI18N
                operatingSystem = OS_SOLARIS;
            else if ("Linux".equals (osName)) // NOI18N
                operatingSystem = OS_LINUX;
            else if ("HP-UX".equals (osName)) // NOI18N
                operatingSystem = OS_HP;
            else if ("AIX".equals (osName)) // NOI18N
                operatingSystem = OS_AIX;
            else if ("Irix".equals (osName)) // NOI18N
                operatingSystem = OS_IRIX;
            else if ("SunOS".equals (osName)) // NOI18N
                operatingSystem = OS_SUNOS;
            else if ("Digital UNIX".equals (osName)) // NOI18N
                operatingSystem = OS_DEC;
            else if ("OS/2".equals (osName)) // NOI18N
                operatingSystem = OS_OS2;
            else if ("Mac OS".equals (osName)) // NOI18N
                operatingSystem = OS_MAC;
            else
                operatingSystem = OS_OTHER;
        }
        return operatingSystem;
    }

    /** Test whether the IDE is running on some variant of Windows.
    * @return <code>true</code> if Windows, <code>false</code> if some other manner of operating system
    */
    public static final boolean isWindows () {
        return (getOperatingSystem () & OS_WINDOWS_MASK) != 0;
    }

    /** Test whether the IDE is running on some variant of Unix.
    * Linux is included as well as the commercial vendors.
    * @return <code>true</code> some sort of Unix, <code>false</code> if some other manner of operating system
    */
    public static final boolean isUnix () {
        return (getOperatingSystem () & OS_UNIX_MASK) != 0;
    }

    /** The operating system on which NetBeans runs*/
    private static int operatingSystem = -1;

    /** Hashtable contains keywords. It is forbidden to use this
        keywords as a java identifier 
     * @associates String*/
    static java.util.Hashtable keywords;

    static {
        keywords = new java.util.Hashtable();
        keywords.put("abstract","abstract"); keywords.put("default","default"); // NOI18N
        keywords.put("if","if"); keywords.put("private","private"); // NOI18N
        keywords.put("throw","throw"); keywords.put("boolean","boolean"); // NOI18N
        keywords.put("do","do"); keywords.put("implements","implements"); // NOI18N
        keywords.put("protected","protected"); keywords.put("throws","throws"); // NOI18N
        keywords.put("break","break"); keywords.put("double","double"); // NOI18N
        keywords.put("import","import"); keywords.put("public","public"); // NOI18N
        keywords.put("transient","transient");keywords.put("byte","byte"); // NOI18N
        keywords.put("else","else");keywords.put("instanceof","instanceof"); // NOI18N
        keywords.put("return","return");keywords.put("try","try"); // NOI18N
        keywords.put("case","case");keywords.put("extends","extends"); // NOI18N
        keywords.put("int","int");keywords.put("short","short"); // NOI18N
        keywords.put("void","void");keywords.put("catch","catch"); // NOI18N
        keywords.put("final","final");keywords.put("interface","interface"); // NOI18N
        keywords.put("static","static");keywords.put("volatile","volatile"); // NOI18N
        keywords.put("char","char");keywords.put("finally","finally"); // NOI18N
        keywords.put("long","long");keywords.put("class","class"); // NOI18N
        keywords.put("while","while");keywords.put("super","super"); // NOI18N
        keywords.put("float","float");keywords.put("native","native"); // NOI18N
        keywords.put("switch","switch");keywords.put("const","const"); // NOI18N
        keywords.put("for","for");keywords.put("new","new"); // NOI18N
        keywords.put("synchronized","synchronized");keywords.put("continue","continue"); // NOI18N
        keywords.put("continue","continue");keywords.put("goto","goto"); // NOI18N
        keywords.put("package","package");keywords.put("this","this"); // NOI18N
        keywords.put("null","null");keywords.put("true","true"); // NOI18N
        keywords.put("false","false"); // NOI18N
    }


    /** Test whether a given string is a valid Java identifier.
    * @param id string which should be checked
    * @return <code>true</code> if a valid identifier
    */
    public static final boolean isJavaIdentifier(String id) {
        if (id == null) return false;
        if (id.equals("")) return false; // NOI18N
        if (!(java.lang.Character.isJavaIdentifierStart(id.charAt(0))) )
            return false;
        for (int i = 1; i < id.length(); i++) {
            if (!(java.lang.Character.isJavaIdentifierPart(id.charAt(i))) )
                return false;
        }
        // test if id is a keyword
        if (keywords.containsKey(id)) return false;

        return true;
    }

    /** Central method for obtaining <code>BeanInfo</code> for potential JavaBean classes.
    * This implementation provides additional functionality for Swing bean infos.
    * @param clazz class of the bean to provide the <code>BeanInfo</code> for
    * @return the bean info
    * @throws java.beans.IntrospectionException for the usual reasons
    * @see java.beans.Introspector#getBeanInfo(Class)
    */
    public static java.beans.BeanInfo getBeanInfo(Class clazz) throws java.beans.IntrospectionException {
        java.beans.BeanInfo bi;
        if (clazz.getName().startsWith("javax.swing")) // NOI18N
            bi = SwingEditors.scanAndSetBeanInfo(java.beans.Introspector.getBeanInfo(clazz));
        else
            bi = java.beans.Introspector.getBeanInfo(clazz);
        if (java.awt.Component.class.isAssignableFrom (clazz)) {
            java.beans.PropertyDescriptor[] pds = bi.getPropertyDescriptors ();
            for (int i = 0; i < pds.length; i++) {
                if (pds[i].getName ().equals ("cursor")) { // NOI18N
                    try {
                        Method getter = Component.class.getDeclaredMethod ("getCursor", new Class[0]); // NOI18N
                        Method setter = Component.class.getDeclaredMethod ("setCursor", new Class[] { Cursor.class }); // NOI18N
                        pds[i] = new java.beans.PropertyDescriptor ("cursor", getter, setter); // NOI18N
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        return bi;
    }

    /** Central method for obtaining <code>BeanInfo</code> for potential JavaBean classes, with a stop class.
    * This implementation provides additional functionality for Swing bean infos.
    * @param clazz class of the bean to provide the <code>BeanInfo</code> for
    * @param stopClass the stop class
    * @return the bean info
    * @throws java.beans.IntrospectionException for the usual reasons
    * @see java.beans.Introspector#getBeanInfo(Class, Class)
    */
    public static java.beans.BeanInfo getBeanInfo (Class clazz, Class stopClass) throws java.beans.IntrospectionException {
        if (clazz.getName().startsWith("javax.swing")) // NOI18N
            return SwingEditors.scanAndSetBeanInfo(java.beans.Introspector.getBeanInfo (clazz, stopClass));
        else return java.beans.Introspector.getBeanInfo(clazz, stopClass);
    }

    /** Wrap multi-line strings (and get the individual lines).
    * @param original  the original string to wrap
    * @param width     the maximum width of lines
    * @param wrapWords if <code>true</code>, the lines are wrapped on word boundaries (if possible);
    *                  if <code>false</code>, character boundaries are used
    * @param removeNewLines if <code>true</code>, any newlines in the original string are ignored
    * @return the lines after wrapping
    */
    public static String[] wrapStringToArray (String original, int width, boolean wrapWords, boolean removeNewLines) {
        // substitute original newlines with spaces,
        // remove newlines from head and tail
        if (removeNewLines) {
            while (original.startsWith ("\n")) // NOI18N
                original = original.substring (1);
            while (original.endsWith ("\n")) // NOI18N
                original = original.substring (0, original.length () - 1);
            original = original.replace ('\n', ' ');
        }

        if (width < 1) width = 1;
        if (original.length () <= width) {
            String s[] = new String [1];
            s [0] = original;
            return s;
        }

        java.util.Vector lines = new java.util.Vector ();
        int lineStart = 0; // the position of start of currently processed line in the original string
        int lastSpacePos = -1;
        for (int i = 0; i < original.length (); i++) {
            if (lineStart >= original.length () - 1)
                break;

            // newline in the original string
            if (original.charAt (i) == '\n') {
                lines.addElement (original.substring (lineStart, i));
                lineStart = i+1;
                lastSpacePos = -1;
                continue;
            }

            // remember last space position
            if (Character.isSpaceChar (original.charAt (i)))
                lastSpacePos = i;

            // last position in the original string
            if (i == original.length () - 1) {
                lines.addElement (original.substring (lineStart));
                break;
            }

            // reached width
            if (i - lineStart == width) {
                if (wrapWords && (lastSpacePos != -1)) {
                    lines.addElement (original.substring (lineStart, lastSpacePos));
                    lineStart = lastSpacePos + 1; // the space is consumed for the newline
                    lastSpacePos = -1;
                } else {
                    lines.addElement (original.substring (lineStart, i));
                    lineStart = i;
                    lastSpacePos = -1;
                }
            }
        }

        String s[] = new String [lines.size ()];
        lines.copyInto (s);
        return s;
    }

    /** Wrap multi-line strings.
    * @param original  the original string to wrap
    * @param width     the maximum width of lines
    * @param wrapWords if <code>true</code>, the lines are wrapped on word boundaries (if possible);
    *                  if <code>false</code>, character boundaries are used
    * @param removeNewLines if <code>true</code>, any newlines in the original string are ignored
    * @return the whole string with embedded newlines
    */
    public static String wrapString (String original, int width, boolean wrapWords, boolean removeNewLines) {
        // substitute original newlines with spaces,
        // remove newlines from head and tail
        if (removeNewLines) {
            while (original.startsWith ("\n")) // NOI18N
                original = original.substring (1);
            while (original.endsWith ("\n")) // NOI18N
                original = original.substring (0, original.length () - 1);
            original = original.replace ('\n', ' ');
        }

        if (width < 1) width = 1;
        if (original.length () <= width) return original;

        java.util.Vector lines = new java.util.Vector ();
        int lineStart = 0; // the position of start of currently processed line in the original string
        int lastSpacePos = -1;
        for (int i = 0; i < original.length (); i++) {
            if (lineStart >= original.length () - 1)
                break;

            // newline in the original string
            if (original.charAt (i) == '\n') {
                lines.addElement (original.substring (lineStart, i));
                lineStart = i+1;
                lastSpacePos = -1;
                continue;
            }

            // remember last space position
            if (Character.isSpaceChar (original.charAt (i)))
                lastSpacePos = i;

            // last position in the original string
            if (i == original.length () - 1) {
                lines.addElement (original.substring (lineStart));
                break;
            }

            // reached width
            if (i - lineStart == width) {
                if (wrapWords && (lastSpacePos != -1)) {
                    lines.addElement (original.substring (lineStart, lastSpacePos));
                    lineStart = lastSpacePos + 1; // the space is consumed for the newline
                    lastSpacePos = -1;
                } else {
                    lines.addElement (original.substring (lineStart, i));
                    lineStart = i;
                    lastSpacePos = -1;
                }
            }
        }

        StringBuffer retBuf = new StringBuffer ();
        for (java.util.Enumeration e = lines.elements (); e.hasMoreElements ();) {
            retBuf.append ((String) e.nextElement ());
            retBuf.append ('\n');
        }
        return retBuf.toString ();
    }

    /** Search-and-replace fixed string matches within a string.
    * @param original the original string
    * @param replaceFrom the substring to be find
    * @param replaceTo the substring to replace it with
    * @return a new string with all occurrences replaced
    */
    public static String replaceString (String original, String replaceFrom, String replaceTo) {
        int index = 0;
        if ("".equals (replaceFrom)) return original; // NOI18N

        StringBuffer buf = new StringBuffer ();
        while (true) {
            int pos = original.indexOf (replaceFrom, index);
            if (pos == -1) {
                buf.append (original.substring (index));
                return buf.toString ();
            }
            buf.append (original.substring (index, pos));
            buf.append (replaceTo);
            index = pos + replaceFrom.length ();
            if (index == original.length ())
                return buf.toString ();
        }
    }

    /** Turn full name of an inner class into its pure form.
    * @param fullName e.g. <code>some.pkg.SomeClass$Inner</code>
    * @return e.g. <code>Inner</code>
    */
    public static final String pureClassName (final String fullName) {
        final int index = fullName.indexOf('$');
        if ((index >= 0) && (index < fullName.length()))
            return fullName.substring(index+1, fullName.length());
        return fullName;
    }

    /** Test whether the operating system supports icons on frames (windows).
    * @return <code>true</code> if it does <em>not</em>
    *
    */
    public static final boolean isLargeFrameIcons() {
        return (getOperatingSystem () == OS_SOLARIS) || (getOperatingSystem () == OS_HP);
    }

    /** Compute hash code of array.
    * Asks all elements for their own code and composes the
    * values.
    * @param arr array of objects, can contain <code>null</code>s
    * @return the hash code
    * @see Object#hashCode
    */
    public static int arrayHashCode (Object[] arr) {
        int c = 0;
        int len = arr.length;
        for (int i = 0; i < len; i++) {
            Object o = arr[i];
            int v = o == null ? 1 : o.hashCode ();
            c += (v ^ i);
        }
        return c;
    }

    /** Safe equality check.
    * The supplied objects are equal if: <UL>
    * <LI> both are <code>null</code>
    * <LI> both are arrays with same length and equal items (if the items are arrays,
    *      they are <em>not</em> checked the same way again)
    * <LI> the two objects are {@link Object#equal}
    * </UL>
    * This method is <code>null</code>-safe, so if one of the parameters is true and the second not,
    * it returns <code>false</code>.
    * @param  o1 the first object to compare
    * @param  o2 the second object to compare
    * @return <code>true</code> if the objects are equal
    */
    public static boolean compareObjects (Object o1, Object o2) {
        return compareObjectsImpl (o1, o2, 1);
    }

    /** Safe equality check with array recursion.
    * @param  o1 the first object to compare
    * @param  o2 the second object to compare
    * @param  checkArraysDepth the depth to which arrays should be compared for equality (negative for infinite depth, zero for no comparison of elements, one for shallow, etc.)
    * @return <code>true</code> if the objects are equal
    * @see #compareObjects(Object, Object)
    */
    public static boolean compareObjectsImpl (Object o1, Object o2, int checkArraysDepth) {
        // handle null values
        if (o1 == null)
            return (o2 == null);
        else if (o2 == null) return false;

        // handle arrays
        if (checkArraysDepth > 0) {
            if ((o1 instanceof Object[]) && (o2 instanceof Object[])) {
                // Note: also handles multidimensional arrays of primitive types correctly.
                // I.e. new int[0][] instanceof Object[]
                Object[] o1a = (Object[]) o1;
                Object[] o2a = (Object[]) o2;
                int l1 = o1a.length;
                int l2 = o2a.length;
                if (l1 != l2) return false;
                for (int i = 0; i < l1; i++) {
                    if (! compareObjectsImpl (o1a[i], o2a[i], checkArraysDepth - 1)) {
                        return false;
                    }
                }
                return true;
            } else if ((o1 instanceof byte[]) && (o2 instanceof byte[])) {
                byte[] o1a = (byte[]) o1;
                byte[] o2a = (byte[]) o2;
                int l1 = o1a.length;
                int l2 = o2a.length;
                if (l1 != l2) return false;
                for (int i = 0; i < l1; i++)
                    if (o1a[i] != o2a[i]) return false;
                return true;
            } else if ((o1 instanceof short[]) && (o2 instanceof short[])) {
                short[] o1a = (short[]) o1;
                short[] o2a = (short[]) o2;
                int l1 = o1a.length;
                int l2 = o2a.length;
                if (l1 != l2) return false;
                for (int i = 0; i < l1; i++)
                    if (o1a[i] != o2a[i]) return false;
                return true;
            } else if ((o1 instanceof int[]) && (o2 instanceof int[])) {
                int[] o1a = (int[]) o1;
                int[] o2a = (int[]) o2;
                int l1 = o1a.length;
                int l2 = o2a.length;
                if (l1 != l2) return false;
                for (int i = 0; i < l1; i++)
                    if (o1a[i] != o2a[i]) return false;
                return true;
            } else if ((o1 instanceof long[]) && (o2 instanceof long[])) {
                long[] o1a = (long[]) o1;
                long[] o2a = (long[]) o2;
                int l1 = o1a.length;
                int l2 = o2a.length;
                if (l1 != l2) return false;
                for (int i = 0; i < l1; i++)
                    if (o1a[i] != o2a[i]) return false;
                return true;
            } else if ((o1 instanceof float[]) && (o2 instanceof float[])) {
                float[] o1a = (float[]) o1;
                float[] o2a = (float[]) o2;
                int l1 = o1a.length;
                int l2 = o2a.length;
                if (l1 != l2) return false;
                for (int i = 0; i < l1; i++)
                    if (o1a[i] != o2a[i]) return false;
                return true;
            } else if ((o1 instanceof double[]) && (o2 instanceof double[])) {
                double[] o1a = (double[]) o1;
                double[] o2a = (double[]) o2;
                int l1 = o1a.length;
                int l2 = o2a.length;
                if (l1 != l2) return false;
                for (int i = 0; i < l1; i++)
                    if (o1a[i] != o2a[i]) return false;
                return true;
            } else if ((o1 instanceof char[]) && (o2 instanceof char[])) {
                char[] o1a = (char[]) o1;
                char[] o2a = (char[]) o2;
                int l1 = o1a.length;
                int l2 = o2a.length;
                if (l1 != l2) return false;
                for (int i = 0; i < l1; i++)
                    if (o1a[i] != o2a[i]) return false;
                return true;
            } else if ((o1 instanceof boolean[]) && (o2 instanceof boolean[])) {
                boolean[] o1a = (boolean[]) o1;
                boolean[] o2a = (boolean[]) o2;
                int l1 = o1a.length;
                int l2 = o2a.length;
                if (l1 != l2) return false;
                for (int i = 0; i < l1; i++)
                    if (o1a[i] != o2a[i]) return false;
                return true;
            }
            // else not array type
        }

        // handle common objects--non-arrays, or arrays when depth == 0
        return o1.equals (o2);
    }

    /** Assemble a human-presentable class name for a specified class.
    * Arrays are represented as e.g. <code>java.lang.String[]</code>.
    * @param clazz the class to name
    * @return the human-presentable name
    */
    public static String getClassName (Class clazz) {
        // if it is an array, get short name of element type and append []
        if (clazz.isArray ())
            return getClassName (clazz.getComponentType ()) + "[]"; // NOI18N
        else
            return clazz.getName ();
    }

    /** Assemble a human-presentable class name for a specified class (omitting the package).
    * Arrays are represented as e.g. <code>String[]</code>.
    * @param clazz the class to name
    * @return the human-presentable name
    */
    public static String getShortClassName (Class clazz) {
        // if it is an array, get short name of element type and append []
        if (clazz.isArray ())
            return getShortClassName (clazz.getComponentType ()) + "[]"; // NOI18N

        String name = clazz.getName ().replace ('$', '.');
        return name.substring (name.lastIndexOf (".") + 1, name.length ()); // NOI18N
    }

    /**
    * Convert an array of objects to an array of primitive types.
    * E.g. an <code>Integer[]</code> would be changed to an <code>int[]</code>.
    * @param array the wrapper array
    * @return a primitive array
    * @throws IllegalArgumentException if the array element type is not a primitive wrapper
    */
    public static Object toPrimitiveArray (Object[] array) {
        if (array instanceof Integer[]) {
            int[] r = new int [array.length];
            int i, k = array.length;
            for (i = 0; i < k; i++) r [i] = (((Integer)array[i]) == null) ? 0 : ((Integer)array[i]).intValue ();
            return r;
        }
        if (array instanceof Boolean[]) {
            boolean[] r = new boolean [array.length];
            int i, k = array.length;
            for (i = 0; i < k; i++) r [i] = (((Boolean)array[i]) == null) ? false : ((Boolean)array[i]).booleanValue ();
            return r;
        }
        if (array instanceof Byte[]) {
            byte[] r = new byte [array.length];
            int i, k = array.length;
            for (i = 0; i < k; i++) r [i] = (((Byte)array[i]) == null) ? 0 : ((Byte)array[i]).byteValue ();
            return r;
        }
        if (array instanceof Character[]) {
            char[] r = new char [array.length];
            int i, k = array.length;
            for (i = 0; i < k; i++) r [i] = (((Character)array[i]) == null) ? 0 : ((Character)array[i]).charValue ();
            return r;
        }
        if (array instanceof Double[]) {
            double[] r = new double [array.length];
            int i, k = array.length;
            for (i = 0; i < k; i++) r [i] = (((Double)array[i]) == null) ? 0 : ((Double)array[i]).doubleValue ();
            return r;
        }
        if (array instanceof Float[]) {
            float[] r = new float [array.length];
            int i, k = array.length;
            for (i = 0; i < k; i++) r [i] = (((Float)array[i]) == null) ? 0 : ((Float)array[i]).floatValue ();
            return r;
        }
        if (array instanceof Long[]) {
            long[] r = new long [array.length];
            int i, k = array.length;
            for (i = 0; i < k; i++) r [i] = (((Long)array[i]) == null) ? 0 : ((Long)array[i]).longValue ();
            return r;
        }
        if (array instanceof Short[]) {
            short[] r = new short [array.length];
            int i, k = array.length;
            for (i = 0; i < k; i++) r [i] = (((Short)array[i]) == null) ? 0 : ((Short)array[i]).shortValue ();
            return r;
        }
        throw new IllegalArgumentException ();
    }

    /**
    * Convert an array of primitive types to an array of objects.
    * E.g. an <code>int[]</code> would be turned into an <code>Integer[]</code>.
    * @param array the primitive array
    * @return a wrapper array
    * @throws IllegalArgumentException if the array element type is not primitive
    */
    public static Object[] toObjectArray (Object array) {
        if (array instanceof Object[]) return (Object[]) array;
        if (array instanceof int[]) {
            int i, k = ((int[])array).length;
            Integer[] r = new Integer [k];
            for (i = 0; i < k; i++) r [i] = new Integer (((int[]) array)[i]);
            return r;
        }
        if (array instanceof boolean[]) {
            int i, k = ((boolean[])array).length;
            Boolean[] r = new Boolean [k];
            for (i = 0; i < k; i++) r [i] = new Boolean (((boolean[]) array)[i]);
            return r;
        }
        if (array instanceof byte[]) {
            int i, k = ((byte[])array).length;
            Byte[] r = new Byte [k];
            for (i = 0; i < k; i++) r [i] = new Byte (((byte[]) array)[i]);
            return r;
        }
        if (array instanceof char[]) {
            int i, k = ((char[])array).length;
            Character[] r = new Character [k];
            for (i = 0; i < k; i++) r [i] = new Character (((char[]) array)[i]);
            return r;
        }
        if (array instanceof double[]) {
            int i, k = ((double[])array).length;
            Double[] r = new Double [k];
            for (i = 0; i < k; i++) r [i] = new Double (((double[]) array)[i]);
            return r;
        }
        if (array instanceof float[]) {
            int i, k = ((float[])array).length;
            Float[] r = new Float [k];
            for (i = 0; i < k; i++) r [i] = new Float (((float[]) array)[i]);
            return r;
        }
        if (array instanceof long[]) {
            int i, k = ((long[])array).length;
            Long[] r = new Long [k];
            for (i = 0; i < k; i++) r [i] = new Long (((long[]) array)[i]);
            return r;
        }
        if (array instanceof short[]) {
            int i, k = ((short[])array).length;
            Short[] r = new Short [k];
            for (i = 0; i < k; i++) r [i] = new Short (((short[]) array)[i]);
            return r;
        }
        throw new IllegalArgumentException ();
    }

    /**
    * Get the object type for given primitive type.
    *
    * @param c primitive type (e.g. <code>int</code>)
    * @return object type (e.g. <code>Integer</code>)
    */
    public static Class getObjectType (Class c) {
        if (!c.isPrimitive ()) return c;
        if (c == Integer.TYPE) return Integer.class;
        if (c == Boolean.TYPE) return Boolean.class;
        if (c == Byte.TYPE) return Byte.class;
        if (c == Character.TYPE) return Character.class;
        if (c == Double.TYPE) return Double.class;
        if (c == Float.TYPE) return Float.class;
        if (c == Long.TYPE) return Long.class;
        if (c == Short.TYPE) return Short.class;
        throw new IllegalArgumentException ();
    }

    /**
    * Get the primitive type for given object type.
    *
    * @param c object type (e.g. <code>Integer</code>)
    * @return primitive type (e.g. <code>int</code>)
    */
    public static Class getPrimitiveType (Class c) {
        if (!c.isPrimitive ()) return c;
        if (c == Integer.class) return Integer.TYPE;
        if (c == Boolean.class) return Boolean.TYPE;
        if (c == Byte.class) return Byte.TYPE;
        if (c == Character.class) return Character.TYPE;
        if (c == Double.class) return Double.TYPE;
        if (c == Float.class) return Float.TYPE;
        if (c == Long.class) return Long.TYPE;
        if (c == Short.class) return Short.TYPE;
        throw new IllegalArgumentException ();
    }

    /** Find a focus-traverable component.
    * @param c the component to look in
    * @return the same component if traversable, else a child component if present, else <code>null</code>
    * @see Component#isFocusTraversable
    */
    public static Component getFocusTraversableComponent (Component c) {
        if (c.isFocusTraversable ()) return c;
        if (!(c instanceof Container)) return null;
        int i, k = ((Container)c).getComponentCount ();
        for (i = 0; i < k; i++) {
            Component v = ((Container)c).getComponent (i);
            if (v != null) return v;
        }
        return null;
    }

    /** Parses parameters from a given string in shell-like manner.
    * Users of the Bourne shell (e.g. on Unix) will already be familiar with the behavior.
    * Otherwise, see {@link org.openide.execution.NbProcessDescriptor#getProcessArgs}
    * for examples of what it does.
    * @param s a string to parse
    * @return an array of parameters
    */
    public static String[] parseParameters(String s) {
        int NULL = 0x0;  // STICK + whitespace or NULL + non_"
        int INPARAM = 0x1; // NULL + " or STICK + " or INPARAMPENDING + "\ // NOI18N
        int INPARAMPENDING = 0x2; // INPARAM + \
        int STICK = 0x4; // INPARAM + " or STICK + non_" // NOI18N
        int STICKPENDING = 0x8; // STICK + \
        Vector params = new Vector(5,5);
        char c;

        int state = NULL;
        StringBuffer buff = new StringBuffer(20);
        int slength = s.length();
        for (int i = 0; i < slength; i++) {
            c = s.charAt(i);
            if (Character.isWhitespace(c)) {
                if (state == NULL) {
                    if (buff.length () > 0) {
                        params.addElement(buff.toString());
                        buff.setLength(0);
                    }
                } else if (state == STICK) {
                    params.addElement(buff.toString());
                    buff.setLength(0);
                    state = NULL;
                } else if (state == STICKPENDING) {
                    buff.append('\\');
                    params.addElement(buff.toString());
                    buff.setLength(0);
                    state = NULL;
                } else if (state == INPARAMPENDING) {
                    state = INPARAM;
                    buff.append('\\');
                    buff.append(c);
                } else {    // INPARAM
                    buff.append(c);
                }
                continue;
            }

            if (c == '\\') {
                if (state == NULL) {
                    ++i;
                    if (i < slength) {
                        char cc = s.charAt(i);
                        if (cc == '"' || cc == '\\') {
                            buff.append(cc);
                        } else if (Character.isWhitespace(cc)) {
                            buff.append(c);
                            --i;
                        } else {
                            buff.append(c);
                            buff.append(cc);
                        }
                    } else {
                        buff.append('\\');
                        break;
                    }
                    continue;
                } else if (state == INPARAM) {
                    state = INPARAMPENDING;
                } else if (state == INPARAMPENDING) {
                    buff.append('\\');
                    state = INPARAM;
                } else if (state == STICK) {
                    state = STICKPENDING;
                } else if (state == STICKPENDING) {
                    buff.append('\\');
                    state = STICK;
                }
                continue;
            }

            if (c == '"') {
                if (state == NULL) {
                    state = INPARAM;
                } else if (state == INPARAM) {
                    state = STICK;
                } else if (state == STICK) {
                    state = INPARAM;
                } else if (state == STICKPENDING) {
                    buff.append('"');
                    state = STICK;
                } else { // INPARAMPENDING
                    buff.append('"');
                    state = INPARAM;
                }
                continue;
            }

            if (state == INPARAMPENDING) {
                buff.append('\\');
                state = INPARAM;
            } else if (state == STICKPENDING) {
                buff.append('\\');
                state = STICK;
            }
            buff.append(c);
        }
        // collect
        if (state == INPARAM) {
            params.addElement(buff.toString());
        } else if ((state & (INPARAMPENDING | STICKPENDING)) != 0) {
            buff.append('\\');
            params.addElement(buff.toString());
        } else { // NULL or STICK
            if (buff.length() != 0) {
                params.addElement(buff.toString());
            }
        }
        String[] ret = new String[params.size()];
        params.copyInto(ret);
        return ret;
    }


    //
    // Key conversions
    //


    /** Initialization of the names and values
    */
    private static void initialize () {
        names = new HashMap ();
        values = new HashMap ();

        Field[] fields = KeyEvent.class.getDeclaredFields ();

        for (int i = 0; i < fields.length; i++) {
            if (Modifier.isStatic (fields[i].getModifiers ())) {
                String name = fields[i].getName ();
                if (name.startsWith ("VK_")) { // NOI18N
                    // exclude VK
                    name = name.substring (3);
                    try {
                        int numb = fields[i].getInt (null);
                        Integer value = new Integer (numb);
                        names.put (name, value);
                        values.put (value, name);
                    } catch (IllegalArgumentException ex) {
                    } catch (IllegalAccessException ex) {
                    }
                }
            }
        }
    }


    /** Converts a Swing key stroke descriptor to a familiar Emacs-like name.
    * @param stroke key description
    * @return name of the key (e.g. <code>CS-F1</code> for control-shift-function key one)
    * @see #stringToKey
    */
    public static String keyToString (KeyStroke stroke) {
        StringBuffer sb = new StringBuffer ();

        // add modifiers that must be pressed
        if (addModifiers (sb, stroke.getModifiers ())) {
            sb.append ('-');
        }

        String c = (String)values.get (new Integer (stroke.getKeyCode ()));
        if (c == null) {
            sb.append (stroke.getKeyChar ());
        } else {
            sb.append (c);
        }

        return sb.toString ();
    }

    /** Construct a new key description from a given universal string
    * description.
    * Provides mapping between Emacs-like textual key descriptions and the
    * <code>KeyStroke</code> object used in Swing.
    * <P>
    * This format has following form:
    * <P><code>[C][A][S][M]-<em>identifier</em></code>
    * <p>Where:
    * <UL>
    * <LI> <code>C</code> stands for the Control key
    * <LI> <code>A</code> stands for the Alt key
    * <LI> <code>S</code> stands for the Shift key
    * <LI> <code>M</code> stands for the Meta key
    * </UL>
    * Every modifier before the hyphen must be pressed.
    * <em>identifier</EM> can be any text constant from {@link KeyEvent} but
    * without the leading <code>VK_</code> characters. So {@link KeyEvent#VK_ENTER} is described as
    * <code>ENTER</code>.
    *
    * @param s the string with the description of the key
    * @return key description object, or <code>null</code> if the string does not represent any valid key
    */
    public static KeyStroke stringToKey (String s) {
        StringTokenizer st = new StringTokenizer (s.toUpperCase (), "-", true); // NOI18N

        int needed = 0;

        int lastModif = -1;
        try {
            for (;;) {
                String el = st.nextToken ();
                // required key
                if (el.equals ("-")) { // NOI18N
                    if (lastModif != -1) {
                        needed |= lastModif;
                        lastModif = -1;
                    }
                    continue;
                }
                // if there is more elements
                if (st.hasMoreElements ()) {
                    // the text should describe modifiers
                    lastModif = readModifiers (el);
                } else {
                    // last text must be the key code
                    Integer i = (Integer)names.get (el);
                    if (i != null) {
                        return KeyStroke.getKeyStroke (i.intValue (), needed);
                    } else {
                        return null;
                    }
                }
            }
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    /** Convert a space-separated list of user-friendly key binding names to a list of Swing key strokes.
    * @param s the string with keys
    * @return array of key strokes, or <code>null</code> if the string description is not valid
    * @see #stringToKey
    */
    public static KeyStroke[] stringToKeys (String s) {
        StringTokenizer st = new StringTokenizer (s.toUpperCase (), " "); // NOI18N
        ArrayList arr = new ArrayList ();

        while (st.hasMoreElements ()) {
            s = st.nextToken ();
            KeyStroke k = stringToKey (s);
            if (k == null) return null;
            arr.add (k);
        }

        return (KeyStroke[])arr.toArray (new KeyStroke[arr.size ()]);
    }


    /** Adds characters for modifiers to the buffer.
    * @param buf buffer to add to
    * @param modif modifiers to add (KeyEvent.XXX_MASK)
    * @return true if something has been added
    */
    private static boolean addModifiers (StringBuffer buf, int modif) {
        boolean b = false;

        if ((modif & KeyEvent.CTRL_MASK) != 0) {
            buf.append("C"); // NOI18N
            b = true;
        }
        if ((modif & KeyEvent.ALT_MASK) != 0) {
            buf.append("A"); // NOI18N
            b = true;
        }
        if ((modif & KeyEvent.SHIFT_MASK) != 0) {
            buf.append("S"); // NOI18N
            b = true;
        }
        if ((modif & KeyEvent.META_MASK) != 0) {
            buf.append("M"); // NOI18N
            b = true;
        }

        return b;
    }

    /** Reads for modifiers and creates integer with required mask.
    * @param s string with modifiers
    * @return integer with mask
    * @exception NoSuchElementException if some letter is not modifier
    */
    private static int readModifiers (String s) throws NoSuchElementException {
        int m = 0;
        for (int i = 0; i < s.length (); i++) {
            switch (s.charAt (i)) {
            case 'C':
                m |= KeyEvent.CTRL_MASK;
                break;
            case 'A':
                m |= KeyEvent.ALT_MASK;
                break;
            case 'M':
                m |= KeyEvent.META_MASK;
                break;
            case 'S':
                m |= KeyEvent.SHIFT_MASK;
                break;
            default:
                throw new NoSuchElementException ();
            }
        }
        return m;
    }

    /** Exception indicating that a given list could not be partially-ordered.
    * @see #partialSort
    */
    public static class UnorderableException extends RuntimeException {
        private Collection unorderable;
        private Map deps;

        static final long serialVersionUID =6749951134051806661L;
        /** Create a new unorderable-list exception with no detail message.
        * @param unorderable a collection of list elements which could not be ordered
        *                    (because there was some sort of cycle)
        * @param deps dependencies associated with the list; a map from list elements
        *             to sets of list elements which that element must appear after
        */
        public UnorderableException (Collection unorderable, Map deps) {
            super (/* "Cannot be ordered: " + unorderable */); // NOI18N
            this.unorderable = unorderable;
            this.deps = deps;
        }

        /** Create a new unorderable-list exception with a specified detail message.
        * @param message the detail message
        * @param unorderable a collection of list elements which could not be ordered
        *                    (because there was some sort of cycle)
        * @param deps dependencies associated with the list; a map from list elements
        *             to sets of list elements which that element must appear after
        */
        public UnorderableException (String message, Collection unorderable, Map deps) {
            super (message);
            this.unorderable = unorderable;
            this.deps = deps;
        }

        /** Get the unorderable elements.
        * @return the elements
        * @see #UnorderableException(Collection,Map)
        */
        public Collection getUnorderable () {
            return unorderable;
        }

        /** Get the dependencies.
        * @return the dependencies
        * @see #UnorderableException(Collection,Map)
        */
        public Map getDeps () {
            return deps;
        }

    }

    /** Sort a list according to a specified partial order.
    * Note that in the current implementation, the comparator will be called
    * exactly once for each distinct pair of list elements, ignoring order,
    * so caching its results is a waste of time.
    * @param l the list to sort (will not be modified)
    * @param c a comparator to impose the partial order; "equal" means that the elements
    *          are not ordered with respect to one another, i.e. may be only a partial order
    * @param stable whether to attempt a stable sort, meaning that the position of elements
    *               will be disturbed as little as possible; might be slightly slower
    * @return the partially-sorted list
    * @throws UnorderableException if the specified partial order is inconsistent on this list
    */
    public static List partialSort (List l, Comparator c, boolean stable) throws UnorderableException {
        // map from objects in the list to null or sets of objects they are greater than
        // (i.e. must appear after):
        Map deps = new HashMap (); // Map<Object,Set<Object>>
        int size = l.size ();
        // Create a table of dependencies.
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                int cmp = c.compare (l.get (i), l.get (j));
                if (cmp != 0) {
                    Object earlier = l.get (cmp < 0 ? i : j);
                    Object later = l.get (cmp > 0 ? i : j);
                    Set s = (Set) deps.get (later);
                    if (s == null)
                        deps.put (later, s = new HashSet ());
                    s.add (earlier);
                }
            }
        }
        // Lists of items to process, and items sorted.
        List left = new LinkedList (l);
        List sorted = new ArrayList (size);
        while (left.size () > 0) {
            boolean stillGoing = false;
            Iterator it = left.iterator ();
            while (it.hasNext ()) {
                Object elt = it.next ();
                Set eltDeps = (Set) deps.get (elt);
                if (eltDeps == null || eltDeps.size () == 0) {
                    // This one is OK to add to the result now.
                    it.remove ();
                    stillGoing = true;
                    sorted.add (elt);
                    // Mark other elements that should be later
                    // than this as having their dep satisfied.
                    Iterator it2 = left.iterator ();
                    while (it2.hasNext ()) {
                        Object elt2 = it2.next ();
                        Set eltDeps2 = (Set) deps.get (elt2);
                        if (eltDeps2 != null) eltDeps2.remove (elt);
                    }
                    if (stable) break;
                }
            }
            if (! stillGoing) throw new UnorderableException (left, deps);
        }
        return sorted;
    }
    /** cache for old_name:new_name mapping */
    private static Reference nameMap;

    /** Handles repackaging whole IDE for runtime compatibility.
      * It should not be used by new code except during a package rename.
      *
      * @param name fully qualified name of a class to translate
      * @exception NullPointerException iff <tt>className</tt> is <tt>null</tt>
      */
    public static String translate(final String className) {
        
        if (className.length() == 0) {
            return className;
        }
        
        String name;
        int arrayPrefix;
        if (className.charAt(0) == '[') {
            for (arrayPrefix = 1; className.charAt(arrayPrefix) == '['; arrayPrefix++);
            if (className.charAt(arrayPrefix++) != 'L') {
                return className;
            }
            name = className.substring(arrayPrefix);
        } else {
            name = className;
            arrayPrefix = -1;
        }

        // Obviously this shortcut depends on the contents on the packages.txt:
        if (name.startsWith ("com" + ".netbeans.") ||
                name.startsWith ("com" + ".sun.forte4j.")) {

            String packageName;
            int dot = name.lastIndexOf('.');
            if ((dot > 0) &&
                    (dot != name.length() - 1)) { // last char
                packageName = name.substring(0, dot);
            } else {
                return className;
            }

            try {
                // line_no * 2
                String[][] map;

                // acquire mapping
                if (nameMap == null || (map = (String[][]) nameMap.get()) == null) {
                    map = loadNameMapping();
                    nameMap = new SoftReference(map);
                }

                // search for "the" name
                final int mapsize = map.length;
                for (int i = 0; i < mapsize; i++) {
                    if (packageName.startsWith(map[i][0])) {
                        if (arrayPrefix < 0) {
                            return map[i][1] + name.substring(map[i][0].length());
                        } else {
                            return className.substring(0, arrayPrefix) + map[i][1] + name.substring(map[i][0].length());
                        }
                    }
                }
            } catch (IOException e) {
                TopManager.getDefault().notifyException(e);
            }
        }
        // default
        return className;
    }

    /** Loads name mapping from file org/openide/util/packages.txt */
    private static String[][] loadNameMapping() throws IOException {
        BufferedReader reader =
            new BufferedReader(
                new InputStreamReader(
                    Utilities.class.getClassLoader().getResourceAsStream("org/openide/util/packages.txt")
                )
            );

        ArrayList chunks = new ArrayList(50);
        for (;;) {
            String line = reader.readLine();
            String[] pair = parseLine(line);
            if (pair == null) { // EOF
                break;
            }
            chunks.add(pair);
        }

        Collections.sort(chunks, new StringArrayComparator());
        final int pairslen = chunks.size();
        String[][] mapping = new String[pairslen][2];
        for (int i = 0; i < pairslen; i++) {
            String[] chunk = (String[]) chunks.get(i);
            mapping[i][0] = chunk[0];
            mapping[i][1] = chunk[1];
        }
        return mapping;
    }

    private static String[] parseLine(final String line) {
        if (line == null) {
            return null;
        }
        final int slen = line.length();
        int space = line.indexOf(' ');
        if (space <= 0 || (space == slen - 1)) {
            return null;
        }
        String[] chunk = new String[] {line.substring(0, space), null};

        space++;
        int c;
        while ((space < slen) && (line.charAt(space++) == ' '));
        if (space == slen) {
            return null;
        }
        String token = line.substring(--space);
        token = token.trim();
        chunk[1] = token;
        return chunk;
    }

    /** Compares to object by length of String returned by toString(). */
    static final class StringArrayComparator implements Comparator {
        public boolean equals(Object o) {
            return super.equals(o);
        }

        public int compare(Object o1, Object o2) {
            String[] s1 = (String[]) o1;
            String[] s2 = (String[]) o2;
            return (s2[0].length() - s1[0].length());
        }
    }
}

/*
 * Log
 *  31   Gandalf   1.30        4/15/00  Jesse Glick     Fixed translate (it did 
 *       not produce the correct result).
 *  30   Gandalf   1.29        4/15/00  Jesse Glick     Even more minor 
 *       massages.
 *  29   Gandalf   1.28        4/14/00  Jesse Glick     Minor massages to 
 *       translate function.
 *  28   Gandalf   1.27        4/14/00  Ales Novak      translate() added
 *  27   Gandalf   1.26        2/14/00  Ian Formanek    Added constant for 
 *       Windows 2000, Win2000 correctly recognized as "isWindows"
 *  26   Gandalf   1.25        1/15/00  Pavel Buzek     #5326
 *  25   Gandalf   1.24        1/13/00  Ian Formanek    NOI18N
 *  24   Gandalf   1.23        1/12/00  Pavel Buzek     I18N
 *  23   Gandalf   1.22        1/7/00   Pavel Buzek     create new property 
 *       descriptor for cursor property of java.awt.Component subclasses
 *  22   Gandalf   1.21        11/26/99 Patrik Knakal   
 *  21   Gandalf   1.20        11/25/99 Jesse Glick     Added partialSort 
 *       function.
 *  20   Gandalf   1.19        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  19   Gandalf   1.18        9/7/99   Ian Formanek    "default" is correctly 
 *       treated as not being Java identifier
 *  18   Gandalf   1.17        7/13/99  Jesse Glick     Fixed to handle arrays 
 *       of primitives.
 *  17   Gandalf   1.16        6/10/99  Jaroslav Tulach Recognizes SunOSxxxxx as
 *       OS_SOLARIS
 *  16   Gandalf   1.15        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  15   Gandalf   1.14        6/4/99   Ales Novak      fix for parsing of 
 *       commandline
 *  14   Gandalf   1.13        5/15/99  Jesse Glick     [JavaDoc], and 
 *       constructor private for stylistic reasons.
 *  13   Gandalf   1.12        4/1/99   Jan Jancura     bug in getShortClassName
 *  12   Gandalf   1.11        3/26/99  David Simonek   pureName removed (was 
 *       useless)
 *  11   Gandalf   1.10        3/21/99  Jaroslav Tulach Keys.
 *  10   Gandalf   1.9         3/14/99  Jaroslav Tulach Change of 
 *       MultiDataObject.Entry.
 *  9    Gandalf   1.8         3/12/99  Jaroslav Tulach 
 *  8    Gandalf   1.7         3/8/99   Ian Formanek    Removed 
 *       getMultiLineString
 *  7    Gandalf   1.6         3/4/99   Petr Hamernik   
 *  6    Gandalf   1.5         3/4/99   Jaroslav Tulach API cleaning
 *  5    Gandalf   1.4         3/4/99   Petr Hamernik   
 *  4    Gandalf   1.3         2/10/99  David Simonek   
 *  3    Gandalf   1.2         2/3/99   David Simonek   
 *  2    Gandalf   1.1         1/20/99  David Simonek   rework of class DO
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
