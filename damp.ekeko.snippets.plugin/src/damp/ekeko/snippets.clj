(ns 
  ^{:doc "Snippet-driven querying of Java projects."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets
  (:refer-clojure :exclude [== type])
  (:require [clojure.core.logic :as cl]) 
  (:require [damp.ekeko.snippets 
             [snippet :as snippet]
             [matching :as matching]
             [rewriting :as rewriting]
             [querying :as querying]
             [snippetgroup :as snippetgroup]
             [parsing :as parsing]
             [util :as util]
             [persistence :as persistence]
             [operators]
             [operatorsrep]
             [precondition]
             [querying]
             [gui]
             [runtime]
             [transformation]
             ;todo: fix
             ;[searchspace]
             [public]
             ;todo: fix
             ;[datastore]
             ])
  (:require [damp.ekeko.jdt 
             [astnode :as astnode]
             [rewrites :as rewrites]
             ])
  (:require [damp.ekeko])
  (:require [damp.ekeko.logic :as el]))    

(defn
  query-by-snippet*
  "Queries the Ekeko projects for matches for the given snippet. Opens Eclipse view on results."
  [snippet]
  (eval (querying/snippet-query snippet 'damp.ekeko/ekeko*)))

(defn
  query-by-snippet
  "Queries the Ekeko projects for matches for the given snippet."
  [snippet]
  (eval (querying/snippet-query snippet 'damp.ekeko/ekeko)))


(defn
  query-by-snippetgroup*
  "Queries the Ekeko projects for matches for the given snippetgroup. Opens Eclipse view on results."
  [snippetgroup]
  (let [q (querying/snippetgroup-query snippetgroup 'damp.ekeko/ekeko*)]
    (println q)
    (eval q)))

(defn
  query-by-snippetgroup
  "Queries the Ekeko projects for matches for the given snippetgroup."
  [snippetgroup]
  (eval (querying/snippetgroup-query snippetgroup 'damp.ekeko/ekeko)))

(defn
  transform-by-snippetgroups
  "Performs the program transformation defined by the lhs and rhs snippetgroups." 
  [snippetgroup|lhs snippetgroup|rhs]
  (do 
    (eval (querying/transformation-query snippetgroup|lhs snippetgroup|rhs))
    (rewrites/apply-and-reset-rewrites)))

(defn
  register-callbacks
  []
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_QUERY_BY_SNIPPET) query-by-snippet*)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_QUERY_BY_SNIPPETGROUP) query-by-snippetgroup*)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_TRANSFORM_BY_SNIPPETGROUPS) transform-by-snippetgroups))

(register-callbacks)


