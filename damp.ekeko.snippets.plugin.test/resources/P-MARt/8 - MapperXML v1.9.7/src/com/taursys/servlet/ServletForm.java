/**
 * ServletForm - Container for Components used by a Servlet Application
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
package com.taursys.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.taursys.dom.AbstractWriter;
import com.taursys.dom.DocumentAdapter;
import com.taursys.dom.XMLWriter;
import com.taursys.dom.DOM_1_20000929_DocumentAdapter;
import com.taursys.xml.event.*;
import com.taursys.servlet.respond.Responder;
import com.taursys.servlet.respond.HTMLResponder;
import com.taursys.debug.Debug;
import com.taursys.xml.Container;
import com.taursys.xml.Form;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Collections;

/**
 * ServletForm is the base container invoked used by Servlet application.
 * It contains all the components that make up the form.  The ServletForm
 * is responsible for servicing requests which are routed to it by the
 * ServletApp.
 * <p>
 * Your servlet forms will typically extend this base class.  The ServletForm
 * provides a default processing cycle within the <code>doGet</code> method.
 * <p>
 * The ServletForm is composed of many subcomponents to support the processing
 * of the request: ParameterDispatcher, InputDispatcher,
 * RenderDispatcher, TriggerDispatcher, DocumentAdapter and Responder.
 * <p>
 * You must supply this ServletForm with a DOM Document.  There are a variety
 * of ways you can achieve this: 1) use a DOM parser such as Xerces, 2) use
 * a DOM compiler such as Enhydra's XMLC, 3) build the DOM programatically.
 * <p>
 * The Document is normally created and attached to this ServletForm in the
 * <code>initForm</code> method.  This method is normally only called once when
 * the ServletForm is first invoked.  Below is an example using the Xerces
 * parser:
 * <pre>
 * protected void initForm() throws Exception {
 *   super.initForm();
 *   DOMParser parser = new DOMParser();
 *   InputSource is =
 *       new InputSource(getClass().getResourceAsStream("MyPage.html"));
 *   parser.parse(is);
 *   this.setDocument(parser.getDocument());
 * }
 * </pre>
 * In typical applications, you will add Components to this ServletForm and
 * set their properties to bind to the DOM Document elements, http request
 * parameters, and value objects.  These components are capable of modifying the
 * DOM Document, storing and retrieving value from bound objects, and reading
 * parameters from the http request, and parsing/converting between text values
 * and java data types.  This ServletForm is the base container for these
 * components and contains dispatchers which dispatch events to the components.
 * This ServletForm generates the events within the <code>doGet</code> method
 * in a fixed sequence (see javadoc for doGet).
 * <p>
 * The below example creates a HTMLInputText component which binds to an HTML
 * form input text field.  It also binds to the lastName property of a Java
 * value object class.
 * <pre>
 * public class MyPage extends ServletForm {
 *   HTMLInputText lastName = new HTMLInputText();
 *   VOValueHolder person = new VOValueHolder();
 *
 *   public MyPage() {
 *     lastName.setPropertyName("lastName");
 *     lastName.setValueHolder(person);
 *     lastName.setId("lastName");
 *     this.add(lastName);
 *   }
 *   ...
 *   protected void openForm() throws Exception {
 *     // Retrieve or create the value object
 *     Person personVO = new Person(1629, "Pulaski", "Katherine", null);
 *     // Bind value object to person ValueHolder
 *     person.setValueObject(personVO);
 *   }
 * </pre>
 * There are many components you can use in a ServletForm.  These include:
 * Parameter, Template, TextField, Trigger, Button, HTMLAnchorURL,
 * HTMLCheckBox, HTMLInputText, HTMLSelect, HTMLTextArea, and others.
 * <p>
 * You can control the response of this ServletForm by changing the Responder
 * subcomponent at runtime.  The default Responder is an HTMLResponder which
 * sends the DOM Document as the response.
 * <p>
 * The ServletForm is a reusable object. The ServletApp (master servlet) will
 * normally recycle ServletForms for an application unless their
 * <code>recycle</code> method returns false. The recycle method dispatches
 * a RecycleEvent to all components.
 * <p>
 * The ServletForm also supports multipart type requests. A multipart request
 * is sent by the browser when form data contains 1 or more uploaded files.
 * To support this feature, if the incoming request has a content type of
 * "multipart/form-data", the request is wrapped in another request object
 * which is capable of processing multipart requests. The wrapper request is
 * created via the createRequestWrapper method. By default, createRequestWrapper
 * returns a HttpMultiPartServletRequest object which has a maximum file size
 * of 1 megabyte and maximum single line size of 4,096 bytes. You can change
 * this by overriding the createRequestWrapper method:
 * <pre>
 * protected HttpServletRequest createRequestWrapper(HttpServletRequest rq)
 *     throws Exception {
 *   HttpMultiPartServletRequest multi = new HttpMultiPartServletRequest(rq);
 *   // set maximum sizes if defaults if needed
 *   multi.setMaxFileSize(2048);
 *   multi.setMaxLineLength(80);
 *   // parse the request
 *   multi.parseRequest();
 *   return multi;
 * }
 * </pre>
 * @see HttpMultiPartServletRequest
 */
public class ServletForm extends Form {
  private HttpServletRequest request;
  private HttpServletResponse response;
  private boolean enableInput = true;
  private boolean enableActions = true;
  private com.taursys.servlet.respond.Responder responder;

  // ************************************************************************
  //                   Constructors and Recycle Method
  // ************************************************************************

  /**
   * Creates new servlet form and default dispatchers.
   */
  public ServletForm() {
    setResponder(createDefaultResponder());
  }

  /**
   * Dispatches a RecycleEvent to all components and returns true if successful.
   * If an exception occurs during recycling, it is logged using Debug and
   * this method returns false.
   * If the form cannot be reused, override this method and return false.
   * Override this method to provide custom behavior to recycle this form
   * for future re-use.
   */
  public boolean recycle() {
    try {
      ((RecycleDispatcher)getDispatcher(RecycleEvent.class.getName())).dispatch();
      return true;
    } catch (RecycleException ex) {
      com.taursys.debug.Debug.error("Problem during recycling",ex);
      return false;
    }
  }

  // ************************************************************************
  //               Primary Method for Processing Request
  // ************************************************************************

  /**
   * This method is invoked by the application servlet to service the request.
   * It is invoked by the ServletApp for all request types (GET, POST, etc).
   * This method invokes a series of other support methods in a specific sequence:
   * <ul>
   * <li>saves request and response in properties (available using getRequest
   * and getResponse)
   * </li>
   * <li>initForm (only invoked if isInitialized is false)
   * </li>
   * <li>If this is a multipart type request, uses the request wrapper returned
   * by createRequestWrapper in place of the original request
   * </li>
   * <li>initializes the parameterMap property with parameters contained in the
   * request using the createParameterMap method.
   * </li>
   * <li>dispatchInitContext is invoked which dispatches an InitContextEvent
   * to any nested forms
   * </li>
   * <li>dispatchInitForm is invoked which dispatches an InitFormEvent
   * to any nested forms
   * </li>
   * <li>dispatchParameters is invoked
   * </li>
   * <li>openForm is invoked
   * </li>
   * <li>dispatchOpenForm is invoked which dispatches an OpenFormEvent
   * to any nested forms
   * </li>
   * <li>if enableInput flag is true, dispatchInput is invoked
   * </li>
   * <li>if enableActions flag is true, dispatchActions is invoked
   * </li>
   * <li>sendResponse is invoked
   * </li>
   * <li>dispatchCloseForm is invoked which dispatches an CloseFormEvent
   * to any nested forms
   * </li>
   * <li>closeForm is invoked
   * </li>
   * </ul>
   * If an exception is generated by any of these methods, the handleException
   * method is invoked. It can either handle the exception and send the
   * response, or it can rethrow the exception and let the application servlet
   * handle it (latter is default behavior).
   * @param req the incoming HttpServletRequest
   * @param resp the outgoing HttpServletResponse
   * @throws Exception if problem during processing the request
   */
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws Exception {
    try {
      response = resp;
      request = req;
      if (!isInitialized())
        initForm();
      if (req.getContentType() != null && req.getContentType().startsWith(
          HttpMultiPartServletRequest.MULTIPART_FORM_DATA)) {
        request = createRequestWrapper(req);
      }
      setParameterMap(createParameterMap());
      // dispatch initialization to nested forms
      dispatchInitContext();
      dispatchInitForm();
      // Begin processing
      dispatchParameters();
      openForm();
      dispatchOpenForm();
      if (enableInput) {
        dispatchInput();
      }
      if (enableActions) {
        dispatchActions();
      }
      sendResponse();
    } catch (Exception ex) {
      handleException(ex);
    } finally {
      dispatchCloseForm();
      closeForm();
    }
  }

  // ************************************************************************
  //                   Request Processing Support Methods
  // ************************************************************************

  /**
   * This method is invoked by run to dispatch the formContext to nested Forms.
   * This method depends on the request, response, and parameterMap properties
   * being set. A new context is constructed which contains everything already
   * in this <code>ServletForm's</code> plus the request, response and
   * parameterMap.
   */
  protected void dispatchInitContext() throws Exception {
    HashMap newContext = new HashMap(getFormContext());
    newContext.put(HttpServletRequest.class.getName(), request);
    newContext.put(HttpServletResponse.class.getName(), response);
    newContext.put(PARAMETER_MAP, getParameterMap());
    getDispatcher(InitContextEvent.class.getName()).dispatch(newContext);
  }

  /**
   * Send the appropriate response. It is invoked by doGet following the
   * dispatchActions method.  This method invokes the current Responder's
   * respond method to provide the appropriate response.
   * Change the Responder to provide custom response.
   */
  protected void sendResponse() throws Exception {
    responder.respond();
  }

  /**
   * This method is invoked whenever an exception occurs within doGet.
   * Override this method to provide custom exception handling behavior.
   * Throwing an exception will delegate the exception handling to the
   * caller of the doGet method.
   * The default behavior of this method is to simply re-throw the exception.
   */
  protected void handleException(Exception ex) throws Exception {
    throw ex;
  }

  // ************************************************************************
  //                     Subcomponent Creation Methods
  // ************************************************************************

  /**
   * Creates a multipart request wrapper to service a multipart request.
   * By default, this implementation creates a
   * <code>com.taursys.servlet.HttpMultiPartServletRequest</code> and invokes
   * its <code>parseRequest()</code> method.
   * You can override this method to create and initialize your own default
   * request wrapper.
   * @param the original servlet request
   * @return a request wrapper to service a multipart request.
   * @throws Exception if problem creating or initializing request wrapper
   */
  protected HttpServletRequest createRequestWrapper(HttpServletRequest rq)
      throws Exception {
    HttpMultiPartServletRequest multi = new HttpMultiPartServletRequest(rq);
    multi.parseRequest();
    return multi;
  }

  /**
   * Creates the default Responder for this component.
   * This implementation creates an com.taursys.servlet.respond.HTMLResponder.
   * You can override this method to create your own default Responder.
   * @return new instance of the default Responder for this component.
   */
  protected Responder createDefaultResponder() {
    return new HTMLResponder();
  }

  /**
   * Create a Map of parameters contained in the request. The resulting Map
   * is not modifiable. The Map contains String keys for each parameter and
   * String[] arrays for each parameter value. This method was included to
   * supplement the Servlet 2.2 spec which does not provide this method in
   * the ServletRequest interface.
   * @return a Map of parameters contained in the request.
   */
  public Map createParameterMap() {
    HashMap map = new HashMap();
    Enumeration enum_ = request.getParameterNames();
    while (enum_.hasMoreElements()) {
      String key = (String)enum_.nextElement();
      map.put(key, request.getParameterValues(key));
    }
    return Collections.unmodifiableMap(map);
  }

  // =======================================================================
  //                       INIT CONTEXT EVENT METHODS
  // =======================================================================

  /**
   * Processes a given InitContextEvent by setting the <code>formContext</code>,
   * <code>parameterMap</code>, <code>request</code> and <code>response</code>
   * from the given message, then dispatching an <code>InitContextEvent</code>
   * to the <code>Form's</code> children, and finally propagating the
   * <code>InitContextEvent</code> to registered listeners.
   * @param e the InitContextEvent to process
   */
  protected void processInitContextEvent(InitContextEvent e) throws Exception {
    request = (HttpServletRequest)
        e.getContext().get(HttpServletRequest.class.getName());
    response = (HttpServletResponse)
        e.getContext().get(HttpServletResponse.class.getName());
    super.processInitContextEvent(e);
  }

  // ************************************************************************
  //                       Property Accessor Methods
  // ************************************************************************

  /**
   * Sets the HttpServletRequest object for this ServletForm.
   * This is normally only valid during the invocation of the run method.
   * @param newRequest the HttpServletRequest object for this ServletForm
   */
  public void setRequest(HttpServletRequest newRequest) {
    request = newRequest;
  }

  /**
   * Gets the HttpServletRequest object for this ServletForm.
   * This is normally only valid during the invocation of the run method.
   * @return the HttpServletRequest object for this ServletForm
   */
  public HttpServletRequest getRequest() {
    return request;
  }

  /**
   * Sets the HttpServletResponse object for this ServletForm.
   * This is normally only valid during the invocation of the run method.
   * @param newResponse the HttpServletResponse object for this ServletForm
   */
  public void setResponse(HttpServletResponse newResponse) {
    response = newResponse;
  }

  /**
   * Gets the HttpServletResponse object for this ServletForm.
   * This is normally only valid during the invocation of the run method.
   * @return the HttpServletResponse object for this ServletForm
   */
  public HttpServletResponse getResponse() {
    return response;
  }

  /**
   * Set enableInput flag indicating whether or not to process input.
   * If this flag is set, the run method will invoke the dispatchInput method
   * to process input parameters for components. Disable input if you are
   * processing a request where no input parameters are expected. This will
   * avoid any input being set by default values (or behavior of HTMLCheckbox).
   * The default value for this flag is <code>true</code>.
   * @param newEnableInput flag indicating whether or not to process input.
   */
  public void setEnableInput(boolean newEnableInput) {
    enableInput = newEnableInput;
  }

  /**
   * Get enableInput flag indicating whether or not to process input.
   * If this flag is set, the run method will invoke the dispatchInput method
   * to process input parameters for components. Disable input if you are
   * processing a request where no input parameters are expected. This will
   * avoid any input being set by default values (or behavior of HTMLCheckbox).
   * The default value for this flag is <code>true</code>.
   * @return flag indicating whether or not to process input.
   */
  public boolean isEnableInput() {
    return enableInput;
  }

  /**
   * Set enableActions flag indicating whether or not to process actions.
   * If this flag is set, the run method will invoke the dispatchActions method
   * to process action parameters for trigger components.
   * Disable actions if you are processing a request where no action
   * parameters are expected.  This will avoid any actions being triggered
   * by default values.
   * The default value for this flag is <code>true</code>.
   * @param newEnableActions flag indicating whether or not to process actions.
   */
  public void setEnableActions(boolean newEnableActions) {
    enableActions = newEnableActions;
  }

  /**
   * Get enableActions flag indicating whether or not to process actions.
   * If this flag is set, the run method will invoke the dispatchActions method
   * to process action parameters for trigger components.
   * Disable actions if you are processing a request where no action
   * parameters are expected.  This will avoid any actions being triggered
   * by default values.
   * The default value for this flag is <code>true</code>.
   * @return flag indicating whether or not to process actions.
   */
  public boolean isEnableActions() {
    return enableActions;
  }

  /**
   * Set the Responder which will provide appropriate response.
   * You can change the Responder during runtime to provide different
   * kinds of responses.  A default Responder is created when this ServletForm
   * is created by the createDefaultResponder method.
   * This method also sets the Responder's servletForm property to this ServletForm.
   * @param newResponder the Responder which will provide appropriate response.
   */
  public void setResponder(Responder newResponder) {
    responder = newResponder;
    responder.setServletForm(this);
  }

  /**
   * Get the Responder which will provide appropriate response.
   * You can change the Responder during runtime to provide different
   * kinds of responses.  A default Responder is created when this ServletForm
   * is created by the createDefaultResponder method.
   * @return the Responder which will provide appropriate response.
   */
  public Responder getResponder() {
    return responder;
  }

  // ************************************************************************
  //                    Deprecated Methods and Properties
  // ************************************************************************
  private ServletParameterDispatcher spd;
  private AbstractWriter xmlWriter;
  private ServletInputDispatcher sid;
  private ServletTriggerDispatcher std;

  /**
   * Creates the default AbstractWriter for this component.
   * This implementation creates an com.taursys.xml.XMLWriter.
   * You can override this method to create your own default
   * AbstractWriter.
   * @deprecated the AbstractWriter is now a subcomponent of the DocumentAdapter.
   * This property is no longer used and will be removed shortly.
   */
  protected AbstractWriter createDefaultWriter() {
    return new XMLWriter();
  }

  /**
   * Sets the AbstractWriter used by this form to render the Document to
   * an XML stream.
   * @deprecated the AbstractWriter is now a subcomponent of the DocumentAdapter.
   * This property is no longer used and will be removed shortly.
   */
  public void setXmlWriter(AbstractWriter newXmlWriter) {
    xmlWriter = newXmlWriter;
  }

  /**
   * Returns the AbstractWriter used by this form to render the Document to
   * an XML stream.
   * @deprecated the AbstractWriter is now a subcomponent of the DocumentAdapter.
   * This property is no longer used and will be removed shortly.
   */
  public AbstractWriter getXmlWriter() {
    return xmlWriter;
  }

  /**
   * Creates the default ServletParameterDispatcher used by this container
   * @deprecated this now uses the inherited ParameterDispatcher. This method
   * is no longer used and will be removed in a future release.
   */
  protected ServletParameterDispatcher createDefaultServletParameterDispatcher() {
    return new ServletParameterDispatcher();
  }

  /**
   * Creates the default ServletInputDispatcher used by this container
   * @deprecated this now uses the inherited InputDispatcher. This method
   * is no longer used and will be removed in a future release.
   */
  protected ServletInputDispatcher createDefaultServletInputDispatcher() {
    return new ServletInputDispatcher();
  }

  /**
   * Creates the default ServletTriggerDispatcher used by this container
   * @deprecated this now uses the inherited TriggerDispatcher. This method
   * is no longer used and will be removed in a future release.
   */
  protected ServletTriggerDispatcher createDefaultServletTriggerDispatcher() {
    return new ServletTriggerDispatcher();
  }

  /**
   * Sets the ServletParameterDispatcher used by this container
   * @deprecated this now uses the inherited ParameterDispatcher. This method
   * is no longer used and will be removed in a future release.
   */
  public void setServletParameterDispatcher(ServletParameterDispatcher d) {
    spd = d;
  }

  /**
   * Returns the ServletParameterDispatcher used by this container
   * @deprecated this now uses the inherited ParameterDispatcher. This method
   * is no longer used and will be removed in a future release.
   */
  public ServletParameterDispatcher getServletParameterDispatcher() {
    return spd;
  }

  /**
   * Sets the ServletInputDispatcher used by this container
   * @deprecated this now uses the inherited InputDispatcher. This method
   * is no longer used and will be removed in a future release.
   */
  public void setServletInputDispatcher(ServletInputDispatcher dispatcher) {
    sid = dispatcher;
  }

  /**
   * Returns the ServletInputDispatcher used by this container
   * @deprecated this now uses the inherited InputDispatcher. This method
   * is no longer used and will be removed in a future release.
   */
  public ServletInputDispatcher getServletInputDispatcher() {
    return sid;
  }


  /**
   * Sets the ServletTriggerDispatcher used by this container
   * @deprecated this now uses the inherited TriggerDispatcher. This method
   * is no longer used and will be removed in a future release.
   */
  public void setServletTriggerDispatcher(ServletTriggerDispatcher newTriggerDispatcher) {
    std = newTriggerDispatcher;
  }

  /**
   * Returns the ServletTriggerDispatcher used by this container
   * @deprecated this now uses the inherited TriggerDispatcher. This method
   * is no longer used and will be removed in a future release.
   */
  public ServletTriggerDispatcher getServletTriggerDispatcher() {
    return std;
  }

}
