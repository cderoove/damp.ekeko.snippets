(ns 
  ^{:doc "Test suite for matching strategies of snippets."
    :author "Siltvani, Coen De Roover"}
  test.damp.ekeko.snippets.matching
  (:refer-clojure :exclude [== type declare record?])
  (:require  [clojure.core.logic :exclude [is] :as l])
  (:require [damp.ekeko.snippets 
             [querying :as querying]
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [matching :as matching]
             [parsing :as parsing]
             [util :as util]])
  (:require [test.damp [ekeko :as test]])
  (:require [damp.ekeko.jdt 
             [astnode :as astnode]
             [ast :as ast]
             ])
  (:require [damp.ekeko 
             [logic :as el]
             [snippets :as snippets]])
  (:use clojure.test))

;; Matching Strategy: defaults (exact)
;; -----------------------------------

;; ASTNodes in general

(deftest
  ^{:doc "For all nodes n, n should be included in the matches for snippet(n)."}
  exactmatch-node 
  (let [nodes 
        (map first 
             (damp.ekeko/ekeko [?ast] (l/fresh [?kind] (ast/ast ?kind ?ast))))
        snippets
        (map matching/jdt-node-as-snippet nodes)]
    (doseq [[node snippet] (map vector nodes snippets)]
      (is (some #{node} (map first (snippets/query-by-snippet snippet)))))))
    

;; Test suite
;; ----------

(deftest
   test-suite 
   
   (test/against-project-named "TestCase-JDT-CompositeVisitor" false exactmatch-node)

   )

(defn 
  test-ns-hook 
  []
  (test/with-ekeko-disabled test-suite))


(comment  
  ;;Example repl session 
  (run-tests)
  )
  

