/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.summary;
import org.acm.seguin.parser.ast.ASTImportDeclaration;
import org.acm.seguin.parser.ast.ASTName;

/**
 *  Stores the summary of an import
 *
 *@author     Chris Seguin
 *@created    June 6, 1999
 */
public class ImportSummary extends Summary {
	//  Instance Variables
	private PackageSummary packageSummary;
	private String type;


	/**
	 *  Create an import summary
	 *
	 *@param  parentSummary  the parent summary
	 *@param  importDecl     the import declaration
	 */
	public ImportSummary(Summary parentSummary, ASTImportDeclaration importDecl) {
		//  Load parent class
		super(parentSummary);

		//  Local Variables
		ASTName name = (ASTName) importDecl.jjtGetChild(0);

		if (importDecl.isImportingPackage()) {
			type = null;
			packageSummary = PackageSummary.getPackageSummary(name.getName());
		}
		else {
			int last = name.getNameSize() - 1;
			type = name.getNamePart(last).intern();
			String packageName = getPackageName(last, name);
			packageSummary = PackageSummary.getPackageSummary(packageName);
		}
	}


	/**
	 *  Get the package
	 *
	 *@return    the package summary
	 */
	public PackageSummary getPackage() {
		return packageSummary;
	}


	/**
	 *  Get the type
	 *
	 *@return    the name of the type or null if this represents the entire
	 *      package
	 */
	public String getType() {
		return type;
	}


	/**
	 *  Provide method to visit a node
	 *
	 *@param  visitor  the visitor
	 *@param  data     the data for the visit
	 *@return          some new data
	 */
	public Object accept(SummaryVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}


	/**
	 *  Extract the name of the package
	 *
	 *@param  last  the index of the last
	 *@param  name  the name
	 *@return       the package name
	 */
	private String getPackageName(int last, ASTName name) {
		if (last > 0) {
			StringBuffer buffer = new StringBuffer(name.getNamePart(0));
			for (int ndx = 1; ndx < last; ndx++) {
				buffer.append(".");
				buffer.append(name.getNamePart(ndx));
			}
			return buffer.toString();
		}
		return "";
	}

	public String toString() {
		if (type == null)
			return "ImportSummary<" + packageSummary.getName() + ".*>";
		return "ImportSummary<" +  packageSummary.getName() + "." + type + ">";
	}

	public String getName() {
		return type;
	}
}
