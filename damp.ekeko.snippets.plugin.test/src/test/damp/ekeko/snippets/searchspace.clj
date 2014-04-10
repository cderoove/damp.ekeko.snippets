(ns 
  ^{:doc "Test suite for searchspace."
    :author "Siltvani, Coen De Roover"}
  test.damp.ekeko.snippets.searchspace
  (:refer-clojure :exclude [== type declare])
  (:use [clojure.core.logic :exclude [is]] :reload)
  (:require [damp.ekeko])
  (:require [damp.ekeko 
             [logic :as el]
             [snippets :as snippets]])
  (:require [damp.ekeko.snippets 
             [querying :as querying]
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [operators :as operators]
             [matching :as matching]
             [parsing :as parsing]
             [precondition :as precondition]
             [searchspace :as searchspace]
             [util :as util]])
  (:require [test.damp [ekeko :as test]])
  (:require [damp.ekeko.jdt 
             [astnode :as astnode]
             [reification :as reification]
             ])
  (:use clojure.test))



(defn test-dfs [] 
  (defn safe-operator? [op value]
    (not (and (= op '/) (= value 0))))
  (let [init-value 0
        goal (/ 105 6)
        values '(100 0 10 5 6)
        map-operators {'+ + '- - '* * '/ /}]
    (Thread. 
      (fn [] 
        (let [start (java.util.Date.)]
          (searchspace/dfs init-value goal values map-operators safe-operator? =)
          (println "Time : " start (java.util.Date.)))))))

(def test-dfs-thread (test-dfs))

(defn test-dfs-snippet [] 
  (defn result-equal 
    [snippet string]
    (= (test/tuples-to-stringsetstring 
         (snippets/query-by-snippet snippet))
       string))
  (let [node
        (parsing/parse-string-declaration 
          "public int myMethodF(int val) {	r = 0; if (val == 0) {	r = val;	} return r; }")
        snippet 
        (snippet/jdt-node-as-snippet node)
        resnippet 
        (operators/allow-ifstatement-with-else
          snippet
          (fnext (.statements (.getBody node))))
        strsnippet 
        (test/tuples-to-stringsetstring 
          (snippets/query-by-snippet resnippet))
        snippet-nodes
        (remove  (fn [x] (= (:ast snippet) x)) (snippet/snippet-nodes snippet))]
    (Thread. 
      (fn [] 
        (let [start (java.util.Date.)]
          (searchspace/dfs 
            snippet 
            strsnippet 
            snippet-nodes
            precondition/searchspace-operators
            precondition/safe-operator-for-node? 
            result-equal)
          (println "Time : " start (java.util.Date.)))))))

(def test-dfs-snippet-thread (test-dfs-snippet))

;(.start test-dfs-snippet-thread )
;(.stop test-dfs-snippet-thread )



