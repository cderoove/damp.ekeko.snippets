(ns 
  ^{:doc "Test suite to ensure that the solution templates used for the SSBSE16 paper continue working."
    :author "Tim Molderez"}
  test.damp.ekeko.snippets.ssbse
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
  ^{:doc "JHotDraw - Observer"}
  jhd-observer
  (run-query-test "/resources/EkekoX-Specifications/experiments/observer-jhotdraw/solution.ekt"
                  (fn [matches] (is (= 21 (count (into #{} matches)))))))
(deftest
  ^{:doc "JHotDraw - Prototype"}
  jhd-prototype
  (run-query-test "/resources/EkekoX-Specifications/experiments/prototype-jhotdraw/solution.ekt"
                  (fn [matches] (is (= 27 (count (into #{} matches)))))))

(deftest
  ^{:doc "JHotDraw - Template method"}
  jhd-templatemethod
  (run-query-test "/resources/EkekoX-Specifications/experiments/templatemethod-jhotdraw/solution.ekt"
                  (fn [matches] (is (= 47 (count (into #{} matches)))))))

(deftest
  ^{:doc "JHotDraw - Strategy"}
  jhd-strategy
  (run-query-test "/resources/EkekoX-Specifications/experiments/strategy-jhotdraw/solution3.ekt"
                  (fn [matches] (is (= 13 (count (into #{} matches)))))))

(deftest
  ^{:doc "JHotDraw - Factory method"}
  jhd-factorymethod
  (run-query-test "/resources/EkekoX-Specifications/experiments/factorymethod-jhotdraw/solution_take4-reorder2.ekt"
                  (fn [matches] (is (= 22 (count (into #{} matches)))))))

(deftest
  ^{:doc "Nutch - Template method"}
  nutch-templatemethod
  (run-query-test "/resources/EkekoX-Specifications/experiments/templatemethod-nutch/solution3.ekt"
                  (fn [matches] (is (= 7 (count (into #{} matches)))))))

(deftest
  ^{:doc "Nutch - Strategy"}
  nutch-strategy
  (run-query-test "/resources/EkekoX-Specifications/experiments/strategy-nutch/solution3.ekt"
                  (fn [matches] (is (= 74 (count (into #{} matches)))))))

(deftest
  ^{:doc "Nutch - Bridge"}
  nutch-bridge
  (run-query-test "/resources/EkekoX-Specifications/experiments/bridge-nutch/solution3.ekt"
                  (fn [matches] (is (= 69 (count (into #{} matches)))))))


                    
;; Test suite
;; ----------

(deftest
   test-suite 
   (let [jhotdraw "6 - JHotDraw v5.1"
         nutch "10 - Nutch v0.4"]
     (test/against-project-named 
       jhotdraw 
       false 
       (fn [] 
         (jhd-observer)
         (jhd-prototype)
         (jhd-templatemethod)
         (jhd-strategy)
         (jhd-factorymethod)
         ))
     (test/against-project-named 
       nutch 
       false 
       (fn [] 
         (nutch-templatemethod)
         (nutch-strategy)
         (nutch-bridge)
         ))
     ))

(defn 
  test-ns-hook 
  []
  (test/with-ekeko-disabled test-suite))

(comment  
  ;;Example repl session
  ; Running these tests takes a few minutes (3 or so)
  (run-tests))