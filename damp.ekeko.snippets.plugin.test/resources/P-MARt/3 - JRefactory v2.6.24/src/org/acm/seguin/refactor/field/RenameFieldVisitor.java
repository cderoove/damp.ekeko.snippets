/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor.field;

import java.util.Iterator;
import java.util.StringTokenizer;

import org.acm.seguin.parser.ChildrenVisitor;
import org.acm.seguin.parser.ast.ASTArguments;
import org.acm.seguin.parser.ast.ASTConstructorDeclaration;
import org.acm.seguin.parser.ast.ASTFieldDeclaration;
import org.acm.seguin.parser.ast.ASTMethodDeclaration;
import org.acm.seguin.parser.ast.ASTName;
import org.acm.seguin.parser.ast.ASTPackageDeclaration;
import org.acm.seguin.parser.ast.ASTPrimaryExpression;
import org.acm.seguin.parser.ast.ASTPrimaryPrefix;
import org.acm.seguin.parser.ast.ASTPrimarySuffix;
import org.acm.seguin.parser.ast.ASTUnmodifiedClassDeclaration;
import org.acm.seguin.parser.ast.ASTUnmodifiedInterfaceDeclaration;
import org.acm.seguin.parser.ast.ASTVariableDeclarator;
import org.acm.seguin.parser.ast.ASTVariableDeclaratorId;
import org.acm.seguin.summary.LocalVariableSummary;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.ParameterSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.VariableSummary;
import org.acm.seguin.summary.query.GetMethodSummary;
import org.acm.seguin.summary.query.GetTypeSummary;
import org.acm.seguin.summary.query.ImportsType;
import org.acm.seguin.summary.query.LookupVariable;

/**
 *  Visitor that traverses an AST and removes a specified field
 *
 *@author    Chris Seguin
 */
public class RenameFieldVisitor extends ChildrenVisitor {
	/**
	 *  Visit a package declaration
	 *
	 *@param  node  the class body node
	 *@param  data  the data for the visitor
	 *@return       the field if it is found
	 */
	public Object visit(ASTPackageDeclaration node, Object data)
	{
		RenameFieldData rfd = (RenameFieldData) data;

		ASTName name = (ASTName) node.jjtGetChild(0);
		PackageSummary packageSummary = PackageSummary.getPackageSummary(name.getName());
		rfd.setCurrentSummary(packageSummary);

		return super.visit(node, data);
	}


	/**
	 *  Visit a class declaration
	 *
	 *@param  node  the class body node
	 *@param  data  the data for the visitor
	 *@return       the field if it is found
	 */
	public Object visit(ASTUnmodifiedClassDeclaration node, Object data)
	{
		RenameFieldData rfd = (RenameFieldData) data;
		Summary current = rfd.getCurrentSummary();

		if (current == null) {
			rfd.setCurrentSummary(GetTypeSummary.query("", node.getName()));
		}
		else if (current instanceof PackageSummary) {
			rfd.setCurrentSummary(GetTypeSummary.query((PackageSummary) current, node.getName()));
		}
		else if (current instanceof TypeSummary) {
			rfd.setCurrentSummary(GetTypeSummary.query((TypeSummary) current, node.getName()));
		}
		else if (current instanceof MethodSummary) {
			rfd.setCurrentSummary(GetTypeSummary.query((MethodSummary) current, node.getName()));
		}

		Object result = super.visit(node, data);

		rfd.setCurrentSummary(current);
		return result;
	}


	/**
	 *  Visit a class declaration
	 *
	 *@param  node  the class body node
	 *@param  data  the data for the visitor
	 *@return       the field if it is found
	 */
	public Object visit(ASTUnmodifiedInterfaceDeclaration node, Object data)
	{
		RenameFieldData rfd = (RenameFieldData) data;
		Summary current = rfd.getCurrentSummary();

		if (current == null) {
			rfd.setCurrentSummary(GetTypeSummary.query("", node.getName()));
		}
		else if (current instanceof PackageSummary) {
			rfd.setCurrentSummary(GetTypeSummary.query((PackageSummary) current, node.getName()));
		}
		else if (current instanceof TypeSummary) {
			rfd.setCurrentSummary(GetTypeSummary.query((TypeSummary) current, node.getName()));
		}
		else if (current instanceof MethodSummary) {
			rfd.setCurrentSummary(GetTypeSummary.query((MethodSummary) current, node.getName()));
		}

		Object result = super.visit(node, data);

		rfd.setCurrentSummary(current);
		return result;
	}


	/**
	 *  Visit a field declaration
	 *
	 *@param  node  the class body node
	 *@param  data  the data for the visitor
	 *@return       the field if it is found
	 */
	public Object visit(ASTFieldDeclaration node, Object data)
	{
		RenameFieldData rfd = (RenameFieldData) data;

		if (rfd.getCurrentSummary() == rfd.getTypeSummary()) {
			for (int ndx = 1; ndx < node.jjtGetNumChildren(); ndx++) {
				ASTVariableDeclarator next = (ASTVariableDeclarator) node.jjtGetChild(ndx);
				ASTVariableDeclaratorId id = (ASTVariableDeclaratorId) next.jjtGetChild(0);
				if (id.getName().equals(rfd.getOldName())) {
					id.setName(rfd.getNewName());
				}
			}
		}

		return super.visit(node, data);
	}


	/**
	 *  Visit a primary expression
	 *
	 *@param  node  the class body node
	 *@param  data  the data for the visitor
	 *@return       the field if it is found
	 */
	public Object visit(ASTPrimaryExpression node, Object data)
	{
		RenameFieldData rfd = (RenameFieldData) data;
		ASTPrimaryPrefix prefix = (ASTPrimaryPrefix) node.jjtGetChild(0);
		if ("this".equals(prefix.getName())) {
			processThisExpression(rfd, node, prefix);
		}
		else if ((prefix.jjtGetNumChildren() >= 1) && (prefix.jjtGetChild(0) instanceof ASTName)) {
			processNameExpression(rfd, node, prefix);
		}

		return super.visit(node, data);
	}


	/**
	 *  Visit a method declaration
	 *
	 *@param  node  Description of Parameter
	 *@param  data  Description of Parameter
	 *@return       Description of the Returned Value
	 */
	public Object visit(ASTMethodDeclaration node, Object data)
	{
		RenameFieldData rfd = (RenameFieldData) data;

		Summary current = rfd.getCurrentSummary();
		MethodSummary found = GetMethodSummary.query((TypeSummary) current, node);
		rfd.setCurrentSummary(found);
		rfd.setMustInsertThis(isAlreadyPresent(found, rfd.getNewName()));

		boolean thisRequired = LookupVariable.getLocal(found, rfd.getOldName()) != null;
		rfd.setThisRequired(thisRequired);

		Object result = super.visit(node, data);

		rfd.setThisRequired(false);
		rfd.setCurrentSummary(current);

		return result;
	}


	/**
	 *  Visit a constructor declaration
	 *
	 *@param  node  Description of Parameter
	 *@param  data  Description of Parameter
	 *@return       Description of the Returned Value
	 */
	public Object visit(ASTConstructorDeclaration node, Object data)
	{
		RenameFieldData rfd = (RenameFieldData) data;

		Summary current = rfd.getCurrentSummary();
		MethodSummary found = GetMethodSummary.query((TypeSummary) current, node);
		rfd.setCurrentSummary(found);
		rfd.setMustInsertThis(isAlreadyPresent(found, rfd.getNewName()));

		boolean thisRequired = LookupVariable.getLocal(found, rfd.getOldName()) != null;
		rfd.setThisRequired(thisRequired);

		Object result = super.visit(node, data);

		rfd.setThisRequired(false);
		rfd.setCurrentSummary(current);

		return result;
	}


	/**
	 *  Determine if the new name is already present in the method
	 *
	 *@param  method   Description of Parameter
	 *@param  newName  Description of Parameter
	 *@return          The AlreadyPresent value
	 */
	private boolean isAlreadyPresent(MethodSummary method, String newName)
	{
		Iterator iter = method.getParameters();
		if (iter != null) {
			while (iter.hasNext()) {
				ParameterSummary next = (ParameterSummary) iter.next();
				if (next.getName().equals(newName)) {
					return true;
				}
			}
		}

		iter = method.getDependencies();
		if (iter != null) {
			while (iter.hasNext()) {
				Summary next = (Summary) iter.next();
				if ((next instanceof LocalVariableSummary) && (next.getName().equals(newName))) {
					return true;
				}
			}
		}

		return false;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  name                Description of Parameter
	 *@param  oldName             Description of Parameter
	 *@param  current             Description of Parameter
	 *@param  hasSuffixArguments  Description of Parameter
	 *@param  changingType        Description of Parameter
	 *@return                     Description of the Returned Value
	 */
	private int shouldChangePart(ASTName name, String oldName,
			Summary current, boolean hasSuffixArguments, TypeSummary changingType)
	{
		int last = name.getNameSize() - 1;
		if (hasSuffixArguments) {
			last--;
		}

		int forwardTo = -1;
		for (int ndx = last; ndx >= 0; ndx--) {
			if (name.getNamePart(ndx).equals(oldName)) {
				forwardTo = ndx;
			}
		}

		if (forwardTo == -1) {
			return -1;
		}

		VariableSummary varSummary = LookupVariable.query((MethodSummary) current, name.getNamePart(0));
		if (varSummary == null) {
			return -1;
		}
		TypeSummary currentType = GetTypeSummary.query(varSummary.getTypeDecl());

		for (int ndx = 1; ndx < forwardTo; ndx++) {
			varSummary = LookupVariable.queryFieldSummary(currentType, name.getNamePart(ndx));
			if (varSummary == null) {
				return -1;
			}
			currentType = GetTypeSummary.query(varSummary.getTypeDecl());
		}

		if (currentType == changingType) {
			return forwardTo;
		}

		return -1;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  rfd     Description of Parameter
	 *@param  node    Description of Parameter
	 *@param  prefix  Description of Parameter
	 */
	private void processThisExpression(RenameFieldData rfd, ASTPrimaryExpression node, ASTPrimaryPrefix prefix)
	{
		if (rfd.isAllowedToChangeThis() && (node.jjtGetNumChildren() >= 2)) {
			ASTPrimarySuffix suffix = (ASTPrimarySuffix) node.jjtGetChild(1);
			if (rfd.getOldName().equals(suffix.getName())) {
				boolean change = true;

				if (node.jjtGetNumChildren() >= 3) {
					ASTPrimarySuffix next = (ASTPrimarySuffix) node.jjtGetChild(2);
					if ((next.jjtGetChild(0) != null) && (next.jjtGetChild(0) instanceof ASTArguments)) {
						change = false;
					}
				}

				if (change) {
					suffix.setName(rfd.getNewName());
				}
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  rfd     Description of Parameter
	 *@param  node    Description of Parameter
	 *@param  prefix  Description of Parameter
	 */
	private void processNameExpression(RenameFieldData rfd, ASTPrimaryExpression node, ASTPrimaryPrefix prefix)
	{
		ASTName name = (ASTName) prefix.jjtGetChild(0);

		if (!rfd.isThisRequired()) {
			boolean hasSuffixArguments = false;

			if (node.jjtGetNumChildren() >= 2) {
				ASTPrimarySuffix next = (ASTPrimarySuffix) node.jjtGetChild(1);
				if ((next.jjtGetChild(0) != null) && (next.jjtGetChild(0) instanceof ASTArguments)) {
					hasSuffixArguments = true;
				}
			}

			if ((name.getNameSize() > 1) || !hasSuffixArguments) {
				if ((rfd.isAllowedToChangeFirst()) && (name.getNamePart(0).equals(rfd.getOldName()))) {
					name.setNamePart(0, rfd.getNewName());
					if (rfd.isMustInsertThis()) {
						name.insertNamePart(0, "this");
					}
				}
				else {
					int index = shouldChangePart(name, rfd.getOldName(), rfd.getCurrentSummary(), hasSuffixArguments, rfd.getTypeSummary());
					if (index > -1) {
						name.setNamePart(index, rfd.getNewName());
					}
				}
			}
		}

		if (rfd.getOldField().getModifiers().isStatic()) {
			String nameString = name.getName();
			if (nameString.startsWith(rfd.getFullName())) {
				replaceNamePart(name, rfd.getFullName(), rfd.getNewName());
			}
			else if (nameString.startsWith(rfd.getImportedName()) && ImportsType.query(rfd.getCurrentSummary(), rfd.getTypeSummary())) {
				replaceNamePart(name, rfd.getImportedName(), rfd.getNewName());
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  name     Description of Parameter
	 *@param  form     Description of Parameter
	 *@param  newName  Description of Parameter
	 */
	private void replaceNamePart(ASTName name, String form, String newName)
	{
		StringTokenizer tok = new StringTokenizer(form, ".");
		int count = -1;
		String finalPart = null;
		while (tok.hasMoreTokens()) {
			finalPart = tok.nextToken();
			count++;
		}

		if (name.getNamePart(count).equals(finalPart)) {
			name.setNamePart(count, newName);
		}
	}
}
