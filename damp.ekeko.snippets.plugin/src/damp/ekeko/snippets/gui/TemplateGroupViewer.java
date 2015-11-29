package damp.ekeko.snippets.gui;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import clojure.lang.IFn;
import damp.ekeko.snippets.EkekoSnippetsPlugin;
import damp.ekeko.snippets.data.SnippetOperator;
import damp.ekeko.snippets.data.TemplateGroup;

public class TemplateGroupViewer extends Composite {

	public static IFn FN_SNIPPET_VALUE_FOR_IDENTIFIER;

	public Object getValueForIdentifierInTemplate(Object identifier) {
		return FN_SNIPPET_VALUE_FOR_IDENTIFIER.invoke(cljTemplate, identifier);
	}

	private TextViewer textViewerSnippet;
	private TreeViewer snippetTreeViewer;
	private TreeViewerColumn snippetKindCol;
	private TreeViewerColumn snippetPropCol;
	private TreeViewerColumn snippetNodeCol;
	private TreeViewerColumn snippetDirectivesCol;

	private List<TemplateGroupViewerNodeSelectionListener> nodeSelectionListeners; 
	private List<TemplateGroupViewerNodeDoubleClickListener> nodeDoubleClickListeners; 

	private TemplateGroup jGroup;
	private Object cljTemplate, cljNode;
	private TreeViewerColumn snippetElementCol;
	//private TextViewer textViewerNode;

	private List<StyleRange> hyperlinks;
	private StyleRange lastUnderlined;

	private TemplateEditor parentTemplateEditor;

	public void setParentTemplateEditor(TemplateEditor editor) {
		parentTemplateEditor = editor;
	}

	public TemplateEditor getParentTemplateEditor() {
		return parentTemplateEditor;
	}

	public TemplateGroupViewer(Composite parent, int style) {
		super(parent, SWT.NONE);

		nodeSelectionListeners = new LinkedList<TemplateGroupViewerNodeSelectionListener>();
		nodeDoubleClickListeners = new LinkedList<TemplateGroupViewerNodeDoubleClickListener>();
		hyperlinks = new LinkedList<StyleRange>();
		

		//Composite composite = this;
		//gridLayout.marginWidth = 0;
		//gridLayout.marginHeight = 0;
		//composite.setLayout(gridLayout);

		this.setLayout(new FillLayout());
		SashForm composite = new SashForm(this, SWT.VERTICAL);

		textViewerSnippet = new TextViewer(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		
		final StyledText styledText = textViewerSnippet.getTextWidget();
		//GridData gd_styledText = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		//gd_styledText.heightHint = 100;
		//styledText.setLayoutData(gd_styledText);
		textViewerSnippet.setEditable(false);		
		
		styledText.setFont(EkekoSnippetsPlugin.getEditorFont());
		styledText.setCaret(null);


		//Label label = new Label(getShell(), SWT.SINGLE);
		//RGB background = label.getBackground().getRGB();
		//label.dispose();


		/*
		textViewerNode = new TextViewer(composite, SWT.NONE | SWT.WRAP | SWT.READ_ONLY | SWT.NO_FOCUS);
		StyledText textViewerNodeText = textViewerNode.getTextWidget();
		GridData gd_styledTextNode = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
		//gd_styledTextNode.heightHint = 100;
		textViewerNodeText.setLayoutData(gd_styledTextNode);
		//textViewerNodeText.setFont(EkekoSnippetsPlugin.getEditorFont());
		textViewerNodeText.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		textViewerNodeText.setCaret(null);
		 */

		snippetTreeViewer = new TreeViewer(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		snippetTreeViewer.setAutoExpandLevel(1);
		Tree treeSnippet = snippetTreeViewer.getTree();
		treeSnippet.setHeaderVisible(true);
		treeSnippet.setLinesVisible(true);
		//treeSnippet.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		snippetNodeCol = new TreeViewerColumn(snippetTreeViewer, SWT.NONE);
		TreeColumn trclmnNode = snippetNodeCol.getColumn();
		trclmnNode.setWidth(150);
		trclmnNode.setText("Element");

		snippetElementCol = new TreeViewerColumn(snippetTreeViewer, SWT.NONE);
		TreeColumn templateElementColCol = snippetElementCol.getColumn();
		templateElementColCol.setWidth(150);
		templateElementColCol.setText("Textual Representation");

		snippetKindCol = new TreeViewerColumn(snippetTreeViewer, SWT.NONE);
		TreeColumn snippetKindColCol = snippetKindCol.getColumn();
		snippetKindColCol.setWidth(150);
		snippetKindColCol.setText("Element Kind");

		snippetPropCol = new TreeViewerColumn(snippetTreeViewer, SWT.NONE);
		TreeColumn trclmnProperty = snippetPropCol.getColumn();
		trclmnProperty.setWidth(150);
		trclmnProperty.setText("Description");

		snippetDirectivesCol = new TreeViewerColumn(snippetTreeViewer, SWT.NONE);
		TreeColumn snippetDirectivesColCol = snippetDirectivesCol.getColumn();
		snippetDirectivesColCol.setWidth(150);
		snippetDirectivesColCol.setText("Directives");
		composite.setWeights(new int[] {144, 153});




		snippetTreeViewer.setContentProvider(new TemplateTreeContentProvider());

		treeSnippet.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				onNodeSelectionInternal();
			}
		});	

		// Set context menu for selected tree nodes
	    Menu popupMenu = new Menu(treeSnippet);
	    
	    MenuItem deleteItem = new MenuItem(popupMenu, SWT.NONE);
	    deleteItem.setText("Remove node");
	    deleteItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				jGroup.applyOperator(SnippetOperator.operatorFromId("remove-node"), 
						SnippetOperator.getOperands(
								jGroup, getSelectedSnippet(), getSelectedSnippetNode(), 
								SnippetOperator.operatorFromId("remove-node")));
				updateWidgets();
				parentTemplateEditor.becomeDirty();
			}
		});

	    MenuItem replaceByWildCardItem = new MenuItem(popupMenu, SWT.NONE);
	    replaceByWildCardItem.setText("Replace by wildcard");
	    replaceByWildCardItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				jGroup.applyOperator(SnippetOperator.operatorFromId("replace-by-wildcard"), 
						SnippetOperator.getOperands(
								jGroup, getSelectedSnippet(), getSelectedSnippetNode(), 
								SnippetOperator.operatorFromId("replace-by-wildcard")));
				updateWidgets();
				parentTemplateEditor.becomeDirty();
			}
		});
	    
	    treeSnippet.setMenu(popupMenu);
		
		snippetTreeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				onNodeDoubleClickInternal();
			}
		}); 
		
		styledText.setBlockSelection(false);

		styledText.addListener(SWT.MouseMove, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					int offset = styledText.getOffsetAtLocation(new Point(event.x, event.y));
					StyleRange smallestEncompassing = getSmallestEncompassingHyperlink(offset);
			
					if(lastUnderlined != null) {
						if(lastUnderlined.equals(smallestEncompassing))
							return;
						
						StyleRange[] styleRanges = styledText.getStyleRanges(lastUnderlined.start, lastUnderlined.length);
						for(StyleRange range : styleRanges) {
							range.underline = false;
						}
						styledText.replaceStyleRanges(lastUnderlined.start, lastUnderlined.length, styleRanges);						
						lastUnderlined = null;
					}
					
					
					if(smallestEncompassing != null) {
						lastUnderlined = smallestEncompassing;
						StyleRange[] styleRanges = styledText.getStyleRanges(smallestEncompassing.start, smallestEncompassing.length);
						for(StyleRange range : styleRanges) {
							range.underlineStyle = SWT.UNDERLINE_LINK;
							range.underline = true;
						}
						styledText.replaceStyleRanges(smallestEncompassing.start, smallestEncompassing.length, styleRanges);
						//styledText.redraw();	
					}


				}
				catch(IllegalArgumentException e) {
					//when mouse cursor out on boundary of widget
				}

			}

		});




		styledText.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				try {
					int offset = styledText.getOffsetAtLocation(new Point(event.x, event.y));
					StyleRange smallestEncompassing = getSmallestEncompassingHyperlink(offset);

					if(smallestEncompassing != null) {
						Object valueIdentifier = smallestEncompassing.data;
						if(valueIdentifier != null) {
							Object valueForIdentifierInTemplateGroup = getValueForIdentifierInTemplate(valueIdentifier);
							if(valueForIdentifierInTemplateGroup != null) {
								snippetTreeViewer.setSelection(new StructuredSelection(valueForIdentifierInTemplateGroup), true);
								//updateTextFields();
								onNodeSelectionInternal();
								snippetTreeViewer.getControl().setFocus();

							}
						}
					}

				} catch (IllegalArgumentException e) {
					// no character under event.x, event.y
				}

			}

		});



	}

	private StyleRange getSmallestEncompassingHyperlink(int offset) {
		StyleRange smallestEncompassing = null;
		for(StyleRange link : hyperlinks) {
			//find candidate
			if(link.start <= offset && offset <= link.start + link.length) {
				//find candidate with smallest length
				if(smallestEncompassing == null) {
					smallestEncompassing = link;
				}
				else {
					if(link.length <= smallestEncompassing.length) {
						smallestEncompassing = link;
					}
				}
			}
		}
		return smallestEncompassing;


	}

	private void onNodeDoubleClickInternal() {
		TemplateGroupViewerNodeSelectionEvent event = new TemplateGroupViewerNodeSelectionEvent(this, jGroup, cljTemplate, cljNode);
		for(TemplateGroupViewerNodeDoubleClickListener listener : nodeDoubleClickListeners) {
			listener.nodeDoubleClicked(event);
		}

	}

	private void onNodeSelectionInternal() {
		updateTextFields();
		TemplateGroupViewerNodeSelectionEvent event = new TemplateGroupViewerNodeSelectionEvent(this, jGroup, cljTemplate, cljNode);
		for(TemplateGroupViewerNodeSelectionListener listener : nodeSelectionListeners) {
			listener.nodeSelected(event);
		}

	}

	public boolean addNodeSelectionListener(TemplateGroupViewerNodeSelectionListener listener) {
		return nodeSelectionListeners.add(listener);
	}

	public boolean removeNodeSelectionListener(TemplateGroupViewerNodeSelectionListener listener) {
		return nodeSelectionListeners.remove(listener);
	}

	public boolean addNodeDoubleClickListener(TemplateGroupViewerNodeDoubleClickListener listener) {
		return nodeDoubleClickListeners.add(listener);
	}

	public boolean removeNodeDoubleClickListener(TemplateGroupViewerNodeDoubleClickListener listener) {
		return nodeDoubleClickListeners.remove(listener);
	}


	public Object getSelectedSnippetNode() {
		IStructuredSelection selection = (IStructuredSelection) snippetTreeViewer.getSelection();
		cljNode = selection.getFirstElement();
		return cljNode;
	}

	public Object getSelectedSnippet() {
		cljTemplate = jGroup.getSnippet(getSelectedSnippetNode());
		return cljTemplate;
	}

	private void updateTextFields() {
		StyledText textWidget = textViewerSnippet.getTextWidget();


		Object selectedSnippet = getSelectedSnippet();
		if(selectedSnippet == null) {
			textWidget.setText("");
			return;
		}			

		Object selectedSnippetNode = getSelectedSnippetNode();
		TemplatePrettyPrinter prettyprinter = new TemplatePrettyPrinter(jGroup);
		prettyprinter.setHighlightNode(selectedSnippetNode);
		textWidget.setText(prettyprinter.prettyPrintSnippet(selectedSnippet));
		for(StyleRange range : prettyprinter.getStyleRanges())
			textWidget.setStyleRange(range);

		this.hyperlinks = prettyprinter.getHyperlinks();


	}

	public void clearSelection() {
		cljTemplate = null;
		cljNode = null;
		snippetTreeViewer.setSelection(null);
	}

	public void updateWidgets() {
		setInput(jGroup, cljTemplate, cljNode);
		updateTextFields();
	}
	
	public void setSelection(Object cljTemplate, Object cljNode) {
		if(cljNode != null) {
			//set selection to node
			snippetTreeViewer.setSelection(new StructuredSelection(cljNode), true);
		}
		else if (cljTemplate != null) {
			//set selection to template
			snippetTreeViewer.setSelection(new StructuredSelection(cljTemplate), true);
		}
		else  {
			//set selection to first template in template group
			Tree tree = snippetTreeViewer.getTree();
			TreeItem[] items = tree.getItems();
			if(items.length > 0)
				tree.setSelection(items[0]);
		}
	}

	public void setInput(TemplateGroup jGroup, Object cljTemplate, Object cljNode) {
		this.jGroup = jGroup;
		this.cljTemplate = cljTemplate;
		this.cljNode = cljNode;

		Object cljGroup = jGroup.getGroup();

		snippetElementCol.setLabelProvider(new TemplateTreeLabelProviders.ElementColumnLabelProvider(cljGroup));	
		snippetNodeCol.setLabelProvider(new TemplateTreeLabelProviders.NodeColumnLabelProvider(cljGroup));		
		snippetPropCol.setLabelProvider(new TemplateTreeLabelProviders.PropertyColumnLabelProvider(cljGroup));
		snippetKindCol.setLabelProvider(new TemplateTreeLabelProviders.KindColumnLabelProvider(cljGroup));
		snippetDirectivesCol.setLabelProvider(new TemplateTreeLabelProviders.DirectivesColumnLabelProvider(cljGroup));
		snippetTreeViewer.setInput(cljGroup);
		setSelection(cljTemplate, cljNode);
		onNodeSelectionInternal();


	}

	@Override
	public boolean setFocus() {
		return snippetTreeViewer.getControl().setFocus();
	}

}

