(ns 
  ^{:doc "Runtime predicates for snippet-driven querying."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets.runtime
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko.snippets 
             [util :as util]
             [representation :as representation]])
  (:require 
    [damp.ekeko [logic :as el]]
    [damp.ekeko.jdt 
     [astnode :as astnode]
     [reification :as reification]]))

;;not yet working
(defn 
  list-matches-list
    "Predicate to check the list matches for the given list size and element conditions
     For Ekeko wrappers of ASTNode$NodeList instances:
        (listvalue ?var-match)
        (fresh [?newly-generated-var]
             (value-raw ?var-match ?newly-generated-var) 
             (equals snippet-list-size (.size ?var-match))
             (contains ?newly-generated-var ?var-for-element1) ... (contains ?newly-generated-var ?var-for-elementn))"
  [?var-match snippet-list-size & elements]
  (let [?var-match-raw (util/gen-lvar)]
       ((reification/listvalue ?var-match)
           (cl/fresh [?var-match-raw]
                  (reification/value-raw ?var-match ?var-match-raw)
                  (el/equals snippet-list-size (.size ?var-match-raw))
                  (for [?el elements]
                    (el/contains ?var-match-raw ?el))))))

