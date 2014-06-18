(ns 
  ^{:doc "Test suite for matching strategies of snippets."
    :author "Coen De Roover"}
  test.damp.ekeko.snippets.matching
  (:refer-clojure :exclude [== type declare record?])
  (:require  [clojure.core.logic :exclude [is] :as l])
  (:require [damp.ekeko.snippets 
             [querying :as querying]
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [matching :as matching]
             [parsing :as parsing]
             [util :as util]
             [persistence :as persistence]
             ])
  (:require [test.damp [ekeko :as test]])
  (:require [damp.ekeko.jdt 
             [astnode :as astnode]
             [ast :as ast]
             ])
  (:require [damp.ekeko 
             [logic :as el]
             [snippets :as snippets]])
  (:use clojure.test))



(defn
  snippetgroup-from-resource
  [pathrelativetobundle]
  (persistence/slurp-snippet (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile pathrelativetobundle)))


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
    

;; Persisted templates 
(deftest 
  ^{:doc "There are 11 class declarations in the project."}
  match-persisted-anyclass
  (let [snippet (snippetgroup-from-resource "/resources/TestCase-JDT-CompositeVisitor-Templates/anyclass.ekt")]
    (is (= 11 (count (snippets/query-by-snippetgroup snippet))))))


(deftest 
  ^{:doc "The anymethod.ekt template matches all non-constructor method declarations."}
  match-persisted-anymethod
  (let [ms-by-query 
        (count (damp.ekeko/ekeko [?m] 
                                 (l/fresh [?val]
                                        (ast/ast :MethodDeclaration ?m)
                                        (ast/has :constructor ?m ?val)
                                        (ast/value-raw ?val false))))
        snippet
        (snippetgroup-from-resource "/resources/TestCase-JDT-CompositeVisitor-Templates/anymethod.ekt")
        ms-by-snippet
        (count (snippets/query-by-snippetgroup snippet))]
    (is (= ms-by-query ms-by-snippet))))
        
        
;; Matching Strategy: regexp
;; -------------------------


(deftest 
  match-regexp-oneormore
  ^{:doc 
    "Contains an [?unbound.addComponent(...);]@[(multiplicity +)] invocation, of which there is only a sequence
     in method TestCase.runTest from the CompositeVisitor project."}
  (let [results
        (snippets/query-by-snippetgroup 
          (snippetgroup-from-resource "/resources/TestCase-JDT-CompositeVisitor-Templates/regexp_oneormore.ekt"))]
    (is (= 1 (count results)))))
                    
;; Test suite
  ;; ----------

(deftest
   test-suite 
   (let [testproject "TestCase-JDT-CompositeVisitor"]
     (test/against-project-named testproject false exactmatch-node)
     (test/against-project-named testproject false match-persisted-anyclass)
     (test/against-project-named testproject false match-persisted-anymethod)
     (test/against-project-named testproject false match-regexp-oneormore)

     )
   )

(defn 
  test-ns-hook 
  []
  (test/with-ekeko-disabled test-suite))


(comment  
  ;;Example repl session 
  (run-tests)
  )
  

