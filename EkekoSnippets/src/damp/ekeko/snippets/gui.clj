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


(defn
  templateviewtreecontentprovider-children
  [snippet val]
  ;treeview children of given treeview parent
  (to-array
    (cond 
      (astnode/ast? val) (util/filtered-node-propertyvalues val)
      (astnode/lstvalue? val) (:value val)
      :else [])))

(defn
  templateviewtreecontentprovider-parent
  [snippet c]
  ;treeview parent of given treeview child
  (astnode/owner c))

(defn
  templateviewtreecontentprovider-elements
  [snippetgroup input]
  ;roots of treeview
  (if 
    (= input snippetgroup)
    (to-array (map snippet/snippet-root (snippetgroup/snippetgroup-snippetlist snippetgroup)))
    nil))



;; TemplateViewTreeLabelProvider
;; -----------------------------

(defn
  templateviewtreelabelprovider-node
  [snippet element]
  (cond 
    (astnode/nilvalue? element)
    "null"
    (astnode/value? element)
    (str (snippet/snippet-value-for-node snippet element))
    :else 
    (str element)))


(defn
  templateviewtreelabelprovider-property
  [snippet element]
  (astnode/property-descriptor-id (astnode/owner-property element)))

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



;; Print Snippet
;;--------------

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


;; Opening a View - Snippet Text
;; -----------------------------

(def snippet-text-viewer-cnt (atom 0))

(defn 
  open-snippet-text-viewer
  [snippet]
  (let [page (-> (PlatformUI/getWorkbench)
               .getActiveWorkbenchWindow ;nil if called from non-ui thread 
               .getActivePage)
        qvid (damp.ekeko.snippets.gui.viewer.SnippetTextViewer/ID)
        uniqueid (str @snippet-text-viewer-cnt)
        viewpart (.showView page qvid uniqueid (IWorkbenchPage/VIEW_ACTIVATE))]
    (swap! snippet-text-viewer-cnt inc)
    (.setViewID viewpart uniqueid)
    (.setInput viewpart snippet (:ast snippet))
    viewpart))

(defn
  view-snippet-text
  [snippet]
  (damp.ekeko.gui/eclipse-uithread-return (fn [] (open-snippet-text-viewer snippet))))



;;OTHER FUNCTIONS' NAME
;;---------------------------

(defn print-template [s] (print (print-snippet s)))
(defn print-rewrite-sequence [s] (print (print-snippet s)))
