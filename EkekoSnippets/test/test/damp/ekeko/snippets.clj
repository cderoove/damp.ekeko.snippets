(ns 
  ^{:doc "Test suite for snippet-driven querying of Java projects."
    :author "Siltvani, Coen De Roover"}
  test.damp.ekeko.snippets
  (:refer-clojure :exclude [== type declare])
  (:use [clojure.core.logic :exclude [is]] :reload)
  (:use [damp.ekeko logic])
  (:use clojure.test)
  (:require [test.damp [ekeko :as test]]
            [damp.ekeko [snippets :as snippets]]))

;; Individual tests

(deftest
  invocationsnippet-exactmatch-test
  (test/tuples-are 
    (snippets/query-by-snippet (snippets/jdt-node-as-snippet (snippets/parse-string-expression "x.m()")))
    "#{(\"x.m()\")}")) ;string obtained by evaluating (test/tuples-to-stringsetstring (snippets/query-by-snippet .....
                         

;; Test suite

(deftest
   test-suite 
   (test/against-project-named "TestCase-Snippets-BasicMatching" false invocationsnippet-exactmatch-test)
   ;;other tests should be invoked here
   )




(defn 
  test-ns-hook 
  [] 
  (test/with-ekeko-disabled test-suite))


(comment  
  ;;Example repl session
  
  (run-tests)
  
  )
  

