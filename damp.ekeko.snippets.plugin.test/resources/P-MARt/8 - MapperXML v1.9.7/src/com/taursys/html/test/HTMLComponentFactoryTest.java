package com.taursys.html.test;

import junit.framework.TestCase;
import com.taursys.html.*;
import com.taursys.util.*;
import com.taursys.xml.*;
import com.taursys.model.*;
import com.taursys.model.test.*;
import com.taursys.dom.*;
import org.w3c.dom.*;
import java.util.*;
import org.xml.sax.InputSource;
import org.apache.xerces.parsers.DOMParser;


/* JUnitTest case for class: com.taursys.html.HTMLComponentFactory */
public class HTMLComponentFactoryTest extends TestCase {
  private TestContainer container = new TestContainer();
  private TestFactory factory = null;
  private VOValueHolder personHolder = new VOValueHolder();
  private VOCollectionValueHolder invoiceHolder = new VOCollectionValueHolder();
  private VOValueHolder unnamedHolder = new VOValueHolder();
  private ValueHolder[] holders = new ValueHolder[] {
      personHolder, invoiceHolder, unnamedHolder};

  public HTMLComponentFactoryTest(String _name) {
    super(_name);
  }

  /* setUp method for test case */
  protected void setUp() {
    factory = new TestFactory();
    personHolder.setAlias("Person");
    invoiceHolder.setAlias("Invoice");
    unnamedHolder.setAlias(null);
  }

  protected void setupTestPage1() throws Exception {
    DOMParser parser = new DOMParser();
    InputSource is = new InputSource(
        getClass().getResourceAsStream("TestPage1.html"));
    parser.parse(is);
    container.setDocument(parser.getDocument());
  }

  /* tearDown method for test case */
  protected void tearDown() {
  }

  /* test for method getSuggestedComponents(..) */
  public void testGetSuggestedComponentsForSpan() throws Exception {
    setupTestPage1();
    Vector suggestions = factory.getSuggestedComponents(
        container.getDocumentAdapter().getElementById("Person__fullName"));
    assertEquals("size", 2, suggestions.size());
    assertEquals("Suggestion 1", TextField.class.getName(),
        suggestions.elementAt(0));
    assertEquals("Suggestion 2", DocumentElement.class.getName(),
        suggestions.elementAt(1));
  }

  /* test for method getSuggestedComponents(..) */
  public void testGetSuggestedComponentsForTemplate() throws Exception {
    setupTestPage1();
    Vector suggestions = factory.getSuggestedComponents(
        container.getDocumentAdapter().getElementById("Invoice__TEMPLATE_NODE"));
    assertEquals("size", 2, suggestions.size());
    assertEquals("Suggestion 1", Template.class.getName(),
        suggestions.elementAt(0));
    assertEquals("Suggestion 2", DocumentElement.class.getName(),
        suggestions.elementAt(1));
  }

  /* test for method getSuggestedComponents(..) */
  public void testGetSuggestedComponentsForTD() throws Exception {
    setupTestPage1();
    Vector suggestions = factory.getSuggestedComponents(
        container.getDocumentAdapter().getElementById("Invoice__invoiceNumber"));
    assertEquals("size", 2, suggestions.size());
    assertEquals("Suggestion 1", TextField.class.getName(),
        suggestions.elementAt(0));
    assertEquals("Suggestion 2", DocumentElement.class.getName(),
        suggestions.elementAt(1));
  }

  /* test for method getSuggestedComponents(..) */
  public void testGetSuggestedComponentsForInputText() throws Exception {
    setupTestPage1();
    Vector suggestions = factory.getSuggestedComponents(
        container.getDocumentAdapter().getElementById("Person__lastName"));
    assertEquals("size", 2, suggestions.size());
    assertEquals("Suggestion 1", HTMLInputText.class.getName(),
        suggestions.elementAt(0));
    assertEquals("Suggestion 2", DocumentElement.class.getName(),
        suggestions.elementAt(1));
  }

  /* test for method getSuggestedComponents(..) */
  public void testGetSuggestedComponentsForSubmitButton() throws Exception {
    setupTestPage1();
    Vector suggestions = factory.getSuggestedComponents(
        container.getDocumentAdapter().getElementById("SaveButton"));
    assertEquals("size", 3, suggestions.size());
    assertEquals("Suggestion 1", Button.class.getName(),
        suggestions.elementAt(0));
    assertEquals("Suggestion 2", Trigger.class.getName(),
        suggestions.elementAt(1));
    assertEquals("Suggestion 3", DocumentElement.class.getName(),
        suggestions.elementAt(2));
  }

  public void testCreateComponentForSpan() throws Exception {
    setupTestPage1();
    Component component = factory.createComponentForElement(
        "Person__fullName",
        container.getDocumentAdapter().getElementById("Person__fullName"),
        holders
    );
    assertNotNull("Expected component not null", component);
    assertEquals("Component class", TextField.class, component.getClass());
    assertEquals("Component id", "Person__fullName",
        ((TextField)component).getId());
    assertEquals("Component holder", personHolder,
        ((TextField)component).getValueHolder());
    assertEquals("Component property name", "fullName",
        ((TextField)component).getPropertyName());
    assertNull("Component parameter not null",
        ((TextField)component).getParameter());
  }

  public void testCreateComponentForSpan2() throws Exception {
    setupTestPage1();
    Component component = factory.createComponentForElement(
        "Person__fullName__2",
        container.getDocumentAdapter().getElementById("Person__fullName__2"),
        holders
    );
    assertNotNull("Expected component not null", component);
    assertEquals("Component class", TextField.class, component.getClass());
    assertEquals("Component id", "Person__fullName__2",
        ((TextField)component).getId());
    assertEquals("Component holder", personHolder,
        ((TextField)component).getValueHolder());
    assertEquals("Component property name", "fullName",
        ((TextField)component).getPropertyName());
    assertNull("Component parameter not null",
        ((TextField)component).getParameter());
  }

  public void testCreateComponentForTemplate() throws Exception {
    setupTestPage1();
    Component component = factory.createComponentForElement(
        "Invoice__TEMPLATE_NODE",
        container.getDocumentAdapter().getElementById("Invoice__TEMPLATE_NODE"),
        holders
    );
    assertNotNull("Expected component not null", component);
    assertEquals("Component class", Template.class, component.getClass());
    assertEquals("Component id", "Invoice__TEMPLATE_NODE",
        ((Template)component).getId());
    assertEquals("Component holder", invoiceHolder,
        ((Template)component).getCollectionValueHolder());
  }

  public void testCreateComponentForBad1() throws Exception {
    setupTestPage1();
    Component component = factory.createComponentForElement(
        "__",
        container.getDocumentAdapter().getElementById("__"),
        holders
    );
    assertNull("Expected null", component);
  }

  public void testCreateComponentForBad2() throws Exception {
    setupTestPage1();
    Component component = factory.createComponentForElement(
        "Person__",
        container.getDocumentAdapter().getElementById("Person__"),
        holders
    );
    assertNull("Expected null", component);
  }

  public void testCreateComponentForInputText() throws Exception {
    setupTestPage1();
    Component component = factory.createComponentForElement(
        "Person__lastName",
        container.getDocumentAdapter().getElementById("Person__lastName"),
        holders
    );
    assertNotNull("Expected component not null", component);
    assertEquals("Component class", HTMLInputText.class, component.getClass());
    assertEquals("Component id", "Person__lastName",
        ((HTMLInputText)component).getId());
    assertEquals("Component holder", personHolder,
        ((HTMLInputText)component).getValueHolder());
    assertEquals("Component property name", "lastName",
        ((HTMLInputText)component).getPropertyName());
    assertEquals("Component parameter", "lastName",
        ((HTMLInputText)component).getParameter());
  }

  public void testCreateComponentForInputCheckbox() throws Exception {
    setupTestPage1();
    Component component = factory.createComponentForElement(
        "Person__active",
        container.getDocumentAdapter().getElementById("Person__active"),
        holders
    );
    assertNotNull("Expected component not null", component);
    assertEquals("Component class", HTMLCheckBox.class, component.getClass());
    assertEquals("Component id", "Person__active",
        ((HTMLCheckBox)component).getId());
    assertEquals("Component holder", personHolder,
        ((HTMLCheckBox)component).getValueHolder());
    assertEquals("Component property name", "active",
        ((HTMLCheckBox)component).getPropertyName());
    assertEquals("Component parameter", "active",
        ((HTMLCheckBox)component).getParameter());
/** @todo New feature to pickup value in document */
//    assertEquals("Component selected value", "Y",
//        ((HTMLCheckBox)component).getSelectedValue());
    assertEquals("Component unselected value", "",
        ((HTMLCheckBox)component).getUnselectedValue());
  }

  public void testCreateComponentsWithoutExisting() throws Exception {
    setupTestPage1();
    factory.createComponents(container, holders);

    Component[] components = container.getComponents();
    assertEquals("Component count in master container", 7, components.length);
    assertEquals("Component 0", TextField.class, components[0].getClass());
    assertEquals("Component 1", TextField.class, components[1].getClass());
    assertEquals("Component 2", Template.class, components[2].getClass());
    assertEquals("Component 3", HTMLInputText.class, components[3].getClass());
    assertEquals("Component 4", HTMLInputText.class, components[4].getClass());
    assertEquals("Component 5", HTMLCheckBox.class, components[5].getClass());
    assertEquals("Component 6", HTMLAnchorURL.class, components[6].getClass());

    components = ((Template)components[2]).getComponents();
    assertEquals("Component count in template container", 3, components.length);
    assertEquals("Component 0", TextField.class, components[0].getClass());
    assertEquals("Component 1", TextField.class, components[1].getClass());
    assertEquals("Component 2", TextField.class, components[2].getClass());
  }

  public void testCreateComponentsWithExistingTopContainer() throws Exception {
    setupTestPage1();
    Button button = new Button();
    button.setId("SaveButton");
    button.setParameter("action");
    button.setText("Save");
    container.add(button);
    factory.createComponents(container, holders);

    Component[] components = container.getComponents();
    assertEquals("Component count in master container", 8, components.length);
    assertEquals("Component 0", TextField.class, components[0].getClass());
    assertEquals("Component 1", TextField.class, components[1].getClass());
    assertEquals("Component 2", Template.class, components[2].getClass());
    assertEquals("Component 3", HTMLInputText.class, components[3].getClass());
    assertEquals("Component 4", HTMLInputText.class, components[4].getClass());
    assertEquals("Component 5", HTMLCheckBox.class, components[5].getClass());
    assertEquals("Component 6", Button.class, components[6].getClass());
    assertEquals("Component 7", HTMLAnchorURL.class, components[7].getClass());

    components = ((Template)components[2]).getComponents();
    assertEquals("Component count in template container", 3, components.length);
    assertEquals("Component 0", TextField.class, components[0].getClass());
    assertEquals("Component 1", TextField.class, components[1].getClass());
    assertEquals("Component 2", TextField.class, components[2].getClass());
  }

  public void testCreateComponentsWithExistingSubContainer() throws Exception {
    setupTestPage1();

    Button button = new Button();
    button.setId("SaveButton");
    button.setParameter("action");
    button.setText("Save");
    container.add(button);

    TextField field = new TextField();
    field.setId("Invoice__issueDate");
    container.add(field);
    factory.createComponents(container, holders);

    Component[] components = container.getComponents();
    assertEquals("Component count in master container", 8, components.length);
    assertEquals("Component 0", TextField.class, components[0].getClass());
    assertEquals("Component 1", TextField.class, components[1].getClass());
    assertEquals("Component 2", Template.class, components[2].getClass());
    assertEquals("Component 3", HTMLInputText.class, components[3].getClass());
    assertEquals("Component 4", HTMLInputText.class, components[4].getClass());
    assertEquals("Component 5", HTMLCheckBox.class, components[5].getClass());
    assertEquals("Component 6", Button.class, components[6].getClass());
    assertEquals("Component 7", HTMLAnchorURL.class, components[7].getClass());

    components = ((Template)components[2]).getComponents();
    assertEquals("Component count in template container", 3, components.length);
    assertEquals("Component 0", TextField.class, components[0].getClass());
    // Make sure Component 1 is the same field we created ahead of time.
    assertEquals("Component 1", field, components[1]);
    assertEquals("Component 2", TextField.class, components[2].getClass());
  }

  public void testCreateComponentWithDateFormat() throws Exception {
    setupTestPage1();
    Component component = factory.createComponentForElement(
        "Invoice__issueDate",
        container.getDocumentAdapter().getElementById("Invoice__issueDate"),
        holders
    );
    assertNotNull("Expected component not null", component);
    assertEquals("Component class", TextField.class, component.getClass());
    assertNotNull("Component format should not be null",
        ((TextField)component).getFormat());
    assertEquals("Component format class", java.text.SimpleDateFormat.class,
        ((TextField)component).getFormat().getClass());
    assertEquals("Component format pattern", "MM/dd/yyyy",
        ((TextField)component).getFormatPattern());
  }

  public void testCreateComponentWithNumberFormat() throws Exception {
    setupTestPage1();
    Component component = factory.createComponentForElement(
        "Invoice__invoiceNumber",
        container.getDocumentAdapter().getElementById("Invoice__invoiceNumber"),
        holders
    );
    assertNotNull("Expected component not null", component);
    assertEquals("Component class", TextField.class, component.getClass());
    assertNotNull("Component format should not be null",
        ((TextField)component).getFormat());
    assertEquals("Component format class", java.text.DecimalFormat.class,
        ((TextField)component).getFormat().getClass());
    assertEquals("Component format pattern", "0000000",
        ((TextField)component).getFormatPattern());
  }

  public void testCreateComponentWithMessageFormat() throws Exception {
    setupTestPage1();
    Component component = factory.createComponentForElement(
        "Invoice__customerID",
        container.getDocumentAdapter().getElementById("Invoice__customerID"),
        holders
    );
    assertNotNull("Expected component not null", component);
    assertEquals("Component class", TextField.class, component.getClass());
    assertNotNull("Component format should not be null",
        ((TextField)component).getFormat());
    assertEquals("Component format class", java.text.MessageFormat.class,
        ((TextField)component).getFormat().getClass());
    assertEquals("Component format pattern", "CustNo={0}",
        ((TextField)component).getFormatPattern());
  }

  public void testCreateComponentWithMessageFormatInValueAttribute() throws Exception {
    setupTestPage1();
    Component component = factory.createComponentForElement(
        "Person__lastName",
        container.getDocumentAdapter().getElementById("Person__lastName"),
        holders
    );
    assertNotNull("Expected component not null", component);
    assertEquals("Component class", HTMLInputText.class, component.getClass());
    assertNotNull("Component format should not be null",
        ((HTMLInputText)component).getFormat());
    assertEquals("Component format class", java.text.MessageFormat.class,
        ((HTMLInputText)component).getFormat().getClass());
    assertEquals("Component format pattern", "{0}",
        ((HTMLInputText)component).getFormatPattern());
  }

  public void testCreateComponentWithoutFormat() throws Exception {
    setupTestPage1();
    Component component = factory.createComponentForElement(
        "Person__fullName",
        container.getDocumentAdapter().getElementById("Person__fullName"),
        holders
    );
    assertNotNull("Expected component not null", component);
    assertEquals("Component class", TextField.class, component.getClass());
    assertNull("Component format should be null",
        ((TextField)component).getFormat());
    assertNull("Component format pattern should be null",
        ((TextField)component).getFormatPattern());
  }

  public void testCreateComponentWithMessageFormatInHrefAttribute() throws Exception {
    setupTestPage1();
    Component component = factory.createComponentForElement(
        "Person__personID",
        container.getDocumentAdapter().getElementById("Person__personID"),
        holders
    );
    assertNotNull("Expected component not null", component);
    assertEquals("Component class", HTMLAnchorURL.class, component.getClass());
    assertNotNull("Component format should not be null",
        ((HTMLAnchorURL)component).getFormat());
    assertEquals("Component format class", java.text.MessageFormat.class,
        ((HTMLAnchorURL)component).getFormat().getClass());
    assertEquals("Component format pattern", "http://localhost/mypage.mxform?pid={0}",
        ((HTMLAnchorURL)component).getFormatPattern());
  }

  // ==========================================================================
  //                     Test Support Methods and Classes
  // ==========================================================================

  /**
   * Test Factory Class
   */
  class TestFactory extends HTMLComponentFactory {
    protected Component createComponentForElement(String id, Element element,
        ValueHolder[] holders) {
      return super.createComponentForElement(id, element, holders);
    }
  }

  /**
   * Test Container Class
   */
  class TestContainer extends Container {
    private DocumentAdapter documentAdapter;

    public DocumentAdapter getDocumentAdapter() {
      return documentAdapter;
    }

    public void setDocument(org.w3c.dom.Document newDocument) {
      documentAdapter = new DOM_1_20000929_DocumentAdapter(newDocument);
    }

    public void removeNotify() {}
    public void addNotify() {}

    public org.w3c.dom.Document getDocument() {
      if (documentAdapter == null)
        return null;
      else
        return documentAdapter.getDocument();
    }
  }

  /* Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {HTMLComponentFactoryTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }
}
