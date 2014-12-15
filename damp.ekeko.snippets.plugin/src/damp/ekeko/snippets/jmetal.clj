(ns 
  ^{:doc "Provides integration with the JMetal library, so we can do multi-objective genetic search
          in order to automatically find a template from a given set of code snippets"
    :author "Tim Molderez"}
damp.ekeko.snippets.jmetal
  (:refer-clojure :exclude [rand-nth rand-int rand])
  (:require [inspector-jay [core :as jay]])
  (:require [damp.ekeko.snippets 
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [persistence :as persistence]
             [querying :as querying]
             [matching :as matching]
             [operators :as operators]
             [operatorsrep :as operatorsrep]
             [util :as util]
             [directives :as directives]
             [search :as search]])
  (:import [jmetal.core Solution SolutionSet Problem SolutionType]
           [jmetal.util Ranking]
           [jmetal.util.comparators FitnessComparator]
           [jmetal.metaheuristics.ibea IBEA]
           [jmetal.operators.selection BinaryTournament]))

(defn make-solution-type
  "Make a dummy solution type (with 0 decision variables)"
  [problem]
  (proxy [SolutionType] [problem]
    (createVariables [] nil)))

(defn make-problem 
  "Create a new 2-objective Problem instance"
  []
  (let [problem (proxy [Problem] []
                  (getNumberOfObjectives [] 2)
                  (evaluate [solution] nil))]
    (.setNumberOfVariables problem 0)
    (.setSolutionType problem (make-solution-type problem))
    problem))

(defn make-solution 
  "Make a JMetal wrapper for an individual"
  [individual]
  (proxy [Solution] [(make-problem)]
    ; Hrm.. this is pretty gross.. can't add new methods using proxy, so I'm just hijacking some unused method..
    (clone [] individual)))

(defn- get-individual [solution]
  (.clone solution))

(defn- population-to-solution-set
  "Wrap a population in a JMetal SolutionSet, and store the objective values of each individual"
  [population verifiedmatches]
  (let [pop-size (count population)
        solution-set (new SolutionSet pop-size)]
    (doseq [i (range 0 pop-size)]
      (let [templategroup (nth population i)
            sol (make-solution templategroup)
            matches (search/templategroup-matches templategroup)]
;        (.setObjective sol 0 (* 1 (search/rand-int 1000)))
;        (.setObjective sol 1 (* 1 (search/rand-int 1000)))
; Fitness of an individual depends on the entire population .. fitness is NaN when all objective vals of the population are the same..
        (.setObjective sol 0 (search/fmeasure matches verifiedmatches))
        (.setObjective sol 1 (search/directive-count-measure templategroup))
        (.add solution-set sol)))
    solution-set))

(defn- solution-set-to-population
  "Obtain the population encapsulated by a JMetal SolutionSet"
  [solution-set]
  (map get-individual
       (iterator-seq (.iterator solution-set))))

(defn format-double [num]
  (format "%.3f" num))

(defn print-generation-info
  [generation solution-set verifiedmatches]
  (println "Generation:" generation)
  (iterator-seq (.iterator solution-set))
  (doseq [x (iterator-seq (.iterator solution-set))]
    (let [individual (get-individual x)] 
      (println
        "O1" (format-double (.getObjective x 0)) 
        "O2" (format-double (.getObjective x 1))
        "F" (format-double (.getFitness x)))))
;  (jay/inspect (map 
;                 (fn [individual]
;                   [;(snippetgroup/snippetgroup-name individual)
;                    (search/fmeasure 
;                      (search/templategroup-matches individual)
;                      verifiedmatches)
;                    (meta individual)
;                    ;(snippetgroup/snippetgroup-snippetlist individual)
;                    (persistence/snippetgroup-string individual)]) 
;                 (solution-set-to-population solution-set)))
  )

(defn make-selection-operator []
  "Create a JMetal selection operator"
  (new BinaryTournament
       (java.util.HashMap. {"comparator" (new FitnessComparator)})))

(defn 
  tournament-select
  "Do tournament selection in a sequence of solutions:
   We pick a number of random entries in the population, then return the best one from those entries.
   @param tournament-size  The number of random entries to pick"
  [solution-seq tournament-size]
  (let [do-select (fn [best tournaments]
                    (if (> 0 tournaments)
                      (recur
                        (let [candidate (search/rand-nth solution-seq)]
                          (if (> (.getFitness candidate) (.getFitness best))
                            candidate
                            best))
                        (dec tournament-size))
                      best))]
    (get-individual (do-select 
                      (search/rand-nth solution-seq)
                      (dec tournament-size)))))

(defn
  generate-new-population
  "Generate a new population based on the previous generation"
  [solution-set population-size verifiedmatches]
  (let [tournament-size 2 ; p.47 of essentials of meta-heuristics; 2 is most common in general; 7 most common for gen. prog.
        population (solution-set-to-population solution-set)
        solution-seq (iterator-seq (.iterator solution-set))
;        selector (make-selection-operator)
;        select (fn [solution-set]
;                 (get-individual (.execute selector solution-set)))
        select (fn [] (tournament-select solution-seq tournament-size))
        is-viable (fn [individual]
                    (pos? (search/fmeasure 
                            (search/templategroup-matches individual)
                            verifiedmatches)))]
    (concat
      ; Mutation
      (util/viable-repeat 
        (* 1/2 population-size) 
        #(search/mutate (select)) 
        is-viable)
      ; Crossover (Note that each crossover operation produces a pair)
      (apply concat
             (util/viable-repeat 
               (* 1/8 population-size)
               #(search/crossover (select) (select))
               (fn [x] (and (is-viable (first x)) (is-viable (second x))))))
      ; Selection
      (util/viable-repeat 
        (* 1/4 population-size) 
        #(select)
        is-viable))))

(defn ibea-algorithm [verifiedmatches population-size archive-size max-generations]
  (proxy [IBEA] [(make-problem)]
    (execute []
      (let [initial-population (search/population-from-tuples (:positives verifiedmatches))
            initial-solution-set (population-to-solution-set initial-population verifiedmatches)]
        (loop [generation 0
               solution-set initial-solution-set 
               archive (new SolutionSet archive-size)]
          (if (< generation max-generations)
            ; Create the next generation
            (let [new-archive (let [union (.union solution-set archive)]
                                (.calculateFitness this union)
                                (print-generation-info generation solution-set verifiedmatches)
                                (while (> (.size union) population-size)
                                  (.removeWorst this union))
                                union)
                  new-solution-set (population-to-solution-set
                                     (generate-new-population new-archive population-size verifiedmatches)
                                     verifiedmatches)]
              (recur (inc generation) new-solution-set new-archive))
            ; Once we're all done, create a ranking of the best solutions
            (.getSubfront (new Ranking archive) 0)))))))

(defn ibea-evolve [verifiedmatches max-generations]
  (let [algo (ibea-algorithm 
               verifiedmatches 
               50 ; population size 
               50 ; archive size
               max-generations)]
    (.execute algo)))

(comment
  (def templategroup
    (persistence/slurp-from-resource "/resources/EkekoX-Specifications/invokedby.ekt"))
  (def matches (search/templategroup-matches templategroup))
  (def verifiedmatches (search/make-verified-matches matches []))
  (ibea-evolve verifiedmatches 50)
)