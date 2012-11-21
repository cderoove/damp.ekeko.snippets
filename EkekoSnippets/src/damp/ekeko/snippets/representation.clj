(ns
  ^{:doc "Core functionality related to the Snippet datatype used in snippet-driven queries."
    :author "Coen De Roover, Siltvani"}
   damp.ekeko.snippets.representation
  (:require [damp.ekeko.snippets 
             [util :as util]]))


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
; - ast2constrainf:  map from an AST node of the snippet to a function that generates "constraining" conditions. 
;   These will constrain the bindings for the corresponding logic variable to an AST node of the Java project
;   that actually matches the AST node of the snippet. 
; - var2ast: map from a logic variable to the an AST node which is bound to its match
; - var2uservar: map from logic variable to the user defined logic variable 

(defrecord 
  Snippet
  [ast ast2var ast2groundf ast2constrainf var2ast var2uservar])

(defn 
  snippet-var-for-node 
  "For the given AST node of the given snippet, returns the name of the logic
   variable that will be bound to a matching AST node from the Java project."
  [snippet snippet-node]
  (get-in snippet [:ast2var snippet-node]))

(defn 
  snippet-grounder-for-node
  "For the given AST node of the given snippet, returns the function
   that will generate grounding conditions for the corresponding logic variable."
  [snippet template-ast]
  (get-in snippet [:ast2groundf template-ast]))

(defn 
  snippet-constrainer-for-node
  "For the given AST node of the given snippet, returns the function
   that will generate constraining conditions for the corresponding logic variable."
  [snippet template-ast]
  (get-in snippet [:ast2constrainf template-ast]))

(defn 
  snippet-node-for-var 
  "For the logic variable of the given snippet, returns the AST node  
   that is bound to a matching logic variable."
  [snippet snippet-var]
  (get-in snippet [:var2ast snippet-var]))

(defn 
  snippet-uservar-for-var 
  "For the givenlogis var of the given snippet, returns the name of the user defined logic variable."
  [snippet snippet-var]
  (get-in snippet [:var2uservar snippet-var]))

(defn 
  snippet-nodes
  "Returns all AST nodes of the given snippet."
  [snippet]
  (keys (:ast2var snippet)))

(defn 
  snippet-vars
  "Returns the logic variables that correspond to the AST nodes
   of the given snippet. These variables will be bound to matching
   AST nodes from the queried Java project."
  [snippet]
  (vals (:ast2var snippet)))

;; Constructing Snippet instances
;; ------------------------------


(defn 
  jdt-node-as-snippet
  "Interpretes the given JDT ASTNode as a snippet with default matching 
   strategies (i.e., grounding=:minimalistic, constaining=:exact)
   for the values of its properties."
  [n]
  (defn assoc-snippet-value [snippet value]
    (let [lvar (util/gen-readable-lvar-for-value value)]
      (->
        snippet
        (assoc-in [:ast2var value] lvar)
        (assoc-in [:ast2groundf value] :minimalistic)
        (assoc-in [:ast2constrainf value] :exact)
        (assoc-in [:var2ast lvar] value))))
  (let [snippet (atom (Snippet. n {} {} {} {} {}))]
    (util/walk-jdt-node 
      n
      (fn [astval] (swap! snippet assoc-snippet-value astval))
      (fn [lstval] (swap! snippet assoc-snippet-value lstval))
      (fn [primval]  (swap! snippet assoc-snippet-value primval))
      (fn [nilval] (swap! snippet assoc-snippet-value nilval)))
    @snippet))
  
  
  

