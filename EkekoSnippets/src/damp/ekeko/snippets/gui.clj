(ns 
  ^{:doc "GUI-related functionality of snippet-driven querying."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets.gui
  (:import 
    [org.eclipse.jdt.core.dom ASTNode]
    [org.eclipse.jface.viewers TreeViewerColumn]
    [org.eclipse.swt SWT]
    [org.eclipse.ui IWorkbench PlatformUI IWorkbenchPage IWorkingSet IWorkingSetManager]
    [org.eclipse.swt.widgets Display])
  (:require [damp.ekeko.snippets 
             [util :as util]
             [representation :as representation]
             ])
  (:require [damp.ekeko.jdt [astnode :as astnode]])
  (:require [damp.ekeko.gui]))

; View elements
; -------------

(defn
  snippetviewer-elements
  [snippet input]
  ;roots of treeview
  (if 
    (= input snippet)
    (to-array [(:ast snippet)])
    nil))

(defn
  snippetviewer-children
  [snippet p]
  ;treeview children of given treeview parent
  (cond 
     (astnode/ast? p) 
     (to-array (astnode/node-propertyvalues p)) 
     (astnode/lstvalue? p) 
     (to-array (seq (representation/snippet-value-for-node snippet p)))
     :else 
     (to-array [])))

(defn
  snippetviewer-parent
  [snippet c]
  ;treeview parent of given treeview child
  (astnode/owner c))

(defn
  snippetgroupviewer-elements
  [snippetgroup input]
  ;roots of treeview
  (if 
    (= input snippetgroup)
    (to-array [(:name snippetgroup)])
    nil))

(defn
  snippetgroupviewer-children
  [snippetgroup p]
  (if (= p (:name snippetgroup))
    (to-array (map (fn [x] (:ast x)) (representation/snippetgroup-snippetlist snippetgroup)))
    (snippetviewer-children
      (representation/snippetgroup-snippet-for-node snippetgroup p)
      p)))
      
(defn
  snippetgroupviewer-parent
  [snippetgroup c]
  (let [snippet (representation/snippetgroup-snippet-for-node snippetgroup c)]
    (if (= c (:ast snippet))
      (:name snippetgroup)
      (snippetviewer-parent snippet c))))
 


;; View Columns
;; ------------

(defn
  snippetviewercolumn-node
  [snippet element]
  (cond 
    (astnode/nilvalue? element)
    "null"
    (astnode/value? element)
    (str (representation/snippet-value-for-node snippet element))
    :else 
    (str element)))


(defn
  snippetviewercolumn-kind
  [snippet element]
  (cond 
    (astnode/nilvalue? element)
    "null"
    (astnode/value? element)
    (util/class-simplename (class  (:value element)))
    :else 
    (util/class-simplename (class element))))

(defn
  snippetviewercolumn-property
  [snippet element]
  (astnode/property-descriptor-id (astnode/owner-property element)))


(defn
  snippetviewercolumn-variable
  [snippet element]
  (str (representation/snippet-var-for-node snippet element)))

(defn
  snippetviewercolumn-grounder
  [snippet element]
  (str (representation/snippet-grounder-for-node snippet element)))

(defn
  snippetviewercolumn-constrainer
  [snippet element]
  (str (representation/snippet-constrainer-for-node snippet element)))

(defn
  snippetgroupviewercolumn-node
  [snippetgroup element]
  (snippetviewercolumn-node
    (representation/snippetgroup-snippet-for-node snippetgroup element)
    element))

(defn
  snippetgroupviewercolumn-variable
  [snippetgroup element]
  (snippetviewercolumn-variable
    (representation/snippetgroup-snippet-for-node snippetgroup element)
    element))


;; Opening a View
;; --------------

(def snippet-viewer-cnt (atom 0))

(defn 
  open-snippet-viewer
  [snippet]
  (let [page (-> (PlatformUI/getWorkbench)
               .getActiveWorkbenchWindow ;nil if called from non-ui thread 
               .getActivePage)
        qvid (damp.ekeko.snippets.SnippetViewer/ID)
        uniqueid (str @snippet-viewer-cnt)
        viewpart (.showView page qvid uniqueid (IWorkbenchPage/VIEW_ACTIVATE))]
    (swap! snippet-viewer-cnt inc)
    (.setViewID viewpart uniqueid)
    (.setInput (.getViewer viewpart) snippet)
    viewpart))


(defn
  view-snippet
  [snippet]
  (damp.ekeko.gui/eclipse-uithread-return (fn [] (open-snippet-viewer snippet))))

;; Print Snippet
;;-------------------

;; Print Snippet
;;--------------

(defn
  print-snippet-with-highlight
  [snippet highlightnode]
  (let [visitor (damp.ekeko.snippets.SnippetPrettyPrinter.)]
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
  (let [str-list (map print-snippet (representation/snippetgroup-snippetlist snippetgroup))]
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
        qvid (damp.ekeko.snippets.SnippetTextViewer/ID)
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


;; Opening a View - Snippet Group Plugin
;; -------------------------------------
    
(def snippetgroup-viewer-cnt (atom 0))

(defn 
  open-snippetgroup-viewer
  []
  (let [page (-> (PlatformUI/getWorkbench)
               .getActiveWorkbenchWindow ;nil if called from non-ui thread 
               .getActivePage)
        qvid (damp.ekeko.snippets.gui.SnippetView/ID)
        uniqueid (str @snippetgroup-viewer-cnt)
        viewpart (.showView page qvid uniqueid (IWorkbenchPage/VIEW_ACTIVATE))]
    (swap! snippetgroup-viewer-cnt inc)
    (.setViewID viewpart uniqueid)
    viewpart))


(defn
  view-snippetgroup
  []
  (damp.ekeko.gui/eclipse-uithread-return (fn [] (open-snippetgroup-viewer))))

