(ns 
  ^{:doc "Test suite for snippet-driven querying of Java projects."
    :author "Siltvani, Coen De Roover"}
  test.damp.ekeko.snippets
  (:refer-clojure :exclude [== type declare])
  (:use [clojure.core.logic :exclude [is]] :reload)
  (:use [damp.ekeko logic])
  (:use clojure.test)
  (:require [test.damp [ekeko :as test]]
            [damp.ekeko.jdt [reification :as reification]]
            [damp.ekeko [snippets :as snippets]]))

;; Individual tests

(deftest
  invocationsnippet-exactmatch-test
  (test/tuples-are 
    (snippets/query-by-snippet (snippets/jdt-node-as-snippet (snippets/parse-string-expression "x.m()")))
    "#{(\"x.m()\")}")) ;string obtained by evaluating (test/tuples-to-stringsetstring (snippets/query-by-snippet .....
     

;; Project tests

;; Snippet = methodA
(defn selected-snippet []
  (first (first 
           (damp.ekeko/ekeko [?m] 
                    (reification/ast :MethodDeclaration ?m) 
                    (fresh [?n]
                           (reification/has :name ?m ?n)
                           (reification/has :identifier ?n "methodA"))))))
        
;; Node exact match
;; methodA

(deftest
  node-exactmatch-test
  (let [snippet     (snippets/jdt-node-as-snippet (selected-snippet))]
    (test/tuples-are 
      (snippets/query-by-snippet snippet)
      (test/tuples-to-stringsetstring 
          (damp.ekeko/ekeko [?m] 
                    (reification/ast :MethodDeclaration ?m) 
                    (fresh [?n]
                           (reification/has :name ?m ?n)
                           (reification/has :identifier ?n "methodA")))))))

;; Introduce logic variable 
;; methodA & methodA1

;; Node for a SimpleName of methodA
(defn selected-node [?m]
  (first (first 
           (damp.ekeko/ekeko [?s]
                           (reification/has :name ?m ?s)
                           (reification/has :identifier ?s "methodA")))))

(deftest
  introduce-logic-variable-test
  (let [selected    (selected-snippet)
        snippet     (snippets/jdt-node-as-snippet selected)
        newsnippet  (snippets/introduce-logic-variable snippet (selected-node selected) '?m)]
    (test/tuples-are 
      (snippets/query-by-snippet newsnippet)
      (test/tuples-to-stringsetstring 
          (damp.ekeko/ekeko [?m] 
                    (reification/ast :MethodDeclaration ?m) 
                    (fresh [?n ?id]
                           (reification/has :name ?m ?n)
                           (reification/has :identifier ?n ?id)
                           (contains ["methodA" "methodA1"] ?id)))))))

;; List contains match
;; methodA, methodA1 & methodA2

;; list (statements) of methodA
(defn selected-list [?m]
  (first (first 
           (damp.ekeko/ekeko [?l]
                    (fresh [?b]
                           (reification/has :body ?m ?b)
                           (reification/has :statements ?b ?l))))))

(deftest
  list-contains-test
  (let [selected    (selected-snippet)
        snippet     (snippets/jdt-node-as-snippet selected)
        newsnippet  (snippets/introduce-logic-variable snippet (selected-node selected) '?m)
        newsnippet  (snippets/ignore-elements-sequence newsnippet (selected-list selected))]
    (test/tuples-are 
      (snippets/query-by-snippet newsnippet)
      (test/tuples-to-stringsetstring 
          (damp.ekeko/ekeko [?m] 
                    (reification/ast :MethodDeclaration ?m) 
                    (fresh [?n ?id]
                           (reification/has :name ?m ?n)
                           (reification/has :identifier ?n ?id)
                           (contains ["methodA" "methodA1" "methodA2"] ?id)))))))

;; Test suite

(deftest
   test-suite 
   (test/against-project-named "TestCase-Snippets-BasicMatching" false invocationsnippet-exactmatch-test)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false node-exactmatch-test)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false introduce-logic-variable-test)
   (test/against-project-named "TestCase-Snippets-BasicMatching" false list-contains-test)
   )

(defn 
  test-ns-hook 
  []
  (test/with-ekeko-disabled test-suite))


(comment  
  ;;Example repl session 
  (run-tests)
  )
  

