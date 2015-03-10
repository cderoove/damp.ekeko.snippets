/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import org.openide.awt.MouseUtils;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.util.actions.SystemAction;
import org.netbeans.modules.form.compat2.border.DesignBorder;
import org.netbeans.modules.form.compat2.layouts.DesignLayout;
import org.netbeans.modules.form.compat2.layouts.support.DesignSupportLayout;
import org.netbeans.modules.form.actions.DesignModeAction;
import org.netbeans.modules.form.palette.ComponentPalette;
import org.netbeans.modules.form.palette.PaletteAction;
import org.netbeans.modules.form.palette.PaletteItem;

import java.awt.*;
import java.awt.event.*;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

/* TODO:
  - visual manipulation of components adding/removing/...
  - clipboard ops.
  - undo/redo
*/

/* TO CHECK:
  - visualToRAD, componentToLayer against X2 usage
  - usage of state modes
  - locales usage
  - validation of formContainer
  - property change of ComponentInspector's explorerManager
*/

/** FormManager2 is a main managing class of one form during the design-time.
*
* @author Ian Formanek
*/
public class FormManager2 {

    // -----------------------------------------------------------------------------
    // Private properties
    private static ComponentPalette palette = ComponentPalette.getDefault ();

    private static DesignModeAction designModeAction = new DesignModeAction ();

    /** Minimal distance in pixels that must be reached on x or y axis before dragging takes place */
    public static final int MIN_DRAG_DIST = 3;

    private FormEditorSupport formEditorSupport;
    private FormDataObject formObject;
    private CodeGenerator codeGenerator;
    private EventsManager eventsManager;
    private VariablesPool variablesPool;
    private FormTopComponent formWindow;
    private RADForm radForm;
    private ArrayList nonVisList;
    private NonVisualChildren nonVisChildren;
    private ComponentContainer nonVisualsContainer;

    /**
     * @associates FormListener 
     */
    private ArrayList formListeners;

    private transient boolean initialized = false;

    // Visual Management
    transient private Container formContainer;
    /** A mapping <Component -> RADVisualComponent> */
    transient private HashMap visualToRAD = new HashMap ();
    /** mapping <JavaBean, LightweightLayer> (for Lightweight components only) 
     * @associates LightweightLayer*/
    transient private HashMap componentToLayer = new HashMap (10);

    // Selection management
    /** A mapping <RADVisualComponent -> Selection> (Selection is the component wrapper around the component) 
     * @associates Component*/
    transient private HashMap radToSelection = new HashMap ();
    transient private RADComponent[] currentSelection = new RADComponent[0];
    transient private Selection.ResizeListener rListener;
    transient private Selection.MoveListener mListener;

    // Mouse listeners
    /** A mouse listener that provides the main mouse functionality on the form */
    transient private ManagerMouseAdapter mma;
    /** A mouse listened that translates mouse events to the ManagerMouseAdapter */
    transient private MouseProcessor mouseProcessor;
    /** A mouse listened that translates mouse motion events to the ManagerMouseAdapter */
    transient private MouseMotionProcessor mouseMotionProcessor;
    /** A listener that tracks selected nodes on the explorer manager of the form nodes and visually marks the components as "selected" */ // NOI18N
    // Dragging operation context
    transient private RADComponent connectionSource;
    transient private boolean ignoreMouse = false; // in some stages prevents corrupting selected nodes whe user clicks on the form
    // while waiting for some action to complete
    transient private RADVisualComponent movingRADVisualComponent;
    transient private Point dragPoint;         // current drag point - changes during dragging
    transient private Point originalDragPoint; // the point where dragging started - does not change during dragging
    transient private Point originalHotSpot;   // the hot spot of the original mouse press (position within the dragged component)
    transient private Point originalLocation;  // the original location of the dragged component
    transient private DesignLayout dragLayout;                             // the design layout in which we are dragging
    transient private DesignLayout.ConstraintsDescription dragConstraints; // the latest constraints during the drag operation
    transient private DesignLayout.ConstraintsDescription originalDragConstraints; // the original constraints from which the drag operation started

    // Form editor states
    transient private boolean deletingNodes;
    transient private boolean addingMode;
    transient private boolean addingDragMode;
    transient private boolean resizingMode;
    transient private boolean outerMovingMode;
    transient private boolean movingMode;
    transient private boolean movingDragMode;
    transient private PaletteItem addItem;
    transient private RADVisualContainer parentRADContainer;
    /** The design/real mode of the form - see DesignLayout.REAL_MODE and DESIGN_MODE for details */
    transient private int mode = DesignLayout.DESIGN_MODE;
    /** The testMode/designTimeMode flag - in test mode, the components on the form behave as during run-time */
    transient private boolean testMode = false;

    transient private Rectangle savedFormBounds;
    transient private int savedDesignMode;

    /** Encoding for the form */
    transient private String encoding = "UTF-8";  // NOI18N //XXX(-tdt) System.getProperty("file.encoding", "ISO-8859-1");

    // -----------------------------------------------------------------------------
    // FINALIZE DEBUG METHOD

    public void finalize () throws Throwable {
        if (System.getProperty ("netbeans.debug.form.finalize") != null) {
            System.out.println("finalized: "+this.getClass ().getName ()+", instance: "+this); // NOI18N
        }
        super.finalize ();
    }

    // -----------------------------------------------------------------------------
    // Constructor and initialization

    public FormManager2 (FormDataObject formObject, RADForm radForm) {
        this.formObject = formObject;
        this.radForm = radForm;
        nonVisList = new ArrayList (10);
        formEditorSupport = formObject.getFormEditor ();
        eventsManager = new EventsManager (this);
        variablesPool = new VariablesPool (this);
        radForm.initialize (this);
    }

    public void initialize () {
        if (initialized) {
            throw new InternalError ("Form already initialized"); // NOI18N
        }

        // create mouse and selection listeners
        rListener = new SelectionResizeListener ();
        mListener = new SelectionMoveListener ();
        if (mma == null) mma = new ManagerMouseAdapter();
        mouseProcessor = new MouseProcessor (mma);
        mouseMotionProcessor = new MouseMotionProcessor (mma);

        formContainer = ((RADVisualContainer)radForm.getTopLevelComponent ()).getContainer ();

        formContainer.addMouseListener(mouseProcessor);
        formContainer.addMouseMotionListener(mouseMotionProcessor);

        // if the form top container is visual, process components on it
        if (radForm.getTopLevelComponent () instanceof RADVisualContainer) {
            getFormTopComponent ().add (radForm.getFormInfo ().getTopContainer (), java.awt.BorderLayout.CENTER);
            visualToRAD.put (formContainer, radForm.getTopLevelComponent ());
            addVisualComponentsRecursively ((RADVisualContainer)radForm.getTopLevelComponent ());
        }

        // resore menu bar on the form window
        Object menuVal = radForm.getTopLevelComponent ().getAuxValue (RADVisualFormContainer.AUX_MENU_COMPONENT);
        if ((menuVal != null) && (menuVal instanceof String) && (radForm.getTopLevelComponent () instanceof RADVisualFormContainer)) {
            ((RADVisualFormContainer)radForm.getTopLevelComponent ()).setFormMenu ((String)menuVal); // [PENDING - incorrect cast]
        }

        // enforce init of Code Generator
        getCodeGenerator ();
        initialized = true;

        fireFormLoaded ();
    }

    // -----------------------------------------------------------------------------
    // Important Public Interface

    public ComponentContainer getNonVisualsContainer () {
        if (nonVisualsContainer == null) {
            nonVisualsContainer = new ComponentContainer () {
                                      public RADComponent[] getSubBeans () {
                                          return getNonVisualComponents ();
                                      }

                                      public void initSubComponents (RADComponent[] initComponents) {
                                          initNonVisualComponents (initComponents);
                                      }

                                      public void reorderSubComponents (int[] perm) {
                                          reorderNonVisualComponents (perm);
                                      }

                                      public void add (RADComponent comp) {
                                          throw new InternalError (); // should not be used
                                      }

                                      public void remove (RADComponent comp) {
                                          throw new InternalError (); // should not be used
                                      }

                                      public int getIndexOf (RADComponent comp) {
                                          throw new InternalError (); // should not be used
                                      }
                                  };
        }
        return nonVisualsContainer;
    }

    public RADComponent[] getNonVisualComponents () {
        RADComponent[] comps = new RADComponent[nonVisList.size ()];
        nonVisList.toArray (comps);
        return comps;
    }

    public Collection getVisualComponents () {
        ArrayList visualComponents = new ArrayList ();
        visualComponents.add (getRADForm ().getTopLevelComponent ());
        addComponentsRecursively (getRADForm ().getFormContainer (), visualComponents);
        return visualComponents;
    }

    public Collection getAllComponents () {
        ArrayList allComponents = new ArrayList ();
        allComponents.add (getRADForm ().getTopLevelComponent ());
        RADComponent[] comps = getNonVisualComponents ();
        for (int i = 0; i < comps.length; i++) {
            allComponents.add (comps[i]);
            if (comps[i] instanceof ComponentContainer) addComponentsRecursively (((ComponentContainer) comps[i]), allComponents);
        }
        addComponentsRecursively (getRADForm ().getFormContainer (), allComponents);
        return allComponents;
    }

    public RADComponent findRADComponent (String name) {
        for (Iterator allComps = getAllComponents ().iterator (); allComps.hasNext (); ) {
            RADComponent comp = (RADComponent)allComps.next ();
            if (name.equals (comp.getName ())) return comp;
        }

        return null;
    }

    private void addComponentsRecursively (ComponentContainer cont, ArrayList list) {
        RADComponent[] children = cont.getSubBeans ();
        for (int i = 0; i < children.length; i++) {
            list.add (children[i]);
            if (children[i] instanceof ComponentContainer)
                addComponentsRecursively ((ComponentContainer)children[i], list);
        }
    }

    void reorderNonVisualComponents (int[] perm) {
        for (int i = 0; i < perm.length; i++) {
            int from = i;
            int to = perm[i];
            if (from == to) continue;
            Object value = nonVisList.remove (from);
            if (from < to) {
                nonVisList.add (to - 1, value);
            } else {
                nonVisList.add (to, value);
            }
        }
        fireComponentsReordered (nonVisualsContainer);
    }

    /** Used during deserialization of opened forms */
    void initFormTopComponent (FormTopComponent ftc) {
        formWindow = ftc;
    }

    public FormTopComponent getFormTopComponent () {
        if (formWindow == null) {
            formWindow = new FormTopComponent (formObject, this);
        }

        return formWindow;
    }

    /** @return The mode of the FormEditor for this form. */
    public int getMode () {
        return mode;
    }

    /** Sets the mode of the FormEditor for this form. */
    public void setMode (int value) {
        mode = value;
        if (radForm.getTopLevelComponent () instanceof RADVisualContainer) {
            setModeRecursively ((RADVisualContainer)radForm.getTopLevelComponent (), value);
            formContainer.validate ();
        }
    }

    /** Traverses the hierarchy of nodes and sets the mode on design
    * layouts of all containers.
    */
    private void setModeRecursively (RADVisualContainer cont, int mode) {
        DesignLayout dl = cont.getDesignLayout ();
        if (dl != null)
            dl.setMode (mode);
        RADVisualComponent[] children = cont.getSubComponents ();
        for (int i = 0; i < children.length; i++)
            if (children [i] instanceof RADVisualContainer)
                setModeRecursively ((RADVisualContainer)children [i], mode);
    }

    public boolean isTestMode () {
        return testMode;
    }

    private static Node[] lastSelectedNodes = null;

    public void setTestMode (boolean value) {
        if (value == testMode) return;
        testMode = value;
        if (testMode) {
            lastSelectedNodes = FormEditor.getComponentInspector ().getSelectedNodes ();
            cancelSelection ();
            java.awt.Window wnd = SwingUtilities.windowForComponent (formContainer);
            if (wnd != null) savedFormBounds = wnd.getBounds ();

            for (Iterator it = componentToLayer.values ().iterator (); it.hasNext ();) {
                ((LightweightLayer)it.next ()).setConsumeMouse (false);
            }

            savedDesignMode = getMode ();
            setMode (DesignLayout.REAL_MODE);
            designModeAction.setFormManager (this);
            if (wnd != null) wnd.pack ();
            formContainer.validate ();
        } else {
            if (lastSelectedNodes!=null) {
                try {
                    FormEditor.getComponentInspector ().setSelectedNodes (lastSelectedNodes, this);
                } catch (java.beans.PropertyVetoException e){
                    e.printStackTrace();
                }
            }
            for (Iterator it = componentToLayer.values ().iterator (); it.hasNext ();) {
                ((LightweightLayer)it.next ()).setConsumeMouse (true);
            }
            setMode (savedDesignMode);

            // restore state before test mode
            designModeAction.setFormManager (this);
            if (savedFormBounds != null) {
                java.awt.Window wnd = SwingUtilities.windowForComponent (formContainer);
                if (wnd != null) wnd.setBounds (savedFormBounds);
            }

            formContainer.validate ();
        }
    }

    public FormDataObject getFormObject () {
        return formObject;
    }

    public RADForm getRADForm () {
        return radForm;
    }

    public boolean isInitialized () {
        return initialized;
    }

    // -----------------------------------------------------------------------------
    // Other and package-private Interface

    FormEditorSupport getFormEditorSupport () {
        return formEditorSupport;
    }

    EventsManager getEventsManager () {
        return eventsManager;
    }

    NonVisualChildren getNonVisualChildren () {
        if (nonVisChildren == null) {
            nonVisChildren = new NonVisualChildren (this);
        }
        return nonVisChildren;
    }

    void initNonVisualComponents (RADComponent[] comps) {
        for (int i = 0; i < comps.length; i++) {
            nonVisList.add (comps[i]);
        }
    }

    /** Getter for property encoding.
     *@return Value of property encoding.
     */
    String getEncoding() {
        return encoding;
    }

    /** Setter for property encoding.
     *@param encoding New value of property encoding.
     */
    void setEncoding(String encoding) {
        Object oldValue = this.encoding;
        this.encoding = encoding;
        fireComponentChanged ((RADComponent) radForm.getFormContainer () , "encoding", oldValue, encoding); // NOI18N
    }

    // -----------------------------------------------------------------------------
    // Visual management

    public Component getVisualRepresentation (RADComponent radComp) {
        if (radComp instanceof RADVisualComponent) {
            Component comp = (Component)radToSelection.get (radComp);
            if (comp == null) {
                return ((RADVisualComponent)radComp).getComponent (); // [PENDING - this should not be necessary]
            }
            return comp;
        } else {
            JButton button = new JButton ();
            //button.setIcon (radComp.getIcon ()); // [PENDING - icon]
            return button;
        }
    }

    // -----------------------------------------------------------------------------
    // Form Listener operations

    public synchronized void addFormListener (FormListener l) {
        if (formListeners == null) {
            formListeners = new ArrayList ();
        }

        formListeners.add (l);
    }

    public synchronized void removeFormListener (FormListener l) {
        if (formListeners == null) {
            return;
        }

        formListeners.remove (l);
    }

    /** fire the info about that the form is loaded and fully initialized */
    final protected synchronized void fireFormLoaded () {
        if (!initialized) return;
        ArrayList listeners = (ArrayList)formListeners.clone ();
        for (Iterator it = listeners.iterator (); it.hasNext ();) {
            ((FormListener)it.next ()).formLoaded ();
        }
    }

    /** fire the info about that the form is about to be saved */
    final protected synchronized void fireFormToBeSaved () {
        if (!initialized) return;
        ArrayList listeners = (ArrayList)formListeners.clone ();
        for (Iterator it = listeners.iterator (); it.hasNext ();) {
            ((FormListener)it.next ()).formToBeSaved ();
        }
    }

    /** fire change that should lead to regeneration of both initializer and variables declaration */
    final protected synchronized void fireFormChange () {
        if (!initialized) return;
        ArrayList listeners = (ArrayList)formListeners.clone ();
        for (Iterator it = listeners.iterator (); it.hasNext ();) {
            ((FormListener)it.next ()).formChanged ();
        }
    }

    final protected synchronized void fireCodeChange () {
        if (!initialized) return;
        ArrayList listeners = (ArrayList)formListeners.clone ();
        for (Iterator it = listeners.iterator (); it.hasNext ();) {
            ((FormListener)it.next ()).codeChanged ();
        }
    }

    /** fire change that should lead to regeneration of initializer only */
    final protected synchronized void fireComponentsReordered (ComponentContainer cont) {
        if (!initialized) return;
        ArrayList listeners = (ArrayList)formListeners.clone ();
        for (Iterator it = listeners.iterator (); it.hasNext ();) {
            ((FormListener)it.next ()).componentsReordered (cont);
        }
    }

    final protected synchronized void fireComponentsAdded (RADComponent[] comps) {
        if (!initialized) return;
        if (formListeners == null) return;
        ArrayList listeners = (ArrayList)formListeners.clone ();
        for (Iterator it = listeners.iterator (); it.hasNext ();) {
            ((FormListener)it.next ()).componentsAdded (comps);
        }
    }

    final protected synchronized void fireComponentsRemoved (RADComponent[] comps) {
        if (!initialized) return;
        if (formListeners == null) return;
        ArrayList listeners = (ArrayList)formListeners.clone ();
        for (Iterator it = listeners.iterator (); it.hasNext ();) {
            ((FormListener)it.next ()).componentsRemoved (comps);
        }
    }

    final protected synchronized void fireComponentChanged (RADComponent comp, String propertyName, Object oldValue, Object newValue) {
        if (!initialized) return;
        if (formListeners == null) return;
        FormPropertyEvent evt = new FormPropertyEvent (comp, propertyName, oldValue, newValue);
        ArrayList listeners = (ArrayList)formListeners.clone ();
        for (Iterator it = listeners.iterator (); it.hasNext ();) {
            ((FormListener)it.next ()).componentChanged (evt);
        }
    }

    /** Fire layout property change event change. This is either change of layout property or
    * constraint property of some component in the layout.
    * @param cont The container on which layout the change occured
    * @param comp The component, which constraints has changed or null if layout property has changed
    * @param propName name of the changed property
    * @param oldVal the old value of the property
    * @param newVal the new value of the property
    */
    final public synchronized void fireLayoutChanged (RADVisualContainer cont, RADVisualComponent comp, String propName, Object oldVal, Object newVal) {
        if (formListeners == null) return;
        ArrayList listeners = (ArrayList)formListeners.clone ();
        for (Iterator it = listeners.iterator (); it.hasNext ();) {
            ((FormListener)it.next ()).layoutChanged (cont, comp, propName, oldVal, newVal);
        }
    }

    final protected synchronized void firePropertyChanged (RADComponent comp, String propertyName, Object oldValue, Object newValue) {
        if (!initialized) return;
        if (formListeners == null) return;
        FormPropertyEvent evt = new FormPropertyEvent (comp, propertyName, oldValue, newValue);
        ArrayList listeners = (ArrayList)formListeners.clone ();
        for (Iterator it = listeners.iterator (); it.hasNext ();) {
            ((FormListener)it.next ()).propertyChanged (evt);
        }
    }

    final protected synchronized void fireEventAdded (RADComponent comp, EventsManager.EventHandler event) {
        if (!initialized) return;
        if (formListeners == null) return;
        FormEventEvent evt = new FormEventEvent (comp, event);
        ArrayList listeners = (ArrayList)formListeners.clone ();
        for (Iterator it = listeners.iterator (); it.hasNext ();) {
            ((FormListener)it.next ()).eventAdded (evt);
        }
    }

    final protected synchronized void fireEventRemoved (RADComponent comp, EventsManager.EventHandler event) {
        if (!initialized) return;
        if (formListeners == null) return;
        FormEventEvent evt = new FormEventEvent (comp, event);
        ArrayList listeners = (ArrayList)formListeners.clone ();
        for (Iterator it = listeners.iterator (); it.hasNext ();) {
            ((FormListener)it.next ()).eventRemoved (evt);
        }
    }

    final protected synchronized void fireEventRenamed (RADComponent comp, EventsManager.EventHandler event, String oldName) {
        if (!initialized) return;
        if (formListeners == null) return;
        FormEventEvent evt = new FormEventEvent (comp, event);
        ArrayList listeners = (ArrayList)formListeners.clone ();
        for (Iterator it = listeners.iterator (); it.hasNext ();) {
            ((FormListener)it.next ()).eventRenamed (evt);
        }
    }

    // -----------------------------------------------------------------------------
    // Code generator

    public CodeGenerator getCodeGenerator () {
        if (codeGenerator == null) {
            codeGenerator = new JavaCodeGenerator (); // Java is default
            codeGenerator.initialize (this);
        }

        return codeGenerator;
    }

    public void setCodeGenerator (CodeGenerator codeGenerator) {
        this.codeGenerator = codeGenerator;
        // [PENDING - fire change, regenerate]
    }

    public VariablesPool getVariablesPool () {
        return variablesPool;
    }

    // -----------------------------------------------------------------------------
    // Performing methods

    /** Sets a design layout represented by the specified palette node on the given container.
    * @param cont the container to set the layout on
    * @param layout Paletteitem representing the design layout to set
    */
    public void setDesignLayout (RADVisualContainer cont, PaletteItem layout) {
        DesignLayout layoutInstance = null;
        try {
            layoutInstance = (DesignLayout) layout.createInstance ();
        } catch (Exception e) {
            String message = MessageFormat.format(FormEditor.getFormBundle().getString("FMT_ERR_LayoutInit"),
                                                  new Object [] { layout.getItemClass().getName(), e.getClass().getName() });
            TopManager.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
            return;
        }
        cancelSelection (); // clear the current selection
        DesignLayout current = cont.getDesignLayout ();
        cont.setDesignLayout (layoutInstance);

        if (cont.getNodeReference () != null) {
            // it can be null during init
            Node[] childrenNodes = ((RADChildren)cont.getNodeReference ().getChildren ()).getNodes ();

            if (childrenNodes != null) {
                Node layoutNode = null;
                if ((cont instanceof FormContainer) && (childrenNodes.length > 0)) {
                    layoutNode = childrenNodes[1];
                } else if (childrenNodes.length > 0) {
                    layoutNode =  childrenNodes[0]; // [PENDING IAN - ugly patch !!! - on Form nodes, the layout is the second child]
                }
                if ((layoutNode != null) && (layoutNode instanceof RADLayoutNode)) {
                    ((RADLayoutNode)layoutNode).updateState ();
                }
            }
        }

        fireCodeChange ();
    }

    public void addMouseAdaptersForAdditional (Component comp, RADVisualContainer parent) {
        if (mma == null) mma = new ManagerMouseAdapter ();
        comp.addMouseListener (new MouseProcessor (mma, parent.getContainer ()));
        comp.addMouseMotionListener (new MouseMotionProcessor (mma, parent.getContainer ()));
    }

    /** Check if the names of all subcomponents are valid variables.
        This check must be done for cut/paste between two different forms since
        names of pasted components (valid in form from which they were cutted) 
        can be already used in target form. */
    private void checkComponentNames (RADComponent comp) {
        comp.useStoredName ();
        if (comp instanceof ComponentContainer) {
            RADComponent comps[] = ((ComponentContainer) comp).getSubBeans ();
            for (int i=0, n=comps.length; i<n; i++) {
                checkComponentNames (comps[i]);
            }
        }
    }

    void addVisualComponent (
        RADVisualComponent comp,
        RADVisualContainer parentContainer,
        DesignLayout.ConstraintsDescription constraints)
    {
        Component finalAddingComponent = comp.getComponent ();
        boolean isContainer = comp instanceof RADVisualContainer;
        // we are not doing LightWeight layer over heavyweight components
        // and over lightweight containers
        if (!FormUtils.isHeavyweight(finalAddingComponent)) {
            if (!isContainer) {
                LightweightLayer layer = new LightweightLayer(finalAddingComponent);
                componentToLayer.put(finalAddingComponent, layer);
                finalAddingComponent = layer;
            }
            finalAddingComponent.addMouseListener(mouseProcessor);
            finalAddingComponent.addMouseMotionListener(mouseMotionProcessor);
            finalAddingComponent = new JSelectionLayer (comp, finalAddingComponent, rListener, mListener);
            if (isContainer) {
                Container cont = (Container) comp.getComponent ();
                addMouseAdapters (comp, cont, cont);
            }
        } else {
            finalAddingComponent.addMouseListener(mouseProcessor);
            finalAddingComponent.addMouseMotionListener(mouseMotionProcessor);

            if (finalAddingComponent instanceof Container)
                addMouseAdapters (comp, (Container)finalAddingComponent, finalAddingComponent);

            finalAddingComponent = new SelectionLayer (comp, finalAddingComponent, rListener, mListener);
        }

        //comp.addPropertyChangeListener(regenerationListener);


        // if the comp and subcomponents have a valid variable name stored then use it
        checkComponentNames (comp);

        visualToRAD.put(comp.getComponent (), comp);
        radToSelection.put (comp, finalAddingComponent);

        parentContainer.add (comp);

        DesignLayout designLayout = parentContainer.getDesignLayout (); // assured that the layout is not null
        if (constraints != null)
            designLayout.addComponent (comp, constraints);
        else
            designLayout.addComponent (comp);

        // [PENDING - patch to make JInternalFrames appear under JDK 1.3]
        if (comp.getBeanInstance () instanceof javax.swing.JInternalFrame) {
            ((javax.swing.JInternalFrame)comp.getBeanInstance ()).setVisible (true);
            comp.setAuxValue (JavaCodeGenerator.AUX_CREATE_CODE_POST, comp.getName ()+".setVisible (true);"); // NOI18N
        }

        fireComponentsAdded (new RADComponent[] { comp });
    }

    void addNonVisualComponent (RADComponent comp, ComponentContainer parentContainer) {

        // if the comp has a valid stored variable name, not reserved by other component, reuse it
        //    String storedName = comp.getStoredName ();
        //    if ((storedName == null) || (variablesPool.findVariable (storedName) != null))
        comp.setName(variablesPool.getNewName (comp.getBeanClass ()));
        //    else
        //      comp.setName (storedName);
        if (parentContainer == null) {
            nonVisList.add (comp);
            getNonVisualChildren ().updateKeys ();
        } else {
            parentContainer.add (comp);
        }

        fireComponentsAdded (new RADComponent[] { comp });
    }

    private void deleteVariables (RADComponent comp) {
        variablesPool.deleteVariable (comp.getName ());
        if (comp instanceof ComponentContainer) {
            RADComponent comps[] = ((ComponentContainer) comp).getSubBeans ();
            for (int i=0, n=comps.length; i<n; i++) {
                deleteVariables (comps[i]);
            }
        }
    }

    void deleteComponent (RADComponent comp) {
        String compName = comp.getName ();
        if (comp instanceof RADVisualComponent) {
            RADVisualComponent vcomp = (RADVisualComponent)comp;
            vcomp.getParentContainer ().remove (vcomp);
            visualToRAD.remove (vcomp.getComponent ());

            Component layer = (Component)componentToLayer.remove (vcomp.getComponent ());
            if (layer != null) {
                layer.removeMouseListener (mouseProcessor);
                layer.removeMouseMotionListener (mouseMotionProcessor);
            } else {
                vcomp.getComponent ().removeMouseListener (mouseProcessor);
                vcomp.getComponent ().removeMouseMotionListener (mouseMotionProcessor);
            }
            radToSelection.remove (vcomp);

        } else if (comp instanceof RADMenuItemComponent) {
            RADMenuItemComponent mcomp = (RADMenuItemComponent)comp;
            if (mcomp.getParentMenu () == null) { // top-level menu

                nonVisList.remove (comp);
                getNonVisualChildren ().updateKeys ();

                // if removing menu currently used as form's main menu, remove it
                if (comp instanceof RADMenuComponent) {
                    if ((((RADVisualFormContainer)radForm.getTopLevelComponent ()).getFormMenu () != null) &&
                            (((RADVisualFormContainer)radForm.getTopLevelComponent ()).getFormMenu ().equals (comp.getName ()))) {
                        ((RADVisualFormContainer)radForm.getTopLevelComponent ()).setFormMenu (null);
                    }
                }
            } else {
                mcomp.getParentMenu ().remove (mcomp);
            }
        } else {
            // deleting non-visual component
            nonVisList.remove (comp);
            getNonVisualChildren ().updateKeys ();
        }

        // delete attached events
        EventsList.EventSet[] eventSets = comp.getEventsList ().getEventSets ();
        for (int i = 0; i < eventSets.length; i++) {
            EventsList.Event[] events = eventSets[i].getEvents();
            for (int j = 0; j < events.length; j++) {
                if (events[j].getHandlers ().size () > 0) {
                    eventsManager.removeEventHandler (events[j]);
                }
            }
        }

        // release name from variables pool (including subcomponents)
        deleteVariables (comp);

        fireComponentsRemoved (new RADComponent[] { comp });

        formWindow.invalidate ();
        formWindow.validate ();
        formWindow.repaint (); // because of lightweight components
    }

    // -----------------------------------------------------------------------------
    // Mouse Listeners

    private void addMouseAdaptersRecursively (Component comp, Component parent, Map table) {
        comp.addMouseListener (new MouseProcessor (mma, parent));
        comp.addMouseMotionListener (new MouseMotionProcessor (mma, parent));
        if (comp instanceof Container) {
            Component[] children = ((Container)comp).getComponents ();
            for (int i = 0; i < children.length; i++) {
                if (table.get (children[i]) == null)
                    addMouseAdaptersRecursively (children[i], parent, table);
            }
        }
    }

    private void addMouseAdapters (RADVisualComponent node, Container cont, Component parent) {
        // for the case, where the container was pasted with some RADVisualComponents inside,
        // we cannot do the next step for these subcomponents, so we build a hashtable
        // of "correct" // NOI18N
        HashMap notDiveInto = new HashMap ();

        if (node instanceof RADVisualContainer) {
            RADVisualComponent[] subComps = ((RADVisualContainer)node).getSubComponents ();
            for (int i = 0; i < subComps.length; i++) {
                Component comp = getVisualRepresentation (subComps[i]);
                notDiveInto.put (comp, comp);
            }
        }

        Component[] children = cont.getComponents ();
        // attach a ComponentListener to the Container
        for (int i = 0; i < children.length; i++)  {
            if (notDiveInto.get (children [i]) == null)
                addMouseAdaptersRecursively (children[i], parent, notDiveInto);
        }
    }

    /** Translates specified event so that the source component of the new
    * event is the original event's source component's parent and the coordinates
    * of the event are translated appropriately to the parent's coordinate space.
    * @param evt the MouseEvent to be translated
    * @exception IllegalArgumentException is thrown if the source component
    *    of the MouseEvent is not a subcomponent of given parent component
    */
    private MouseEvent createParentEvent(MouseEvent evt, Component parent) {
        if (parent == null)
            return evt;
        Component comp = evt.getComponent();
        while (!parent.equals(comp)) {
            if (comp instanceof JComponent)
                evt.translatePoint (((JComponent)comp).getX (), ((JComponent)comp).getY ());
            else {
                Rectangle bounds = comp.getBounds ();
                evt.translatePoint (bounds.x, bounds.y);
            }
            comp = comp.getParent();
            if (comp == null) {
                System.err.println("Component: "+evt.getSource ()+" is not under its parent's container: "+parent); // NOI18N
                break;
            }
        }

        return new MouseEvent(
                   parent,
                   evt.getID(),
                   evt.getWhen(),
                   evt.getModifiers(),
                   evt.getX(),
                   evt.getY(),
                   evt.getClickCount(),
                   evt.isPopupTrigger()
               );
    }

    /** The MouseProcessor is a MouseListener that changes the
    * MouseEvent events in the listener methods so that the source component
    * of the event is the MouseEvent source component's parent and
    * the position is changed accordingly to the parent coordinate space.
    * The translated MouseEvent is then passed to the listener specified in
    * the constructor.
    */
    class MouseProcessor implements MouseListener {
        /** The listener that receives the redirected event */
        private MouseListener mouseListener;

        /** The Component parent to which the event should be changed -
        * the event then appears as if it originated on that parent Component and
        * the coordinates are changed accordingly.
        * The MouseEnter and MouseExit are silently consumed if not originated at
        * the final Component recipient
        */
        private Component translateParent;

        MouseProcessor(MouseListener listener) {
            this (listener, null);
        }

        MouseProcessor(MouseListener listener, Component translateParent) {
            mouseListener = listener;
            this.translateParent = translateParent;
        }

        public void mousePressed(MouseEvent evt) {
            if (!testMode) {
                if (translateParent != null)
                    evt = createParentEvent (evt, translateParent);
                evt.consume();
                mouseListener.mousePressed(evt);
            }
        }
        public void mouseReleased(MouseEvent evt) {
            if (!testMode) {
                if (translateParent != null)
                    evt = createParentEvent (evt, translateParent);
                evt.consume();
                mouseListener.mouseReleased(evt);
            }
        }
        public void mouseClicked(MouseEvent evt) {
            if (!testMode) {
                if (translateParent != null)
                    evt = createParentEvent (evt, translateParent);
                evt.consume();
                mouseListener.mouseClicked(evt);
            }
        }
        public void mouseEntered(MouseEvent evt) {
            if (!testMode) {
                evt.consume();
                if (translateParent == null)
                    mouseListener.mouseEntered(evt);
            }
        }
        public void mouseExited(MouseEvent evt) {
            if (!testMode) {
                evt.consume();
                if (translateParent == null)
                    mouseListener.mouseExited(evt);
            }
        }
    }

    /** The MouseMotionProcessor is a MouseMotionListener that changes the
    * MouseMotionEvent events in the listener methods so that the source component
    * of the event is the MouseMotionEvent source component's parent and
    * the position is changed accordingly to the parent coordinate space.
    * The translated MouseMotionEvent is then passed to the listener specified in
    * the constructor.
    */
    class MouseMotionProcessor implements MouseMotionListener {
        /** The listener that receives the redirected event */
        private MouseMotionListener mouseMotionListener;
        /** The Component parent to which the event should be changed -
        * the event then appears as if it originated on that parent Component and
        * the coordinates are changed accordingly.
        */
        private Component translateParent;

        MouseMotionProcessor(MouseMotionListener listener) {
            this (listener, null);
        }

        MouseMotionProcessor(MouseMotionListener listener, Component translateParent) {
            mouseMotionListener = listener;
            this.translateParent = translateParent;
        }

        public void mouseMoved(MouseEvent evt) {
            if (!testMode) {
                if (translateParent != null)
                    evt = createParentEvent (evt, translateParent);
                evt.consume();
                mouseMotionListener.mouseMoved(evt);
            }
        }

        public void mouseDragged(MouseEvent evt) {
            if (!testMode) {
                if (translateParent != null)
                    evt = createParentEvent (evt, translateParent);
                evt.consume();
                mouseMotionListener.mouseDragged(evt);
            }
        }
    }

    static Point recalculatePoint (MouseEvent evt) {
        Point p = evt.getPoint ();
        Component comp = evt.getComponent ();
        Point loc = comp.getLocation ();
        Component parent = comp.getParent ();
        while (parent != null) {
            p.x += loc.x;
            p.y += loc.y;
            loc = parent.getLocation ();
            parent = parent.getParent ();
        }
        return p;
    }

    /** @return a real location of the component represented by the specified RADVisualComponent.
    * This skips lightweight layer and/or the selection layer to provide a real location
    * within its parent.
    */
    private Point getRealLocation (RADVisualComponent radComp) {
        Component comp = radComp.getComponent ().getParent ();
        if (comp instanceof LightweightLayer)
            comp = comp.getParent ();
        return comp.getLocation ();
    }

    class ManagerMouseAdapter implements MouseListener, MouseMotionListener {
        public void mousePressed(MouseEvent evt) {
            if (ignoreMouse) return;
            // 1. filter out cancelling presses
            // if in adding & right mouse button pressed ==>> cancel adding
            if (addingMode && (
                        ((evt.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) ||
                        ((evt.getModifiers() & MouseEvent.BUTTON2_MASK) != 0))) {
                addingMode = false;
                addingDragMode = false;
                dragLayout.markResizeTo (null);
                TopManager.getDefault().setStatusText(""); // NOI18N
                return;
            }

            // if in moving & right mouse button pressed ==>> cancel moving
            if (movingMode && (
                        ((evt.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) ||
                        ((evt.getModifiers() & MouseEvent.BUTTON2_MASK) != 0))) {
                movingDragMode = false;
                movingMode = false;
                RADVisualContainer parent = movingRADVisualComponent.getParentContainer();
                parent.getDesignLayout().markMoveTo (movingRADVisualComponent, null);  // assured that the layout is not null
                TopManager.getDefault().setStatusText(""); // NOI18N
                return;
            }

            // 2. initialize various informations
            Component clickedComponent = evt.getComponent();
            int x = evt.getX();
            int y = evt.getY();
            Dimension size = clickedComponent.getSize();

            // we must filter out clicking on window's insets
            if (clickedComponent instanceof Window) {
                Insets insets = ((Container)clickedComponent).getInsets();
                size.width -= insets.left + insets.right;
                size.height -= insets.top + insets.bottom;
                x -= insets.left;
                y -= insets.top;
            }
            if ((x < 0) || (y < 0)) return;
            if ((x >= size.width) || (y >= size.height)) return;


            if (evt.getComponent () instanceof Container) {
                Insets insets = ((Container)(evt.getComponent ())).getInsets ();
                evt.translatePoint (-insets.left, -insets.top);
            }

            addItem = palette.getSelectedItem ();
            int paletteMode = palette.getMode ();

            // find the RADVisualComponent for the clicked component
            RADVisualComponent clickedRADVisualComponent = (RADVisualComponent)visualToRAD.get(clickedComponent);
            if (clickedRADVisualComponent == null)
                throw new InternalError("RAD Node Null"); // NOI18N

            // 3. displaying context menu on right click
            if (MouseUtils.isRightMouseButton (evt)) {
                // [PENDING - do not uncomment until it works properly]
                int correctPos = 0;
                if (!isSelected (clickedRADVisualComponent)) {
                    boolean multiple = ((evt.getModifiers() & MouseEvent.CTRL_MASK) != 0);
                    selectComponent (clickedRADVisualComponent, multiple);
                    int borderSize = FormEditor.getFormSettings ().getSelectionBorderSize ();

                    // [PENDING] does not work well on BorderLayout, ...
                    if (!(clickedRADVisualComponent instanceof FormContainer)) {
                        correctPos = borderSize;
                    }
                }

                Node[] selNodes = FormEditor.getComponentInspector ().getSelectedNodes();

                JPopupMenu popup = NodeOp.findContextMenu(selNodes);
                popup.setLightWeightPopupEnabled (false);
                if (popup != null) {
                    Component popupComponent = clickedComponent;
                    Point popupPos = evt.getPoint ();
                    Rectangle r;
                    if (popupComponent instanceof Container) {
                        Insets insets = ((Container)popupComponent).getInsets ();
                        popupPos.x = popupPos.x + insets.left - correctPos;
                        popupPos.y = popupPos.y + insets.top - correctPos;
                    }
                    while (popupComponent.getParent () != null) {
                        if (popupComponent instanceof JComponent) {
                            popupPos.x += ((JComponent)popupComponent).getX();
                            popupPos.y += ((JComponent)popupComponent).getY();
                        } else {
                            r = popupComponent.getBounds ();
                            popupPos.x += r.x;
                            popupPos.y += r.y;
                        }
                        popupComponent = popupComponent.getParent ();
                    }
                    formWindow.displayedPopup = popup;
                    popup.show(popupComponent, popupPos.x - 2, popupPos.y - 2);
                }
                return;
            }

            // 4. Selection mode
            if ((paletteMode == PaletteAction.MODE_SELECTION) || (paletteMode == PaletteAction.MODE_CONNECTION)) {
                // possibly clear previously started connection (which should not be kept anymore)
                if (paletteMode == PaletteAction.MODE_SELECTION) {
                    connectionSource = null;
                }
                // do default action only in selection mode
                if (MouseUtils.isDoubleClick(evt) && (paletteMode != PaletteAction.MODE_CONNECTION)) {
                    SystemAction defaultAction = clickedRADVisualComponent.getNodeReference ().getDefaultAction ();
                    if ((defaultAction != null) && (defaultAction.isEnabled ())) {
                        defaultAction.actionPerformed (new ActionEvent (
                                                           formWindow, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                    }
                    return;
                }
                boolean multiple = ((evt.getModifiers() & MouseEvent.CTRL_MASK) != 0);
                selectComponent (clickedRADVisualComponent, multiple);

                // if left mouse pressed and not multiple selection -> start selection drag
                // (the right-click is selection-only to prevent unwanted dragging)
                // we do not start dragging if in CONNECTION_MODE
                if (!multiple && ((evt.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) &&
                        (paletteMode != PaletteAction.MODE_CONNECTION)) {

                    // special patch - on ScrollBar, the mouseDrag and mouseReleased will not come
                    if (clickedRADVisualComponent.getComponent () instanceof java.awt.Scrollbar)
                        return;

                    RADVisualContainer parent = clickedRADVisualComponent.getParentContainer();
                    if (parent != null) {
                        dragLayout = parent.getDesignLayout ();  // assured that the layout is not null
                        if (dragLayout.canMove()) {
                            dragConstraints = (DesignLayout.ConstraintsDescription)clickedRADVisualComponent.getConstraints(parent.getDesignLayout().getClass());  // assured that the layout is not null
                            originalDragConstraints = dragConstraints;
                            movingRADVisualComponent = clickedRADVisualComponent;
                            movingMode = true;
                            dragPoint = evt.getPoint();
                            originalLocation = getRealLocation (clickedRADVisualComponent);
                            originalHotSpot = evt.getPoint ();
                            originalDragPoint = recalculatePoint (evt);
                        }
                    }
                }
                return;
            }
            // 5. Add mode - addItem is the palette representation of the component to add
            // possibly clear previously started connection (which should not be kept anymore)
            connectionSource = null;

            // clear the Palette's selected component if the shift was not hold
            if ((evt.getModifiers() & MouseEvent.SHIFT_MASK) == 0)
                palette.setMode (PaletteAction.MODE_SELECTION);

            // Find the RADVisualComponent for the clicked component and the clicked container
            // (if the clicked component is not a container, it is its parent)
            if (clickedRADVisualComponent instanceof RADVisualContainer)
                parentRADContainer = (RADVisualContainer)clickedRADVisualComponent;
            else
                parentRADContainer = clickedRADVisualComponent.getParentContainer();

            // a. adding design layout
            if (addItem.isDesignLayout()) {
                DesignLayout currentLayout = parentRADContainer.getDesignLayout ();
                // changing layout on containers with support layouts is not allowed
                if ((currentLayout != null) && (!(currentLayout instanceof DesignSupportLayout)))
                    setDesignLayout (parentRADContainer, addItem);
                return;
            }

            // b. design border
            if (addItem.isBorder()) {
                if (clickedRADVisualComponent.getComponent() instanceof JComponent) {
                    try {
                        JComponent borderComp = (JComponent)clickedRADVisualComponent.getComponent();
                        Object oldBorder = borderComp.getBorder ();
                        javax.swing.border.Border newBorder = addItem.createBorder ();
                        RADComponent.RADProperty prop = clickedRADVisualComponent.getPropertyByName ("border");
                        if (prop == null) {
                            return;
                        }
                        prop.setValue (newBorder);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace ();
                        // [PENDING]
                    } catch (IllegalAccessException e) {
                        e.printStackTrace ();
                        // [PENDING]
                    } catch (InstantiationException e) {
                        e.printStackTrace ();
                        // [PENDING]
                    } catch (java.lang.reflect.InvocationTargetException e) {
                        e.printStackTrace ();
                        // [PENDING]
                    }
                    selectComponent (clickedRADVisualComponent, false);

                    // [PENDING - open the border editor]
                }
                return;
            }

            // c. adding MenuBar

            if (addItem.isMenu()) {
                RADMenuComponent newMenuComp = new RADMenuComponent ();
                newMenuComp.initialize (FormManager2.this);
                newMenuComp.setComponent (addItem.getItemClass ());
                newMenuComp.initSubComponents (new RADComponent[0]);
                addNonVisualComponent (newMenuComp, null);
                // for some components, we initialize their properties with some non-default values
                // e.g. a label on buttons, checkboxes
                FormEditor.defaultMenuInit (newMenuComp);

                org.openide.util.datatransfer.NewType[] newTypes = newMenuComp.getNewTypes();
                if (newTypes.length != 0) {
                    try {
                        newTypes[0].create();
                    } catch (java.io.IOException e) {
                    }
                }

                if (((radForm.getFormInfo () instanceof JMenuBarContainer) && (javax.swing.JMenuBar.class.isAssignableFrom (addItem.getItemClass ())))
                        || ((radForm.getFormInfo () instanceof MenuBarContainer) && (java.awt.MenuBar.class.isAssignableFrom (addItem.getItemClass ())))) {
                    if (((RADVisualFormContainer)radForm.getTopLevelComponent ()).getFormMenu () == null) {
                        ((RADVisualFormContainer)radForm.getTopLevelComponent ()).setFormMenu (newMenuComp.getName ());
                    }
                }

                selectComponent (newMenuComp, false);
                return;
            }

            // d. adding nonvisual JavaBean
            if (!addItem.isVisual()) {
                RADComponent newNonVisualComp = new RADComponent ();
                newNonVisualComp.initialize (FormManager2.this);
                newNonVisualComp.setComponent (addItem.getItemClass ());
                addNonVisualComponent (newNonVisualComp, null);
                selectComponent (newNonVisualComp, false);
                return;
            }

            // e. adding visual JavaBean - start addingMode
            //    the final adding is done in mouseReleased
            // create a new instance of the bean to add
            dragLayout = parentRADContainer.getDesignLayout();
            if (dragLayout != null) {
                addingMode = true;
                if (!(clickedRADVisualComponent instanceof RADVisualContainer)) {
                    evt = createParentEvent (evt, parentRADContainer.getContainer ()); // [PENDINGchange]
                }

                originalDragPoint = evt.getPoint();
                dragConstraints = dragLayout.getConstraintsDescription(originalDragPoint);
            }

        }

        public void mouseReleased(MouseEvent evt) {
            if (evt.getComponent () instanceof Container) {
                Insets insets = ((Container)(evt.getComponent ())).getInsets ();
                evt.translatePoint (-insets.left, -insets.top);
            }

            if (movingMode) {
                if (movingDragMode) {
                    DesignLayout dLayout = movingRADVisualComponent.getParentContainer().getDesignLayout ();  // assured that the layout is not null
                    dLayout.markMoveTo (movingRADVisualComponent, null);
                    if (!dragConstraints.equals (originalDragConstraints)) {
                        dLayout.removeComponent(movingRADVisualComponent);
                        dLayout.addComponent (movingRADVisualComponent, dragConstraints);
                        movingRADVisualComponent.getNodeReference ().notifyPropertiesChange ();
                        formContainer.validate();
                        // regenerate the code to reflect the change
                        fireCodeChange ();
                    }
                }
                movingMode = false;
                movingDragMode = false;
                // clear the visual feedback of the move operation
                TopManager.getDefault().setStatusText(""); // NOI18N
            }
            else if (addingMode) {
                addingMode = false;
                addingDragMode = false;

                // clear the visual feedback of the adding operation
                dragLayout.markResizeTo (null);
                TopManager.getDefault().setStatusText(""); // NOI18N
                // dragPoint      the point where the original click occured
                // newRADVisualComponent       the RADVisualComponent for the new component

                // we are not doing a LightweightLayer over LightweightContainers [PENDING]
                RADVisualComponent newRADVisualComponent = null;

                DesignLayout dl = FormEditor.findDesignLayout (addItem);
                if (addItem.isContainer() && (dl != null)) {
                    newRADVisualComponent = new RADVisualContainer();
                    newRADVisualComponent.initialize (FormManager2.this);
                    //XXX(-tdt) newRADVisualComponent.setComponent (addItem.getItemClass ()); // [PENDING - how about serialized prototypes and using createInstance on the PaletteItem]
                    try {
                        Object comp = addItem.createInstance();
                        newRADVisualComponent.setInstance(comp);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return;
                    }

                    ((RADVisualContainer)newRADVisualComponent).initSubComponents (new RADComponent[0]);
                    ((RADVisualContainer)newRADVisualComponent).setDesignLayout (dl);
                }
                else {
                    newRADVisualComponent = new RADVisualComponent();
                    newRADVisualComponent.initialize (FormManager2.this);
                    // XXX(-tdt) newRADVisualComponent.setComponent (addItem.getItemClass ()); // [PENDING - how about serialized prototypes and using createInstance on the PaletteItem]

                    try {
                        Object comp = addItem.createInstance();
                        newRADVisualComponent.setInstance(comp);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return;
                    }
                }

                addVisualComponent (newRADVisualComponent, parentRADContainer, dragConstraints);

                // for some components, we initialize their properties with some non-default values
                // e.g. a label on buttons, checkboxes
                FormEditor.defaultComponentInit (newRADVisualComponent);
                selectComponent (newRADVisualComponent, false);
                formWindow.validate();
                fireCodeChange ();
            }
        }

        public void mouseClicked(MouseEvent evt) {}
        public void mouseEntered(MouseEvent evt) {}
        public void mouseExited(MouseEvent evt) {}

        public void mouseMoved(MouseEvent evt) {
            if (ignoreMouse) return;
            if (evt.getComponent () instanceof Container) {
                Insets insets = ((Container)(evt.getComponent ())).getInsets ();
                evt.translatePoint (-insets.left, -insets.top);
            }

            if (palette.getMode () == PaletteAction.MODE_CONNECTION) {
                RADVisualComponent clickedRADVisualComponent = (RADVisualComponent)visualToRAD.get(evt.getSource());
                if (clickedRADVisualComponent == null)
                    return;
                if (connectionSource != null) { // connecting the target
                    TopManager.getDefault().setStatusText(MessageFormat.format (
                                                              FormEditor.getFormBundle ().getString ("FMT_MSG_ConnectionTarget"),
                                                              new Object[] {
                                                                  connectionSource.getName (),
                                                                  clickedRADVisualComponent.getName ()
                                                              }
                                                          )
                                                         );
                } else {
                    TopManager.getDefault().setStatusText(MessageFormat.format (
                                                              FormEditor.getFormBundle ().getString ("FMT_MSG_ConnectionSource"),
                                                              new Object[] {
                                                                  clickedRADVisualComponent.getName ()
                                                              }
                                                          )
                                                         );
                }
            } else if (!(addingMode || movingMode) &&
                       (palette.getMode () == PaletteAction.MODE_ADD))
            {
                RADVisualComponent clickedRADVisualComponent = (RADVisualComponent)visualToRAD.get(evt.getSource());
                if (clickedRADVisualComponent == null)
                    return;
                RADVisualContainer parent;
                if (!(clickedRADVisualComponent instanceof RADVisualContainer)) {
                    parent = clickedRADVisualComponent.getParentContainer();
                    evt = createParentEvent (evt, parent.getContainer ()); // [PENDINGchange]
                }
                else
                    parent = (RADVisualContainer)clickedRADVisualComponent;

                PaletteItem paletteAddItem = palette.getSelectedItem  ();
                if (paletteAddItem.isDesignLayout ()) {
                    DesignLayout currentLayout = parent.getDesignLayout ();
                    if ((currentLayout != null) && (!(currentLayout instanceof DesignSupportLayout))) {
                        TopManager.getDefault().setStatusText(MessageFormat.format (
                                                                  FormEditor.getFormBundle ().getString ("FMT_MSG_SetLayout"),
                                                                  new Object[] { parent.getName () }
                                                              )
                                                             );
                    } else {
                        TopManager.getDefault().setStatusText(MessageFormat.format (
                                                                  FormEditor.getFormBundle ().getString ("FMT_MSG_CannotSetLayout"),
                                                                  new Object[] { parent.getName () }
                                                              )
                                                             );
                    }
                } else if (paletteAddItem.isBorder ()) {
                    if (clickedRADVisualComponent.getComponent () instanceof JComponent) {
                        TopManager.getDefault().setStatusText(MessageFormat.format (
                                                                  FormEditor.getFormBundle ().getString ("FMT_MSG_SetBorder"),
                                                                  new Object[] { clickedRADVisualComponent.getName () }
                                                              )
                                                             );
                    } else {
                        TopManager.getDefault().setStatusText(MessageFormat.format (
                                                                  FormEditor.getFormBundle ().getString ("FMT_MSG_CannotSetBorder"),
                                                                  new Object[] { clickedRADVisualComponent.getName () }
                                                              )
                                                             );
                    }
                } else if ((!paletteAddItem.isVisual ()) || (paletteAddItem.isMenu ())) {
                    TopManager.getDefault().setStatusText(MessageFormat.format (
                                                              FormEditor.getFormBundle ().getString ("FMT_MSG_AddNonVisualComponent"),
                                                              new Object[] {
                                                                  paletteAddItem.getItemClass ().getName ()
                                                              }
                                                          )
                                                         );
                } else {
                    DesignLayout dl = parent.getDesignLayout();
                    if (dl != null) {
                        DesignLayout.ConstraintsDescription cd = dl.getConstraintsDescription(evt.getPoint());
                        TopManager.getDefault().setStatusText(MessageFormat.format (
                                                                  FormEditor.getFormBundle ().getString ("FMT_MSG_AddComponent"),
                                                                  new Object[] {
                                                                      cd.getConstraintsString(),
                                                                      parent.getName (),
                                                                      paletteAddItem.getItemClass ().getName ()
                                                                  }
                                                              )
                                                             );
                    }
                }
            }
        }

        public void mouseDragged(MouseEvent evt) {
            if (ignoreMouse) return;
            if (evt.getComponent () instanceof Container) {
                Insets insets = ((Container)(evt.getComponent ())).getInsets ();
                evt.translatePoint (-insets.left, -insets.top);
            }

            if (addingMode) {
                Point current = evt.getPoint();
                int diffX = current.x - originalDragPoint.x;
                int diffY = current.y - originalDragPoint.y;
                if ((java.lang.Math.abs(diffX) >= MIN_DRAG_DIST) ||
                        (java.lang.Math.abs(diffY) >= MIN_DRAG_DIST))
                    addingDragMode = true;
                if (addingDragMode && dragLayout.canResize()) {
                    dragConstraints = dragLayout.resizeTo (dragConstraints, diffX, diffY);
                    dragLayout.markResizeTo (dragConstraints);
                    TopManager.getDefault().setStatusText(MessageFormat.format (
                                                              FormEditor.getFormBundle ().getString ("FMT_MSG_ResizeComponent"),
                                                              new Object[] {
                                                                  dragConstraints.getConstraintsString(),
                                                              }
                                                          )
                                                         );
                }
            }
            else if (movingMode) {
                Point current = recalculatePoint (evt);
                int diffX = current.x - originalDragPoint.x;
                int diffY = current.y - originalDragPoint.y;
                if (!movingDragMode)
                    if ((Math.abs(diffX) >= MIN_DRAG_DIST) ||
                            (Math.abs(diffY) >= MIN_DRAG_DIST))
                        movingDragMode = true;
                if (movingDragMode && dragLayout.canMove()) {
                    DesignLayout.ConstraintsDescription newDragConstraints =dragLayout.moveTo (dragConstraints, originalLocation.x + diffX, originalLocation.y + diffY, originalHotSpot);
                    if (!newDragConstraints.equals (dragConstraints)) {
                        dragConstraints = newDragConstraints;
                        dragLayout.markMoveTo (movingRADVisualComponent, dragConstraints);
                        TopManager.getDefault().setStatusText(MessageFormat.format (
                                                                  FormEditor.getFormBundle ().getString ("FMT_MSG_MoveComponent"),
                                                                  new Object[] {
                                                                      dragConstraints.getConstraintsString(),
                                                                  }
                                                              )
                                                             );
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------------
    // Tuborg Code

    void addVisualComponentsRecursively (RADVisualContainer parent) {
        RADComponent[] children = parent.getSubBeans ();
        DesignLayout layout = parent.getDesignLayout ();
        if (layout == null) return;

        for (int i = 0; i < children.length; i++) {
            if (!(children[i] instanceof RADVisualComponent)) {
                // do not process non-visual components
                continue;
            }

            RADVisualComponent visualComp = (RADVisualComponent)children [i];

            Component finalAddingComponent = visualComp.getComponent ();

            // we do not do this init for top level containers
            if ((!FormUtils.isHeavyweight(finalAddingComponent)) &&
                    (!(finalAddingComponent instanceof Window)))
            {
                if (!(visualComp instanceof RADVisualContainer)) {
                    LightweightLayer layer = new LightweightLayer(finalAddingComponent);
                    componentToLayer.put(finalAddingComponent, layer);
                    finalAddingComponent = layer;
                }
                finalAddingComponent.addMouseListener(mouseProcessor);
                finalAddingComponent.addMouseMotionListener(mouseMotionProcessor);
                finalAddingComponent = new JSelectionLayer (visualComp, finalAddingComponent, rListener, mListener);
            } else {
                finalAddingComponent.addMouseListener(mouseProcessor);
                finalAddingComponent.addMouseMotionListener(mouseMotionProcessor);
                finalAddingComponent = new SelectionLayer (visualComp, finalAddingComponent, rListener, mListener);
            }

            visualToRAD.put (visualComp.getComponent (), visualComp);
            radToSelection.put (visualComp, finalAddingComponent);
        }

        layout.updateLayout ();
        for (int i = 0; i < children.length; i++) {
            if (!(children[i] instanceof RADVisualComponent)) {
                // do not process non-visual components
                continue;
            }

            // [PENDING - patch to make JInternalFrames appear under JDK 1.3]
            if (children[i].getBeanInstance () instanceof javax.swing.JInternalFrame) {
                ((javax.swing.JInternalFrame)children[i].getBeanInstance ()).setVisible (true);
            }
        }


        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof RADVisualContainer) {
                addVisualComponentsRecursively ((RADVisualContainer)children[i]);
            }
        }
    }

    // -----------------------------------------------------------------------------
    // Connection Wizard

    private void startConnection (RADComponent source, RADComponent target) {
        ConnectionWizard1 cw1 = new ConnectionWizard1 (source);
        ConnectionWizard2 cw2 = null;
        ConnectionWizard3 cw3 = null;
        java.lang.reflect.Method storedCW2Method = null;
        // stored for preserving Wizard3 settings if
        // user moves to Wizard2 and back to Wizard3 with
        // the same selected method/property

        int currentStage = 1;
        while (true) {
            if (currentStage == 1) {
                cw1.show ();
                if (cw1.getReturnStatus () == ConnectionWizard1.CANCEL) {
                    cw1.dispose ();
                    if (cw2 != null) cw2.dispose ();
                    if (cw3 != null) cw3.dispose ();
                    return;
                }
                currentStage = 2;
                continue;
            } else if (currentStage == 2) {
                if (cw2 == null)
                    cw2 = new ConnectionWizard2 (target);
                cw2.show ();
                if (cw2.getReturnStatus () == ConnectionWizard2.CANCEL) {
                    cw1.dispose ();
                    cw2.dispose ();
                    if (cw3 != null) cw3.dispose ();
                    return;
                } else if (cw2.getReturnStatus () == ConnectionWizard2.PREVIOUS) {
                    currentStage = 1;
                    continue;
                } else {
                    currentStage = 3;
                    continue;
                }
            } else {
                // if the third stage should not be displayed, finish it
                if ((cw2.getActionType () == ConnectionWizard2.CODE_TYPE) ||
                        ((cw2.getActionType () == ConnectionWizard2.METHOD_TYPE) &&
                         (cw2.getSelectedMethod ().getMethod ().getParameterTypes ().length == 0)))
                {
                    finishConnection (source, target, cw1, cw2, null);
                    cw1.dispose ();
                    cw2.dispose ();
                    if (cw3 != null) cw3.dispose ();
                    return;
                } else {
                    if (cw2.getActionType () == ConnectionWizard2.METHOD_TYPE) {
                        if ((storedCW2Method == null) || (!storedCW2Method.equals (cw2.getSelectedMethod ().getMethod ()))) {
                            storedCW2Method = cw2.getSelectedMethod ().getMethod ();
                            if (cw3 != null)
                                cw3.dispose ();
                            cw3 = new ConnectionWizard3 (this, storedCW2Method, source);
                        }
                    } else if (cw2.getActionType () == ConnectionWizard2.PROPERTY_TYPE) {
                        if ((storedCW2Method == null) || (!storedCW2Method.equals (cw2.getSelectedProperty ().getWriteMethod ()))) {
                            storedCW2Method = cw2.getSelectedProperty ().getWriteMethod ();
                            if (cw3 != null)
                                cw3.dispose ();
                            cw3 = new ConnectionWizard3 (this, storedCW2Method, source);
                        }
                        // [PENDING - indexed properties]
                    }
                    cw3.show ();
                    if (cw3.getReturnStatus () == ConnectionWizard2.CANCEL) {
                        cw1.dispose ();
                        cw2.dispose ();
                        cw3.dispose ();
                        return;
                    } else if (cw3.getReturnStatus () == ConnectionWizard3.PREVIOUS) {
                        currentStage = 2;
                        continue;
                    } else { // finish wizard
                        finishConnection (source, target, cw1, cw2, cw3);
                        cw1.dispose ();
                        cw2.dispose ();
                        cw3.dispose ();
                        return;
                    }
                }
            }
        }
    }

    private void finishConnection (RADComponent source, RADComponent target,
                                   ConnectionWizard1 cw1, ConnectionWizard2 cw2, ConnectionWizard3 cw3)
    {
        EventsList.Event evt = cw1.getSelectedEvent ();
        String eventName = cw1.getEventName ();

        String bodyText;
        if (cw2.getActionType () == ConnectionWizard2.CODE_TYPE) {
            bodyText = null;
        } else {
            StringBuffer buf = new StringBuffer ();
            java.lang.reflect.Method m;
            if (cw2.getActionType () == ConnectionWizard2.METHOD_TYPE) {
                m = cw2.getSelectedMethod ().getMethod ();
            } else {
                m = cw2.getSelectedProperty ().getWriteMethod (); // [PENDING - indexed properties]
            }
            java.util.HashMap exceptions2 = new java.util.HashMap ();
            Object values[] = cw3.getParameters();
            for (int i=0; i<values.length; i++) {
                if (values[i] instanceof RADConnectionPropertyEditor.RADConnectionDesignValue) {
                    RADConnectionPropertyEditor.RADConnectionDesignValue val = (RADConnectionPropertyEditor.RADConnectionDesignValue) values[i];
                    if (val.type == RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_METHOD) {
                        Class [] except = val.getMethod ().getMethod ().getExceptionTypes ();
                        for (int j=0; j<except.length; j++) {
                            exceptions2.put(except[j], except[j]);
                        }
                    }
                }
            }
            Class[] exceptions = m.getExceptionTypes ();
            for (int k=0; k<exceptions.length; k++) {
                exceptions2.put(exceptions[k], exceptions[k]);
            }
            // if either the setter or some of the methods that get parameters throw checked exceptions,
            // we must generate try/catch block around it.
            if (exceptions2.size() > 0) {
                buf.append ("try {\n  "); // NOI18N
            }

            if (! (target instanceof FormContainer)) { // not generated for the form
                buf.append (target.getName ());
                buf.append ("."); // NOI18N
            }
            buf.append (m.getName ());
            buf.append (" ("); // NOI18N
            if (cw3 != null) {
                buf.append (cw3.getParametersText ());
            }
            buf.append (");"); // NOI18N

            int varCount = 1;
            // add the catch for all checked exceptions
            for (java.util.Iterator it = exceptions2.keySet().iterator(); it.hasNext();) {
                Class exceptionClass = (Class) it.next();
                buf.append ("\n} catch ("); // NOI18N
                buf.append (exceptionClass.getName ());
                buf.append (" "); // NOI18N
                String excName = "e"+varCount; // NOI18N
                varCount++;
                while (getVariablesPool ().isReserved (excName)) {
                    excName = "e"+varCount; // NOI18N
                    varCount++;
                }
                buf.append (excName);
                buf.append (") {\n"); // NOI18N
                buf.append ("  "+excName); // NOI18N
                buf.append (".printStackTrace ();\n"); // NOI18N
            }
            if (!exceptions2.isEmpty()) buf.append ("}\n"); // NOI18N

            bodyText = buf.toString ();
        }

        EventsManager.EventHandler handler=null;
        for (java.util.Iterator iter = evt.getHandlers ().iterator (); iter.hasNext();) {
            EventsManager.EventHandler eh = (EventsManager.EventHandler) iter.next();
            if (eh.getName ().equals (eventName)) {
                handler = eh;
                break;
            }
        }
        if (handler == null) {
            // new handler
            getEventsManager ().addEventHandler (evt, eventName, bodyText);
        } else {
            handler.setHandlerText (bodyText);
        }
        //regenerateInitializer (); // [PENDING]
        fireCodeChange ();
        evt.gotoEventHandler ();
    }

    // -----------------------------------------------------------------------------
    // Selection Management

    /** Cancels the current selection. */
    public void cancelSelection () {
        try {
            FormEditor.getComponentInspector ().setSelectedNodes(new Node[0], this); // cancel the current selection
        } catch (java.beans.PropertyVetoException e) {
            // nothing here
        }
    }

    /** @return true if the specified component is currently selected, false otherwise */
    private boolean isSelected (RADComponent radComp) {
        Node[] selected = FormEditor.getComponentInspector ().getSelectedNodes ();
        for (int i = 0; i < selected.length; i++) {
            RADComponentCookie cookie = (RADComponentCookie)selected[i].getCookie (RADComponentCookie.class);
            if ((cookie != null) && (radComp == cookie.getRADComponent ())) {
                return true;
            }
        }
        return false;
    }

    /** Selects the given component or toggles the selection (on the component)
    * if the multiSelect flag is true.
    * @param radComp    The RADComponent of the component to select
    * @param multiSelect if true, toggles the selection,
    *                    if false forces the selection of the component
    */
    void selectComponent (RADComponent radComp, boolean multiSelect) {
        if (FormEditor.getComponentInspector ().getFocusedForm () != this) return;

        if (!multiSelect) {
            RADComponentNode node = radComp.getNodeReference ();
            if (node != null) {
                try {
                    FormEditor.getComponentInspector ().setSelectedNodes(new Node[] { node }, this);
                } catch (java.beans.PropertyVetoException e) {
                    System.err.println("setSelectedNodes vetoed in FormManager2 #4");
                }
            }
        }
        else {
            Node[] selected = FormEditor.getComponentInspector ().getSelectedNodes();
            int selIndex = -1;
            for (int i=0; i < selected.length; i++) {
                RADComponentCookie cookie = (RADComponentCookie)selected[i].getCookie (RADComponentCookie.class);
                if ((cookie != null) && (radComp == cookie.getRADComponent ())) {
                    selIndex = i;
                    break;
                }
            }
            if (selIndex == -1) { // add radComp to selection
                Node[] newSelectedNodes = new Node[selected.length + 1];
                System.arraycopy(selected, 0, newSelectedNodes, 0, selected.length);
                newSelectedNodes[selected.length] = radComp.getNodeReference ();
                try {
                    FormEditor.getComponentInspector ().setSelectedNodes(newSelectedNodes, this);
                } catch (java.beans.PropertyVetoException e) {
                    System.err.println("setSelectedNodes vetoed in FormManager2 #5");
                }
            } else {              // remove radComp from selection (from pos selIndex)
                Node[] newSelectedNodes = new Node[selected.length - 1];
                if (selIndex != 0)
                    System.arraycopy(selected, 0, newSelectedNodes, 0, selIndex);
                if (selIndex != selected.length - 1)
                    System.arraycopy(selected, selIndex + 1, newSelectedNodes, selIndex, selected.length - selIndex - 1);
                try {
                    FormEditor.getComponentInspector ().setSelectedNodes(newSelectedNodes, this);
                } catch (java.beans.PropertyVetoException e) {
                    System.err.println("setSelectedNodes vetoed in FormManager2 #6");
                }
            }
        }

        formWindow.updateActivatedNodes ();
    }

    void updateSelection (Node[] newSelectionNodes) {
        RADComponent[] newSelection = getComponentsFromNodes (newSelectionNodes);

        RADComponent[] toAdd = findDelta (currentSelection, newSelection);
        RADComponent[] toRemove = findDelta (newSelection, currentSelection);
        currentSelection = newSelection;

        // [PENDING]
        int paletteMode = palette.getMode ();

        // mark selection on components to select
        for (int i=0; i < toAdd.length; i++)
            if (toAdd[i] instanceof RADVisualComponent) {
                Selection selection = (Selection)radToSelection.get (toAdd[i]);
                if (selection != null) {
                    selection.setSelected (true, paletteMode == PaletteAction.MODE_CONNECTION);
                    DesignLayout dl = ((RADVisualComponent)toAdd[i]).getParentContainer ().getDesignLayout ();
                    selection.setResizable ((dl != null) && dl.canMove () && dl.canResize ());
                    selection.setMovable ((dl != null) && dl.canMove ());
                }
            }

        boolean deselectNodes = true;

        // possibly clear previously started connection (which should not be kept anymore)
        // [PENDING]
        if (paletteMode != PaletteAction.MODE_CONNECTION) {
            connectionSource = null;
        }

        if ((paletteMode == PaletteAction.MODE_CONNECTION) &&
                (newSelection.length == 1) &&
                (newSelection[0] instanceof RADComponent) &&
                (connectionSource != null))
            deselectNodes = false;

        if (deselectNodes) {
            // cancel selection on components to deselect
            for (int i=0; i < toRemove.length; i++) {
                if (toRemove[i] instanceof RADVisualComponent) {
                    Selection selection = (Selection)radToSelection.get (toRemove[i]);
                    if (selection != null)
                        selection.setSelected (false, false);
                }
            }
        }

        // [PENDING]
        if (paletteMode == PaletteAction.MODE_CONNECTION) {
            if (newSelection.length == 0) {
                return; // ignore empty selection
            } else if (newSelection.length > 1) {
                // cancel connection mode if multiple components are selected
                palette.setMode (PaletteAction.MODE_SELECTION);
                connectionSource = null;
            }
            if (!(newSelection[0] instanceof RADComponent)) {
                return;  // ignore other selection than RADComponents (e.g. Non-visuals node, layout node)
            }
            if (connectionSource != null) {
                if (connectionSource.equals (newSelection[0]))
                    return; // cannot connect component to itself
                ignoreMouse = true;
                startConnection (connectionSource, (RADComponent)newSelection[0]);
                palette.setMode (PaletteAction.MODE_SELECTION);
                selectComponent (connectionSource, false); // [PENDING]
                connectionSource = null;
                ignoreMouse = false;
            } else {
                connectionSource = (RADComponent)newSelection[0];
            }
            return;
        }
    }

    private static RADComponent[] getComponentsFromNodes (Node[] nodes) {
        ArrayList list = new ArrayList (nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            RADComponentCookie cookie = (RADComponentCookie)nodes[i].getCookie (RADComponentCookie.class);
            if (cookie != null) {
                list.add (cookie.getRADComponent ());
            }
        }
        RADComponent[] comps = new RADComponent [list.size ()];
        list.toArray (comps);
        return comps;
    }

    /** @return a list of nodes that are in the second array and are not
    * in the first array.
    */
    private static RADComponent[] findDelta (RADComponent[] from, RADComponent[] to) {
        HashMap fromHash = new HashMap();
        for (int i=0; i < from.length; i++)
            fromHash.put (from[i], from [i]);
        ArrayList toList = new ArrayList(to.length);
        for (int i=0; i < to.length; i++)
            if (fromHash.get(to[i]) == null)
                toList.add (to[i]);
        RADComponent[] ret = new RADComponent[toList.size()];
        toList.toArray (ret);
        return ret;
    }

    class SelectionResizeListener implements Selection.ResizeListener {
        public void resizeStarted (RADVisualComponent comp) {
            dragLayout = comp.getParentContainer ().getDesignLayout ();
            dragConstraints = comp.getConstraints (dragLayout.getClass ());
            movingRADVisualComponent = comp;
            resizingMode = true;
        }

        public void resizeTo (Rectangle rect) {
            if (!resizingMode) return;
            dragConstraints = dragLayout.resizeToBounds (dragConstraints, rect);
            dragLayout.markResizeTo (dragConstraints);
            TopManager.getDefault().setStatusText(MessageFormat.format (
                                                      FormEditor.getFormBundle ().getString ("FMT_MSG_ResizeComponent"),
                                                      new Object[] {
                                                          dragConstraints.getConstraintsString(),
                                                      }
                                                  )
                                                 );
        }

        public void resizeCancelled () {
            resizingMode = false;
            dragLayout.markResizeTo (null);
            TopManager.getDefault().setStatusText(""); // NOI18N
        }

        public void resizeFinished () {
            if (!resizingMode) return;

            dragLayout.markResizeTo (null);
            dragLayout.removeComponent(movingRADVisualComponent);
            dragLayout.addComponent (movingRADVisualComponent, dragConstraints);
            movingRADVisualComponent.getNodeReference ().notifyPropertiesChange ();
            formContainer.validate();

            // regenerate the code to reflect the change
            fireCodeChange ();

            resizingMode = false;
        }
    }

    class SelectionMoveListener implements Selection.MoveListener {
        public void moveStarted (RADVisualComponent comp) {
            dragLayout = comp.getParentContainer ().getDesignLayout ();
            dragConstraints = comp.getConstraints (dragLayout.getClass ());
            movingRADVisualComponent = comp;
            outerMovingMode = true;
        }

        public void moveTo (Point point, Point hotSpot) {
            if (!outerMovingMode) return;
            dragConstraints = dragLayout.moveTo (dragConstraints, point.x, point.y, hotSpot);
            dragLayout.markMoveTo (movingRADVisualComponent, dragConstraints);
            TopManager.getDefault().setStatusText(MessageFormat.format (
                                                      FormEditor.getFormBundle ().getString ("FMT_MSG_MoveComponent"),
                                                      new Object[] {
                                                          dragConstraints.getConstraintsString(),
                                                      }
                                                  )
                                                 );
        }

        public void moveCancelled () {
            outerMovingMode = false;
            dragLayout.markMoveTo (movingRADVisualComponent, null);
            TopManager.getDefault().setStatusText(""); // NOI18N
        }

        public void moveFinished () {
            if (!outerMovingMode) return;

            dragLayout.markMoveTo (movingRADVisualComponent, null);
            dragLayout.removeComponent(movingRADVisualComponent);
            dragLayout.addComponent (movingRADVisualComponent, dragConstraints);
            movingRADVisualComponent.getNodeReference ().notifyPropertiesChange ();
            formContainer.validate();

            // regenerate the code to reflect the change
            fireCodeChange ();

            outerMovingMode = false;
        }
    }

}

/*
 * Log
 *  71   Gandalf-post-FCS1.69.1.0    3/10/00  Tran Duc Trung  forms use UTF-8 encoding
 *       by default
 *  70   Gandalf   1.69        3/7/00   Tran Duc Trung  fix #5791: cannot add 
 *       serialized bean to component palette
 *  69   Gandalf   1.68        2/17/00  Tran Duc Trung  works around a bug on 
 *       Solaris: file.encoding can have some weird value ("646")
 *  68   Gandalf   1.67        1/20/00  Ian Formanek    System.out changed to 
 *       System.err as this was an error message
 *  67   Gandalf   1.66        1/17/00  Pavel Buzek     cut/paste - store and 
 *       reuse names, assign new names on paste (not on copy)
 *  66   Gandalf   1.65        1/13/00  Ian Formanek    NOI18N #2
 *  65   Gandalf   1.64        1/12/00  Pavel Buzek     I18N
 *  64   Gandalf   1.63        1/11/00  Pavel Buzek     
 *  63   Gandalf   1.62        1/11/00  Pavel Buzek     
 *  62   Gandalf   1.61        1/10/00  Pavel Buzek     #3227
 *  61   Gandalf   1.60        1/8/00   Pavel Buzek     #2574
 *  60   Gandalf   1.59        1/5/00   Ian Formanek    NOI18N
 *  59   Gandalf   1.58        12/14/99 Pavel Buzek     #2574 - checking 
 *       exceptions in code generated by ConnectionWizard
 *  58   Gandalf   1.57        12/13/99 Pavel Buzek     adding visual components
 *       to layouts after Paste
 *  57   Gandalf   1.56        12/8/99  Pavel Buzek     
 *  56   Gandalf   1.55        12/3/99  Pavel Buzek     2868 fixed - remember 
 *       focus of component inspector when switching to test mode
 *  55   Gandalf   1.54        11/30/99 Ian Formanek    findWindow replaced with
 *       SwingUtilities.windowForComponent
 *  54   Gandalf   1.53        11/25/99 Ian Formanek    Uses Utilities module
 *  53   Gandalf   1.52        11/25/99 Pavel Buzek     support for multiple 
 *       handlers for one event
 *  52   Gandalf   1.51        11/15/99 Pavel Buzek     
 *  51   Gandalf   1.50        11/15/99 Ian Formanek    Fixed bug 4717 - On JDK 
 *       1.3, added JInternalFrames are not visible and the generated code does 
 *       not contain the required setVisible call.
 *  50   Gandalf   1.49        11/15/99 Pavel Buzek     property for encoding
 *  49   Gandalf   1.48        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  48   Gandalf   1.47        10/9/99  Ian Formanek    Fixed bug 3896 - After 
 *       deleting a component from form, new component can't have the name of 
 *       the deleted one.
 *  47   Gandalf   1.46        10/9/99  Ian Formanek    Fixed bug 4151 - Menus, 
 *       MenuItems etc. are missing in Component combo box in Form Connection 
 *       (or Connection Wizard).
 *  46   Gandalf   1.45        10/9/99  Ian Formanek    Fixed bug 3272 - I open 
 *       form in FormEditor.Than I close Form. I use test mode. Exception was 
 *       throwed.
 *  45   Gandalf   1.44        10/9/99  Ian Formanek    Fixed bug 3272 - I open 
 *       form in FormEditor.Than I close Form. I use test mode. Exception was 
 *       throwed.
 *  44   Gandalf   1.43        10/9/99  Ian Formanek    Fixed bug 4411 - Delete 
 *       of a jMenuItem does not work. (No action is performed.)
 *  43   Gandalf   1.42        10/6/99  Ian Formanek    addVisualComponentsRecursively
 *        made package private (used from RADComponentNode when pasting copies 
 *       of containers)
 *  42   Gandalf   1.41        9/29/99  Ian Formanek    codeChanged added to 
 *       FormListener
 *  41   Gandalf   1.40        9/24/99  Ian Formanek    comment only
 *  40   Gandalf   1.39        9/7/99   Ian Formanek    Fixed bug 3272 - I open 
 *       form in FormEditor.Than I close Form. I use test mode. Exception was 
 *       throwed.
 *  39   Gandalf   1.38        9/2/99   Ian Formanek    Fixed bug 2806 - If I 
 *       delete component from form then I cannot delete method for event 
 *       handler
 *  38   Gandalf   1.37        9/2/99   Ian Formanek    Reflecting changes in 
 *       RADComponent.getPropertyByName
 *  37   Gandalf   1.36        8/18/99  Ian Formanek    Some methods made public
 *  36   Gandalf   1.35        8/8/99   Ian Formanek    Fixed bug 3138 - delete 
 *       JMenuBar from JFrame throws exception
 *  35   Gandalf   1.34        8/1/99   Ian Formanek    Improved deserialization
 *       of opened forms
 *  34   Gandalf   1.33        7/31/99  Ian Formanek    Provided public methods 
 *       for obtaining list of components on the form and finding a component by
 *       name
 *  33   Gandalf   1.32        7/25/99  Ian Formanek    Variables management 
 *       moved to RADComponent
 *  32   Gandalf   1.31        7/20/99  Ian Formanek    Setting menu after load
 *  31   Gandalf   1.30        7/19/99  Ian Formanek    pack () in test mode
 *  30   Gandalf   1.29        7/18/99  Ian Formanek    removed obsoleted 
 *       comment
 *  29   Gandalf   1.28        7/14/99  Ian Formanek    Fixed bug 1830 - Layout 
 *       panel is not synchronized with Form Window
 *  28   Gandalf   1.27        7/11/99  Ian Formanek    initNonVisualNodes->initNonVisualComponents
 *       
 *  27   Gandalf   1.26        7/9/99   Ian Formanek    Sets added menu on the 
 *       form
 *  26   Gandalf   1.25        7/9/99   Ian Formanek    Menu editor improvements
 *  25   Gandalf   1.24        7/5/99   Ian Formanek    
 *  24   Gandalf   1.23        7/5/99   Ian Formanek    getComponentInstance->getBeanInstance,
 *        getComponentClass->getBeanClass
 *  23   Gandalf   1.22        7/4/99   Ian Formanek    Popup menu is hidden 
 *       when form window loses focus
 *  22   Gandalf   1.21        7/3/99   Ian Formanek    non-visual components 
 *       are selected after they are added to form, fixed selection after 
 *       finishing Conn Wizard
 *  21   Gandalf   1.20        7/3/99   Ian Formanek    Menus added as 
 *       non-visual components, fixed first switch to TestMode and back 
 *       (restoring correctly previously used DesignMode)
 *  20   Gandalf   1.19        6/30/99  Ian Formanek    fireFormToBeSaved method
 *       added
 *  19   Gandalf   1.18        6/24/99  Ian Formanek    Popup menu is back
 *  18   Gandalf   1.17        6/10/99  Ian Formanek    Fixed problem with 
 *       refreshing layout node name for the top-level container
 *  17   Gandalf   1.16        6/10/99  Ian Formanek    Regeneration on layout 
 *       changes
 *  16   Gandalf   1.15        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  15   Gandalf   1.14        6/7/99   Ian Formanek    Fixed bug 2092 - No AWT 
 *       component cannot be added to AWT top component (Applet, Frame, Dialog, 
 *       Panel), for J top components works OK.
 *  14   Gandalf   1.13        6/7/99   Ian Formanek    
 *  13   Gandalf   1.12        6/3/99   Ian Formanek    Fixed removing 
 *       components
 *  12   Gandalf   1.11        6/2/99   Ian Formanek    ToolsAction, Reorder
 *  11   Gandalf   1.10        5/31/99  Ian Formanek    Design/Test Mode
 *  10   Gandalf   1.9         5/31/99  Ian Formanek    
 *  9    Gandalf   1.8         5/26/99  Ian Formanek    Does not fire change 
 *       until initialized
 *  8    Gandalf   1.7         5/26/99  Ian Formanek    Minor cleanup
 *  7    Gandalf   1.6         5/24/99  Ian Formanek    Non-Visual components
 *  6    Gandalf   1.5         5/24/99  Ian Formanek    Added access to 
 *       non-visual components
 *  5    Gandalf   1.4         5/20/99  Ian Formanek    FormNodeCookie->RADComponentCookie
 *       
 *  4    Gandalf   1.3         5/16/99  Ian Formanek    Fixed bug 1828 - 
 *       Changing layout of a component doesn't change the textual 
 *       represenatation of layout of the component in Component Inspector  
 *       Fixed bug 1827 - When I delete a component from Component Inspector 
 *       then the Form doesn't update its UI in order to reflect this removing
 *  3    Gandalf   1.2         5/16/99  Ian Formanek    Fixed bug 1829 - 
 *       Duplicate variable declaration .
 *  2    Gandalf   1.1         5/16/99  Ian Formanek    
 *  1    Gandalf   1.0         5/15/99  Ian Formanek    
 * $
 */
