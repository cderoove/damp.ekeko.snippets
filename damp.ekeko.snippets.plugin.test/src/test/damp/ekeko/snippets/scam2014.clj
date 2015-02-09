(ns 
  ^{:doc "Test suite to ensure that the SCAM type parameterization demo continues working."
    :author "Coen De Roover"}
  test.damp.ekeko.snippets.scam2014
  (:refer-clojure :exclude [== type declare record?])
  (:require  [clojure.core.logic :exclude [is] :as l])
  (:require [damp.ekeko 
             [snippets :as snippets]])
  (:require [damp.ekeko.snippets 
             [querying :as querying]
             [persistence :as persistence]
             [transformation :as transformation]
             ])
  (:require [test.damp [ekeko :as test]])
  (:use clojure.test))


(defn
  slurp-from-resource
  [pathrelativetobundle]
  (persistence/slurp-snippet (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile pathrelativetobundle)))

(deftest
  ^{:doc "scam_demo1.ekx"}
  match-demo1
  (let [snippet (transformation/transformation-lhs (slurp-from-resource "/resources/EkekoX-Specifications/scam_demo1.ekx"))]
    (is (= 93 (count (snippets/query-by-snippetgroup snippet))))))

(deftest
  ^{:doc "scam_demo2.ekx"}
  match-demo2
  (let [snippet (transformation/transformation-lhs (slurp-from-resource "/resources/EkekoX-Specifications/scam_demo2.ekx"))]
    (is (= 94 (count (snippets/query-by-snippetgroup snippet))))))

(deftest
  ^{:doc "scam_demo3.ekx"}
  match-demo3
  (let [snippet (transformation/transformation-lhs  (slurp-from-resource "/resources/EkekoX-Specifications/scam_demo3.ekx"))]
    (is (= 260 (count (snippets/query-by-snippetgroup snippet))))))

                    
;; Test suite
  ;; ----------

(deftest
   test-suite 
   (let [demoproject "TestCase-TypeParameters"]
     (test/against-project-named demoproject false match-demo1)
     (test/against-project-named demoproject false match-demo2)
     (test/against-project-named demoproject false match-demo3)
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
  

