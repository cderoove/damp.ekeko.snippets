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


(declare flat-map)

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
  (flat-map snippet/snippet-userqueries (snippetgroup-snippetlist snippetgroup)))

(defn
  snippetgroup-nodes
  "Returns all nodes from the given snippet group."
  [snippetgroup]
  (flat-map snippet/snippet-nodes (snippetgroup-snippetlist snippetgroup)))

(defn
  snippetgroup-rootvars
  "Returns all logic variables of root node of all snippets in snippet group."
  [snippetgroup]
  (map snippet/snippet-var-for-root (snippetgroup-snippetlist snippetgroup)))


(defn
  snippetgroup-vars
  "Returns all logic variables from the given snippet group."
  [snippetgroup]
  (flat-map snippet/snippet-vars (snippetgroup-snippetlist snippetgroup)))

(defn
  snippetgroup-uservars
  "Returns all user logic variables from the given snippet group."
  [snippetgroup]
  (flat-map snippet/snippet-uservars (snippetgroup-snippetlist snippetgroup)))

(defn
  snippetgroup-uservars-for-information
  [snippetgroup]
  (flat-map snippet/snippet-uservars-for-information (snippetgroup-snippetlist snippetgroup)))

(defn
  snippetgroup-uservars-for-variable
  [snippetgroup]
  (flat-map snippet/snippet-uservars-for-variable (snippetgroup-snippetlist snippetgroup)))

(defn snippetgroup-snippet-for-node
  [group node]
  (defn find-snippet [listsnippet node]
    (cond 
      (= node nil) nil
      (empty? listsnippet) nil
      (contains? (:ast2var (first listsnippet)) node) (first listsnippet)
      :else (find-snippet (rest listsnippet) node)))
  (find-snippet (snippetgroup-snippetlist group) node))

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


;;TODO: remove all of these "related..."
(defn
  snippetgroup-related-snippets-basedon-userqueries
  [grp snippet]
  (let [related-snippets (map (fn [x] (snippetgroup-snippet-for-var grp x)) (snippet/snippet-userqueries-vars snippet))]
    (filter (fn [x] (and (not (nil? x)) (not (= x snippet)))) related-snippets)))        

(defn
  snippetgroup-related-snippets
  [grp snippet]
  (let [related-snippets (snippetgroup-snippetlist grp)]
    (remove #{snippet} related-snippets)))        

(defn
  snippetgroup-related-snippets-basedon-mandatory-and-userqueries
  [grp snippet]
  (let [query-related-snippets (snippetgroup-related-snippets-basedon-userqueries grp snippet)
        mandatory-related-snippets 
        (filter
          (fn [s] (.contains (snippetgroup-related-snippets-basedon-userqueries grp s) snippet))
          (snippetgroup-related-snippets grp snippet))]
    (distinct (concat query-related-snippets mandatory-related-snippets))))        

(defn
  snippetgroup-userfs
  [grp]
  "Returns all ast to user functions of the given grp."
  (flat-map snippet/snippet-userfs (:snippetlist grp)))

(defn flat-map
  "Returns list of results (= f(each-element)) in the form of flat list.
   Function f here return a list.
   flat-map similar with function map, but instead of return nested list, flat-map returns unnested list."
  [f lst]
  (if (empty? lst)
    '()
    (concat (f (first lst))
            (flat-map f (rest lst)))))

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




;;OTHER FUNCTIONS' NAME
;;---------------------------

(def document-as-template snippet/document-as-snippet)
(def template-root snippet/snippet-root)
(def make-templategroup make-snippetgroup)
