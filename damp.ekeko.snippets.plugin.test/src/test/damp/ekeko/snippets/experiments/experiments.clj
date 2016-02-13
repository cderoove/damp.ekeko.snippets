(ns 
  ^{:doc "Test suite that performs a number of genetic search experiments."
    :author "Tim Molderez"}
  test.damp.ekeko.snippets.experiments.experiments
  (:refer-clojure :exclude [== type declare record?])
  (:require  [clojure.core.logic :exclude [is] :as l])
  (:require [damp.ekeko.snippets 
             [operatorsrep :as operatorsrep]])
  (:require [damp.ekeko.snippets.geneticsearch
             [fitness :as fitness]
             [search :as search]
             [pmart :as pmart]])
  (:require [damp.ekeko.snippets 
             [querying :as querying]
             [persistence :as persistence]
             [transformation :as transformation]])
  (:require [test.damp [ekeko :as test]])
  (:use clojure.test))

(def experiment-config-default
  {:max-generations 50
   :fitness-weights [18/20 2/20]
   :match-timeout 30000
   :selection-weight 1/4
   :mutation-weight 2/4
   :crossover-weight 1/4
   :population-size 20
   :tournament-rounds 7})

(def experiments-root "/resources/EkekoX-Specifications/experiments/")
(def output-root "/Users/soft/Documents/experiments/")

(defn slurp-from-resource
  "Retrieve a resource file, relative to the root of the damp.ekeko.snippets.plugin.test project"
  [pathrelativetobundle]
  (persistence/slurp-snippet (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile pathrelativetobundle)))

(defn find-new-experiment-folder [experiment-name]
  "Return a unique folder name to write experiment results to"
  (loop [i 1]
    (let [folder-path (str output-root experiment-name "-" i)] 
      (if (.exists (clojure.java.io/as-file folder-path))
        (recur (inc i))
        folder-path
       ))))

(defn run-experiment
  "Given a set of verified matches, use the genetic search algorithm to find a template to match them all.
   An initial population may also be specified. If not, (part of) the verified matches are used to fill the initial population.
   @param projects             List of projects to be Ekeko-enabled. Each project is identified by its project name in Eclipse.
   @param config               Genetic search options map. This can be used to override the settings in experiment-config-default.
   @param initial-population   Optional: 
   @param verified             List of template groups; their matches serve as the verified matches."
  ([projects config verified]
    (run-experiment projects config verified verified))
  ([projects config initial-population verified]
    (test/with-ekeko-disabled
      (fn [] 
        (test/against-projects-named 
          projects false
          (fn []
            (let [merged-cfg (merge experiment-config-default config)
                  merged-cfg2 (merge merged-cfg 
                                     {:initial-population 
                                      (search/population-from-templates initial-population (:population-size merged-cfg))})
                  verifiedmatches (search/make-verified-matches
                                    (mapcat (fn [x] (into [] (fitness/templategroup-matches x)))
                                            verified)
                                    [])]
              (println "Finished construction of initial population; starting experiment..")
              (apply search/evolve verifiedmatches (mapcat identity (vec merged-cfg2))))))))))

(defn run-experiment-from-files
  "@see run-experiment .. except that you now pass along paths to .ekt files, instead of template groups"
  ([projects config verifiedmatches-ekt]
    (run-experiment-from-files projects config verifiedmatches-ekt verifiedmatches-ekt))
  ([projects config initial-population-ekt verifiedmatches-ekt]
    (run-experiment projects config
                    (map slurp-from-resource initial-population-ekt)
                    (map slurp-from-resource verifiedmatches-ekt))))

(deftest
  ^{:doc "Prototype in JHotdraw"}
  jh-prototype
  (let [tg (new ThreadGroup "Prototype")
        config {:max-generations 1200
                :match-timeout 360000
                :fitness-weights [18/20 2/20 0/20]
                :fitness-threshold 0.95
                :population-size 30
                :quick-matching true
                :partial-matching true
                :selection-weight 1/4
                :mutation-weight 3/4
                :crossover-weight 0/4
                :tournament-rounds 5
                :mutation-operators
                (filter 
                  (fn [op] 
                    (some #{(operatorsrep/operator-id op)} 
                          ["replace-by-variable"
                           ;"replace-by-exp"
                           "add-directive-equals"
                           ;"add-directive-equivalent"
                           ;"add-directive-protect" 
                           "add-directive-invokes" 
                           ;"add-directive-invokedby" 
                           "add-directive-constructs" 
                           ;"add-directive-constructedby" 
                           ;"add-directive-overrides" 
                           ;"add-directive-refersto" 
                           ;"add-directive-referredby" 
                           "add-directive-type" 
                           ;"add-directive-type|qname" 
                           ;"add-directive-type|sname" 
                           "add-directive-subtype+" 
                           ;"add-directive-subtype+|qname" 
                           ;"add-directive-subtype+|sname" 
                           "add-directive-subtype*" 
                           ;"add-directive-subtype*|qname" 
                           ;"add-directive-subtype*|sname" 
                           ;"restrict-scope-to-child" 
                           ;"relax-scope-to-child+" 
                           ;"relax-scope-to-child*" 
                           ;"generalize-directive" 
                           ;"remove-directive" 
                           ;"relax-size-to-atleast" 
                           ;"empty-body" 
                           ;"or-block" 
                           ;"relax-scope-to-member" 
                           ;"add-directive-replace" 
                           ;"add-directive-replace-value" 
                           ;"add-directive-add-element" 
                           ;"add-directive-insert-before" 
                           ;"add-directive-insert-after" 
                           ;"add-directive-remove-element" 
                           ;"add-directive-remove-element-alt" 
                           ;"add-directive-copy-node" 
                           ;"add-directive-move-element" 
                           ;"remove-node" 
                           ;"replace-parent" 
                           ;"replace-parent-stmt" 
                           ;"isolate-stmt-in-block" ; Bug in isolate-*? 
                           ;"isolate-stmt-in-method" 
                           ;"isolate-expr-in-method" 
                           ;"insert-node-before" 
                           ;"insert-node-after" 
                           ;"insert-node-at" 
                           ;"replace-node" 
                           ;"replace-value" 
                           ;"erase-list" 
                           ;"erase-comments" 
                           ;"ignore-comments" 
                           ;"ignore-absentvalues" 
                           "replace-by-wildcard" 
                           ;"replace-by-checked-wildcard" 
                           ;"consider-regexp|list" 
                           ;"consider-regexp|cfglist" 
                           ;"update-multiplicity" 
                           ;"consider-set|lst" 
                           ;"include-inherited" 
                           ;"add-directive-orimplicit" 
                           ;"add-directive-notnil" 
                           ;"add-directive-orsimple" 
                           ;"add-directive-orexpression" 
                           ;"generalize-references" 
                           ;"generalize-types" 
                           ;"generalize-types|qname" 
                           ;"extract-template" 
                           ;"generalize-invocations" 
                           ;"generalize-constructorinvocations"
                           ]))
                  (operatorsrep/registered-operators))
                :thread-group tg
                :output-dir (find-new-experiment-folder "prototype") ;(slurp "/Users/soft/Documents/workspace-runtime2/experiment-config.txt")
                }]
    (run-experiment-from-files
      [(pmart/projects :jhotdraw)]
      config
      [(str experiments-root "prototype-jhotdraw/initial.ekt")]
      [(str experiments-root "prototype-jhotdraw/solution.ekt")])))


(deftest
  ^{:doc "Template method in JHotdraw"}
  jh-template-method
  (let [tg (new ThreadGroup "Template-group")
        config {:max-generations 1200
                :match-timeout 240000
                :fitness-weights [18/20 2/20 0/20]
                :fitness-threshold 0.95
                :population-size 30
                :quick-matching false
                :partial-matching true
                :selection-weight 1/4
                :mutation-weight 3/4
                :crossover-weight 0/4
                :tournament-rounds 7
                :mutation-operators
                
                (filter 
                  (fn [op] 
                    (some #{(operatorsrep/operator-id op)} 
                          ["replace-by-variable"
                           ;"replace-by-exp"
                           "add-directive-equals"
                           ;"add-directive-equivalent"
                           ;"add-directive-protect" 
                           "add-directive-invokes" 
                           ;"add-directive-invokedby" 
                           "add-directive-constructs" 
                           ;"add-directive-constructedby" 
                           "add-directive-overrides" 
                           "add-directive-refersto" 
                           ;"add-directive-referredby" 
                           "add-directive-type" 
                           ;"add-directive-type|qname" 
                           ;"add-directive-type|sname" 
                           ;"add-directive-subtype+" 
                           ;"add-directive-subtype+|qname" 
                           ;"add-directive-subtype+|sname" 
                           "add-directive-subtype*" 
                           ;"add-directive-subtype*|qname" 
                           ;"add-directive-subtype*|sname" 
                           ;"restrict-scope-to-child" 
                           ;"relax-scope-to-child+" 
                           ;"relax-scope-to-child*" 
                           "generalize-directive" 
                           "remove-directive" 
                           ;"relax-size-to-atleast" 
                           ;"empty-body" 
                           ;"or-block" 
                           ;"relax-scope-to-member" 
                           ;"add-directive-replace" 
                           ;"add-directive-replace-value" 
                           ;"add-directive-add-element" 
                           ;"add-directive-insert-before" 
                           ;"add-directive-insert-after" 
                           ;"add-directive-remove-element" 
                           ;"add-directive-remove-element-alt" 
                           ;"add-directive-copy-node" 
                           ;"add-directive-move-element" 
                           "remove-node" 
                           ;"replace-parent" 
                           ;"replace-parent-stmt" 
                           ;"isolate-stmt-in-block" ; Bug in isolate-*? 
                           ;"isolate-stmt-in-method" 
                           ;"isolate-expr-in-method" 
                           ;"insert-node-before" 
                           ;"insert-node-after" 
                           ;"insert-node-at" 
                           ;"replace-node" 
                           ;"replace-value" 
                           ;"erase-list" 
                           ;"erase-comments" 
                           ;"ignore-comments" 
                           ;"ignore-absentvalues" 
                           "replace-by-wildcard" 
                           ;"replace-by-checked-wildcard" 
                           ;"consider-regexp|list" 
                           ;"consider-regexp|cfglist" 
                           ;"update-multiplicity" 
                           "consider-set|lst" 
                           "include-inherited" 
                           ;"add-directive-orimplicit" 
                           ;"add-directive-notnil" 
                           ;"add-directive-orsimple" 
                           ;"add-directive-orexpression" 
                           "generalize-references" 
                           "generalize-types" 
                           ;"generalize-types|qname" 
                           ;"extract-template" 
                           "generalize-invocations" 
                           ;"generalize-constructorinvocations"
                           ]))
                  (operatorsrep/registered-operators))
                :thread-group tg
                :output-dir (find-new-experiment-folder "template-method")
                }]
    (run-experiment-from-files
      [(pmart/projects :jhotdraw)]
      config
      [(str experiments-root "templatemethod-jhotdraw/initial.ekt")]
      [(str experiments-root "templatemethod-jhotdraw/solution.ekt")])))

(deftest
  ^{:doc "Observer in JHotdraw"}
  jh-observer
  (let [tg (new ThreadGroup "observer")
        config {:max-generations 1200
                :match-timeout 360000
                :fitness-weights [18/20 2/20 0/20]
                :fitness-threshold 0.95
                :population-size 30
                :quick-matching false
                :partial-matching true
                :selection-weight 1/4
                :mutation-weight 3/4
                :crossover-weight 0/4
                :tournament-rounds 7
                :mutation-operators
                (filter 
                  (fn [op] 
                    (some #{(operatorsrep/operator-id op)} 
                          ["replace-by-variable"
                           ;"replace-by-exp"
                           "add-directive-equals"
                           ;"add-directive-equivalent"
                           ;"add-directive-protect" 
                           "add-directive-invokes" 
                           ;"add-directive-invokedby" 
                           "add-directive-constructs" 
                           ;"add-directive-constructedby" 
                           "add-directive-overrides" 
                           "add-directive-refersto" 
                           ;"add-directive-referredby" 
                           "add-directive-type" 
                           ;"add-directive-type|qname" 
                           ;"add-directive-type|sname" 
                           ;"add-directive-subtype+" 
                           ;"add-directive-subtype+|qname" 
                           ;"add-directive-subtype+|sname" 
                           "add-directive-subtype*" 
                           ;"add-directive-subtype*|qname" 
                           ;"add-directive-subtype*|sname" 
                           ;"restrict-scope-to-child" 
                           ;"relax-scope-to-child+" 
                           ;"relax-scope-to-child*" 
                           "generalize-directive" 
                           "remove-directive" 
                           ;"relax-size-to-atleast" 
                           ;"empty-body" 
                           ;"or-block" 
                           ;"relax-scope-to-member" 
                           ;"add-directive-replace" 
                           ;"add-directive-replace-value" 
                           ;"add-directive-add-element" 
                           ;"add-directive-insert-before" 
                           ;"add-directive-insert-after" 
                           ;"add-directive-remove-element" 
                           ;"add-directive-remove-element-alt" 
                           ;"add-directive-copy-node" 
                           ;"add-directive-move-element" 
                           "remove-node" 
                           ;"replace-parent" 
                           ;"replace-parent-stmt" 
                           ;"isolate-stmt-in-block" ; Bug in isolate-*? 
                           ;"isolate-stmt-in-method" 
                           ;"isolate-expr-in-method" 
                           ;"insert-node-before" 
                           ;"insert-node-after" 
                           ;"insert-node-at" 
                           ;"replace-node" 
                           ;"replace-value" 
                           ;"erase-list" 
                           ;"erase-comments" 
                           ;"ignore-comments" 
                           ;"ignore-absentvalues" 
                           "replace-by-wildcard" 
                           ;"replace-by-checked-wildcard" 
                           ;"consider-regexp|list" 
                           ;"consider-regexp|cfglist" 
                           ;"update-multiplicity" 
                           "consider-set|lst" 
                           "include-inherited" 
                           ;"add-directive-orimplicit" 
                           ;"add-directive-notnil" 
                           ;"add-directive-orsimple" 
                           ;"add-directive-orexpression" 
                           "generalize-references" 
                           "generalize-types" 
                           ;"generalize-types|qname" 
                           ;"extract-template" 
                           "generalize-invocations" 
                           ;"generalize-constructorinvocations"
                           ]))
                  (operatorsrep/registered-operators))
                :thread-group tg
                :output-dir (find-new-experiment-folder "observer")
                }]
    (run-experiment-from-files
      [(pmart/projects :jhotdraw)]
      config
      [(str experiments-root "observer-jhotdraw/initial.ekt")]
      [(str experiments-root "observer-jhotdraw/solution.ekt")])))


(deftest
  ^{:doc "Strategy in JHotdraw"}
  jh-strategy
  (let [tg (new ThreadGroup "Strategy")
        config {:max-generations 1200
                :match-timeout 360000
                :fitness-weights [12/20 8/20 0/20]
                :fitness-threshold 0.95
                :population-size 30
                :quick-matching false
                :partial-matching true
                :selection-weight 1/4
                :mutation-weight 3/4
                :crossover-weight 0/4
                :tournament-rounds 5
                :mutation-operators
                (filter 
                  (fn [op] 
                    (some #{(operatorsrep/operator-id op)} 
                          ["replace-by-variable"
                           ;"replace-by-exp"
                           "add-directive-equals"
                           ;"add-directive-equivalent"
                           ;"add-directive-protect" 
                           "add-directive-invokes" 
                           ;"add-directive-invokedby" 
                           "add-directive-constructs" 
                           ;"add-directive-constructedby" 
                           ;"add-directive-overrides" 
                           ;"add-directive-refersto" 
                           ;"add-directive-referredby" 
                           "add-directive-type" 
                           ;"add-directive-type|qname" 
                           ;"add-directive-type|sname" 
                           "add-directive-subtype+" 
                           ;"add-directive-subtype+|qname" 
                           ;"add-directive-subtype+|sname" 
                           "add-directive-subtype*" 
                           ;"add-directive-subtype*|qname" 
                           ;"add-directive-subtype*|sname" 
                           ;"restrict-scope-to-child" 
                           ;"relax-scope-to-child+" 
                           ;"relax-scope-to-child*" 
                           ;"generalize-directive" 
                           ;"remove-directive" 
                           ;"relax-size-to-atleast" 
                           ;"empty-body" 
                           ;"or-block" 
                           ;"relax-scope-to-member" 
                           ;"add-directive-replace" 
                           ;"add-directive-replace-value" 
                           ;"add-directive-add-element" 
                           ;"add-directive-insert-before" 
                           ;"add-directive-insert-after" 
                           ;"add-directive-remove-element" 
                           ;"add-directive-remove-element-alt" 
                           ;"add-directive-copy-node" 
                           ;"add-directive-move-element" 
                           ;"remove-node" 
                           ;"replace-parent" 
                           ;"replace-parent-stmt" 
                           ;"isolate-stmt-in-block" ; Bug in isolate-*? 
                           ;"isolate-stmt-in-method" 
                           ;"isolate-expr-in-method" 
                           ;"insert-node-before" 
                           ;"insert-node-after" 
                           ;"insert-node-at" 
                           ;"replace-node" 
                           ;"replace-value" 
                           ;"erase-list" 
                           ;"erase-comments" 
                           ;"ignore-comments" 
                           ;"ignore-absentvalues" 
                           "replace-by-wildcard" 
                           ;"replace-by-checked-wildcard" 
                           ;"consider-regexp|list" 
                           ;"consider-regexp|cfglist" 
                           ;"update-multiplicity" 
                           ;"consider-set|lst" 
                           ;"include-inherited" 
                           ;"add-directive-orimplicit" 
                           ;"add-directive-notnil" 
                           ;"add-directive-orsimple" 
                           ;"add-directive-orexpression" 
                           ;"generalize-references" 
                           ;"generalize-types" 
                           ;"generalize-types|qname" 
                           ;"extract-template" 
                           ;"generalize-invocations" 
                           ;"generalize-constructorinvocations"
                           ]))
                  (operatorsrep/registered-operators))
                :thread-group tg
                :output-dir (find-new-experiment-folder "strategy")
                }]
    (run-experiment-from-files
      [(pmart/projects :jhotdraw)]
      config
      [(str experiments-root "strategy-jhotdraw/initial.ekt")]
      [(str experiments-root "strategy-jhotdraw/solution.ekt")])))

(deftest test-suite 
  (jh-template-method)
  (jh-observer)
  (jh-prototype))

(defn test-ns-hook []
  (test/with-ekeko-disabled test-suite))

(comment  
  ; Run *all* experiments (This will take several hours!)
  (run-tests))