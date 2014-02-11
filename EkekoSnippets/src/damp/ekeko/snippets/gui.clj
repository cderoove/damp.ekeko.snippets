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
             ])
  (:require [damp.ekeko.jdt [astnode :as astnode]])
  (:require [damp.ekeko.gui]))

; Callbacks for TemplateViewTreeContentProvider
; ---------------------------------------------

;;the following explicitly avoid creating new values that are not yet in the snippet datastructure


;(remove (fn [x] (astnode/primitivevalue? x))
(defn
  templateviewtreecontentprovider-children
  [snippetgroup val]
  ;treeview children of given treeview parent
  (to-array 
    (mapcat 
      (fn [snippet] 
        (snippet/snippet-node-children snippet val))
      (snippetgroup/snippetgroup-snippetlist snippetgroup))))
    
 
(defn
  templateviewtreecontentprovider-parent
  [snippetgroup c]
  ;treeview parent of given treeview child
  (some 
    (fn [snippet] 
      (snippet/snippet-node-owner snippet c))
    (snippetgroup/snippetgroup-snippetlist snippetgroup)))
    
(defn
  templateviewtreecontentprovider-elements
  [snippetgroup input]
  ;roots of treeview
  (when 
    (= input snippetgroup)
    (to-array (map snippet/snippet-root (snippetgroup/snippetgroup-snippetlist snippetgroup)))))
  

;; TemplateViewTreeLabelProvider
;; -----------------------------

(defn
  templateviewtreelabelprovider-node
  [snippet element]
  (cond 
    (astnode/nilvalue? element)
    "null"
    (astnode/value? element)
    (str (:value element))
    :else 
    (str element)))

(defn
  templateviewtreelabelprovider-kind
  [snippet element]
  (if
    (astnode/ast? element)
    (.getSimpleName (class element))
    ""))

(defn
  templateviewtreelabelprovider-property
  [snippet element]
  (let [property (astnode/owner-property element)
        id (astnode/property-descriptor-id property)]
    (if 
      (astnode/property-descriptor-list? property)
      (if 
        (astnode/lstvalue? element)
        (str "list " id)
        (str "element of " id))
      id)))
  

;; Opening  TemplateView programmatically
;; --------------------------------------

(def templateview-cnt (atom 0))

(defn 
  open-templateview
  [snippetgroup]
  (let [page (-> (PlatformUI/getWorkbench)
               .getActiveWorkbenchWindow ;nil if called from non-ui thread 
               .getActivePage)
        qvid (damp.ekeko.snippets.gui.TemplateView/ID)
        uniqueid (str @templateview-cnt)
        viewpart (.showView page qvid uniqueid (IWorkbenchPage/VIEW_ACTIVATE))]
    (swap! templateview-cnt inc)
    (.setViewID viewpart uniqueid)
    (.setInput (.getViewer viewpart) snippetgroup)
    viewpart))


(defn
  view-template
  [snippetgroup]
  (damp.ekeko.gui/eclipse-uithread-return (fn [] (open-templateview snippetgroup))))



;; Pretty printing
;;----------------

(defn
  print-plain-node
  [snippet node]
  (let [visitor (damp.ekeko.snippets.gui.viewer.SnippetPlainPrettyPrinter.)]
    (.setSnippet visitor snippet)
    (.accept node visitor)
    (.getResult visitor)))

(defn
  print-plain-snippet
  [snippet]
  (let [visitor (damp.ekeko.snippets.gui.viewer.SnippetPlainPrettyPrinter.)]
    (.setSnippet visitor snippet)
    (.accept (:ast snippet) visitor)
    (.getResult visitor)))

(defn
  print-snippet-with-highlight
  [snippet highlightnode]
  (let [visitor (damp.ekeko.snippets.gui.viewer.SnippetPrettyPrinter.)]
    (.setSnippet visitor snippet)
    (.setHighlightNode visitor highlightnode)
    (.accept (:ast snippet) visitor)
    (list (.getResult visitor) (.getHighlightPos visitor))))

(defn
  print-snippet
  [snippet]
  (first (print-snippet-with-highlight snippet nil))) 

(defn
  print-snippetgroup
  [snippetgroup]
  (let [str-list (map print-snippet (snippetgroup/snippetgroup-snippetlist snippetgroup))]
    (reduce str str-list))) 


;;OTHER FUNCTIONS' NAME
;;---------------------------

(defn print-template [s] (print (print-snippet s)))
(defn print-rewrite-sequence [s] (print (print-snippet s)))


(defn
  configure-callbacks
  []
  (set! (damp.ekeko.snippets.gui.TemplateViewTreeContentProvider/FN_ELEMENTS) templateviewtreecontentprovider-elements)
  (set! (damp.ekeko.snippets.gui.TemplateViewTreeContentProvider/FN_CHILDREN) templateviewtreecontentprovider-children)
  (set! (damp.ekeko.snippets.gui.TemplateViewTreeContentProvider/FN_PARENT) templateviewtreecontentprovider-parent)
  
  (set! (damp.ekeko.snippets.gui.TemplateViewTreeLabelProviders/FN_LABELPROVIDER_NODE) templateviewtreelabelprovider-node)
  (set! (damp.ekeko.snippets.gui.TemplateViewTreeLabelProviders/FN_LABELPROVIDER_KIND) templateviewtreelabelprovider-kind)
  (set! (damp.ekeko.snippets.gui.TemplateViewTreeLabelProviders/FN_LABELPROVIDER_PROPERTY) templateviewtreelabelprovider-property)
  
  ;;TODO: eliminate
  (set! (damp.ekeko.snippets.data.SnippetGroupHistory/FN_PRINT_SNIPPETGROUP) print-snippetgroup)
  (set! (damp.ekeko.snippets.data.SnippetGroupHistory/FN_PRINT_PLAINNODE) print-plain-node)
  (set! (damp.ekeko.snippets.data.SnippetGroupHistory/FN_PRINT_SNIPPETHIGHLIGHT) print-snippet-with-highlight)
  
  )


(configure-callbacks)


  
  
  
