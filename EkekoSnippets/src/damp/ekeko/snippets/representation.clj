(ns
  ^{:doc "Core functionality related to the Snippet datatype used in snippet-driven queries."
    :author "Coen De Roover, Siltvani"}
   damp.ekeko.snippets.representation
  (:require [damp.ekeko.snippets 
             [util :as util]
             [parsing :as parsing]])
  (:require [damp.ekeko.jdt 
             [astnode :as astnode]])
  (:import [org.eclipse.jdt.core.dom.rewrite ASTRewrite])
  (:import [org.eclipse.jdt.core.dom ASTNode ASTNode$NodeList CompilationUnit]))


;; Snippet Datatype
;; ----------------

; Datatype representing a code snippet that can be matched against Java projects.

; For each AST node of the code snippet, a matching AST node has to be found in the Java project.
; To this end, an Ekeko query is generated that can be launched against the project. 
; The logic variables in the query correspond to invididual AST nodes of the snippet. 
; They will be bound to matching AST nodes in the project. 
; The actual conditions that are generated for the query depend on how each AST node of the snippet
; is to be matched (e.g., more lenient, or rather strict).

; members of the Snippet datatype:
; - ast: complete AST for the original code snippet
; - ast2var: map from an AST node of the snippet to the logic variable that is to be bound to its match
; - ast2groundf: map from an AST node of the snippet to a function that generates "grounding" conditions. 
;   These will ground the corresponding logic variable to an AST node of the Java project
;   format of the function is list (:type argument1 argument2 ..)
; - ast2constrainf:  map from an AST node of the snippet to a function that generates "constraining" conditions. 
;   These will constrain the bindings for the corresponding logic variable to an AST node of the Java project
;   that actually matches the AST node of the snippet. 
;   format of the function is list (:type argument1 argument2 ..)
; - ast2userfs:  map from an AST node of the snippet to a user function. 
;   format of the function is list of list ((fuction1 argument) (fuction2 argument) ..)
;   generates query in group
; - var2ast: map from a logic variable to the an AST node which is bound to its match
; - var2uservar: map from logic variable to the user defined logic variable 
; - userquery: user defined logic conditions 
;   format in quote '((...)(....))
; - document: java Document source code
; - rewrite: ASTRewrite
;   note: to use the Track, we should call the function track for each node before any modification of ASTRewrite
; - track2ast: map from track (in original document) to node 
;   {[property, start, length] ast}
; - flag: :mandatory or :optional
;   flag is used in the group to generate query for related mandatory snippets

(defrecord 
  Snippet
  [ast ast2var ast2groundf ast2constrainf ast2userfs var2ast var2uservar 
   userquery document rewrite track2ast ast2track flag])

(declare flat-map)

(defn 
  snippet-root 
  [snippet]
  (:ast snippet))

(defn 
  snippet-var-for-node 
  "For the given AST node of the given snippet, returns the name of the logic
   variable that will be bound to a matching AST node from the Java project."
  [snippet snippet-node]
  (get-in snippet [:ast2var snippet-node]))

(defn
  snippet-var-for-root
  "Returns the logic variable associated with the root of the snippet
   (i.e., the JDT node the snippet originated from)."
  [snippet]
  (snippet-var-for-node snippet (:ast snippet)))

(defn 
  snippet-grounder-with-args-for-node
  [snippet template-ast]
  (get-in snippet [:ast2groundf template-ast]))

(defn 
  snippet-constrainer-with-args-for-node
  [snippet template-ast]
  (get-in snippet [:ast2constrainf template-ast]))

(defn 
  snippet-grounder-for-node
  "For the given AST node of the given snippet, returns the function type
   that will generate grounding conditions for the corresponding logic variable."
  [snippet template-ast]
  (first (get-in snippet [:ast2groundf template-ast])))

(defn 
  snippet-constrainer-for-node
  "For the given AST node of the given snippet, returns the function type
   that will generate constraining conditions for the corresponding logic variable."
  [snippet template-ast]
  (first (get-in snippet [:ast2constrainf template-ast])))

(defn 
  snippet-grounder-args-for-node
  "For the given AST node of the given snippet, returns the list of function arguments
   that will generate grounding conditions for the corresponding logic variable."
  [snippet template-ast]
  (rest (get-in snippet [:ast2groundf template-ast])))

(defn 
  snippet-constrainer-args-for-node
  "For the given AST node of the given snippet, returns the function arguments
   that will generate constraining conditions for the corresponding logic variable."
  [snippet template-ast]
  (rest (get-in snippet [:ast2constrainf template-ast])))

(defn 
  snippet-userfs-for-node
  "For the given AST node of the given snippet, returns list of user functions
   with format ((function1 arg) (function2 arg) ..)."
  [snippet template-ast]
  (let [userfs (get-in snippet [:ast2userfs template-ast])]
    (if (nil? userfs)
      '()
      userfs)))

(defn 
  snippet-node-for-var 
  "For the logic variable of the given snippet, returns the AST node  
   that is bound to a matching logic variable."
  [snippet snippet-var]
  (get-in snippet [:var2ast snippet-var]))

(defn 
  snippet-node-for-uservar 
  "For the user logic variable of the given snippet, returns the AST node  
   that is bound to a matching logic variable."
  [snippet user-var]
  (let [found-map (filter (fn [x] (= (val x) user-var)) (:var2uservar snippet))]
    (snippet-node-for-var snippet (key (first found-map))))) 

(defn 
  snippet-uservar-for-var 
  "For the given logic var of the given snippet, returns the name of the user defined logic variable."
  [snippet snippet-var]
  (get-in snippet [:var2uservar snippet-var]))

(defn 
  snippet-uservar-for-node 
  "For the given AST node of the given snippet, returns the name of the user logic
   variable that will be bound to a matching AST node from the Java project."
  [snippet template-ast]
  (snippet-uservar-for-var snippet (snippet-var-for-node snippet template-ast)))

(defn 
  snippet-lvar-for-node 
  "For the given AST node of the given snippet, returns the name of the user logic
   variable (if exist) or default logic variable that will be bound to a matching AST node from the Java project."
  [snippet snippet-var]
  (let [uservar (snippet-uservar-for-node snippet snippet-var)]
    (if (nil? uservar)
      (snippet-var-for-node snippet snippet-var)
      uservar)))

(defn 
  snippet-node-for-lvar 
  "For the given var or user var of the given snippet, returns ASTNode bound to it."
  [snippet snippet-var]
  (let [node (snippet-node-for-var snippet snippet-var)]
    (if (nil? node)
      (snippet-node-for-uservar snippet snippet-var)
      node)))

(defn 
  snippet-node-with-member
  "Returns node (= wrapper of NodeList) which it's :value (= NodeList) has member mbr."
  [snippet mbr]
  (let [parent (.getParent mbr)
        property (.getLocationInParent mbr)
        value (.getStructuralProperty parent property)]    
    (astnode/make-value parent property value)))

(defn 
  snippet-node-with-value
  "Returns node (= wrapper of NodeList) which has :value = value.
  value at least should have one member."
  [snippet value]
  (snippet-node-with-member snippet (first value)))

(defn
  snippet-value-for-node
  "Return :value of the given node (= wrapper of NodeList)."
  [snippet node]
  (:value node))

(defn 
  snippet-nodes
  "Returns all AST nodes of the given snippet."
  [snippet]
  (keys (:ast2var snippet)))

(defn
  snippet-userfs
  "Returns all ast to user functions of the given snippet."
  [snippet]
  (:ast2userfs snippet))

(defn 
  snippet-vars
  "Returns the logic variables that correspond to the AST nodes
   of the given snippet. These variables will be bound to matching
   AST nodes from the queried Java project."
  [snippet]
  (vals (:ast2var snippet)))

(defn 
  snippet-uservars
  "Returns the logic variables defined by users of the given snippet."
  [snippet]
  (vals (:var2uservar snippet)))

(defn 
  snippet-uservars-for-information
  "Returns the logic variables of node with matching strategy :exact-variable or :variable-info of the given snippet."
  [snippet]
  (vals
    (filter 
      (fn [x] 
        (let [cf (snippet-constrainer-for-node 
                   snippet
                   (snippet-node-for-var snippet (key x)))]
          (or (= cf :exact-variable)
              (= cf :variable-info))))
      (:var2uservar snippet))))

(defn 
  snippet-uservars-for-variable
  "Returns the logic variables of node with matching strategy != :exact-variable of the given snippet."
  [snippet]
  (vals
    (filter 
      (fn [x] 
        (let [cf (snippet-constrainer-for-node 
                   snippet
                   (snippet-node-for-var snippet (key x)))]
          (and (not (= cf :exact-variable))
               (not (= cf :variable-info)))))
      (:var2uservar snippet))))

(defn 
  snippet-userqueries
  "Returns the logic conditions defined by users of the given snippet."
  [snippet]
  (let [query (:userquery snippet)]
        (if (nil? query)
          '()
          query)))

(defn
  snippet-userqueries-vars
  [snippet]
  (reduce 
    (fn [list el] (concat list (rest el))) 
    (rest (first (snippet-userqueries snippet)))  
    (rest (snippet-userqueries snippet))))  
  

(defn 
  snippet-document
  "Returns the document of source code of the given snippet."
  [snippet]
  (:document snippet))

(defn 
  snippet-is-mandatory?
  "Returns true if snippet flag = :mandatory."
  [snippet]
  (= (:flag snippet) :mandatory))

(defn 
  snippet-update-flag
  "Update flag :mandatory or :optional."
  [snippet flag]
  (assoc snippet :flag flag))

(defn 
  snippet-switch-flag
  "Update flag :mandatory or :optional."
  [snippet]
  (if (snippet-is-mandatory? snippet)
    (snippet-update-flag snippet :optional)
    (snippet-update-flag snippet :mandatory)))

(defn 
  snippet-rewrite
  "Returns the ASTRewrite from the root of snippet."
  [snippet]
  (:rewrite snippet))

(defn 
  snippet-node-for-track 
  "For the node track in document of the given snippet, returns the AST node."
  [snippet track]
  (get-in snippet [:track2ast track]))

(defn 
  snippet-track-for-node 
  "Returns node track in document of the given snippet for the given AST node."
  [snippet node]
  (get-in snippet [:ast2track node]))

(defn
  snippet-property-for-node
  [snippet ast]
  (if (instance? CompilationUnit ast)
    (clojure.core/type ast)
    (astnode/property-descriptor-id (astnode/owner-property ast))))

  

;; Constructing Snippet instances
;; ------------------------------

(defn 
  make-astrewrite
  [node]
  (ASTRewrite/create (.getAST node)))

(defn 
  jdt-node-as-snippet
  "Interpretes the given JDT ASTNode as a snippet with default matching 
   strategies (i.e., grounding=:minimalistic, constaining=:exact)
   for the values of its properties.
   note: Only used to test operators related binding."
  [n]
  (defn assoc-snippet-value [snippet value]
    (let [lvar (util/gen-readable-lvar-for-value value)]
      (->
        snippet
        (assoc-in [:ast2var value] lvar)
        (assoc-in [:ast2groundf value] (list :minimalistic))
        (assoc-in [:ast2constrainf value] (list :exact))
        (assoc-in [:var2ast lvar] value))))
  (let [snippet (atom (Snippet. n {} {} {} {} {} {} '() nil nil {} {} :mandatory))]
    (util/walk-jdt-node 
      n
      (fn [astval] (swap! snippet assoc-snippet-value astval))
      (fn [lstval] 
        (swap! snippet assoc-snippet-value lstval)
        (let [rawlst (:value lstval)
              rawlstvar (util/gen-readable-lvar-for-value rawlst)]
          (swap! snippet assoc-in [:ast2var rawlst] rawlstvar)))
      (fn [primval]  (swap! snippet assoc-snippet-value primval))
      (fn [nilval] (swap! snippet assoc-snippet-value nilval)))
    @snippet))
  

(defn 
  document-as-snippet
  "Parse Document doc as a snippet with default matching strategies (i.e., grounding=:minimalistic, constaining=:exact)
   for the values of its properties.
   Function ASTRewrite/track is called for each ASTNode to activate the Node Tracking in ASTRewrite." 
  [doc]
  (defn assoc-snippet-value [snippet value track]
    (let [lvar (util/gen-readable-lvar-for-value value)
          arrTrack [(snippet-property-for-node snippet value) 
                    (.getStartPosition track) 
                    (.getLength track)]]
      (->
        snippet
        (assoc-in [:ast2var value] lvar)
        (assoc-in [:ast2groundf value] (list :minimalistic))
        (assoc-in [:ast2constrainf value] (list :exact))
        (assoc-in [:var2ast lvar] value)
        (assoc-in [:track2ast arrTrack] value)
        (assoc-in [:ast2track value] arrTrack))))
  (let [n (parsing/parse-document doc)
        rw (make-astrewrite n)
        snippet (atom (Snippet. n {} {} {} {} {} {} '() doc rw {} {} :mandatory))]
    (util/walk-jdt-node 
      n
      (fn [astval] 
        (swap! snippet assoc-snippet-value astval (.track rw astval)))
      (fn [lstval] 
        (let [rawlst (:value lstval)
              rawlstvar (util/gen-readable-lvar-for-value rawlst)]
          (swap! snippet assoc-snippet-value lstval (.track rw (:owner lstval)))
          (swap! snippet assoc-in [:ast2var rawlst] rawlstvar)))
      (fn [primval]  (swap! snippet assoc-snippet-value primval (.track rw (:owner primval))))
      (fn [nilval] (swap! snippet assoc-snippet-value nilval (.track rw (:owner nilval)))))
    @snippet))
  

;; Updating Snippet instances
;;-----------------------------

(defn 
  update-gf-cf-for-node
  "Update grounding & constraining function for node and all child+ of a given node in snippet."
  [snippet node gf cf]
  (defn update-snippet-value [snippet value]
    (update-in snippet [:ast2groundf value] (fn [x] (list gf)))
    (update-in snippet [:ast2constrainf value] (fn [x] (list cf))))
  (let [snippet (atom snippet)]
    (util/walk-jdt-node 
      node
      (fn [astval] (swap! snippet update-snippet-value astval))
      (fn [lstval] (swap! snippet update-snippet-value lstval))
      (fn [primval]  (swap! snippet update-snippet-value primval))
      (fn [nilval] (swap! snippet update-snippet-value nilval)))
    @snippet))

(defn 
  remove-gf-cf-for-node
  "Clear grounding & constraining function for node and all child of a given node in snippet."
  [snippet node]
  (update-gf-cf-for-node snippet node :epsilon :epsilon))


;; Copying Snippet and Apply rewrite
;; ---------------------------------

(defn
  copy-snippet
  "Copy all informations in oldsnippet to newsnippet, comparing each node with NodeTrackPosition of ASTRewrite."
  [oldsnippet newsnippet]
  (defn update-userfs [snippet newast value]
    (let [userfs (get-in oldsnippet [:ast2userfs value])]
      (if (not (nil? userfs)) 
        (assoc-in snippet [:ast2userfs newast] userfs)
        snippet)))
  (defn update-newsnippet-value [snippet value track]
    (let [arrTrack [(snippet-property-for-node oldsnippet value) 
                    (.getStartPosition track) 
                    (.getLength track)]
          newast (snippet-node-for-track snippet arrTrack)] 
      (if (not (nil? newast))
        (->
          snippet
          (update-in [:ast2var newast] (fn [x] (snippet-var-for-node oldsnippet value)))
          (update-in [:ast2groundf newast] (fn [x] (get-in oldsnippet [:ast2groundf value])))
          (update-in [:ast2constrainf newast] (fn [x] (get-in oldsnippet [:ast2constrainf value])))
          (update-userfs newast value)
          (util/dissoc-in [:var2ast (snippet-var-for-node snippet newast)])      ;new variable replaced by old variable 
          (assoc-in  [:var2ast (snippet-var-for-node oldsnippet value)] newast))
        snippet)))
  (let [snippet (atom newsnippet)
        rw (:rewrite oldsnippet)]
    (util/walk-jdt-node 
      (:ast oldsnippet)
      (fn [astval]  (swap! snippet update-newsnippet-value astval (.track rw astval)))
      (fn [lstval]  (swap! snippet update-newsnippet-value lstval (.track rw (:owner lstval))))
      (fn [primval] (swap! snippet update-newsnippet-value primval (.track rw (:owner primval))))
      (fn [nilval]  (swap! snippet update-newsnippet-value nilval (.track rw (:owner nilval)))))
    (swap! snippet update-in [:var2uservar] (fn [x] (:var2uservar oldsnippet)))
    (swap! snippet update-in [:userquery] (fn [x] (:userquery oldsnippet)))
    (swap! snippet update-in [:flag] (fn [x] (:flag oldsnippet)))
    @snippet))

(defn 
  apply-rewrite 
  "Apply rewrite to snippet."
  [snippet]
  (let [rewrite (snippet-rewrite snippet)
        document (snippet-document snippet)]
    (.apply (.rewriteAST rewrite document nil) document)
    (let [newsnippet (document-as-snippet document)]
      (copy-snippet snippet newsnippet)))) 

(defn 
  snippet-new-state
  [snippet]
  (let [document (snippet-document snippet)]
    (let [new-document (parsing/parse-string-to-document (.get document))
          newsnippet (document-as-snippet new-document)]
      (copy-snippet snippet newsnippet)))) 

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
  snippetgroup-userqueries
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
  (flat-map snippet-userqueries (snippetgroup-snippetlist snippetgroup)))

(defn
  snippetgroup-rootvars
  "Returns all logic variables of root node of all snippets in snippet group."
  [snippetgroup]
  (map snippet-var-for-root (snippetgroup-snippetlist snippetgroup)))


(defn
  snippetgroup-vars
  "Returns all logic variables from the given snippet group."
  [snippetgroup]
  (flat-map snippet-vars (snippetgroup-snippetlist snippetgroup)))

(defn
  snippetgroup-uservars
  "Returns all user logic variables from the given snippet group."
  [snippetgroup]
  (flat-map snippet-uservars (snippetgroup-snippetlist snippetgroup)))

(defn
  snippetgroup-uservars-for-information
  [snippetgroup]
  (flat-map snippet-uservars-for-information (snippetgroup-snippetlist snippetgroup)))

(defn
  snippetgroup-uservars-for-variable
  [snippetgroup]
  (flat-map snippet-uservars-for-variable (snippetgroup-snippetlist snippetgroup)))

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
      (.contains (snippet-uservars (first listsnippet)) var) (first listsnippet)
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
  (snippet-var-for-node
    (snippetgroup-snippet-for-node grp node)
    node))

(defn 
  snippetgroup-node-for-var
  [grp var]
  (snippet-node-for-lvar
    (snippetgroup-snippet-for-var grp var)
    var))

(defn
  snippetgroup-related-snippets-basedon-userqueries
  [grp snippet]
  (let [related-snippets (map (fn [x] (snippetgroup-snippet-for-var grp x)) (snippet-userqueries-vars snippet))]
    (filter (fn [x] (and (not (nil? x)) (not (= x snippet)))) related-snippets)))        

(defn
  snippetgroup-related-snippets
  [grp snippet]
  (let [related-snippets (filter (fn [x] (snippet-is-mandatory? x)) (snippetgroup-snippetlist grp))]
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
  (flat-map (fn [s] (snippet-userfs s)) (:snippetlist grp)))

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
  (let [new-snippetlist (map (fn [snippet] (snippet-new-state snippet)) (:snippetlist grp))] 
    (update-in grp [:snippetlist] (fn [x] new-snippetlist))))
  
  
;; Constructing SnippetGroup instances
;; -----------------------------------

(defn 
  make-snippetgroup
  "Create SnippetGroup instance."
  [name]
  (SnippetGroup. name '() '()))



;; Snippets Group History Datatype
;; --------------------------------

; Datatype representing a group (list) of Original Snippet(s) and 
; history of applied operators in order
; original-snippetgroup --> original snippet group
; operators-history --> vector of [applied operator-id, var-node, args]
; operators-undo-history --> applied operator which are undo by user
;                            list of [applied operator-id, var-node, args]
;; use vector for history, because always add element as last element
;; and use list for undohistory, because always add element as first element

(defrecord SnippetGroupHistory [original-snippetgroup current-snippetgroup operators-history operators-undohistory])


(defn 
  snippetgrouphistory-original
  "Returns original snippet group of the given snippet group history."
  [snippetgrouphistory]
  (:original-snippetgroup snippetgrouphistory))

(defn 
  snippetgrouphistory-current
  "Returns current snippet group of the given snippet group history."
  [snippetgrouphistory]
  (:current-snippetgroup snippetgrouphistory))

(defn 
  snippetgrouphistory-history
  "Returns history of applied operators of the given snippet group history."
  [snippetgrouphistory]
  (:operators-history snippetgrouphistory))

(defn 
  snippetgrouphistory-undohistory
  "Returns history of undo applied operators of the given snippet group history."
  [snippetgrouphistory]
  (:operators-undohistory snippetgrouphistory))

(defn 
  snippetgrouphistory-var-for-node
  [grp node]
  (snippetgroup-var-for-node (:current-snippetgroup grp) node))

(defn 
  snippetgrouphistory-node-for-var
  [grp var]
  (snippetgroup-node-for-var (:current-snippetgroup grp) var))

(defn 
  history-operator
  "Returns operator id of the given history [op-id, var-node, args]."
  [history]
  (first history))

(defn 
  history-varnode
  "Returns var node of the given history [op-id, var-node, args]."
  [history]
  (fnext history))

(defn 
  history-args
  "Returns arguments of the given history [op-id, var-node, args]."
  [history]
  (last history))

;; Constructing SnippetGroupHistory instances
;; -------------------------------------------

(defn 
  make-snippetgrouphistory
  "Create SnippetGroupHistory instance and keeping the track of operators history."
  [name]
  (let [snippetgroup (make-snippetgroup name)]
    (SnippetGroupHistory. snippetgroup snippetgroup [] '())))

(defn 
  make-snippetgrouphistory-from-snippetgroup
  "Create SnippetGroupHistory instance from given group."
  [snippetgroup]
  (SnippetGroupHistory. snippetgroup snippetgroup [] '()))

(defn 
  reset-snippetgrouphistory
  "Reset SnippetGroupHistory instance, but keep the undo history."
  [grouphistory]
  (let [snippetgroup (:original-snippetgroup grouphistory)
        undohistory (:operators-undohistory grouphistory)]
    (SnippetGroupHistory. snippetgroup snippetgroup [] undohistory)))

(defn
  snippetgrouphistory-clean-history
  [grouphistory]
  (update-in 
    (update-in grouphistory [:operators-undohistory] (fn [x] '()))
    [:operators-history]
    (fn [x] [])))

(defn
  snippetgrouphistory-new-state
  [grp]
  (let [user-cond (:userquery (:current-snippetgroup grp))
        new-grp (update-in (snippetgroup-new-state (:current-snippetgroup grp)) [:userquery] (fn [x] user-cond))]
    (update-in
      (update-in 
        (snippetgrouphistory-clean-history grp) 
        [:original-snippetgroup] (fn [x] new-grp))
      [:current-snippetgroup] (fn [x] new-grp))))


  
;; Updating SnippetGroupHistory instances
;; -------------------------------------------

(defn snippetgrouphistory-update-original-group
  [grouphistory newgroup]
  (update-in grouphistory [:original-snippetgroup] (fn [x] newgroup)))

(defn snippetgrouphistory-update-group
  [grouphistory newgroup]
  (update-in grouphistory [:current-snippetgroup] (fn [x] newgroup)))

(defn snippetgrouphistory-add-history
  [grouphistory op-id node args]
  (let [newhistory (conj (snippetgrouphistory-history grouphistory) [op-id node args])]
    (update-in grouphistory [:operators-history] (fn [x] newhistory))))

(defn snippetgrouphistory-last-history
  [grouphistory]
  "Returns last applied operators." 
  (last (snippetgrouphistory-history grouphistory)))

(defn snippetgrouphistory-first-undohistory
  [grouphistory]
  "Returns first undo applied operators." 
  (first (snippetgrouphistory-undohistory grouphistory)))

(defn snippetgrouphistory-add-undohistory
  [grouphistory]
  "Add the last element of list history as first element of undo history 
   and returns new snippet group history." 
  (let [lasthistory (snippetgrouphistory-last-history grouphistory)
        newundohistory (cons lasthistory (snippetgrouphistory-undohistory grouphistory))]
    (update-in grouphistory [:operators-undohistory] (fn [x] newundohistory))))

(defn snippetgrouphistory-remove-undohistory
  [grouphistory]
  "Remove first undo applied operator from the list undo history,
   and returns new snippet group history." 
  (let [firstundo (snippetgrouphistory-first-undohistory grouphistory)
        newundo (rest (snippetgrouphistory-undohistory grouphistory))]
    (update-in grouphistory [:operators-undohistory] (fn [x] newundo))))

