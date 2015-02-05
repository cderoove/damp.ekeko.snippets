package damp.ekeko.snippets.geneticsearch;

import org.eclipse.jdt.core.ITypeHierarchyChangedListener;
import org.eclipse.jdt.core.dom.ASTNode;

import damp.ekeko.JavaProjectModel;
import damp.ekeko.TableGatheringVisitor;

/**
 * A PartialJavaProjectModel is useful if you want to query only very specific parts of code.
 * More specifically, a PartialJavaProjectModel is like a view/filter on an existing JavaProjectModel:
 * You can set it up such that you're querying only certain AST subtrees. 
 * (, whereas in a normal JavaProjectModel, you can only filter at the level of entire compilation units..) 
 * 
 * @author Tim
 */
public class PartialJavaProjectModel extends JavaProjectModel implements ITypeHierarchyChangedListener {
	public PartialJavaProjectModel() {
		super(null);
	}
	
	/**
	 * Add an AST subtree (from another JavaProjectModel) to this partial model
	 * @param node
	 */
	public void addExistingAST(ASTNode node) {
		TableGatheringVisitor v = new TableGatheringVisitor();
		node.accept(v);
		addInformationFromVisitor(v);
	}
}