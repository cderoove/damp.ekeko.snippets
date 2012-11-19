(ns 
  ^{:doc "Auxiliary functions for snippet-driven querying."
    :author "Coen De Roover, Siltvani."}
  damp.ekeko.snippets.util)

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

