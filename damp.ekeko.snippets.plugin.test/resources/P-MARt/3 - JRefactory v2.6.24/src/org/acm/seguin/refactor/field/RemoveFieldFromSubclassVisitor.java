/*
 * Author:  Chris Seguin
 * 
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.refactor.field;

import java.util.Iterator;

import org.acm.seguin.refactor.ComplexTransform;
import org.acm.seguin.summary.FieldSummary;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.TraversalVisitor;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.query.Ancestor;
import org.acm.seguin.summary.query.GetTypeSummary;

/**
 *  Removes the field from all subclasses of a particular class. 
 *
 *@author    Chris Seguin 
 */
public class RemoveFieldFromSubclassVisitor extends TraversalVisitor {
	private FieldSummary target;
	private TypeSummary ancestor;
	private ComplexTransform complex;
	private TypeSummary notHere;


	/**
	 *  Constructor for the RemoveFieldFromSubclassVisitor object 
	 *
	 *@param  type        the ancestor type 
	 *@param  init        the field 
	 *@param  notThisOne  a type to skip 
	 *@param  transform   Description of Parameter 
	 */
	public RemoveFieldFromSubclassVisitor(TypeSummary type, 
			FieldSummary init, TypeSummary notThisOne, 
			ComplexTransform transform) {
		target = init;
		ancestor = type;
		notHere = notThisOne;
		complex = transform;
	}


	/**
	 *  Visits a file summary node and updates it if necessary 
	 *
	 *@param  fileSummary  Description of Parameter 
	 *@param  data         Description of Parameter 
	 *@return              Description of the Returned Value 
	 */
	public Object visit(FileSummary fileSummary, Object data) {
		complex.clear();
		super.visit(fileSummary, data);
		if (complex.hasAnyChanges()) {
			complex.apply(fileSummary.getFile(), fileSummary.getFile());
		}
		return data;
	}


	/**
	 *  Visits a type summary and updates it 
	 *
	 *@param  typeSummary  Description of Parameter 
	 *@param  data         Description of Parameter 
	 *@return              Description of the Returned Value 
	 */
	public Object visit(TypeSummary typeSummary, Object data) {
		if ((typeSummary != notHere) && Ancestor.query(typeSummary, ancestor)) {
			Iterator iter = typeSummary.getFields();
			if (iter != null) {
				while (iter.hasNext()) {
					visit((FieldSummary) iter.next(), data);
				}
			}
		}
		return data;
	}


	/**
	 *  Visits the field summary and determines if it should be removed. 
	 *
	 *@param  fieldSummary  Description of Parameter 
	 *@param  data          Description of Parameter 
	 *@return               Description of the Returned Value 
	 */
	public Object visit(FieldSummary fieldSummary, Object data) {
		if (fieldSummary.getName().equals(target.getName())) {
			TypeDeclSummary current = fieldSummary.getTypeDecl();
			TypeDeclSummary targetDecl = target.getTypeDecl();
			if (GetTypeSummary.query(current) == GetTypeSummary.query(targetDecl)) {
				complex.add(new RemoveFieldTransform(target.getName()));
			}
		}

		return data;
	}
}
