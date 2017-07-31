(ns 
  ^{:doc "Test suite to ensure that the demo templates used for the Cha-Q project continue working."
    :author "Tim Molderez"}
  test.damp.ekeko.snippets.chaq
  (:refer-clojure :exclude [== type declare record?])
  (:require  [clojure.core.logic :exclude [is] :as l])
  (:require [damp.ekeko 
             [snippets :as snippets]])
  (:require [damp.ekeko.snippets 
             [querying :as querying]
             [persistence :as persistence]
             [transformation :as transformation]
             [matching2 :as matching2]
             ])
  (:require [test.damp [ekeko :as test]])
  (:use clojure.test))


(defn slurp-from-resource
  "Read an .ekt template, or .ekx transformation file"
  [pathrelativetobundle]
  (persistence/slurp-snippet (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile pathrelativetobundle)))

(defn run-query-test 
  "Query a .ekt/.ekx file on the given path,
   and pass the resulting list of matches to testfn, which determines whether a test fails/passes"
  [path testfn]
  (let [input 
        (cond
          (.endsWith path ".ekt")
          (slurp-from-resource path)
          (.endsWith path ".ekx")
          (transformation/transformation-lhs (slurp-from-resource path))
          :else
          (throw (Exception. "Unknown file type")))
        ; start (. System (nanoTime))
        matches (matching2/query-templategroup input)
        ; end (/ (double (- (. System (nanoTime)) start)) 1000000.0)
        ]
    (testfn matches)))

(deftest
  ^{:doc "chaq-demo1.ekx"}
  match-demo1
  (run-query-test "/resources/EkekoX-Specifications/chaq-demo/type-params/chaq-demo1.ekx"
                  (fn [matches] (is (= 1 (count matches))))))

(deftest
  ^{:doc "chaq-demo2.ekx"}
  match-demo2
  (run-query-test "/resources/EkekoX-Specifications/chaq-demo/type-params/chaq-demo2.ekx"
                  (fn [matches] (is (= 93 (count matches))))))

(deftest
  ^{:doc "chaq-demo3.ekx"}
  match-demo3
  (run-query-test "/resources/EkekoX-Specifications/chaq-demo/type-params/chaq-demo3.ekx"
                  (fn [matches] (is (= 94 (count matches))))))

(deftest
  ^{:doc "chaq-demo4.ekx"}
  match-demo4
  (run-query-test "/resources/EkekoX-Specifications/chaq-demo/type-params/chaq-demo4.ekx"
                  (fn [matches] (is (= 94 (count matches))))))

(deftest
  ^{:doc "chaq-demo5.ekx"}
  match-demo5
  (run-query-test "/resources/EkekoX-Specifications/chaq-demo/type-params/chaq-demo5.ekx"
                  (fn [matches] (is (= 136 (count matches))))))

                    
;; Test suite
;; ----------

(deftest
   test-suite 
   (let [demoproject "TestCase-TypeParameters"]
     (test/against-project-named 
       demoproject 
       false 
       (fn [] 
         (match-demo1)
         (match-demo2)
         (match-demo3)
         (match-demo4)
         (match-demo5)))))

(defn 
  test-ns-hook 
  []
  (test/with-ekeko-disabled test-suite))

(comment  
  ;;Example repl session 
  (run-tests))