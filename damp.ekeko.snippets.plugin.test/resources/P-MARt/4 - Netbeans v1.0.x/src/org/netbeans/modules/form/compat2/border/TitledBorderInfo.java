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

import org.openide.explorer.propertysheet.editors.ChoicePropertyEditor;
import org.openide.nodes.*;

import org.netbeans.modules.form.FormUtils;

/**
*
* @author   Petr Hamernik
*/
public class TitledBorderInfo extends BorderInfoSupport {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8482431946425011087L;

    /** Default icons for Border in design time. */
    static protected Image defaultIcon;
    static protected Image defaultIcon32;

    static {
        defaultIcon = Toolkit.getDefaultToolkit ().getImage (
                          TitledBorderInfo.class.getResource ("/org/netbeans/modules/form/resources/palette/titledBorder.gif")); // NOI18N
        defaultIcon32 = Toolkit.getDefaultToolkit ().getImage (
                            TitledBorderInfo.class.getResource ("/org/netbeans/modules/form/resources/palette/titledBorder32.gif")); // NOI18N
    }

    private static final String PROP_TITLE = "title"; // NOI18N
    private static final String PROP_BORDER = "border"; // NOI18N
    private static final String PROP_TITLE_JUSTIFICATION = "titleJustification"; // NOI18N
    private static final String PROP_TITLE_POSITION = "titlePosition"; // NOI18N
    private static final String PROP_TITLE_FONT = "titleFont"; // NOI18N
    private static final String PROP_TITLE_COLOR = "titleColor"; // NOI18N

    private final static Border DEFAULT_INNER_BORDER = new DesignBorder(new EtchedBorderInfo());
    private final static TitledBorder DEFAULT_BORDER = new TitledBorder(DEFAULT_INNER_BORDER, ""); // NOI18N

    private static final int[][] constructors = new int[][] { {0}, {1}, {1,0},
            {1,0,2,3}, {1,0,2,3,4}, {1,0,2,3,4,5}};

    private String title;
    private Border innerBorder;
    private int justification;
    private int position;
    private Font font;
    private Color color;

    public TitledBorderInfo() {
        super();

        TitledBorder b = getTitledBorder();

        title = b.getTitle();
        innerBorder = DEFAULT_INNER_BORDER;
        justification = b.getTitleJustification();
        position = b.getTitlePosition();
        font = b.getTitleFont();
        color = b.getTitleColor();
    }

    /** An icon of the border. This icon will be used on the ComponentPalette
    * for this layout's item.
    * @param  type the desired type of the icon (BeanInfo.ICON_???)
    * @return layout's icon.
    */
    public Image getIcon(int type) {
        if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16))
            return defaultIcon;
        else
            return defaultIcon32;
    }

    protected Border createDefaultBorder() {
        return DEFAULT_BORDER;
    }

    private TitledBorder getTitledBorder() {
        return (TitledBorder) getBorder();
    }

    private void updateBorder() {
        this.border = new TitledBorder(innerBorder, title, justification, position, font, color);
    }

    /** Returns the border's properties for specified component.
    * The default implementation returns empty array.
    * @param node the RADVisualNode of the component which properties we request
    * @return the layout-specific properties for specified component
    */
    public Node.Property[] getProperties() {
        return new Node.Property[] {
                   new BorderProp(PROP_TITLE, String.class,
                                  BorderInfo.bundle.getString("PROP_Title"),
                                  BorderInfo.bundle.getString("HINT_Title")) {
                       public Object getValue () {
                           return getTitledBorder().getTitle();
                       }

                       public void setValue (Object val) throws IllegalArgumentException {
                           if (val instanceof String) {
                               title = (String)val;
                               updateBorder();
                               firePropChange();
                           }
                           else
                               throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return false;
                       }
                   },

                   new BorderProp(PROP_BORDER, Border.class,
                                  BorderInfo.bundle.getString("PROP_Border"),
                                  BorderInfo.bundle.getString("HINT_Border")) {
                       public Object getValue () {
                           return innerBorder;
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           if ((val == null) || (val instanceof Border)) {
                               innerBorder = (Border) val;
                               updateBorder();
                               firePropChange();
                           }
                           else
                               throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return DEFAULT_INNER_BORDER.equals(innerBorder);
                       }
                   },

                   new BorderProp(PROP_TITLE_JUSTIFICATION, Integer.TYPE,
                                  BorderInfo.bundle.getString("PROP_TitleJustification"),
                                  BorderInfo.bundle.getString("HINT_TitleJustification")) {
                       public Object getValue () {
                           return new Integer(justification);
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           if (val instanceof Integer) {
                               justification = ((Integer)val).intValue();
                               updateBorder();
                               firePropChange();
                           }
                           else
                               throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return DEFAULT_BORDER.getTitleJustification() == justification;
                       }
                       public PropertyEditor getPropertyEditor() {
                           if (System.getProperty ("java.version", "1.3").startsWith ("1.2")) { // there are no LEADING and TRAILING until JDK 1.3
                               return new ChoicePropertyEditor(
                                          new int[] {
                                              TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.LEFT,
                                              TitledBorder.CENTER, TitledBorder.RIGHT
                                          },
                                          new String[] {
                                              BorderInfo.bundle.getString("VALUE_JustDefault"),
                                              BorderInfo.bundle.getString("VALUE_JustLeft"),
                                              BorderInfo.bundle.getString("VALUE_JustCenter"),
                                              BorderInfo.bundle.getString("VALUE_JustRight"),
                                          }
                                      );
                           } else {
                               // the numbers 4 and 5 instead of the constants from TitledBorder
                               // are used to make the build script (using JDK 1.2 compiler) happy
                               return new ChoicePropertyEditor(
                                          new int[] {
                                              TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.LEFT,
                                              TitledBorder.CENTER, TitledBorder.RIGHT, 4 /*TitledBorder.LEADING */, 5 /*TitledBorder.TRAILING */
                                          },
                                          new String[] {
                                              BorderInfo.bundle.getString("VALUE_JustDefault"),
                                              BorderInfo.bundle.getString("VALUE_JustLeft"),
                                              BorderInfo.bundle.getString("VALUE_JustCenter"),
                                              BorderInfo.bundle.getString("VALUE_JustRight"),
                                              BorderInfo.bundle.getString("VALUE_JustLeading"),
                                              BorderInfo.bundle.getString("VALUE_JustTrailing"),
                                          }
                                      );
                           }
                       }
                   },

                   new BorderProp(PROP_TITLE_POSITION, Integer.TYPE,
                                  BorderInfo.bundle.getString("PROP_TitlePosition"),
                                  BorderInfo.bundle.getString("HINT_TitlePosition")) {
                       public Object getValue () {
                           return new Integer(position);
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           if (val instanceof Integer) {
                               position = ((Integer)val).intValue();
                               updateBorder();
                               firePropChange();
                           }
                           else
                               throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return DEFAULT_BORDER.getTitlePosition() == position;
                       }
                       public PropertyEditor getPropertyEditor() {
                           return new ChoicePropertyEditor(
                                      new int[] {
                                          TitledBorder.DEFAULT_POSITION, TitledBorder.ABOVE_TOP, TitledBorder.TOP,
                                          TitledBorder.BELOW_TOP, TitledBorder.ABOVE_BOTTOM, TitledBorder.BOTTOM,
                                          TitledBorder.BELOW_BOTTOM
                                      },
                                      new String[] {
                                          BorderInfo.bundle.getString("VALUE_PosDefault"),
                                          BorderInfo.bundle.getString("VALUE_PosAboveTop"),
                                          BorderInfo.bundle.getString("VALUE_PosTop"),
                                          BorderInfo.bundle.getString("VALUE_PosBelowTop"),
                                          BorderInfo.bundle.getString("VALUE_PosAboveBottom"),
                                          BorderInfo.bundle.getString("VALUE_PosBottom"),
                                          BorderInfo.bundle.getString("VALUE_PosBelowBottom")
                                      }
                                  );
                       }
                   },

                   new BorderProp(PROP_TITLE_FONT, Font.class,
                                  BorderInfo.bundle.getString("PROP_TitleFont"),
                                  BorderInfo.bundle.getString("HINT_TitleFont")) {
                       public Object getValue () {
                           return font;
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           if (val instanceof Font) {
                               font = (Font)val;
                               updateBorder();
                               firePropChange();
                           }
                           else
                               throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return DEFAULT_BORDER.getTitleFont().equals(font);
                       }
                   },

                   new BorderProp(PROP_TITLE_COLOR, Color.class,
                                  BorderInfo.bundle.getString("PROP_TitleColor"),
                                  BorderInfo.bundle.getString("HINT_TitleColor")) {
                       public Object getValue () {
                           return color;
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           if (val instanceof Color) {
                               color = (Color)val;
                               updateBorder();
                               firePropChange();
                           }
                           else
                               throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return DEFAULT_BORDER.getTitleColor().equals(color);
                       }
                   },

               };
    }

    protected int[][] getConstructors() {
        return constructors;
    }

    public String getDisplayName() {
        return BorderInfo.bundle.getString("NAME_TitledBorder");
    }

    //--------------------------------------------------------------------------
    // XMLPersistence implementation

    public static final String XML_TITLED_BORDER = "TitledBorder"; // NOI18N

    public static final String ATTR_TITLE = "title"; // NOI18N
    public static final String ATTR_INNER_BORDER = "innerBorder"; // NOI18N
    public static final String ATTR_JUSTIFICATION = "justification"; // NOI18N
    public static final String ATTR_POSITION = "position"; // NOI18N
    public static final String ATTR_FONT = "font"; // NOI18N
    public static final String ATTR_COLOR = "color"; // NOI18N

    /** Called to load property value from specified XML subtree. If succesfully loaded,
    * the value should be available via the getValue method.
    * An IOException should be thrown when the value cannot be restored from the specified XML element
    * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
    * @exception IOException thrown when the value cannot be restored from the specified XML element
    */
    public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_TITLED_BORDER.equals (element.getNodeName ())) {
            throw new java.io.IOException ();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
        try {
            org.w3c.dom.Node aNode = attributes.getNamedItem (ATTR_TITLE);
            if(aNode != null) {
                title = aNode.getNodeValue ();
            }
            aNode = attributes.getNamedItem (ATTR_JUSTIFICATION);
            if(aNode != null) {
                justification = Integer.parseInt (aNode.getNodeValue ());
            }
            aNode = attributes.getNamedItem (ATTR_POSITION);
            if(aNode != null) {
                position = Integer.parseInt (aNode.getNodeValue ());
            }
            Border readBorder = (Border) FormUtils.readProperty (ATTR_INNER_BORDER, Border.class, element);
            if (readBorder != null) innerBorder =  readBorder;

            Color readColor = (Color) FormUtils.readProperty (ATTR_COLOR, Color.class, element);
            if (readColor != null) color =  readColor;

            Font readFont = (Font) FormUtils.readProperty (ATTR_FONT, Font.class, element);
            if (readFont != null) font =  readFont;
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
        org.w3c.dom.Element el = doc.createElement (XML_TITLED_BORDER);
        if (!title.equals(DEFAULT_BORDER.getTitle())) el.setAttribute (ATTR_TITLE, title);
        if (justification != DEFAULT_BORDER.getTitleJustification()) el.setAttribute (ATTR_JUSTIFICATION, Integer.toString (justification));
        if (position != DEFAULT_BORDER.getTitlePosition()) el.setAttribute (ATTR_POSITION, Integer.toString (position));
        if(!font.equals (DEFAULT_BORDER.getTitleFont())) {
            FormUtils.writeProperty (ATTR_FONT, font, Font.class, el, doc);
        }
        if(!color.equals (DEFAULT_BORDER.getTitleColor())) {
            FormUtils.writeProperty (ATTR_COLOR, color, Color.class, el, doc);
        }
        if(!innerBorder.equals (DEFAULT_INNER_BORDER)) {
            FormUtils.writeProperty (ATTR_INNER_BORDER, innerBorder, Border.class, el, doc);
        }
        return el;
    }
}

/*
 * Log
 *  11   Gandalf   1.10        2/18/00  Ian Formanek    Fixed to make JDK 1.2 
 *       compiler (in build script) happy
 *  10   Gandalf   1.9         2/17/00  Ian Formanek    Fixed bug 5823 - The 
 *       code for TitledBorder is not generated, if font or color properties of 
 *       the TitledBorder are modified.
 *  9    Gandalf   1.8         1/12/00  Ian Formanek    NOI18N
 *  8    Gandalf   1.7         12/9/99  Pavel Buzek     
 *  7    Gandalf   1.6         11/24/99 Pavel Buzek     added support for saving
 *       in XML format
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         6/30/99  Ian Formanek    reflecting package 
 *       change of ChoicePropertyEditor
 *  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         4/6/99   Ian Formanek    Fixed last change
 *  2    Gandalf   1.1         4/6/99   Ian Formanek    fixed obtaining 
 *       resources (Object.class.getResource -> getClass ().getResource)
 *  1    Gandalf   1.0         4/2/99   Ian Formanek    
 * $
 */
