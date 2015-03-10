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

package org.netbeans.examples.layoutmanager.twocolumn;

import java.awt.*;
import java.beans.*;

// Open APIs:
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;

// Layout Manager API:
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.compat2.layouts.DesignLayout;

/** Design-time support for TwoColumnLayout.
 */
public class DesignTwoColumnLayout extends DesignLayout
{
    /** Code name of the property for the horizontal gap.
     */
    private static final String PROP_HGAP = "horizontalGap";
    /** Code name of the property for the vertical gap.
     */
    private static final String PROP_VGAP = "verticalGap";
    /** Default value for the vertical gap.
     */
    private static final int DEFAULT_VGAP = 0;
    /** Default value for the horizontal gap.
     */
    private static final int DEFAULT_HGAP = 0;

    /** Code name of the property for the alignment.
     */
    private static final String PROP_ALIGNMENT = "alignment";
    /** Default value for the alignment.
     */
    private static final String DEFAULT_ALIGNMENT = TwoColumnLayout.LEFT;

    private Sheet _propSheet;
    private Node.Property _hgapProp;
    private Node.Property _vgapProp;

    private int _hgap = DEFAULT_HGAP;
    private int _vgap = DEFAULT_VGAP;

    public String getDisplayName() {
        return "TwoColumnLayout";
    }

    public Class getLayoutClass() {
        return TwoColumnLayout.class;
    }

    private static Image _icon16;

    public Image getIcon(int iconKind) {
        if (iconKind == BeanInfo.ICON_COLOR_16x16) {
            if (_icon16 == null) {
                _icon16 = Toolkit.getDefaultToolkit().getImage(
                              getClass().getResource("TwoColumnLayout16.gif"));
            }
            return _icon16;
        }
        else
            return super.getIcon(iconKind);
    }

    //
    // container/components handling
    //

    public void setRADContainer (RADVisualContainer cont) {
        super.setRADContainer(cont);
        if (cont != null) {
            getContainer().setLayout(new TwoColumnLayout(_hgap, _vgap));
        }
    }

    public void addComponent (RADVisualComponent comp) {
        ConstraintsDescription cd = comp.getConstraints(getClass());
        if (cd == null) {
            cd = new ConstraintsDescription();
            comp.setConstraints(getClass(), cd);
        }

        Object align = cd.getConstraintsObject();
        Component visual = getFormManager().getVisualRepresentation (comp);
        getContainer().add(visual, align);
    }

    public void removeComponent (RADVisualComponent comp) {
        Component visual = getFormManager().getVisualRepresentation (comp);
        getContainer().remove(visual);
    }

    public void updateLayout () {
        getContainer().removeAll();

        RADVisualComponent[] children = getRADContainer().getSubComponents();

        for (int i = 0; i < children.length; i++)
            addComponent(children[i]);
    }

    void updateComponent (RADVisualComponent comp) {
        Component visual = getFormManager().getVisualRepresentation(comp);

        ConstraintsDescription cd = comp.getConstraints(getClass());
        if (cd == null) {
            cd = new ConstraintsDescription();
            comp.setConstraints(getClass(), cd);
        }

        int index = -1;
        for (int i = 0; i < getContainer().getComponentCount(); i++) {
            if (visual == getContainer().getComponent(i)) {
                index = i;
                break;
            }
        }

        if (index >= 0) {
            getContainer().remove(visual);
            getContainer().add(visual, cd.getConstraintsObject(), index);
            getContainer().validate();
        }
    }

    public ConstraintsDescription getConstraintsDescription(Point p) {
        return new ConstraintsDesc();
    }

    //
    // property handling
    //

    public void initChangedProperties (java.util.Map changedProperties) {
        Integer hgapVal = (Integer) changedProperties.get(PROP_HGAP);
        if (hgapVal != null)
            _hgap = hgapVal.intValue ();
        Integer vgapVal = (Integer) changedProperties.get (PROP_VGAP);
        if (vgapVal != null)
            _vgap = vgapVal.intValue ();
    }

    public java.util.List getChangedProperties () {
        // enforce creation of properties
        getPropertySet ();

        java.util.ArrayList list = new java.util.ArrayList(2);
        if (_hgap != DEFAULT_HGAP)
            list.add(_hgapProp);
        if (_vgap != DEFAULT_VGAP)
            list.add(_vgapProp);
        return list;
    }

    // access method for 1.2 compiler bug
    private Container _getContainer () {
        return getContainer ();
    }

    public Node.PropertySet[] getPropertySet() {
        if (_propSheet != null)
            return _propSheet.toArray();

        _propSheet = new Sheet ();
        Sheet.Set set = Sheet.createPropertiesSet ();
        set.put(_hgapProp = new PropertySupport.ReadWrite (
                                PROP_HGAP,
                                Integer.TYPE,
                                "horizontal gap",
                                "horizontal gap between components"
                            ) {
                                public Object getValue () {
                                    return new Integer(_hgap);
                                }

                                public void setValue (Object val) throws IllegalArgumentException {
                                    if (val instanceof Integer) {
                                        int newValue = ((Integer)val).intValue();
                                        if (_hgap == newValue)
                                            return;

                                        int oldValue = _hgap;
                                        _hgap = newValue;

                                        LayoutManager lm = _getContainer().getLayout();
                                        if (lm instanceof TwoColumnLayout) {
                                            ((TwoColumnLayout)lm).setHgap(_hgap);
                                            _getContainer().invalidate();
                                            _getContainer().validate();
                                        }
                                        firePropertyChange(null, PROP_HGAP,
                                                           new Integer(oldValue), new Integer(_hgap));
                                    }
                                    else
                                        throw new IllegalArgumentException();
                                }

                                public boolean supportsDefaultValue() {
                                    return true;
                                }

                                public void restoreDefaultValue () {
                                    try {
                                        setValue (new Integer (DEFAULT_HGAP));
                                    } catch (IllegalArgumentException e) { } // ignore failure
                                }
                            });

        set.put(_vgapProp = new PropertySupport.ReadWrite(
                                PROP_VGAP,
                                Integer.TYPE,
                                "vertical gap",
                                "vertical gap between components"
                            ) {
                                public Object getValue () {
                                    return new Integer(_vgap);
                                }

                                public void setValue (Object val) throws IllegalArgumentException {
                                    if (val instanceof Integer) {
                                        int newValue = ((Integer)val).intValue();
                                        if (_vgap == newValue)
                                            return;
                                        int oldValue = _vgap;
                                        _vgap = newValue;

                                        LayoutManager lm = _getContainer().getLayout();
                                        if (lm instanceof TwoColumnLayout) {
                                            ((TwoColumnLayout)lm).setVgap(_vgap);
                                            _getContainer().invalidate();
                                            _getContainer().validate();
                                        }
                                        firePropertyChange(null, PROP_VGAP,
                                                           new Integer(oldValue), new Integer(_vgap));
                                    }
                                    else
                                        throw new IllegalArgumentException();
                                }

                                public boolean supportsDefaultValue () {
                                    return true;
                                }

                                public void restoreDefaultValue () {
                                    try {
                                        setValue (new Integer (DEFAULT_VGAP));
                                    } catch (IllegalArgumentException e) { } // ignore failure
                                }
                            });
        _propSheet.put(set);
        return _propSheet.toArray();
    }

    public Node.Property[] getComponentProperties(RADVisualComponent comp) {
        final RADVisualComponent component = comp;
        return new Node.Property[] {
                   new PropertySupport.ReadWrite(
                       PROP_ALIGNMENT,
                       String.class,
                       "alignment",
                       "how to align a component inside its availabel space") {
                       public Object getValue () {
                           ConstraintsDescription cd =
                               component.getConstraints(DesignTwoColumnLayout.class);
                           if (cd == null)
                               return null;

                           return cd.getConstraintsString();
                       }

                       public void setValue (Object val) {
                           if (val instanceof String) {
                               Object oldValue = getValue ();
                               ConstraintsDescription cd = new ConstraintsDesc(val.toString());

                               if (component != null) {
                                   component.setConstraints(DesignTwoColumnLayout.class, cd);
                                   updateComponent(component);
                                   firePropertyChange (component, PROP_ALIGNMENT, oldValue, val);
                               }
                               else {
                                   throw new IllegalArgumentException();
                               }
                           }
                           else
                               throw new IllegalArgumentException();
                       }

                       public PropertyEditor getPropertyEditor () {
                           return new PropertyEditorSupport() {
                                      private /* 1.2 compiler bug: static */ final String[] _values = {
                                          TwoColumnLayout.LEFT,
                                          TwoColumnLayout.RIGHT,
                                          TwoColumnLayout.CENTER,
                                          TwoColumnLayout.FILL,
                                      };

                                      public String[] getTags () {
                                          return _values;
                                      }
                                  };
                       }
                   }
               };
    }

    //
    // Code generation
    //

    public String generateInitCode(RADVisualContainer cont) {
        LayoutManager defaultLM = null;

        try {
            Container defaultCont =
                (Container) BeanSupport.getDefaultInstance(cont.getBeanClass());

            if (defaultCont != null)
                defaultLM = defaultCont.getLayout();
        }
        catch (Exception e) {
            e.printStackTrace ();
            // ok, no default
        }

        if (defaultLM != null) {
            if (defaultLM.getClass().equals(TwoColumnLayout.class)) {
                if ((((TwoColumnLayout)defaultLM).getHgap () == _hgap) &&
                        (((TwoColumnLayout)defaultLM).getVgap () == _vgap)) {
                    return null;
                }
            }
        }

        StringBuffer buf = new StringBuffer();
        buf.append(createContainerGenName(cont));
        if ((_hgap != DEFAULT_HGAP) || (_vgap != DEFAULT_VGAP)) {
            buf.append("setLayout(new org.netbeans.examples.layoutmanager.twocolumn.TwoColumnLayout(");
            buf.append(_hgap);
            buf.append(", ");
            buf.append(_vgap);
            buf.append("));\n");
        }
        else {
            buf.append("setLayout(new org.netbeans.examples.layoutmanager.twocolumn.TwoColumnLayout());\n"); // NOI18N
        }
        return buf.toString();
    }

    public String generateComponentCode(RADVisualContainer cont, RADVisualComponent comp) {
        ConstraintsDescription cd = comp.getConstraints(getClass());
        String constr;

        if ((cd == null)
                || ((constr = (String) cd.getConstraintsObject()) == null))
            return "// ERROR GENERATING CODE\n";

        StringBuffer buf = new StringBuffer();
        buf.append(createContainerGenName(cont));
        buf.append("add(");
        buf.append(comp.getName());
        buf.append(", org.netbeans.examples.layoutmanager.twocolumn.TwoColumnLayout.");
        buf.append(constr.toUpperCase());
        buf.append(");\n");

        return buf.toString();
    }

    //
    // ConstraintsDescription
    //

    public static class ConstraintsDesc extends DesignLayout.ConstraintsDescription
    {
        private static final String CONSTRAINTS = "Component_Constraints";
        private static final String ATTR_ALIGNMENT = "alignment";

        private String _alignment;

        public ConstraintsDesc() {
            this(DEFAULT_ALIGNMENT);
        }

        public ConstraintsDesc(String align) {
            _alignment = align;
        }

        public String getConstraintsString() {
            return _alignment;
        }

        public Object getConstraintsObject() {
            return _alignment;
        }

        public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
            if (!CONSTRAINTS.equals (element.getNodeName ())) {
                throw new java.io.IOException ();
            }
            org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
            org.w3c.dom.Node alignNode = attributes.getNamedItem (ATTR_ALIGNMENT);
            if (alignNode != null)
                _alignment = alignNode.getNodeValue ();
        }

        public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
            org.w3c.dom.Element el = doc.createElement (CONSTRAINTS);
            el.setAttribute(ATTR_ALIGNMENT, _alignment);
            return el;
        }
    }
}

