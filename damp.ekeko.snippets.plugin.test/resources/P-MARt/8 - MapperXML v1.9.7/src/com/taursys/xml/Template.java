/**
 * Template - A Component which replicates itself and its children for each object in its model.
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
package com.taursys.xml;

import com.taursys.xml.event.*;
import com.taursys.xml.render.TemplateRenderer;
import com.taursys.xml.render.DocumentElementRenderer;
import com.taursys.model.CollectionValueHolder;

/**
 * <p>A <code>Template</code> is a component used to display and/or input multiple
 * values. It acts as a <code>Container</code> for other components which
 * provide the actual display or input. A typical application for the
 * <code>Template</code> is to display or edit a table of data. The
 * <code>Template</code> is bound to a single tag in the XML document. This
 * tag in turn contains other tags that make up the "template" row. The
 * <code>Template</code> is also bound to a CollectionValueHolder which
 * contains the values. When rendered, the Template will replicate itself
 * (and its child tags) in the XML document for each value in the holder.
 * </p>
 * <p>The following is an example illustrates a <code>Template</code> used to
 * display a list of names (from a collection of Person objects). A portion
 * of the HTML is listed below along with a portion of the java code. The
 * initForm and openForm methods have been ommitted. The
 * <code>HTMLComponentFactory</code> was not used in this example so that
 * the actual components could be illustrated.
 * </p>
 * <pre>
 *  &lt;h2&gt;List presented in a table&lt;/h2&gt;
 *  &lt;table border="1"&gt;
 *    &lt;tr&gt;
 *      &lt;td &gt;First Name&lt;/td&gt;
 *      &lt;td &gt;Last Name&lt;/td&gt;
 *    &lt;/tr&gt;
 *    &lt;tr id="People__TEMPLATE_NODE"&gt;
 *      &lt;td id="People__firstName"&gt;John&lt;/td&gt;
 *      &lt;td id="People__lastName"&gt;Smith&lt;/td&gt;
 *    &lt;/tr&gt;
 *  &lt;/table&gt;
 *
 * -----------------------------------------------------------------------
 *
 * public class TemplatePage extends ServletForm {
 *   VOCollectionValueHolder people = new VOCollectionValueHolder();
 *   // ===================================================================
 *   // The following code can be ommitted if the component factory is used
 *   // ===================================================================
 *   TextField firstName = new TextField();
 *   TextField lastName = new TextField();
 *   Template report = new Template();
 *   // ===================================================================
 *
 *   public TemplatePage() {
 *     try {
 *       jbInit();
 *     } catch(Exception e) {
 *       e.printStackTrace();
 *     }
 *   }
 *
 *   private void jbInit() throws Exception {
 *     people.setAlias("People");
 *     people.setValueObjectClass(Person.class);
 *     // ===================================================================
 *     // The following code can be ommitted if the component factory is used
 *     // ===================================================================
 *     firstName.setPropertyName(Person.FIRST_NAME);
 *     firstName.setValueHolder(people);
 *     firstName.setId("People__firstName");
 *     lastName.setPropertyName(Person.LAST_NAME);
 *     lastName.setValueHolder(people);
 *     lastName.setId("People__lastName");
 *     report.setId("report");
 *     report.setCollectionValueHolder(people);
 *     report.add(firstName);
 *     report.add(lastName);
 *     this.add(report);
 *     // ===================================================================
 *   }
 * </pre>
 *
 * <p>A <code>Template</code> can also be used to input multiple values. The code
 * is essentially the same, except that you will use input type components
 * in the HTML and Java code. The order and count of the values in the request
 * must correspond to the order and count in the holder. The BusinessDelegate
 * that supplies the values should ensure this.
 * </p>
 * <p>The following example illustrates a form to edit inventory items. A portion
 * of the HTML is listed below along with a portion of the java code. The
 * openForm methods has been ommitted. This example uses the HTMLComponent
 * factory to create the actual components at runtime.
 * </p>
 * <pre>
 *
 * &lt;h2&gt;List of Inventory Items to Edit&lt;/h2&gt;
 *   &lt;form method="post" action="InventoryEditPage.sf"&gt;
 *     &lt;table border="1" width="100%"&gt;
 *       &lt;tr bgcolor="orange"&gt;
 *         &lt;td&gt;Quantity&lt;/td&gt;
 *         &lt;td&gt;Product ID&lt;/td&gt;
 *         &lt;td&gt;Unit Price&lt;/td&gt;
 *         &lt;td&gt;Extension&lt;/td&gt;
 *       &lt;/tr&gt;
 *       &lt;tr id="Inventory__TEMPLATE_NODE"&gt;
 *         &lt;td&gt;
 *           &lt;input type="text" id="Inventory__quantity" name="quantity" value="1"/&gt;
 *         &lt;/td&gt;
 *         &lt;td id="Inventory__productID"&gt;XXXX&lt;/td&gt;
 *         &lt;td&gt;
 *           &lt;input type="text" id="Inventory__unitPrice" name="unitPrice" value="0.00"/&gt;
 *         &lt;/td&gt;
 *         &lt;td id="Inventory__extension"&gt;0.00&lt;/td&gt;
 *       &lt;/tr&gt;
 *     &lt;/table&gt;
 *     &lt;br/&gt;
 *     &lt;input type="submit" name="action" value="Save"/&gt;
 *   &lt;/form&gt;
 *
 * -----------------------------------------------------------------------
 *
 * public class InventoryEditPage extends ServletForm {
 *   private VOListValueHolder inventoryHolder = new VOListValueHolder();
 *
 *   public InventoryEditPage() {
 *     try {
 *       jbInit();
 *     } catch(Exception e) {
 *       e.printStackTrace();
 *     }
 *   }
 *
 *   private void jbInit() throws Exception {
 *     inventoryHolder.setAlias("Inventory");
 *     inventoryHolder.setValueObjectClass(InvoiceItemVO.class);
 *     this.add(warehouseField);
 *   }
 *
 *   protected void initForm() throws java.lang.Exception {
 *     super.initForm();
 *     DOMParser parser = new DOMParser();
 *     InputSource is = new InputSource(
 *         getClass().getResourceAsStream("InventoryEditPage.html"));
 *     parser.parse(is);
 *     this.setDocument(parser.getDocument());
 *     // Use HTMLComponentFactory to create components
 *     HTMLComponentFactory.getInstance().createComponents(this,
 *       new ValueHolder[] {inventoryHolder});
 *   }
 * </pre>
 * <p>Technical Information:
 * </p>
 * <p>The <code>Template</code> uses a <code>TemplateRenderer</code> to do the
 * actual rendering.  Typically, this involves dispatching a
 * <code>RenderEvent</code> to its children, then cloning itself for each item
 * in its <code>collectionValueHolder</code>.
 * </p>
 * <p>The <code>Template</code> creates specialized dispatchers for Input and
 * Trigger events. These dispatchers iterate over each item in the collection
 * (in the order of the iterator), increment the parameter index and dispatch
 * input to child components.
 * </p>
 */
public class Template extends DocumentElement {
  private CollectionValueHolder collectionValueHolder;

  /**
   * Constructs a new template
   */
  public Template() {
  }

  // =======================================================================
  //                          Create Dispatchers
  // =======================================================================

  /**
   * Create the InputDispatcher for this Container.
   * @return the InputDispatcher for this Container.
   */
  protected InputDispatcher createInputDispatcher() {
    return new TemplateInputDispatcher(this);
  }

  /**
   * Create the TriggerDispatcher for this Container.
   * @return the TriggerDispatcher for this Container.
   */
  protected TriggerDispatcher createTriggerDispatcher() {
    return new TemplateTriggerDispatcher(this);
  }

  // =======================================================================
  //                          Create Renderer
  // =======================================================================

  /**
   * Creates the default Renderer for this component.
   * By Default this methos returns a new TemplateRenderer.
   * Override this method to define your own TemplateRenderer.
   */
  protected DocumentElementRenderer createDefaultRenderer() {
    return new TemplateRenderer(this);
  }

  /**
   * Set the CollectionValueHolder that this template will iterate for rendering.
   * @param holder the CollectionValueHolder that this template will iterate for
   * rendering.
   */
  public void setCollectionValueHolder(CollectionValueHolder holder) {
    collectionValueHolder = holder;
  }

  // =======================================================================
  //                          Property Accessors
  // =======================================================================

  /**
   * Get the CollectionValueHolder that this template will iterate for rendering.
   * @return the CollectionValueHolder that this template will iterate for
   * rendering.
   */
  public CollectionValueHolder getCollectionValueHolder() {
    return collectionValueHolder;
  }
}
