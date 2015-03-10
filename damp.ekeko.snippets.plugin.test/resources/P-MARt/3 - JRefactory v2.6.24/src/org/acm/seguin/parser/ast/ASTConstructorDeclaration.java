/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.parser.ast;

import java.text.MessageFormat;

import org.acm.seguin.parser.JavaParser;
import org.acm.seguin.parser.JavaParserVisitor;
import org.acm.seguin.pretty.ForceJavadocComments;
import org.acm.seguin.pretty.JavaDocComponent;
import org.acm.seguin.pretty.JavaDocable;
import org.acm.seguin.pretty.JavaDocableImpl;
import org.acm.seguin.pretty.ModifierHolder;
import org.acm.seguin.pretty.PrintData;
import org.acm.seguin.pretty.ai.RequiredTags;
import org.acm.seguin.util.FileSettings;

/**
 *  Description of the Class 
 *
 *@author     Chris Seguin 
 *@created    October 13, 1999 
 */
public class ASTConstructorDeclaration extends SimpleNode implements JavaDocable {
	//  Instance Variables
	private String name;
	private ModifierHolder modifiers;
	private JavaDocableImpl jdi;


	/**
	 *  Constructor for the ASTConstructorDeclaration object 
	 *
	 *@param  id  Description of Parameter 
	 */
	public ASTConstructorDeclaration(int id) {
		super(id);
		modifiers = new ModifierHolder();
		jdi = new JavaDocableImpl();
	}


	/**
	 *  Constructor for the ASTConstructorDeclaration object 
	 *
	 *@param  p   Description of Parameter 
	 *@param  id  Description of Parameter 
	 */
	public ASTConstructorDeclaration(JavaParser p, int id) {
		super(p, id);
		modifiers = new ModifierHolder();
		jdi = new JavaDocableImpl();
	}


	/**
	 *  Set the object's name 
	 *
	 *@param  newName  the new name 
	 */
	public void setName(String newName) {
		name = newName.intern();
	}


	/**
	 *  Determine if the object is abstract 
	 *
	 *@return    true if this stores an ABSTRACT flag 
	 */
	public boolean isAbstract() {
		return modifiers.isAbstract();
	}


	/**
	 *  Determine if the object is explicit 
	 *
	 *@return    true if this stores an EXPLICIT flag 
	 */
	public boolean isExplicit() {
		return modifiers.isExplicit();
	}


	/**
	 *  Determine if the object is final 
	 *
	 *@return    true if this stores an FINAL flag 
	 */
	public boolean isFinal() {
		return modifiers.isFinal();
	}


	/**
	 *  Determine if the object is interface 
	 *
	 *@return    true if this stores an INTERFACE flag 
	 */
	public boolean isInterface() {
		return modifiers.isInterface();
	}


	/**
	 *  Determine if the object is native 
	 *
	 *@return    true if this stores an NATIVE flag 
	 */
	public boolean isNative() {
		return modifiers.isNative();
	}


	/**
	 *  Determine if the object is private 
	 *
	 *@return    true if this stores an PRIVATE flag 
	 */
	public boolean isPrivate() {
		return modifiers.isPrivate();
	}


	/**
	 *  Determine if the object is protected 
	 *
	 *@return    true if this stores an PROTECTED flag 
	 */
	public boolean isProtected() {
		return modifiers.isProtected();
	}


	/**
	 *  Determine if the object is public 
	 *
	 *@return    true if this stores an PUBLIC flag 
	 */
	public boolean isPublic() {
		return modifiers.isPublic();
	}


	/**
	 *  Determine if the object is static 
	 *
	 *@return    true if this stores an static flag 
	 */
	public boolean isStatic() {
		return modifiers.isStatic();
	}


	/**
	 *  Determine if the object is strict 
	 *
	 *@return    true if this stores an STRICT flag 
	 */
	public boolean isStrict() {
		return modifiers.isStrict();
	}


	/**
	 *  Determine if the object is synchronized 
	 *
	 *@return    true if this stores an SYNCHRONIZED flag 
	 */
	public boolean isSynchronized() {
		return modifiers.isSynchronized();
	}


	/**
	 *  Determine if the object is transient 
	 *
	 *@return    true if this stores an TRANSIENT flag 
	 */
	public boolean isTransient() {
		return modifiers.isTransient();
	}


	/**
	 *  Determine if the object is volatile 
	 *
	 *@return    true if this stores an VOLATILE flag 
	 */
	public boolean isVolatile() {
		return modifiers.isVolatile();
	}


	/**
	 *  Returns a string containing all the modifiers 
	 *
	 *@return    the iterator 
	 */
	public String getModifiersString() {
		return modifiers.toString();
	}


	/**
	 *  Returns the modifier holder 
	 *
	 *@return    the holder 
	 */
	public ModifierHolder getModifiers() {
		return modifiers;
	}


	/**
	 *  Get the object's name 
	 *
	 *@return    the name 
	 */
	public String getName() {
		return name;
	}


	/**
	 *  Checks to see if it was printed 
	 *
	 *@return    true if it still needs to be printed 
	 */
	public boolean isRequired() {
		//  Check if it is required
		ForceJavadocComments fjc = new ForceJavadocComments();
		return jdi.isRequired() && 
				fjc.isJavaDocRequired("method", modifiers);
	}


	/**
	 *  Adds a modifier to a class 
	 *
	 *@param  modifier  the next modifier 
	 */
	public void addModifier(String modifier) {
		modifiers.add(modifier);
	}


	/**
	 *  Convert this object to a string 
	 *
	 *@return    a string representing this object 
	 */
	public String toString() {
		return super.toString() + " [" + getModifiersString() + " " + 
				getName() + "]";
	}



	/**
	 *  Allows you to add a java doc component 
	 *
	 *@param  component  the component that can be added 
	 */
	public void addJavaDocComponent(JavaDocComponent component) {
		jdi.addJavaDocComponent(component);
	}


	/**
	 *  Prints all the java doc components 
	 *
	 *@param  printData  the print data 
	 */
	public void printJavaDocComponents(PrintData printData) {
		FileSettings bundle = FileSettings.getSettings("Refactory", "pretty");
		jdi.printJavaDocComponents(printData, bundle.getString("method.tags"));
	}


	/**
	 *  Makes sure all the java doc components are present. For methods and 
	 *  constructors we need to do more work - checking parameters, return types, 
	 *  and exceptions. 
	 */
	public void finish() {
		//  Local Variables
		int ndx;
		int childCount;

		//  Get the resource bundle
		FileSettings bundle = FileSettings.getSettings("Refactory", "pretty");

		//  Description of the constructor
		Object[] nameArray = new Object[1];
		nameArray[0] = getName();
		String msg = MessageFormat.format(bundle.getString("constructor.descr"), nameArray);
		jdi.require("", msg);

		//  Check for parameters
		ASTFormalParameters params = (ASTFormalParameters) jjtGetChild(0);
		childCount = params.jjtGetNumChildren();
		for (ndx = 0; ndx < childCount; ndx++) {
			ASTFormalParameter nextParam = (ASTFormalParameter) params.jjtGetChild(ndx);
			ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) nextParam.jjtGetChild(1);
			jdi.require("@param", id.getName(), bundle.getString("param.descr"));
		}

		//  Check for exceptions
		if ((jjtGetNumChildren() > 1) && (jjtGetChild(1) instanceof ASTNameList)) {
			ASTNameList exceptions = (ASTNameList) jjtGetChild(1);
			childCount = exceptions.jjtGetNumChildren();
			for (ndx = 0; ndx < childCount; ndx++) {
				ASTName name = (ASTName) exceptions.jjtGetChild(ndx);
				jdi.require("@exception", name.getName(), bundle.getString("exception.descr"));
			}
		}

		//  Require the other tags
		RequiredTags.getTagger().addTags(bundle, "method", getName(), jdi);
	}


	/**
	 *  Accept the visitor. 
	 *
	 *@param  visitor  Description of Parameter 
	 *@param  data     Description of Parameter 
	 *@return          Description of the Returned Value 
	 */
	public Object jjtAccept(JavaParserVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}
