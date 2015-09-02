(ns 
  ^{:doc "Provides integration with the JMetal library, so we can do multi-objective genetic search
          in order to automatically find a template from a given set of code snippets"
    :author "Tim Molderez"}
  damp.ekeko.snippets.geneticsearch.jmetal
  (:refer-clojure :exclude [rand-nth rand-int rand])
  (:require [damp.ekeko.snippets 
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [persistence :as persistence]
             [querying :as querying]
             [matching :as matching]
             [operators :as operators]
             [operatorsrep :as operatorsrep]
             [util :as util]
             [directives :as directives]])
  (:require [damp.ekeko.snippets.geneticsearch.search :as search])
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
  "Create a new Problem instance"
  []
  (let [problem (proxy [Problem] []
                  (getNumberOfObjectives [] 3)
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
;        (.setObjective sol 0 (search/fmeasure matches verifiedmatches))
        (.setObjective sol 0 (/
                               (count (search/truep matches verifiedmatches)) 
                               (count (:positives verifiedmatches))))
        (.setObjective sol 1 (/ 1 (search/template-size templategroup)))
        (.setObjective sol 2 (search/directive-count-measure templategroup))
        (.add solution-set sol)))
    solution-set))

(defn solution-score 
  [sol]
  (let [matches-obj (.getObjective sol 0)
        ast-obj (.getObjective sol 1)]
    (+
      (* 19/20 matches-obj)
      (* 1/20 ast-obj))))

(defn make-solution-comparator
  []
  "Make a Comparator to determine whether one solution is better than another"
  (proxy [java.util.Comparator] []
    (compare [sol1 sol2]
      (let [score1 (solution-score sol1)
            score2 (solution-score sol2)]
        (if (= score1 score2)
          0
          (if (< score1 score2)
            1
            -1))))))

(defn- solution-set-to-population
  "Obtain the population encapsulated by a JMetal SolutionSet"
  [solution-set]
  (map get-individual
       (iterator-seq (.iterator solution-set))))

(defn format-double [num]
  (format "%.4f" num))

(defn format-obj [ind obj]
  (format-double (.getObjective ind obj)))

(defn print-generation-info
  [generation solution-set verifiedmatches]
  (println "Generation:" generation)
  (iterator-seq (.iterator solution-set))
  (doseq [x (iterator-seq (.iterator solution-set))]
    (let [individual (get-individual x)] 
      (println
        "O1" (format-obj x 0) 
        "O2" (format-obj x 1)
        "O3" (format-obj x 2)
        "S" (format-double (solution-score x))
        "F" (format-double (.getFitness x)))))
  (let [best (.best solution-set (make-solution-comparator))]
    (println "Best solution (O1 " (format-obj best 0) " O2 " (format-obj best 1) " O3 " (format-obj best 2) " S " (format-double (solution-score best)) ")")
    (println (persistence/snippetgroup-string (get-individual best))))
  
  
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

(proxy [Solution] [(make-problem)]
    ; Hrm.. this is pretty gross.. can't add new methods using proxy, so I'm just hijacking some unused method..
    (clone [] true))

(defn 
  tournament-select
  "Do tournament selection in a sequence of solutions:
   We pick a number of random entries in the population, then return the best one from those entries.
   @param tournament-size  The number of random entries to pick"
  [solution-seq tournament-size]
  (let [do-select (fn [best tournaments]
                    (if (> tournaments 0)
                      (recur
                        (let [candidate (search/rand-nth solution-seq)]
                          (if (> (solution-score candidate) (solution-score best)) candidate best))
                        (dec tournaments))
                      best))]
    (do-select
      (search/rand-nth solution-seq)
      (dec tournament-size))))

(defn
  generate-new-population
  "Generate a new population based on the previous generation"
  [solution-set population-size verifiedmatches history]
  (let [tournament-size 20 ; p.47 of essentials of meta-heuristics; 2 is most common in general; 7 most common for gen. prog.
        population (solution-set-to-population solution-set)
        solution-seq (iterator-seq (.iterator solution-set))
        select (fn [] (get-individual (tournament-select solution-seq tournament-size)))
        is-viable (fn [individual]
                    (and  
                      ; We ignore the individuals we've seen before
                      (not (contains? history (hash individual)))
                      ; .. and those with fitness 0
                      (pos? (search/fmeasure 
                              (search/templategroup-matches individual)
                              verifiedmatches))))]
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
        (fn [x] true)))))

(defn nan-correct
  "Replace NaN fitness values by 0 .. otherwise IBEA.removeWorst() won't work properly.."
  [solution-set]
  (let [solution-seq (iterator-seq (.iterator solution-set))]
    (doseq [x solution-seq]
      (if (java.lang.Double/isNaN (.getFitness x))
        (.setFitness x 0.0)))))

(defn ibea-algorithm [verifiedmatches population-size archive-size max-generations]
  (proxy [IBEA] [(make-problem)]
    (execute []
      (let [initial-population (search/population-from-tuples (:positives verifiedmatches))
            initial-solution-set (population-to-solution-set initial-population verifiedmatches)]
        (loop [generation 0
               solution-set initial-solution-set 
               archive (new SolutionSet archive-size)
               history #{}]
          (if (< generation max-generations)
            ; Create the next generation
            (let [new-archive (let [union (.union solution-set archive)]
                                (.calculateFitness this union)
                                (nan-correct union)
                                (print-generation-info generation union verifiedmatches)
                                (while (> (.size union) population-size)
                                  (.removeWorst this union))
                                union)
                  new-population (generate-new-population new-archive population-size verifiedmatches history)
                  new-solution-set (population-to-solution-set new-population verifiedmatches)
                  new-history (clojure.set/union
                                history
                                (set (map hash new-population)))]
              (recur (inc generation) new-solution-set new-archive new-history))
            ; Once we're all done, create a ranking of the best solutions
            (.getSubfront (new Ranking archive) 0)))))))

(defn ibea-evolve [verifiedmatches max-generations]
  (let [algo (ibea-algorithm 
               verifiedmatches 
               10 ; population size 
               10 ; archive size
               max-generations)]
    (.execute algo)))

(defmacro dbg[x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))

(comment
  (def templategroup
    (persistence/slurp-from-resource "/resources/EkekoX-Specifications/invokedby.ekt"))
  (def matches (search/templategroup-matches templategroup))
  (def verifiedmatches (search/make-verified-matches matches []))
  (ibea-evolve verifiedmatches 50)
  
  ; Testing selection..
  (let [initial-population (search/population-from-tuples (:positives verifiedmatches))
        solution-set (population-to-solution-set initial-population verifiedmatches)
        solution-seq (iterator-seq (.iterator solution-set))
        select (fn [] (tournament-select solution-seq 70))]
    (doseq [x solution-seq] (println x))
    (.getObjective (select) 1))
)