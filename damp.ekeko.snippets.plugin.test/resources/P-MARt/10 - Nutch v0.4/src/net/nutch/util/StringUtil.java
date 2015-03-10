/* Copyright (c) 2003 The Nutch Organization.  All rights reserved.   */
/* Use subject to the conditions in http://www.nutch.org/LICENSE.txt. */

package net.nutch.util;

/**
 * A collection of String processing utility methods. 
 */
public class StringUtil {

  /**
   * Returns a copy of <code>s</code> padded with trailing spaces so
   * that it's length is <code>length</code>.  Strings already
   * <code>length</code> characters long or longer are not altered.
   */
  public static String rightPad(String s, int length) {
    StringBuffer sb= new StringBuffer(s);
    for (int i= length - s.length(); i > 0; i--) 
      sb.append(" ");
    return sb.toString();
  }

  /**
   * Returns a copy of <code>s</code> padded with leading spaces so
   * that it's length is <code>length</code>.  Strings already
   * <code>length</code> characters long or longer are not altered.
   */
  public static String leftPad(String s, int length) {
    StringBuffer sb= new StringBuffer();
    for (int i= length - s.length(); i > 0; i--) 
      sb.append(" ");
    sb.append(s);
    return sb.toString();
  }
  

}
