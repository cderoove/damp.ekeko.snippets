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
  "Given a templategroup, look for all of its matches in the code
   (An exception is thrown if matching takes longer than timeout milliseconds..)"
  [templategroup]
  (into #{}
        (querying/query-by-snippetgroup
          templategroup 
          'damp.ekeko/ekeko 
          `((damp.ekeko.logic/perform (new-match)))
          '() 
          true)))

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

;(defn directive-count-measure
;  "Produce a score in [0,1] to reflect how complex a template looks,
;   which is based on how many directives are used in the template"
;  [templategroup]
;  (/ 1 (inc (* 1/2 (count-directives templategroup)))))



(defn count-directives
  "Count the number of directives used in a snippet group (excluding default directives)"
  [snippetgroup]
  ;  (count (snippet/snippet-bounddirectives snippet node))
  
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
          (* (count (matching/registered-directives)) (count (snippetgroup-nodes templategroup)))))
  
;  (/ 1 (inc (* 1/2 (count-directives templategroup))))
  )

  (def templategroup1
    (persistence/slurp-from-resource "/resources/EkekoX-Specifications/invokedby.ekt"))
  
  (def templategroup2
    (persistence/slurp-from-resource "/resources/EkekoX-Specifications/invokedby2.ekt"))

  (double (directive-count-measure templategroup1))

;
;(defn simple-measure
;  "Alternative to fmeasure, in which we simply don't care about the matches that are neither in :positives or :negatives.
;   Produces a number in [0-1], such that higher means more correct (positive or negative) results"
;  [matches verifiedmatches]
;  (let [correct-positives (clojure.set/intersection matches (:positives verifiedmatches))
;        correct-negatives (clojure.set/difference (:negatives verifiedmatches) matches)]
;    (/ 
;      (+ (count correct-positives) (count correct-negatives))
;      (+ (count (:positives verifiedmatches)) (count (:negatives verifiedmatches))))))

;(defn- snippetgroup|hasequals?
;  "Count the number of directives used in a snippet group (excluding default directives)"
;  [snippetgroup]
;  (some (fn [snippet]
;          (boolean (directives/bounddirective-for-directive 
;                         (apply concat (snippet/snippet-bounddirectives snippet))
;                         matching/directive-equals)))
;         (snippetgroup/snippetgroup-snippetlist snippetgroup)))

;(defn
;  template-size
;  [templategroup]
;  (.length (persistence/snippetgroup-string templategroup)))

;(defn
;  ast-node-count
;  [templategroup]
;  (let [snippets (snippetgroup/snippetgroup-snippetlist templategroup)]
;    (apply + (for [x snippets]
;               (count (snippet/snippet-nodes x))))))


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
  (let [partial-matches (:done @matched-nodes)
        
;        (remove 
;          (fn [x] (= x node-count))
;          (:done @matched-nodes))
        ]
    (reset-matched-nodes)
    (if (empty? partial-matches)
      0
      (let [score (/ 
                    (reduce + (map (fn [x] (/ x node-count)) partial-matches))
                    (count partial-matches))
;            (/ (apply max partial-matches) node-count)
            ]
        (if (> score 1)
          (println "!Partial matching score cannot > 1" @matched-nodes)
          score)))))

(defn create-partial-model
  "Create a PartialJavaProjectModel such that only the ASTs of verifiedmatches are queried"
  [verifiedmatches]
  (let [partialmodel (new PartialJavaProjectModel)]
    (doseq [matchgroup (:positives verifiedmatches)]
      (doseq [match matchgroup]
        (.addExistingAST partialmodel match)))
    partialmodel))

(defn partial-matches
  [templategroup partialmodel]
  (binding [damp.ekeko.ekekomodel/*queried-project-models* (atom [partialmodel])
            matched-nodes (atom (MatchedNodes. #{} []))
            matching/*partial-matching* true]
    (try
      (templategroup-matches templategroup)
      (new-match)
      (partialmatch-score (count (snippetgroup/snippetgroup-nodes templategroup)))
;      (.clean partialmodel) ; Don't need this anymore..
      ; Hmm, seems that we're going over *all* nodes when generating queries? So partialscore goes up for children of e.g. a wildcard..
;      (partialmatch-score (reduce + (map 
;                                      (fn [snippet] (count (matching/reachable-nodes snippet (snippet/snippet-root snippet))))
;                                      (snippetgroup/snippetgroup-snippetlist templategroup))))
      (catch Exception e
        (println "Partial match failed!")
        0)
      )))

(defn
  make-fitness-function
  "Return a fitness function, used to measure how good/fit an individual is.
   A fitness function returns a pair: [overall-fitness fitness-components]
   ,where overall-fitness is a value between 0 (worst) and 1 (best)
   and fitness-components is a list of components that were used to compute the overall fitness"
  [verifiedmatches config]
  (let [partialmodel (create-partial-model verifiedmatches)]
    (fn [templategroup]
      (let [matches (util/with-timeout (:match-timeout config) (templategroup-matches templategroup) (:thread-group config)) 
            fscore (fmeasure matches verifiedmatches)
            partialscore (util/with-timeout (:match-timeout config) (partial-matches templategroup partialmodel) (:thread-group config))
            directive-count-score (directive-count-measure templategroup)
            
            ; If > 0, we have more false positives ; if < 0, we have more false negatives
            false-bias (- (count (falsep matches verifiedmatches)) (count (falsen matches verifiedmatches)))
            
            weights (:fitness-weights config)
            ;            dirscore (/ 1 (inc (* 1/2 (count-directives templategroup))))
            ;            lengthscore (/ 1 (template-size templategroup))
            ;            partialscore (- 1 (/ 1 (inc (partial-matches templategroup partialmodel))))
            ]
        [(+
           (* (nth weights 0) fscore)
           (* (nth weights 1) partialscore)
           (* (nth weights 2) directive-count-score)
           )
         [fscore partialscore directive-count-score false-bias]]))))