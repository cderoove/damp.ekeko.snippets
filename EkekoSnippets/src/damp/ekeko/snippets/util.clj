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
  convert-string-to-rule
  "Example: \"add[part-of-name]s\" will be converted to \"add + [?lvar start-idx remaining-chars-no] + s\"."
  [string name lvar]
  (let [arr-str (seq (.split (.replace (.replace string "[" ";") "]" ";") ";"))
        prefix (.trim (.get arr-str 0))
        part-of-name (.trim (.get arr-str 1))
        suffix (.trim (.get arr-str 2))
        start-idx (.indexOf name part-of-name)
        rem-length (- (.length name) (.length part-of-name) start-idx)]
    (str prefix "+[" lvar " " start-idx " " rem-length "]+" suffix))) 
       
(defn
  convert-rule-to-string
  "Change a rule to string with specific format.
   Example : \"add + [?lvar 3 0] + s\" with ?lvar = \"getName\" -> \"add[Name]s\".
   with 3: start idx and 0: remaining chars no."
  [rule name]
  (let [arr-rule (seq (.split (.replace rule "+" ";") ";"))
        prefix (.trim (.get arr-rule 0))
        suffix (.trim (.get arr-rule 2))
        arr-name (seq (.split (.replace (.replace (.trim (.get arr-rule 1)) "[" "") "]" "") " "))
        part-of-name (.substring 
                       name 
                       (java.lang.Integer/valueOf (.get arr-name 1))
                       (- (.length name) (java.lang.Integer/valueOf (.get arr-name 2))))]
    (str prefix "[" part-of-name "]" suffix)))

(defn
  convert-rule-to-name
  [rule name]
  (.replace (.replace (convert-rule-to-string rule name) "[" "") "]" ""))