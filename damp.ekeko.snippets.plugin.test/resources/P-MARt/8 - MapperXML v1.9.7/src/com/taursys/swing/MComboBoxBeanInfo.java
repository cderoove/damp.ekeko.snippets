package com.taursys.swing;

import java.beans.*;

/**
 * Title:        Mapper
 * Description:  Presentation Framework for Web and GUI Applicaitons
 * Copyright:    Copyright (c) 2002
 * Company:      Taurus Systems
 * @author Marty Phelan
 * @version 2.0
 */

public class MComboBoxBeanInfo extends SimpleBeanInfo {
  private Class beanClass = MComboBox.class;
  private String iconColor16x16Filename;
  private String iconColor32x32Filename;
  private String iconMono16x16Filename;
  private String iconMono32x32Filename;

  public MComboBoxBeanInfo() {
  }
  public PropertyDescriptor[] getPropertyDescriptors() {
    try {
      PropertyDescriptor _displayPropertyName = new PropertyDescriptor("displayPropertyName", beanClass, "getDisplayPropertyName", "setDisplayPropertyName");
//      _displayPropertyName.setPropertyEditorClass(com.taursys.beans.editors.ClassPropertyNameEditor.class);
      PropertyDescriptor _format = new PropertyDescriptor("format", beanClass, "getFormat", "setFormat");
      _format.setPropertyEditorClass(com.taursys.beans.editors.FormatEditor.class);
      PropertyDescriptor _formatPattern = new PropertyDescriptor("formatPattern", beanClass, "getFormatPattern", "setFormatPattern");
      PropertyDescriptor _MListCellRenderer = new PropertyDescriptor("MListCellRenderer", beanClass, "getMListCellRenderer", null);
      PropertyDescriptor _optionListValueHolder = new PropertyDescriptor("optionListValueHolder", beanClass, "getOptionListValueHolder", "setOptionListValueHolder");
      PropertyDescriptor[] pds = new PropertyDescriptor[] {
	      _displayPropertyName,
	      _format,
	      _formatPattern,
	      _MListCellRenderer,
	      _optionListValueHolder};
      return pds;





}
    catch(IntrospectionException ex) {
      ex.printStackTrace();
      return null;
    }
  }
  public java.awt.Image getIcon(int iconKind) {
    switch (iconKind) {
    case BeanInfo.ICON_COLOR_16x16:
        return iconColor16x16Filename != null ? loadImage(iconColor16x16Filename) : null;
    case BeanInfo.ICON_COLOR_32x32:
        return iconColor32x32Filename != null ? loadImage(iconColor32x32Filename) : null;
    case BeanInfo.ICON_MONO_16x16:
        return iconMono16x16Filename != null ? loadImage(iconMono16x16Filename) : null;
    case BeanInfo.ICON_MONO_32x32:
        return iconMono32x32Filename != null ? loadImage(iconMono32x32Filename) : null;
        }
    return null;
  }
  public BeanInfo[] getAdditionalBeanInfo() {
    Class superclass = beanClass.getSuperclass();
    try {
      BeanInfo superBeanInfo = Introspector.getBeanInfo(superclass);
      return new BeanInfo[] { superBeanInfo };
    }
    catch(IntrospectionException ex) {
      ex.printStackTrace();
      return null;
    }
  }
}
