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

(defrecord SnippetGroup [name snippetlist])

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
  snippetgroup-snippets-userqueries
  "Returns the logic conditions defined by users of the snippets in the snippet group."
  [snippetgroup]
  (mapcat snippet/snippet-userquery (snippetgroup-snippetlist snippetgroup)))

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


;(defn
;  snippetgroup-new-state
;  [grp]
;  (let [new-snippetlist (map snippet/snippet-new-state  (:snippetlist grp))] 
;    (update-in grp [:snippetlist] (fn [x] new-snippetlist))))


(defn 
  add-snippet
  "Add snippet to snippetgroup."
  [snippetgroup snippet]
  (let [new-snippetlist (concat (snippetgroup-snippetlist snippetgroup) (list snippet))]
    (assoc snippetgroup :snippetlist new-snippetlist)))

(defn 
  remove-snippet
  "Remove snippet from snippetgroup."
  [snippetgroup snippet]
  (let [new-snippetlist (remove #{snippet} (snippetgroup-snippetlist snippetgroup))]
    (assoc snippetgroup :snippetlist new-snippetlist)))

  
;; Constructing SnippetGroup instances
;; -----------------------------------

(defn 
  make-snippetgroup
  "Create SnippetGroup instance."
  [name]
  (SnippetGroup. name '()))



;;OTHER FUNCTIONS' NAME
;;---------------------------

(def template-root snippet/snippet-root)
(def make-templategroup make-snippetgroup)

(defn
  register-callbacks 
  []
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_MAKE_SNIPPETGROUP) make-snippetgroup)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPETGROUP_NAME) snippetgroup-name)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPETGROUP_SNIPPET_FOR_NODE) snippetgroup-snippet-for-node)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_ADD_SNIPPET_TO_SNIPPETGROUP) add-snippet)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_REMOVE_SNIPPET_FROM_SNIPPETGROUP) remove-snippet)

  )

(register-callbacks)
