(ns 
  ^{:doc "Runtime predicates for snippet-driven querying."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets.runtime
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko [logic :as el]]))

;;not working yet, not used yet
(comment
(defn
   val-exactmatch-logiclist
   "Predicate to check exact match java property value ?val with snippet list ?llist."
   [?val ?llist]
   (list-exactmatch-logiclist (:value ?val) ?llist))

(defn
   list-exactmatch-logiclist
   "Predicate to check exact match java list ?list with snippet list ?llist."
   [?list ?llist]
   (cl/conde [(cl/emptyo ?list)
           (el/equals true (empty? ?llist))]
          [(cl/fresh [?head ?tail ?ltail]
                  (cl/conso ?head ?tail ?list)
                  (el/equals ?head (first ?llist))
                  (el/equals ?ltail (rest ?llist))
                  (cl/== ?tail ?ltail)
                  (list-exactmatch-logiclist ?tail ?ltail))]))

(defn
   list-element-before
   "Predicate to check exact match java property value ?val with snippet list ?llist."
   [?list ?el1 ?el2]
   (< (.indexOf ?list ?el1) (.indexOf ?list ?el2)))
)

