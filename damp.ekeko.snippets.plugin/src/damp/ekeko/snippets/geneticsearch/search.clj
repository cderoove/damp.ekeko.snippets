(ns 
  ^{:doc "(Genetic) search for template specifications."
  :author "Coen De Roover, Tim Molderez"}
  damp.ekeko.snippets.geneticsearch.search
  (:refer-clojure :exclude [rand-nth rand-int rand])
  (:import 
    [damp.ekeko JavaProjectModel]
    [org.eclipse.jface.text Document]
    [org.eclipse.text.edits TextEdit]
    [org.eclipse.jdt.core ICompilationUnit IJavaProject]
    [org.eclipse.jdt.core.dom BodyDeclaration Expression Statement ASTNode ASTParser AST CompilationUnit]
    [org.eclipse.jdt.core.dom.rewrite ASTRewrite])
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko]
            [damp.ekeko.jdt
             [astnode :as astnode]
             [rewrites :as rewrites]])
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
             [transformation :as transformation]])
  (:import [ec.util MersenneTwister]
           [damp.ekeko.snippets.geneticsearch PartialJavaProjectModel]))

(defmacro dbg[x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))

; Replace Java's pseudo-random number generator for MersenneTwister
(def ^:dynamic *twister* (MersenneTwister.)) ;TODO: use binding to rebind per-thread in different places of the search algo
(defn 
  rand
  ([n] (.nextInt *twister* n)))
(defn
  rand-int
  [n]
  (int (rand n)))
(defn 
  rand-nth
  [coll]
  (nth coll (rand-int (count coll))))

;; Problem representation
;; State corresponds to templategroup (can use sequence of operators later, problem is that there arguments cannot easily cross over)

; Specifies our "oracle" for testing the fitness of templates produced by our search algorithm
; The entries in :positives are snippet groups that a template must match.
; The entries in :negatives are snippet group that a template may not match.
(defrecord
  VerifiedMatches
  [positives negatives])

(defn
  make-verified-matches
  "Create a record of verified matches, consisting of a number of positive and negative matches.
   The positive ones are those that the resulting template must match;
   the negative one are those it may not match."
  [positives negatives]
  (VerifiedMatches. (into #{} positives)
                    (into #{} negatives)))

;http://stackoverflow.com/questions/6694530/executing-a-function-with-a-timeout/6697469#6697469
(defmacro with-timeout [millis & body]
  `(let [future# (future ~@body)]
     (try
       (.get future# ~millis java.util.concurrent.TimeUnit/MILLISECONDS)
       (catch java.util.concurrent.TimeoutException x# 
         (do
           (println "Timed out!!")
           (future-cancel future#)
           nil)))))

(defn
  templategroup-matches-nomemo
  "Given a templategroup, look for all of its matches in the code; no memoization."
  [templategroup]
  (into #{} (with-timeout 10000 (eval (querying/snippetgroup-query|usingpredicates templategroup 'damp.ekeko/ekeko true)))))

;;;MAGIC CONSTANT! timeout for matching of individual snippet group
(def
  templategroup-matches
  "Given a templategroup, look for all of its matches in the code"
  (clojure.core.memoize/memo 
    (fn [templategroup]
      (templategroup-matches-nomemo templategroup)
;      (into #{} (with-timeout 10000 (eval (querying/snippetgroup-query|usingpredicates templategroup 'damp.ekeko/ekeko true))))
      )))



; (Using the picture on http://en.wikipedia.org/wiki/Precision_and_recall as a reference here... )
; There's an important nuance to consider here! What about the results that are neither in :positives or :negatives .. the results in the gray zone?
; Do we consider them relevant, or irrelevant? (i.e. is it okay to match too much .. as long as it's not in :negatives
; .. or should such matches be rejected too?)
; They're considered irrelevant now.. 

; Actually, in this case the notion of :negatives isn't of much use.. 
; If you don't want a particular pattern to match, it simply shouldn't be in positives..

(defn 
  truep
  "True positives; how many results were correctly considered relevant"
  [matches verifiedmatches]
  (clojure.set/intersection matches (:positives verifiedmatches))
  ; (clojure.set/difference matches (:negatives verifiedmatches)) ; If gray zone is considered relevant 
  )

(defn 
  falsep
  "False positives; how many results were incorrectly considered relevant"
  [matches verifiedmatches]
  (clojure.set/difference matches (:positives verifiedmatches))
  ;(clojure.set/intersection matches (:negatives verifiedmatches))) ; If gray zone is considered relevant
  )

(defn
  falsen
  "False negatives; how many results were incorrectly considered irrelevant"
  [matches verifiedmatches]
  (clojure.set/difference (:positives verifiedmatches) matches))
  ; Hmm.. if gray zone were considered relevant, falsen always is infinity as the number of relevant stuff is infinite..
  
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
    (/ ctp (+ ctp cfn))))
  
(defn
  fmeasure
  "Calculate the F-measure/F1-score by comparing the matches produced by an individual
   to the matches we actually want
   @param matches  the matches of an individual
   @param verifiedmatches  the matches we want"
  [matches verifiedmatches]
  (let [p (precision matches verifiedmatches)
        r (recall matches verifiedmatches)]
    (if 
      (= (+ p r) 0)
      0
      (* 2 (/ (* p r) (+ p r))))))

(defn count-directives
  "Count the number of directives used in a snippet group (excluding default directives)"
  [snippetgroup]
  (reduce + 
          (for [snippet (snippetgroup/snippetgroup-snippetlist snippetgroup)]
            (count (mapcat (fn [node] (matching/nondefault-bounddirectives snippet node)) (snippet/snippet-nodes snippet) )))))

(defn directive-count-measure
  "Produce a score in [0,1] to reflect how complex a template looks,
   which is based on how many directives are used in the template"
  [templategroup]
  (/ 1 (inc (* 1/2 (count-directives templategroup)))))

(defn simple-measure
  "Alternative to fmeasure, in which we simply don't care about the matches that are neither in :positives or :negatives.
   Produces a number in [0-1], such that higher means more correct (positive or negative) results"
  [matches verifiedmatches]
  (let [correct-positives (clojure.set/intersection matches (:positives verifiedmatches))
        correct-negatives (clojure.set/difference (:negatives verifiedmatches) matches)]
    (/ 
      (+ (count correct-positives) (count correct-negatives))
      (+ (count (:positives verifiedmatches)) (count (:negatives verifiedmatches))))))

(defn- snippetgroup|hasequals?
  "Count the number of directives used in a snippet group (excluding default directives)"
  [snippetgroup]
  (some (fn [snippet]
          (boolean (directives/bounddirective-for-directive 
                         (apply concat (snippet/snippet-bounddirectives snippet))
                         matching/directive-equals)))
         (snippetgroup/snippetgroup-snippetlist snippetgroup)))

(def operator-directive
  "Retrieve the directive that will be created by an operator
   !! For now, this relies on the naming convention that the operator's id starts with 'add-'
   , followed by the directive's name.."
  (clojure.core.memoize/memo
    (fn [operator-id] 
      (try
        (let [directive-func-name (subs operator-id 4)]
         (eval (read-string (str "matching/" directive-func-name))))
        (catch Exception e nil))))
  ;  (cond
  ;    "replace-by-variable" matching/directive-replacedbyvariable
  ;    "replace-by-exp" matching/directive-replacedbyexp
  ;    "add-directive-equals" matching/directive-equals
  ;    "add-directive-invokes" matching/directive-invokes
  ;    
  ;    :rest nil)
  )

(defn
  template-size
  [templategroup]
  (.length (persistence/snippetgroup-string templategroup)))

(defn
  ast-node-count
  [templategroup]
  (let [snippets (snippetgroup/snippetgroup-snippetlist templategroup)]
    (apply + (for [x snippets]
               (count (snippet/snippet-nodes x))))))

(defn create-partial-model
  "Create a PartialJavaProjectModel such that only the ASTs of verifiedmatches are queried"
  [verifiedmatches]
  (let [partialmodel (new PartialJavaProjectModel)]
    (doseq [matchgroup (:positives verifiedmatches)]
      (doseq [match matchgroup]
        (.addExistingAST partialmodel match)))
    partialmodel))

(def partial-matches
  (clojure.core/memoize
    (fn [templategroup partialmodel]
      (matching/reset-matched-nodes)
      (binding [damp.ekeko.ekekomodel/*queried-project-models* (atom [partialmodel])]
        (templategroup-matches-nomemo templategroup))
      (count @matching/matched-nodes))))

(defn
  make-fitness-function
  "Return a fitness function, used to measure how good/fit an individual is
   0 is worst fitness; 1 is the best"
  [verifiedmatches]
  (let [partialmodel (create-partial-model verifiedmatches)]
    (fn [templategroup]
    (try
      (let [matches (templategroup-matches templategroup)
            ; The more desired matches, the better
            fscore (fmeasure matches verifiedmatches)
;            fscore (/ 
;                     (count (truep matches verifiedmatches)) 
;                     (count (:positives verifiedmatches)))
            ; The fewer directives, the better
            dirscore (/ 1 (inc (* 1/2 (count-directives templategroup))))
            ; The shorter a template-group, the better
            lengthscore (/ 1 (template-size templategroup))
            ; The more ast-relations succeed in the underlying Ekeko-query, the better
;            partialscore (- 1 (/ 1 (inc (partial-matches templategroup partialmodel))))
            ]
        (if (= 0 fscore)
          0
          (+
            (* 20/20 fscore)
            (* 0/20 dirscore)
            (* 0/20 lengthscore)
;            (* 2/20 partialscore)
            )))
      (catch Exception e
        (do
          (print "!")
          (spit "error-log.txt" 
                (str "!!!" e
                     "\nTemplate\n"
                     (persistence/snippetgroup-string templategroup)
                     "Last operation applied:" 
                     (:mutation-operator (meta templategroup))
                     "--------\n\n") 
                :append true)
;          (jay/inspect e)
;          (jay/inspect [e templategroup (querying/snippetgroup-query|usingpredicates templategroup 'damp.ekeko/ekeko true)])
          0))))
    
    
    
    )
  
  )


;; Search

;;assumes: arity of tuple corresponds to number of templategroups required
(defn
  templategroup-from-tuple
  ([tuple]
    (templategroup-from-tuple tuple "A population member"))
  ([tuple name]
    (snippetgroup/make-snippetgroup name
                                    (map matching/snippet-from-node tuple))))

(defn
  population-from-tuples
  "Generate an initial population of templates based on the desired matches"
  [matches]
  (let [id-templates 
        (map-indexed
          (fn [idx tuple] 
            (templategroup-from-tuple tuple (str "Offspring of tuple " idx)))
          matches)]
    (mapcat identity (repeat 1 id-templates))
;    (concat id-templates
            ;[(persistence/slurp-from-resource "/resources/EkekoX-Specifications/invokedby.ekt")]
;            (util/viable-repeat 
;              (* 1 (count id-templates)) 
;              #(mutate (select id-templates 2)) 
;              (fn [templategroup]
;                (try
;                  (let [matches (templategroup-matches templategroup)]
;                    (pos? (count matches)))
;                  (catch Exception e
;                    false))
;                ))
;            )
    ))


(def
  registered-operators|search
  (filter (fn [op] 
            (let [id (operatorsrep/operator-id op)]
              (some #{id} 
                    [
                     "replace-by-variable"
                     "replace-by-wildcard"
                     "remove-node"
                     
                     "add-directive-equals"

                     "add-directive-invokes"
                     "add-directive-invokedby"
                     
                     "restrict-scope-to-child"
                     "relax-scope-to-child+"
                     "relax-scope-to-child*"
                     "relax-size-to-atleast"
                     "relax-scope-to-member"
                     "consider-set|lst"
                     "add-directive-type"
                     "add-directive-type|qname"
                     "add-directive-type|sname"
                     "add-directive-refersto"
                     
;                     ;untested:
;                     ;"replace-parent"
;                     "erase-comments"
                     ]
                    )))
          (operatorsrep/registered-operators)))

(defn- rand-snippet [snippetgroup]
  (-> snippetgroup
    snippetgroup/snippetgroup-snippetlist
    rand-nth))

(defn
  mutate
  "Perform a mutation operation on a snippet group. A random node is chosen among the snippets,
   and a random operation is applied to it, in order to mutate the snippet."
  [snippetgroup]
  (let [group-copy (persistence/copy-snippetgroup snippetgroup)
        snippet (rand-snippet group-copy)
        
        pick-operator
        (fn []
          (let [operator (rand-nth registered-operators|search)
                ; Pick an AST node that the chosen operator can be applied to
                all-valid-nodes (filter
                                  (fn [node] 
                                    (and (operatorsrep/applicable? snippetgroup snippet node operator)
                                         ; Check that you haven't already applied this operation to this node..
                                         (not (boolean
                                                (directives/bounddirective-for-directive
                                                  (snippet/snippet-bounddirectives-for-node snippet node)
                                                  (operator-directive (operatorsrep/operator-id operator)))))))
                                  (snippet/snippet-nodes snippet)
;                                  (filter
;                                    (fn [node] (astnode/ast? node))
;                                    (snippet/snippet-nodes snippet))
                                  )
                ]
            (if (empty? all-valid-nodes)
              (recur)
              [operator (rand-nth all-valid-nodes)])))
        
        [operator value] (pick-operator)
        operands (operatorsrep/operator-operands operator)
        
        operandvalues
        (map
          (fn [operand]
            (rand-nth
              (operatorsrep/possible-operand-values|valid
                    group-copy snippet value operator operand)))
          operands)
        
        bindings
        (cons
          (operatorsrep/make-implicit-operandbinding-for-operator-subject group-copy snippet value operator)
          (map (fn [operand operandval]
                 (operatorsrep/make-binding operand group-copy snippet operandval))
               operands
               operandvalues))]
    (with-meta
      (operatorsrep/apply-operator-to-snippetgroup group-copy snippet value operator bindings)
      {:mutation-operator (operatorsrep/operator-id operator)
       :mutation-node value
       :mutation-opvals operandvalues})))

(defn- node-expected-class
  "Returns the expected type of an ASTnode, more specifically, the type that the parent node expects.
   This is done by looking at the child type in the property descriptor of the parent node."
  [node]
  (let [pd (astnode/owner-property node)]
    (cond 
      (astnode/property-descriptor-simple? pd) (astnode/property-descriptor-value-class pd)
      (astnode/property-descriptor-child? pd) (astnode/property-descriptor-child-node-class pd)
      (astnode/property-descriptor-list? pd) (astnode/property-descriptor-element-node-class pd)
      :else (throw (Exception. "Should not happen.")))))

(defn- non-root-ast-node?
  [node snippet]
  (and 
    (astnode/ast? node) ; Because some nodes aren't AST nodes but property descriptors..
    (not= node (snippet/snippet-root snippet))))

(defn- rand-ast-node
  "Return a random AST node (that is not the root) in a snippet"
  [snippet]
  (let [node (rand-nth (snippet/snippet-nodes snippet))]
    (if (non-root-ast-node? node snippet)
      node
      (recur snippet))))

(defn- find-compatible-ast-pair
  "Given two snippets, find a pair of nodes in each snippet's AST
   such that they can be safely swapped without causing syntax issues"
  [snippet1 snippet2]
  (let [node1 (rand-ast-node snippet1)
        cls1 (node-expected-class node1)
        ; Look for all compatible nodes in snippet2 that could be swapped with node1 
        compatible-node2s (filter
                            (fn [node2]
                              (and
                                (non-root-ast-node? node2 snippet2)
                                (instance? cls1 node2)
                                (instance? (node-expected-class node2) node1)))
                            (snippet/snippet-nodes snippet2))]
    (if (empty? compatible-node2s)
      (recur snippet1 snippet2)
      [node1 (rand-nth compatible-node2s)])))

(defn
  replace-node-with
  "Replaces a node within a snippet with another node from another snippet
   @author Tim"
  [destination-snippet destination-node source-snippet source-node]
  (let [copy-of-source-node
        (ASTNode/copySubtree (.getAST destination-node) source-node) ;copy to ensure ASTs are compatible
        newsnippet 
        (atom destination-snippet)] 
    ; dissoc destination-node and children in destination-snippet
    (snippet/walk-snippet-element
      destination-snippet 
      destination-node
      (fn [val] (swap! newsnippet matching/remove-value-from-snippet val)))
    
    ; do replacement of destination-node by source-node in the actual AST
    (operators/snippet-jdt-replace  
      @newsnippet
      destination-node
      copy-of-source-node)
    
    ;assoc copy-of-source-node and children with default directives in destination-snippet
    (util/walk-jdt-node 
      copy-of-source-node
      (fn [val] (swap! newsnippet matching/add-value-to-snippet val)))
    
    ;update new-node and children with 
    (snippet/walk-snippets-elements
      @newsnippet
      copy-of-source-node
      source-snippet
      source-node
      (fn [[destval srcval]] 
        (let [srcbds
              (snippet/snippet-bounddirectives-for-node source-snippet srcval) 
              destbds
              (map
                (fn [bounddirective]
	                 (directives/make-bounddirective
                    (directives/bounddirective-directive bounddirective)
                    (cons 
                      (directives/make-implicit-operand destval)
                      (rest (directives/bounddirective-operandbindings bounddirective)))))
                   srcbds)]
          (swap! newsnippet snippet/update-bounddirectives  destval destbds))))
    @newsnippet))

(defn
  crossover
  "Performs a crossover between two snippets:
   This means that two AST nodes are chosen at random, and both nodes (and their children) will be swapped.
   (In case this operation leads to invalid syntax, we try again..)
   Returns a vector containing the two crossed-over snippets"
  [snippetgroup1 snippetgroup2]
  (let
    [group-copy1 (persistence/copy-snippetgroup snippetgroup1)
     group-copy2 (persistence/copy-snippetgroup snippetgroup2)
     ; Get two random snippets
     snippet1 (rand-snippet group-copy1)
     snippet2 (rand-snippet group-copy2)
     ; Get two random AST nodes
     node-pair (find-compatible-ast-pair snippet1 snippet2)
     node1 (first node-pair)
     node2 (second node-pair) 
     new-snippet1 (replace-node-with snippet1 node1 snippet2 node2)
     new-snippet2 (replace-node-with snippet2 node2 snippet1 node1)]
    [(with-meta 
       (snippetgroup/snippetgroup-replace-snippet group-copy1 snippet1 new-snippet1)
       {:crossover-old node1 :crossover-new node2})
     (with-meta
       (snippetgroup/snippetgroup-replace-snippet group-copy2 snippet2 new-snippet2)
       {:crossover-old node2 :crossover-new node1})]))

(defn
  sort-by-fitness
  "Sort the individuals in a population (from worst to best) using the fitness function"
  [population fitnessf]
  (sort-by (fn [templategroup]
             (fitnessf templategroup))
           population))

(defn 
  select
  "Do tournament selection in a population:
   Given that the population is sorted from best fitness to worst, we pick a number 
   of random entries in the population, then return the best one from those entries.
   @param tournament-size  The number of random entries to pick"
  [population tournament-size]
  (let [size (count population)]
    (nth population
         (apply max (repeatedly tournament-size #(rand-int size))))))

(defn- correct-implicit-operands?
  [snippetgroup]
  (reduce (fn [sofar node]
            (and 
              sofar
              (every? (fn [bd]
                        (let [implOp (first (.getOperandBindings bd))]
                          (= (.getValue implOp) node)))
                      (snippet/snippet-bounddirectives-for-node node))))
          true
          (mapcat snippet/snippet-nodes snippetgroup)))

(defn
  evolve
  "Look for a template that is able to match with a number of snippets, using genetic search
   @param verifiedmatches  There are the snippets we want to match with (@see make-verified-matches)
   @param max-generations  Stop searching if we haven't found a good solution after this number of generations"
  [verifiedmatches max-generations]
  (let
    [fitness (make-fitness-function verifiedmatches) ;(clojure.core.memoize/memo (make-fitness-function verifiedmatches))
     popsize (count verifiedmatches)
     initial-pop (sort-by-fitness (population-from-tuples (:positives verifiedmatches)) fitness)
     tournament-size 2] 
    (loop 
      [generation 0
       population initial-pop
       history #{}]
      (let [best (last population)
            best-fitness (fitness best)
            is-viable (fn [individual]
                        (and  
                          ; We ignore the individuals we've seen before
                          (not (contains? history (hash individual)))
                          ; .. and those with fitness 0
                          (pos? (fitness individual))))
            new-history (clojure.set/union
                          history
                          (set (map hash population)))]
;        (doseq [individual population]
;          (assert (correct-implicit-operands? individual)))
        (println "Generation:" generation)
        (println "Highest fitness:" best-fitness)
        (println "Fitnesses:" (map fitness population))
        (println "Best specification:" (persistence/snippetgroup-string best))
        ; Show detailed population info
;        (jay/inspect (map 
;                       (fn [individual]
;                         [(snippetgroup/snippetgroup-name individual)
;                          (fitness individual)
;                          (meta individual)
;                          ;(snippetgroup/snippetgroup-snippetlist individual)
;                          (persistence/snippetgroup-string individual)]) 
;                       population))
        (when (< generation max-generations)
          (if
            (> best-fitness 0.95)
            (println "Success:" (persistence/snippetgroup-string best))
            (recur 
              (inc generation)
              (sort-by-fitness
                ; Produce the next generation using mutation, crossover and tournament selection
                (concat
                  ; Random reselect from initial population
                  (util/viable-repeat
                    (* 0/8 (count population))
                    #(rand-nth initial-pop)
                    (fn [x] true))
                  ; Mutation
                  (util/viable-repeat 
                    (* 3/4 (count population))
                    #(mutate (select population tournament-size)) 
                    is-viable)
                  ; Crossover (Note that each crossover operation produces a pair)
                  (apply concat
                         (util/viable-repeat 
                           (* 0/8 (count population))
                           #(crossover
                              (select population tournament-size)
                              (select population tournament-size))
                           (fn [x] (and (is-viable (first x)) (is-viable (second x))))))
                  ; Selection
                  (util/viable-repeat 
                    (* 1/4 (count population)) 
                    #(select population tournament-size) 
                    (fn [x] true)))
                fitness)
              new-history)))))))

;; todo: applicable for equals: bestaande vars (of slechts 1 nieuwe)
;; todo: gewone a* search  

(comment
  (def templategroup
    (persistence/slurp-from-resource "/resources/EkekoX-Specifications/invokedby.ekt"))
  (def matches (templategroup-matches templategroup))
  (def verifiedmatches (make-verified-matches matches []))
  (evolve verifiedmatches 10)
  
  ; SCAM 2014 test begin
  ; scam_demo1 is easy peasy! Now for demo2; mkay easy enough too.. ; now for the big one
  (def templategroup
    (transformation/transformation-lhs (persistence/slurp-from-resource "/resources/EkekoX-Specifications/scam_demo3.ekx")))
  (def matches (templategroup-matches templategroup))
  (def verifiedmatches (make-verified-matches matches []))
  (evolve verifiedmatches 0)
  ; end
  
  
  (persistence/snippetgroup-string templategroup)
  (clojure.pprint/pprint (querying/snippetgroup-query|usingpredicates templategroup 'damp.ekeko/ekeko true))
  
  (= 1 (precision matches verifiedmatches))
  (= 1 (recall matches verifiedmatches))
  (fmeasure matches verifiedmatches)
  ((make-fitness-function verifiedmatches) matches)
  
  (pmap (make-fitness-function verifiedmatches) (population-from-tuples matches))
  
  ;MethodDeclaration - MethodInvocation (vars sorted .. cannot compare otherwise)
  (map (fn [tuples] (map (fn [tuple] (map class tuple)) tuples))
       (map templategroup-matches (population-from-tuples matches)))
  
  ; Testing crossover
  (def m1
    (persistence/slurp-from-resource "/resources/EkekoX-Specifications/m1.ekt"))
  (def m2
    (persistence/slurp-from-resource "/resources/EkekoX-Specifications/m2.ekt"))
  
  (println (persistence/snippetgroup-string m1))
  (println (persistence/snippetgroup-string m2))
  
  (def match1 
    (templategroup-matches m1))
  (def match2 
    (templategroup-matches m2))
  
  ; Testing mutation
  (persistence/snippetgroup-string (mutate m1))
  
  ; Testing crossover
  (doseq [x (range 0 1)]
    (let [[x1 x2] (crossover m1 m2)
          x1-match (templategroup-matches x1)]
      (println "---" (meta x1))
      
      (if (not (empty? x1-match))
        (do
          (println x1-match)
          ;          (println (persistence/snippetgroup-string x1))
          ;          (jay/inspect x1)
          (println "!")))
      
      (assert (correct-implicit-operands? x1))
      (assert (correct-implicit-operands? x2))))
  
  ; Testing filtered Ekeko queries, where we (temporarily) only query certain AST subtrees
  (let [tg (persistence/slurp-from-resource "/resources/EkekoX-Specifications/invokedby.ekt")
        matches (templategroup-matches tg) 
        partialmodel (new PartialJavaProjectModel)
        ]
    (.addExistingAST partialmodel (first (first matches)))
    
    (binding [damp.ekeko.ekekomodel/*queried-project-models* (atom [partialmodel])]
      (damp.ekeko/ekeko [?cu] (damp.ekeko.jdt.ast/ast :Statement ?cu))))
  
  (damp.ekeko.snippets.matching/reset-max-depth)
  (damp.ekeko.snippets.matching/reset-matched-nodes)
  (def templategroup
    (persistence/slurp-from-resource "/resources/EkekoX-Specifications/invokedby.ekt"))
;  (clojure.pprint/pprint (querying/snippetgroup-query|usingpredicates templategroup 'damp.ekeko/ekeko true))
  (templategroup-matches templategroup)  
  (count @damp.ekeko.snippets.matching/matched-nodes)
  
  )