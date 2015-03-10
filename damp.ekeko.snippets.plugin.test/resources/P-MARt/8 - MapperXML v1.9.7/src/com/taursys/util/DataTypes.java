/**
 * DataTypes - Supported Java Data Types and static utility methods
 *
 * Copyright (c) 2002
 *      Marty Phelan, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package com.taursys.util;

import java.util.Hashtable;
import java.sql.Types;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;

/**
 * This class provides definition, conversion, and simple parsing for known data types.
 */
public class DataTypes {
  public static final int TYPE_UNDEFINED = -1;
  public static final int TYPE_STRING = 0;
  public static final int TYPE_BIGDECIMAL = 1;
  public static final int TYPE_TIMESTAMP = 2;
  public static final int TYPE_BOOLEAN = 3;
  public static final int TYPE_INT = 4;
  public static final int TYPE_BYTE = 5;
  public static final int TYPE_SHORT = 6;
  public static final int TYPE_LONG = 7;
  public static final int TYPE_FLOAT = 8;
  public static final int TYPE_DATE = 9;
  public static final int TYPE_TIME = 10;
  public static final int TYPE_DOUBLE = 11;
  public static final int TYPE_SQL_DATE = 12;

  private static DateFormat df = DateFormat.getDateTimeInstance(
      DateFormat.LONG, DateFormat.LONG);

  /**
   * Get the default DataFormat used by the parse/format routines.
   * By default it is initialized to DateFormat.getDateTimeInstance(
   * DateFormat.LONG, DateFormat.LONG).
   * @return the default DataFormat used by the parse/format routines
   */
  public static DateFormat getDefaultDateFormat() {
    return df;
  }

  /**
   * Set the default DataFormat used by the parse/format routines.
   * By default it is initialized to DateFormat.getDateTimeInstance(
   * DateFormat.LONG, DateFormat.LONG).
   * @param dfNew the default DataFormat used by the parse/format routines
   */
  public static void setDefaultDateFormat(DateFormat dfNew) {
    if (dfNew == null)
      throw new IllegalArgumentException("Default Date Format cannot be set to null");
    df = dfNew;
  }

  private static Hashtable javaTypeNames = new Hashtable();

  private static final Class[] CLASSES_FOR_TYPES = {
    String.class,
    BigDecimal.class,
    java.sql.Timestamp.class,
    Boolean.class,
    Integer.class,
    Byte.class,
    Short.class,
    Long.class,
    Float.class,
    java.util.Date.class,
    java.sql.Time.class,
    Double.class,
    java.sql.Date.class,
    };

  private static final String[] JAVA_NAMES_FOR_TYPES = {
    "java.lang.String",
    "java.math.BigDecimal",
    "java.sql.Timestamp",
    "java.lang.Boolean",
    "java.lang.Integer",
    "java.lang.Byte",
    "java.lang.Short",
    "java.lang.Long",
    "java.lang.Float",
    "java.util.Date",
    "java.sql.Time",
    "java.lang.Double",
    "java.sql.Date",
  };

  static {
    javaTypeNames.put("java.lang.String", new Integer(TYPE_STRING));
    javaTypeNames.put("java.math.BigDecimal", new Integer(TYPE_BIGDECIMAL));
    javaTypeNames.put("java.sql.Timestamp", new Integer(TYPE_TIMESTAMP));
    javaTypeNames.put("java.lang.Boolean", new Integer(TYPE_BOOLEAN));
    javaTypeNames.put("java.lang.Integer", new Integer(TYPE_INT));
    javaTypeNames.put("java.lang.Byte", new Integer(TYPE_BYTE));
    javaTypeNames.put("java.lang.Short", new Integer(TYPE_SHORT));
    javaTypeNames.put("java.lang.Long", new Integer(TYPE_LONG));
    javaTypeNames.put("java.lang.Float", new Integer(TYPE_FLOAT));
    javaTypeNames.put("java.util.Date", new Integer(TYPE_DATE));
    javaTypeNames.put("java.sql.Time", new Integer(TYPE_TIME));
    javaTypeNames.put("java.lang.Double", new Integer(TYPE_DOUBLE));
    javaTypeNames.put("java.sql.Date", new Integer(TYPE_SQL_DATE));
    // Special names for primatives
    javaTypeNames.put("int", new Integer(TYPE_INT));
    javaTypeNames.put("boolean", new Integer(TYPE_BOOLEAN));
    javaTypeNames.put("char", new Integer(TYPE_STRING));
    javaTypeNames.put("byte", new Integer(TYPE_BYTE));
    javaTypeNames.put("short", new Integer(TYPE_SHORT));
    javaTypeNames.put("long", new Integer(TYPE_LONG));
    javaTypeNames.put("float", new Integer(TYPE_FLOAT));
    javaTypeNames.put("double", new Integer(TYPE_DOUBLE));
  }

  /**
   * Private constructor - this class is not intended to be instantiated
   */
  private DataTypes() {
  }

  /**
   * Returns the class name for the given Java data type.
   * @param javaDataType constant (see DataTypes.TYPE_XXX)
   * @return Class name for the given Java data type
   * @throws UnsupportedDataTypeException if the given value is not a known type.
   */
  public static String getJavaNameForType(int javaDatType) throws UnsupportedDataTypeException {
    checkJavaDataType(javaDatType);
    return JAVA_NAMES_FOR_TYPES[javaDatType];
  }

  /**
   * Returns the Class for the given Java data type.
   * @param javaDataType constant (see DataTypes.TYPE_XXX)
   * @return Class for the given Java data type
   * @throws UnsupportedDataTypeException if the given value is not a known type.
   */
  public static Class getClassForType(int javaDatType) throws UnsupportedDataTypeException {
    checkJavaDataType(javaDatType);
    return CLASSES_FOR_TYPES[javaDatType];
  }

  /**
   * Checks given index to ensure it is valid type otherwise throws UnsupportedDataTypeException.
   */
  public static void checkJavaDataType(int i) throws UnsupportedDataTypeException {
    if (i < 0 || i >= JAVA_NAMES_FOR_TYPES.length)
      throw new UnsupportedDataTypeException();
  }

  /**
   * Returns an int id for the given data type or TYPE_UNDEFINED if unknown.
   * @return an int id for the given data type or TYPE_UNDEFINED if unknown
   */
  public static int getDataType(String className) {
    Integer integer = (Integer)javaTypeNames.get(className);
    if (integer != null)
      return integer.intValue();
    else
      return TYPE_UNDEFINED;
  }

  /**
   * Returns an object of the type indicated by javaDataType set from the
   * value of the given object.  This effectively converts the given
   * object to another type of object.  There must be a reasonable
   * expectation of conversion, otherwise an Exception
   * ("Unsupported Data Type Conversion") will be thrown.
   * A change of types can also cause rounding or truncation.
   */
  private static Object convert(int javaDataType, Number value)
      throws UnsupportedConversionException, UnsupportedDataTypeException {
    checkJavaDataType(javaDataType);
    switch (javaDataType) {
      case TYPE_BIGDECIMAL:
        return new BigDecimal(((Number)value).toString());
      case TYPE_BYTE:
        return new Byte(((Number)value).byteValue());
      case TYPE_DOUBLE:
        return new Double(((Number)value).doubleValue());
      case TYPE_FLOAT:
        return new Float(((Number)value).floatValue());
      case TYPE_INT:
        return new Integer(((Number)value).intValue());
      case TYPE_LONG:
        return new Long(((Number)value).longValue());
      case TYPE_SHORT:
        return new Short(((Number)value).shortValue());
      default:
        throw new UnsupportedConversionException();
    }
  }

  /**
   * Returns an object of the type indicated by javaDataType set from the
   * value of the given object.  This effectively converts the given
   * object to another type of object.  There must be a reasonable
   * expectation of conversion, otherwise an Exception
   * ("Unsupported Data Type Conversion") will be thrown.
   * A change of types can also cause rounding or truncation.
   */
  private static Object convert(int javaDataType, java.util.Date value)
      throws UnsupportedConversionException {
    checkJavaDataType(javaDataType);
    switch (javaDataType) {
      case TYPE_DATE:
        return value;
      case TYPE_TIME:
        return new java.sql.Time(((java.util.Date)value).getTime());
      case TYPE_TIMESTAMP:
        return new java.sql.Timestamp(((java.util.Date)value).getTime());
      case TYPE_SQL_DATE:
        return new java.sql.Date(((java.util.Date)value).getTime());
      default:
        throw new UnsupportedConversionException();
    }
  }

  /**
   * Returns an object of the type indicated by javaDataType set from the
   * value of the given object.  This effectively converts the given
   * object to another type of object.  There must be a reasonable
   * expectation of conversion, otherwise an Exception
   * ("Unsupported Data Type Conversion") will be thrown.
   * A change of types can also cause rounding or truncation.
   */
  private static Object convert(int javaDataType, String value)
      throws UnsupportedConversionException, UnsupportedDataTypeException {
    checkJavaDataType(javaDataType);
    if (javaDataType == TYPE_STRING)
      return value;
    else
      throw new UnsupportedConversionException();
  }

  /**
   * Returns an object of the type indicated by javaDataType set from the
   * value of the given object.  This effectively converts the given
   * object to another type of object.  There must be a reasonable
   * expectation of conversion, otherwise an Exception
   * ("Unsupported Data Type Conversion") will be thrown.
   * A change of types can also cause rounding or truncation.
   */
  public static Object convert(int javaDataType, Object value)
      throws UnsupportedConversionException, UnsupportedDataTypeException {
    if (value instanceof String)
      return convert(javaDataType, (String)value);
    else if (value instanceof java.util.Date)
      return convert(javaDataType, (java.util.Date)value);
    else if (value instanceof Number)
      return convert(javaDataType, (Number)value);
    else
      throw new UnsupportedConversionException();
  }

  /**
   * Returns a new object of type indicated by javaDataType with parsed given value.
   * @param javaDataType indicates data type of value (see TYPE_xxx constants).
   * @param String value to parse.
   * @return Object representing parsed value of given String.
   * @throws UnsupportedDataTypeException if unknown javaDataType given.
   * @throws ParseException if given value cannot be parsed.
   */
  public static Object parse(int javaDataType, String value)
      throws ParseException, UnsupportedDataTypeException {
    switch (javaDataType) {
      case TYPE_STRING:
        return value;
      case TYPE_BOOLEAN:
        return new Boolean(value);
      case TYPE_DATE:
        return df.parse(value);
      case TYPE_SQL_DATE:
        return new java.sql.Date(df.parse(value).getTime());
      case TYPE_TIME:
        return new java.sql.Time(df.parse(value).getTime());
      case TYPE_TIMESTAMP:
        return new java.sql.Timestamp(df.parse(value).getTime());
      case TYPE_BIGDECIMAL:
        return new BigDecimal(value);
      case TYPE_BYTE:
        return new Byte(value);
      case TYPE_DOUBLE:
        return new Double(value);
      case TYPE_FLOAT:
        return new Float(value);
      case TYPE_INT:
        return new Integer(value);
      case TYPE_LONG:
        return new Long(value);
      case TYPE_SHORT:
        return new Short(value);
      default:
        throw new UnsupportedDataTypeException();
    }
  }

  /**
   * Returns a default formatted String for the given object.
   * If the type is <code>TYPE_UNDEFINED</code>, the <code>toString()</code>
   * method is used.
   * @param javaDataType indicates data type of value (see TYPE_xxx constants).
   * @param value is the object to format as a String
   * @return formatted String using default format for Object type.
   * @throws UnsupportedDataTypeException if unknown javaDataType given.
   */
  public static String format(int javaDataType, Object value)
      throws UnsupportedDataTypeException {
    switch (javaDataType) {
      case TYPE_STRING:
        return value.toString();
      case TYPE_BOOLEAN:
        return value.toString();
      case TYPE_DATE:
        return df.format(value);
      case TYPE_SQL_DATE:
        return df.format(value);
      case TYPE_TIME:
        return df.format(value);
      case TYPE_TIMESTAMP:
        return df.format(value);
      case TYPE_BIGDECIMAL:
        return value.toString();
      case TYPE_BYTE:
        return value.toString();
      case TYPE_DOUBLE:
        return value.toString();
      case TYPE_FLOAT:
        return value.toString();
      case TYPE_INT:
        return value.toString();
      case TYPE_LONG:
        return value.toString();
      case TYPE_SHORT:
        return value.toString();
      case TYPE_UNDEFINED:
        return value.toString();
      default:
        throw new UnsupportedDataTypeException();
    }
  }
}
