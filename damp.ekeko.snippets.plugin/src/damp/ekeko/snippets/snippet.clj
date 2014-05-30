(ns 
  ^{:doc "Core functionality related to the Snippet datatype used in snippet-driven queries."
    :author "Coen De Roover, Siltvani"}
damp.ekeko.snippets.snippet
  (:require [damp.ekeko.snippets 
             [util :as util]])
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
  [ast ast2var ast2bounddirectives var2ast userquery 
   ;document rewrite track2ast ast2track
   ]
  clojure.core.logic.protocols/IUninitialized ;otherwise cannot be bound to logic var
  (-uninitialized [_]
    (Snippet. 
      nil nil nil nil nil)))
     


(defn
  make-snippet
  "For internal use only. 
   Consider matching/snippet-from-string or matching/snippet-from-node instead."
  [node]
  (damp.ekeko.snippets.snippet.Snippet. node {} {} {} '()))
  

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
  snippet-node-for-var 
  "For the logic variable of the given snippet, returns the AST node  
   that is bound to a matching logic variable."
  [snippet snippet-var]
  (get-in snippet [:var2ast snippet-var]))


(defn
  snippet-bounddirectives-for-node
  "For the given AST node of the given snippet, returns the seq of bound directives used to generate conditions to find a match for the node."
  [snippet snippet-node]
  (get-in snippet [:ast2bounddirectives snippet-node]))

(defn
  snippet-bounddirectives
  "Returns all bounddirectives for all nodes of the snippet."
  [snippet]
  (vals (:ast2bounddirectives snippet)))

(defn 
  snippet-nodes
  "Returns all AST nodes of the given snippet."
  [snippet]
  (keys (:ast2var snippet)))

(defn 
  snippet-list-containing
  "Returns value in snippet (= wrapper of NodeList) of which the NodeList contains member mbr."
  [snippet mbr]
  (let [ownerproperty (astnode/owner-property mbr)]
    (some (fn [value] 
            (when 
              (and 
                (astnode/lstvalue? value)
                (= ownerproperty (astnode/owner-property value))
                (some #{mbr} (:value value)))
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
  ;;note that using astnode/node-property-values would create new wrappersfor primitive values.
  ;;which the JFace treeviewer might not like
  (filter
    (fn [value] 
      (= (astnode/owner value) node))
    (snippet-nodes snippet)))


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
  (snippet-uservars snippet))


(defn 
  snippet-userquery
  "Returns the logic conditions defined by users of the given snippet."
  [snippet]
  (let [query (:userquery snippet)]
        (if (nil? query)
          '()
          query)))

(defn
  snippet-userquery-vars
  [snippet]
  (reduce 
    (fn [list el] (concat list (rest el))) 
    (rest (first (snippet-userquery snippet)))  
    (rest (snippet-userquery snippet))))  


(defn
  snippet-corresponding-node
  "Returns the node corresponding to the given one in the snippet. 
   Correspondance is determined solely using the position and type of the node."
  [snippet node]
  )
  

;(defn 
; snippet-document
; "Returns the document of source code of the given snippet."
; [snippet]
; (:document snippet))

;(defn 
;  snippet-rewrite
;  "Returns the ASTRewrite from the root of snippet."
;  [snippet]
;  (:rewrite snippet))

;(defn 
;  snippet-node-for-track 
;  "For the node track in document of the given snippet, returns the AST node."
;  [snippet track]
;  (get-in snippet [:track2ast track]))

;(defn 
;  snippet-track-for-node 
;  "Returns node track in document of the given snippet for the given AST node."
;  [snippet node]
;  (get-in snippet [:ast2track node]))



(defn
  snippet-property-for-node
  "Used together with track to identify node."
  [snippet ast]
  (if (or (instance? CompilationUnit ast) (= ast (:ast snippet)))
    (util/class-simplename (class ast))
    (astnode/property-descriptor-id (astnode/owner-property ast))))

(defn
  snippet-value 
  "Returns the value if there exists a match variable for it in the snippet, returns nil otherwise."
  [snippet val]
  (some #{val} (snippet-nodes snippet)))
  
(defn
  snippet-value-list?
  "Returns true for snippet values that are wrapped lists."
  [snippet val]
  (boolean 
    (if-let [value (snippet-value snippet val)]
      (astnode/lstvalue? value))))

(defn
  snippet-value-list-unwrapped
  "Returns the ASTNode$NodeList wrapped by this template value."
  [snippet val]
  (when
    (snippet-value-list? snippet val)
    (astnode/value-unwrapped val)))
  
(defn
  snippet-value-primitive?
  "Returns true for snippet values that are wrapped primitives."
  [snippet val]
  (boolean 
    (if-let [value (snippet-value snippet val)]
      (astnode/primitivevalue? value))))

(defn
  snippet-value-primitive-unwrapped
  "Returns the primitive wrapped by this template value."
  [snippet val]
  (when
    (snippet-value-primitive? snippet val)
    (astnode/value-unwrapped val)))

(defn
  snippet-value-node?
  "Returns true for snippet values that are AST nodes."
  [snippet val]
  (boolean 
    (if-let [value (snippet-value snippet val)]
      (astnode/ast? value))))

(defn
  snippet-value-node-unwrapped
  "Returns the node 'wrapped' by this template value."
  [snippet val]
  (when
    (snippet-value-node? snippet val)
    val))


(defn
  snippet-value-null?
  "Returns true for snippet values that are wrapped nulls."  
  [snippet val]
  (boolean 
    (if-let [value (snippet-value snippet val)]
      (astnode/nilvalue? value))))


;; Manipulating snippets
;; ---------------------

(defn
  copy-jdt-node
  [^ASTNode node]
  (.copySubtree (.getAST node) node))

(defn
  update-bounddirectives
  [snippet node bounddirectives]
  (update-in snippet
             [:ast2bounddirectives node]
             (fn [oldbounddirectives] 
               bounddirectives)))
  
(defn
  add-bounddirective
  [snippet node bounddirective]
  (update-in snippet
             [:ast2bounddirectives node]
             (fn [oldbounddirectives] (conj oldbounddirectives bounddirective))))

(defn
  remove-bounddirective
  [snippet node bounddirective]
  (update-in snippet
             [:ast2bounddirectives node]
             (fn [oldbounddirectives] (remove #{bounddirective} oldbounddirectives))))


(defn
  update-uservar
  "Adds a mapping from the match for the given snippet node to a user-defined variable."
  [snippet node uservar]
  (assoc-in snippet
            [:var2uservar (snippet-var-for-node snippet node)]
            (symbol uservar)))
                             

(defn
  register-callbacks
  []
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPET_ROOT) snippet-root)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPET_USERQUERY) snippet-userquery)

  (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_LIST_CONTAINING) snippet-list-containing)
  (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_ELEMENT_ISLIST) snippet-value-list?)
  (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_ELEMENT_LIST) snippet-value-list-unwrapped)
  (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_ELEMENT_ISVALUE)  snippet-value-primitive?)
  (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_ELEMENT_VALUE)  snippet-value-primitive-unwrapped)
  (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_ELEMENT_ISNODE) snippet-value-node?)
  (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_ELEMENT_NODE) snippet-value-node-unwrapped)
  (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_ELEMENT_ISNULL) snippet-value-null?)
  
  (set! (damp.ekeko.snippets.gui.BoundDirectivesViewer/FN_BOUNDDIRECTIVES_FOR_NODE) snippet-bounddirectives-for-node)
  
    

  )

(register-callbacks)

