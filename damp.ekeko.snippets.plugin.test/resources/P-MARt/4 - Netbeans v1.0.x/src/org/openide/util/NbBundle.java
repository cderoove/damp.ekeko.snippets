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

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.Attributes;
import java.text.MessageFormat;

/** Convenience class permitting easy loading of localized resources of various sorts.
* Extends the functionality of the default Java resource support, and interacts
* better with class loaders in a multiple-loader system.
* <p>Example usage:
* <p><code><pre>
* package com.mycom;
* public class Foo {
*   // Search for tag Foo_theMessage in /com/mycom/Bundle.properties:
*   private static String theMessage = {@link NbBundle#getBundle(Class) NbBundle.getBundle} (Foo.class).{@link ResourceBundle#getString(String) getString} ("Foo_theMessage");
*   // Might also look in /com/mycom/Bundle_de.properties, etc.
* }
* </pre></code>
*
* @author   Petr Hamernik, Jaroslav Tulach
* @version  0.33, Apr 30, 1998
*/
public class NbBundle extends Object {
    /** encoding that should be used to load localized files for corona
    */
    private static final String RESOURCE_ENCODING = "Cp1250"; // NOI18N

    private static final boolean DEBUG = Boolean.getBoolean ("org.openide.util.NbBundle.DEBUG"); // NOI18N

    /** Cache of URLs for localized files 
     * @associates URL*/
    private static Hashtable cacheListFiles = new Hashtable();

    /** Cache of ResourceBundles 
     * @associates RefBundle*/
    private static Hashtable cacheList = new Hashtable();

    /** Default class loader */
    private static SystemClassLoader defaultLoader = new SystemClassLoader();

    /* This interface has method for resolving the class loader. When someone
    *  calls resourceBundle to get any resource or class, we would like to use caller's
    *  class loader, because not all classes have to use the same.
    *  So if the ClassLoaderFinder is set, we use method find to resolve
    *  the appropriate classloader. Method find should look at stack and takes
    *  class at 5-th position (stack[4]) and return its classloader (it can be also null)
    */
    /** Utility to find the class loader of the class calling a method (for use only by implementation).
    * Used to find the resource bundle appropriate to a class in a different loader.
    * @see NbBundle#setClassLoaderFinder
    */
    public static interface ClassLoaderFinder {
        /** Get the class loader of the calling method.
        * @return class loader of caller
        */
        public ClassLoader find();
    }

    /** Class Loader finder */
    private static ClassLoaderFinder loaderFinder;

    /* This method is used for set the classloader finder. It can be called just once
    * and then NbBundle class uses the given loaderfinder for finding the
    * adequate ClassLoader.
    */
    /** Set the class loader finder (for use only by implementation).
    * @param loaderFinder the new finder
    * @throws Error if already set
    */
    public static void setClassLoaderFinder(ClassLoaderFinder loaderFinder) {
        if (NbBundle.loaderFinder == null)
            NbBundle.loaderFinder = loaderFinder;
        else
            throw new Error(NbBundle.getBundle(NbBundle.class).getString("MSG_ClassFinderAlreadySet"));
    }

    /** Interface for all recognizer */
    private static interface Recognizer {
        /** Tests if recognize the resource with the specific name in the loader.
        * @param loader classloader
        * @param name The name of resource
        * @return true if resource can be loaded using specific classloader.
        */
        public boolean recognize(ClassLoader loader, String name);
    }

    /** Class recognizer is used for recognizing localized files with
    * the specific extension.
    */
    private static class ExtensionRecognizer implements Recognizer {
        /** extension for recognizing objects. */
        public String ext;

        /** recognized object */
        public URL recognURL;

        /** Creates new ExtensionRecognizer for the specific extension */
        public ExtensionRecognizer(String ext) {
            this.ext = ext;
        }

        /** Tests if recognize the resource with the specific name in the loader.
        * @param loader classloader
        * @param name The name of resource
        * @return true if resource can be loaded using specific classloader.
        */
        public boolean recognize(ClassLoader loader, String name) {
            recognURL = loader.getResource(name.replace('.', '/') + '.' + ext);
            return (recognURL != null);
        }
    }

    /** Class recognizer is used for recognizing classes.
    */
    private static class ClassRecognizer implements Recognizer {
        /** recognized object */
        public Object recognObject = null;

        public ClassRecognizer() {
        }

        /** Tests if recognize the resource with the specific name in the loader.
        * @param loader classloader
        * @param name The name of resource
        * @return true if resource can be loaded using specific classloader.
        */
        public boolean recognize(ClassLoader loader, String name) {
            try {
                recognObject = loader.loadClass(name).newInstance();
                return true;
            }
            catch(Exception e) {
                return false;
            }
        }
    }

    /** Get a localized file in the default locale with the default class loader.
    * @param baseName base name of file, as dot-separated path (e.g. <code>some.dir.File</code>)
    * @param ext      extension of file
    * @return URL of matching localized file
    * @throws MissingResourceException if not found
    */
    public static synchronized URL getLocalizedFile(String baseName, String ext)
    throws MissingResourceException {
        return getLocalizedFile(baseName, ext, Locale.getDefault(), getLoader());
    }

    /** Get a localized file with the default class loader.
    * @param baseName base name of file, as dot-separated path (e.g. <code>some.dir.File</code>)
    * @param ext      extension of file
    * @param locale   locale of file
    * @return URL of matching localized file
    * @throws MissingResourceException if not found
    */
    public static synchronized URL getLocalizedFile(String baseName, String ext,
            Locale locale) throws MissingResourceException {
        return getLocalizedFile(baseName, ext, locale, getLoader());
    }

    /** Get a localized file.
    * @param baseName base name of file, as dot-separated path (e.g. <code>some.dir.File</code>)
    * @param ext      extension of file
    * @param locale   locale of file
    * @param loader  class loader to use
    * @return URL of matching localized file
    * @throws MissingResourceException if not found
    */
    public static synchronized URL getLocalizedFile(String baseName, String ext,
            Locale locale, ClassLoader loader) throws MissingResourceException {

        URL lookup = null;
        Enumeration en = getLocaleEnum(locale);
        String cachePrefix = "["+Integer.toString(loader.hashCode())+"]"; // NOI18N
        Vector cacheCandidates = new Vector();
        ExtensionRecognizer recognizer = new ExtensionRecognizer(ext);

        while (en.hasMoreElements()) {
            String searchName = baseName + en.nextElement().toString();
            String cacheName = cachePrefix + searchName.replace('.', '/') + '.' + ext;
            lookup = (URL) cacheListFiles.get(cacheName);
            if (lookup != null)
                break;
            cacheCandidates.addElement(cacheName);
            if (recognizer.recognize(loader, searchName)) {
                lookup = recognizer.recognURL;
                break;
            }
        }
        if (lookup == null) {
            throw new MissingResourceException(MessageFormat.format (NbBundle.getBundle(NbBundle.class).getString("MSG_FMT_CantFindResourceFor"), new Object [] {baseName}), baseName,"");
        }
        else {
            Enumeration enAdd = cacheCandidates.elements();
            while (enAdd.hasMoreElements()) {
                cacheListFiles.put(enAdd.nextElement(), lookup);
            }
            return lookup;
        }
    }

    /** Find a localized value for a given key and locale.
    * Scans through a map to find
    * the most localized match possible. For example:
    * <p><code><PRE>
    *   findLocalizedValue (hashTable, "keyName", new Locale ("cs_CZ"))
    * </PRE></code>
    * <p>This would return the first non-<code>null</code> value obtained from the following tests:
    * <UL>
    * <LI> <CODE>hashTable.get ("keyName_cs_CZ")</CODE>
    * <LI> <CODE>hashTable.get ("keyName_cs")</CODE>
    * <LI> <CODE>hashTable.get ("keyName")</CODE>
    * </UL>
    *
    * @param table mapping from localized strings to objects
    * @param key the key to look for
    * @param locale the locale to use
    * @return the localized object or <code>null</code> if no key matches
    */
    public static Object getLocalizedValue (Map table, String key, Locale locale) {
        if (table instanceof Attributes) {
            throw new IllegalArgumentException ("Please do not use a java.util.jar.Attributes for NbBundle.getLocalizedValue " + // NOI18N
                                                "without using the special form that works properly with Attributes.Name's as keys."); // NOI18N
        }
        Enumeration en = getLocaleEnum (locale);
        while (en.hasMoreElements ()) {
            Object v = table.get (key + (String)en.nextElement ());
            if (v != null) {
                // ok
                return v;
            }
        }
        return null;
    }

    /** Find a localized value for a given key in the default system locale.
    *
    * @param table mapping from localized strings to objects
    * @param key the key to look for
    * @return the localized object or <code>null</code> if no key matches
    * @see #getLocalizedValue(Map,String,Locale)
    */
    public static Object getLocalizedValue (Map table, String key) {
        return getLocalizedValue (table, key, Locale.getDefault ());
    }

    /** Find a localized value in a JAR manifest.
    * @param attr the manifest attributes
    * @param key the key to look for (case-insensitive)
    * @param locale the locale to use
    * @return the value if found, else <code>null</code>
    */
    public static String getLocalizedValue (Attributes attr, Attributes.Name key, Locale locale) {
        return (String)getLocalizedValue (attr2Map (attr), key.toString ().toLowerCase (), locale);
    }

    /** Find a localized value in a JAR manifest in the default system locale.
    * @param attr the manifest attributes
    * @param key the key to look for (case-insensitive)
    * @return the value if found, else <code>null</code>
    */
    public static String getLocalizedValue (Attributes attr, Attributes.Name key) {
        return (String)getLocalizedValue (attr2Map (attr), key.toString ().toLowerCase ());
    }

    /** Necessary because Attributes implements Map; however this is dangerous!
    * The keys are Attributes.Name's, not Strings.
    * Also manifest lookups should not be case-sensitive.
    * (Though the locale suffix still will be!)
    */
    private static Map attr2Map (final Attributes attr) {
        class AttributesMap extends HashMap {
            public AttributesMap () {
                super (7);
            }

            public Object get (Object obj) {
                Attributes.Name an = new Attributes.Name ((String)obj);
                return attr.getValue (an);
            }
        }

        return new AttributesMap ();

        /* JST: Does the previous code do the same? I hope so...
        Map result = new HashMap ();
        Iterator it = attr.entrySet ().iterator ();
        while (it.hasNext ()) {
          Map.Entry entry = (Map.Entry) it.next ();
          String newKey = ((Attributes.Name) entry.getKey ()).toString ().toLowerCase ();
          String value = (String) entry.getValue ();
          result.put (newKey, value);
    }
        return result;
        */
    }

    /**
    * Get a resource bundle with the default class loader and locale.
    * @param baseName bundle basename
    * @return the resource bundle
    * @exception MissingResourceException if the bundle does not exist
    */
    public static final ResourceBundle getBundle(String baseName) throws MissingResourceException {
        return getBundle(baseName, Locale.getDefault(), getLoader());
    }

    /** Get a resource bundle in the same package as the provided class,
    * with the default locale and class loader.
    * This is the usual style of invocation.
    *
    * @param clazz the class to take the package name from
    * @return the resource bundle
    * @exception MissingResourceException if the bundle does not exist
    */
    public static ResourceBundle getBundle (Class clazz) {
        String name = findName (clazz);
        return getBundle(name, Locale.getDefault(), getLoader());
    }

    /** Finds package name for given class */
    private static String findName (Class clazz) {
        String pref = clazz.getName ();
        int last = pref.lastIndexOf ('.');
        if (last >= 0) {
            pref = pref.substring (0, last + 1);
            return pref + "Bundle"; // NOI18N
        } else {
            // base package, search for bundle
            return "Bundle"; // NOI18N
        }
    }

    /**
    * Get a resource bundle with the default class loader.
    * @param baseName bundle basename
    * @param locale the locale to use
    * @return the resource bundle
    * @exception MissingResourceException if the bundle does not exist
    */
    public static final ResourceBundle getBundle(String baseName, Locale locale)
    throws MissingResourceException {
        return getBundle(baseName, locale, getLoader());
    }

    /** Get a resource bundle the hard way.
    * @param baseName bundle basename
    * @param locale the locale to use
    * @param loader the class loader to use
    * @return the resource bundle
    * @exception MissingResourceException if the bundle does not exist
    */
    public static final ResourceBundle getBundle(String baseName, Locale locale,
            ClassLoader loader) throws MissingResourceException {

        Enumeration en = getLocaleEnum(locale);
        RefBundle ret = findBundle(baseName, loader, en);
        if (ret == null)
            throw new MissingResourceException(MessageFormat.format (NbBundle.getBundle(NbBundle.class).getString("MSG_FMT_CantFindResourceFor"), new Object [] {baseName}), baseName,"");
        else
            return ret;
    }

    /** Finds Resource bundle for the baseName in the ClassLoader loader
    * using enumeration of all possible localized sufixes ("_cs_CZ", "_cs", "",..)
    * It returns RefBundle which is subclass of ResourceBundle class and
    * chains them using 'parent' property (i.e. "_cs_CZ" -> "_cs" -> "")
    */
    private static RefBundle findBundle(String baseName, ClassLoader loader, Enumeration en) {
        String cachePrefix = "["+Integer.toString(loader.hashCode())+"]"; // NOI18N
        Vector cacheCandidates = new Vector();
        ExtensionRecognizer extRecognizer = new ExtensionRecognizer("properties"); // NOI18N
        ClassRecognizer clRecognizer = new ClassRecognizer();

        RefBundle lookup = null;

        while (en.hasMoreElements()) {
            String searchName = baseName + en.nextElement().toString();
            String cacheName = cachePrefix + searchName.replace('.', '/');
            lookup = (RefBundle) cacheList.get(cacheName);
            if (lookup != null)
                break;
            cacheCandidates.addElement(cacheName);
            if (clRecognizer.recognize(loader, searchName)) {
                lookup = new RefBundle((ResourceBundle) clRecognizer.recognObject);
                cacheList.put(cacheName, lookup);

                RefBundle par = findBundle(baseName, loader, en);
                lookup.setParent(par);
                break;
            }
            if (extRecognizer.recognize(loader, searchName)) {
                lookup = new RefBundle(createResourceBundleFromURL(extRecognizer.recognURL));
                cacheList.put(cacheName, lookup);

                RefBundle par = findBundle(baseName, loader, en);
                lookup.setParent(par);
                break;
            }
        }
        if (lookup != null) {
            Enumeration enAdd = cacheCandidates.elements();
            while (enAdd.hasMoreElements()) {
                cacheList.put((String)enAdd.nextElement(), lookup);
            }
            cacheCandidates.removeAllElements();
        }
        return lookup;
    }

    /** Creates input reader with present encoding, or default one,
    * if the previous operation fails.
    * 
    * @param is input stream
    * @return input reader
    */
    private static Reader createLoacalizedReader (InputStream is) {
        try {
            return new InputStreamReader (is, RESOURCE_ENCODING);
        } catch (java.io.UnsupportedEncodingException ex) {
            return new InputStreamReader (is);
        }
    }

    private static int counter = 0;
    /** Creates resource bundle using URL */
    private static ResourceBundle createResourceBundleFromURL(URL url) {
        ResourceBundle ret = null;

        try {
            InputStream inp = url.openStream();
            counter++;
            if (DEBUG) System.err.println("NbBundle counter: <" + counter + "> = " + url); // NOI18N
            ret = new ReadBundle (
                      new BufferedReader (createLoacalizedReader (inp)),
                      counter
                  );
            inp.close ();
        } catch (IOException e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) e.printStackTrace();
        }
        return ret;
    }

    /** @return default class loader which is used, when we don't have
    * any other class loader. (in function getBundle(String), getLocalizedFile(String),
    * and so on...
    */
    private static ClassLoader getLoader() {
        ClassLoader ret = null;
        if (loaderFinder != null)
            ret = loaderFinder.find();

        if (ret == null)
            ret = NbBundle.class.getClassLoader();

        return (ret == null) ? defaultLoader : ret;
    }

    /** @return all possible localizing sufixes in Enumeration.
    */
    private static Enumeration getLocaleEnum(Locale loc) {
        return new LocaleIterator(loc);
    }

    /**
    * The SystemClassLoader loads system classes (those in your classpath).
    * This is an attempt to unify the handling of system classes and ClassLoader
    * classes.
    */
    private static class SystemClassLoader extends java.lang.ClassLoader {

        protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            return findSystemClass(name);
        }

        public InputStream getResourceAsStream(String name) {
            return ClassLoader.getSystemResourceAsStream(name);
        }

        public java.net.URL getResource(String name) {
            return ClassLoader.getSystemResource(name);
        }
    }

    /** This class (enumeration) gives all localized sufixes using nextElement
    * method. It goes through given Locale and continues through Locale.getDefault()
    * Example 1:
    *   Locale.getDefault().toString() -> "_us"
    *   you call new LocaleIterator(new Locale("cs", "CZ"));
    *  ==> You will gets: "_cs_CZ", "_cs", "", "_us"
    *
    * Example 2:
    *   Locale.getDefault().toString() -> "_cs_CZ"
    *   you call new LocaleIterator(new Locale("cs", "CZ"));
    *  ==> You will gets: "_cs_CZ", "_cs", ""
    */
    private static class LocaleIterator extends Object implements Enumeration {
        /** this flag means, if default locale is in progress */
        private boolean defaultInProgress = false;

        /** this flag means, if empty sufix was exported yet */
        private boolean empty = false;

        /** curren locale.*/
        private Locale locale;

        /** current sufix which will be returned in next calling nextElement */
        private String current;

        /** Creates new LocaleIterator for given locale.
        * @param locale given Locale
        */
        public LocaleIterator(Locale locale) {
            this.locale = locale;
            if (locale.equals(Locale.getDefault())) {
                defaultInProgress = true;
            }
            current = '_' + locale.toString();
        }

        /** @return next sufix.
        * @exception NoSuchElementException if there is no more locale sufix.
        */
        public Object nextElement() throws NoSuchElementException {
            if (current == null)
                throw new NoSuchElementException();

            String ret = current;
            int lastUnderbar = current.lastIndexOf('_');
            if (lastUnderbar == 0) {
                if (empty)
                    current = null;
                else {
                    current = ""; // NOI18N
                    empty = true;
                }
            }
            else {
                if (lastUnderbar == -1) {
                    if (defaultInProgress)
                        current = null;
                    else {
                        locale = Locale.getDefault();
                        current = '_' + locale.toString();
                        defaultInProgress = true;
                    }
                }
                else {
                    current = current.substring(0, lastUnderbar);
                }
            }
            return ret;
        }

        /** Tests if there is any sufix.*/
        public boolean hasMoreElements() {
            return (current != null);
        }
    }

    /** Encapsulation of ResourceBundle. java.util.ResourceBundle has not protected
    * variable parent and protected setter setParent, and so we have to rewrite
    * this method and make it public.
    */
    private static class RefBundle extends ResourceBundle {

        /** Encapsulated ResourceBundle.*/
        private ResourceBundle ref;

        /** Creates new RefBundle for the ResourceBundle. */
        public RefBundle(ResourceBundle ref) {
            this.ref = ref;
        }

        /** Get an object from a ResourceBundle.
        * @param key see class description.
        */
        protected Object handleGetObject(String key) throws MissingResourceException {
            Object obj;
            try {
                return ref.getObject(key);
            }
            catch (MissingResourceException e) {
                if (parent != null) {
                    return parent.getObject(key);
                }
                else
                    throw new MissingResourceException(NbBundle.getBundle(NbBundle.class).getString("MSG_CantFindResource"), ref.getClass().getName(), key);
            }
        }

        /**
        * Return an enumeration of the keys.
        */
        public Enumeration getKeys() {
            return ref.getKeys();
        }

        /** Sets the parent of this bundle */
        public void setParent(ResourceBundle parent) {
            this.parent = parent;
        }

        /** Gets the parent of this bundle. Can returns null if this bundle has
        * no parent.
        */
        public ResourceBundle getParent() {
            return parent;
        }
    }

    /** ResourceBundle that reads its content from the Reader.
    */
    private static class ReadBundle extends ResourceBundle {
        /** the properties */
        private Properties prop = new Properties ();
        private int counter;

        /** Reads content of the bundle from reader r.
        * @param r the reader
        * @param counter the bundle counter for debugging
        * @exception IOExcepiton if an error occures during reading
        */
        public ReadBundle (Reader r, int counter) throws IOException {
            this.counter = counter;

            BufferedReader in = DEBUG ? new LineNumberReader (r) : new BufferedReader (r);

            int ch = in.read();
            for (;;) {
                switch (ch) {
                case -1: return;
                case '#':
                case '!':
                    do {
                        ch = in.read();
                    } while ((ch >= 0) && (ch != '\n') && (ch != '\r'));
                    continue;
                case '\n':
                case '\r':
                case ' ':
                case '\t':
                    ch = in.read();
                    continue;
                }

                // Read the key
                int line = DEBUG ? ((LineNumberReader) in).getLineNumber () + 1 : 0;
                StringBuffer key = new StringBuffer();
                while (
                    (ch >= 0) && (ch != '=') && (ch != ':') &&
                    (ch != ' ') && (ch != '\t') && (ch != '\n') && (ch != '\r')
                ) {
                    char _ch = (char) ch;
                    key.append (_ch == '\\' ? (char) in.read () : _ch);
                    ch = in.read();
                }
                while ((ch == ' ') || (ch == '\t')) {
                    ch = in.read();
                }
                if ((ch == '=') || (ch == ':')) {
                    ch = in.read();
                }
                while ((ch == ' ') || (ch == '\t')) {
                    ch = in.read();
                }

                // Read the value
                StringBuffer val = new StringBuffer();
                while ((ch >= 0) && (ch != '\n') && (ch != '\r')) {
                    int next = 0;
                    if (ch == '\\') {
                        switch (ch = in.read()) {
                        case '\r':
                            if (
                                ((ch = in.read()) == '\n') ||
                                (ch == ' ') ||
                                (ch == '\t')
                            ) {
                                // fall thru to '\n' case
                            } else {
                                continue;
                            }
                        case '\n':
                            while (((ch = in.read()) == ' ') || (ch == '\t'));
                            continue;
                        case 't': ch = '\t'; next = in.read(); break;
                        case 'n': ch = '\n'; next = in.read(); break;
                        case 'r': ch = '\r'; next = in.read(); break;
                        case 'u': {
                                while ((ch = in.read()) == 'u');
                                int d = 0;
loop:
                                for (int i = 0 ; i < 4 ; i++) {
                                    next = in.read();
                                    switch (ch) {
                    case '0': case '1': case '2': case '3': case '4':
                    case '5': case '6': case '7': case '8': case '9':
                                        d = (d << 4) + ch - '0';
                                        break;
                case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                                        d = (d << 4) + 10 + ch - 'a';
                                        break;
                case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
                                        d = (d << 4) + 10 + ch - 'A';
                                        break;
                                    default:
                                        break loop;
                                    }
                                    ch = next;
                                }
                                ch = d;
                                break;
                            }
                        default: next = in.read(); break;
                        }
                    } else {
                        next = in.read();
                    }
                    val.append((char)ch);
                    ch = next;
                }

                // [PENDING] remove ICON_ test after search module fixed
                if (DEBUG && key.toString ().indexOf ("ICON_") == -1) { // NOI18N
                    val.append (" <"); // NOI18N
                    val.append (Integer.toString (counter));
                    val.append (':');
                    val.append (Integer.toString (line));
                    val.append ('>');
                }

                prop.put(key.toString(), val.toString());
            }
        }

        /** Gets the right object for specified key.
        * @param key the key
        * @return object or null for the key
        */
        public Object handleGetObject (String key) {
            return prop.getProperty (key);
        }

        /** Enumeration of keys.
        * @return enumeration of Strings
        */
        public Enumeration getKeys () {
            Enumeration result;
            if (parent != null) {
                Hashtable temp = new Hashtable();
                for (
                    Enumeration parentKeys = parent.getKeys() ;
                    parentKeys.hasMoreElements() ;
                    /* nothing */
                ) {
                    temp.put(parentKeys.nextElement(), this);
                }
                for (
                    Enumeration thisKeys = prop.keys();
                    thisKeys.hasMoreElements() ;
                    /* nothing */
                ) {
                    temp.put(thisKeys.nextElement(), this);
                }
                result = temp.keys();
            } else {
                result = prop.keys();
            }
            return result;
        }
    }
}


/*
* Log
*  17   Gandalf   1.16        1/16/00  Jesse Glick     May now have escaped 
*       chars in bundle key (acc. to standard Properties syntax).
*  16   Gandalf   1.15        1/14/00  Jesse Glick     IDE versioning fix.
*  15   Gandalf   1.14        1/12/00  Pavel Buzek     I18N
*  14   Gandalf   1.13        1/12/00  Jesse Glick     Better bundle debugger.
*  13   Gandalf   1.12        1/12/00  Jesse Glick     -Dorg.openide.util.NbBundle.DEBUG=true
*        to get bundle debugging.
*  12   Gandalf   1.11        12/9/99  Jaroslav Tulach Hopefully faster impl of 
*       getLocalizedValue (Attribute.Name)
*  11   Gandalf   1.10        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  10   Gandalf   1.9         7/25/99  Ian Formanek    Exceptions printed to 
*       console only on "netbeans.debug.exceptions" flag
*  9    Gandalf   1.8         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  8    Gandalf   1.7         5/15/99  Jesse Glick     [JavaDoc], and 
*       idiotproofed getLocalizedValue (Map, ...).
*  7    Gandalf   1.6         5/14/99  Jaroslav Tulach Bugfixes.
*  6    Gandalf   1.5         5/7/99   Jesse Glick     Module localization.
*  5    Gandalf   1.4         4/27/99  Jesse Glick     [JavaDoc] and 
*       generalizing Hashtable -> Map.
*  4    Gandalf   1.3         3/26/99  Ian Formanek    Removed obsoleted method 
*       getBundle (Object)
*  3    Gandalf   1.2         3/25/99  Ales Novak      
*  2    Gandalf   1.1         1/5/99   Ian Formanek    Property update.
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
* Beta Change History:
*  0    Tuborg    0.30        --/--/98 Jaroslav Tulach Reader for PropertyResourceBundle
*  0    Tuborg    0.31        --/--/98 Petr Hamernik   Bug fix
*  0    Tuborg    0.32        --/--/98 Petr Hamernik   -3 [Petr] class loader finder
*/
