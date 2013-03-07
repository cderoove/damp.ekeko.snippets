package damp.ekeko.snippets.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import clojure.lang.Keyword;
import clojure.lang.LazySeq;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Symbol;

public class OperatorViewer {
	//static {	
	//	RT.var("clojure.core", "require").invoke(Symbol.intern("damp.ekeko.snippets.precondition"));
	//}		

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
	
	public String getOperatorArgumentsInformation() {
		String str = "";
		PersistentVector args = (PersistentVector) RT.var("damp.ekeko.snippets.precondition", "operator-arguments").invoke(Keyword.intern(getSelected()));		
		
		if (args != null) {
			for (int i = 0; i < args.size(); i++) {
				str += "-" + args.get(i) + "\n";
			}	
			
			if (args.size() > 0) 
				str = "\nPlease input parameter below:\n" + str;
	
			if (args.size() > 1) 
				str += "\n(Separate each argument by \":\")\n";
		}
		
		return str;
	}
}
