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

package org.netbeans.modules.form.compat2.border;

import java.awt.*;
import java.beans.*;

import javax.swing.border.*;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.openide.explorer.propertysheet.editors.ChoicePropertyEditor;
import org.openide.nodes.*;

import org.netbeans.modules.form.FormUtils;

/**
*
* @author   Petr Hamernik
* @version  1.02, Aug 07, 1998
*/
public abstract class BevelAbstractBorderInfo extends BorderInfoSupport {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 720699432057720228L;

    private static final String PROP_BEVEL_TYPE = "bevelType"; // NOI18N
    private static final String PROP_HIGHLIGHT_INNER = "highlightInner"; // NOI18N
    private static final String PROP_HIGHLIGHT_OUTER = "highlightOuter"; // NOI18N
    private static final String PROP_SHADOW_INNER = "shadowInner"; // NOI18N
    private static final String PROP_SHADOW_OUTER = "shadowOuter"; // NOI18N

    private final static BevelBorder DEFAULT_BORDER = new BevelBorder(BevelBorder.RAISED);
    private static int DEFAULT_BEVEL_TYPE = BevelBorder.RAISED;
    private static Color DEFAULT_HIGHLIGHT_INNER;
    private static Color DEFAULT_HIGHLIGHT_OUTER;
    private static Color DEFAULT_SHADOW_INNER;
    private static Color DEFAULT_SHADOW_OUTER;

    static {
        SwingUtilities.invokeLater(new Runnable() {
                                       public void run() {
                                           initDefaults();
                                       }
                                   });
    }

    private static final int[][] constructors = new int[][] { {0}, {0,1,2}, {0,1,2,3,4} };

    protected int bevelType;
    protected Color highlightInner;
    protected Color highlightOuter;
    protected Color shadowInner;
    protected Color shadowOuter;

    public BevelAbstractBorderInfo() {
        initDefaults();
        bevelType = DEFAULT_BEVEL_TYPE;
        highlightInner = DEFAULT_HIGHLIGHT_INNER;
        highlightOuter = DEFAULT_HIGHLIGHT_OUTER;
        shadowInner = DEFAULT_SHADOW_INNER;
        shadowOuter = DEFAULT_SHADOW_OUTER;
    }

    static void initDefaults() {
        synchronized (DEFAULT_BORDER) {
            if (DEFAULT_HIGHLIGHT_INNER == null) {
                JButton b = new JButton();
                DEFAULT_HIGHLIGHT_INNER = DEFAULT_BORDER.getHighlightInnerColor(b);
                DEFAULT_HIGHLIGHT_OUTER = DEFAULT_BORDER.getHighlightOuterColor(b);
                DEFAULT_SHADOW_INNER = DEFAULT_BORDER.getShadowInnerColor(b);
                DEFAULT_SHADOW_OUTER = DEFAULT_BORDER.getShadowOuterColor(b);
            }
        }
    }

    protected BevelBorder getBevelBorder() {
        return (BevelBorder) getBorder();
    }

    protected abstract void updateBorder();

    /** Returns the border's properties for specified component.
    * The default implementation returns empty array.
    * @param node the RADVisualNode of the component which properties we request
    * @return the layout-specific properties for specified component
    */
    public Node.Property[] getProperties() {
        return new Node.Property[] {
                   new BorderProp(PROP_BEVEL_TYPE, Integer.TYPE,
                                  BorderInfo.bundle.getString("PROP_BevelType"),
                                  BorderInfo.bundle.getString("HINT_BevelType")) {
                       public Object getValue () {
                           return new Integer(bevelType);
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           if (val instanceof Integer) {
                               bevelType = ((Integer)val).intValue();
                               updateBorder();
                               firePropChange();
                           }
                           else
                               throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return DEFAULT_BORDER.getBevelType() == bevelType;
                       }
                       public PropertyEditor getPropertyEditor() {
                           return new ChoicePropertyEditor(
                                      new int[] { BevelBorder.RAISED, BevelBorder.LOWERED },
                                      new String[] {
                                          BorderInfo.bundle.getString("VALUE_BevelRaised"),
                                          BorderInfo.bundle.getString("VALUE_BevelLowered")
                                      }
                                  );
                       }
                   },

                   new BorderProp(PROP_HIGHLIGHT_INNER, Color.class,
                                  BorderInfo.bundle.getString("PROP_HighlightInner"),
                                  BorderInfo.bundle.getString("HINT_HighlightInner")) {
                       public Object getValue () {
                           return highlightInner;
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           if (val instanceof Color) {
                               highlightInner = (Color)val;
                               updateBorder();
                               firePropChange();
                           }
                           else
                               throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return DEFAULT_HIGHLIGHT_INNER.equals(highlightInner);
                       }
                   },

                   new BorderProp(PROP_HIGHLIGHT_OUTER, Color.class,
                                  BorderInfo.bundle.getString("PROP_HighlightOuter"),
                                  BorderInfo.bundle.getString("HINT_HighlightOuter")) {
                       public Object getValue () {
                           return highlightOuter;
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           if (val instanceof Color) {
                               highlightOuter = (Color)val;
                               updateBorder();
                               firePropChange();
                           }
                           else
                               throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return DEFAULT_HIGHLIGHT_OUTER.equals(highlightOuter);
                       }
                   },

                   new BorderProp(PROP_SHADOW_INNER, Color.class,
                                  BorderInfo.bundle.getString("PROP_ShadowInner"),
                                  BorderInfo.bundle.getString("HINT_ShadowInner")) {
                       public Object getValue () {
                           return shadowInner;
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           if (val instanceof Color) {
                               shadowInner = (Color)val;
                               updateBorder();
                               firePropChange();
                           }
                           else
                               throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return DEFAULT_SHADOW_INNER.equals(shadowInner);
                       }
                   },

                   new BorderProp(PROP_SHADOW_OUTER, Color.class,
                                  BorderInfo.bundle.getString("PROP_ShadowOuter"),
                                  BorderInfo.bundle.getString("HINT_ShadowOuter")) {
                       public Object getValue () {
                           return shadowOuter;
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           if (val instanceof Color) {
                               shadowOuter = (Color)val;
                               updateBorder();
                               firePropChange();
                           }
                           else
                               throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return DEFAULT_SHADOW_OUTER.equals(shadowOuter);
                       }
                   }
               };
    }

    protected int[][] getConstructors() {
        return constructors;
    }

    //--------------------------------------------------------------------------
    // XMLPersistence implementation

    public static final String XML_BEVEL_BORDER = "BevelBorder"; // NOI18N

    public static final String ATTR_TYPE = "bevelType"; // NOI18N
    public static final String ATTR_HIGHLIGHT_INNER = "highlightInner"; // NOI18N
    public static final String ATTR_HIGHLIGHT_OUTER = "highlightOuter"; // NOI18N
    public static final String ATTR_SHADOW_INNER = "shadowInner"; // NOI18N
    public static final String ATTR_SHADOW_OUTER = "shadowOuter"; // NOI18N

    /** Called to load property value from specified XML subtree. If succesfully loaded,
    * the value should be available via the getValue method.
    * An IOException should be thrown when the value cannot be restored from the specified XML element
    * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
    * @exception IOException thrown when the value cannot be restored from the specified XML element
    */
    public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_BEVEL_BORDER.equals (element.getNodeName ())) {
            throw new java.io.IOException ();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
        try {
            org.w3c.dom.Node aNode = attributes.getNamedItem (ATTR_TYPE);
            if(aNode != null) {
                bevelType = Integer.parseInt (aNode.getNodeValue ());
            }
            Color read;
            read = (Color) FormUtils.readProperty (ATTR_HIGHLIGHT_INNER,  Color.class, element);
            if (read != null) highlightInner =  read;
            read = (Color) FormUtils.readProperty (ATTR_HIGHLIGHT_OUTER, Color.class, element);
            if (read != null) highlightOuter =  read;
            read = (Color) FormUtils.readProperty (ATTR_SHADOW_INNER, Color.class, element);
            if (read != null) shadowInner =  read;
            read = (Color) FormUtils.readProperty (ATTR_SHADOW_OUTER, Color.class, element);
            if (read != null) shadowOuter = read;

            updateBorder ();
        } catch (Exception e) {
            throw new java.io.IOException (e.toString());
        }
    }

    /** Called to store current property value into XML subtree. The property value should be set using the
    * setValue method prior to calling this method.
    * @param doc The XML document to store the XML in - should be used for creating nodes only
    * @return the XML DOM element representing a subtree of XML from which the value should be loaded or null if there is no state to save
    */
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        org.w3c.dom.Element el = doc.createElement (XML_BEVEL_BORDER);
        if (bevelType != DEFAULT_BEVEL_TYPE) el.setAttribute (ATTR_TYPE, Integer.toString (bevelType));
        if(!highlightInner.equals (DEFAULT_HIGHLIGHT_INNER)) {
            FormUtils.writeProperty (ATTR_HIGHLIGHT_INNER, highlightInner, Color.class, el, doc);
        }
        if(!highlightOuter.equals (DEFAULT_HIGHLIGHT_OUTER)) {
            FormUtils.writeProperty (ATTR_HIGHLIGHT_OUTER, highlightOuter, Color.class, el, doc);
        }
        if(!shadowInner.equals (DEFAULT_SHADOW_INNER)) {
            FormUtils.writeProperty (ATTR_SHADOW_INNER, shadowInner, Color.class, el, doc);
        }
        if(!shadowOuter.equals (DEFAULT_SHADOW_OUTER)) {
            FormUtils.writeProperty (ATTR_SHADOW_OUTER, shadowOuter, Color.class, el, doc);
        }
        return el;
    }

}

/*
 * Log
 *  8    Gandalf   1.7         1/12/00  Ian Formanek    NOI18N
 *  7    Gandalf   1.6         12/9/99  Pavel Buzek     
 *  6    Gandalf   1.5         11/24/99 Pavel Buzek     added support for saving
 *       in XML format
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/28/99  Petr Hamernik   #4052 fixed
 *  3    Gandalf   1.2         6/30/99  Ian Formanek    reflecting package 
 *       change of ChoicePropertyEditor
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         4/2/99   Ian Formanek    
 * $
 */
