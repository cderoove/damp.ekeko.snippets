(ns 
  ^{:doc "Conversion of snippets to Ekeko queries."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets.querying
  (:require [clojure.core.logic :as cl]) 
  (:require [damp.ekeko.snippets 
             [representation :as representation]
             [util :as util]
             [matching :as matching] 
             ]))
  
;; Converting a snippet to a query
;; -------------------------------

(defn 
  snippet-conditions
  "Returns a list of logic conditions that will retrieve matches for the given snippet."
  [snippet]
  (defn 
    conditions
    [ast-or-list]
    (concat (((matching/make-grounding-function (representation/snippet-grounder-for-node snippet ast-or-list)) ast-or-list) snippet)
            (((matching/make-constraining-function (representation/snippet-constrainer-for-node snippet ast-or-list)) ast-or-list) snippet)))
  (let [ast (:ast snippet)
        query (atom '())]
    (representation/walk-jdt-node-of-snippet 
      snippet
      ast
      (fn [astval]  (swap! query concat (conditions astval)))
      (fn [lstval] (swap! query concat (conditions lstval)))
      (fn [primval] (swap! query concat (conditions primval)))
      (fn [nilval] (swap! query concat (conditions nilval))))
    @query))

(defn
  snippet-var-for-root
  "Returns the logic variable associated with the root of the snippet
   (i.e., the JDT node the snippet originated from)."
  [snippet]
  (representation/snippet-var-for-node snippet (:ast snippet)))


(defn
  snippet-query-with-conditions
  [snippet ekekolaunchersymbol conditions]
  (let [root-var (snippet-var-for-root snippet)
        uservars (into #{} (representation/snippet-uservars snippet))
        vars (disj (into #{} (representation/snippet-vars snippet)) root-var)]
    `(~ekekolaunchersymbol 
       [~root-var ~@uservars]
       (cl/fresh [~@vars]
                 ~@conditions))))
  
(defn
  snippet-query
  "Returns an Ekeko query that that will retrieve matches for the given snippet."
  [snippet ekekolaunchersymbol]
  (snippet-query-with-conditions snippet ekekolaunchersymbol (snippet-conditions snippet)))
 

  
  

