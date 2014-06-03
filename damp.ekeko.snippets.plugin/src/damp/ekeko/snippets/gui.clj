(ns 
  ^{:doc "GUI-related functionality of snippet-driven querying."
    :author "Coen De Roover, Siltvani"}
damp.ekeko.snippets.gui
  (:import 
    [org.eclipse.jdt.core.dom ASTNode CompilationUnit]
    [org.eclipse.jface.viewers TreeViewerColumn]
    [org.eclipse.swt SWT]
    [org.eclipse.ui IWorkbench PlatformUI IWorkbenchPage IWorkingSet IWorkingSetManager]
    [org.eclipse.swt.widgets Display])
  (:require [damp.ekeko.snippets 
             [util :as util]
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [operatorsrep :as operatorsrep]
             [directives :as directives]
             ])
  (:require [damp.ekeko.jdt [astnode :as astnode]])
  (:require [damp.ekeko.gui]))

; Callbacks for TemplateTreeContentProvider
; -----------------------------------------

;;the following explicitly avoid creating new values that are not yet in the snippet datastructure


;(remove (fn [x] (astnode/primitivevalue? x))

(defn
  snippet-node-parent|fortreeviewer
  [snippet c]
  (if-let [ownerproperty (astnode/owner-property c)] ;owner of compilationunit = nil, parent = nil
    ;owner of list = node
    ;owner of node = parent
    ;owner of list element = node ... should look for list containing value insted
    (if
      (and 
        (astnode/property-descriptor-list? ownerproperty)
        (not (astnode/lstvalue? c)))
      (snippet/snippet-list-containing snippet c)
      (snippet/snippet-node-owner snippet c))))

(defn
  snippet-node-children|fortreeviewer
  [snippet node]
  (filter 
    (fn [child]
      (=  node (snippet-node-parent|fortreeviewer snippet child)))
    (snippet/snippet-nodes snippet)))

(defn
  templatetreecontentprovider-children
  [snippetgroup val]
  ;treeview children of given treeview parent
  (to-array 
    (mapcat 
      (fn [snippet] (snippet-node-children|fortreeviewer snippet val))
      (snippetgroup/snippetgroup-snippetlist snippetgroup))))

(defn
  templatetreecontentprovider-parent
  [snippetgroup c]
  ;treeview parent of given treeview child
  (some 
    (fn [snippet] (snippet-node-parent|fortreeviewer snippet c))
    (snippetgroup/snippetgroup-snippetlist snippetgroup)))

(defn
  templatetreecontentprovider-elements
  [snippetgroup input]
  ;roots of treeview
  (when 
    (= input snippetgroup)
    (to-array (map snippet/snippet-root (snippetgroup/snippetgroup-snippetlist snippetgroup)))))


;; Callbacks for TemplateTreeLabelProvider
;; ---------------------------------------

(defn
  templatetreelabelprovider-node
  [snippet element]
  (cond 
    (astnode/nilvalue? element)
    "null"
    (astnode/value? element)
    (str (:value element))
    :else 
    (str element)))

(defn
  templatetreelabelprovider-kind
  [snippet element]
  (cond
    (astnode/ast? element)
    (.getSimpleName (class element))
    (astnode/lstvalue? element)
    "list"
    :default
    ""))

(defn
  templatetreelabelprovider-property
  [snippet element]
  (if-let [owner (astnode/owner element)]  ;compilation unit
    (let [property (astnode/owner-property element)
          id (astnode/property-descriptor-id property)
          idwithowner (str id " of " (.getSimpleName (class owner)))]
      (if 
        (astnode/property-descriptor-list? property)
        (if 
          (astnode/lstvalue? element)
          (str idwithowner)
          (str "element of " idwithowner))
        idwithowner))))

(defn
  templatetreelabelprovider-directives
  [snippet element]
  (snippet/snippet-bounddirectives-for-node snippet element))

; Callbacks for OperatorTreeContentProvider
; -----------------------------------------

(defn- 
  operator-category?
  [operator-or-category]
  (keyword? operator-or-category))

(defn
  operatortreecontentprovider-children
  [snippetgroup snippet snippetnode operator-or-category]
  ;treeview children of given treeview parent
  (to-array 
    (if
      (operator-category? operator-or-category)
      (operatorsrep/applicable-operators-in-category snippetgroup snippet snippetnode operator-or-category)
      [])))


(defn
  operatortreecontentprovider-parent
  [snippetgroup snippet snippetnode operator-or-category]
  (when-not
    (operator-category? operator-or-category)
    (operatorsrep/operator-category operator-or-category)))


(defn
  operatortreecontentprovider-elements
  [snippetgroup snippet snippetnode input]
  ;roots of treeview
  (to-array (operatorsrep/registered-categories)))


;; OperatorTreeLabelProvider
;; -----------------------------

(defn
  operatortreelabelprovider-operator
  [element]
  (if
    (operator-category? element)
    (operatorsrep/category-description element)
    (operatorsrep/operator-name element)))



;; Opening  TemplateView programmatically
;; --------------------------------------

;(def templateview-cnt (atom 0))

;(defn 
;  open-templateview
;  [snippetgroup]
;  (let [page (-> (PlatformUI/getWorkbench)
;               .getActiveWorkbenchWindow ;nil if called from non-ui thread 
;               .getActivePage)
;        qvid (damp.ekeko.snippets.gui.TemplateEditor/ID)
;        uniqueid (str @templateview-cnt)
;        viewpart (.showView page qvid uniqueid (IWorkbenchPage/VIEW_ACTIVATE))]
;    (swap! templateview-cnt inc)
;    (.setViewID viewpart uniqueid)
;    (.setInput (.getViewer viewpart) snippetgroup)
;    viewpart))


;(defn
;  view-template
;  [snippetgroup]
;  (damp.ekeko.gui/eclipse-uithread-return (fn [] (open-templateview snippetgroup))))




;; Callbacks for operator operand bindings
;; ---------------------------------------


(defn
  make-groupnode-selectiondialog
  [shell group template node]
  (damp.ekeko.snippets.gui.TemplateGroupNodeSelectionDialog. shell group template node))

    

(defmulti
  operandbinding-celleditor
  (fn [opviewer table group template subject operator operandbinding]
    (operatorsrep/operand-scope (operatorsrep/binding-operand operandbinding))))

(defmethod 
  operandbinding-celleditor
  operatorsrep/opscope-subject
  [opviewer table group template subject operator operandbinding]
  (let [editor 
        (proxy [org.eclipse.jface.viewers.DialogCellEditor] [table]
          (openDialogBox [window] 
            (let [shell (.getShell window)
                  dialog (make-groupnode-selectiondialog 
                           shell 
                           (operatorsrep/binding-group operandbinding)
                           (operatorsrep/binding-template operandbinding)
                           (operatorsrep/binding-value operandbinding))]
              (.open dialog)
              (.getValue dialog)
              )))]
    editor))

(defmethod 
  operandbinding-celleditor
  operatorsrep/opscope-string 
  [opviewer table group template subject operator operandbinding]
  (let [editor (org.eclipse.jface.viewers.TextCellEditor. table)]
    editor))

(defmethod 
  operandbinding-celleditor
  operatorsrep/opscope-variable 
  [opviewer table group template subject operator operandbinding]
  (let [editor (org.eclipse.jface.viewers.TextCellEditor. table)]
    editor))

(defn-
  make-comboviewcelleditor 
  [opviewer table group template subject operator operandbinding]
  (let [operand
        (operatorsrep/binding-operand operandbinding)
        values
        (operatorsrep/possible-operand-values|valid group template subject operator operand) 
        editor 
        (org.eclipse.jface.viewers.ComboBoxViewerCellEditor. table org.eclipse.swt.SWT/READ_ONLY)]
    (doto editor
      (.setContentProvider (org.eclipse.jface.viewers.ArrayContentProvider.))
      (.setLabelProvider (org.eclipse.jface.viewers.LabelProvider.))
      (.setInput (to-array values))
      (.setValue (first values))
      )
    editor))
  

(defmethod 
  operandbinding-celleditor
  operatorsrep/opscope-nodeclasskeyw
  [opviewer table group template subject operator operandbinding]
  (make-comboviewcelleditor opviewer table group template subject operator operandbinding))

(defmethod 
  operandbinding-celleditor
  operatorsrep/opscope-subjectlistidx
  [opviewer table group template subject operator operandbinding]
  (make-comboviewcelleditor opviewer table group template subject operator operandbinding))




(defn
  make-celleditor-for-operandbinding
  [table opviewer operandbinding]
  (let [operator 
        (.getSelectedOperator opviewer)
        subject 
        (.getSelectedSnippetNode opviewer)
        ;could also be taken from viewer
        group (operatorsrep/binding-group operandbinding)  
        template (operatorsrep/binding-template operandbinding)]
    (let [editor 
          (operandbinding-celleditor opviewer table group template subject operator operandbinding)
          operand
          (operatorsrep/binding-operand operandbinding)]
      (doto 
        editor
        (.setValidator 
          (proxy [org.eclipse.jface.viewers.ICellEditorValidator] []
            (isValid [value]
              (try 
                (operatorsrep/validate-newvalue-for-operandbinding
                  group template subject operator operandbinding value)
                nil ;indicates absence of error message
                (catch IllegalArgumentException e (.getMessage e))))))
        (.addListener
          (proxy [org.eclipse.jface.viewers.ICellEditorListener] []
            (applyEditorValue [])
            (cancelEditor []
              (damp.ekeko.gui/eclipse-uithread-do
                (fn [] (.updateWorkbenchStatusErrorLine opviewer ""))))
            (editorValueChanged [oldStateValid newStateValid]
              (damp.ekeko.gui/eclipse-uithread-do
                (fn []
                  (if
                    newStateValid
                    (.updateWorkbenchStatusErrorLine opviewer "")
                    (.updateWorkbenchStatusErrorLine opviewer (.getErrorMessage editor)))))))
              ))
      editor)))
           


(defn
  operandbinding-labelprovider-descriptiontext
  [operandbinding]
  (operatorsrep/operand-description (operatorsrep/binding-operand operandbinding)))


(defmulti
  operandbinding-labelprovider-valuetext
  (fn [operandbinding]
    (operatorsrep/operand-scope (operatorsrep/binding-operand operandbinding))))



(defmethod
  operandbinding-labelprovider-valuetext
  operatorsrep/opscope-subject
  [operandbinding]
  (let [grp (damp.ekeko.snippets.data.TemplateGroup/newFromClojureGroup (operatorsrep/binding-group operandbinding))
        pp (damp.ekeko.snippets.gui.TemplatePrettyPrinter. grp)]
    (.prettyPrintElement 
      pp 
      (operatorsrep/binding-template operandbinding)
      (operatorsrep/binding-value operandbinding))))

(defmethod
  operandbinding-labelprovider-valuetext
  :default
  [operandbinding]
  (str (operatorsrep/binding-value operandbinding)))







(defn
  configure-callbacks
  []
  (set! (damp.ekeko.snippets.gui.TemplateTreeContentProvider/FN_ELEMENTS) templatetreecontentprovider-elements)
  (set! (damp.ekeko.snippets.gui.TemplateTreeContentProvider/FN_CHILDREN) templatetreecontentprovider-children)
  (set! (damp.ekeko.snippets.gui.TemplateTreeContentProvider/FN_PARENT) templatetreecontentprovider-parent)
  (set! (damp.ekeko.snippets.gui.TemplateTreeLabelProviders/FN_LABELPROVIDER_NODE) templatetreelabelprovider-node)
  (set! (damp.ekeko.snippets.gui.TemplateTreeLabelProviders/FN_LABELPROVIDER_KIND) templatetreelabelprovider-kind)
  (set! (damp.ekeko.snippets.gui.TemplateTreeLabelProviders/FN_LABELPROVIDER_PROPERTY) templatetreelabelprovider-property)
  (set! (damp.ekeko.snippets.gui.TemplateTreeLabelProviders/FN_LABELPROVIDER_DIRECTIVES) templatetreelabelprovider-directives)
  
  
  (set! (damp.ekeko.snippets.gui.OperatorTreeContentProvider/FN_ELEMENTS) operatortreecontentprovider-elements) 
  (set! (damp.ekeko.snippets.gui.OperatorTreeContentProvider/FN_CHILDREN) operatortreecontentprovider-children)
  (set! (damp.ekeko.snippets.gui.OperatorTreeContentProvider/FN_PARENT) operatortreecontentprovider-parent) 
  (set! (damp.ekeko.snippets.gui.OperatorTreeLabelProvider/FN_LABELPROVIDER_OPERATOR)  operatortreelabelprovider-operator)
  
  (set! (damp.ekeko.snippets.gui.OperatorOperandBindingEditingSupport/FN_OPERANDBINDING_EDITOR) make-celleditor-for-operandbinding)
  (set! (damp.ekeko.snippets.gui.OperandBindingLabelProviderDescription/FN_LABELPROVIDER_DESCRIPTION_TEXT) operandbinding-labelprovider-descriptiontext)
  (set! (damp.ekeko.snippets.gui.OperatorOperandBindingLabelProviderValue/FN_LABELPROVIDER_DESCRIPTION_VALUE) operandbinding-labelprovider-valuetext)
  
  
  
  
  )


(configure-callbacks)





