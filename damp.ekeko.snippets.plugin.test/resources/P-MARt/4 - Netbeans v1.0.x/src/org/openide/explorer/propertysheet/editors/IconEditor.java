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

package org.openide.explorer.propertysheet.editors;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.border.*;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

/**
* PropertyEditor for Icons. Depends on existing DataObject for images.
* Images must be represented by some DataObject which returns itselv
* as cookie, and has image file as a primary file. File extensions
* for images is specified in isImage method.
*
* @author Jan Jancura
*/
public class IconEditor extends Object implements PropertyEditor, XMLPropertyEditor {

    public static final int TYPE_URL = 1;
    public static final int TYPE_FILE = 2;
    public static final int TYPE_CLASSPATH = 3;

    static final String URL_PREFIX = "URL"; // NOI18N
    static final String FILE_PREFIX = "File"; // NOI18N
    static final String CLASSPATH_PREFIX = "Classpath"; // NOI18N

    /** Standart variable for localisation. */
    static java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle (
                IconEditor.class);

    public static boolean isImage (String s) {
        s = s.toLowerCase ();
        return s.endsWith (".jpg") || s.endsWith (".gif") || // NOI18N
               s.endsWith (".jpeg") || s.endsWith (".jpe") || // NOI18N
               s.equals ("jpg") || s.equals ("gif") || // NOI18N
               s.equals ("jpeg") || s.equals ("jpe"); // NOI18N
    }

    static String convert (String s) {
        StringTokenizer st = new StringTokenizer (s, "\\"); // NOI18N
        StringBuffer sb = new StringBuffer ();
        if (st.hasMoreElements ()) {
            sb.append (st.nextElement ());
            while (st.hasMoreElements ())
                sb.append ("\\\\").append (st.nextElement ()); // NOI18N
        }
        return new String (sb);
    }


    // variables .................................................................................

    private Icon icon;
    private PropertyChangeSupport support;


    // init .......................................................................................

    public IconEditor() {
        support = new PropertyChangeSupport (this);
    }

    // Special access methods......................................................................

    /** @return the type of image source - one of TYPE_CLASSPATH, TYPE_FILE, TYPE_URL */
    public int getSourceType () {
        if (getValue () == null) return TYPE_FILE;
        try {
            NbImageIcon ii = (NbImageIcon)getValue ();
            return ii.type;
        } catch (ClassCastException e) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
            return TYPE_FILE;
        }
    }

    /** @return the name of image's source - depending on the type it can be a URL, file name or
    * resource path to the image on classpath */
    public String getSourceName () {
        if (getValue () == null) return null;
        try {
            NbImageIcon ii = (NbImageIcon)getValue ();
            return ii.name;
        } catch (ClassCastException e) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
            return null;
        }
    }

    // PropertyEditor methods .....................................................................

    /**
    * @return The value of the property.  Builtin types such as "int" will
    * be wrapped as the corresponding object type such as "java.lang.Integer".
    */
    public Object getValue () {
        return icon;
    }

    /**
    * Set (or change) the object that is to be edited.  Builtin types such
    * as "int" must be wrapped as the corresponding object type such as
    * "java.lang.Integer".
    *
    * @param value The new target object to be edited.  Note that this
    *     object should not be modified by the PropertyEditor, rather
    *     the PropertyEditor should create a new object to hold any
    *     modified value.
    */
    public void setValue (Object object) {
        Icon old = icon;
        icon = (Icon) object;
        support.firePropertyChange ("value", old, icon); // NOI18N
    }

    /**
    * @return The property value as a human editable string.
    * <p>   Returns null if the value can't be expressed as an editable string.
    * <p>   If a non-null value is returned, then the PropertyEditor should
    *       be prepared to parse that string back in setAsText().
    */
    public String getAsText () {
        if (getValue () == null) return "null"; // NOI18N
        try {
            NbImageIcon ii = (NbImageIcon)getValue ();
            switch (ii.type) {
            case TYPE_URL: return URL_PREFIX + ": " + ii.name; // NOI18N
            case TYPE_FILE: return FILE_PREFIX + ": " + ii.name; // NOI18N
            case TYPE_CLASSPATH: return CLASSPATH_PREFIX + ": " + ii.name; // NOI18N
            }
        } catch (ClassCastException e) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
        }
        return null;
    }

    /**
    * Set the property value by parsing a given String.  May raise
    * java.lang.IllegalArgumentException if either the String is
    * badly formatted or if this kind of property can't be expressed
    * as text.
    * @param text  The string to be parsed.
    */
    public void setAsText (String string) throws IllegalArgumentException {
        NbImageIcon ii;
        try {
            if (string.startsWith (FILE_PREFIX)) {
                String s = string.substring (FILE_PREFIX.length () + 1).trim ();
                ii = new NbImageIcon (s);
                ii.type = TYPE_FILE;
                ii.name = s;
            } else
                if (string.startsWith (CLASSPATH_PREFIX)) {
                    String s = string.substring (CLASSPATH_PREFIX.length () + 1).trim ();
                    URL url = TopManager.getDefault ().currentClassLoader ().getResource (s);
                    ii = new NbImageIcon (url);
                    ii.type = TYPE_CLASSPATH;
                    ii.name = s;
                } else
                    if (string.startsWith (URL_PREFIX)) {
                        String s = string.substring (URL_PREFIX.length () + 1).trim ();
                        URL url = new URL (s);
                        ii = new NbImageIcon (url);
                        ii.type = TYPE_URL;
                        ii.name = s;
                    } else {
                        ii = new NbImageIcon (string.trim ());
                        ii.type = TYPE_FILE;
                        ii.name = string;
                    }
            setValue (ii);
        } catch (Exception e) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
            throw new IllegalArgumentException (e.toString ());
        }
    }

    /**
    * This method is intended for use when generating Java code to set
    * the value of the property.  It should return a fragment of Java code
    * that can be used to initialize a variable with the current property
    * value.
    * <p>
    * Example results are "2", "new Color(127,127,34)", "Color.orange", etc.
    *
    * @return A fragment of Java code representing an initializer for the
    *     current value.
    */
    public String getJavaInitializationString () {
        if (getValue () == null) return "null"; // NOI18N
        try {
            NbImageIcon ii = (NbImageIcon)getValue ();
            switch (ii.type) {
            case TYPE_URL: return
                    "new javax.swing.JLabel () {\n" + // NOI18N
                    "  public javax.swing.Icon getIcon () {\n" + // NOI18N
                    "    try {\n" + // NOI18N
                    "      return new javax.swing.ImageIcon (\n" + // NOI18N
                    "        new java.net.URL (\"" + convert (ii.name) + "\")\n" + // NOI18N
                    "      );\n" + // NOI18N
                    "    } catch (java.net.MalformedURLException e) {\n" + // NOI18N
                    "    }\n" + // NOI18N
                    "    return null;\n" + // NOI18N
                    "  }\n" + // NOI18N
                    "}.getIcon ()"; // NOI18N
            case TYPE_FILE: return
                    "new javax.swing.ImageIcon (\"" + convert (ii.name) + "\")"; // NOI18N
            case TYPE_CLASSPATH: return
                    "new javax.swing.ImageIcon (getClass ().getResource (\"" + convert (ii.name) + "\"))"; // NOI18N
            }
        } catch (ClassCastException e) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
        }
        return "null"; // NOI18N
    }

    /**
    * If the property value must be one of a set of known tagged values,
    * then this method should return an array of the tags.  This can
    * be used to represent (for example) enum values.  If a PropertyEditor
    * supports tags, then it should support the use of setAsText with
    * a tag value as a way of setting the value and the use of getAsText
    * to identify the current value.
    *
    * @return The tag values for this property.  May be null if this
    *   property cannot be represented as a tagged value.
    *
    */
    public String[] getTags () {
        return null;
    }

    /**
    * @return  True if the class will honor the paintValue method.
    */
    public boolean isPaintable () {
        return false;
    }

    /**
    * Paint a representation of the value into a given area of screen
    * real estate.  Note that the propertyEditor is responsible for doing
    * its own clipping so that it fits into the given rectangle.
    * <p>
    * If the PropertyEditor doesn't honor paint requests (see isPaintable)
    * this method should be a silent noop.
    * <p>
    * The given Graphics object will have the default font, color, etc of
    * the parent container.  The PropertyEditor may change graphics attributes
    * such as font and color and doesn't need to restore the old values.
    *
    * @param gfx  Graphics object to paint into.
    * @param box  Rectangle within graphics object into which we should paint.
    */
    public void paintValue (Graphics g, Rectangle rectangle) {
    }

    /**
    * @return  True if the propertyEditor can provide a custom editor.
    */
    public boolean supportsCustomEditor () {
        return true;
    }

    /**
    * A PropertyEditor may choose to make available a full custom Component
    * that edits its property value.  It is the responsibility of the
    * PropertyEditor to hook itself up to its editor Component itself and
    * to report property value changes by firing a PropertyChange event.
    * <P>
    * The higher-level code that calls getCustomEditor may either embed
    * the Component in some larger property sheet, or it may put it in
    * its own individual dialog, or ...
    *
    * @return A java.awt.Component that will allow a human to directly
    *      edit the current property value.  May be null if this is
    *      not supported.
    */
    public java.awt.Component getCustomEditor () {
        return new IconPanel ();
    }

    /**
    * Register a listener for the PropertyChange event.  When a
    * PropertyEditor changes its value it should fire a PropertyChange
    * event on all registered PropertyChangeListeners, specifying the
    * null value for the property name and itself as the source.
    *
    * @param listener  An object to be invoked when a PropertyChange
    *    event is fired.
    */
    public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener (propertyChangeListener);
    }

    /**
    * Remove a listener for the PropertyChange event.
    *
    * @param listener  The PropertyChange listener to be removed.
    */
    public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener (propertyChangeListener);
    }


    // innerclasses ...............................................................

    public static class NbImageIcon extends ImageIcon implements Externalizable {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 7018807466471349466L;
        int type;
        String name;

        public NbImageIcon () {
        }

        NbImageIcon (URL url) {
            super (url);
            type = TYPE_URL;
        }

        NbImageIcon (String file) {
            super (file);
            type = TYPE_FILE;
        }

        String getName () {
            return name;
        }

        public void writeExternal (ObjectOutput oo) throws IOException {
            oo.writeObject (new Integer (type));
            oo.writeObject (name);
        }

        public void readExternal (ObjectInput in)
        throws IOException, ClassNotFoundException {
            type = ((Integer)in.readObject ()).intValue ();
            name = (String) in.readObject ();
            ImageIcon ii = null;
            switch (type) {
            case TYPE_URL:
                try {
                    ii = new ImageIcon (new URL (name));
                } catch (java.net.MalformedURLException e) {
                    if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
                    return;
                }
                break;
            case TYPE_FILE:
                ii = new ImageIcon (name);
                break;
            case TYPE_CLASSPATH:
                ii = new ImageIcon (TopManager.getDefault ().currentClassLoader ().getResource (name));
                break;
            }
            setImage (ii.getImage ());
        }
    }

    class IconPanel extends JPanel implements EnhancedCustomPropertyEditor {
        JRadioButton rbUrl, rbFile, rbClasspath, rbNoPicture;
        JTextField tfName;
        JButton bSelect;
        JScrollPane spImage;

        static final long serialVersionUID =-6904264999063788703L;
        IconPanel () {

            // visual components .............................................

            JLabel lab;
            setLayout (new BorderLayout (6, 6));
            setBorder (new EmptyBorder (6, 6, 6, 6));
            JPanel p = new JPanel (new BorderLayout (3, 3));
            JPanel p1 = new JPanel (new BorderLayout ());
            p1.setBorder (new TitledBorder (new EtchedBorder (), bundle.getString ("CTL_ImageSourceType")));
            JPanel p2 = new JPanel ();
            p2.setBorder (new EmptyBorder (0, 3, 0, 3));
            GridBagLayout l = new GridBagLayout ();
            GridBagConstraints c = new GridBagConstraints ();
            p2.setLayout (l);
            c.anchor = GridBagConstraints.WEST;

            p2.add (rbUrl = new JRadioButton (bundle.getString ("CTL_URL")));
            c.gridwidth = 1;
            l.setConstraints (rbUrl, c);

            p2.add (lab = new JLabel (bundle.getString ("CTL_URLExample")));
            c.gridwidth = GridBagConstraints.REMAINDER;
            l.setConstraints (lab, c);

            p2.add (rbFile = new JRadioButton (bundle.getString ("CTL_File")));
            c.gridwidth = 1;
            l.setConstraints (rbFile, c);

            p2.add (lab = new JLabel (bundle.getString ("CTL_FileExample")));
            c.gridwidth = GridBagConstraints.REMAINDER;
            l.setConstraints (lab, c);

            p2.add (rbClasspath = new JRadioButton (bundle.getString ("CTL_Classpath")));
            c.gridwidth = 1;
            l.setConstraints (rbClasspath, c);

            p2.add (lab = new JLabel (bundle.getString ("CTL_ClasspathExample")));
            c.gridwidth = GridBagConstraints.REMAINDER;
            l.setConstraints (lab, c);

            p2.add (rbNoPicture = new JRadioButton (bundle.getString ("CTL_NoPicture")));
            c.gridwidth = 1;
            l.setConstraints (rbNoPicture, c);

            p2.add (lab = new JLabel (bundle.getString ("CTL_Null")));
            c.gridwidth = GridBagConstraints.REMAINDER;
            l.setConstraints (lab, c);

            ButtonGroup bg = new ButtonGroup ();
            bg.add (rbUrl);
            bg.add (rbFile);
            bg.add (rbClasspath);
            bg.add (rbNoPicture);
            rbUrl.setSelected (true);
            p1.add (p2, "West"); // NOI18N
            p.add (p1, "North"); // NOI18N
            p1 = new JPanel (new BorderLayout (6, 6));
            p1.add (new JLabel (bundle.getString ("CTL_ImageSourceName")), "West");
            p1.add (tfName = new JTextField (), "Center"); // NOI18N
            p1.add (bSelect = new JButton ("..."), "East"); // NOI18N
            bSelect.setEnabled (false);
            p.add (p1, "South"); // NOI18N
            add (p, "North"); // NOI18N
            spImage = new JScrollPane () {
                          public Dimension getPreferredSize () {
                              return new Dimension (60, 60);
                          }
                      };
            add (spImage, "Center"); // NOI18N

            // listeners .................................................

            tfName.addActionListener (new ActionListener () {
                                          public void actionPerformed (ActionEvent e) {
                                              setValue ();
                                          }
                                      });
            rbUrl.addActionListener (new ActionListener () {
                                         public void actionPerformed (ActionEvent e) {
                                             bSelect.setEnabled (false);
                                             tfName.setEnabled (true);
                                             setValue ();
                                         }
                                     });
            rbFile.addActionListener (new ActionListener () {
                                          public void actionPerformed (ActionEvent e) {
                                              bSelect.setEnabled (true);
                                              tfName.setEnabled (true);
                                              setValue ();
                                              updateIcon ();
                                          }
                                      });
            rbClasspath.addActionListener (new ActionListener () {
                                               public void actionPerformed (ActionEvent e) {
                                                   bSelect.setEnabled (true);
                                                   tfName.setEnabled (true);
                                                   setValue ();
                                               }
                                           });
            rbNoPicture.addActionListener (new ActionListener () {
                                               public void actionPerformed (ActionEvent e) {
                                                   bSelect.setEnabled (false);
                                                   tfName.setEnabled (false);
                                                   IconEditor.this.setValue (null);
                                                   updateIcon ();
                                               }
                                           });
            bSelect.addActionListener (new ActionListener () {
                                           public void actionPerformed (ActionEvent e) {
                                               if (rbFile.isSelected ()) {
                                                   JFileChooser chooser = new JFileChooser ();
                                                   FileFilter filter = new FileFilter () {
                                                                           public boolean accept (java.io.File f) {
                                                                               return f.isDirectory () || isImage (f.getName ());
                                                                           }
                                                                           public String getDescription () {
                                                                               return bundle.getString ("CTL_ImagesExtensionName");
                                                                           }
                                                                       };
                                                   chooser.setFileFilter (filter);
                                                   chooser.setFileSelectionMode (JFileChooser.FILES_AND_DIRECTORIES );
                                                   int returnVal = chooser.showOpenDialog (
                                                                       IconPanel.this
                                                                   );
                                                   if (returnVal != JFileChooser.APPROVE_OPTION) return;
                                                   tfName.setText (chooser.getSelectedFile ().getAbsolutePath ());
                                                   setValue ();
                                               } else
                                                   if (rbClasspath.isSelected ()) {
                                                       //            InputPanel ip = new InputPanel ();
                                                       Places places = TopManager.getDefault ().getPlaces ();
                                                       Node ds = places.nodes ().repository (new DataFilter () {
                                                                                                 public boolean acceptDataObject (DataObject obj) {
                                                                                                     // accept only data folders but ignore read only roots of file systems
                                                                                                     if (obj instanceof DataFolder)
                                                                                                         return !obj.getPrimaryFile ().isReadOnly () ||
                                                                                                                obj.getPrimaryFile ().getParent () != null;
                                                                                                     return isImage (obj.getPrimaryFile ().getExt ());
                                                                                                 }
                                                                                             });

                                                       String name;
                                                       try {
                                                           // selects one folder from data systems
                                                           DataObject d = (DataObject)
                                                                          TopManager.getDefault ().getNodeOperation ().select (
                                                                              bundle.getString ("CTL_OpenDialogName"),
                                                                              bundle.getString ("CTL_FileSystemName"),
                                                                              TopManager.getDefault ().getPlaces ().nodes ().repository (),
                                                                              new NodeAcceptor () {
                                                                                  public boolean acceptNodes (Node[] nodes) {
                                                                                      if ((nodes == null) || (nodes.length != 1))
                                                                                          return false;
                                                                                      return nodes[0].getCookie(DataFolder.class) == null;
                                                                                  }
                                                                              },
                                                                              null
                                                                          )[0].getCookie(DataObject.class);
                                                           name = (d.getPrimaryFile ().getPackageNameExt ('/', '.'));
                                                       } catch (org.openide.util.UserCancelException ex) {
                                                           return;
                                                       }
                                                       tfName.setText ("/" + name); // NOI18N
                                                       setValue ();
                                                   }
                                           }
                                       });

            // initialization ......................................

            updateIcon ();
            Icon i = (Icon)getValue ();
            if (i == null) {
                rbNoPicture.setSelected (true);
                bSelect.setEnabled (false);
                tfName.setEnabled (false);
                return;
            }
            if (!(i instanceof NbImageIcon)) return;
            switch (((NbImageIcon)i).type) {
            case TYPE_URL:
                rbUrl.setSelected (true);
                bSelect.setEnabled (false);
                break;
            case TYPE_FILE:
                rbFile.setSelected (true);
                bSelect.setEnabled (true);
                break;
            case TYPE_CLASSPATH:
                rbClasspath.setSelected (true);
                bSelect.setEnabled (true);
                break;
            }
            tfName.setText (((NbImageIcon)i).name);

            HelpCtx.setHelpIDString (this, IconPanel.class.getName ());
        }

        void updateIcon () {
            Icon i = (Icon)getValue ();
            spImage.setViewportView ((i == null) ? new JLabel () : new JLabel (i));
            //      repaint ();
            validate ();
        }

        void setValue () {
            String s = ""; // NOI18N
            if (rbUrl.isSelected ()) s = URL_PREFIX + ": "; // NOI18N
            else
                if (rbFile.isSelected ()) s = FILE_PREFIX + ": "; // NOI18N
                else
                    if (rbClasspath.isSelected ()) s = CLASSPATH_PREFIX + ": "; // NOI18N
            try {
                setAsText (s + tfName.getText ());
            } catch (IllegalArgumentException ee) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) ee.printStackTrace (); // NOI18N
            }
            updateIcon ();
            /*      Icon i = (Icon) getValue ();
                  if (i != null) {
                    spImage.setViewportView (new JLabel (i));
                    repaint ();
                    validate ();
                  }*/
        }

        /**
        * @return Returns the property value that is result of the CustomPropertyEditor.
        * @exception InvalidStateException when the custom property editor does not
        * represent valid property value (and thus it should not be set)
        */
        public Object getPropertyValue () throws IllegalStateException {
            NbImageIcon ii = null;
            String s = tfName.getText().trim();
            try {
                if (rbFile.isSelected ()) {
                    ii = new NbImageIcon (s);
                    ii.type = TYPE_FILE;
                    ii.name = s;
                } else
                    if (rbClasspath.isSelected ()) {
                        URL url = TopManager.getDefault ().currentClassLoader ().getResource (s);
                        ii = new NbImageIcon (url);
                        ii.type = TYPE_CLASSPATH;
                        ii.name = s;
                    } else
                        if (rbUrl.isSelected()) {
                            URL url = new URL (s);
                            ii = new NbImageIcon (url);
                            ii.type = TYPE_URL;
                            ii.name = s;
                        }
            } catch (Exception e) {
                if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
                throw new IllegalStateException (e.toString ());
            }
            return ii;
        }

    } // end of IconPanel

    //--------------------------------------------------------------------------
    // XMLPropertyEditor implementation

    public static final String XML_IMAGE = "Image"; // NOI18N

    public static final String ATTR_TYPE = "iconType"; // NOI18N
    public static final String ATTR_NAME = "name"; // NOI18N

    /** Called to load property value from specified XML subtree. If succesfully loaded,
    * the value should be available via the getValue method.
    * An IOException should be thrown when the value cannot be restored from the specified XML element
    * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
    * @exception IOException thrown when the value cannot be restored from the specified XML element
    */
    public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_IMAGE.equals (element.getNodeName ())) {
            throw new java.io.IOException ();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
        try {
            int type = Integer.parseInt (attributes.getNamedItem (ATTR_TYPE).getNodeValue ());
            String name = attributes.getNamedItem (ATTR_NAME).getNodeValue ();
            switch (type) {
            case TYPE_URL: setAsText (URL_PREFIX + ": " + name); break; // NOI18N
            case TYPE_FILE: setAsText (FILE_PREFIX + ": " + name); break; // NOI18N
            case TYPE_CLASSPATH: setAsText (CLASSPATH_PREFIX + ": " + name); break; // NOI18N
            }
        } catch (NullPointerException e) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
            throw new java.io.IOException (e.toString());
        }
    }

    /** Called to store current property value into XML subtree. The property value should be set using the
    * setValue method prior to calling this method.
    * @param doc The XML document to store the XML in - should be used for creating nodes only
    * @return the XML DOM element representing a subtree of XML from which the value should be loaded
    */
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        org.w3c.dom.Element el = doc.createElement (XML_IMAGE);
        NbImageIcon ii = (NbImageIcon)getValue ();
        el.setAttribute (ATTR_TYPE, Integer.toString(ii.type));
        el.setAttribute (ATTR_NAME, ii.name);
        return el;
    }
}

/*
* Log
*  20   Gandalf   1.19        1/12/00  Ian Formanek    
*  19   Gandalf   1.18        1/12/00  Ian Formanek    I18N
*  18   Gandalf   1.17        1/12/00  Ian Formanek    NOI18N
*  17   Gandalf   1.16        12/9/99  Pavel Buzek     
*  16   Gandalf   1.15        11/24/99 Pavel Buzek     XMLPropertyEditor 
*       interface added
*  15   Gandalf   1.14        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  14   Gandalf   1.13        8/17/99  Ian Formanek    Fixed bug 3429 - Icon 
*       propertiess are not supported yet in the forms editor correctly.
*  13   Gandalf   1.12        8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  12   Gandalf   1.11        7/8/99   Jesse Glick     Context help.
*  11   Gandalf   1.10        6/30/99  Ian Formanek    Moved to package 
*       org.openide.explorer.propertysheet.editors
*  10   Gandalf   1.9         6/30/99  Ian Formanek    Reflecting changes in 
*       editors packages and enhanced property editor interfaces
*  9    Gandalf   1.8         6/24/99  Jesse Glick     Gosh-honest HelpID's.
*  8    Gandalf   1.7         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  7    Gandalf   1.6         4/27/99  Jesse Glick     new HelpCtx () -> 
*       HelpCtx.DEFAULT_HELP.
*  6    Gandalf   1.5         3/4/99   Jan Jancura     WindowToolkit removed
*  5    Gandalf   1.4         3/4/99   Jan Jancura     bundle moved
*  4    Gandalf   1.3         2/20/99  David Simonek   bugfix #1142
*  3    Gandalf   1.2         1/6/99   Jaroslav Tulach 
*  2    Gandalf   1.1         1/6/99   Jaroslav Tulach ide.* extended to 
*       ide.loaders.*
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
