/**
 * StringUtil - various additional String methods
 *
 * Copyright (c) 2000, 2001, 2002
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

import java.util.*;

/**
 * Various utility string methods.
 */
public class StringUtil {

  /**
   * Utility method to compare two strings (either of which can be null) and indicate if they differ.
   * It can include null strings in the comparison.  It returns true if both
   * strings are null or both strings match.
   */
  public static boolean differ(String x, String y) {
    if (x == null && y == null)
      return false;
    if (x == null && y != null)
      return true;
    if (x != null && y == null)
      return true;
    return !x.equals(y);
  }

  /**
   * Returns null if given String is blank or empty(length=0) else returns given String.
   */
  public static String nullIfBlank(String s) {
    if (s==null || s.length()==0)
      return null;
    else
      return s;
  }

  /**
   * Returns the base class name without the package name for the given class name.
   */
  public static String baseClassName(String clsName) {
    int idx = clsName.lastIndexOf('.');
    if (idx > 0)
      return clsName.substring(idx+1);
    else
      return clsName;
  }

  /**
   * Returns the package name for the given class name.
   */
  public static String packageName(String clsName) {
    int idx = clsName.lastIndexOf('.');
    if (idx > 0)
      return clsName.substring(0, idx);
    else
      return null;
  }

  /**
   * Returns a class variable name based on the given class name.
   */
  public static String classVarName(String clsName) {
    String baseName = baseClassName(clsName);
    return baseName.substring(0,1).toLowerCase() + baseName.substring(1);
  }

  /**
   * Returns a String with all the toString values(comma separated) from the given array.
   */
  public static String arrayToString(Object[] array) {
    if (array == null)
      return "null";
    if (array.length == 0)
      return "";
    String s = array[0].toString();
    for (int i = 1 ; i < array.length ; i++)
      s += ", " + array[i];
    return s;
  }

  /**
   * Returns an String array from the given String of comma separated names.
   * The size of the returned array is the number of items in the given String
   * plus the indicated number of extra space.
   */
  public static String[] stringToArray(String s, int extraSpace) {
    StringTokenizer tokens = new StringTokenizer(s, ",");
    int count = tokens.countTokens();
    String[] result = new String[count+extraSpace];
    for (int i=0 ; i < count ; i++ ) {
      result[i] = tokens.nextToken().trim();
    }
    return result;
  }

  /**
   * Takes a string of comma separated names and returns as an array of strings.
   */
  public static String[] stringToArray(String s) {
    return stringToArray(s, 0);
  }

  /**
   * Returns true if the given searchString is found in the targetString.
   */
  public static boolean contains(String searchString, String targetString) {
    return targetString.indexOf(searchString) != -1;
  }
}
