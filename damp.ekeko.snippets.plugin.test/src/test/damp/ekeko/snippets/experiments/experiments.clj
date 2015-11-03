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
   :fitness-weights [19/20 1/20]
   :match-timeout 30000
   :selection-weight 1/4
   :mutation-weight 2/4
   :crossover-weight 1/4
   :population-size 20
   :tournament-rounds 7})

(defn slurp-from-resource
  "Retrieve a resource file"
  [pathrelativetobundle]
  (persistence/slurp-snippet (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile pathrelativetobundle)))

(defn run-experiment
  "Given a set of verified matches, use the genetic search algorithm to find a template to match them all.
   An initial population may also be specified. If not, the verified matches are used to fill the initial population.
   @param projects             List of projects to be Ekeko-enabled. Each project is identified by its project name in Eclipse.
   @param config               Genetic search options map. This can be used to override the settings in experiment-config-default.
   @param initial-population   Optional: List of template 
   @param verified             List of template groups; their matches serve as the verified matches."
  ([projects config verified]
    ; In this case, the verified matches are also used as initial population..
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

;(defn run-experiment-with-initial-pop
;  "Given an initial population, as a set of ekt files ; and a solution ekt (whose matches are the verified matches),
;   perform genetic search to find a template that has the same matches as the solution ekt
;   @param folder-name       Folder where to find the ekt files of the initial population
;   @param intial-pop        List of ekt file names of the initial population
;   @param verifiedmatch-ekt File path to the solution ekt
;   @param projects          P-mart project names that should be Ekeko-enabled
;   @param config            Genetic search config"
;  [folder-name initial-pop verifiedmatches-ekt projects config]
;  (run-experiment
;    projects
;    config
;    (pmart/slurp-templategroups
;      "Initial pop"
;      folder-name
;      initial-pop)
;    [(slurp-from-resource verifiedmatches-ekt)]))

(deftest
  ^{:doc "Template method in JHotdraw"}
  jh-template-method
  (let [tg (new ThreadGroup "invokedby")]
    (run-experiment-from-files
      [(pmart/projects :jhotdraw)]
      {:max-generations 1200
      :match-timeout 480000
      :fitness-weights [18/20 2/20 0/20]
      :fitness-threshold 0.95
      :population-size 50
      :quick-matching false
      :selection-weight 1/4
      :mutation-weight 3/4
      :crossover-weight 0/4
      :tournament-rounds 7
      :mutation-operators (filter 
                            (fn [op]
                              (some #{(operatorsrep/operator-id op)} 
                                    ["add-directive-equals"
                                     "add-directive-invokes"
                                     "add-directive-overrides"
                                     "remove-node"
                                     ;"consider-set|lst"
                                     ;"isolate-stmt-in-method"
                                     "replace-by-variable"
                                     "replace-by-wildcard"]))
                            (operatorsrep/registered-operators))
      :thread-group tg
      :output-dir (slurp "/Users/soft/Documents/workspace-runtime2/experiment-config.txt")}
     ["/resources/EkekoX-Specifications/dbg/templatemethod-jhotdraw/initial-population/10.ekt"
      "/resources/EkekoX-Specifications/dbg/templatemethod-jhotdraw/initial-population/9.ekt"
      "/resources/EkekoX-Specifications/dbg/templatemethod-jhotdraw/initial-population/7.ekt"]
     ["/resources/EkekoX-Specifications/dbg/templatemethod-jhotdraw/solution3.ekt"])
;    (run-experiment-with-initial-pop
;     (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile "/resources/EkekoX-Specifications/dbg/templatemethod-jhotdraw")
;     [["-1685183065-AbstractFigure.ekt" "-2090076616-PolygonFigure.ekt"]
;      ["-1685183065-AbstractFigure.ekt" "1655477502-ImageFigure.ekt"]
;      ["2098468581-AttributeFigure.ekt" "-2090076616-PolygonFigure.ekt"]
;      ["2098468581-AttributeFigure.ekt" "1655477502-ImageFigure.ekt"]]
;     "/resources/EkekoX-Specifications/dbg/templatemethod-jhotdraw/solution3.ekt"
;     [(pmart/projects :jhotdraw)]
;     {:max-generations 400
;      :match-timeout 240000
;      :fitness-weights [18/20 2/20 0/20]
;      :fitness-threshold 0.95
;      :population-size 50
;      :selection-weight 1/4
;      :mutation-weight 3/4
;      :crossover-weight 0/4
;      :tournament-rounds 7
;      :mutation-operators (filter 
;                            (fn [op]
;                              (some #{(operatorsrep/operator-id op)} 
;                                    ["add-directive-equals"
;                                     "add-directive-invokes"
;                                     "add-directive-overrides"
;                                     ;"consider-set|lst"
;                                     "isolate-stmt-in-method"
;                                     "replace-by-variable"
;                                     "replace-by-wildcard"]))
;                            (operatorsrep/registered-operators))
;      :thread-group tg
;      :output-dir (slurp "/Users/soft/Documents/workspace-runtime/experiment-config.txt")
;      })
    
    ))                    

(deftest test-suite 
  (jh-template-method) 
;   (let [proj "TestCase-TypeParameters"]
;     (test/against-project-named proj false jh-template-method))
)

(defn test-ns-hook []
  (test/with-ekeko-disabled test-suite))

(comment  
  ; Example repl session 
  (run-tests))