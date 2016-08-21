(ns
  ^{:doc "Functions to compute the fitness of individuals."
    :author "Coen De Roover, Tim Molderez"}
  damp.ekeko.snippets.geneticsearch.fitness
  (:refer-clojure :exclude [rand-nth rand-int rand])
  (:import 
    [damp.ekeko JavaProjectModel]
    [org.eclipse.jface.text Document]
    [org.eclipse.jdt.core ICompilationUnit IJavaProject]
    [org.eclipse.jdt.core.dom BodyDeclaration Expression Statement ASTNode ASTParser AST CompilationUnit]
    [org.eclipse.jdt.core.dom.rewrite ASTRewrite])
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko]
            [damp.ekeko.jdt
             [astnode :as astnode]])
  (:require [damp.ekeko.snippets 
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [persistence :as persistence]
             [querying :as querying]
             [matching :as matching]
             [matching2 :as matching2]
             [operators :as operators]
             [operatorsrep :as operatorsrep]
             [util :as util]
             [directives :as directives]
             [transformation :as transformation]])
  (:import [ec.util MersenneTwister]
           [damp.ekeko.snippets.geneticsearch PartialJavaProjectModel]))

(declare new-match)

(defrecord MatchedNodes
  [in-progress ; Set of nodes that have matched in the current attempt to match with a snippet
   done        ; List of how many nodes matched in the previous attempts
   ])

(def ^:dynamic matched-nodes (atom (MatchedNodes. #{} [])))

(defn templategroup-matches
  "Given a templategroup, look for all of its matches in the code"
  [templategroup]
  (matching2/query-templategroup-list templategroup false))

(defn 
  truep
  "True positives; how many results were correctly considered relevant"
  [matches verifiedmatches]
  (clojure.set/intersection matches (:positives verifiedmatches)))

(defn
  falsep
  "False positives; how many results were incorrectly considered relevant; i.e. these results shouldn't be there"
  [matches verifiedmatches]
  (clojure.set/difference matches (:positives verifiedmatches)))

(defn
  falsen
  "False negatives; how many results were incorrectly considered irrelevant; i.e. these results were missed"
  [matches verifiedmatches]
  (clojure.set/difference (:positives verifiedmatches) matches))
  
(defn 
  precision
  [matches verifiedmatches]
  (let [ctp (count (truep matches verifiedmatches))
        cfp (count (falsep matches verifiedmatches))]
    (if (= 0 (+ cfp ctp))
      0
      (/ ctp (+ cfp ctp)))))
  
(defn
  recall
  [matches verifiedmatches]
  (let [ctp (count (truep matches verifiedmatches))
        cfn (count (falsen matches verifiedmatches))]
    (if (= 0 ctp)
      0
      (/ ctp (+ ctp cfn)))))
  
(defn
  fmeasure
  "Calculate the F-measure/F1-score by comparing the matches produced by an individual
   to the matches we actually want
   @param matches  the matches of an individual
   @param verifiedmatches  the matches we want"
  [matches verifiedmatches]
  (let [p (precision matches verifiedmatches)
        r (recall matches verifiedmatches)]
    (if (= (+ p r) 0)
      0
      (* 2 (/ (* p r) (+ p r))))))
  
(defn snippetgroup-nodes
  [snippetgroup]
  (mapcat snippet/snippet-nodes (snippetgroup/snippetgroup-snippetlist snippetgroup) ))

(defn count-directives
  "Count the number of directives used in a snippet group (excluding default directives)"
  [snippetgroup]
  (reduce 
    (fn [countsofar bdlist]
      (+ countsofar (count bdlist)))
    0
    (mapcat snippet/snippet-bounddirectives (snippetgroup/snippetgroup-snippetlist snippetgroup))))
    
(defn directive-count-measure
  "Produce a score in [0,1] to reflect how complex a template looks,
   which is based on how many directives are used in the template"
  [templategroup]
  (- 1 (/ (count-directives templategroup)   
          (* (count (matching/registered-directives)) (count (snippetgroup-nodes templategroup))))))

(defn register-match [match]
  (swap! matched-nodes 
         (fn [x]
           (assoc x :in-progress
                  (clojure.set/union (:in-progress x) #{match})))))

(defn add-match [?node-var]
  (damp.ekeko.logic/perform (register-match ?node-var)))

(defn new-match []
  (swap! matched-nodes (fn [x]
                         (-> x
                           (assoc :done (conj (:done x) (count (:in-progress x))))
                           (assoc :in-progress #{})))))

(defn reset-matched-nodes []
  (reset! matched-nodes (MatchedNodes. #{} [])))


(defn partialmatch-score
  "Compute the partial matching score of the last templategroup that we tried to match
   @param node-count number of nodes in that last templategroup"
  [node-count]
  (let [partial-matches (first (:done @matched-nodes))]
    (reset-matched-nodes)
    (let [score (/ partial-matches node-count)]
        (if (> score 1)
          (println "!Partial matching score cannot > 1" @matched-nodes)
          score))))

(defn create-partial-model
  "Create a PartialJavaProjectModel such that only the given list of ASTs are queried"
  [matchgroups]
  (let [partialmodel (new PartialJavaProjectModel)]
    (doseq [matchgroup matchgroups]
      (doseq [match matchgroup]
        (.addExistingAST partialmodel match)))
    partialmodel))

(defn make-fitness-function
  "Return a fitness function, used to measure how good/fit an individual is.
   A fitness function returns a pair: [overall-fitness fitness-components]
   ,where overall-fitness is a value between 0 (worst) and 1 (best)
   and fitness-components is a list of components that were used to compute the overall fitness"
  [verifiedmatches config]
  (let [partialmodels (map
                        (fn [match] (create-partial-model [match]))
                        (:positives verifiedmatches))
        partialmodel-merged (create-partial-model (:positives verifiedmatches))]
    (fn [templategroup]
      (let [matches (templategroup-matches templategroup)
            fscore (double (fmeasure matches verifiedmatches))
            node-count (matching2/templategroup-node-count templategroup)
            
            partialscore (double 
                           (if (:partial-matching config)
                             (/
                               (reduce + (pmap (fn [partialmodel]
                                                 (binding [damp.ekeko.ekekomodel/*queried-project-models* (atom [partialmodel])]
                                                   (/ 
                                                     (:node-count (meta (matching2/query-templategroup templategroup))) 
                                                     node-count)))
                                               partialmodels))
                               (count partialmodels))
                             0))
            
            weights (:fitness-weights config)]
        [(+
           (* (nth weights 0) fscore)
           (* (nth weights 1) partialscore))
         [fscore partialscore 0 0]]))))

(defn make-fitness-function-old
  "Return a fitness function, used to measure how good/fit an individual is.
   A fitness function returns a pair: [overall-fitness fitness-components]
   ,where overall-fitness is a value between 0 (worst) and 1 (best)
   and fitness-components is a list of components that were used to compute the overall fitness"
  [verifiedmatches config]
  (let [partialmodels (map
                        (fn [match] (create-partial-model [match]))
                        (:positives verifiedmatches))
        partialmodel-merged (create-partial-model (:positives verifiedmatches))]
    (fn [templategroup]
      (util/with-timeout (:match-timeout config)
        (let [
              tmp-ns (util/gen-ns) ; Temporary namespace in which we'll do the query matching
;              tmp-ns2 (util/gen-ns)
              [defines query] (querying/query-by-snippetgroup-noeval templategroup 'damp.ekeko/ekeko `((damp.ekeko.logic/perform (new-match))) '() true)
;              all-code (querying/query-by-snippetgroup-fast-noeval templategroup 'damp.ekeko/ekeko)
              defs! (doseq [define defines] (util/eval-in-ns define tmp-ns))
;              defs2! (doseq [[defines query] all-code] (doseq [define defines] (util/eval-in-ns define tmp-ns2)))
;              eval-query-in-ns (fn []
;                                 (let [results
;                                       (pmap (fn [[defines query]]
;                                                       (into #{} (util/eval-in-ns query tmp-ns2)))
;                                                 all-code)
;                                       combined-results (querying/combine-noeval-results templategroup results 'damp.ekeko/ekeko)]
;                                   (set (querying/strip-uservars templategroup combined-results))))
              
              matches (if (:quick-matching config)
                        (binding [damp.ekeko.ekekomodel/*queried-project-models* (atom [partialmodel-merged])]
                          (into #{} (util/eval-in-ns query tmp-ns)) ;(eval-query-in-ns)
                          )
                        (into #{} (util/eval-in-ns query tmp-ns)) ;(eval-query-in-ns)
                        )
              fscore (double (fmeasure matches verifiedmatches))
              
              node-count
              (reduce + (map 
                          (fn [snippet] (count (matching/reachable-nodes snippet (snippet/snippet-root snippet))))
                          (snippetgroup/snippetgroup-snippetlist templategroup)))
              
              partialscore
              (double 
                (if (:partial-matching config)
                  (/
                    (reduce + (pmap (fn [partialmodel]
                                      (binding [damp.ekeko.ekekomodel/*queried-project-models* (atom [partialmodel])
                                                matched-nodes (atom (MatchedNodes. #{} []))]
                                        (do
                                          (into #{} (util/eval-in-ns query tmp-ns))
                                          (new-match)
                                          (partialmatch-score node-count)))
                                      )
                                    partialmodels))
                    (count partialmodels))
                  0))
              
              weights (:fitness-weights config)
              clean! (remove-ns (ns-name tmp-ns))
;              clean2! (remove-ns (ns-name tmp-ns2))
              
              ; directive-count-score (directive-count-measure templategroup)
              ; If > 0, we have more false positives ; if < 0, we have more false negatives
              ; false-bias (- (count (falsep matches verifiedmatches)) (count (falsen matches verifiedmatches)))
              ; dirscore (/ 1 (inc (* 1/2 (count-directives templategroup))))
              ; lengthscore (/ 1 (template-size templategroup))
              ]
          [(+
             (* (nth weights 0) fscore)
             (* (nth weights 1) partialscore))
           [fscore partialscore 0 0]])
        (:thread-group config)))))