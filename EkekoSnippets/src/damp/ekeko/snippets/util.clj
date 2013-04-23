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

(defn dissoc-in [tmap keys]
  (let [newmap (dissoc ((first keys) tmap) (fnext keys))]
    (update-in tmap [(first keys)] (fn [x] newmap))))

(defn
  change-string
  "Change a string with specific format to another string.
   example : \"add + [getName 3 7] + s\" -> \"addNames\"."
  [string]
  (let [arr-str (seq (.split (.replace string "+" ";") ";"))
        arr-size (.size arr-str)
        name-idx (if (.contains (.get arr-str 0) "[") 0 1)
        prefix (if (= name-idx 0) "" (.trim (.get arr-str 0)))
        suffix (if (= name-idx (- arr-size 1)) "" (.trim (.get arr-str (- arr-size 1))))
        arr-name (seq (.split (.replace (.replace (.trim (.get arr-str name-idx)) "[" "") "]" "") " "))
        name (.substring 
               (.get arr-name 0) 
               (java.lang.Integer/valueOf (.get arr-name 1))
               (java.lang.Integer/valueOf (.get arr-name 2)))]
    (str prefix name suffix)))
