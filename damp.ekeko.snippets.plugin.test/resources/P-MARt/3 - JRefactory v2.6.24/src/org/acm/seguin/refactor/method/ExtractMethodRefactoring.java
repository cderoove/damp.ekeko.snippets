/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.method;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.LinkedList;

import org.acm.seguin.parser.Node;
import org.acm.seguin.parser.ast.ASTBlockStatement;
import org.acm.seguin.parser.ast.ASTClassBody;
import org.acm.seguin.parser.ast.ASTClassBodyDeclaration;
import org.acm.seguin.parser.ast.ASTClassDeclaration;
import org.acm.seguin.parser.ast.ASTCompilationUnit;
import org.acm.seguin.parser.ast.ASTMethodDeclaration;
import org.acm.seguin.parser.ast.ASTReturnStatement;
import org.acm.seguin.parser.ast.ASTStatement;
import org.acm.seguin.parser.ast.ASTTypeDeclaration;
import org.acm.seguin.parser.ast.ASTUnmodifiedClassDeclaration;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.parser.build.BuildExpression;
import org.acm.seguin.parser.factory.BufferParserFactory;
import org.acm.seguin.parser.query.Found;
import org.acm.seguin.parser.query.Search;
import org.acm.seguin.pretty.ModifierHolder;
import org.acm.seguin.pretty.PrettyPrintVisitor;
import org.acm.seguin.pretty.PrintData;
import org.acm.seguin.refactor.Refactoring;
import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.SummaryLoadVisitor;
import org.acm.seguin.summary.SummaryLoaderState;
import org.acm.seguin.summary.VariableSummary;

/**
 *  Refactoring class that extracts a portion of the method and creates a new
 *  method with what the user has selected.
 *
 *@author    Chris Seguin
 */
public class ExtractMethodRefactoring extends Refactoring
{
	private StringBuffer fullFile = null;
	private String selection = null;
	private String methodName = null;
	private SimpleNode root;
	private FileSummary mainFileSummary;
	private FileSummary extractedMethodFileSummary;
	private Node key;
	private EMParameterFinder empf = null;
	private StringBuffer signature;
	/**
	 *  Stores the return type for the extracted method
	 */
	private Object returnType = null;
	private int prot = PRIVATE;
	private Object[] arguments = new Object[0];

	/**
	 *  The extracted method should be private
	 */
	public final static int PRIVATE = 0;
	/**
	 *  The extracted method should have package scope
	 */
	public final static int PACKAGE = 1;
	/**
	 *  The extracted method should have protected scope
	 */
	public final static int PROTECTED = 2;
	/**
	 *  The extracted method should have public scope
	 */
	public final static int PUBLIC = 3;


	/**
	 *  Constructor for the ExtractMethodRefactoring object
	 */
	protected ExtractMethodRefactoring()
	{
		super();

		signature = new StringBuffer();
	}


	/**
	 *  Sets the FullFile attribute of the ExtractMethodRefactoring object
	 *
	 *@param  value  The new FullFile value
	 */
	public void setFullFile(String value)
	{
		fullFile = new StringBuffer(value);
	}


	/**
	 *  Sets the FullFile attribute of the ExtractMethodRefactoring object
	 *
	 *@param  value  The new FullFile value
	 */
	public void setFullFile(StringBuffer value)
	{
		fullFile = value;
	}


	/**
	 *  Sets the Selection attribute of the ExtractMethodRefactoring object
	 *
	 *@param  value  The new Selection value
	 */
	public void setSelection(String value) throws RefactoringException
	{
		if (value == null) {
			throw new RefactoringException("Nothing has been selected, so nothing can be extracted");
		}

		selection = value.trim();

		if (isStatement())
		{
			setReturnType(null);
		}
		else
		{
			setReturnType("boolean");
		}
	}


	/**
	 *  Sets the MethodName attribute of the ExtractMethodRefactoring object
	 *
	 *@param  value  The new MethodName value
	 */
	public void setMethodName(String value)
	{
		methodName = value;
		if ((methodName == null) || (methodName.length() == 0))
		{
			methodName = "extractedMethod";
		}
	}


	/**
	 *  Sets the order of the parameters
	 *
	 *@param  data  The new ParameterOrder value
	 */
	public void setParameterOrder(Object[] data)
	{
		empf.setParameterOrder(data);
		arguments = data;
	}


	/**
	 *  Sets the Protection attribute of the ExtractMethodRefactoring object
	 *
	 *@param  value  The new Protection value
	 */
	public void setProtection(int value)
	{
		prot = value;
	}


	/**
	 *  Sets the return type for the extracted method
	 *
	 *@param  obj  The new ReturnType value
	 */
	public void setReturnType(Object obj)
	{
		returnType = obj;
	}


	/**
	 *  Gets the Description attribute of the ExtractMethodRefactoring object
	 *
	 *@return    The Description value
	 */
	public String getDescription()
	{
		return "Extract a method named " + methodName;
	}


	/**
	 *  Gets the FullFile attribute of the ExtractMethodRefactoring object
	 *
	 *@return    The FullFile value
	 */
	public String getFullFile()
	{
		return fullFile.toString();
	}


	/**
	 *  Gets the Parameters attribute of the ExtractMethodRefactoring object
	 *
	 *@return                           The Parameters value
	 *@exception  RefactoringException  Description of Exception
	 */
	public VariableSummary[] getParameters() throws RefactoringException
	{
		preconditions();
		Search srch = new Search();
		empf = prescan(srch);

		LinkedList list = empf.getList();
		VariableSummary[] result = new VariableSummary[list.size()];
		Iterator iter = list.iterator();
		int count = 0;
		while (iter.hasNext())
		{
			result[count] = (VariableSummary) iter.next();
			count++;
		}

		arguments = result;

		return result;
	}


	/**
	 *  Gets the possible return types
	 *
	 *@return                           The return types
	 *@exception  RefactoringException  problem in loading these
	 */
	public Object[] getReturnTypes() throws RefactoringException
	{
		if (empf == null)
		{
			return null;
		}
		return empf.getReturnTypes();
	}


	/**
	 *  Gets the Statement attribute of the ExtractMethodRefactoring object
	 *
	 *@return    The Statement value
	 */
	public boolean isStatement()
	{
		return (selection.indexOf(";") > 0) || (selection.indexOf("}") > 0);
	}


	/**
	 *  Gets the Signature attribute of the ExtractMethodRefactoring object
	 *
	 *@return    The Signature value
	 */
	public String getSignature()
	{
		signature.setLength(0);
		signature.append(getProtection());
		signature.append(" ");
		signature.append(getReturnTypeString());
		signature.append(" ");
		signature.append(methodName);
		signature.append("(");
		for (int ndx = 0; ndx < arguments.length; ndx++)
		{
			signature.append(((VariableSummary) arguments[ndx]).getDeclaration());
			if (ndx != arguments.length - 1)
			{
				signature.append(", ");
			}
		}
		signature.append(")");

		return signature.toString();
	}


	/**
	 *  Gets the return type for the extracted method
	 *
	 *@return    The return type
	 */
	public Object getReturnType()
	{
		return returnType;
	}


	/**
	 *  Gets the ID attribute of the ExtractMethodRefactoring object
	 *
	 *@return    The ID value
	 */
	public int getID()
	{
		return EXTRACT_METHOD;
	}


	/**
	 *  These items must be true before the refactoring will work
	 *
	 *@exception  RefactoringException  the problem that arose
	 */
	protected void preconditions() throws RefactoringException
	{
		if (fullFile == null)
		{
			throw new RefactoringException("No file specified");
		}

		if (selection == null)
		{
			throw new RefactoringException("No selection specified");
		}

		if (methodName == null)
		{
			throw new RefactoringException("No method specified");
		}

		root = getFileRoot();
		if (root == null)
		{
			throw new RefactoringException("Unable to parse the current file.\n" +
					"Please make sure you can compile this file before\n" +
					"trying to extract a method from it.");
		}

		mainFileSummary = findVariablesUsed(root);

		SimpleNode newMethod = getMethodTree();
		if (newMethod == null)
		{
			throw new RefactoringException("Unable to parse the current selection.\n" +
					"Please make sure you have highlighted the entire expression\n" +
					"or set of statements.");
		}
	}


	/**
	 *  Actually make the transformation
	 */
	protected void transform()
	{
		replaceAllInstances(root);
		printFile(root);
	}


	/**
	 *  Gets the MethodTree attribute of the ExtractMethodRefactoring object
	 *
	 *@return    The MethodTree value
	 */
	private SimpleNode getMethodTree()
	{
		String tempClass = "public class TempClass { " + makeMethod() + "}";
		BufferParserFactory bpf = new BufferParserFactory(tempClass);
		SimpleNode root = bpf.getAbstractSyntaxTree(false);

		extractedMethodFileSummary = findVariablesUsed(root);

		ASTTypeDeclaration top = (ASTTypeDeclaration) root.jjtGetChild(0);
		ASTClassDeclaration classDecl = (ASTClassDeclaration) top.jjtGetChild(0);
		ASTUnmodifiedClassDeclaration unmodifiedClassDecl =
				(ASTUnmodifiedClassDeclaration) classDecl.jjtGetChild(0);
		ASTClassBody classBody = (ASTClassBody) unmodifiedClassDecl.jjtGetChild(0);
		ASTClassBodyDeclaration bodyDecl = (ASTClassBodyDeclaration) classBody.jjtGetChild(0);

		return (SimpleNode) bodyDecl.jjtGetChild(0);
	}


	/**
	 *  Gets the FileRoot attribute of the ExtractMethodRefactoring object
	 *
	 *@return    The FileRoot value
	 */
	private SimpleNode getFileRoot()
	{
		BufferParserFactory bpf = new BufferParserFactory(fullFile.toString());
		SimpleNode root = bpf.getAbstractSyntaxTree(true);
		return root;
	}


	/**
	 *  Returns the protection
	 *
	 *@return    The Protection value
	 */
	private String getProtection()
	{
		switch (prot)
		{
			case PRIVATE:
				return "private";
			case PACKAGE:
				return "";
			case PROTECTED:
				return "protected";
			case PUBLIC:
				return "public";
		}
		return "private";
	}


	/**
	 *  Returns the return type
	 *
	 *@return    The ReturnType value
	 */
	private String getReturnTypeString()
	{
		if (returnType == null)
		{
			return "void";
		}
		else if (returnType instanceof String)
		{
			return (String) returnType;
		}
		else if (returnType instanceof VariableSummary)
		{
			return ((VariableSummary) returnType).getTypeDecl().getName();
		}
		else
		{
			return returnType.toString();
		}
	}


	/**
	 *  Replace all instances of code with a selected value
	 *
	 *@param  root  Description of Parameter
	 */
	private void replaceAllInstances(SimpleNode root)
	{
		EMBuilder builder = new EMBuilder();
		builder.setMethodName(methodName);
		builder.setStatement(isStatement());

		Search srch = new Search();
		if (empf == null)
		{
			empf = prescan(srch);
		}
		builder.setParameters(empf.getList());

		SimpleNode methodTree = addReturn(getMethodTree());

		Found result = srch.search(root, key);
		updateModifiers((SimpleNode) result.getRoot(), methodTree);

		if (returnType instanceof VariableSummary)
		{
			builder.setReturnSummary((VariableSummary) returnType);
			FindLocalVariableDeclVisitor flvdv = new FindLocalVariableDeclVisitor();
			methodTree.jjtAccept(flvdv, returnType);
			builder.setLocalVariableNeeded(flvdv.isFound());
		}

		SimpleNode firstResult = (SimpleNode) result.getRoot();
		while (result != null)
		{
			replaceExtractedMethod(result, builder);
			result = srch.search(root, key);
		}

		insertAtNextClass(firstResult, methodTree);
	}


	/**
	 *  Prints the file using the pretty printer option
	 *
	 *@param  root  Description of Parameter
	 */
	private void printFile(SimpleNode root)
	{
		ByteArrayOutputStream baos;

		baos = new ByteArrayOutputStream();
		PrintData pd = new PrintData(baos);
		PrettyPrintVisitor ppv = new PrettyPrintVisitor();
		ppv.visit((ASTCompilationUnit) root, pd);
		pd.close();

		byte[] buffer = baos.toByteArray();
		String file = new String(buffer);
		if (file.length() > 0)
		{
			fullFile = new StringBuffer(file);
		}
	}


	/**
	 *  Creates a string with the new method in it
	 *
	 *@return    the new method
	 */
	private String makeMethod()
	{
		if (isStatement())
		{
			return getSignature() + "{" + selection + "}";
		}
		else
		{
			return getSignature() + "{ return " + selection + "; }";
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  node  Description of Parameter
	 *@return       Description of the Returned Value
	 */
	private FileSummary findVariablesUsed(Node node)
	{
		if (node == null)
		{
			return null;
		}
		SummaryLoaderState state = new SummaryLoaderState();
		node.jjtAccept(new SummaryLoadVisitor(), state);
		return (FileSummary) state.getCurrentSummary();
	}


	/**
	 *  Finds the parameters
	 *
	 *@param  result  the location where the section was found
	 *@return         Description of the Returned Value
	 */
	private EMParameterFinder findParameters(Found result)
	{
		EMParameterFinder empf = new EMParameterFinder();
		empf.setMainFileSummary(mainFileSummary);
		empf.setExtractFileSummary(extractedMethodFileSummary);
		empf.setLocation(result.getRoot());
		empf.run();
		return empf;
	}


	/**
	 *  This allows us to scan for the parameters first
	 *
	 *@param  srch  Description of Parameter
	 *@return       Description of the Returned Value
	 */
	private EMParameterFinder prescan(Search srch)
	{
		EMDigger digger = new EMDigger();
		if (isStatement())
		{
			key = digger.last((ASTMethodDeclaration) getMethodTree());
		}
		else
		{
			key = digger.dig((ASTMethodDeclaration) getMethodTree());
		}

		Found result = srch.search(root, key);

		EMParameterFinder parameterFinder = findParameters(result);

		LinkedList list = parameterFinder.getList();
		arguments = new Object[list.size()];
		Iterator iter = list.iterator();
		int count = 0;
		while (iter.hasNext())
		{
			arguments[count] = iter.next();
			count++;
		}

		return parameterFinder;
	}


	/**
	 *  Replaces the extracted method
	 *
	 *@param  result   where we found the portion to replace
	 *@param  builder  build a method invocation
	 */
	private void replaceExtractedMethod(Found result, EMBuilder builder)
	{
		int index = result.getIndex();
		int length = key.jjtGetNumChildren();
		Node location = result.getRoot();

		for (int ndx = 0; ndx < length; ndx++)
		{
			location.jjtDeleteChild(index);
		}
		location.jjtInsertChild(builder.build(), index);
	}


	/**
	 *  Adds the return at the end of the method if one is necessary
	 *
	 *@param  methodDecl  The feature to be added to the Return attribute
	 *@return             Description of the Returned Value
	 */
	private SimpleNode addReturn(SimpleNode methodDecl)
	{
		if (returnType instanceof VariableSummary)
		{
			Node block = methodDecl.jjtGetChild(methodDecl.jjtGetNumChildren() - 1);

			ASTBlockStatement blockStatement = new ASTBlockStatement(0);

			ASTStatement statement = new ASTStatement(0);
			blockStatement.jjtAddChild(statement, 0);

			ASTReturnStatement returnStatement = new ASTReturnStatement(0);
			statement.jjtAddChild(returnStatement, 0);

			BuildExpression be = new BuildExpression();
			String name = ((VariableSummary) returnType).getName();
			returnStatement.jjtAddChild(be.buildName(name), 0);

			block.jjtAddChild(blockStatement, block.jjtGetNumChildren());
		}

		return methodDecl;
	}


	/**
	 *  Adds the static and synchronized attributes to the extracted method
	 *
	 *@param  currentNode  where the body of the extracted method was found
	 *@param  methodTree   the method we are extracting
	 */
	private void updateModifiers(SimpleNode currentNode, SimpleNode methodTree)
	{
		while (!(currentNode instanceof ASTMethodDeclaration))
		{
			currentNode = (SimpleNode) currentNode.jjtGetParent();
			if (currentNode instanceof ASTClassBody)
			{
				return;
			}
		}

		ASTMethodDeclaration extractedFrom = (ASTMethodDeclaration) currentNode;
		ASTMethodDeclaration newMethod = (ASTMethodDeclaration) methodTree;

		ModifierHolder efmh = extractedFrom.getModifiers();
		ModifierHolder nmmh = newMethod.getModifiers();
		nmmh.setStatic(efmh.isStatic());
		nmmh.setSynchronized(nmmh.isSynchronized());
	}


	/**
	 *  Inserts the method at the next class found when heading up from the first
	 *  place where we replaced this value
	 *
	 *@param  currentNode  where this was found
	 *@param  methodTree   the method to be inserted
	 */
	private void insertAtNextClass(SimpleNode currentNode, SimpleNode methodTree)
	{
		while (!(currentNode instanceof ASTClassBody))
		{
			currentNode = (SimpleNode) currentNode.jjtGetParent();
			if (currentNode == null)
			{
				return;
			}
		}

		currentNode.jjtInsertChild(methodTree, currentNode.jjtGetNumChildren());
	}
}
