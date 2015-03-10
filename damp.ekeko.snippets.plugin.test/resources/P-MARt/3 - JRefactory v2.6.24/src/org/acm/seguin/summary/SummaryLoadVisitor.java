/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.summary;

import org.acm.seguin.parser.Node;
import org.acm.seguin.parser.ast.ASTAllocationExpression;
import org.acm.seguin.parser.ast.ASTArguments;
import org.acm.seguin.parser.ast.ASTBlock;
import org.acm.seguin.parser.ast.ASTBlockStatement;
import org.acm.seguin.parser.ast.ASTClassBody;
import org.acm.seguin.parser.ast.ASTClassDeclaration;
import org.acm.seguin.parser.ast.ASTConstructorDeclaration;
import org.acm.seguin.parser.ast.ASTExplicitConstructorInvocation;
import org.acm.seguin.parser.ast.ASTFieldDeclaration;
import org.acm.seguin.parser.ast.ASTFormalParameter;
import org.acm.seguin.parser.ast.ASTFormalParameters;
import org.acm.seguin.parser.ast.ASTImportDeclaration;
import org.acm.seguin.parser.ast.ASTInitializer;
import org.acm.seguin.parser.ast.ASTInterfaceBody;
import org.acm.seguin.parser.ast.ASTInterfaceDeclaration;
import org.acm.seguin.parser.ast.ASTLocalVariableDeclaration;
import org.acm.seguin.parser.ast.ASTMethodDeclaration;
import org.acm.seguin.parser.ast.ASTMethodDeclarator;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTNameList;
import org.acm.seguin.parser.ast.ASTNestedClassDeclaration;
import org.acm.seguin.parser.ast.ASTNestedInterfaceDeclaration;
import org.acm.seguin.parser.ast.ASTPackageDeclaration;
import org.acm.seguin.parser.ast.ASTPrimaryExpression;
import org.acm.seguin.parser.ast.ASTPrimaryPrefix;
import org.acm.seguin.parser.ast.ASTPrimarySuffix;
import org.acm.seguin.parser.ast.ASTPrimitiveType;
import org.acm.seguin.parser.ast.ASTResultType;
import org.acm.seguin.parser.ast.ASTStatement;
import org.acm.seguin.parser.ast.ASTSwitchStatement;
import org.acm.seguin.parser.ast.ASTType;
import org.acm.seguin.parser.ast.ASTTypeDeclaration;
import org.acm.seguin.parser.ast.ASTUnmodifiedClassDeclaration;
import org.acm.seguin.parser.ast.ASTUnmodifiedInterfaceDeclaration;
import org.acm.seguin.parser.ast.ASTVariableDeclaratorId;
import org.acm.seguin.parser.ast.SimpleNode;

/**
 *  This object visits an abstract syntax tree with the purpose of gathering
 *  summary information.
 *
 *@author     Chris Seguin
 *@created    May 30, 1999
 */
public class SummaryLoadVisitor extends LineCountVisitor {
	private int anonCount = 1;


	/**
	 *  Visits a package declaration
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTPackageDeclaration node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		//  Get the name
		ASTName name = (ASTName) node.jjtGetChild(0);

		//  Lookup the summary
		PackageSummary packageSummary = PackageSummary.getPackageSummary(name.getName());

		//  Create the file summary
		if (state.getCode() == SummaryLoaderState.INITIALIZE) {
			state.startSummary(new FileSummary(packageSummary, state.getFile()));
			state.setCode(SummaryLoaderState.LOAD_FILE);
		}

		return super.visit(node, data);
	}


	/**
	 *  Visits an import statement
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTImportDeclaration node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		//  Get the current file summary - add the import
		FileSummary current = (FileSummary) state.getCurrentSummary();
		ImportSummary importSummary = new ImportSummary(current, node);
		current.add(importSummary);

		int start = getLineCount() + 1;

		//  Done
		Object obj = super.visit(node, data);

		int end = getLineCount();
		importSummary.setStartLine(start);
		importSummary.setEndLine(end);

		return obj;
	}


	/**
	 *  Visits a type declaration
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTTypeDeclaration node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int start = getLineCount() + 1;

		//  Get the current file summary
		FileSummary current = (FileSummary) state.getCurrentSummary();

		//  Create Type Summary
		TypeSummary next = new TypeSummary(current, node);
		current.add(next);

		//  Set the next type summary as the current summary
		state.startSummary(next);
		int oldCode = state.getCode();
		state.setCode(SummaryLoaderState.LOAD_TYPE);

		//  Traverse Children
		super.visit(node, data);

		//  Back to loading the file
		state.finishSummary();
		state.setCode(oldCode);

		int end = getLineCount();
		next.setStartLine(start);
		next.setEndLine(end);

		//  Done
		return data;
	}


	/**
	 *  Visits a class declaration
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTClassDeclaration node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int start = getLineCount() + 1;

		//  Get the current type summary
		TypeSummary current = (TypeSummary) state.getCurrentSummary();

		//  Save the modifiers
		current.setModifiers(node.getModifiers());

		//  Traverse the children
		return super.visit(node, data);
	}


	/**
	 *  Visits a class declaration
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTUnmodifiedClassDeclaration node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int start = getLineCount() + 1;

		int code = state.getCode();

		//  Get the current type summary
		Summary currentSummary = state.getCurrentSummary();
		TypeSummary current;

		if (currentSummary instanceof TypeSummary) {
			current = (TypeSummary) currentSummary;
		}
		else {
			//  Create Type Summary
			current = new TypeSummary(currentSummary, node);
			((MethodSummary) currentSummary).addDependency(current);
			state.startSummary(current);
		}

		//  Remember the name
		current.setName(node.getName().intern());

		//  Iterate through the children
		//  Add the class body
		state.setCode(SummaryLoaderState.LOAD_TYPE);
		super.visit(node, data);

		state.setCode(code);

		//  This is a class
		current.setInterface(false);

		if (currentSummary instanceof TypeSummary) {
		}
		else {
			state.finishSummary();
		}

		return data;
	}


	/**
	 *  Visit the items in the class body
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTClassBody node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int start = getLineCount() + 1;

		int oldCode = state.getCode();

		state.setCode(SummaryLoaderState.LOAD_CLASSBODY);

		super.visit(node, data);

		state.setCode(oldCode);

		return data;
	}


	/**
	 *  Visit a class that is nested in another class
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTNestedClassDeclaration node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int start = getLineCount() + 1;

		int code = state.getCode();

		//  Get the current type summary
		Summary current = state.getCurrentSummary();

		TypeSummary nested = new TypeSummary(current, node);

		//  Add it in
		if (current instanceof TypeSummary) {
			((TypeSummary) current).add(nested);
		}
		else {
			((MethodSummary) current).addDependency(nested);
		}

		//  Continue deeper
		state.startSummary(nested);
		state.setCode(SummaryLoaderState.LOAD_TYPE);

		//  Save the modifiers
		nested.setModifiers(node.getModifiers());

		//  Traverse the children
		super.visit(node, data);

		int end = getLineCount();
		nested.setStartLine(start);
		nested.setEndLine(end);

		//  Finish the summary
		state.finishSummary();
		state.setCode(code);

		//  Return something
		return data;
	}


	/**
	 *  Visit an interface declaration
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTInterfaceDeclaration node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int start = getLineCount() + 1;

		//  Get the current type summary
		TypeSummary current = (TypeSummary) state.getCurrentSummary();

		//  Save the modifiers
		current.setModifiers(node.getModifiers());

		//  Traverse the children
		return super.visit(node, data);
	}


	/**
	 *  Visit an interface that is nested in a class
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTNestedInterfaceDeclaration node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int start = getLineCount() + 1;

		int code = state.getCode();

		//  Get the current type summary
		Summary current = state.getCurrentSummary();

		//  Create the new nested type summary
		TypeSummary nested = new TypeSummary(current, node);

		//  Add it in
		if (current instanceof TypeSummary) {
			((TypeSummary) current).add(nested);
		}
		else {
			((MethodSummary) current).addDependency(nested);
		}

		//  Continue deeper
		state.startSummary(nested);
		state.setCode(SummaryLoaderState.LOAD_TYPE);

		//  Save the modifiers
		nested.setModifiers(node.getModifiers());

		//  Traverse the children
		super.visit(node, data);

		int end = getLineCount();
		nested.setStartLine(start);
		nested.setEndLine(end);

		//  Finish the summary
		state.finishSummary();
		state.setCode(code);

		//  Return something
		return data;
	}


	/**
	 *  Visit an interface
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTUnmodifiedInterfaceDeclaration node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int start = getLineCount() + 1;

		int code = state.getCode();

		//  Get the current type summary
		Summary currentSummary = state.getCurrentSummary();
		TypeSummary current;

		if (currentSummary instanceof TypeSummary) {
			current = (TypeSummary) currentSummary;
		}
		else {
			//  Create Type Summary
			current = new TypeSummary(currentSummary, node);
			((MethodSummary) currentSummary).addDependency(current);
			state.startSummary(current);
		}

		//  Remember the name
		current.setName(node.getName().intern());

		//  Iterate through the children of the interface
		state.setCode(SummaryLoaderState.LOAD_TYPE);

		super.visit(node, data);

		state.setCode(code);

		//  This is an interface
		current.setInterface(true);

		if (currentSummary instanceof TypeSummary) {
		}
		else {
			state.finishSummary();
		}

		//  Done
		return data;
	}


	/**
	 *  Visit the body of an interface
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTInterfaceBody node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int start = getLineCount() + 1;

		int oldCode = state.getCode();

		state.setCode(SummaryLoaderState.LOAD_CLASSBODY);

		super.visit(node, data);

		state.setCode(oldCode);

		return data;
	}


	/**
	 *  Visit a field declaration
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTFieldDeclaration node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int start = getLineCount() + 1;
		int code = state.getCode();
		state.setCode(SummaryLoaderState.IGNORE);
		countFieldStart(node, data);
		state.setCode(code);

		//  Get the current type summary
		TypeSummary current = (TypeSummary) state.getCurrentSummary();

		//  Local Variables
		FieldSummary result = null;
		int last = node.jjtGetNumChildren();
		ASTType type = (ASTType) node.jjtGetChild(0);

		//  Create a summary for each field
		for (int ndx = 1; ndx < last; ndx++) {
			Node next = node.jjtGetChild(ndx);
			result = new FieldSummary(current, type,
					(ASTVariableDeclaratorId) next.jjtGetChild(0));

			//  Count anything on the declarator
			state.setCode(SummaryLoaderState.IGNORE);
			next.jjtGetChild(0).jjtAccept(this, data);
			state.setCode(code);

			//  Continue setting up the field summary
			result.setModifiers(node.getModifiers());
			result.setStartLine(start);

			//  Load the initializer
			if (next.jjtGetNumChildren() > 1) {
				loadInitializer(current, state,
						(SimpleNode) next.jjtGetChild(1),
						node.getModifiers().isStatic());
			}

			//  Finish setting variables for the field summary
			countLines(node.getSpecial("comma" + (ndx - 2)));
			result.setEndLine(getLineCount());
			current.add(result);
		}
		countLines(node.getSpecial("semicolon"));
		if (result != null) {
			result.setEndLine(getLineCount());
		}

		return data;
	}


	/**
	 *  Visits a method
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTMethodDeclaration node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int start = getLineCount() + 1;

		countMethodHeader(node, data);

		int declStart = getLineCount();

		int code = state.getCode();

		if (code == SummaryLoaderState.LOAD_CLASSBODY) {
			ASTMethodDeclarator decl = (ASTMethodDeclarator) node.jjtGetChild(1);

			//  Get the current type summary
			TypeSummary current = (TypeSummary) state.getCurrentSummary();
			MethodSummary methodSummary = new MethodSummary(current);
			state.startSummary(methodSummary);
			current.add(methodSummary);

			//  Load the method summary
			//  Remember the modifiers
			methodSummary.setModifiers(node.getModifiers());

			//  Load the method names
			methodSummary.setName(decl.getName());

			//  Load the return type
			loadMethodReturn(node, methodSummary, decl.getArrayCount());

			//  Load the parameters
			loadMethodParams(decl, state);

			//  Load the exceptions
			loadMethodExceptions(node, state, 2);

			//  Initialize the dependency list
			loadMethodBody(node, state);

			//  Done
			state.setCode(SummaryLoaderState.LOAD_CLASSBODY);
			state.finishSummary();

			int end = getLineCount();
			methodSummary.setStartLine(start);
			methodSummary.setDeclarationLine(declStart);
			methodSummary.setEndLine(end);

			return data;
		}
		else {
			System.out.println("Encountered a method in state:  " + code);
			return data;
		}
	}


	/**
	 *  Visit a formal parameter
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTFormalParameter node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int start = getLineCount() + 1;

		int code = state.getCode();

		if (code == SummaryLoaderState.LOAD_PARAMETERS) {
			//  Local Variables
			MethodSummary methodSummary = (MethodSummary) state.getCurrentSummary();

			//  For each variable create a summary
			methodSummary.add(new ParameterSummary(methodSummary, (ASTType) node.jjtGetChild(0),
					(ASTVariableDeclaratorId) node.jjtGetChild(1)));

			//  Continue with the state
			return state;
		}
		else {
			//  Get the parent
			MethodSummary parent = (MethodSummary) state.getCurrentSummary();

			//  Add the dependency
			parent.addDependency(new ParameterSummary(parent, (ASTType) node.jjtGetChild(0),
					(ASTVariableDeclaratorId) node.jjtGetChild(1)));

			//  Return something
			return data;
		}
	}


	/**
	 *  Visit a constructor
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTConstructorDeclaration node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int code = state.getCode();

		if (code == SummaryLoaderState.LOAD_CLASSBODY) {
			int start = getLineCount() + 1;

			countConstructorHeader(node);

			int declStart = getLineCount();

			//  Get the current type summary
			TypeSummary current = (TypeSummary) state.getCurrentSummary();
			MethodSummary methodSummary = new MethodSummary(current);
			current.add(methodSummary);
			state.startSummary(methodSummary);

			//  Load the constructor
			//  Remember the modifiers
			methodSummary.setModifiers(node.getModifiers());

			//  Load the method names
			methodSummary.setName(node.getName());

			//  Load the parameters
			loadMethodParams(node, state);

			//  Load the exceptions
			loadMethodExceptions(node, state, 1);

			//  Initialize the dependency list
			methodSummary.beginBlock();
			state.setCode(SummaryLoaderState.LOAD_METHODBODY);
			int last = node.jjtGetNumChildren();
			for (int ndx = 1; ndx < last; ndx++) {
				SimpleNode body = (SimpleNode) node.jjtGetChild(ndx);
				if (body instanceof ASTExplicitConstructorInvocation) {
					body.jjtAccept(this, data);
				}
				else if (body instanceof ASTBlockStatement) {
					body.jjtAccept(this, data);
				}
			}
			methodSummary.endBlock();

			//  Done
			state.setCode(SummaryLoaderState.LOAD_CLASSBODY);
			state.finishSummary();

			countLines(node.getSpecial("end"));
			int end = getLineCount();
			methodSummary.setStartLine(start);
			methodSummary.setDeclarationLine(declStart);
			methodSummary.setEndLine(end);

			return data;
		}
		else {
			System.out.println("Encountered a constructor in state:  " + code);
			return data;
		}
	}


	/**
	 *  Visit an initializer
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTInitializer node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int start = getLineCount() + 1;

		int code = state.getCode();

		if (code == SummaryLoaderState.LOAD_CLASSBODY) {
			//  Get the current type summary
			TypeSummary current = (TypeSummary) state.getCurrentSummary();

			int last = node.jjtGetNumChildren();
			SimpleNode body = (SimpleNode) node.jjtGetChild(last - 1);
			if (body instanceof ASTBlock) {
				loadInitializer(current, state, body, node.isUsingStatic());
			}

			return data;
		}
		else {
			System.out.println("Encountered a method in state:  " + code);
			return data;
		}
	}


	/**
	 *  Visit a type
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTType node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		MethodSummary parent = (MethodSummary) state.getCurrentSummary();

		//  Add the dependency
		parent.addDependency(TypeDeclSummary.getTypeDeclSummary(parent, node));

		//  Return the data
		return super.visit(node, data);
	}


	/**
	 *  Visit a return type
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTResultType node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int start = getLineCount() + 1;

		MethodSummary parent = (MethodSummary) state.getCurrentSummary();

		//  Add the dependency
		parent.addDependency(TypeDeclSummary.getTypeDeclSummary(parent, node));

		//  Return the data
		return data;
	}


	/**
	 *  Visit a name
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTName node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int code = state.getCode();

		//  Get the current type summary
		if (code == SummaryLoaderState.LOAD_TYPE) {
			TypeSummary current = (TypeSummary) state.getCurrentSummary();
			current.setParentClass(new TypeDeclSummary(current, node));
		}

		return super.visit(node, data);
	}


	/**
	 *  Visit a list of names
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTNameList node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int code = state.getCode();

		//  Get the current type summary
		if (code == SummaryLoaderState.LOAD_TYPE) {
			TypeSummary current = (TypeSummary) state.getCurrentSummary();

			//  Local Variables
			int last = node.jjtGetNumChildren();

			//  For each interface it implements create a summary
			state.setCode(SummaryLoaderState.IGNORE);
			for (int ndx = 0; ndx < last; ndx++) {
				countLines(node.getSpecial("comma." + (ndx - 1)));
				ASTName next = (ASTName) node.jjtGetChild(ndx);
				current.add(new TypeDeclSummary(current, next));
				next.jjtAccept(this, data);
			}
			state.setCode(code);

			return data;
		}
		else if (code == SummaryLoaderState.LOAD_EXCEPTIONS) {
			//  Local Variables
			int last = node.jjtGetNumChildren();
			MethodSummary methodSummary = (MethodSummary) state.getCurrentSummary();

			//  For each variable create a summary
			state.setCode(SummaryLoaderState.IGNORE);
			for (int ndx = 0; ndx < last; ndx++) {
				countLines(node.getSpecial("comma." + (ndx - 1)));
				ASTName next = (ASTName) node.jjtGetChild(ndx);
				methodSummary.add(new TypeDeclSummary(methodSummary, next));
				next.jjtAccept(this, data);
			}
			state.setCode(code);

			//  Return something
			return data;
		}
		else {
			return super.visit(node, data);
		}
	}


	/**
	 *  Visit an expression
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTPrimaryExpression node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int start = getLineCount() + 1;

		//  Local Variables
		ASTName name;

		//  Check out the prefix
		ASTPrimaryPrefix prefix = (ASTPrimaryPrefix) node.jjtGetChild(0);
		if (!prefix.hasAnyChildren()) {
			//  Count the lines
			countLines(node.getSpecial("this"));
			countLines(node.getSpecial("id"));

			//  It is entirely controlled by the name of the node
			String prefixName = prefix.getName();

			//  Create the name
			name = new ASTName(0);

			//  Check out the name
			if (prefixName.equals("this")) {
				name.addNamePart(prefixName);
			}
			else {
				name.addNamePart("super");
				name.addNamePart(prefixName.substring(7, prefixName.length()));
			}
		}
		else if (prefix.jjtGetChild(0) instanceof ASTName) {
			//  Count the items in the name
			int code = state.getCode();
			state.setCode(SummaryLoaderState.IGNORE);
			super.visit(prefix, data);
			state.setCode(code);

			//  Remember the name
			name = (ASTName) prefix.jjtGetChild(0);
		}
		else {
			//  Our work is done here
			return super.visit(node, data);
		}

		//  Get the parent
		MethodSummary parent = (MethodSummary) state.getCurrentSummary();

		//  Check out the suffix
		boolean isMessageSend = false;
		int suffixCount = node.jjtGetNumChildren();
		boolean sentLast = false;
		if (suffixCount > 1) {
			for (int ndx = 1; ndx < suffixCount; ndx++) {
				ASTPrimarySuffix suffix = (ASTPrimarySuffix) node.jjtGetChild(ndx);
				if (!suffix.hasAnyChildren()) {
					//  Count the lines
					countLines(node.getSpecial("dot"));
					countLines(node.getSpecial("id"));

					name = new ASTName(0);
					name.addNamePart(suffix.getName());
					sentLast = false;
				}
				else if (suffix.jjtGetChild(0) instanceof ASTArguments) {
					addAccess(parent, name, true);
					sentLast = true;
					super.visit((SimpleNode) suffix.jjtGetChild(0), data);
				}
				else if (!sentLast) {
					addAccess(parent, name, false);
					sentLast = true;

					//  Count the lines
					countLines(node.getSpecial("["));
					countLines(node.getSpecial("]"));
					countLines(node.getSpecial("dot"));
					countLines(node.getSpecial("id"));

					super.visit((SimpleNode) suffix.jjtGetChild(0), data);
				}
			}
		}

		//  Get the parent
		if (!sentLast) {
			addAccess(parent, name, false);
		}

		//  Return some value
		return data;
	}


	/**
	 *  Visit an allocation
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTAllocationExpression node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int start = getLineCount() + 1;

		MethodSummary parent = (MethodSummary) state.getCurrentSummary();

		//  Add the dependency
		Node child = node.jjtGetChild(0);
		TypeDeclSummary parentClass = null;
		if (child instanceof ASTName) {
			parentClass = new TypeDeclSummary(parent, (ASTName) child);
		}
		else if (child instanceof ASTPrimitiveType) {
			parentClass = new TypeDeclSummary(parent, (ASTPrimitiveType) child);
		}
		parent.addDependency(parentClass);

		//  Count the lines in the type and before
		countLines(node.getSpecial("id"));
		int code = state.getCode();
		state.setCode(SummaryLoaderState.IGNORE);
		child.jjtAccept(this, data);
		state.setCode(code);

		int last = node.jjtGetNumChildren();
		for (int ndx = 1; ndx < last; ndx++) {
			Node next = node.jjtGetChild(ndx);
			if (next instanceof ASTClassBody) {
				//  Create Type Summary
				TypeSummary typeSummary = new TypeSummary(parent, null);
				typeSummary.setName("Anonymous" + anonCount);
				anonCount++;
				typeSummary.setParentClass(parentClass);

				parent.addDependency(typeSummary);

				//  Set the next type summary as the current summary
				state.startSummary(typeSummary);
				int oldCode = state.getCode();
				state.setCode(SummaryLoaderState.LOAD_TYPE);

				//  Traverse Children
				next.jjtAccept(this, data);

				//  Back to loading the file
				state.finishSummary();
				state.setCode(oldCode);
			}
			else {
				((SimpleNode) next).jjtAccept(this, data);
			}
		}

		int end = getLineCount();
		parentClass.setStartLine(start);
		parentClass.setEndLine(end);

		//  Return the data
		return data;
	}


	/**
	 *  Visit a statement
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTStatement node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		Node child = node.jjtGetChild(0);
		if (child instanceof ASTBlock) {
			//  Don't count blocks as statements
		}
		else {
			MethodSummary parent = (MethodSummary) state.getCurrentSummary();
			parent.incrStatementCount();
		}

		return super.visit(node, data);
	}


	/**
	 *  Explicit constructor invocation gets one statement count
	 *
	 *@param  node  Description of Parameter
	 *@param  data  Description of Parameter
	 *@return       Description of the Returned Value
	 */
	public Object visit(ASTExplicitConstructorInvocation node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		MethodSummary parent = (MethodSummary) state.getCurrentSummary();
		parent.incrStatementCount();

		return super.visit(node, data);
	}


	/**
	 *  Visit the local variables
	 *
	 *@param  node  the node we are visiting
	 *@param  data  the state we are in
	 *@return       nothing of interest
	 */
	public Object visit(ASTLocalVariableDeclaration node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		int start = getLineCount() + 1;
		countLocalVariable(node, data);
		int end = getLineCount();

		MethodSummary parent = (MethodSummary) state.getCurrentSummary();

		parent.incrStatementCount();

		//  Get the field summaries
		LocalVariableSummary[] result = LocalVariableSummary.createNew(parent, node);

		//  Add the dependencies into the method
		int last = result.length;
		for (int ndx = 0; ndx < last; ndx++) {
			parent.addDependency(result[ndx]);
			result[ndx].setStartLine(start);
			result[ndx].setEndLine(end);
		}

		//  Return some result
		return data;
	}

	/**
	 *  Description of the Method
	 *
	 *@param  node  Description of Parameter
	 *@param  data  Description of Parameter
	 */
	protected void forInit(ASTLocalVariableDeclaration node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			super.visit(node, data);
			return;
		}

		int start = getLineCount() + 1;
		countLocalVariable(node, data);
		int end = getLineCount();

		MethodSummary parent = (MethodSummary) state.getCurrentSummary();

		//  Get the field summaries
		LocalVariableSummary[] result = LocalVariableSummary.createNew(parent, node);

		//  Add the dependencies into the method
		int last = result.length;
		for (int ndx = 0; ndx < last; ndx++) {
			parent.addDependency(result[ndx]);
			result[ndx].setStartLine(start);
			result[ndx].setEndLine(end);
		}
	}


	/**
	 *  Visits a block in the parse tree. This code counts the block depth
	 *  associated with a method. Deeply nested blocks in a method is a sign of
	 *  poor design.
	 *
	 *@param  node  the block node
	 *@param  data  the information that is used to traverse the tree
	 *@return       data is returned
	 */
	public Object visit(ASTBlock node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		//  Get the current file summary
		Summary current = state.getCurrentSummary();
		if (current instanceof MethodSummary) {
			((MethodSummary) current).beginBlock();
		}

		Object result = super.visit(node, data);

		if (current instanceof MethodSummary) {
			((MethodSummary) current).endBlock();
		}

		return result;
	}


	/**
	 *  A switch statement counts as a block, even though it
	 *  does not use the block parse token.
	 *
	 *@param  node  the switch node in the parse tree
	 *@param  data  the data used to visit this parse tree
	 *@return       the data
	 */
	public Object visit(ASTSwitchStatement node, Object data) {
		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		if (state.getCode() == SummaryLoaderState.IGNORE) {
			return super.visit(node, data);
		}

		//  Get the current file summary
		Summary current = state.getCurrentSummary();
		if (current instanceof MethodSummary) {
			((MethodSummary) current).beginBlock();
		}

		Object result = super.visit(node, data);

		if (current instanceof MethodSummary) {
			((MethodSummary) current).endBlock();
		}

		return result;
	}


	/**
	 *  Adds an access to the method
	 *
	 *@param  parent         the parent
	 *@param  name           the name
	 *@param  isMessageSend  is this a message send
	 */
	protected void addAccess(MethodSummary parent, ASTName name, boolean isMessageSend) {
		//  Record the access
		if (isMessageSend) {
			//  Add the dependency
			parent.addDependency(new MessageSendSummary(parent, (ASTName) name));
		}
		else {
			//  Add the dependency
			parent.addDependency(new FieldAccessSummary(parent, (ASTName) name));
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  current   Description of Parameter
	 *@param  state     Description of Parameter
	 *@param  body      Description of Parameter
	 *@param  isStatic  Description of Parameter
	 */
	void loadInitializer(TypeSummary current, SummaryLoaderState state, SimpleNode body, boolean isStatic) {
		MethodSummary methodSummary = current.getInitializer(isStatic);
		state.startSummary(methodSummary);

		//  Load the method summary's dependency list
		int oldCode = state.getCode();
		state.setCode(SummaryLoaderState.LOAD_METHODBODY);
		body.jjtAccept(this, state);

		//  Done
		state.setCode(oldCode);
		state.finishSummary();
	}


	/**
	 *  Counts the lines associated with the field declaration and the associated
	 *  type
	 *
	 *@param  node  the field declaration
	 *@param  data  the data for the visitor
	 */
	private void countFieldStart(ASTFieldDeclaration node, Object data) {
		//  Print any tokens
		countLines(node.getSpecial("static"));
		countLines(node.getSpecial("transient"));
		countLines(node.getSpecial("volatile"));
		countLines(node.getSpecial("final"));
		countLines(node.getSpecial("public"));
		countLines(node.getSpecial("protected"));
		countLines(node.getSpecial("private"));

		//  Handle the first two children (which are required)
		Node next = node.jjtGetChild(0);
		next.jjtAccept(this, data);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  node  Description of Parameter
	 *@param  data  Description of Parameter
	 */
	private void countMethodHeader(ASTMethodDeclaration node, Object data) {
		countLines(node.getSpecial("public"));
		countLines(node.getSpecial("protected"));
		countLines(node.getSpecial("private"));
		countLines(node.getSpecial("static"));
		countLines(node.getSpecial("abstract"));
		countLines(node.getSpecial("final"));
		countLines(node.getSpecial("native"));
		countLines(node.getSpecial("synchronized"));
		countLines(getInitialToken((ASTResultType) node.jjtGetChild(0)));
		countLines(node.getSpecial("throws"));
		countLines(node.getSpecial("semicolon"));
	}


	/**
	 *  Description of the Method
	 *
	 *@param  node  Description of Parameter
	 */
	private void countConstructorHeader(ASTConstructorDeclaration node) {
		countLines(node.getSpecial("public"));
		countLines(node.getSpecial("protected"));
		countLines(node.getSpecial("private"));
		countLines(node.getSpecial("id"));
		countLines(node.getSpecial("throws"));
		countLines(node.getSpecial("begin"));
	}


	/**
	 *  Description of the Method
	 *
	 *@param  decl   Description of Parameter
	 *@param  state  Description of Parameter
	 */
	private void loadMethodParams(SimpleNode decl, SummaryLoaderState state) {
		Node child = decl.jjtGetChild(0);
		if (!(child instanceof ASTFormalParameters)) {
			System.out.println("ERROR!  Not formal parameters");
			return;
		}
		state.setCode(SummaryLoaderState.LOAD_PARAMETERS);
		child.jjtAccept(this, state);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  node           Description of Parameter
	 *@param  methodSummary  Description of Parameter
	 *@param  count          Description of Parameter
	 */
	private void loadMethodReturn(ASTMethodDeclaration node, MethodSummary methodSummary, int count) {
		TypeDeclSummary returnType =
				TypeDeclSummary.getTypeDeclSummary(methodSummary, (ASTResultType) node.jjtGetChild(0));
		returnType.setArrayCount(returnType.getArrayCount() + count);
		methodSummary.setReturnType(returnType);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  node   Description of Parameter
	 *@param  state  Description of Parameter
	 *@param  index  Description of Parameter
	 */
	private void loadMethodExceptions(SimpleNode node, SummaryLoaderState state, int index) {
		if (node.jjtGetNumChildren() > index) {
			Node child = node.jjtGetChild(index);
			if (child instanceof ASTNameList) {
				state.setCode(SummaryLoaderState.LOAD_EXCEPTIONS);
				child.jjtAccept(this, state);
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  node   Description of Parameter
	 *@param  state  Description of Parameter
	 */
	private void loadMethodBody(ASTMethodDeclaration node, SummaryLoaderState state) {
		state.setCode(SummaryLoaderState.LOAD_METHODBODY);
		int last = node.jjtGetNumChildren();
		SimpleNode body = (SimpleNode) node.jjtGetChild(last - 1);
		if (body instanceof ASTBlock) {
			body.jjtAccept(this, state);
		}
	}

	private void countLocalVariable(ASTLocalVariableDeclaration node, Object data) {
		//  Traverse the children
		int last = node.jjtGetNumChildren();

		//  Print the final token
		if (node.isUsingFinal()) {
			countLines(node.getSpecial("final"));
		}

		//  Convert the data into the correct form
		SummaryLoaderState state = (SummaryLoaderState) data;

		//  The first child is special - it is the type
		int code = state.getCode();
		state.setCode(SummaryLoaderState.IGNORE);
		node.jjtGetChild(0).jjtAccept(this, data);
		state.setCode(code);

		//  Traverse the rest of the children
		for (int ndx = 1; ndx < last; ndx++) {
			countLines(node.getSpecial("comma." + (ndx - 1)));
			//  Visit the child
			node.jjtGetChild(ndx).jjtAccept(this, data);
		}
	}
}
