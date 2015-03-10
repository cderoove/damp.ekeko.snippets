package org.acm.seguin.refactor.type;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;

import org.acm.seguin.refactor.AddImportTransform;
import org.acm.seguin.refactor.ComplexTransform;
import org.acm.seguin.refactor.RemoveImportTransform;
import org.acm.seguin.refactor.TransformAST;
import org.acm.seguin.summary.FieldAccessSummary;
import org.acm.seguin.summary.FieldSummary;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.ImportSummary;
import org.acm.seguin.summary.LocalVariableSummary;
import org.acm.seguin.summary.MessageSendSummary;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.ParameterSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TraversalVisitor;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.VariableSummary;

/**
 *  Scans through the summary objects to create a list of files that reference
 *  a particular class.
 *
 *@author    Chris Seguin
 */
public abstract class TypeChangeVisitor extends TraversalVisitor {
	//  Instance Variables
	private ComplexTransform refactoring;


	/**
	 *  Visitor for type changes
	 *
	 *@param  complex  Description of Parameter
	 */
	public TypeChangeVisitor(ComplexTransform complex) {
		refactoring = complex;
	}


	/**
	 *  Visit a summary node. This is the default method.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(Summary node, Object data) {
		//  Shouldn't have to do anything about one of these nodes
		return data;
	}


	/**
	 *  Visit a file summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(FileSummary node, Object data) {
		if (node.getFile() == null) {
			return null;
		}

		if (!preconditions(node)) {
			return null;
		}

		refactoring.clear();
		LinkedList list = getAppropriateClasses(node);

		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			//  Get the name of the class
			String className = (String) iter.next();

			//  First check to see if any of the classes were imported
			boolean foundImport = checkImports(node, className);

			//  Now we get down to the business of checking individual types
			if (checkTypes(node, getState(foundImport, node, className))) {
				AddImportTransform ait = getNewImports(node, className);
				if (ait != null) {
					ait.setIgnorePackageName(true);
					refactoring.add(ait);
				}
				addRenamingTransforms(refactoring, node, className);
			}
		}

		refactoring.add(getFileSpecificTransform(node));

		if (refactoring.hasAnyChanges()) {
			File oldFile = node.getFile();
			File newFile = getNewFile(node);
			System.out.println("Updating:  " + oldFile.getName());
			refactoring.add(new RemoveSamePackageTransform());
			refactoring.apply(oldFile, newFile);
		}

		//  Return some value
		return refactoring;
	}


	/**
	 *  Visit a import summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(ImportSummary node, Object data) {
		//  Local Variables
		boolean importedClass = false;
		boolean importedPackage = false;
		boolean gettingPackage = node.getPackage().getName().equals(getCurrentPackage());

		//  Check to see if we have a specific class
		if (gettingPackage) {
			String typeName = node.getType();
			if (typeName == null) {
				importedPackage = true;
			}
			else {
				String className = (String) data;
				importedClass = (className.equals(typeName));
			}
		}

		//  At this point we know if we specifically imported the class
		if (importedClass) {
			refactoring.add(getRemoveImportTransform(node));
		}

		//  Return an integer code for what was found in this import
		return new Boolean(importedPackage || importedClass);
	}


	/**
	 *  Visit a type summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(TypeSummary node, Object data) {
		Boolean result = new Boolean(false);

		//  Check extension
		TypeDeclSummary parent = node.getParentClass();
		if (parent != null) {
			result = (Boolean) parent.accept(this, data);
		}
		if (result.booleanValue()) {
			return result;
		}

		//  Check list of implemented interfaces
		Iterator iter = node.getImplementedInterfaces();
		if (iter != null) {
			while (iter.hasNext()) {
				TypeDeclSummary next = (TypeDeclSummary) iter.next();
				result = (Boolean) next.accept(this, data);
				if (result.booleanValue()) {
					return result;
				}
			}
		}

		//  Over the fields
		iter = node.getFields();
		if (iter != null) {
			while (iter.hasNext()) {
				FieldSummary next = (FieldSummary) iter.next();
				result = (Boolean) next.accept(this, data);
				if (result.booleanValue()) {
					return result;
				}
			}
		}

		//  Over the methods
		iter = node.getMethods();
		if (iter != null) {
			while (iter.hasNext()) {
				MethodSummary next = (MethodSummary) iter.next();
				result = (Boolean) next.accept(this, data);
				if (result.booleanValue()) {
					return result;
				}
			}
		}

		//  Over the types
		iter = node.getTypes();
		if (iter != null) {
			while (iter.hasNext()) {
				TypeSummary next = (TypeSummary) iter.next();
				result = (Boolean) next.accept(this, data);
				if (result.booleanValue()) {
					return result;
				}
			}
		}

		//  Return the last false value
		return result;
	}


	/**
	 *  Visit a method summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(MethodSummary node, Object data) {
		Boolean result = new Boolean(false);

		//  Check the return type
		TypeDeclSummary returnType = node.getReturnType();
		if (returnType != null) {
			result = (Boolean) returnType.accept(this, data);
			if (result.booleanValue()) {
				return result;
			}
		}

		//  Check the parameters
		Iterator iter = node.getParameters();
		if (iter != null) {
			while (iter.hasNext()) {
				ParameterSummary next = (ParameterSummary) iter.next();
				result = (Boolean) next.accept(this, data);
				if (result.booleanValue()) {
					return result;
				}
			}
		}

		//  Check the exceptions
		iter = node.getExceptions();
		if (iter != null) {
			while (iter.hasNext()) {
				TypeDeclSummary next = (TypeDeclSummary) iter.next();
				result = (Boolean) next.accept(this, data);
				if (result.booleanValue()) {
					return result;
				}
			}
		}

		//  Check the dependencies
		iter = node.getDependencies();
		if (iter != null) {
			while (iter.hasNext()) {
				Summary next = (Summary) iter.next();
				result = (Boolean) next.accept(this, data);
				if (result.booleanValue()) {
					return result;
				}
			}
		}

		return result;
	}


	/**
	 *  Visit a field summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(FieldSummary node, Object data) {
		return visit((VariableSummary) node, data);
	}


	/**
	 *  Visit a parameter summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(ParameterSummary node, Object data) {
		return visit((VariableSummary) node, data);
	}


	/**
	 *  Visit a local variable summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(LocalVariableSummary node, Object data) {
		return visit((VariableSummary) node, data);
	}


	/**
	 *  Visit a variable summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(VariableSummary node, Object data) {
		return node.getTypeDecl().accept(this, data);
	}


	/**
	 *  Visit a type declaration summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(TypeDeclSummary node, Object data) {
		//  Local Variables
		State state = (State) data;
		boolean mustUsePackage = state.isPackageRequired();
		String className = state.getClassName();
		String nodePackageName = node.getPackage();

		//  Check if the package names match
		if (isMatchingPackage(nodePackageName, mustUsePackage)) {
			//  Check for the specific type name
			return new Boolean(className.equals(node.getType()));
		}

		return new Boolean(false);
	}


	/**
	 *  Visit a message send summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(MessageSendSummary node, Object data) {
		//  Local Variables
		State state = (State) data;
		boolean mustUsePackage = state.isPackageRequired();
		String className = state.getClassName();

		//  Check if the package names match
		boolean classNameMatches =
				(node.getObjectName() != null) &&
				(node.getObjectName().equals(className));

		boolean packageNameMatches = isMatchingPackage(node.getPackageName(), mustUsePackage);

		return new Boolean(classNameMatches && packageNameMatches);
	}


	/**
	 *  Visit a field access summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(FieldAccessSummary node, Object data) {
		//  Local Variables
		State state = (State) data;
		boolean mustUsePackage = state.isPackageRequired();
		String className = state.getClassName();

		boolean classNameMatches =
				(node.getObjectName() != null) &&
				(node.getObjectName().equals(className));

		boolean packageNameMatches = isMatchingPackage(node.getPackageName(), mustUsePackage);

		return new Boolean(classNameMatches && packageNameMatches);
	}


	/**
	 *  Returns the state object to be used to determine if the particular type
	 *  we are deleting is present
	 *
	 *@param  foundImport  Description of Parameter
	 *@param  node         Description of Parameter
	 *@param  className    Description of Parameter
	 *@return              The State value
	 */
	protected State getState(boolean foundImport, FileSummary node, String className) {
		boolean mustUsesFullPackageName = !(foundImport || isSamePackage(node));
		return new State(className, mustUsesFullPackageName);
	}


	/**
	 *  Gets the File Specific Transform
	 *
	 *@param  summary  Gets a file specific transform
	 *@return          The FileSpecificTransform value
	 */
	protected abstract TransformAST getFileSpecificTransform(FileSummary summary);


	/**
	 *  Gets the New Imports transform
	 *
	 *@param  node       the file summary
	 *@param  className  the name of the class that is changing
	 *@return            The NewImports value
	 */
	protected abstract AddImportTransform getNewImports(FileSummary node, String className);


	/**
	 *  Gets the Remove Imports transform
	 *
	 *@param  node  the import summary
	 *@return       The transform
	 */
	protected abstract RemoveImportTransform getRemoveImportTransform(ImportSummary node);


	/**
	 *  Gets the list of classes to iterate over
	 *
	 *@param  node  the file summary
	 *@return       The list
	 */
	protected abstract LinkedList getAppropriateClasses(FileSummary node);


	/**
	 *  Gets the reference to the file where the refactored output should be sent
	 *
	 *@param  node  the files summary
	 *@return       The NewFile value
	 */
	protected abstract File getNewFile(FileSummary node);


	/**
	 *  Return the current package
	 *
	 *@return    the current package of the class
	 */
	protected abstract String getCurrentPackage();


	/**
	 *  Checks any preconditions
	 *
	 *@param  summary  Description of Parameter
	 *@return          Description of the Returned Value
	 */
	protected boolean preconditions(FileSummary summary) {
		return true;
	}


	/**
	 *  Gets the RenamingTransform
	 *
	 *@param  refactoring  the refactoring
	 *@param  node         the file summary to reference
	 *@param  className    the name of the class that is changing
	 */
	protected abstract void addRenamingTransforms(ComplexTransform refactoring,
			FileSummary node, String className);


	/**
	 *  Returns true if the package is the same
	 *
	 *@param  node  the current node
	 *@return       true if the object is in the package
	 */
	private boolean isSamePackage(FileSummary node) {
		PackageSummary parent = (PackageSummary) node.getParent();
		return parent.getName().equals(getCurrentPackage());
	}


	/**
	 *  Determines if the package matches
	 *
	 *@param  nodePackageName  The node's package
	 *@param  mustUsePackage   must it use the full package name
	 *@return                  true if the package matches
	 */
	private boolean isMatchingPackage(String nodePackageName, boolean mustUsePackage) {
		boolean nullPackageName = (nodePackageName == null);
		if (mustUsePackage && nullPackageName) {
			return false;
		}

		return nullPackageName || (nodePackageName.equals(getCurrentPackage()));
	}


	/**
	 *  Determine if there was anything by that name imported
	 *
	 *@param  node  The file summary node
	 *@param  data  Data used for traversing the tree
	 *@return       true if the data was imported
	 */
	private boolean checkImports(FileSummary node, Object data) {
		//  Iterate over the import statements
		Iterator iter = node.getImports();

		if (iter != null) {
			while (iter.hasNext()) {
				ImportSummary next = (ImportSummary) iter.next();
				Object nodeReturn = next.accept(this, data);
				if (((Boolean) nodeReturn).booleanValue()) {
					return true;
				}
			}
		}

		//  Not found in import statements
		return false;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  node  the file summary node to traverse
	 *@param  data  data to help with the traversal
	 *@return       return true if the types used the specified class
	 */
	private boolean checkTypes(FileSummary node, Object data) {
		Iterator iter = node.getTypes();

		if (iter != null) {
			while (iter.hasNext()) {
				TypeSummary next = (TypeSummary) iter.next();
				if (((Boolean) next.accept(this, data)).booleanValue()) {
					return true;
				}
			}
		}

		return false;
	}
}
