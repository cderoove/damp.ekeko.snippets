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
  (:require [damp.ekeko.snippets [util :as util]])
  (:require [damp.ekeko.jdt [astnode :as astnode]])
  (:require [damp.ekeko.gui]))

; Eclipse view on snippets
; ------------------------

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
     (to-array (seq (:value p)))
     :else 
     (to-array [])))


(defn
  snippetviewer-parent
  [snippet c]
  ;treeview parent of given treeview child
  (astnode/owner c))

(defn
  snippetviewercolumn-kind
  [snippet element]
  (util/class-simplename (class element)))

(defn
  snippetviewercolumn-variable
  [snippet element]
  (str (snippet-var-for-node snippet element)))

(defn
  snippetviewercolumn-grounder
  [snippet element]
  (str (snippet-grounder-for-node snippet element)))

(defn
  snippetviewercolumn-constrainer
  [snippet element]
  (str (snippet-constrainer-for-node snippet element)))

   
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


    
