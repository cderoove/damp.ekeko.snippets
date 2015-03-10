package com.taursys.dom.test;

import junit.framework.TestCase;
import com.taursys.dom.*;
import org.xml.sax.InputSource;
import org.apache.xerces.parsers.DOMParser;
import java.io.*;
import org.w3c.dom.*;

/* JUnitTest case for class: com.taursys.dom.DOM_1_20000929_DocumentAdapter */
public class DOM_1_20000929_DocumentAdapterTest extends TestCase {
  DOM_1_20000929_DocumentAdapter adapter = null;

  public DOM_1_20000929_DocumentAdapterTest(String _name) {
    super(_name);
  }

  /* setUp method for test case */
  protected void setUp() {
  }

  /* tearDown method for test case */
  protected void tearDown() {
  }

  protected byte[] getEmptyXMLDocBytes() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(bos);
    writer.println("<marty>");
    writer.println("</marty>");
    writer.flush();
    writer.close();
    return bos.toByteArray();
  }

  protected byte[] getHTMLDoc1Bytes() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(bos);

//    writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    writer.println("<html>");
    writer.println("  <head>");
    writer.println("    <title>Test</title>");
    writer.println("  </head>");
    writer.println("  <body>");
    writer.println("    <h1>Test</h1>");
    writer.println("    <p id=\"p1\">Test</p>");
    writer.println("    <p id=\"p2\">Test</p>");
    writer.println("  </body>");
    writer.println("</html>");
    writer.flush();
    writer.close();
    return bos.toByteArray();
  }

  protected byte[] getXHTMLBytes() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(bos);

//    writer.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
    writer.println(
        "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" "
        + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
//        + "\"file:///home/marty/jbproject/MapperXML/xhtml1-transitional.dtd\">");
    writer.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");

    writer.println("  <head>");
    writer.println("    <title>Test</title>");
    writer.println("  </head>");
    writer.println("  <body>");
    writer.println("    <h1>Test</h1>");
    writer.println("    <p id=\"p1\">Test</p>");
    writer.println("    <p id=\"p2\">Test</p>");
    writer.println("  </body>");
    writer.println("</html>");
    writer.flush();
    writer.close();
    return bos.toByteArray();
  }

  protected byte[] getHTMLNoXMLBytes() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(bos);
    writer.println("<html>");
    writer.println("  <head>");
    writer.println("    <title>Test</title>");
    writer.println("  </head>");
    writer.println("  <body>");
    writer.println("    <h1>Test</h1>");
    writer.println("    <p id=\"p3\">Test</p>");
    writer.println("  </body>");
    writer.println("</html>");
    writer.flush();
    writer.close();
    return bos.toByteArray();
  }

  protected byte[] getHTMLWithEndingSlash() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(bos);
    writer.println("<html>");
    writer.println("  <head>");
    writer.println("    <title>Test</title>");
    writer.println("  </head>");
    writer.println("  <body>");
    writer.println("    <h1>Test</h1>");
    writer.println("    <br/>");
    writer.println("    <img alt=\"img\" src=\"img.gif\"/>");
    writer.println("  </body>");
    writer.println("</html>");
    writer.flush();
    writer.close();
    return bos.toByteArray();
  }

  protected byte[] getHTMLWithoutEndingSlash() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(bos);
    writer.println("<html>");
    writer.println("  <head>");
    writer.println("    <title>Test</title>");
    writer.println("  </head>");
    writer.println("  <body>");
    writer.println("    <h1>Test</h1>");
    writer.println("    <br>");
    writer.println("    <img alt=\"img\" src=\"img.gif\">");
    writer.println("  </body>");
    writer.println("</html>");
    writer.flush();
    writer.close();
    return bos.toByteArray();
  }

  protected Document getDocumentFromBytes(byte[] bytes) throws Exception {
    DOMParser parser = new DOMParser();
    InputSource is = new InputSource(new ByteArrayInputStream(bytes));
    parser.parse(is);
    return parser.getDocument();
  }

  /** Returns a sorted list of attributes. */
  protected Attr[] sortAttributes(NamedNodeMap attrs) {
    int len = (attrs != null) ? attrs.getLength() : 0;
    Attr array[] = new Attr[len];
    for (int i = 0; i < len; i++) {
      array[i] = (Attr)attrs.item(i);
    }
    for (int i = 0; i < len - 1; i++) {
      String name = array[i].getNodeName();
      int index = i;
      for (int j = i + 1; j < len; j++) {
        String curName = array[j].getNodeName();
        if (curName.compareTo(name) < 0) {
          name = curName;
          index = j;
        }
      }
      if (index != i) {
        Attr temp = array[i];
        array[i] = array[index];
        array[index] = temp;
      }
    }
    return array;
  }

  protected void dump(Document doc) {
    System.out.println("Begin dump ===========================================");
    dump(doc,"");
    System.out.println("End dump ===========================================");
  }

  protected void dump(Node node, String level) {
    System.out.print(level);
    System.out.print(node.getNodeName());
    Attr attrs[] = sortAttributes(node.getAttributes());
    for (int i = 0; i < attrs.length; i++) {
      Attr attr = attrs[i];
      System.out.print(' ');
      System.out.print(attr.getNodeName());
      System.out.print("=");
      System.out.print(attr.getNodeValue());
    }
    System.out.println(" value=" + node.getNodeValue());

    Node child = node.getFirstChild();
    while (child != null) {
      dump(child, level+"  ");
      child = child.getNextSibling();
    }
  }

  protected void dumpDocInfo(Document document) {
    System.out.println(">>>>>>>>>>>>>>>>>>>>>>");
//    System.out.println("Doc version=" + document.getVersion());
//    System.out.println("Doc encoding=" + document.getEncoding());
    System.out.println("Doc localName=" + document.getLocalName());
    System.out.println("Doc prefix=" + document.getPrefix());
    System.out.println("Doc namespaceURI=" + document.getNamespaceURI());
    System.out.println("Doc nodeName=" + document.getNodeName());
    System.out.println("Doc documentElement.nodeName=" + document.getDocumentElement().getNodeName());
    DocumentType docType = document.getDoctype();
    System.out.println("Doc type=" + docType);
    if (docType != null) {
      System.out.println("  DocType name=" +  docType.getName());
      System.out.println("  DocType localName=" +  docType.getLocalName());
      System.out.println("  DocType namespaceURI=" +  docType.getNamespaceURI());
      System.out.println("  DocType nodeName=" +  docType.getNodeName());
      System.out.println("  DocType nodeValue=" +  docType.getNodeValue());
      System.out.println("  DocType prefix=" +  docType.getPrefix());
      System.out.println("  DocType publicId=" +  docType.getPublicId());
      System.out.println("  DocType systemId=" +  docType.getSystemId());
    }
  }

  protected void parseWrite(byte[] bytes) throws Exception {
    parseWrite(bytes, bytes);
  }

  protected void parseWrite(byte[] sourceBytes, byte[] expectedBytes)
      throws Exception {
    Document doc = getDocumentFromBytes(sourceBytes);
    adapter = new DOM_1_20000929_DocumentAdapter(doc);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    adapter.write(bos);
    BufferedReader resultsReader = new BufferedReader(
      new InputStreamReader(new ByteArrayInputStream(bos.toByteArray())));
    BufferedReader expectedReader = new BufferedReader(
      new InputStreamReader(new ByteArrayInputStream(expectedBytes)));
    // Check each line
    String expected;
    int i = 0;
    while ((expected = expectedReader.readLine()) != null) {
      assertEquals("Contents line #" + i, expected, resultsReader.readLine());
      i++;
    }
  }

  // ========================================================================
  //                                Tests
  // ========================================================================

  public void testCreate() throws Exception {
    Document doc = getDocumentFromBytes(getHTMLDoc1Bytes());
    adapter = new DOM_1_20000929_DocumentAdapter(doc);
    assertEquals("Document", doc, adapter.getDocument());
    assertNotNull("p2 Element not found", adapter.getElementById("p2"));
  }

  /* test for method setDocument(..) */
  public void testSetDocument() throws Exception {
    Document doc = getDocumentFromBytes(getEmptyXMLDocBytes());
    adapter = new DOM_1_20000929_DocumentAdapter(doc);
    doc = getDocumentFromBytes(getHTMLDoc1Bytes());
    adapter.setDocument(doc);
    assertEquals("Document", doc, adapter.getDocument());
    assertNotNull("p2 Element not found", adapter.getElementById("p2"));
  }

  /* test for method write(..) */
  public void testWrite_HTML() throws Exception {
    parseWrite(getHTMLDoc1Bytes());
  }

  /* test for method write(..) */
  public void testWrite_XHTML() throws Exception {
    parseWrite(getXHTMLBytes());
  }

  /* test for method write(..) */
  public void testWrite_HTMLNoXML() throws Exception {
    parseWrite(getHTMLNoXMLBytes());
  }

  /* test for method write(..) */
  public void testWrite_HTMLWithoutEndingSlash() throws Exception {
    parseWrite(getHTMLWithEndingSlash(), getHTMLWithoutEndingSlash());
  }

  /* Executes the test case */
  public static void main(String[] argv) {
    String[] testCaseList = {DOM_1_20000929_DocumentAdapterTest.class.getName()};
    junit.swingui.TestRunner.main(testCaseList);
  }
}
