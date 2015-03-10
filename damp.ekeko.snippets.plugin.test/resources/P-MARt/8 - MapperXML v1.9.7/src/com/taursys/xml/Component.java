/**
 * Component - Foundation Object for Mapper XML Components
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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import com.taursys.xml.event.RenderException;
import com.taursys.util.MapperComponent;
import com.taursys.xml.event.*;

/**
 * <p>This is the foundation object for MapperXML components.  All components
 * share this base abstract class.  It provides common attributes and basic
 * event dispatching methods with limited implementation.
 * </p>
 * <p>This object defines the following properties:</p>
 * <ul>
 * <li><code>parent</code> - the parent <code>Container</code> for this
 * <code>Component</code>. This property is set by the <code>Container</code>
 * methods: <code>add</code> and <code>remove</code>.
 * </li>
 * <li><code>visible</code> - this property is used by render subcomponents to
 * determine whether to hide or show this component during rendering.
 * </li>
 * </ul>
 * <p>This object is designed to respond to 5 types of events. The following
 * are the events this component is designed to respond to:</p>
 * <ul>
 * <li><code>ParameterEvent</code> - this event is generated when parameter
 * values are available for this component. A <code>ParameterEvent</code>
 * differs from an <code>InputEvent</code> only by WHEN it is dispatched.
 * <code>ParameterEvents</code> are normally dispatched before
 * <code>InputEvents</code>
 * </li>
 * <li><code>InputEvent</code> - this event is generated when input
 * values are available for this component. An <code>InputEvent</code>
 * differs from a <code>ParameterEvent</code> only by WHEN it is dispatched.
 * <code>InputEvents</code> are normally dispatched after
 * <code>ParameterEvents</code>
 * </li>
 * <li><code>TriggerEvent</code> - this event is generated when a specific
 * name/value pair appears (or does not appear) in the request.
 * </li>
 * <li><code>RenderEvent</code> - this event is generated when it is time
 * for components to render their value to the document.
 * </li>
 * <li><code>RecycleEvent</code> - this event is generated after the
 * request/response cycle as a signal that components should return to a
 * default or initial state. Components may also modify the document as
 * part of their response to this event.
 * </li>
 * </ul>
 * <p>In order to be notified of these events, this component must register
 * with each of the appropriate dispatchers. This is done by adding/removing
 * the types of events you wish to be notified of to the <code>eventTypeList</code>.
 * This is done by invoking the <code>addEventType</code> or
 * <code>removeEventType</code> method with the fully qualified class name
 * of the desired event. The <code>eventTypeList</code> is used by the
 * <code>addNotify</code> and <code>removeNotify</code> methods which are
 * invoked by the <code>Container</code> during the <code>add</code> and
 * <code>remove</code> <code>Container</code> methods.
 * </p>
 * <p>This component contains a 2 general purpose methods modeled after
 * AWT and Swing: <code>dispatchEvent</code> and <code>processEvent</code>.
 * These 2 methods simply call the specific processXXXEvent method based
 * on the event type.
 * </p>
 * <p>For each of the specific event types, there are a set of 3 related
 * methods.</p>
 * <ul>
 * <li><code>processXXXEvent</code> - this method is where you would provide
 * the necessary behavior to respond to the event. This abstract implementation
 * simply propagates the event to registered listeners. You should override
 * this method to provide appropriate behavior.
 * </li>
 * <li><code>addXXXListener</code> - registers the given listener with this
 * component to be notified whenever this component has began/ended processing
 * the event.
 * </li>
 * <li><code>removeXXXListener</code> - unregisters the given listener so it
 * will no longer be notified.
 * </li>
 * </ul>
 */
public abstract class Component implements MapperComponent {
  private transient ArrayList parameterListeners;
  private transient ArrayList inputListeners;
  private transient ArrayList renderListeners;
  private transient ArrayList recycleListeners;
  private transient ArrayList triggerListeners;
  /** Parent container for this component - package visibility */
  com.taursys.xml.Container parent;
  private boolean visible = true;
  private ArrayList eventTypeList = new ArrayList();
  private boolean notifySet = false;

  /**
   * Constructs a component
   */
  public Component() {
  }

  // =======================================================================
  //                     PROPERTY ACCESSOR METHODS
  // =======================================================================

  /**
   * Returns the parent container of this component else null.
   */
  public com.taursys.xml.Container getParent() {
    return parent;
  }

  /**
   * Set the indicator whether or not this component should be rendered visible.
   * This indicator is only meaningful for components which are in the Document.
   * @param visible the indicator whether or not this component should be rendered visible.
   */
  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  /**
   * Get the indicator whether or not this component should be rendered visible.
   * This indicator is only meaningful for components which are in the Document.
   * @return indicator whether or not this component should be rendered visible.
   */
  public boolean isVisible() {
    return visible;
  }

  // =======================================================================
  //              Event Notification Registration Methods
  // =======================================================================

  /**
   * Removes the given <code>eventType</code> from the event type list.
   * The event type list is used by <code>addNotify</code> and
   * <code>removeNotify</code> methods to register/un-register with
   * <code>Dispatchers</code> for events. The <code>eventType</code> is the
   * fully qualified class name of the event. This method does not
   * re-register with <code>Dispatchers</code>. You must invoke the
   * <code>addNotify</code> and <code>removeNotify</code> methods to
   * register with <code>Dispatchers</code>.
   * @param eventType fully qualified class name of the Event
   */
  protected void removeEventType(String eventType) {
    if (eventTypeList.contains(eventType)) {
      eventTypeList.remove(eventType);
    }
  }

  /**
   * Adds the given <code>eventType</code> to the event type list.
   * The event type list is used by <code>addNotify</code> and
   * <code>removeNotify</code> methods to register/un-register with
   * <code>Dispatchers</code> for events. The <code>eventType</code> is the
   * fully qualified class name of the event. This method does not
   * re-register with <code>Dispatchers</code>. You must invoke the
   * <code>addNotify</code> and <code>removeNotify</code> methods to
   * register with <code>Dispatchers</code>.
   * @param eventType fully qualified class name of the Event
   */
  protected void addEventType(String eventType) {
    if (!eventTypeList.contains(eventType)) {
      eventTypeList.add(eventType);
    }
  }

  /**
   * Registers this component with dispatcher to be notified of ParameterEvents
   * This method invokes the lazyAddNotify method to perform the work
   */
  public void addNotify() {
    lazyAddNotify();
  }

  /**
   * Un-Registers this component with dispatcher.
   * This method invokes the lazyRemoveNotify method to perform the work
   */
  public void removeNotify() {
    lazyRemoveNotify();
  }

  /**
   * Conditionally registers this component with dispatchers for the event types
   * contained in the <code>eventTypeList</code>. In order to be notified, this
   * component must have a parent and the notifySet flag must be false.
   */
  protected void lazyAddNotify() {
    if (!notifySet && parent != null) {
      // Register with each Dispatcher in eventTypeList
      Iterator iter = eventTypeList.iterator();
      while (iter.hasNext()) {
        Dispatcher dispatcher = parent.getDispatcher((String)iter.next());
        if (dispatcher != null) {
          dispatcher.addNotify(this);
        }
      }
      notifySet = true;
    }
  }

  /**
   * Conditionally un-registers this component from dispatcher for the event types
   * contained in the <code>eventTypeList</code>.
   * Only un-registers if it WAS registered and parent is not null.
   */
  protected void lazyRemoveNotify() {
    if (notifySet && parent != null) {
      // Un-register with each Dispatcher in eventTypeList
      Iterator iter = eventTypeList.iterator();
      while (iter.hasNext()) {
        Dispatcher dispatcher = parent.getDispatcher((String)iter.next());
        if (dispatcher != null)
          dispatcher.removeNotify(this);
      }
      notifySet = false;
    }
  }

  /**
   * Gets indicator of whether or not notification has been setup.
   * This flag is set to true after addNotify has successfully registered,
   * and set to false after removeNotify has successfully un-registered.
   * @return indicator if notification has been setup.
   */
  protected boolean isNotifySet() {
    return notifySet;
  }

  // =======================================================================
  //                       GENERAL EVENT PROCESSING
  // =======================================================================

  /**
   * Dispatches an event to this component or one of its subcomponents.
   * This method invokes processEvent before returning.
   */
  public void dispatchEvent(EventObject e) throws Exception {
    processEvent(e);
  }

  /**
   * Processes given event by invoking the appropriate processX__Event method.
   * The appropriate event type is determined by the class of the given
   * EventObject.
   */
  protected void processEvent(EventObject e) throws Exception {
    // In awt, selected events are dispatched to the specific processXxxEvent
    if (e instanceof InputEvent)
      processInputEvent((InputEvent)e);
    if (e instanceof ParameterEvent)
      processParameterEvent((ParameterEvent)e);
    if (e instanceof RenderEvent)
      processRenderEvent((RenderEvent)e);
    if (e instanceof TriggerEvent)
      processTriggerEvent((TriggerEvent)e);
    if (e instanceof RecycleEvent)
      processRecycleEvent((RecycleEvent)e);
  }

  // =======================================================================
  //                       PARAMETER EVENT METHODS
  // =======================================================================

  /**
   * Processes a given ParameterEvent.  This implementation simply propagates
   * the ParameterEvent to registered listeners. Components who need to respond
   * to ParameterEvents should override this method.
   */
  protected void processParameterEvent(ParameterEvent e) throws Exception {
    fireParameterReceived(e);
  }

  /**
   * Removes given listener from notification list for ParameterEvents
   */
  public void removeParameterListener(ParameterListener l) {
    if (parameterListeners != null && parameterListeners.contains(l)) {
      parameterListeners.remove(l);
    }
  }

  /**
   * Adds given listener to notification list for ParameterEvents
   */
  public void addParameterListener(ParameterListener l) {
    if (parameterListeners != null) {
      if (!parameterListeners.contains(l)) {
        parameterListeners.add(l);
      }
    } else {
      parameterListeners = new ArrayList();
      parameterListeners.add(l);
    }
  }

  /**
   * Notifies all registered listeners of a given ParameterEvent
   */
  protected void fireParameterReceived(ParameterEvent e) throws Exception {
    if (parameterListeners != null) {
      Iterator iter = parameterListeners.iterator();
      while (iter.hasNext()) {
        ((ParameterListener)iter.next()).parameterReceived(e);
      }
    }
  }

  // =======================================================================
  //                       INPUT EVENT METHODS
  // =======================================================================

  /**
   * Processes a given InputEvent.  This implementation simply propagates
   * the InputEvent to registered listeners. Components who need to respond
   * to InputEvents should override this method.
   */
  protected void processInputEvent(InputEvent e) throws Exception {
    fireInputReceived(e);
  }


  /**
   * Removes given listener from notification list for InputEvents
   */
  public synchronized void removeInputListener(InputListener l) {
    if (inputListeners != null && inputListeners.contains(l)) {
      inputListeners.remove(l);
    }
  }

  /**
   * Adds given listener to notification list for InputEvents
   */
  public synchronized void addInputListener(InputListener l) {
    if (inputListeners != null) {
      if (!inputListeners.contains(l)) {
        inputListeners.add(l);
      }
    } else {
      inputListeners = new ArrayList();
      inputListeners.add(l);
    }
  }

  /**
   * Notifies all registered listeners of a given InputEvent
   */
  protected void fireInputReceived(InputEvent e) throws Exception {
    if (inputListeners != null) {
      Iterator iter = inputListeners.iterator();
      while (iter.hasNext()) {
        ((InputListener)iter.next()).inputReceived(e);
      }
    }
  }

  // =======================================================================
  //                       RENDER EVENT METHODS
  // =======================================================================

  /**
   * Processes a given RenderEvent.  This implementation simply propagates
   * the RenderEvent to registered listeners. Components who need to respond
   * to RenderEvents should override this method.
   * @param e the RenderEvent to process
   * @throws RenderException for any problems during rendering
   */
  public void processRenderEvent(RenderEvent e) throws RenderException {
    fireRender(e);
  }

  /**
   * Removes given listener from notification list for RenderEvents
   */
  public synchronized void removeRenderListener(RenderListener l) {
    if (renderListeners != null && renderListeners.contains(l)) {
      renderListeners.remove(l);
    }
  }

  /**
   * Adds given listener to notification list for RenderEvents
   */
  public synchronized void addRenderListener(RenderListener l) {
    if (renderListeners != null) {
      if (!renderListeners.contains(l)) {
        renderListeners.add(l);
      }
    } else {
      renderListeners = new ArrayList();
      renderListeners.add(l);
    }
  }

  /**
   * Notifies all registered listeners of a given RenderEvent
   */
  protected void fireRender(RenderEvent e) throws RenderException {
    if (renderListeners != null) {
      Iterator iter = renderListeners.iterator();
      while (iter.hasNext()) {
        ((RenderListener)iter.next()).render(e);
      }
    }
  }

  // =======================================================================
  //                       TRIGGER EVENT METHODS
  // =======================================================================

  /**
   * Processes a given TriggerEvent.  This implementation simply propagates
   * the TriggerEvent to registered listeners. Components who need to respond
   * to TriggerEvents should override this method.
   */
  protected void processTriggerEvent(TriggerEvent e) throws Exception {
    fireActionPerformed(e);
  }

  /**
   * Removes given listener from notification list for TriggerEvents
   */
  public synchronized void removeTriggerListener(TriggerListener l) {
    if (triggerListeners != null && triggerListeners.contains(l)) {
      triggerListeners.remove(l);
    }
  }

  /**
   * Adds given listener to notification list for TriggerEvents
   */
  public synchronized void addTriggerListener(TriggerListener l) {
    if (triggerListeners != null) {
      if (!triggerListeners.contains(l)) {
        triggerListeners.add(l);
      }
    } else {
      triggerListeners = new ArrayList();
      triggerListeners.add(l);
    }
  }

  /**
   * Notifies all registered listeners of a given TriggerEvent
   */
  protected void fireActionPerformed(TriggerEvent e) throws Exception {
    if (triggerListeners != null) {
      Iterator iter = triggerListeners.iterator();
      while (iter.hasNext()) {
        ((TriggerListener)iter.next()).actionPerformed(e);
      }
    }
  }

  // =======================================================================
  //                       RECYCLE EVENT METHODS
  // =======================================================================

  /**
   * Processes a given RecycleEvent.  This implementation simply propagates
   * the RecycleEvent to registered listeners. Components who need to respond
   * to RecycleEvents should override this method.
   * @param e the RecycleEvent to process
   * @throws RecycleException for any problems during recycleing
   */
  public void processRecycleEvent(RecycleEvent e) throws RecycleException {
    fireRecycle(e);
  }

  /**
   * Removes given listener from notification list for RecycleEvents
   */
  public synchronized void removeRecycleListener(RecycleListener l) {
    if (recycleListeners != null && recycleListeners.contains(l)) {
      recycleListeners.remove(l);
    }
  }

  /**
   * Adds given listener to notification list for RecycleEvents
   */
  public synchronized void addRecycleListener(RecycleListener l) {
    if (recycleListeners != null) {
      if (!recycleListeners.contains(l)) {
        recycleListeners.add(l);
      }
    } else {
      recycleListeners = new ArrayList();
      recycleListeners.add(l);
    }
  }

  /**
   * Notifies all registered listeners of a given RecycleEvent
   */
  protected void fireRecycle(RecycleEvent e) throws RecycleException {
    if (recycleListeners != null) {
      Iterator iter = recycleListeners.iterator();
      while (iter.hasNext()) {
        ((RecycleListener)iter.next()).recycle(e);
      }
    }
  }
}
