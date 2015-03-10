/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.method;

import java.util.Iterator;
import java.util.LinkedList;

import org.acm.seguin.parser.Node;
import org.acm.seguin.parser.ast.ASTClassBodyDeclaration;
import org.acm.seguin.parser.ast.ASTConstructorDeclaration;
import org.acm.seguin.parser.ast.ASTFormalParameters;
import org.acm.seguin.parser.ast.ASTInitializer;
import org.acm.seguin.parser.ast.ASTMethodDeclaration;
import org.acm.seguin.parser.ast.ASTMethodDeclarator;
import org.acm.seguin.parser.ast.ASTVariableDeclaratorId;
import org.acm.seguin.summary.FieldAccessSummary;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.LocalVariableSummary;
import org.acm.seguin.summary.MessageSendSummary;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.ParameterSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.VariableSummary;

/**
 *  Creates a list of arguments to the extacted method
 *
 *@author    Chris Seguin
 */
public class EMParameterFinder
{
	private FileSummary main;
	private FileSummary extract;
	private Node found;
	private LinkedList list;
	private LinkedList cantBe;
	private LinkedList parameters;
	private LinkedList returnTypes;


	/**
	 *  Constructor for the EMParameterFinder object
	 */
	public EMParameterFinder()
	{
		list = new LinkedList();
		cantBe = new LinkedList();
		parameters = new LinkedList();
		returnTypes = new LinkedList();
	}


	/**
	 *  Sets the MainFileSummary attribute of the EMParameterFinder object
	 *
	 *@param  value  The new MainFileSummary value
	 */
	public void setMainFileSummary(FileSummary value)
	{
		main = value;
	}


	/**
	 *  Sets the ExtractFileSummary attribute of the EMParameterFinder object
	 *
	 *@param  value  The new ExtractFileSummary value
	 */
	public void setExtractFileSummary(FileSummary value)
	{
		extract = value;
	}


	/**
	 *  Sets the Location attribute of the EMParameterFinder object
	 *
	 *@param  value  The new Location value
	 */
	public void setLocation(Node value)
	{
		found = value;
	}


	/**
	 *  Sets the order of the parameters
	 *
	 *@param  data  The new ParameterOrder value
	 */
	public void setParameterOrder(Object[] data)
	{
		parameters = new LinkedList();
		for (int ndx = 0; ndx < data.length; ndx++)
		{
			parameters.add(data[ndx]);
		}
	}


	/**
	 *  Gets the list of parameters
	 *
	 *@return    The List value
	 */
	public LinkedList getList()
	{
		return parameters;
	}


	/**
	 *  Main processing method for the EMParameterFinder object
	 */
	public void run()
	{
		TypeSummary type = (TypeSummary) extract.getTypes().next();
		MethodSummary method = (MethodSummary) type.getMethods().next();

		MethodSummary mainMethod = find();

		Iterator iter = method.getDependencies();
		if (iter == null)
		{
			return;
		}

		while (iter.hasNext())
		{
			Object next = iter.next();
			if (next instanceof FieldAccessSummary)
			{
				FieldAccessSummary fas = (FieldAccessSummary) next;
				String fieldName = fas.getFirstObject();
				updateLists(fieldName, mainMethod);
				if (fas.isAssignment())
				{
					VariableSummary paramSummary = find(fieldName, mainMethod);
					if (paramSummary != null)
					{
						addReturnType(paramSummary);
					}
				}
			}
			else if (next instanceof MessageSendSummary)
			{
				MessageSendSummary mss = (MessageSendSummary) next;
				String name = mss.getFirstObject();
				updateLists(name, mainMethod);
			}
			else if (next instanceof LocalVariableSummary)
			{
				LocalVariableSummary lvs = (LocalVariableSummary) next;
				cantBe.add(lvs.getName());
				addReturnType(lvs);
			}
			else if (next instanceof ParameterSummary)
			{
				ParameterSummary param = (ParameterSummary) next;
				cantBe.add(param.getName());
			}
		}
	}


	/**
	 *  Returns the list of possible return types
	 *
	 *@return    The ReturnTypes value
	 */
	Object[] getReturnTypes()
	{
		Object[] result = new Object[returnTypes.size() + 1];
		Iterator iter = returnTypes.iterator();
		result[0] = "void";
		int count = 1;
		while (iter.hasNext())
		{
			result[count] = iter.next();
			count++;
		}
		return result;
	}


	/**
	 *  Determines if the method summary and the method parse tree are the same
	 *
	 *@param  methodSummary  the summary in question
	 *@param  classBodyDecl  Description of Parameter
	 *@return                true if they appear to be the same
	 */
	private boolean isSame(MethodSummary methodSummary, ASTClassBodyDeclaration classBodyDecl)
	{
		Node child = classBodyDecl.jjtGetChild(0);
		if (child instanceof ASTMethodDeclaration)
		{
			ASTMethodDeclarator decl = (ASTMethodDeclarator) child.jjtGetChild(1);
			if (decl.getName().equals(methodSummary.getName()))
			{
				ASTFormalParameters params = (ASTFormalParameters) decl.jjtGetChild(0);
				return isParametersSame(params, methodSummary);
			}
			return false;
		}

		if (child instanceof ASTConstructorDeclaration)
		{
			ASTConstructorDeclaration decl = (ASTConstructorDeclaration) child;
			if (methodSummary.isConstructor())
			{
				ASTFormalParameters params = (ASTFormalParameters) decl.jjtGetChild(0);
				return isParametersSame(params, methodSummary);
			}
			return false;
		}

		if (child instanceof ASTInitializer)
		{
			return methodSummary.isInitializer();
		}

		return false;
	}


	/**
	 *  Gets the ParametersSame attribute of the EMParameterFinder object
	 *
	 *@param  methodSummary  Description of Parameter
	 *@param  params         Description of Parameter
	 *@return                The ParametersSame value
	 */
	private boolean isParametersSame(Node params, MethodSummary methodSummary)
	{
		if (params.jjtGetNumChildren() == methodSummary.getParameterCount())
		{
			if (methodSummary.getParameterCount() == 0)
			{
				return true;
			}
			int count = 0;
			Iterator iter = methodSummary.getParameters();
			while (iter.hasNext())
			{
				ParameterSummary next = (ParameterSummary) iter.next();
				Node nextNode = params.jjtGetChild(count);
				ASTVariableDeclaratorId paramName = (ASTVariableDeclaratorId) nextNode.jjtGetChild(1);

				String nextName = next.getName();
				String parameterID = paramName.getName();

				if (!nextName.equals(parameterID))
				{
					return false;
				}

				count++;
			}
			return true;
		}

		return false;
	}


	/**
	 *  Checks to see if the lists have variables with the same names in it
	 *
	 *@param  var   Description of Parameter
	 *@param  list  Description of Parameter
	 *@return       The Contained value
	 */
	private boolean isContained(VariableSummary var, LinkedList list)
	{
		Iterator iter = list.iterator();
		while (iter.hasNext())
		{
			Object obj = iter.next();
			if (obj instanceof VariableSummary)
			{
				VariableSummary next = (VariableSummary) obj;
				if (next.getName().equals(var.getName()))
				{
					return true;
				}
			}
		}

		return false;
	}


	/**
	 *  Finds the object we extracted the code from
	 *
	 *@return    the portion of the parse tree
	 */
	private ASTClassBodyDeclaration findDecl()
	{
		Node current = found;
		while (!(current instanceof ASTClassBodyDeclaration))
		{
			current = current.jjtGetParent();
			if (current == null)
			{
				return null;
			}
		}

		return (ASTClassBodyDeclaration) current;
	}


	/**
	 *  Finds the method summary
	 *
	 *@return    the method summary
	 */
	private MethodSummary find()
	{
		ASTClassBodyDeclaration classBodyDecl = findDecl();
		if (classBodyDecl == null)
		{
			return null;
		}
		Iterator typeIterator = main.getTypes();
		while (typeIterator.hasNext())
		{
			TypeSummary nextType = (TypeSummary) typeIterator.next();
			Iterator methodIterator = nextType.getMethods();
			while (methodIterator.hasNext())
			{
				MethodSummary nextMethod = (MethodSummary) methodIterator.next();
				if (isSame(nextMethod, classBodyDecl))
				{
					return nextMethod;
				}
			}
		}

		return null;
	}


	/**
	 *  Searches for a variable declaration of a variable named name in the
	 *  method summary method
	 *
	 *@param  name    the variable name
	 *@param  method  the method we are searching in
	 *@return         the variable summary if one is found
	 */
	private VariableSummary find(String name, MethodSummary method)
	{
		if (method == null)
		{
			return null;
		}

		Iterator iter = method.getParameters();
		while ((iter != null) && iter.hasNext())
		{
			ParameterSummary param = (ParameterSummary) iter.next();
			if (param.getName().equals(name))
			{
				return param;
			}
		}

		iter = method.getDependencies();
		while ((iter != null) && iter.hasNext())
		{
			Object next = iter.next();
			if (next instanceof LocalVariableSummary)
			{
				LocalVariableSummary lvs = (LocalVariableSummary) next;
				if (lvs.getName().equals(name))
				{
					return lvs;
				}
			}
			else if (next instanceof ParameterSummary)
			{
				ParameterSummary ps = (ParameterSummary) next;
				if (ps.getName().equals(name))
				{
					return ps;
				}
			}
		}

		return null;
	}


	/**
	 *  Updates the list of parameters for the extracted method
	 *
	 *@param  name        the method to add to the various lists
	 *@param  mainMethod  the method summary
	 */
	private void updateLists(String name, MethodSummary mainMethod)
	{
		if (name == null)
		{
			//  Do nothing
		}
		else if (cantBe.contains(name))
		{
			//System.out.println("---" + name + " c");
		}
		else if (!list.contains(name))
		{
			//System.out.println("Variable:  " + name);
			list.add(name);
			VariableSummary paramSummary = find(name, mainMethod);
			if (paramSummary != null)
			{
				//System.out.println("\t***");
				parameters.add(paramSummary);
			}
		}
		else
		{
			//System.out.println("---" + name + " a");
		}
	}


	/**
	 *  Adds a return type to the list
	 *
	 *@param  var  the variable summary to add
	 */
	private void addReturnType(VariableSummary var)
	{
		if (!isContained(var, returnTypes))
		{
			returnTypes.add(var);
		}
	}
}
