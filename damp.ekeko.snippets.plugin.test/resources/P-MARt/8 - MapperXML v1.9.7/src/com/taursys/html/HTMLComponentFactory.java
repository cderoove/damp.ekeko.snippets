/**
 * HTMLComponentFactory - Factory which creates XMLComponents from an HTML Doc.
 *
 * Copyright (c) 2000
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
package com.taursys.html;

import java.util.*;
import java.text.*;
import org.w3c.dom.*;
import com.taursys.xml.*;
import com.taursys.html.*;
import com.taursys.model.*;
import com.taursys.debug.*;
import com.taursys.dom.*;

/**
 * HTMLComponentFactory is used to automate the creation of Components based on
 * the HTML Document and its Elements. It determines the Component type based
 * on its element tag "type" attribute and "id" attribute. This class
 * initializes the tagTable with suggested components for HTML tags in its
 * constructor by calling the <code>initTagTable</code> method.
 * <p>
 * This class provides a Singleton via the <code>getInstance</code> method. This
 * is recommended over constructing a new instance.
 * <p>
 * This class contains two primary methods:
 * <ul>
 * <li><code>getSuggestedComponents</code> - for use by design tools.</li>
 * <li><code>createComponents</code> - to automatically create and bind
 * <code>Components</code> at runtime.</li>
 * </ul>
 */
public class HTMLComponentFactory extends ComponentFactory {
  private static HTMLComponentFactory factory;

  /**
   * Default constructor for HTMLComponentFactory.
   * @see #getInstance
   */
  public HTMLComponentFactory() {
    super();
  }

  /**
   * Get the singleton instance of the HTMLComponentFactory.
   */
  public static HTMLComponentFactory getInstance() {
    if (factory == null) {
      factory = new HTMLComponentFactory();
    }
    return factory;
  }

  // ***********************************************************************
  // *                       PROTECTED METHODS
  // ***********************************************************************

  /**
   * Initialize the factory's tagTable with suggested components for HTML
   * documents. During the automated Component creation, only the first
   * suggestion is used. The other suggestions are intended for use by design
   * tools. The following are the suggestions created by this class:
   * <table>
   *  <tr>
   *    <td>Element Tag</td>
   *    <td>Type Attribute</td>
   *    <td>Suggested Component(s)</td>
   *  </tr>
   *  <tr>
   *    <td>a</td>
   *    <td>n/a</td>
   *    <td>HTMLAnchorURL</td>
   *  </tr>
   *  <tr>
   *    <td>select</td>
   *    <td>na</td>
   *    <td>HTMLSelect</td>
   *  </tr>
   *  <tr>
   *    <td>span</td>
   *    <td>n/a</td>
   *    <td>TextField</td>
   *  </tr>
   *  <tr>
   *    <td>td</td>
   *    <td>n/a</td>
   *    <td>TextField</td>
   *  </tr>
   *  <tr>
   *    <td>textarea</td>
   *    <td>n/a</td>
   *    <td>HTMLInputText</td>
   *  </tr>
   *  <tr>
   *    <td>input</td>
   *    <td>hidden</td>
   *    <td>HTMLInputText</td>
   *  </tr>
   *  <tr>
   *    <td>input</td>
   *    <td>password</td>
   *    <td>HTMLInputText</td>
   *  </tr>
   *  <tr>
   *    <td>input</td>
   *    <td>submit</td>
   *    <td>Button, Trigger</td>
   *  </tr>
   *  <tr>
   *    <td>input</td>
   *    <td>text</td>
   *    <td>HTMLInputText</td>
   *  </tr>
   *  <tr>
   *    <td>input</td>
   *    <td>checkbox</td>
   *    <td>HTMLCheckBox</td>
   *  </tr>
   * </table>
   * <p>
   */
  protected void initTagTable() {
    Vector suggestions = new Vector();
    suggestions.add(HTMLAnchorURL.class.getName());
    tagTable.put("a", suggestions);

    suggestions = new Vector();
    suggestions.add(HTMLSelect.class.getName());
    tagTable.put("select", suggestions);

    suggestions = new Vector();
    suggestions.add(TextField.class.getName());
    tagTable.put("span", suggestions);

    suggestions = new Vector();
    suggestions.add(TextField.class.getName());
    tagTable.put("td", suggestions);

    suggestions = new Vector();
    suggestions.add(HTMLTextArea.class.getName());
    tagTable.put("textarea", suggestions);

    suggestions = new Vector();
    suggestions.add(HTMLInputText.class.getName());
    tagTable.put("input-hidden", suggestions);

    suggestions = new Vector();
    suggestions.add(HTMLInputText.class.getName());
    tagTable.put("input-password", suggestions);

    suggestions = new Vector();
    suggestions.add(Button.class.getName());
    suggestions.add(Trigger.class.getName());
    tagTable.put("input-submit", suggestions);

    suggestions = new Vector();
    suggestions.add(HTMLInputText.class.getName());
    tagTable.put("input-text", suggestions);

    suggestions = new Vector();
    suggestions.add(HTMLCheckBox.class.getName());
    tagTable.put("input-checkbox", suggestions);
  }

  /**
   * Returns a Vector of suggested Component class names for given Element.
   * This method will choose the appropriate Components based on the type of
   * Element given.  The default Component type will be the first in the list.
   * <p>
   * If the component is an input component, then the TYPE attribute is also
   * used in selecting the right component.
   * <p>
   * If the Element has an ID, then the com.taursys.xml.DocumentElement Component
   * will be added to the end of the suggestion list.
   * <p>
   * If the id contains the TEMPLATE_NODE keyword, then a Template will be added
   * to the top of the suggestion list.
   * <p>
   * If there are no suggested types of Component for the given Element, then
   * an empty Vector will be returned.
   * <p>
   * Subclasses should override this method if more than the Element tag name
   * is needed to determine the suggested components.
   * @param element to return default Component type for
   * @return a Vector containing any suggested Component class names
   */
  public Vector getSuggestedComponents(Element element) {
    String tagName = element.getTagName();
    if (tagName.equals("input"))
      tagName += "-" + element.getAttribute("type");
    String id = element.getAttribute("id");
    return getSuggestedComponents(tagName, id, element);
  }

  /**
   * <p>Create a component for given element and set its properties.
   * Only bound components with id's following a strict id naming convention
   * will be created.
   * </p>
   * <p>The id must begin with a ValueHolder's alias. It must then be followed
   * by a double-underscore ("__"). Next the propertyName must appear or the
   * keyword "TEMPLATE_NODE".  An optional suffix can be added to ensure
   * unique id's (as required by spec). The optional suffix must be separated
   * from the property name by a double-underscore("__"). The following are
   * examples of valid id format:</p>
   * <ul>
   * <li>Person__lastName</li>
   * <li>Person__lastName__2</li>
   * <li>Invoices__TEMPLATE_NODE</li>
   * <li>Invoices__TEMPLATE_NODE__2</li>
   * </ul>
   * <p>The alias of the id (first part), must match an alias of a ValueHolder
   * in the given array of ValueHolders, otherwise no Component will be
   * created. The ValueHolder with a matching alias will be set as the new
   * Component's valueHolder.
   * </p>
   * <p>If the new Component is an AbstractField subclass, then its
   * propertyName will be set to the propertyName of the id (second part). If
   * the given Element has a "name" attribute, the Component's parameter will
   * be set to the value of the "name" attribute.
   * <p>If the new Component is a Template (or subclass), then only its
   * collectionValueHolder property will be set. The associated ValueHolder
   * for this Component must be a CollectionValueHolder, otherwise no
   * Component will be created.
   * </p>
   * <p>This method will also attempt to setup the formatting properties
   * for the new Component. This only applies to AbstractField subclasses.
   * The format is extracted from the document within the element's "value"
   * attribute, "href" attribute, or text node. The format must be specified
   * as TYPE:pattern, where TYPE is one of: DATE NUMBER or MSG. The pattern
   * should be a valid pattern for the format type. The following are examples
   * of use:
   * <ul>
   * <li>&lt;span id="Person__birthdate"&gt;DATE:MM/dd/yyyy&lt;/span&gt;</li>
   * <li>&lt;input type="text" id="InvoiceItem__unitPrice" value="NUMBER:###,##0.00" /&gt;</li>
   * <li>&lt;a id="Person__personID" href="MSG:/PersonProfile.mxform?personID={0}"&gt;Link to Person&lt;/a&gt;</li>
   * </ul>
   * </p>
   * @param id of Element to create component for
   * @param element to create component for
   * @param holders the array of ValueHolders for binding
   * @return new Component with properties set or null
   */
  protected Component createComponentForElement(String id, Element element,
      ValueHolder[] holders) {
    if (id == null)
      throw new IllegalArgumentException(
          "Null id passed to createComponentForElement");

    // Extract holder alias
    int pos = id.indexOf(ID_DELIMITER);
    if (pos < 1)
      return null;
    String alias = id.substring(0, pos);

    // Extract property name (remove suffix)
    pos += 2;
    if (id.length() <= pos)
      return null;
    String propertyName = id.substring(pos);
    pos = propertyName.indexOf(ID_DELIMITER);
    if (pos != -1)
      propertyName = propertyName.substring(0, pos);

    // Find holder for alias
    ValueHolder holder = null;
    for (int i = 0; i < holders.length; i++) {
      holder = holders[i];
      if (alias.equals(holder.getAlias()))
        break;
    }
    if (holder == null)
      return null;

    // Create appropriate component
    Vector suggestions = getSuggestedComponents(element);
    if (suggestions.size() == 0)
      return null;
    Component component = null;
    try {
      component = (Component)
          Class.forName((String)suggestions.get(0)).newInstance();
    } catch (Exception ex) {
      Debug.error("Error during create component: " + ex.getMessage(), ex);
      return null;
    }

    // Set properties as appropriate:
    // id, valueHolder, propertyName, parameter, format, formatPattern)
    if (component instanceof AbstractField) {
      ((AbstractField)component).setId(id);
      ((AbstractField)component).setValueHolder(holder);
      ((AbstractField)component).setPropertyName(propertyName);
      String parameter = element.getAttribute("name");
      if (parameter != null && parameter.length() == 0)
        parameter = null;
      ((AbstractField)component).setParameter(parameter);
      setupFormat((AbstractField)component, element);
      return component;
    } else if (component instanceof Template) {
      ((Template)component).setId(id);
      if (holder instanceof CollectionValueHolder) {
        ((Template)component).setCollectionValueHolder((CollectionValueHolder)holder);
        return component;
      } else {
        return null;
      }
    } else {
      return null;
    }
  }

  private void setupFormat(AbstractField field, Element element) {
    // Try to get format from value attribute or text node
    String formatInfo = element.getAttribute("value");
    if (formatInfo == null || formatInfo.length() == 0)
      formatInfo = element.getAttribute("href");
    if (formatInfo == null || formatInfo.length() == 0)
      formatInfo = DOM_1_20000929_DocumentAdapter.getElementText(element);
    // Determine what kind of format this is
    if (formatInfo != null && formatInfo.length() > 0) {
      if (formatInfo.startsWith("DATE:")) {
        field.setFormat(new java.text.SimpleDateFormat());
        field.setFormatPattern(formatInfo.substring(5));
      } else if (formatInfo.startsWith("NUMBER:")) {
        field.setFormat(new DecimalFormat());
        field.setFormatPattern(formatInfo.substring(7));
      } else if (formatInfo.startsWith("MSG:")) {
        field.setFormat(new MessageFormat(""));
        field.setFormatPattern(formatInfo.substring(4));
      }
    }
  }
}
