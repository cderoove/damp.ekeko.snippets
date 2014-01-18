(ns 
  ^{:doc "Runtime functions can be used by user."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets.public
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko [logic :as el]]
            [damp.ekeko.jdt
             [ast :as ast]])
  (:require [damp.ekeko.snippets
             [runtime :as runtime]
             [rewrite :as rewrite]]))


; ---------------------------------
; RUNTIME PREDICATES QUERY DRIVEN
; ---------------------------------

(defn
  var-type
   "Relation between ASTNode variable with it's type."
  [?var ?type]
  (cl/fresh [?qname ?var-type]
            (runtime/ast-variable-type ?var ?var-type)
            (runtime/ast-type-qualifiednamestring ?var-type ?qname)
            (runtime/ast-type-qualifiednamestring ?type ?qname)))

(defn
  method-dec
   "Relation between ASTNode invocation with it's declaration."
  [?inv ?dec]
  (runtime/ast-invocation-declaration ?inv ?dec))

(defn
  var-dec
   "Relation between ASTNode variable with it's declaration."
  [?var ?dec]
  (runtime/ast-variable-declaration ?var ?dec))

(defn
  var-binding
   "Relation between ASTNode var1 and var2 with the same resolveBinding."
  [?var1 ?var2]
  (runtime/ast-variable-samebinding ?var1 ?var2))

(defn
  enclosing-class
   "Relation between ASTNode var with its enclosing class."
  [?var ?class]
  (cl/all
    (ast/ast :TypeDeclaration ?class)    
    (ast/child+ ?class ?var)))

(defn
  enclosing-method
   "Relation between ASTNode var with its enclosing method."
  [?var ?method]
  (cl/all
    (ast/ast :MethodDeclaration ?method)    
    (ast/child+ ?method ?var)))


; ---------------------------------
; REWRITE FUNCTIONS
; ---------------------------------

(defn 
  add-node-before
  "Add string as a new node with position before the given node"
  [node string]
  (let [parent (.getParent node)
        property (.getLocationInParent node)
        idx (.indexOf (.getStructuralProperty parent property) node)]
    (rewrite/add-node-in-rewrite-code parent (keyword (.getId property)) string idx)))

(defn 
  add-node-after
  "Add string as a new node with position after the given node"
  [node string]
  (let [parent (.getParent node)
        property (.getLocationInParent node)
        idx (.indexOf (.getStructuralProperty parent property) node)]
    (rewrite/add-node-in-rewrite-code parent (keyword (.getId property)) string (+ idx 1))))

(defn 
  add-member-node
  "Add string as a new node as member of the given nodelist."
  [nodelist string]
  (let [parent (:owner nodelist)
        property (:property nodelist)]
    (rewrite/add-node-in-rewrite-code parent (keyword (.getId property)) string 0)))

(defn 
  replace-node
  "Replace node with string as a new node."
  [node string]
  (rewrite/replace-node-in-rewrite-code node string))

(defn 
  add-import-node
  "Add string as an import declaration node in enclosing cu of given node."
  [node string]
  (let [cu (.getRoot node)]
    (rewrite/add-node-in-rewrite-code cu :imports string 0)))
