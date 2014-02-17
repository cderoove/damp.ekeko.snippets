(ns 
    ^{:doc "Core functionality related to groups of snippets."
      :author "Coen De Roover, Siltvani"}
damp.ekeko.snippets.snippetgroup
  (:require [damp.ekeko.snippets 
             [snippet :as snippet]
             [util :as util]
             [parsing :as parsing]])
  (:require [damp.ekeko.jdt 
             [astnode :as astnode]])
  (:import [org.eclipse.jdt.core.dom.rewrite ASTRewrite])
  (:import [org.eclipse.jdt.core.dom ASTNode ASTNode$NodeList CompilationUnit]))


;; Snippets Group Datatype
;; ----------------------

; Datatype representing a group (list) of Snippet(s) and additional logic condition

(defrecord SnippetGroup [name snippetlist userquery])

(defn 
  snippetgroup-name
  "Returns name of the given snippet group."
  [snippetgroup]
  (:name snippetgroup))

(defn 
  snippetgroup-snippetlist
  "Returns the list of Snippet(s) of the given snippet group."
  [snippetgroup]
  (:snippetlist snippetgroup))

(defn 
  snippetgroup-userquery
  "Returns the logic conditions defined by users of the given snippet group."
  [snippetgroup]
  (let [query (:userquery snippetgroup)]
        (if (nil? query)
          '()
          query)))

(defn 
  snippetgroup-snippets-userqueries
  "Returns the logic conditions defined by users of the snippets in the snippet group."
  [snippetgroup]
  (mapcat snippet/snippet-userqueries (snippetgroup-snippetlist snippetgroup)))

(defn
  snippetgroup-nodes
  "Returns all nodes from the given snippet group."
  [snippetgroup]
  (mapcat snippet/snippet-nodes (snippetgroup-snippetlist snippetgroup)))

(defn
  snippetgroup-rootvars
  "Returns all logic variables of root node of all snippets in snippet group."
  [snippetgroup]
  (map snippet/snippet-var-for-root (snippetgroup-snippetlist snippetgroup)))


(defn
  snippetgroup-vars
  "Returns all logic variables from the given snippet group."
  [snippetgroup]
  (mapcat snippet/snippet-vars (snippetgroup-snippetlist snippetgroup)))

(defn
  snippetgroup-uservars
  "Returns all user logic variables from the given snippet group."
  [snippetgroup]
  (mapcat snippet/snippet-uservars (snippetgroup-snippetlist snippetgroup)))

(defn
  snippetgroup-uservars-for-information
  [snippetgroup]
  (mapcat snippet/snippet-uservars-for-information (snippetgroup-snippetlist snippetgroup)))

(defn
  snippetgroup-uservars-for-variable
  [snippetgroup]
  (mapcat snippet/snippet-uservars-for-variable (snippetgroup-snippetlist snippetgroup)))

(defn 
  snippetgroup-snippet-for-node
  [group node]
  (some 
    (fn [snippet]
      (when 
        (contains? (:ast2var snippet) node)
        snippet))
    (snippetgroup-snippetlist group)))
 

(defn snippetgroup-snippet-for-var
  [group var]
  (defn find-snippet [listsnippet var]
    (cond 
      (= var nil) nil
      (empty? listsnippet) nil
      (contains? (:var2ast (first listsnippet)) var) (first listsnippet)
      (.contains (snippet/snippet-uservars (first listsnippet)) var) (first listsnippet)
      :else (find-snippet (rest listsnippet) var)))
  (find-snippet (snippetgroup-snippetlist group) var))

(defn snippetgroup-snippet-index
  [group snippet]
  (.indexOf (snippetgroup-snippetlist group) snippet))

(defn snippetgroup-replace-snippet
  [group oldsnippet newsnippet]
  (let [newlist (replace {oldsnippet newsnippet} (:snippetlist group))]
    (update-in group [:snippetlist] (fn [x] newlist))))

(defn 
  snippetgroup-var-for-node
  [grp node]
  (snippet/snippet-var-for-node
    (snippetgroup-snippet-for-node grp node)
    node))

(defn 
  snippetgroup-node-for-var
  [grp var]
  (snippet/snippet-node-for-lvar
    (snippetgroup-snippet-for-var grp var)
    var))

(defn
  snippetgroup-userfs
  [grp]
  "Returns all ast to user functions of the given grp."
  (mapcat snippet/snippet-userfs (:snippetlist grp)))


(defn
  snippetgroup-new-state
  [grp]
  (let [new-snippetlist (map snippet/snippet-new-state  (:snippetlist grp))] 
    (update-in grp [:snippetlist] (fn [x] new-snippetlist))))
  
  
;; Constructing SnippetGroup instances
;; -----------------------------------

(defn 
  make-snippetgroup
  "Create SnippetGroup instance."
  [name]
  (SnippetGroup. name '() '()))



;; Pretty printing
;; ----------------


(defn
  print-snippetgroup
  [snippetgroup]
  (let [str-list (map snippet/print-snippet (snippetgroup-snippetlist snippetgroup))]
    (reduce str str-list))) 



;;OTHER FUNCTIONS' NAME
;;---------------------------

(def document-as-template snippet/document-as-snippet)
(def template-root snippet/snippet-root)
(def make-templategroup make-snippetgroup)

(defn
  register-callbacks 
  []
  (set! (damp.ekeko.snippets.data.SnippetGroupHistory/FN_SNIPPETGROUP_NAME) snippetgroup-name)
  (set! (damp.ekeko.snippets.data.SnippetGroupHistory/FN_SNIPPETGROUP_SNIPPET_FOR_NODE) snippetgroup-snippet-for-node)
  (set! (damp.ekeko.snippets.data.SnippetGroupHistory/FN_SNIPPETGROUP_USERQUERY) snippetgroup-userquery)
  (set! (damp.ekeko.snippets.data.SnippetGroupHistory/FN_SNIPPETGROUPHISTORY_NEWSTATE) snippetgroup-new-state)
  (set! (damp.ekeko.snippets.data.SnippetGroupHistory/FN_PRINT_SNIPPETGROUP) print-snippetgroup)

  )

(register-callbacks)
