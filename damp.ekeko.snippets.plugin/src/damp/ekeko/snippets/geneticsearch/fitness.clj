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
             [astnode :as astnode]
             [rewrites :as rewrites]])
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

(defn templategroup-matches
  "Given a templategroup, look for all of its matches in the code
   (An exception is thrown if matching takes longer than timeout milliseconds..)"
  [templategroup timeout]
  (into #{}
        (util/with-timeout timeout 
          (eval (querying/snippetgroup-query|usingpredicates 
                  templategroup 'damp.ekeko/ekeko 
                  [`(damp.ekeko.logic/succeeds (do (new-match) true))]
                  '() true)))))

(defn 
  truep
  "True positives; how many results were correctly considered relevant"
  [matches verifiedmatches]
  (clojure.set/intersection matches (:positives verifiedmatches)))

(defn 
  falsep
  "False positives; how many results were incorrectly considered relevant"
  [matches verifiedmatches]
  (clojure.set/difference matches (:positives verifiedmatches)))

(defn
  falsen
  "False negatives; how many results were incorrectly considered irrelevant"
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

;(defn count-directives
;  "Count the number of directives used in a snippet group (excluding default directives)"
;  [snippetgroup]
;  (reduce + 
;          (for [snippet (snippetgroup/snippetgroup-snippetlist snippetgroup)]
;            (count (mapcat (fn [node] (matching/nondefault-bounddirectives snippet node)) (snippet/snippet-nodes snippet) )))))
;
;(defn directive-count-measure
;  "Produce a score in [0,1] to reflect how complex a template looks,
;   which is based on how many directives are used in the template"
;  [templategroup]
;  (/ 1 (inc (* 1/2 (count-directives templategroup)))))
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



(defrecord MatchedNodes
  [in-progress ; Set of nodes that have matched in the current attempt to match with a snippet
   done        ; List of how many nodes matched in the previous attempts
   ])

(def ^:dynamic matched-nodes (atom (MatchedNodes. #{} [])))

(defn partialmatch-score
  "Compute the partial matching score of the last templategroup that we tried to match
   @param node-count number of nodes in that last templategroup"
  [node-count]
  (let [partial-matches
        (remove 
          (fn [x] (= x node-count))
          (:done @matched-nodes))]
    (if (empty? partial-matches)
      0
      (/ (apply max partial-matches) node-count))))

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

;(def max-depth (atom 0))
;(defn set-depth [depth]
;  (el/succeeds (do
;                 (if (> depth @max-depth)
;                   (swap! max-depth (fn [x] depth))) 
;                 true)))
;(defn reset-max-depth []
;  (reset! max-depth 0))



(defn create-partial-model
  "Create a PartialJavaProjectModel such that only the ASTs of verifiedmatches are queried"
  [verifiedmatches]
  (let [partialmodel (new PartialJavaProjectModel)]
    (doseq [matchgroup (:positives verifiedmatches)]
      (doseq [match matchgroup]
        (.addExistingAST partialmodel match)))
    partialmodel))

(defn partial-matches
  [templategroup partialmodel timeout]
  (reset-matched-nodes)
;  (templategroup-matches templategroup timeout)
  (binding [damp.ekeko.ekekomodel/*queried-project-models* (atom [partialmodel])]
    (templategroup-matches templategroup timeout))
  (new-match)
  (partialmatch-score (count (snippetgroup/snippetgroup-nodes templategroup))))

(defn
  make-fitness-function
  "Return a fitness function, used to measure how good/fit an individual is.
   A fitness function returns a pair: [overall-fitness fitness-components]
   ,where overall-fitness is a value between 0 (worst) and 1 (best)
   and fitness-components is a list of components that were used to compute the overall fitness"
  [verifiedmatches config]
  (let [partialmodel (create-partial-model verifiedmatches)]
    (fn [templategroup]
      (try
        (let [matches (templategroup-matches templategroup (:match-timeout config))
              fscore (fmeasure matches verifiedmatches)
              ;partialscore 0
              partialscore (partial-matches templategroup partialmodel (:match-timeout config))
              weights (:fitness-weights config)
              ;            dirscore (/ 1 (inc (* 1/2 (count-directives templategroup))))
              ;            lengthscore (/ 1 (template-size templategroup))
              ;            partialscore (- 1 (/ 1 (inc (partial-matches templategroup partialmodel))))
              ]
          [(+
             (* (nth weights 0) fscore)
             (* (nth weights 1) partialscore))
           [fscore partialscore]])
        (catch Exception e
          (let [id (util/current-time)]
            (print "!")
            (persistence/spit-snippetgroup (str "error" id ".ekt") templategroup)
            (util/log "error"
                      (str 
                        "!!!" id "---" e
                        "\nTemplate\n"
                        (persistence/snippetgroup-string templategroup)
                        "\nStacktrace\n"
                        (.toString (.printStackTrace e (new java.io.StringWriter)))
                        "-----\n\n"))
            0))))))