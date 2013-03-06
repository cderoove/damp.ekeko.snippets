package damp.ekeko.snippets.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import clojure.lang.LazySeq;
import clojure.lang.RT;

public class OperatorViewer {
	private Tree tree;
	private MainView mainView;

	public OperatorViewer(MainView mainView, Composite parent, int style) {
		tree = new Tree(parent, style);
		this.mainView = mainView;
		setActions();
	}
	
	public void showOperators() {
		TreeItem root = new TreeItem(tree, 0);
		root.setText("Operator");
		LazySeq operators = (LazySeq) RT.var("damp.ekeko.snippets.precondition", "operator-names").invoke();

		for (int i = 0; i < operators.size(); i++) {
			TreeItem child = new TreeItem(root, 0);
			child.setText(operators.get(i).toString().replace(":", ""));
		}	
		
		tree.getItems()[0].setExpanded(true);	
	}

	public void setActions() {
		tree.setBounds(0, 0, 100, 100);
		
		tree.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
		        mainView.onOperatorSelection(getSelected());
			}
		});		
	}

	public String getSelected() {
        TreeItem[] selection = tree.getSelection();
        return selection[0].getText();
	}
}
