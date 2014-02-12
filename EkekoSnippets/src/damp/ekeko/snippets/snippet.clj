(ns 
  ^{:doc "Core functionality related to the Snippet datatype used in snippet-driven queries."
    :author "Coen De Roover, Siltvani"}
damp.ekeko.snippets.snippet
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


(defrecord 
  Snippet
  [ast ast2var ast2groundf ast2constrainf ast2userfs var2ast var2uservar 
   userquery document rewrite track2ast ast2track])


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
  snippet-nodes
  "Returns all AST nodes of the given snippet."
  [snippet]
  (keys (:ast2var snippet)))


(defn 
  snippet-list-containing
  "Returns value in snippet (= wrapper of NodeList) of which the NodeList contains member mbr. Should only be used by pretty printer."
  [snippet mbr]
  (let [ownerproperty (astnode/owner-property mbr)]
    (some (fn [value] 
            (when 
              (and 
                (astnode/lstvalue? value)
                (= ownerproperty (astnode/owner-property value)))
              value)) 
          (snippet-nodes snippet))))
    
(defn 
  snippet-node-with-value
  "Returns node (= wrapper of NodeList) which has :value = value.
  value at least should have one member."
  [snippet value]
  (snippet-list-containing snippet (first value)))


(defn
  snippet-node-owner
  "Returns representation in snippet for owner of given node."
  [snippet node]
  (let [owner (astnode/owner node)]
    ;finds value equal to, but not identitical to owner .. should not make a difference in practice (see note in make-value, and see jdt-node-as-snippet)
    (some #{owner} (snippet-nodes snippet)))) 

(defn
  snippet-node-children
  "Returns representations in snippet for children of given node."
  [snippet node]
  ;;finds all values in snippet whose owner is equal to the given node.
  (filter
    (fn [value] 
      (= (astnode/owner value) node))
    (snippet-nodes snippet)))

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
  "Used together with track to identify node."
  [snippet ast]
  (if (or (instance? CompilationUnit ast) (= ast (:ast snippet)))
    (util/class-simplename (class ast))
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
   strategies (i.e., grounding=:exact, constaining=:exact)
   for the values of its properties.
   note: Only used to test operators related binding."
  [n]
  (defn assoc-snippet-value [snippet value]
    (let [lvar (util/gen-readable-lvar-for-value value)]
      (->
        snippet
        (assoc-in [:ast2var value] lvar)
        (assoc-in [:ast2groundf value] (list :exact))
        (assoc-in [:ast2constrainf value] (list :exact))
        (assoc-in [:var2ast lvar] value))))
  (let [snippet (atom (Snippet. n {} {} {} {} {} {} '() nil nil {} {}))]
    (util/walk-jdt-node 
      n
      (fn [astval] (swap! snippet assoc-snippet-value astval))
      (fn [lstval] 
        (swap! snippet assoc-snippet-value lstval)
        ;;TODO: should not be necessary, also not doing this for null-valued and primitive-valued properties
        ;;(let [rawlst (:value lstval)
        ;;     rawlstvar (util/gen-readable-lvar-for-value rawlst)]
        ;;  (swap! snippet assoc-in [:ast2var rawlst] rawlstvar))
        
        )
      (fn [primval]  (swap! snippet assoc-snippet-value primval))
      (fn [nilval] (swap! snippet assoc-snippet-value nilval)))
    @snippet))
  

(defn 
  document-as-snippet
  "Parse Document doc as a snippet with default matching strategies 
   (i.e., grounding=:exact, constaining=:exact)
   for the values of its properties.
   Function ASTRewrite/track is called for each ASTNode to activate the Node Tracking in ASTRewrite." 
  [doc]
  (defn assoc-snippet-value [snippet value track]
    (let [lvar (util/gen-readable-lvar-for-value value)
          arrTrack [(util/class-simplename (class value))
                    (snippet-property-for-node snippet value) 
                    (.getStartPosition track) 
                    (.getLength track)]]
      (->
        snippet
        (assoc-in [:ast2var value] lvar)
        (assoc-in [:ast2groundf value] (list :exact))
        (assoc-in [:ast2constrainf value] (list :exact))
        (assoc-in [:var2ast lvar] value)
        (assoc-in [:track2ast arrTrack] value)
        (assoc-in [:ast2track value] arrTrack))))
  (let [n (parsing/parse-document doc)
        rw (make-astrewrite n)
        snippet (atom (Snippet. n {} {} {} {} {} {} '() doc rw {} {}))]
    (util/walk-jdt-node 
      n
      (fn [astval] 
        (swap! snippet assoc-snippet-value astval (.track rw astval)))
      (fn [lstval] 
        (swap! snippet assoc-snippet-value lstval (.track rw (:owner lstval))))

       ; (let [rawlst (:value lstval)
       ;       rawlstvar (util/gen-readable-lvar-for-value rawlst)]
        ;  (swap! snippet assoc-in [:ast2var rawlst] rawlstvar)))
      (fn [primval]  (swap! snippet assoc-snippet-value primval (.track rw (:owner primval))))
      (fn [nilval] (swap! snippet assoc-snippet-value nilval (.track rw (:owner nilval)))))
    @snippet))
  

;; Updating Snippet instances
;;-----------------------------

(defn 
  update-gf-for-node
  "Update grounding function for node and all child+ of a given node in snippet."
  [snippet node gf]
  (defn update-snippet-value [snippet value]
    (update-in snippet [:ast2groundf value] (fn [x] (list gf))))
  (let [snippet (atom snippet)]
    (util/walk-jdt-node 
      node
      (fn [astval] (swap! snippet update-snippet-value astval))
      (fn [lstval] (swap! snippet update-snippet-value lstval))
      (fn [primval]  (swap! snippet update-snippet-value primval))
      (fn [nilval] (swap! snippet update-snippet-value nilval)))
    @snippet))

(defn 
  update-cf-for-node
  "Update constraining function for node and all child+ of a given node in snippet."
  [snippet node cf]
  (defn update-snippet-value [snippet value]
    (if (or (= (snippet-constrainer-for-node snippet value) :variable)
            (= (snippet-constrainer-for-node snippet value) :variable-info))
      snippet
      (update-in snippet [:ast2constrainf value] (fn [x] (list cf)))))
  (let [snippet (atom snippet)]
    (util/walk-jdt-node 
      node
      (fn [astval] (swap! snippet update-snippet-value astval))
      (fn [lstval] (swap! snippet update-snippet-value lstval))
      (fn [primval]  (swap! snippet update-snippet-value primval))
      (fn [nilval] (swap! snippet update-snippet-value nilval)))
    @snippet))

(defn 
  remove-gf-for-node
  "Clear grounding function for node and all child of a given node in snippet."
  [snippet node]
  (update-gf-for-node snippet node :epsilon))

(defn 
  remove-cf-for-node
  "Clear constarining function for node and all child of a given node in snippet."
  [snippet node]
  (update-cf-for-node snippet node :epsilon))

(defn 
  remove-gf-cf-for-node
  "Clear grounding and constraining function for node and all child of a given node in snippet."
  [snippet node]
  (update-gf-for-node 
    (update-cf-for-node snippet node :epsilon)
    node :epsilon))

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
    (let [arrTrack [(util/class-simplename (class value))
                    (snippet-property-for-node oldsnippet value) 
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


(defn
  register-callbacks
  []
  (set! (damp.ekeko.snippets.data.SnippetGroupHistory/FN_SNIPPET_ROOT) snippet-root)
  (set! (damp.ekeko.snippets.data.SnippetGroupHistory/FN_SNIPPET_USERQUERIES) snippet-userqueries)
  (set! (damp.ekeko.snippets.data.SnippetGroupHistory/FN_SNIPPET_FROMDOCUMENT) document-as-snippet)

  (set! (damp.ekeko.snippets.gui.viewer.SnippetPrettyPrinter/FN_SNIPPET_VAR_FOR_NODE) snippet-var-for-node)
  (set! (damp.ekeko.snippets.gui.viewer.SnippetPrettyPrinter/FN_SNIPPET_USERVAR_FOR_NODE) snippet-uservar-for-node)
  (set! (damp.ekeko.snippets.gui.viewer.SnippetPrettyPrinter/FN_SNIPPET_GROUNDER_FOR_NODE) snippet-grounder-for-node)
  (set! (damp.ekeko.snippets.gui.viewer.SnippetPrettyPrinter/FN_SNIPPET_CONSTRAINER_FOR_NODE) snippet-constrainer-for-node)
  (set! (damp.ekeko.snippets.gui.viewer.SnippetPrettyPrinter/FN_SNIPPET_USERFS_FOR_NODE) snippet-userfs-for-node)
  (set! (damp.ekeko.snippets.gui.viewer.SnippetPrettyPrinter/FN_SNIPPET_GROUNDERWITHARGS_FOR_NODE) snippet-grounder-with-args-for-node)
  (set! (damp.ekeko.snippets.gui.viewer.SnippetPrettyPrinter/FN_SNIPPET_CONSTRAINERWITHARGS_FOR_NODE) snippet-constrainer-with-args-for-node)
  (set! (damp.ekeko.snippets.gui.viewer.SnippetPrettyPrinter/FN_SNIPPET_LIST_CONTAINING) snippet-list-containing)


  )

(register-callbacks)








