package com.taursys.model;

import java.text.Format;
import java.text.MessageFormat;
import com.taursys.util.DataTypes;
import com.taursys.util.UnsupportedDataTypeException;
import com.taursys.util.UnsupportedConversionException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.IllegalAccessException;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;
import com.taursys.util.DataTypes;
import java.util.StringTokenizer;

/**
 * Sets the name of the property in the valueObject to use.  This is the
 * property that get/setObject and get/setText methods will access.  The
 * property name should follow JavaBean conventions.
 * <p>
 * Example: propertyName="color": then it expects getColor and setColor
 * will be the method names in the valueObject.
 * <p>
 * NOTE: Setting the propertyName will only have effect BEFORE the
 * setAccessorMethods process is invoked.  Once the accessor methods have
 * been set, subsequent changes to this property will have no effect.
 */
public class PropertyAdapter {
  private String propertyName;
  private int javaDataType = DataTypes.TYPE_UNDEFINED;
  private java.lang.reflect.Method writeMethod;
  private java.lang.reflect.Method[] readMethods;

  public PropertyAdapter(Class valueObjectClass, String propertyName) throws IntrospectionException,
      InvocationTargetException, IllegalAccessException {
    setAccessorMethods(valueObjectClass, propertyName);
  }

  // ***********************************************************************
  // *                     INITIALIZATION METHODS
  // ***********************************************************************

  /**
   * Sets the javaDataType, readMethods and writeMethod properties.
   * The valueObject and propertyName must be valid before invoking this
   * method, otherwise an IntrospectionException will be thrown.
   * The readMethods or writeMethod may be null if the valueObject does
   * not have a cooresponding get or set method. This method uses reflection
   * and draws information from the valueObject's BeanInfo and PropertyDescriptors.
   * <p>
   * This method also sets a flag to indicate that the valueObject has been
   * introspected and the accessor methods are set.
   */
  private void setAccessorMethods(Class c, String propertyName) throws
      IntrospectionException, InvocationTargetException, IllegalAccessException {
    if (c == null)
      throw new IntrospectionException("Value Object class is null");
    if (propertyName == null || propertyName.length() == 0)
      throw new IntrospectionException("propertyName is null or blank");
    this.propertyName = propertyName;
    StringTokenizer tokenizer = new StringTokenizer(propertyName, ".");
    int count = tokenizer.countTokens() ;
    readMethods = new Method[count];
    PropertyDescriptor prop = null;
    for (int i = 0; i < count ; i++) {
      String propName = tokenizer.nextToken();
      prop = getProperty(c, propName);
      readMethods[i] = prop.getReadMethod();
      c = prop.getPropertyType();
    }
    javaDataType = DataTypes.getDataType(c.getName());
    writeMethod = prop.getWriteMethod();
  }

  /**
   * Searches the given class for the given property name and returns the PropertyDescriptor.
   * Throws IntrospectionException if property is not found.
   */
  protected PropertyDescriptor getProperty(Class c, String propName)
      throws IntrospectionException {
    BeanInfo info = Introspector.getBeanInfo(c);
    PropertyDescriptor[] props = info.getPropertyDescriptors();
    for( int i=0 ; i < props.length ; i++ ) {
      if (props[i].getName().equals(propName)) {
        return props[i];
      }
    }
    throw new IntrospectionException("Property " + propName
        + " is not found in "
        + c.getName());
  }

  // ***********************************************************************
  // *               TEXT PARSE/FORMAT ROUTINES
  // ***********************************************************************

  /**
   * Returns the accessed Object as a String value using Format if defined.
   * If the accessed Object is null, it will return an empty String ("").
   */
  public String getText(Object valueObject, Format format)
      throws IntrospectionException, InvocationTargetException,
      IllegalAccessException {
    Object value = getPropertyValue(valueObject);
    if (value == null)
      return "";
    if (format == null)
      return DataTypes.format(javaDataType, value);
    else if (format instanceof MessageFormat)
      return ((MessageFormat)format).format(new Object[] {value});
    else
      return format.format(value);
  }

  /**
   * Sets the accessed Object from the given String value to DataType using Format(if defined) to parse.
   * If the given String value is null or empty (""), the accessed Object is
   * set to null.
   */
  public void setText(Object target, String value, Format format)
      throws IntrospectionException, InvocationTargetException,
      IllegalAccessException, ParseException, UnsupportedDataTypeException,
      UnsupportedConversionException {
    if (value == null || value.length() == 0)
      setPropertyValue(target, null);
    else if (format == null)
      setPropertyValue(target, DataTypes.parse(javaDataType, value));
    else if (format instanceof MessageFormat) {
      Object[] values = ((MessageFormat)format).parse(value);
      setPropertyValue(target, DataTypes.convert(javaDataType, values[0]));
    } else
      setPropertyValue(target, DataTypes.convert(javaDataType, format.parseObject(value)));
  }

  // ***********************************************************************
  // *               PROPERTY VALUE ACCESSOR METHODS
  // ***********************************************************************

  /**
   * Returns a property value from the valueObject based on the propertyName.
   * The valueObject and propertyName properties must be defined before
   * you invoke this method, and the valueObject must have a getter for
   * the propertyName, otherwise IntrospectionException will be raised.
   * This method will invoke the setAccessorMethods method if it has not
   * yet been invoked.
   */
  public Object getPropertyValue(Object valueObject)
      throws IntrospectionException, InvocationTargetException,
      IllegalAccessException {
    Object obj = valueObject;
    for (int i = 0; obj != null && i < readMethods.length; i++) {
      if (readMethods[i] == null)
        throw new IntrospectionException("Property " + propertyName
            + " has no get method defined in "
            + obj.getClass().getName());
      obj = readMethods[i].invoke(obj, new Object[] {});
    }
    return obj;
  }

  /**
   * Sets a property of the valueObject to the given value based on the propertyName.
   * The valueObject and propertyName properties must be defined before
   * you invoke this method, and the valueObject must have a setter for
   * the propertyName, otherwise IntrospectionException will be raised.
   * This method will invoke the setAccessorMethods method if it has not
   * yet been invoked.
   */
  public void setPropertyValue(Object valueObject, Object value)
      throws IntrospectionException, InvocationTargetException,
      IllegalAccessException {
    if(valueObject == null)
      throw new IntrospectionException("Value object is null");
    if(writeMethod == null)
      throw new IntrospectionException("Property " + propertyName
          + " has no set method defined in "
          + valueObject.getClass().getName());
    Object target = valueObject;
    int count = readMethods.length -1;
    for (int i = 0; target != null && i < count ; i++) {
      if (readMethods[i] == null)
        throw new IntrospectionException("Property " + propertyName
            + " has no get method defined in "
            + target.getClass().getName());
      target = readMethods[i].invoke(target, new Object[] {});
    }
    writeMethod.invoke(target, new Object[] {value});
  }

  // ***********************************************************************
  // *               PROPERTY ACCESSOR METHODS
  // ***********************************************************************

  /**
   * Returns the name of the property in the valueObject to use.  This is the
   * property that get/setObject and get/setText methods will access.  The
   * property name should follow JavaBean conventions.
   * <p>
   * Example: propertyName="color": then it expects getColor and setColor
   * will be the method names in the valueObject.
   */
  public String getPropertyName() {
    return propertyName;
  }

  /**
   * Returns the Methods used to get/read the target property.
   * This value is set by setAccessorMethods which is invoked
   * automatically by the set/getObject or setText methods.
   */
  protected java.lang.reflect.Method[] getReadMethods() {
    return readMethods;
  }

  /**
   * Returns the Method used to get/read the target property.
   * This value is set by setAccessorMethods which is invoked
   * automatically by the set/getObject or setText methods.
   */
  protected java.lang.reflect.Method getWriteMethod() {
    return writeMethod;
  }
}
