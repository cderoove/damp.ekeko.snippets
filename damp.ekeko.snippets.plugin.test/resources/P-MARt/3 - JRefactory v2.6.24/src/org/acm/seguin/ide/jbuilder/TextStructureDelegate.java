package org.acm.seguin.ide.jbuilder;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.text.Document;
import javax.swing.tree.DefaultMutableTreeNode;

public class TextStructureDelegate extends TextStructure {
	private TextStructure delegate;
  // Constructors
  public TextStructureDelegate(TextStructure init) { delegate = init; }

  // Methods
  public void setExpandState(List p0) { delegate.setExpandState(p0); }
  public List getExpandState() { return delegate.getExpandState();}
  public void mouseExited(MouseEvent p0) { delegate.mouseExited(p0); }
  public void mouseEntered(MouseEvent p0) { delegate.mouseEntered(p0); }
  public void mouseReleased(MouseEvent p0) { delegate.mouseReleased(p0); }
  public void mousePressed(MouseEvent p0) { delegate.mousePressed(p0); }
  public void mouseClicked(MouseEvent p0) { delegate.mouseClicked(p0); }
  public void keyTyped(KeyEvent p0) { delegate.keyTyped(p0); }
  public void keyReleased(KeyEvent p0) { delegate.keyReleased(p0); }
  public void keyPressed(KeyEvent p0) { delegate.keyPressed(p0); }
  public Component getTreeCellRendererComponent(JTree p0, Object p1, boolean p2, boolean p3, boolean p4, int p5, boolean p6) {return delegate.getTreeCellRendererComponent(p0, p1, p2, p3, p4, p5, p6); }
  public void setCaretPosition(int p0, boolean p1) { delegate.setCaretPosition(p0, p1); }
  public void setCaretPosition(int p0, int p1, boolean p2) { delegate.setCaretPosition(p0, p1, p2);}
  public void setCaretOffset(int p0, boolean p1) { delegate.setCaretOffset(p0, p1);}
  public EditorPane getEditorPane() {return delegate.getEditorPane(); }
  public Icon getStructureIcon(Object p0) { return delegate.getStructureIcon(p0); }
  public void nodeActivated(DefaultMutableTreeNode p0) { delegate.nodeActivated(p0); }
  public void nodeSelected(DefaultMutableTreeNode p0) { delegate.nodeSelected(p0); }
  public JPopupMenu getPopup() { return delegate.getPopup(); }
  public void updateStructure(Document p0) { delegate.updateStructure(p0); }
  public JTree getTree() { return delegate.getTree(); }
  public void setTree(JTree p0) { delegate.setTree(p0); }
  public void setFileNode(FileNode p0) { delegate.setFileNode(p0); }
}
