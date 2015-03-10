/**
 * DispatchingContainer - Container that has Dispatchers for all Events.
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
import java.util.*;

/**
 * <p><code>DispatchingContainer</code> is a <code>Container</code> which can
 * function as a top or intermediate level <code>Container</code> and has
 * its own set of event <code>Dispatchers</code> for its children. It responds to
 * <code>ParameterEvents</code>, <code>InputEvents</code> and
 * <code>TriggerEvents</code> by simply propagating the event to its children.
 * </p>
 * <p>In response to a <code>RenderEvent</code>, it simply shows or hides the
 * Document Element and, if visible, dispatches a <code>RenderEvent</code> to
 * the children of the <code>DocumentElement</code>.
 * </p>
 * <p>In response to a <code>RecycleEvent</code>, it simply makes the Document
 * Element visible, and dispatches a <code>RecycleEvent</code> to
 * the children of the <code>DocumentElement</code>.
 * </p>
 * @author Marty Phelan
 * @version 1.0
 */
public class DispatchingContainer extends Container {
  private ParameterDispatcher parameterDispatcher;
  private InputDispatcher inputDispatcher;
  private TriggerDispatcher triggerDispatcher;
  private RenderDispatcher renderDispatcher;
  private RecycleDispatcher recycleDispatcher;
  private Map parameterMap = Collections.EMPTY_MAP;

  /**
   * Constructs a new DispatchingContainer
   */
  public DispatchingContainer() {
    parameterDispatcher = createParameterDispatcher();
    inputDispatcher = createInputDispatcher();
    triggerDispatcher = createTriggerDispatcher();
    renderDispatcher = createRenderDispatcher();
    recycleDispatcher = createRecycleDispatcher();
    addDispatcher(ParameterEvent.class.getName(), parameterDispatcher);
    addDispatcher(InputEvent.class.getName(), inputDispatcher);
    addDispatcher(TriggerEvent.class.getName(), triggerDispatcher);
    addDispatcher(RenderEvent.class.getName(), renderDispatcher);
    addDispatcher(RecycleEvent.class.getName(), recycleDispatcher);
    addEventType(ParameterEvent.class.getName());
    addEventType(InputEvent.class.getName());
    addEventType(TriggerEvent.class.getName());
    addEventType(RenderEvent.class.getName());
    addEventType(RecycleEvent.class.getName());
  }

  // =======================================================================
  //                          Create Dispatchers
  // =======================================================================

  /**
   * Create the ParameterDispatcher for this Container.
   * @return the ParameterDispatcher for this Container.
   */
  protected ParameterDispatcher createParameterDispatcher() {
    return new ParameterDispatcher();
  }

  /**
   * Create the InputDispatcher for this Container.
   * @return the InputDispatcher for this Container.
   */
  protected InputDispatcher createInputDispatcher() {
    return new InputDispatcher();
  }

  /**
   * Create the TriggerDispatcher for this Container.
   * @return the TriggerDispatcher for this Container.
   */
  protected TriggerDispatcher createTriggerDispatcher() {
    return new TriggerDispatcher();
  }

  /**
   * Create the RenderDispatcher for this Container.
   * @return the RenderDispatcher for this Container.
   */
  protected RenderDispatcher createRenderDispatcher() {
    return new RenderDispatcher(this);
  }

  /**
   * Create the RecycleDispatcher for this Container.
   * @return the RecycleDispatcher for this Container.
   */
  protected RecycleDispatcher createRecycleDispatcher() {
    return new RecycleDispatcher(this);
  }


  // =======================================================================
  //                          Property Accessors
  // =======================================================================

  /**
   * Get the Map of parameters contained in the request. The Map must contain
   * String keys for each parameter and String[] arrays for each parameter value.
   * @return a Map of parameters contained in the request.
   */
  public Map getParameterMap() {
    return parameterMap;
  }

  /**
   * Set the Map of parameters contained in the request. The Map must contain
   * String keys for each parameter and String[] arrays for each parameter value.
   * @param map a Map of parameters contained in the request.
   */
  public void setParameterMap(Map map) {
    parameterMap = map;
  }

  // =======================================================================
  //                       Event Initiator Methods
  // =======================================================================

  /**
   * Initiate the dispatch of the <code>ParameterEvent</code> to registered
   * components using the current <code>parameterMap</code>.
   */
  protected void dispatchParameters() throws Exception {
    parameterDispatcher.dispatch(parameterMap);
  }

  /**
   * Initiate the dispatch of the <code>InputEvent</code> to registered
   * components using the current <code>parameterMap</code>.
   */
  protected void dispatchInput() throws Exception {
    inputDispatcher.dispatch(parameterMap);
  }

  /**
   * Initiate the dispatch of the <code>TriggerEvent</code> to registered
   * components using the current <code>parameterMap</code>.
   */
  protected void dispatchActions() throws Exception {
    triggerDispatcher.dispatch(parameterMap);
  }

  /**
   * Initiate the dispatch of the <code>RenderEvent</code> to registered
   * components.
   */
  protected void dispatchRender() throws Exception {
    renderDispatcher.dispatch();
  }

  /**
   * Initiate the dispatch of the <code>RecycleEvent</code> to registered
   * components.
   */
  protected void dispatchRecycle() throws Exception {
    recycleDispatcher.dispatch();
  }

  // =======================================================================
  //                       Event Processing Methods
  // =======================================================================

  /**
   * Dispatches given <code>ParameterEvent</code> to children then notifies all
   * <code>ParameterListeners</code> of event.
   * @param e the <code>ParameterEvent</code>
   * @throws Exception from child or listener in response to event
   */
  protected void processParameterEvent(ParameterEvent e) throws Exception {
    parameterDispatcher.dispatch(e.getMap());
    fireParameterReceived(e);

  }

  /**
   * Dispatches given <code>InputEvent</code> to children then notifies all
   * <code>InputListeners</code> of event.
   * @param e the <code>InputEvent</code>
   * @throws Exception from child or listener in response to event
   */
  protected void processInputEvent(InputEvent e) throws Exception {
    inputDispatcher.dispatch(e.getMap());
    fireInputReceived(e);
  }

  /**
   * Dispatches given <code>TriggerEvent</code> to children then notifies all
   * <code>TriggerListeners</code> of event.
   * @param e the <code>TriggerEvent</code>
   * @throws Exception from child or listener in response to event
   */
  protected void processTriggerEvent(TriggerEvent e) throws Exception {
    triggerDispatcher.dispatch(e.getMap());
    fireActionPerformed(e);
  }

  /**
   * Notifies all <code>RenderListeners</code> of given
   * <code>RenderEvent</code> then dispatches event to children.
   * @param e the <code>RenderEvent</code>
   * @throws RenderException from child or listener in response to event
   */
  public void processRenderEvent(RenderEvent e) throws RenderException {
    fireRender(e);
    renderDispatcher.dispatch();
  }

  /**
   * Dispatches given <code>RecycleEvent</code> to children then notifies all
   * <code>RecycleListeners</code> of event.
   * @param e the <code>RecycleEvent</code>
   * @throws RecycleException from child or listener in response to event
   */
  public void processRecycleEvent(RecycleEvent e) throws RecycleException {
    recycleDispatcher.dispatch();
    fireRecycle(e);
  }
}
