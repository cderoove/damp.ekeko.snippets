package net.sourceforge.pmd.util.viewer.model;

import net.sourceforge.pmd.jaxen.Attribute;


/**
 * A toolkit for vaious attribute translations
 *
 * @author Boris Gruschko ( boris at gruschko.org )
 * @version $Id: AttributeToolkit.java,v 1.1 2004/07/14 16:37:13 ngjanice Exp $
 */
public class AttributeToolkit
{
  /**
   * formats a value for it's usage in XPath expressions
   *
   * @param attribute atribute which value should be formatted
   *
   * @return formmated value
   */
  public static String formatValueForXPath( Attribute attribute )
  {
    return "'" + attribute.getValue(  ) + "'";
  }

  /**
   * constructs a predicate from the given attribute
   *
   * @param attribute attribute to be formatted as predicate
   *
   * @return predicate
   */
  public static String constructPredicate( Attribute attribute )
  {
    return "[@" + attribute.getName(  ) + "=" +
    formatValueForXPath( attribute ) + "]";
  }
}


/*
 * $Log: AttributeToolkit.java,v $
 * Revision 1.1  2004/07/14 16:37:13  ngjanice
 * 14 juillet 2004 - 12h32
 *
 * Revision 1.1  2003/09/23 20:32:42  tomcopeland
 * Added Boris Gruschko's new AST/XPath viewer
 *
 * Revision 1.1  2003/09/24 01:33:03  bgr
 * moved to a new package
 *
 * Revision 1.1  2003/09/23 07:52:16  bgr
 * menus added
 *
 */
