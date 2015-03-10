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

package org.netbeans.modules.java;

import java.io.*;
import java.util.LinkedList;
import java.lang.reflect.Modifier;

import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

import org.openide.src.*;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.text.EditorSupport;

/** Describes variable in class.
*
* @author Petr Hamernik
*/
final class FieldElementImpl extends MemberElementImpl implements FieldElement.Impl {

    /** Bounds for the type specification; the field is used iff the field is
      clustered in a group of fields. In that case, prevField and nextField are
      also filled. */
    PositionBounds typeBounds;

    /** Points to the prevField field in a group of fields in the same declaration statement
    */
    FieldElementImpl prevField;

    /** Points to the nextField field in a group. */
    FieldElementImpl nextField;

    /** Type of exception */
    Type type;

    /** Init value of variable */
    String initValue;

    private static final boolean DEBUG = false;

    static final long serialVersionUID =-2897555431526009073L;

    /** Constructor for the parser. */
    public FieldElementImpl() {
    }

    /** Copy constructor.
    * @param el element to copy from
    */
    public FieldElementImpl(final FieldElement el, final PositionBounds bounds) throws SourceException {
        super(el, bounds);
        type = el.getType ();
        initValue = el.getInitValue();
        //  javadoc = new JavaDocImpl.Field(el.getJavaDoc().getRawText(), this);
        if (bounds != null)
            regenerate(el);
    }

    private FieldElementImpl findSibling(Identifier name) {
        ClassElement clazz = ((MemberElement)element).getDeclaringClass();
        FieldElement f = clazz.getField(name);
        if (f != null) {
            return (FieldElementImpl)f.getCookie(ElementImpl.class);
        } else {
            return null;
        }
    }

    /** Updates the element fields. This method is called after reparsing.
    * @param impl the carrier of new information.
    */
    void updateImpl(FieldElementImpl impl, LinkedList changes, int changesMask) {
        boolean changesMatch = ((changesMask & JavaConnections.TYPE_FIELDS_CHANGE) != 0);
        MemberElement prevElement = super.updateImpl(impl, changesMatch);

        /* Reestablish links to fellow fields in the same field group */
        if (impl.prevField != null) {
            prevField = findSibling(impl.prevField.getName());
        } else {
            prevField = null;
        }
        if (impl.nextField != null) {
            nextField = findSibling(impl.nextField.getName());
        } else {
            nextField = null;
        }
        this.typeBounds = impl.typeBounds;

        if (!type.compareTo(impl.type, true)) {
            if (changesMatch && (prevElement == null))
                prevElement = (FieldElement)(((FieldElement)element).clone());
            Type old = type;
            type = impl.type;
            firePropertyChange(PROP_TYPE, old, type);
        }
        if ((initValue != impl.initValue) && (initValue == null || !initValue.equals(impl.initValue))) {
            if (changesMatch && (prevElement == null))
                prevElement = (FieldElement)(((FieldElement)element).clone());
            String old = initValue;
            initValue = impl.initValue;
            firePropertyChange(PROP_INIT_VALUE, old, initValue);
        }
        if (changesMatch && (prevElement != null)) {
            changes.add(new JavaConnections.Change(JavaConnections.TYPE_FIELDS_CHANGE, prevElement, element));
        }
    }

    /* ========================= ELEMENT PROPERTIES MODIFICATION ============================ */
    /** Type of the variable.
    * @return the type
    */
    public Type getType() {
        return type;
    }

    /** Setter for type of the variable.
    * @param type the variable type
    */
    public void setType(Type type) throws SourceException {
        checkNotLocked();

        if (compareSourceTypes(this.type, type)) {
            return;
        }
        Type old = this.type;
        this.type = type;
        try {
            if (isSingle()) {
                regenerateHeader();
            } else {
                breakFieldGroup(false);
            }
            modify();
            firePropertyChange(PROP_TYPE, old, type);
        }
        catch (SourceException e) {
            this.type = old;
            throw e;
        }
    }

    /** Getter for the initial value.
    * @return initial value for the variable or empty string if it is not initialized
    */
    public String getInitValue() {
        return initValue;
    }

    /** Setter for the initial value.
    * @param value initial value for the variable
    */
    public void setInitValue(String initValue) throws SourceException {
        if ((initValue == this.initValue) ||
                ((initValue != null) && initValue.equals(this.initValue)))
            return;

        checkNotLocked();
        String old = this.initValue;
        this.initValue = initValue;
        try {
            if (isSingle()) {
                regenerate((FieldElement)element);
            } else {
                regenerateInitializer();
            }
            firePropertyChange (PROP_INIT_VALUE, old, initValue);
        }
        catch (SourceException e) {
            this.initValue = old;
        }
    }

    private void regenerateInitializer() throws SourceException {
        final PositionRef start = bodyBounds.getBegin();
        final StyledDocument doc;

        try {
            doc = start.getEditorSupport().openDocument();
        } catch (IOException e) {
            throw new SourceException(e.getMessage());
        }

        Util.ExceptionRunnable run = new Util.ExceptionRunnable() {
                                         public void run() throws Exception {
										     String txt;
											 
											 if (FieldElementImpl.this.initValue != null &&
											 	 !FieldElementImpl.this.initValue.equals("")) {												 
	                                             StringWriter writer = new StringWriter();
    	                                         Writer iWriter = Util.findIndentWriter(doc, start.getOffset(), writer);
												 iWriter.write(" = ");
            	                                 ElementPrinter prn = new CodeGenerator.ElementPrinterImpl(iWriter, element, ElementPrinter.BODY_BEGIN,
                	                                                  ElementPrinter.BODY_END);
                    	                         try {
                        	                         element.print(prn);
                            	                 } catch (ElementPrinterInterruptException e) {
                                	             }
												 txt = writer.toString();
											 } else {
											   txt = "";
											 }
											 PositionBounds initBounds = new PositionBounds(
											 	headerBounds.getEnd(), bodyBounds.getEnd());
                                    	     initBounds.setText(txt);
                                         }
                                     };

        Util.runAtomic(doc, run);
    }

    /** @return java doc for the field
    */
    public JavaDoc.Field getJavaDoc () {
        return (JavaDoc.Field) javadoc;
    }

    public Object readResolve() {
        return new FieldElement(this, null);
    }

    /** The method provides modified behaviour when modifiers are changed for a field
      that is part of a field group. If the field is declared alone, the request
      is delegated to the superclass implementation.
      @param newMods new modifiers for this particular field.
      @throws SourceException if the change is not permited
    */
    public void setModifiers(int newMods) throws SourceException {
        if (isSingle()) {
            super.setModifiers(newMods);
            return;
        }
        checkNotLocked();

        int oldMods = mod;
        try {
            mod = newMods;
            breakFieldGroup(false);
        } catch (SourceException e) {
            mod = oldMods;
            throw e;
        }
    }

    /* =================== FIELD GROUPS SUPPORT ==========================*/

    void setPreviousField(FieldElementImpl prev) {
        if (prevField == prev) {
            return;
        }
        prevField = prev;
        if (prev != null) {
            prev.nextField = this;
        }
        adjustBounds();
    }

    void setNextField(FieldElementImpl next) {
        if (nextField == next) return;
        nextField = next;
        if (next != null) {
            next.setPreviousField(this);
        }
        adjustBounds();
    }

    private void adjustBounds() {
        if (prevField != null || nextField != null) {
            return;
        }
        //if (DEBUG) System.out.println("Adjusting bounds for single field...");
        headerBounds = new PositionBounds(typeBounds.getBegin(), headerBounds.getEnd());
        bounds = new PositionBounds(typeBounds.getBegin(), bounds.getEnd());
    }

    /** Determines, if the field is declared alone, or as a part of a field group.
      @return true, if the field is declared alone; false otherwise.
    */
    boolean isSingle() {
        return prevField == null && nextField == null;
    }

    /** Returns immediately preceding field in the field group.
      @return field that immediately precedes this field in the field group, or null,
        if the field is the leading one, or is not part of any field group.
    */
    FieldElementImpl previousField() {
        return prevField;
    }

    /** Returns following field in a field group.
      @return field that immediately follows this one. Returns null for the last field
        in a group, or if the field is not a part of any field group.
    */
    FieldElementImpl nextField() {
        return nextField;
    }

    /** Checks if the field is a leading field in a field group.
      @return true, if the field is a leading field, or is not part of any field group.
    */
    boolean isLeading() {
        return prevField == null;
    }

    FieldElementImpl getLeadingField() {
        FieldElementImpl ret;

        for (ret = this; !ret.isLeading(); ret = ret.previousField()) ;
        return ret;
    }

    /** Overrides the default behaviour in case that the field is a part of a
      field group. If not, the method simply calls the superclass' implementation.
      Otherwise, the method removes <b>only</b> portion of text between separator from
      the prevField field to the end of field's name (inclusive).
      @throws SourceException if the source file change is rejected.
    */
    void removeFromSource() throws SourceException {
        if (isSingle()) {
            super.removeFromSource();
            return;
        }

        checkValid();
        // We're not the single field in the declarator. Only this field
        PositionRef begin, end;
        EditorSupport supp = bounds.getBegin().getEditorSupport();

        /*
        if (DEBUG) {
            System.out.println("Field " + getName().toString() + ": removing from source file.");
        }
        */
        if (prevField != null) {
            begin = supp.createPositionRef(prevField.bodyBounds.getEnd().getOffset(),
                                           Position.Bias.Forward);
            end = supp.createPositionRef(bounds.getEnd().getOffset() - 1, Position.Bias.Backward);
        } else {
            // We're the first field in the field element chain. We have to retain
            // the type declarator in place and remove only the name.
            begin = bounds.getBegin();
            // must delete our OWN delimiter.
			end = bounds.getEnd();
        }
        /*
        if (DEBUG) {
            System.out.println("begin: " + begin.getOffset() + ", end: " + end.getOffset());
        }
        */
        SourceElementImpl.clearBounds(new PositionBounds(begin, end));
        if (prevField != null) {
            prevField.setNextField(null);
        }
        if (nextField != null) {
            nextField.setPreviousField(null);
        }
        prevField = nextField = null;
    }

    public boolean isAtOffset(int offset) {
        if (isSingle() || !isLeading()) {
            return super.isAtOffset(offset);
        }
        PositionRef b, e;
        if (javadoc != null && docBounds != null) {
            b = docBounds.getBegin();
        } else {
            b = typeBounds.getBegin();
        }
        e = bounds.getEnd();
        return (b.getOffset() <= offset) && (e.getOffset() >= offset);
    }

    /* ========================= CODE GENERATORS/MODIFIERS =============================*/

    PositionRef getJavaDocPosition() {
        if (docBounds != null) {
            return docBounds.getBegin();
        }
        if (isSingle()) {
            return bounds.getBegin();
        } else {
            return typeBounds.getBegin();
        }
    }

    /** Regenerates the field's type into the source file. Simply delegates to
      superclass' implementation (regenerates type and name of the field) if the field
      is not a part of a group. If it is, the function only rewrites type declaration.
      @throws SourceException if the source change is rejected.
    */
    public void regenerateType() throws SourceException {
        if (isSingle()) {
            super.regenerate(element);
            return;
        }

        // we have to regenerate header and body.
        try {
            final StyledDocument doc = headerBounds.getBegin().getEditorSupport().openDocument();
            // Must take over CodeGenerator:
            final PositionBounds bounds = typeBounds;
            final String name = getType().getSourceString();
            Util.ExceptionRunnable run = new Util.ExceptionRunnable() {
                                             public void run() throws Exception {
                                                 bounds.setText(name);
                                             }
                                         };
            Util.runAtomic(doc, run);
        } catch (SourceException e) {
            throw e;
        } catch (Exception e) {
            throw new SourceException(e.getMessage());
        }
    }

    /** Creates runnable that re-creates field's name in the source file.
      This method is used internally from {@link #regenerateHeader}.
      @return runnable that can be used inside runAtomic method to recreate
        field's name in the source file.
    */
    protected Util.ExceptionRunnable createNameGenerator() {
        // Must take over CodeGenerator:
        final PositionBounds bounds = headerBounds;
        final String name = getName().toString();
        Util.ExceptionRunnable run = new Util.ExceptionRunnable() {
                                         public void run() throws Exception {
                                             bounds.setText(name);
                                         }
                                     };
        return run;
    }

    /** Creates runnable object capable of recreating initializer code for this field.
      @return runnable object that can be used inside runAtomic to recreate initializer.
    */
    protected Util.ExceptionRunnable createInitializerGenerator() {
        final EditorSupport supp = headerBounds.getBegin().getEditorSupport();

        return new Util.ExceptionRunnable() {
                   public void run() throws Exception {
                       final StyledDocument doc = supp.openDocument();
                       StringWriter writer = new StringWriter();
                       Writer indentWriter = Util.findIndentWriter(doc, bodyBounds.getBegin().getOffset(), writer);
                       indentWriter.write(initValue);
                       bodyBounds.setText(writer.toString());
                   }
               };
    }

    /** Re-creates the header for a field. If the field is not a part of a group,
      the method delegates to the superclass' implementation (both type and name
      are rewritten). If it is in a group, the method only rewrites field's name
      using name generator runnable.
    */
    public void regenerateHeader() throws SourceException {
        if (isSingle()) {
            super.regenerateHeader();
            return;
        }
        //if (DEBUG) System.out.println("Regenerating partial header");
        try {
            Util.ExceptionRunnable r1 = createNameGenerator();
            final StyledDocument doc = headerBounds.getBegin().getEditorSupport().openDocument();
            Util.runAtomic(doc, r1);
        } catch (SourceException e) {
            throw e;
        } catch (Exception e) {
            throw new SourceException(e.getMessage());
        }
    }

    /** Cuts this field from its field group and places it right before or after
      the remains of the group. If the field is a leading one, it is placed
      immediately before the field group. If it is not the first declared in the
      group, the field moves right behind the group.
      If the field group has common JavaDoc comment, the comment is copied
      so the moved field remains documented.
      @throws SourceException if the source change is rejected.
    */

    public void breakFieldGroup(boolean copyJavaDoc) throws SourceException {
        JavaDocImpl newJavaDoc = null;
        JavaDocImpl oldJavaDoc = null;

        checkNotLocked();

        PositionBounds oldBounds = bounds;
        try {
            final PositionBounds removal;
            final EditorSupport supp = bounds.getBegin().getEditorSupport();
            final StyledDocument doc = supp.openDocument();
            final PositionRef insertionPos;

            if (isLeading()) {
                // This field is the leading one.
                removal = new PositionBounds(headerBounds.getBegin(),
                                             supp.createPositionRef(bodyBounds.getEnd().getOffset() + 1, Position.Bias.Forward));
                // remove javaDoc
                if (docBounds == null) {
                    insertionPos = typeBounds.getBegin();
                } else {
                    insertionPos = docBounds.getBegin();
                    newJavaDoc = new JavaDocImpl.Field(javadoc.getRawText(), this);
                }
            } else {
                removal = new PositionBounds(
                              supp.createPositionRef(prevField.bounds.getEnd().getOffset() - 1, Position.Bias.Forward),
                              supp.createPositionRef(bounds.getEnd().getOffset() - 1, Position.Bias.Backward));
                // insertion point will be AFTER the current declaration...
                FieldElementImpl last;

                for (last = this; last.nextField != null; last = last.nextField) ;
                insertionPos = supp.createPositionRef(last.bounds.getEnd().getOffset() + 1,
		    Position.Bias.Forward);
                if (docBounds != null) {
                    newJavaDoc = new JavaDocImpl.Field(javadoc.getRawText(), this);
                }
            }

            // Since the move is required to be done atomically, I mostly copied the code
            // from CodeGenerator.regenerateElement().
            oldJavaDoc = javadoc;
            Util.ExceptionRunnable r = new Util.ExceptionRunnable() {
                                           public void run() throws Exception {
                                               PositionRef endRef = removal.getEnd();
                                               SourceElementImpl.clearBounds(removal);
                                               PositionBounds insertion = SourceElementImpl.createNewLineBoundsAt(insertionPos);
                                               StringWriter writer = new StringWriter();
                                               Writer indentWriter = Util.findIndentWriter(doc, insertion.getBegin().getOffset(), writer);
					       bounds = insertion;
                                               CodeGenerator.WholeElementPrinter printer = new CodeGenerator.WholeElementPrinter(
                                                                            indentWriter, writer, element, FieldElementImpl.this, supp);
                                               try {
                                                   element.print(printer);
                                               } catch (ElementPrinterInterruptException e) {
                                               }
                                               if (DEBUG) {
                                                   System.out.println("Inserted text: " + writer.toString());
                                               }
                                               insertion.setText(writer.toString());
					       printer.finish();
                                               if (prevField != null) {
                                                   prevField.bounds = new PositionBounds(
                                                       prevField.bounds.getBegin(), 
						       supp.createPositionRef(endRef.getOffset() + 1, Position.Bias.Backward)
                                                   );
                                                   prevField.setNextField(nextField);
                                               }
                                               if (nextField != null) {
                                                   nextField.setPreviousField(prevField);
                                               }
					       prevField = nextField = null;
                                           }
                                       };
            javadoc = newJavaDoc;
            Util.runAtomic(doc, r);
        } catch (SourceException e) {
            javadoc = oldJavaDoc;
	    bounds = oldBounds;
        } catch (Exception e) {
            javadoc = oldJavaDoc;
            throw new SourceException(e.getMessage());
        }
    }
}

/*
 * Log
 *  20   Gandalf-post-FCS1.13.2.5    4/14/00  Svatopluk Dedic Fixed initializer 
 *       generation
 *  19   Gandalf-post-FCS1.13.2.4    4/3/00   Svatopluk Dedic JavaConnection fixes
 *  18   Gandalf-post-FCS1.13.2.3    3/10/00  Svatopluk Dedic Incorrect updating of 
 *       init value fixed (NPE)
 *  17   Gandalf-post-FCS1.13.2.2    3/8/00   Svatopluk Dedic Deleted fields throw 
 *       SourceExceptions upon modification
 *  16   Gandalf-post-FCS1.13.2.1    2/24/00  Svatopluk Dedic Minor changes
 *  15   Gandalf-post-FCS1.13.2.0    2/24/00  Ian Formanek    Post FCS changes
 *  14   src-jtulach1.13        1/10/00  Petr Hamernik   regeneration of 
 *       ClassElements improved (AKA #4536)
 *  13   src-jtulach1.12        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  12   src-jtulach1.11        8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  11   src-jtulach1.10        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  10   src-jtulach1.9         6/2/99   Petr Hamernik   connections of java 
 *       sources
 *  9    src-jtulach1.8         5/17/99  Petr Hamernik   missing implementation 
 *       added
 *  8    src-jtulach1.7         5/13/99  Petr Hamernik   changes in comparing 
 *       Identifier, Type classes
 *  7    src-jtulach1.6         5/10/99  Petr Hamernik   
 *  6    src-jtulach1.5         4/21/99  Petr Hamernik   Java module updated
 *  5    src-jtulach1.4         3/29/99  Petr Hamernik   
 *  4    src-jtulach1.3         3/29/99  Petr Hamernik   
 *  3    src-jtulach1.2         3/10/99  Petr Hamernik   
 *  2    src-jtulach1.1         2/25/99  Petr Hamernik   
 *  1    src-jtulach1.0         2/18/99  Petr Hamernik   
 * $
 */
