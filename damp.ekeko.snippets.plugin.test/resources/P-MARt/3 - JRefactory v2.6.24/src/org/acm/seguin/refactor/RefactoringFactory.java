/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor;

import org.acm.seguin.refactor.field.FieldRefactoringFactory;
import org.acm.seguin.refactor.field.PushDownFieldRefactoring;
import org.acm.seguin.refactor.field.PushUpFieldRefactoring;
import org.acm.seguin.refactor.field.RenameFieldRefactoring;
import org.acm.seguin.refactor.method.ExtractMethodRefactoring;
import org.acm.seguin.refactor.method.MethodRefactoringFactory;
import org.acm.seguin.refactor.method.MoveMethodRefactoring;
import org.acm.seguin.refactor.method.PushDownMethodRefactoring;
import org.acm.seguin.refactor.method.PushUpAbstractMethodRefactoring;
import org.acm.seguin.refactor.method.PushUpMethodRefactoring;
import org.acm.seguin.refactor.method.RenameParameterRefactoring;
import org.acm.seguin.refactor.type.AddAbstractParent;
import org.acm.seguin.refactor.type.AddChildRefactoring;
import org.acm.seguin.refactor.type.ExtractInterfaceRefactoring;
import org.acm.seguin.refactor.type.MoveClass;
import org.acm.seguin.refactor.type.RemoveEmptyClassRefactoring;
import org.acm.seguin.refactor.type.RenameClassRefactoring;
import org.acm.seguin.refactor.type.TypeRefactoringFactory;

/**
 *  Factory for all refactorings
 *
 *@author    Chris Seguin
 */
public class RefactoringFactory {
	/**
	 *  Generates the type refactorings
	 */
	private TypeRefactoringFactory typeFactory;

	/**
	 *  Generates the field refactorings
	 */
	private FieldRefactoringFactory fieldFactory;

	/**
	 *  Generates the method refactorings
	 */
	private MethodRefactoringFactory methodFactory;

	/**
	 *  Stores the singleton
	 */
	private static RefactoringFactory singleton;


	/**
	 *  Constructor for the RefactoringFactory object
	 */
	protected RefactoringFactory()
	{
		typeFactory = new TypeRefactoringFactory();
		fieldFactory = new FieldRefactoringFactory();
		methodFactory = new MethodRefactoringFactory();
	}


	/**
	 *  Adds a feature to the Child attribute of the TypeRefactoringFactory
	 *  object
	 *
	 *@return    Description of the Returned Value
	 */
	public AddChildRefactoring addChild()
	{
		prepare();
		return typeFactory.addChild();
	}


	/**
	 *  Adds a feature to the Parent attribute of the TypeRefactoringFactory
	 *  object
	 *
	 *@return    Description of the Returned Value
	 */
	public AddAbstractParent addParent()
	{
		prepare();
		return typeFactory.addParent();
	}


	/**
	 *  Creates a move class refactoring object
	 *
	 *@return    the move class refactoring object
	 */
	public MoveClass moveClass()
	{
		prepare();
		return typeFactory.moveClass();
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public RenameClassRefactoring renameClass()
	{
		prepare();
		return typeFactory.renameClass();
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public RemoveEmptyClassRefactoring removeEmptyClass()
	{
		prepare();
		return typeFactory.removeEmptyClass();
	}


	/**
	 *  Extracts the interface of a class into a new interface object
	 *
	 *@return    Description of the Returned Value
	 */
	public ExtractInterfaceRefactoring extractInterface()
	{
		prepare();
		return typeFactory.extractInterface();
	}


	/**
	 *  Moves the field into the parent class
	 *
	 *@return    Description of the Returned Value
	 */
	public PushDownFieldRefactoring pushDownField()
	{
		prepare();
		return fieldFactory.pushDownField();
	}


	/**
	 *  Renames a field
	 *
	 *@return    The refactoring
	 */
	public RenameFieldRefactoring renameField()
	{
		prepare();
		return fieldFactory.renameField();
	}


	/**
	 *  Moves the field into the child class
	 *
	 *@return    Description of the Returned Value
	 */
	public PushUpFieldRefactoring pushUpField()
	{
		prepare();
		return fieldFactory.pushUpField();
	}


	/**
	 *  Moves the method into the parent class
	 *
	 *@return    Description of the Returned Value
	 */
	public PushUpMethodRefactoring pushUpMethod()
	{
		prepare();
		return methodFactory.pushUpMethod();
	}


	/**
	 *  Moves the method signature into the parent class
	 *
	 *@return    Description of the Returned Value
	 */
	public PushUpAbstractMethodRefactoring pushUpAbstractMethod()
	{
		prepare();
		return methodFactory.pushUpAbstractMethod();
	}


	/**
	 *  Moves the method into a child class
	 *
	 *@return    Description of the Returned Value
	 */
	public PushDownMethodRefactoring pushDownMethod()
	{
		prepare();
		return methodFactory.pushDownMethod();
	}


	/**
	 *  Moves the method into another class
	 *
	 *@return    Description of the Returned Value
	 */
	public MoveMethodRefactoring moveMethod()
	{
		prepare();
		return methodFactory.moveMethod();
	}


	/**
	 *  Renames a parameter
	 *
	 *@return    Description of the Returned Value
	 */
	public RenameParameterRefactoring renameParameter()
	{
		prepare();
		return methodFactory.renameParameter();
	}


	/**
	 *  Extracts code from one method to create a new method
	 *
	 *@return    Description of the Returned Value
	 */
	public ExtractMethodRefactoring extractMethod()
	{
		return methodFactory.extractMethod();
	}


	/**
	 *  Prepare to create a refactoring that operates on files on the disk. This
	 *  is an IDE's last opportunity to save files before the refactoring is
	 *  performed. This is not used for ExtractMethod which works on code that is
	 *  in memory (rather than on the disk)
	 */
	protected void prepare() { }


	/**
	 *  This allows someone to replace this factory
	 *
	 *@param  value  The new Singleton value
	 */
	public static void setSingleton(RefactoringFactory value)
	{
		singleton = value;
	}


	/**
	 *  A standard method to get the factory
	 *
	 *@return    Description of the Returned Value
	 */
	public static RefactoringFactory get()
	{
		if (singleton == null) {
			init();
		}
		return singleton;
	}


	/**
	 *  Initializes the singleton
	 */
	private static synchronized void init()
	{
		if (singleton == null) {
			singleton = new RefactoringFactory();
		}
	}
}
