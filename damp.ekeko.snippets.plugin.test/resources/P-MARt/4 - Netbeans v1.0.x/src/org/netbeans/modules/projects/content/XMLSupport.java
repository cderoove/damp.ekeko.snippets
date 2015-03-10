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

package org.netbeans.modules.projects.content;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;

import org.openide.loaders.XMLDataObject;
import org.openide.util.SharedClassObject;
import org.openide.util.Utilities;
import org.openide.util.io.*;

import org.openidex.projects.*;

import org.xml.sax.*;

/**
 *
 * @author  mryzl
 * @version 
 */
public class XMLSupport extends Object {

    public static final String EMPTY_STRING = "";  // NOI18N

    public static final String FMT_XML_HEADER = "<?xml version=\"1.0\"?>"; // NOI18N
    public static final String FMT_OBJECTS_START = "<{0} version=\"1.0\">"; // NOI18N
    public static final String FMT_OBJECTS_END = "</{0}>"; // NOI18N
    public static final String ELEMENT_CONTROL_PANEL = "control-panel"; // NOI18N
    public static final String ELEMENT_MODULES= "modules"; // NOI18N
    public static final String ELEMENT_LOADERS = "loaders"; // NOI18N

    /** Creates new XMLSupport */
    public XMLSupport() {
    }

    public static String encodeToProperXML(String text) {
        if (text.indexOf ('&') != -1) text = Utilities.replaceString (text, "&", "&amp;"); // must be the first to prevent changes in the &XX; codes // NOI18N

        if (text.indexOf ('<') != -1) text = Utilities.replaceString (text, "<", "&lt;"); // NOI18N
        if (text.indexOf ('>') != -1) text = Utilities.replaceString (text, ">", "&gt;"); // NOI18N
        if (text.indexOf ('\'') != -1) text = Utilities.replaceString (text, "\'", "&apos;"); // NOI18N
        if (text.indexOf ('\"') != -1) text = Utilities.replaceString (text, "\"", "&quot;"); // NOI18N
        if (text.indexOf ('\n') != -1) text = Utilities.replaceString (text, "\n", "&#xa;"); // NOI18N
        if (text.indexOf ('\t') != -1) text = Utilities.replaceString (text, "\t", "&#x9;"); // NOI18N
        return text;
    }

    /** Decodes a value of from the specified String containing textual representation of serialized stream.
    * @return decoded object
    * @exception IOException thrown if an error occures during deserializing the object
    */
    public static Object decodeValue (String value) throws IOException {
        if ((value == null) || (value.length () == 0)) return null;

        char[] bisChars = value.toCharArray ();
        byte[] bytes = new byte[bisChars.length];
        String singleNum = ""; // NOI18N
        int count = 0;
        for (int i = 0; i < bisChars.length; i++) {
            if (',' == bisChars[i]) {
                try {
                    bytes[count++] = Byte.parseByte (singleNum);
                } catch (NumberFormatException e) {
                    if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
                    throw new IOException ();
                }
                singleNum = EMPTY_STRING;
            } else {
                singleNum += bisChars[i];
            }
        }
        // add the last byte
        bytes[count++] = Byte.parseByte (singleNum);
        ByteArrayInputStream bis = new ByteArrayInputStream (bytes, 0, count);
        try {
            ObjectInputStream ois = new NbObjectInputStream (bis);
            Object ret = ois.readObject ();
            return ret;
        } catch (IOException ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
            throw ex;
        } catch (Exception ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
            throw new FoldingIOException(ex);
        }
    }

    /** Encodes specified value to a String containing textual representation of serialized stream.
     * @return String containing textual representation of the serialized object
     */
    public static String encodeValue (Object value) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream ();
        try {
            ObjectOutputStream oos = new NbObjectOutputStream (bos);
            oos.writeObject (value);
            oos.close ();
        } catch (Exception ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
            throw new FoldingIOException(ex); // problem during serialization
        }
        byte[] bosBytes = bos.toByteArray ();
        StringBuffer sb = new StringBuffer (bosBytes.length);
        for (int i = 0; i < bosBytes.length; i++) {
            if (i != bosBytes.length - 1) {
                sb.append (bosBytes[i]+","); // NOI18N
            } else {
                sb.append ("" + bosBytes[i]); // NOI18N
            }
        }
        return sb.toString ();
    }

    /** Encode strings to one. Comma will be used as a delimiter.
    * @param list of strings
    */
    public static String encodeStrings(List list) {
        Iterator it = list.iterator();
        StringBuffer sb = new StringBuffer();

        boolean comma = false;
        for(int i = 0; it.hasNext();) {
            if (comma) {
                sb.append(',');
            } else {
                comma = true;
            }
            sb.append(encodeToProperXML((String)it.next()));
        }
        return sb.toString();
    }

    /** Decode strings from one string with comma as a delimiter.
    * @return list of strings
    */
    public static List decodeStrings(String value) {
        List list = new LinkedList();
        StringTokenizer st = new StringTokenizer(value, ","); // NOI18N
        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        return list;
    }


    /** Save given array of objects to the XML file.
    * @param writer 
    * @param map with classnames and values
    * @param mainElement main element of the XML
    */
    public static void saveObjects(Writer writer, Map map, String mainElement) {
        Object[] formatObjs = new Object[] { mainElement };
        PrintWriter pw = new PrintWriter(writer);
        pw.println(FMT_XML_HEADER);
        pw.println(MessageFormat.format(FMT_OBJECTS_START, formatObjs));
        Iterator it = map.keySet().iterator();
        while (it.hasNext()) {
            SharedClassObject sco = (SharedClassObject) it.next();
            pw.print("  <object name=\""); // NOI18N
            pw.print(encodeToProperXML(sco.getClass().getName()));
            pw.print("\" value=\""); // NOI18N
            pw.print(map.get(sco));
            pw.println("\"/>"); // NOI18N
        }
        pw.println(MessageFormat.format(FMT_OBJECTS_END, formatObjs));
        pw.flush();
    }

    /** Load objects from XML file.
    * @param reader
    * @param map an empty map. It will be filled by processed values and returned back.
    * @param mainElement main element of the XML
    * @return a map, where keys are SharedClassObjects and values are values of SCO encoded to String.
    */
    public static Map loadObjects(Reader reader, Map map, String mainElement) throws IOException {
        try {
            Parser parser = XMLDataObject.createParser();
            HandlerObjects handler = new HandlerObjects(map, mainElement);
            parser.setDocumentHandler(handler);
            parser.parse(new InputSource(reader));
        } catch (SAXException ex) {
            throw new org.openide.util.io.FoldingIOException(ex);
        }
        return map;
    }

    /**
    */
    public static void loadDiffSet(Reader reader, DiffSet diffset) throws IOException {
        try {
            Parser parser = XMLDataObject.createParser();
            HandlerDiffSet handler = new HandlerDiffSet(diffset);
            parser.setDocumentHandler(handler);
            parser.parse(new InputSource(reader));
        } catch (SAXException ex) {
            throw new org.openide.util.io.FoldingIOException(ex);
        }
    }

    /**
    */
    public static void saveDiffSet(Writer writer, DiffSet diffset) throws IOException {
        // write header
        PrintWriter pw = new PrintWriter(writer);
        pw.println(FMT_XML_HEADER);
        // write diff set start element
        pw.println("<" + HandlerDiffSet.ELEMENT_DIFF_SET + " clear=\"" + diffset.isClear() + "\" version=\"1.0\">"); // NOI18N
        // write add
        Iterator it = diffset.addedItems().iterator();
        while (it.hasNext()) {
            String item = (String) it.next();
            pw.print("  <add name=\""); // NOI18N
            pw.print(encodeToProperXML(item));
            pw.print("\" value=\""); // NOI18N
            pw.print(encodeValue(diffset.addedItem(item)));
            pw.println("\"/>"); // NOI18N
        }
        // write remove
        it = diffset.removedItems().iterator();
        while (it.hasNext()) {
            String item = (String) it.next();
            pw.print("  <remove name=\""); // NOI18N
            pw.print(encodeToProperXML(item));
            pw.println("\"/>"); // NOI18N
        }
        // write order
        pw.print("  <order value=\""); // NOI18N
        pw.print(encodeStrings(diffset.getOrder()));
        pw.println("\"/>"); // NOI18N

        // write diff set end element
        pw.println("</" + HandlerDiffSet.ELEMENT_DIFF_SET +">"); // NOI18N

    }

}