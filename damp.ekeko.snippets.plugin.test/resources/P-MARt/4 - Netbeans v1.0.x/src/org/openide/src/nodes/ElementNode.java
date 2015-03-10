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

package org.openide.src.nodes;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.DataFlavor;
import java.beans.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import org.openide.src.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListener;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.ExTransferable;

/** Superclass of nodes representing elements in the source hierarchy.
* <p>Element nodes generally:
* <ul>
* <li>Have an associated icon, according to {@link #resolveIconBase}.
* <li>Have a display name based on the element's properties, using {@link #elementFormat};
* changes to {@link ElementFormat#dependsOnProperty relevant} element properties
* automatically affect the display name.
* <li>Have some node properties (displayable on the property sheet), according to
* the element's properties, and with suitable editors.
* <li>Permit renames and deletes, if a member element and writeable.
* <li>As permitted by the element, and a writable flag in the node,
* permit cut/copy/paste operations, as well as creation of new members.
* </ul>
*
* @author Petr Hamernik
*/
public abstract class ElementNode extends AbstractNode implements IconStrings, ElementProperties {
    /** Source of the localized human presentable strings. */
    static ResourceBundle bundle = NbBundle.getBundle(ElementNode.class);

    /** Options for the display name format. */
    protected static final SourceOptions sourceOptions = (SourceOptions)SourceOptions.findObject(SourceOptions.class, true);

    /** Default return value of getIconAffectingProperties method. */
    private static final String[] ICON_AFFECTING_PROPERTIES = new String[] {
                PROP_MODIFIERS
            };

    /** Associated element. */
    protected Element element;

    /** Format for {@link java.beans.FeatureDescriptor#getDisplayName}. */
    protected ElementFormat elementFormat = new ElementFormat (""); // NOI18N

    /** Is this node read-only or are modifications permitted? */
    protected boolean writeable;

    /** Listener to forbid its garbage collection */
    private transient PropertyChangeListener listener;

    /** Create a new element node.
    *
    * @param element element to represent
    * @param children child nodes
    * @param writeable <code>true</code> if this node should allow modifications.
    *        These include writable properties, clipboard operations, deletions, etc.
    */
    public ElementNode(Element element, Children children, boolean writeable) {
        super(children);
        this.element = element;
        this.writeable = writeable;
        setIconBase(resolveIconBase());
        setDisplayName(getElementFormat().format(element));
        listener = new ElementListener();
        element.addPropertyChangeListener(WeakListener.propertyChange (listener, element));
        displayFormat = null;
    }

    /* Gets the short description of this node.
    * @return A localized short description associated with this node.
    */
    public String getShortDescription() {
        try {
            return getHintElementFormat().format(element);
        }
        catch (IllegalArgumentException e) {
            return super.getShortDescription();
        }
    }

    /** Get the currently appropriate icon base.
    * Subclasses should make this sensitive to the state of the element--for example,
    * a private variable may have a different icon than a public one.
    * The icon will be automatically changed whenever a
    * {@link #getIconAffectingProperties relevant} change is made to the element.
    * @return icon base
    * @see AbstractNode#setIconBase
    */
    abstract protected String resolveIconBase();

    /** Get the names of all element properties which might affect the choice of icon.
    * The default implementation just returns {@link #PROP_MODIFIERS}.
    * @return the property names, from {@link ElementProperties}
    */
    protected String[] getIconAffectingProperties() {
        return ICON_AFFECTING_PROPERTIES;
    }

    /** Get a format for the element's display name.
    * The display name will be automatically updated whenever a
    * {@link ElementFormat#dependsOnProperty relevant}
    * change is made to the element.
    * @return the format
    */
    public final ElementFormat getElementFormat() {
        return elementFormat;
    }

    /** Set the format for the display name.
    * @param elementFormat the new format
    */
    public final void setElementFormat(ElementFormat elementFormat) {
        this.elementFormat = elementFormat;
        setDisplayName(elementFormat.format(ElementNode.this.element));
    }

    /** Get a format for creating this node's
    * {@link java.beans.FeatureDescriptor#getShortDescription short description}.
    */
    abstract protected ElementFormat getHintElementFormat();

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ElementNode.class);
    }

    /** Test whether this node can be renamed.
    * The default implementation assumes it can if this node is {@link #writeable}.
    *
    * @return <code>true</code> if this node can be renamed
    */
    public boolean canRename() {
        return writeable;
    }

    /** Test whether this node can be deleted.
    * The default implementation assumes it can if this node is {@link #writeable}.
    *
    * @return <code>true</code> if this node can be renamed
    */
    public boolean canDestroy () {
        return writeable;
    }

    /* Copy this node to the clipboard.
    *
    * @return {@link ExTransferable.Single} with one flavor, {@link NodeTransfer#nodeCopyFlavor}
    * @throws IOException if it could not copy
    */
    public Transferable clipboardCopy () throws IOException {
        ExTransferable ex = ExTransferable.create(super.clipboardCopy());
        ex.put(new ElementStringTransferable());
        return ex;
    }

    /* Cut this node to the clipboard.
    *
    * @return {@link ExTransferable.Single} with one flavor, {@link NodeTransfer#nodeCopyFlavor}
    * @throws IOException if it could not cut
    */
    public Transferable clipboardCut () throws IOException {
        if (!writeable)
            throw new IOException();

        ExTransferable ex = ExTransferable.create(super.clipboardCut());
        ex.put(new ElementStringTransferable());
        return ex;
    }

    /** Transferable for elements as String. */
    class ElementStringTransferable extends ExTransferable.Single {
        /** Construct new Transferable for this node. */
        ElementStringTransferable() {
            super(DataFlavor.stringFlavor);
        }

        /** @return the data as String */
        protected Object getData() {
            return element.toString();
        }
    }

    /** Test whether this node can be copied.
    * The default implementation returns <code>true</code>.
    * @return <code>true</code> if it can
    */
    public boolean canCopy () {
        return true;
    }

    /** Test whether this node can be cut.
    * The default implementation assumes it can if this node is {@link #writeable}.
    * @return <code>true</code> if it can
    */
    public boolean canCut () {
        return writeable;
    }

    /** Set all actions for this node.
    * @param actions new list of actions
    */
    public void setActions(SystemAction[] actions) {
        systemActions = actions;
    }

    /** Calls super.fireCookieChange. The reason why is redefined
    * is only to allow the access from this package.
    */
    void superFireCookieChange() {
        fireCookieChange();
    }

    /** Get a cookie from this node.
    * First tries the node itself, then {@link Element#getCookie}.
    * Since {@link Element} implements <code>Node.Cookie</code>, it is
    * possible to find the element from a node using code such as:
    * <p><code><pre>
    * Node someNode = ...;
    * MethodElement element = (MethodElement) someNode.getCookie (MethodElement.class);
    * if (element != null) { ... }
    * </pre></code>
    * @param type the cookie class
    * @return the cookie or <code>null</code>
    */
    public Node.Cookie getCookie (Class type) {
        Node.Cookie c = super.getCookie(type);
        if (c == null)
            c = element.getCookie(type);

        return c;
    }

    /** Test for equality.
    * @return <code>true</code> if the represented {@link Element}s are equal
    */
    public boolean equals (Object o) {
        return (o instanceof ElementNode) && (element.equals (((ElementNode)o).element));
    }

    /** Get a hash code.
    * @return the hash code from the represented {@link Element}
    */
    public int hashCode () {
        return element.hashCode ();
    }


    void superSetName(String name) {
        super.setName(name);
    }

    void superPropertyChange (String name, Object o, Object n) {
        super.firePropertyChange (name, o, n);
    }

    void superShortDescriptionChange (String o, String n) {
        super.fireShortDescriptionChange(o, n);
    }

    // ================== Element listener =================================

    /** Listener for changes of the element's property changes.
    * It listens and changes updates the iconBase and displayName
    * if the changed property could affect them.
    */
    private class ElementListener implements PropertyChangeListener {
        /** Called when any element's property changed.
        */
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (propName == null) {
                setDisplayName(getElementFormat().format(ElementNode.this.element));
                setIconBase(resolveIconBase());
            }
            else {
                // display name
                if (getElementFormat().dependsOnProperty(propName))
                    setDisplayName(getElementFormat().format(ElementNode.this.element));

                // icon
                String[] iconProps = getIconAffectingProperties();
                for (int i = 0; i < iconProps.length; i++) {
                    if (iconProps[i].equals(propName)) {
                        setIconBase(resolveIconBase());
                        break;
                    }
                }

                if (propName.equals(ElementProperties.PROP_NAME)) {
                    // set inherited name - this code should rather in MemberElementNode,
                    // but we safe one instance of listener for each node
                    // if it will be here. [Petr]
                    try {
                        superSetName(((MemberElement)ElementNode.this.element).getName().toString());
                    }
                    catch (ClassCastException e) {
                        // it is strange - PROP_NAME has only member element.
                    }
                }
                else {
                    if (propName.equals(Node.PROP_COOKIE)) {
                        // Fires the changes of the cookies of the element.
                        superFireCookieChange();
                        return;
                    }
                }
            }

            if (getHintElementFormat().dependsOnProperty(evt.getPropertyName()))
                superShortDescriptionChange("", getShortDescription()); // NOI18N

            superPropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        }
    }

    // ================== Property support for element nodes =================

    /** Property support for element nodes properties.
    */
    static abstract class ElementProp extends PropertySupport {
        /** Constructs a new ElementProp - support for properties of
        * element hierarchy nodes.
        *
        * @param name The name of the property
        * @param type The class type of the property
        * @param canW The canWrite flag of the property
        */
        public ElementProp(String name, java.lang.Class type, boolean canW) {
            super(name, type,
                  bundle.getString("PROP_" + name),
                  bundle.getString("HINT_" + name),
                  true, canW);
        }

        /** Setter for the value. This implementation only tests
        * if the setting is possible.
        *
        * @param val the value of the property
        * @exception IllegalAccessException when this ElementProp was constructed
        *            like read-only.
        */
        public void setValue (Object val) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
            if (!canWrite())
                throw new IllegalAccessException(bundle.getString("MSG_Cannot_Write"));
        }

        /** Invokes the runnable using NbDocument.runAtomic.
        * If BadLocationException occured inside, it will be thrown
        * the new SourceException.
        */
        void runAtomic(Element element, final SourceEditSupport.ExceptionalRunnable exRun) throws InvocationTargetException {
            final SourceException[] ex = { null };
            try {
                SourceElement source = SourceEditSupport.findSource(element);
                Runnable run = new Runnable() {
                                   public void run() {
                                       try {
                                           exRun.run();
                                       }
                                       catch (SourceException e) {
                                           ex[0] = e;
                                       }
                                   }
                               };
                source.runAtomicAsUser(run);
            }
            catch (SourceException e) {
                ex[0] = e;
            }
            if (ex[0] != null) {
                throw new InvocationTargetException(ex[0]);
            }
        }
    }
}

/*
* Log
*  32   Gandalf-post-FCS1.30.2.0    4/17/00  Svatopluk Dedic Initializes node display 
*       format from SourceOptions option.
*  31   src-jtulach1.30        1/15/00  Jaroslav Tulach Compiles with JDK 1.3
*  30   src-jtulach1.29        1/12/00  Petr Hamernik   i18n using perl script 
*       (//NOI18N comments added)
*  29   src-jtulach1.28        1/8/00   Radko Najman    fixed bug #2568
*  28   src-jtulach1.27        11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  27   src-jtulach1.26        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  26   src-jtulach1.25        10/7/99  Petr Hamernik   formating and a small 
*       improvement
*  25   src-jtulach1.24        9/13/99  Petr Hamernik   runAsUser implemented and
*       used
*  24   src-jtulach1.23        7/8/99   Jesse Glick     Context help.
*  23   src-jtulach1.22        7/1/99   Petr Hamernik   Clipboard operations on 
*       source Elements
*  22   src-jtulach1.21        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  21   src-jtulach1.20        4/26/99  Jesse Glick     [JavaDoc]
*  20   src-jtulach1.19        4/26/99  Petr Hamernik   getCookie minor changes
*  19   src-jtulach1.18        4/21/99  Petr Hamernik   debug pring removed
*  18   src-jtulach1.17        4/20/99  Petr Hamernik   weak listener bugfix
*  17   src-jtulach1.16        4/20/99  Petr Hamernik   debugs removed, small 
*       bugfix
*  16   src-jtulach1.15        4/20/99  Jaroslav Tulach 
*  15   src-jtulach1.14        4/13/99  Petr Hamernik   getCookie improved
*  14   src-jtulach1.13        4/12/99  Jesse Glick     [JavaDoc]
*  13   src-jtulach1.12        4/9/99   Jan Jancura     Bug 1507 equals added
*  12   src-jtulach1.11        4/8/99   Jan Jancura     equals added
*  11   src-jtulach1.10        4/2/99   Jesse Glick     [JavaDoc]
*  10   src-jtulach1.9         4/2/99   Jesse Glick     [JavaDoc]
*  9    src-jtulach1.8         4/1/99   Jan Jancura     Object Browser support
*  8    src-jtulach1.7         3/26/99  Petr Hamernik   fire of properties - 
*       bugfix
*  7    src-jtulach1.6         3/18/99  Petr Hamernik   
*  6    src-jtulach1.5         3/18/99  Petr Hamernik   
*  5    src-jtulach1.4         3/16/99  Petr Hamernik   properties improvements
*  4    src-jtulach1.3         3/15/99  Petr Hamernik   
*  3    src-jtulach1.2         3/15/99  Petr Hamernik   
*  2    src-jtulach1.1         3/15/99  Petr Hamernik   
*  1    src-jtulach1.0         3/12/99  Petr Hamernik   
* $
*/
