/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.properties;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.Map;
import java.util.TreeMap;
import java.text.MessageFormat;
import java.io.IOException;

import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.util.MapFormat;
import org.openide.filesystems.FileObject;
import org.openide.TopManager;
import org.openide.loaders.DataObject;

/**
 *
 * @author  pjiricka
 * @version 
 */
public class ResourceBundleStringEditor extends PropertyEditorSupport
    implements EnhancedCustomPropertyEditor, XMLPropertyEditor {

    public static final String MAP_BUNDLE       = "BUNDLE";
    public static final String MAP_BUNDLE_DOTS  = "BUNDLE_DOTS";
    public static final String MAP_KEY          = "KEY";
    //public static String javaStringFormat = "java.util.ResourceBundle.getBundle(\"{" + MAP_BUNDLE + "}\").getString(\"{" + MAP_KEY + "}\")";
    public static String javaStringFormat = "java.util.ResourceBundle.getBundle(\"{0}\").getString(\"{2}\")";

    /** Creates new ResourceBundleStringEditor */
    public ResourceBundleStringEditor() {
    }

    protected ResourceBundleString currentValue;

    public Object getPropertyValue() throws IllegalStateException {
        ResourceBundleString bundleString = (ResourceBundleString)getValue();
        PropertiesModule.setLastBundleUsed(bundleString.getResourceBundle());
        return getValue();
    }

    /** @return null as we don't support this */
    public String[] getTags() {
        return null;
    }

    /** @return text for the current value */
    public String getAsText () {
        Object ret = ((ResourceBundleString)getValue()).getPropertyValue();
        if ((ret != null) && (ret instanceof String)) {
            ResourceBundleString rbs = (ResourceBundleString)getValue();
            return "[KEY:" + rbs.getKey() + ", RB:" + rbs.getResourceBundle().getPrimaryFile().getPackageName('/') + "]";
        }
        else
            return "[INVALID RESOURCE VALUE]";
    }

    public void setValue(Object value) {
        if (value == null) {
            currentValue = null;
            getValue(); // sets it
        }
        else
            if (value instanceof ResourceBundleString)
                currentValue = (ResourceBundleString)value;
            else
                currentValue = new ResourceBundleString();
    }

    public Object getValue() {
        if (currentValue == null)
            currentValue = new ResourceBundleString();
        if (currentValue.getResourceBundle() == null)
            currentValue.setResourceBundle(PropertiesModule.getLastBundleUsed());
        else
            if (PropertiesModule.getLastBundleUsed() == null)
                PropertiesModule.setLastBundleUsed(currentValue.getResourceBundle());
        return currentValue;
    }


    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public java.awt.Component getCustomEditor () {
        ResourceBundlePanel pan = new ResourceBundlePanel ();
        ResourceBundleString newValue = new ResourceBundleString((ResourceBundleString)getValue());
        pan.setValue(newValue);
        return pan;
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    protected String doFormatting() {
        return MessageFormat.format(javaStringFormat, new Object[] {
                                        currentValue.getResourceBundle().getPrimaryFile().getPackageName('/'),
                                        currentValue.getResourceBundle().getPrimaryFile().getPackageName('.'),
                                        currentValue.getKey(),
                                        className == null ? "" : className
                                    });
    }

    public String getJavaInitializationString () {
        if (currentValue != null) {
            if (currentValue.getResourceBundle() != null && currentValue.getKey() != null) {
                // 0 .. bundle with slashes
                // 1 .. bundle with dots
                // 2 .. key
                // 3 .. class name set by setClassName()
                String result = doFormatting();
                String[] args = currentValue.getArguments();
                if (args.length > 0) {
                    StringBuffer sb = new StringBuffer("java.text.MessageFormat.format(");
                    sb.append(result);
                    sb.append(", new Object[] {");
                    for (int i = 0; i < args.length; i++) {
                        sb.append(args[i]);
                        if (i < args.length - 1)
                            sb.append(", ");
                    }
                    sb.append("})");
                    result = sb.toString();
                }
                return result;

                /*Map map = new TreeMap();
                map.put(MAP_BUNDLE, currentValue.getResourceBundle().getPrimaryFile().getPackageName('/'));
                map.put(MAP_BUNDLE_DOTS, currentValue.getResourceBundle().getPrimaryFile().getPackageName('.'));
                map.put(MAP_KEY, currentValue.getKey());
                return MapFormat.format(javaStringFormat, map); */
            }
        }
        return null;
    }

    private String className;

    //--------------------------------------------------------------------------
    // XMLPropertyEditor implementation

    public static final String XML_RESOURCESTRING = "ResourceString";
    public static final String XML_ARGUMENT = "Argument";

    public static final String ATTR_BUNDLE   = "bundle";
    public static final String ATTR_KEY      = "key";
    public static final String ATTR_INDEX    = "index";
    public static final String ATTR_JAVACODE = "javacode";

    private static final int MAX_INDEX       = 1000;

    /** Called to load property value from specified XML subtree. If succesfully loaded,
    * the value should be available via the getValue method.
    * An IOException should be thrown when the value cannot be restored from the specified XML element
    * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
    * @exception IOException thrown when the value cannot be restored from the specified XML element
    */
    public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_RESOURCESTRING.equals (element.getNodeName ())) {
            throw new java.io.IOException ();
        }
        ResourceBundleString bun = null;

        org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
        try {
            org.w3c.dom.Node n;
            // bundle
            n = attributes.getNamedItem (ATTR_BUNDLE);
            String bundleName = (n == null) ? null : n.getNodeValue ();

            // key
            n = attributes.getNamedItem (ATTR_KEY);
            String key = (n == null) ? null : n.getNodeValue ();

            bun = (ResourceBundleString)getValue();
            // set the bundle
            if (bundleName != null) {
                FileObject fo = TopManager.getDefault().getRepository().findResource(bundleName);
                if (fo != null)
                    try {
                        DataObject dObj = TopManager.getDefault().getLoaderPool().findDataObject(fo);
                        if (dObj instanceof PropertiesDataObject)
                            bun.setResourceBundle((PropertiesDataObject)dObj);
                    }
                catch (IOException e) {}
            }

            // set the key
            if (key != null && key.length() > 0)
                bun.setKey(key);
        } catch (NullPointerException e) {
            throw new java.io.IOException ();
        }

        // read the arguments
        if (element instanceof org.w3c.dom.Element) {
            org.w3c.dom.Element elem = (org.w3c.dom.Element)element;
            org.w3c.dom.NodeList args = elem.getElementsByTagName(XML_ARGUMENT);

            // find out the highest index
            int highest = -1;
            for (int i = 0; i < args.getLength(); i++) {
                org.w3c.dom.Node arg = args.item(i);
                org.w3c.dom.NamedNodeMap attr = arg.getAttributes ();
                /*System.out.println("length " + attr.getLength());
                for (int j=0; j<attr.getLength(); j++)
                System.out.println("name " + attr.item(j).getNodeName());*/
                org.w3c.dom.Node n = attr.getNamedItem (ATTR_INDEX);
                String indexStr = (n == null) ? null : n.getNodeValue ();
                //System.out.println("indexStr " +         indexStr);
                if (indexStr != null) {
                    try {
                        int index = Integer.parseInt(indexStr);
                        //System.out.println("index " +         index);
                        if (index > highest && index < MAX_INDEX)
                            highest = index;
                    }
                catch (Exception e) {}
                }
            }
            //System.out.println("highest " +         highest);

            // construct the array
            String[] params = new String[highest + 1];

            // fill the array
            for (int i = 0; i < args.getLength(); i++) {
                org.w3c.dom.Node arg = args.item(i);
                org.w3c.dom.NamedNodeMap attr = arg.getAttributes ();
                org.w3c.dom.Node n = attr.getNamedItem (ATTR_INDEX);
                String indexStr = (n == null) ? null : n.getNodeValue ();
                if (indexStr != null) {
                    try {
                        int index = Integer.parseInt(indexStr);
                        if (index < MAX_INDEX) {
                            org.w3c.dom.Node n2 = attr.getNamedItem (ATTR_JAVACODE);
                            String javaCode = (n2 == null) ? null : n2.getNodeValue ();
                            params[index] = javaCode;
                        }
                    }
                    catch (Exception e) {}
                }
            }

            // fill all the values in case some are missing - make it really foolproof
            for (int i = 0; i < params.length; i++)
                if (params[i] == null)
                    params[i] = "";

            // set the parameters
            bun.setArguments(params);
        }

        // set the bundle
        setValue (bun);
    }

    /** Called to store current property value into XML subtree. The property value should be set using the
    * setValue method prior to calling this method.
    * @param doc The XML document to store the XML in - should be used for creating nodes only
    * @return the XML DOM element representing a subtree of XML from which the value should be loaded
    */
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        //getValue();  initialize the value
        org.w3c.dom.Element el = doc.createElement (XML_RESOURCESTRING);
        String bundleName = (currentValue.getResourceBundle() == null) ? "" :
                            currentValue.getResourceBundle().getPrimaryFile().getPackageNameExt('/', '.');
        el.setAttribute (ATTR_BUNDLE, bundleName);
        el.setAttribute (ATTR_KEY,    (currentValue.getKey() == null) ? "" : currentValue.getKey());

        // append subelements corresponding to parameters
        String[] params = currentValue.getArguments();
        for (int i = 0; i < params.length; i++) {
            org.w3c.dom.Element param = doc.createElement (XML_ARGUMENT);
            param.setAttribute (ATTR_INDEX, "" + i);
            param.setAttribute (ATTR_JAVACODE, params[i]);
            try {
                el.appendChild(param);
            }
            catch (org.w3c.dom.DOMException e) {}
        }

        return el;
    }

}

/*
 * <<Log>>
 *  14   Gandalf   1.13        11/4/99  Petr Jiricka    Removed debug println-s
 *  13   Gandalf   1.12        10/25/99 Petr Jiricka    Fixes in a number of 
 *       areas - saving, UI, cookies, ...
 *  12   Gandalf   1.11        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   Gandalf   1.10        10/12/99 Petr Jiricka    
 *  10   Gandalf   1.9         9/2/99   Petr Jiricka    Modified setValue()
 *  9    Gandalf   1.8         8/31/99  Petr Jiricka    One more fix of 
 *       storeToXML
 *  8    Gandalf   1.7         8/31/99  Petr Jiricka    Hopefully fixed 
 *       NullPointerException in storeToXML
 *  7    Gandalf   1.6         8/18/99  Petr Jiricka    Changed java string 
 *       generation
 *  6    Gandalf   1.5         8/17/99  Petr Jiricka    implements XML things
 *  5    Gandalf   1.4         8/17/99  Petr Jiricka    Serialization
 *  4    Gandalf   1.3         8/3/99   Petr Jiricka    Returns a different 
 *       value from getAsText
 *  3    Gandalf   1.2         8/2/99   Petr Jiricka    
 *  2    Gandalf   1.1         8/1/99   Petr Jiricka    
 *  1    Gandalf   1.0         7/29/99  Petr Jiricka    
 * $
 */
