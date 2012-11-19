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
      (astnode/lstvalue? value)
      "ListVal"
      (astnode/primitivevalue? value)
      "PrimVal"
      :else
      (class-simplename (class (:value value))))))

(defn 
  walk-jdt-node
  "Recursive descent through a JDT node, applying given functions to the encountered 
   ASTNode instances and Ekeko wrappers for their property values."
  [ast node-f list-f primitive-f]
  (defn walk-jdt-nodes [nodes]
    (when-not (empty? nodes)
      (let [val (first nodes)
            others (rest nodes)]
        (cond 
          (astnode/ast? val)
          (do
            (node-f val)
            (recur (concat (astnode/node-propertyvalues ast) others)))
          (astnode/lstvalue? ast)
          (do 
            (list-f ast)
            (recur (concat (:value ast) others)))
          :else 
          (do 
            (primitive-f ast)
            (recur others))))))
  (walk-jdt-nodes (list ast)))
