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

package org.netbeans.modules.javadoc.comments;

import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import java.beans.PropertyEditor;

import org.openide.nodes.*;
import org.openide.src.*;
import org.openide.src.nodes.FilterFactory;
import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;


/** This FilterFactory descendant adds the JavaDoc property to every Method, Constructor, Field and
 * class node create for JavaDataObjects
 *
 * @author Petr Hrebejk
 */

public class JavaDocPropertySupportFactory extends FilterFactory {

    protected static final ResourceBundle bundle = NbBundle.getBundle( JavaDocPropertySupportFactory.class );

    private static final String PROP_JAVADOCCOMMENT = "javadocComment"; // NOI18N

    public Node createMethodNode(MethodElement element) {
        Node node = super.createMethodNode( element );
        JavaDocPropertySupportFactory.addMethodJavaDocProperty( node );
        return node;
    }

    public Node createConstructorNode(ConstructorElement element) {
        Node node = super.createConstructorNode( element );
        JavaDocPropertySupportFactory.addMethodJavaDocProperty( node );
        return node;
    }

    public Node createFieldNode(FieldElement element) {
        Node node = super.createFieldNode( element );
        JavaDocPropertySupportFactory.addFieldJavaDocProperty( node );
        return node;
    }

    public Node createClassNode(final ClassElement element) {
        Node node = super.createClassNode( element );
        JavaDocPropertySupportFactory.addClassJavaDocProperty( node );
        return node;
    }

    // UTILITY FUNCTIONS ------------------------------------------------------------------------------

    /** Adds JavaDoc property to node for class
     */

    static void addClassJavaDocProperty( final Node node ) {

        // PENDING: THIS IS A WORKAROUND FOR ADDING NEW PROPERTIES ONLY
        //          TO JAVA NODES NOT TO CLAZZ NODES

        Node.PropertySet [] ps = node.getPropertySets();

        for ( int i = 0; i < ps.length; i++ ) {
            if ( ps[i].getName().equals( Sheet.PROPERTIES ) && ps[i] instanceof Sheet.Set ) {
                ((Sheet.Set)ps[i]).put(createClassJavaDocProperty( true, node ));
            }
        }
    }

    /** Create a node property for javadoc comment for class element
    * @param canW if <code>false</code>, property will be read-only
    * @return the property
    */ 
    static protected Node.Property createClassJavaDocProperty(boolean canW, final Node node ) {
        JavaDocPropertySupport prop = new JavaDocPropertySupport(node, PROP_JAVADOCCOMMENT, JavaDoc.Class.class, true) {

                                          //  Gets the value
                                          public Object getValue () {
                                              ClassElement me = (ClassElement)node.getCookie( ClassElement.class );
                                              return me.getJavaDoc();
                                          }

                                          // Sets the value
                                          public void setValue(Object val) throws IllegalArgumentException,
                                              IllegalAccessException, InvocationTargetException {

                                              super.setValue(val);

                                              try {
                                                  final ClassElement me = (ClassElement)node.getCookie( ClassElement.class );
                                                  final String rt = ((JavaDoc) val).getRawText();
                                                  final SourceException[] sex = { null };
                                                  SourceElement source = me.getSource();
                                                  if ( source != null ) {
                                                      source.runAtomicAsUser( new Runnable() {
                                                                                  public void run() {
                                                                                      try {
                                                                                          me.getJavaDoc().setRawText( rt );
                                                                                      }
                                                                                      catch ( SourceException e ){
                                                                                          sex[0] = e;
                                                                                      }
                                                                                  }
                                                                              } );
                                                      if ( sex[0] != null ) {
                                                          NotifyDescriptor nd = new NotifyDescriptor.Message(
                                                                                    this.bundle.getString( "MSG_WriteToGuardedBlock" ), NotifyDescriptor.ERROR_MESSAGE );
                                                          TopManager.getDefault().notify( nd );
                                                      }
                                                  }
                                              }
                                              catch (SourceException e) {
                                                  throw new InvocationTargetException(e);
                                              }
                                              catch (ClassCastException e) {
                                                  throw new IllegalArgumentException();
                                              }

                                          }

                                          public PropertyEditor getPropertyEditor() {
                                              ClassElement me = (ClassElement)node.getCookie( ClassElement.class );
                                              return new JavaDocEditor( me );
                                          }

                                      };

        return prop;
    }




    /** Adds JavaDoc property to node for method
     */

    static void addMethodJavaDocProperty( final Node node ) {

        // PENDING: THIS IS A WORKAROUND FOR ADDING NEW PROPERTIES ONLY
        //          TO JAVA NODES NOT TO CLAZZ NODES

        Node.PropertySet [] ps = node.getPropertySets();

        for ( int i = 0; i < ps.length; i++ ) {
            if ( ps[i].getName().equals( Sheet.PROPERTIES ) && ps[i] instanceof Sheet.Set ) {
                ((Sheet.Set)ps[i]).put(createMethodJavaDocProperty( true, node ));
            }
        }
    }

    /** Create a node property for javadoc comment for field element
    * @param canW if <code>false</code>, property will be read-only
    * @return the property
    */ 
    static protected Node.Property createMethodJavaDocProperty(boolean canW, final Node node ) {
        JavaDocPropertySupport prop = new JavaDocPropertySupport(node, PROP_JAVADOCCOMMENT, JavaDoc.Method.class, true) {

                                          //  Gets the value
                                          public Object getValue () {
                                              ConstructorElement me = (ConstructorElement)node.getCookie( ConstructorElement.class );
                                              return me.getJavaDoc();
                                          }

                                          // Sets the value
                                          public void setValue(Object val) throws IllegalArgumentException,
                                              IllegalAccessException, InvocationTargetException {

                                              super.setValue(val);

                                              try {
                                                  final ConstructorElement me = (ConstructorElement)node.getCookie( ConstructorElement.class );
                                                  final String rt = ((JavaDoc)val).getRawText();
                                                  final SourceException[] sex = { null };
                                                  SourceElement source = me.getDeclaringClass().getSource();
                                                  if ( source != null ) {
                                                      source.runAtomicAsUser( new Runnable() {
                                                                                  public void run() {
                                                                                      try {
                                                                                          me.getJavaDoc().setRawText( rt );
                                                                                      }
                                                                                      catch ( SourceException e ){
                                                                                          sex[0] = e;
                                                                                      }
                                                                                  }
                                                                              } );
                                                      if ( sex[0] != null ) {
                                                          NotifyDescriptor nd = new NotifyDescriptor.Message(
                                                                                    this.bundle.getString( "MSG_WriteToGuardedBlock" ), NotifyDescriptor.ERROR_MESSAGE );
                                                          TopManager.getDefault().notify( nd );
                                                      }
                                                  }
                                              }
                                              catch (SourceException e) {
                                                  throw new InvocationTargetException(e);
                                              }
                                              catch (ClassCastException e) {
                                                  throw new IllegalArgumentException();
                                              }

                                          }

                                          public PropertyEditor getPropertyEditor() {
                                              ConstructorElement me = (ConstructorElement)node.getCookie( ConstructorElement.class );
                                              return new JavaDocEditor( me );
                                          }

                                      };

        return prop;
    }



    /** Adds JavaDoc property to node for field
     */
    static void addFieldJavaDocProperty( final Node node ) {

        // PENDING: THIS IS A WORKAROUND FOR ADDING NEW PROPERTIES ONLY
        //          TO JAVA NODES NOT TO CLAZZ NODES

        Node.PropertySet [] ps = node.getPropertySets();

        for ( int i = 0; i < ps.length; i++ ) {
            if ( ps[i].getName().equals( Sheet.PROPERTIES ) && ps[i] instanceof Sheet.Set ) {
                ((Sheet.Set)ps[i]).put(createFieldJavaDocProperty( true, node ));
            }
        }
    }

    /** Create a node property for javadoc comment for method element
     * @param canW if <code>false</code>, property will be read-only
     * @return the property
     */ 
    static protected Node.Property createFieldJavaDocProperty(boolean canW, final Node node ) {
        JavaDocPropertySupport prop = new JavaDocPropertySupport(node, PROP_JAVADOCCOMMENT, JavaDoc.Field.class, true) {

                                          //  Gets the value
                                          public Object getValue () {
                                              FieldElement me = (FieldElement)node.getCookie( FieldElement.class );
                                              return me.getJavaDoc();
                                          }

                                          // Sets the value
                                          public void setValue(Object val) throws IllegalArgumentException,
                                              IllegalAccessException, InvocationTargetException {

                                              super.setValue(val);

                                              try {
                                                  final FieldElement me = (FieldElement)node.getCookie( FieldElement.class );
                                                  final String rt = ((JavaDoc) val).getRawText();
                                                  final SourceException[] sex = { null };
                                                  SourceElement source = me.getDeclaringClass().getSource();
                                                  if ( source != null ) {
                                                      source.runAtomicAsUser( new Runnable() {
                                                                                  public void run() {
                                                                                      try {
                                                                                          me.getJavaDoc().setRawText( rt );
                                                                                      }
                                                                                      catch ( SourceException e ){
                                                                                          sex[0] = e;
                                                                                      }
                                                                                  }
                                                                              } );
                                                      if ( sex[0] != null ) {
                                                          NotifyDescriptor nd = new NotifyDescriptor.Message(
                                                                                    this.bundle.getString( "MSG_WriteToGuardedBlock" ), NotifyDescriptor.ERROR_MESSAGE );
                                                          TopManager.getDefault().notify( nd );
                                                      }
                                                  }
                                              }
                                              catch (SourceException e) {
                                                  throw new InvocationTargetException(e);
                                              }
                                              catch (ClassCastException e) {
                                                  throw new IllegalArgumentException();
                                              }

                                          }

                                          public PropertyEditor getPropertyEditor() {
                                              FieldElement me = (FieldElement)node.getCookie( FieldElement.class );
                                              return new JavaDocEditor( me );
                                          }

                                      };

        return prop;
    }
}

/*
 * Log
 *  5    Gandalf   1.4         1/12/00  Petr Hrebejk    i18n
 *  4    Gandalf   1.3         1/4/00   Petr Hrebejk    Bug fix 5007
 *  3    Gandalf   1.2         1/3/00   Petr Hrebejk    Various bugfixes - 4709,
 *       4978, 5017, 4981, 4976, 5016, 4740,  5005
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         7/9/99   Petr Hrebejk    
 * $ 
 */ 