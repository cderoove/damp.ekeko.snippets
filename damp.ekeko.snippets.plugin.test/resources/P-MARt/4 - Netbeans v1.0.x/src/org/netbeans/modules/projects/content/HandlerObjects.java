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

import java.util.*;

import org.openide.util.SharedClassObject;
import org.openidex.projects.*;

/**
 * Receive notification of general document events.
 *
 * Example:
 * <pre>
 *     org.xml.sax.Parser parser = org.xml.sax.helpers.ParserFactory.makeParser();
 *     <font color="blue">parser.setDocumentHandler (new HandlerControlPanel());</font>
 * </pre>
 *
 * @see org.xml.sax.Parser#setDocumentHandler
 * @see org.xml.sax.Locator
 * @see org.xml.sax.HandlerBase
 */
public class HandlerObjects implements org.xml.sax.DocumentHandler {

    public static final String ELEMENT_CLASS = "object"; // NOI18N
    public static final String ATTRIBUTE_VERSION = "version"; // NOI18N
    public static final String ATTRIBUTE_NAME = "name"; // NOI18N
    public static final String ATTRIBUTE_VALUE = "value"; // NOI18N

    private String mainElement;

    /**
     * @associates String 
     */
    private Map map;

    /**
    */
    public HandlerObjects( Map map, String mainElement) {
        this.mainElement = mainElement;
        this.map = map;
    }

    /**
     * Receive an object for locating the origin of SAX document events.
     *
     * <p>SAX parsers are strongly encouraged (though not absolutely
     * required) to supply a locator: if it does so, it must supply
     * the locator to the application by invoking this method before
     * invoking any of the other methods in the DocumentHandler
     * interface.</p>
     *
     * <p>The locator allows the application to determine the end
     * position of any document-related event, even if the parser is
     * not reporting an error.  Typically, the application will
     * use this information for reporting its own errors (such as
     * character content that does not match an application's
     * business rules).  The information returned by the locator
     * is probably not sufficient for use with a search engine.</p>
     *
     * <p>Note that the locator will return correct information only
     * during the invocation of the events in this interface.  The
     * application should not attempt to use it at any other time.</p>
     *
     * @param locator An object that can return the location of
     *                any SAX document event.
     * @see org.xml.sax.Locator
     */
    public void setDocumentLocator(org.xml.sax.Locator locator) {
    }

    /**
     * Receive notification of the beginning of a document.
     *
     * <p>The SAX parser will invoke this method only once, before any
     * other methods in this interface or in DTDHandler (except for
     * setDocumentLocator).</p>
     *
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     */
    public void startDocument() throws org.xml.sax.SAXException {
    }

    /**
     * Receive notification of the end of a document.
     *
     * <p>The SAX parser will invoke this method only once, and it will
     * be the last method invoked during the parse.  The parser shall
     * not invoke this method until it has either abandoned parsing
     * (because of an unrecoverable error) or reached the end of
     * input.</p>
     *
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     */
    public void endDocument() throws org.xml.sax.SAXException {
    }

    /**
     * Receive notification of the beginning of an element.
     *
     * <p>The Parser will invoke this method at the beginning of every
     * element in the XML document; there will be a corresponding
     * endElement() event for every startElement() event (even when the
     * element is empty). All of the element's content will be
     * reported, in order, before the corresponding endElement()
     * event.</p>
     *
     * <p>If the element name has a namespace prefix, the prefix will
     * still be attached.  Note that the attribute list provided will
     * contain only attributes with explicit values (specified or
     * defaulted): #IMPLIED attributes will be omitted.</p>
     *
     * @param name The element type name.
     * @param atts The attributes attached to the element, if any.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #endElement
     * @see org.xml.sax.AttributeList
     */
    public void startElement(java.lang.String name,org.xml.sax.AttributeList atts) throws org.xml.sax.SAXException {

        if (name.equals (mainElement)) { // <control-panel>
            int len = atts.getLength();
            for (int i = 0; i < len; i++) {
                String attrName = atts.getName (i);
                String attrValue = atts.getValue (i);
                if (attrName.equals (ATTRIBUTE_VERSION)) { // <control-panel version="???"> // NOI18N
                    // attrValue;
                }
            }
        }

        if (name.equals (ELEMENT_CLASS)) { // <class>
            String className = null;
            String value = null;

            int len = atts.getLength();
            for (int i = 0; i < len; i++) {
                String attrName = atts.getName (i);
                String attrValue = atts.getValue (i);
                if (attrName.equals (ATTRIBUTE_VALUE)) { // <class value="???"> // NOI18N
                    value = attrValue;
                }
                if (attrName.equals (ATTRIBUTE_NAME)) { // <class name="???"> // NOI18N
                    className = attrValue;
                }
            }

            try {
                className = org.openide.util.Utilities.translate(className);
                Class clazz = org.openide.TopManager.getDefault().systemClassLoader().loadClass(className);
                SharedClassObject sco = SharedClassObject.findObject(clazz, true);
                map.put(sco, value);
            } catch (Exception ex) {
                // notified in decodeValue ....
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
            }
        }
    }

    /**
     * Receive notification of the end of an element.
     *
     * <p>The SAX parser will invoke this method at the end of every
     * element in the XML document; there will be a corresponding
     * startElement() event for every endElement() event (even when the
     * element is empty).</p>
     *
     * <p>If the element name has a namespace prefix, the prefix will
     * still be attached to the name.</p>
     *
     * @param name The element type name
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     */
    public void endElement(java.lang.String name) throws org.xml.sax.SAXException {
        if (name.equals (mainElement)) { // <control-panel>
        }
        if (name.equals (ELEMENT_CLASS)) { // <class>
        }
    }

    /**
     * Receive notification of character data.
     *
     * <p>The Parser will call this method to report each chunk of
     * character data.  SAX parsers may return all contiguous character
     * data in a single chunk, or they may split it into several
     * chunks; however, all of the characters in any single event
     * must come from the same external entity, so that the Locator
     * provides useful information.</p>
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * <p>Note that some parsers will report whitespace using the
     * ignorableWhitespace() method rather than this one (validating
     * parsers must do so).</p>
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param length The number of characters to read from the array.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #ignorableWhitespace 
     * @see org.xml.sax.Locator
     */
    public void characters(char[] ch,int start,int length) throws org.xml.sax.SAXException {
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     *
     * <p>Validating Parsers must use this method to report each chunk
     * of ignorable whitespace (see the W3C XML 1.0 recommendation,
     * section 2.10): non-validating parsers may also use this method
     * if they are capable of parsing and using content models.</p>
     *
     *
     * <p>SAX parsers may return all contiguous whitespace in a single
     * chunk, or they may split it into several chunks; however, all of
     * the characters in any single event must come from the same
     * external entity, so that the Locator provides useful
     * information.</p>
     *
     * <p>The application must not attempt to read from the array
     * outside of the specified range.</p>
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param length The number of characters to read from the array.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     * @see #characters
     */
    public void ignorableWhitespace(char[] ch,int start,int length) throws org.xml.sax.SAXException {
    }

    /**
     * Receive notification of a processing instruction.
     * 
     * <p>The Parser will invoke this method once for each processing
     * instruction found: note that processing instructions may occur
     * before or after the main document element.</p>
     *
     * <p>A SAX parser should never report an XML declaration (XML 1.0,
     * section 2.8) or a text declaration (XML 1.0, section 4.3.1)
     * using this method.</p>
     *
     * @param target The processing instruction target.
     * @param data The processing instruction data, or null if
     *        none was supplied.
     * @exception org.xml.sax.SAXException Any SAX exception, possibly
     *            wrapping another exception.
     */
    public void processingInstruction(java.lang.String target,java.lang.String data) throws org.xml.sax.SAXException {
    }

}
