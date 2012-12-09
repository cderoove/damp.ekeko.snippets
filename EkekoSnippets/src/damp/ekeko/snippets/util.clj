(ns 
  ^{:doc "Auxiliary functions for snippet-driven querying."
    :author "Coen De Roover, Siltvani."}
  damp.ekeko.snippets.util
  (:require [damp.ekeko.jdt [astnode :as astnode]]))

(defn
  class-simplename
  "Returns the unqualified name (a String) of the given java.lang.Class."
  [clazz]
  (last (.split #"\." (.getName clazz))))

(defn 
  gen-lvar
  "Generates a unique symbol starting with ?v
   (i.e., a symbol to be used as the name for a logic variable)."
  ([] (gen-lvar "?v"))
  ([prefix] (gensym (str "?" prefix))))

(defn
  gen-readable-lvar-for-value
  "Generates a unique logic variable, of which the name 
   gives a hint about the class of the given JDT property value."
  [value]
  (gen-lvar
    (cond
      (nil? value)
      "NullShouldNotBeHere"
      (astnode/lstvalue? value)
      "ListVal"
      (astnode/primitivevalue? value)
      "PrimVal"
      (astnode/nilvalue? value)
      "NilVal"
      (astnode/ast? value)
      (class-simplename (class value)))))

(defn 
  walk-jdt-node
  "Recursive descent through a JDT node, applying given functions to the encountered 
   ASTNode instances and Ekeko wrappers for their property values."
  [n node-f list-f primitive-f null-f]
  (loop
    [nodes (list n)]
    (when-not (empty? nodes)
      (let [val (first nodes)
            others (rest nodes)]
        (cond 
          (astnode/ast? val)
          (do
            (node-f val)
            (recur (concat (astnode/node-propertyvalues val) others)))
          (astnode/lstvalue? val)
          (do 
            (list-f val)
            (recur (concat (:value val) others)))
          (astnode/primitivevalue? val)
          (do
            (primitive-f val)
            (recur others))
          (astnode/nilvalue? val)
          (do
            (null-f val)
            (recur others)))))))

(defn 
  create-listrewrite
  "Return new ListRewrite for property of :owner of the list (e.g. owner : block, property : statements).
   Note : only using NodeList.add, not able to rewrite the list."
  [lst]
  (let [owner (:owner lst)
        property (:property lst)
        ast-rewrite (org.eclipse.jdt.core.dom.rewrite.ASTRewrite/create (.getAST owner))]
    (.getListRewrite ast-rewrite owner property)))

(defn
  add-node-to-listrewrite
  "Add node in given index idx to the list-rewrite."
  [list-rewrite node idx]
  (.insertAt list-rewrite node idx (new org.eclipse.text.edits.TextEditGroup "snippet")))

(defn
  remove-node-from-listrewrite
  "Remove node from the list-rewrite."
  [list-rewrite node]
  (.remove list-rewrite node (new org.eclipse.text.edits.TextEditGroup "snippet")))

(defn
  rewritten-list-from-listrewrite
  [list-rewrite]
  (.getRewrittenList list-rewrite))
