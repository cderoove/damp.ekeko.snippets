/**
 * Form -
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

import com.taursys.xml.render.*;
import com.taursys.xml.event.*;
import com.taursys.servlet.*;
import com.taursys.dom.*;
import java.util.*;

/**
 * Form is ...
 * @author Marty Phelan
 * @version 1.0
 */
public class Form extends DocumentElement {
  public static final String PARAMETER_MAP = "com.taursys.xml.ParameterMap";
  private transient ArrayList initContextListeners;
  private transient ArrayList initFormListeners;
  private transient ArrayList openFormListeners;
  private transient ArrayList closeFormListeners;

  private InitContextDispatcher initContextDispatcher = new InitContextDispatcher(this);
  private InitFormDispatcher initFormDispatcher = new InitFormDispatcher(this);
  private OpenFormDispatcher openFormDispatcher = new OpenFormDispatcher(this);
  private CloseFormDispatcher closeFormDispatcher = new CloseFormDispatcher(this);

  private String sourceId;
  private boolean initialized;
  private DocumentAdapter documentAdapter;
  private Map formContext = new HashMap();

  // =======================================================================
  //                    Constructors and Subcomponents
  // =======================================================================

  /**
   * Constructs a new Form
   */
  public Form() {
    super();

    addEventType(InitContextEvent.class.getName());
    addEventType(InitFormEvent.class.getName());
    addEventType(OpenFormEvent.class.getName());
    addEventType(CloseFormEvent.class.getName());

    addDispatcher(InitContextEvent.class.getName(), initContextDispatcher);
    addDispatcher(InitFormEvent.class.getName(),initFormDispatcher);
    addDispatcher(OpenFormEvent.class.getName(),openFormDispatcher);
    addDispatcher(CloseFormEvent.class.getName(),closeFormDispatcher);
  }

  /**
   * Creates the default DocumentElementRenderer for this component.
   * By Default this methos returns a new DocumentElementRenderer.
   * Override this method to define your own DocumentElementRenderer.
   */
  protected DocumentElementRenderer createDefaultRenderer() {
    return new FormRenderer(this);
  }

  // =======================================================================
  //                       Event Initiator Methods
  // =======================================================================

  /**
   * This method is invoked by run to dispatch the formContext to nested Forms.
   * This method depends on the parameterMap property being set. A new context
   * is constructed which contains everything already in this
   * <code>Form's</code> plus the parameterMap.
   */
  protected void dispatchInitContext() throws Exception {
    HashMap newContext = new HashMap(formContext);
    newContext.put(PARAMETER_MAP, getParameterMap());
    initContextDispatcher.dispatch(newContext);
  }

  /**
   * Initiate the dispatch of the <code>InitFormEvent</code> to registered
   * nested <code>Forms</code>. This method should only be invoked after the
   * <code>InitContextEvent</code> has been dispatched.
   */
  protected void dispatchInitForm() throws Exception {
    initFormDispatcher.dispatch();
  }

  /**
   * Initiate the dispatch of the <code>OpenFormEvent</code> to registered
   * nested <code>Forms</code>. This method should only be invoked after the
   * <code>InitFormEvent</code> and <code>ParameterEvent</code> have been
   * dispatched.
   */
  protected void dispatchOpenForm() throws Exception {
    openFormDispatcher.dispatch();
  }

  /**
   * Initiate the dispatch of the <code>CloseFormEvent</code> to registered
   * nested <code>Forms</code>. This method should be invoked after all
   * processing is complete. Normally, nested <code>Forms</code> should be
   * allowed to close before this form closes.
   */
  protected void dispatchCloseForm() throws Exception {
    closeFormDispatcher.dispatch();
  }

  // =======================================================================
  //                       GENERAL EVENT PROCESSING
  // =======================================================================

  /**
   * Processes given event by invoking the appropriate processX__Event method.
   * The appropriate event type is determined by the class of the given
   * EventObject.
   */
  protected void processEvent(EventObject e) throws Exception {
    if (e instanceof InitContextEvent)
      processInitContextEvent((InitContextEvent)e);
    if (e instanceof InitFormEvent)
      processInitFormEvent((InitFormEvent)e);
    if (e instanceof OpenFormEvent)
      processOpenFormEvent((OpenFormEvent)e);
    if (e instanceof CloseFormEvent)
      processCloseFormEvent((CloseFormEvent)e);
    else
      super.processEvent(e);
  }

  // =======================================================================
  //                       INIT CONTEXT EVENT METHODS
  // =======================================================================

  /**
   * Processes a given InitContextEvent by setting the <code>formContext</code>
   * and <code>parameterMap</code> from the given message, then dispatching an
   * <code>InitContextEvent</code> to the <code>Form's</code> children, and
   * finally propagating the <code>InitContextEvent</code> to registered
   * listeners.
   * @param e the InitContextEvent to process
   */
  protected void processInitContextEvent(InitContextEvent e) throws Exception {
    formContext = e.getContext();
    setParameterMap((Map)lookup(PARAMETER_MAP));
    initContextDispatcher.dispatch(e);
    fireInitContextReceived(e);
  }

  /**
   * Removes given listener from notification list for InitContextEvents
   */
  public void removeInitContextListener(InitContextListener l) {
    if (initContextListeners != null && initContextListeners.contains(l)) {
      initContextListeners.remove(l);
    }
  }

  /**
   * Adds given listener to notification list for InitContextEvents
   */
  public void addInitContextListener(InitContextListener l) {
    if (initContextListeners != null) {
      if (!initContextListeners.contains(l)) {
        initContextListeners.add(l);
      }
    } else {
      initContextListeners = new ArrayList();
      initContextListeners.add(l);
    }
  }

  /**
   * Notifies all registered listeners of a given InitContextEvent
   */
  protected void fireInitContextReceived(InitContextEvent e) throws Exception {
    if (initContextListeners != null) {
      Iterator iter = initContextListeners.iterator();
      while (iter.hasNext()) {
        ((InitContextListener)iter.next()).initContext(e);
      }
    }
  }

  // =======================================================================
  //                       INIT FORM EVENT METHODS
  // =======================================================================

  /**
   * This method is invoked by run to initialize the form.  It is
   * the first method invoked by the doGet method before any parameters have
   * been dispatched. This method sets the initialized flag to true.
   * Override this method to provide custom behavior.  If you override,
   * be sure to invoke super.initForm or setInitialized(true) if this
   * method should only be called once.
   */
  protected void initForm() throws Exception {
    initialized = true;
  }

  /**
   * Processes a given InitFormEvent by invoking the Form's
   * initForm method (unless initialized is true), then dispatching an
   * <code>InitFormEvent</code> to the <code>Form's</code> children, and
   * finally propagating the <code>InitFormEvent</code> to registered listeners.
   * @param e the InitFormEvent to process
   */
  protected void processInitFormEvent(InitFormEvent e) throws Exception {
    if (!initialized)
      initForm();
    initFormDispatcher.dispatch(e);
    fireInitFormReceived(e);
  }

  /**
   * Removes given listener from notification list for InitFormEvents
   */
  public void removeInitFormListener(InitFormListener l) {
    if (initFormListeners != null && initFormListeners.contains(l)) {
      initFormListeners.remove(l);
    }
  }

  /**
   * Adds given listener to notification list for InitFormEvents
   */
  public void addInitFormListener(InitFormListener l) {
    if (initFormListeners != null) {
      if (!initFormListeners.contains(l)) {
        initFormListeners.add(l);
      }
    } else {
      initFormListeners = new ArrayList();
      initFormListeners.add(l);
    }
  }

  /**
   * Notifies all registered listeners of a given InitFormEvent
   */
  protected void fireInitFormReceived(InitFormEvent e) throws Exception {
    if (initFormListeners != null) {
      Iterator iter = initFormListeners.iterator();
      while (iter.hasNext()) {
        ((InitFormListener)iter.next()).initForm(e);
      }
    }
  }

  // =======================================================================
  //                       OPEN FORM EVENT METHODS
  // =======================================================================

  /**
   * This method is invoked as part of the processOpenFormEvent method.
   * It is normally invoked after processParameterEvent, but before
   * processInputEvent.  Override this method to provide custom
   * behavior such as opening data sources. This implementation has no
   * specific behavior.
   */
  protected void openForm() throws Exception {
  }

  /**
   * Processes a given OpenFormEvent by invoking the Form's
   * openForm method, then dispatching an <code>OpenFormEvent</code> to the
   * <code>Form's</code> children, and finally propagating the
   * <code>OpenFormEvent</code> to registered listeners.
   * @param e the OpenFormEvent to process
   */
  protected void processOpenFormEvent(OpenFormEvent e) throws Exception {
    openForm();
    openFormDispatcher.dispatch(e);
    fireOpenFormReceived(e);
  }

  /**
   * Removes given listener from notification list for OpenFormEvents
   */
  public void removeOpenFormListener(OpenFormListener l) {
    if (openFormListeners != null && openFormListeners.contains(l)) {
      openFormListeners.remove(l);
    }
  }

  /**
   * Adds given listener to notification list for OpenFormEvents
   */
  public void addOpenFormListener(OpenFormListener l) {
    if (openFormListeners != null) {
      if (!openFormListeners.contains(l)) {
        openFormListeners.add(l);
      }
    } else {
      openFormListeners = new ArrayList();
      openFormListeners.add(l);
    }
  }

  /**
   * Notifies all registered listeners of a given OpenFormEvent
   */
  protected void fireOpenFormReceived(OpenFormEvent e) throws Exception {
    if (openFormListeners != null) {
      Iterator iter = openFormListeners.iterator();
      while (iter.hasNext()) {
        ((OpenFormListener)iter.next()).openForm(e);
      }
    }
  }

  // =======================================================================
  //                       CLOSE FORM EVENT METHODS
  // =======================================================================

  /**
   * Closes the form and any resources it may have opened. This method is
   * invoked by the processCloseForm method which is normally invoked at the
   * end of the processing cycle but before this Form is recycled.
   * Override this method to provide custom behavior. This implementation has
   * no specific behavior.
   */
  protected void closeForm() throws Exception {
  }

  /**
   * Processes a given CloseFormEvent by invoking the Form's
   * closeForm method, then dispatching an <code>CloseFormEvent</code> to the
   * <code>Form's</code> children, and finally propagating the
   * <code>CloseFormEvent</code> to registered listeners.
   * @param e the CloseFormEvent to process
   */
  protected void processCloseFormEvent(CloseFormEvent e) throws Exception {
    closeForm();
    closeFormDispatcher.dispatch(e);
    fireCloseFormReceived(e);
  }

  /**
   * Removes given listener from notification list for CloseFormEvents
   */
  public void removeCloseFormListener(CloseFormListener l) {
    if (closeFormListeners != null && closeFormListeners.contains(l)) {
      closeFormListeners.remove(l);
    }
  }

  /**
   * Adds given listener to notification list for CloseFormEvents
   */
  public void addCloseFormListener(CloseFormListener l) {
    if (closeFormListeners != null) {
      if (!closeFormListeners.contains(l)) {
        closeFormListeners.add(l);
      }
    } else {
      closeFormListeners = new ArrayList();
      closeFormListeners.add(l);
    }
  }

  /**
   * Notifies all registered listeners of a given CloseFormEvent
   */
  protected void fireCloseFormReceived(CloseFormEvent e) throws Exception {
    if (closeFormListeners != null) {
      Iterator iter = closeFormListeners.iterator();
      while (iter.hasNext()) {
        ((CloseFormListener)iter.next()).closeForm(e);
      }
    }
  }

  // =======================================================================
  //                          Property Accessors
  // =======================================================================

  /**
   * Returns the id of the node this component is bound to.  This is the node
   * which this component will replicate.
   */
  public String getSourceId() {
    return sourceId;
  }

  /**
   * Sets the id of the node this component is bound to.  This is the node
   * which this component will replicate.
   */
  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  /**
   * Sets the document and creates the documentAdapter for this form.
   * This is the document which will be typically be modified and sent
   * back as the response.
   * @param newDocument for this form.
   */
  public void setDocument(org.w3c.dom.Document newDocument) {
/** @todo Use a factory to obtain a DocumentAdapter */
    documentAdapter = new DOM_1_20000929_DocumentAdapter(newDocument);
  }

  /**
   * Returns the document for this form.  This is the document which will
   * be sent back as the response.  This is also the document which the
   * components of this form will modify.
   */
  public org.w3c.dom.Document getDocument() {
    if (documentAdapter == null)
      return null;
    else
      return documentAdapter.getDocument();
  }

  /**
   * Sets the documentAdapter for this form.  The document adapter is used
   * by components as an adapter for the actual Document.  The adapter provides
   * the needed methods for components to manipulate the Document regardless
   * of the DOM version.
   * @param newDocumentAdapter for this form.
   */
  public void setDocumentAdapter(DocumentAdapter newDocumentAdapter) {
    documentAdapter = newDocumentAdapter;
  }

  /**
   * Gets the documentAdapter for this form.  The document adapter is used
   * by components as an adapter for the actual Document.  The adapter provides
   * the needed methods for components to manipulate the Document regardless
   * of the DOM version.
   * @return the documentAdapter for this form.
   */
  public DocumentAdapter getDocumentAdapter() {
    return documentAdapter;
  }

  /**
   * Sets an indicator that the form has been initialized (via the initForm method).
   * The run method will only invoke the initForm method if this indicator is
   * false, and then it will set this indicator to true.  This will prevent the
   * form from being initialized again (if it is recycled).
   */
  public void setInitialized(boolean newInitialized) {
    initialized = newInitialized;
  }

  /**
   * Indicates whether the form has been initialized (via the initForm method).
   * The run method will only invoke the initForm method if this indicator is
   * false, and then it will set this indicator to true.  This will prevent the
   * form from being initialized again (if it is recycled).
   */
  public boolean isInitialized() {
    return initialized;
  }

  /**
   * Get the <code>Map</code> of key/value pairs for this <code>Form's</code>
   * context.
   * @return the <code>Map</code> of key/value pairs for this <code>Form's</code>
   * context.
   */
  public Map getFormContext() {
    return formContext;
  }

  /**
   * Set the <code>Map</code> of key/value pairs for this <code>Form's</code>
   * context.
   * @param formContext the <code>Map</code> of key/value pairs for this
   * <code>Form's</code> context.
   * @throws IllegalArgumentException if the given value is null
   */
  public void setFormContext(Map formContext) {
    if (formContext == null)
      throw new IllegalArgumentException("Attempt to set formContext to null");
    this.formContext = formContext;
  }

  /**
   * Lookup a value from this <code>Form's</code> context.
   * @param key the key for the value to lookup
   * @return the value as an Object or null if not found.
   */
  public Object lookup(Object key) {
    return formContext.get(key);
  }
}
