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
             [rewrites :as rewrites]
             [transformation :as transformation]])
  (:require [damp.ekeko.snippets.geneticsearch 
             [individual :as individual]
             [fitness :as fitness]])
  (:import [damp.ekeko.snippets.geneticsearch.fitness MatchedNodes])
  (:import [ec.util MersenneTwister]
           [damp.ekeko.snippets.geneticsearch PartialJavaProjectModel]
           [damp.ekeko.jdt.astnode EkekoAbsentValueWrapper]))

; Replace Java's pseudo-random number generator for MersenneTwister
(def ^:dynamic *twister* (MersenneTwister.)) ;TODO: use binding to rebind per-thread in different places of the search algo
(defn rand
  ([n] (.nextInt *twister* n)))
(defn rand-int [n]
  (int (rand n)))
(defn rand-nth [coll]
  (nth coll (rand-int (count coll))))

(def
  ^{:doc "List of all possible mutation operators"}
  registered-operators|search
  (filter 
    (fn [op] 
      (some #{(operatorsrep/operator-id op)} 
            [
             "replace-by-variable"
             "replace-by-wildcard"
;             "remove-node"
             "add-directive-equals"
             "add-directive-invokes"
;             "add-directive-invokedby"
;             "restrict-scope-to-child"
;             "relax-scope-to-child+"
             "relax-scope-to-child*"
;             "relax-size-to-atleast"
;             "relax-scope-to-member"
             "consider-set|lst"
             "add-directive-type"
             "add-directive-subtype*"
;             "add-directive-type|qname"
;             "add-directive-type|sname"
             "add-directive-refersto"
;             "erase-list"

;             "replace-parent"
;             "erase-comments"

             "add-directive-constructs"
;             "add-directive-constructedby"
             "add-directive-overrides"
;             "generalize-directive"
;             "remove-directive"
;             "extract-template" ; ! Don't use this for genetic search, as it expects a certain number of templates in a templategroup/individual
             "generalize-references"
             "generalize-types"
;             "generalize-types|qname"
;             "generalize-invocations" ; Bug?
;             "generalize-constructorinvocations"
;             "isolate-stmt-in-method"
             "isolate-expr-in-method"
;             "isolate-stmt-in-block"
             ]))
    (operatorsrep/registered-operators)))

(defn all-operator-ids []
  (sort (map
          (fn [op] (operatorsrep/operator-id op))
          (operatorsrep/registered-operators))))

(def
  ^{:doc "Default configuration options in the genetic search algorithm"}
  config-default
  {:max-generations 1200
   :population-size 30
   :initial-population nil ; If nil, the initial population is generated from the verified matches
   
   :selection-weight 1/4
   :mutation-weight 3/4
   :crossover-weight 0/4
   
   :fitness-function fitness/make-fitness-function
   :fitness-weights [12/20 8/20 0/20]
   :fitness-threshold 0.95
   :fitness-filter-comp 0 ; This is the index of the fitness component that must be strictly positive; otherwise the individual will be filtered out. If -1, the overall fitness must be positive.
   
   :parallel-individuals :reducer ; Generate + test the fitness of each individual in a generation in parallel (either :sequential, :partitioned or :reducer)
   :parallel-individuals-threads 8
   :parallel-matching :reducer ; Process each potential match in parallel (either :sequential, :partitioned or :reducer)
   :parallel-matching-threads 8
   
   :output-dir nil
   :partial-matching true
   :quick-matching false ; If enabled, template matching only considers the classes occuring in verified matches. Matching will be much faster, but the resulting templates can produce false positives.
   :match-timeout 960000 ; DEPRECATED
   :thread-group nil ; DEPRECATED
   :tournament-rounds 7
   :mutation-operators registered-operators|search
   :gui-editor nil ; If set to a RecommendationEditor instance, the results of each generation are pushed to this GUI component
   })

(defrecord
  ^{:doc "Specifies the 'oracle' for testing the fitness of templates produced by our genetic search algorithm
          The entries in :positives are snippets that a template must match, whereas
          the entries in :negatives may not match."}
  VerifiedMatches
  [positives negatives])

(defn make-verified-matches
  [positives negatives]
  (VerifiedMatches. (into #{} positives)
                    (into #{} negatives)))

(defn
  individual-from-snippet
  ([snippet]
    (individual-from-snippet snippet "A population member"))
  ([snippet name]
    (individual/make-individual
      (snippetgroup/make-snippetgroup 
       name (map matching/snippet-from-node snippet))
      {:id (str (gensym "0"))})))

(defn
  population-from-snippets
  "Generate an initial population of individuals based on the desired matches
   (In case population-size is larger than the number of matches,
    we cycle through the matches again until the population is filled..)"
  [matches population-size]
  (assert (not (empty? matches)) "Empty initial population! Is Ekeko enabled on the right project?")
  (let [id-templates 
        (map-indexed
          (fn [idx snippet] 
            (individual-from-snippet snippet (str "Offspring of snippet " idx)))
          matches)]
    (for [x (range 0 population-size)]
      (nth id-templates (mod x (count matches))))))

(defn
  population-from-templates
  "Generate an initial population of individuals based on the desired templates
   (In case population-size is larger than the number of matches,
    we cycle through the matches again until the population is filled..)"
  [templates population-size]
  (let [id-templates (map
                       (fn [template]
                         (individual/make-individual template {:id (str (gensym "0"))}))
                       templates)]
    (for [x (range 0 population-size)]
      (nth id-templates (mod x (count templates))))))

(defn spit-templategroup-matches
  "Find all matches of a templategroup, and store each one as an .ekt file in the given directory"
  [templategroup output-dir]
  (let [matches (fitness/templategroup-matches templategroup)]
    (util/make-dir output-dir)
    (map-indexed
      (fn [idx match]
        (println (str "Writing " output-dir "/" idx ".ekt"))
        (persistence/spit-snippetgroup (str output-dir "/" idx ".ekt")
                                       (operatorsrep/preprocess-templategroup
                                         (snippetgroup/make-snippetgroup 
                                          "Exported matches"
                                          (map matching/snippet-from-node match)))))
      matches)))

(defn- rand-snippet [snippetgroup]
  (-> snippetgroup
    snippetgroup/snippetgroup-snippetlist
    rand-nth))

(defn operator-directive
  "Retrieve the directive that will be created by an operator"
  [operator-id]
  (case operator-id
    ; these exceptions don't follow the "add-directive-" naming convention
    "relax-scope-to-child+" matching/directive-child+
    "relax-scope-to-child*" matching/directive-child*
    "restrict-scope-to-child" matching/directive-child
    "relax-size-to-atleast" matching/directive-size|atleast
    "empty-body" matching/directive-emptybody
    "relax-scope-to-member" matching/directive-member
    "consider-set|lst" matching/directive-consider-as-set|lst
    
    ; Gross hack; FIXME later!!
    "generalize-types" matching/directive-type
    "generalize-references" matching/directive-replacedbyvariable
    "generalize-constructorinvocations" matching/directive-constructs
    ; default case; we rely on the naming convention that operator id starts with "add-directive-"
    (try
      (let [directive-func-name (subs operator-id 4)]
        (eval (read-string (str "matching/" directive-func-name))))
      (catch Exception e nil))))

(defn- gen-operand-values
  "Generates valid operand values for a given template group
   Returns a pair with the operand values, and an updated template group
   (In some cases we will add an equals directive to match an operand value, which is why the template group may be modified.)"
  [tgroup operator types]
  (let [opnodes-per-snippet ; Possible operand nodes found in each snippet 
        (remove
          (fn [[snip nodes]] (empty? nodes))
          (for [snip (snippetgroup/snippetgroup-snippetlist tgroup)]
            [snip (matching/reachable-nodes-of-type snip (snippet/snippet-root snip) types)]))]
    (if (empty? opnodes-per-snippet)
      [[] tgroup]
      (let [[snip opnodes] (rand-nth opnodes-per-snippet)
            opnode (rand-nth opnodes)
            
            ; Now check whether opnode is replaced by a metavar, or has an equals directive
            replacement-var (matching/snippet-replacement-var-for-node snip opnode)
            eq-var (matching/snippet-equals-var-for-node snip opnode)
            
            node-var (cond
                       (not (nil? replacement-var)) replacement-var
                       (not (nil? eq-var)) eq-var
                       :else (str (util/gen-lvar)))
            
            new-tgroup (if (and (nil? replacement-var) (nil? eq-var))
                         (let [new-snip (operators/add-directive-equals snip opnode (str node-var))]
                           (snippetgroup/replace-snippet tgroup snip new-snip))
                         tgroup)]
        [[[node-var]] new-tgroup]))))

(defn- gen-operand-values2
  "Variant for subtype!! TODO Clean this duplicated mess up later..
   Generates valid operand values for a given template group
   Returns a pair with the operand values, and an updated template group
   (In some cases we will add an equals directive to match an operand value, which is why the template group may be modified.)"
  [tgroup operator types]
  (let [opnodes-per-snippet ; Possible operand nodes found in each snippet 
        (remove
          (fn [[snip nodes]] (empty? nodes))
          (for [snip (snippetgroup/snippetgroup-snippetlist tgroup)]
            [snip (filter 
                    (fn [node]
                      (if (= :SimpleName (astnode/ekeko-keyword-for-class-of node))
                        (let [parent (snippet/snippet-node-parent|conceptually snip node)]
                          (if (not (nil? parent))
                            (= :TypeDeclaration (astnode/ekeko-keyword-for-class-of parent))
                            false
                            ))
                        true)
                      )
                    (matching/reachable-nodes-of-type snip (snippet/snippet-root snip) types))]))]
    (if (empty? opnodes-per-snippet)
      [[] tgroup]
      (let [[snip opnodes] (rand-nth opnodes-per-snippet)
            opnode (rand-nth opnodes)
            
            ; Now check whether opnode is replaced by a metavar, or has an equals directive
            replacement-var (matching/snippet-replacement-var-for-node snip opnode)
            eq-var (matching/snippet-equals-var-for-node snip opnode)
            
            node-var (cond
                       (not (nil? replacement-var)) replacement-var
                       (not (nil? eq-var)) eq-var
                       :else (str (util/gen-lvar)))
            
            new-tgroup (if (and (nil? replacement-var) (nil? eq-var))
                         (let [new-snip (operators/add-directive-type snip opnode (str node-var))]
                           (snippetgroup/replace-snippet tgroup snip new-snip))
                         tgroup)]
        [[[node-var]] new-tgroup]))))

(defn
  mutate
  "Perform a mutation operation on a template group. A random node is chosen among the snippets,
   and a random operation is applied to it, in order to mutate the snippet."
  [individual operators]
  (let [snippetgroup (individual/individual-templategroup individual)
        group-copy (persistence/copy-snippetgroup snippetgroup)
;        operator-bias (nth (individual/individual-fitness-components individual) 3)
        
        snippetno (rand-nth (range 0 (count (snippetgroup/snippetgroup-snippetlist group-copy))))
        snippet (nth (snippetgroup/snippetgroup-snippetlist group-copy) snippetno)
        pick-operator
        (fn []
          (let [
                refining-ops (filter (fn [x] (= :refinement (operatorsrep/operator-category x)) ) operators)
                generalizing-ops (filter (fn [x] (= :generalization (operatorsrep/operator-category x)) ) operators)
                other-ops (filter (fn [x] (and (not= :refinement (operatorsrep/operator-category x))
                                               (not= :generalization (operatorsrep/operator-category x))) ) operators)
                
;                operator (let [rand-num (rand 100)]
;                           (if (pos? operator-bias)
;                             (cond
;                               (<= rand-num 20) (rand-nth generalizing-ops)
;                               (>= rand-num 60) (rand-nth refining-ops)
;                               :else (rand-nth other-ops))
;                             (cond
;                               (<= rand-num 40) (rand-nth generalizing-ops)
;                               (>= rand-num 80) (rand-nth refining-ops)
;                               :else (rand-nth other-ops))))
                
                operator (rand-nth operators)

                ; Pick an AST node that the chosen operator can be applied to
                all-valid-nodes (filter
                                  (fn [node]
                                    (and
                                      (operatorsrep/applicable? snippetgroup snippet node operator)
                                      ; In case of an operator that adds a directive, check that the directive isn't already there..
                                      (not (boolean
                                             (directives/bounddirective-for-directive
                                               (snippet/snippet-bounddirectives-for-node snippet node)
                                               (operator-directive (operatorsrep/operator-id operator)))))
                                      ))
                                  (matching/reachable-nodes snippet (snippet/snippet-root snippet)))]
            (if (empty? all-valid-nodes)
              (do
                (print ".")
                (recur)) ; Try again if there are no valid subjects..
              (let [subject (rand-nth all-valid-nodes)
                    operands (operatorsrep/operator-operands operator)
                    op-id (operatorsrep/operator-id operator)
                    [possiblevalues updated-group]
                    (cond 
                      (= op-id "add-directive-invokedby")
                      (gen-operand-values group-copy operator [:MethodInvocation :SuperMethodInvocation])
                      (= op-id "add-directive-invokes")
                      (gen-operand-values group-copy operator [:MethodDeclaration])
                      (= op-id "add-directive-overrides")
                      (gen-operand-values group-copy operator [:MethodDeclaration])
                      (= op-id "add-directive-subtype*")
                      (gen-operand-values2 group-copy operator [:SimpleName :ArrayType :ParameterizedType :PrimitiveType :QualifiedType :SimpleType :UnionType :WildcardType :TypeParameter :Type])
                      (= op-id "add-directive-subtype+")
                      (gen-operand-values2 group-copy operator [:SimpleName :ArrayType :ParameterizedType :PrimitiveType :QualifiedType :SimpleType :UnionType :WildcardType :TypeParameter :Type])
                      :else
                      [(for [operand operands] (operatorsrep/possible-operand-values|valid group-copy snippet subject operator operand))
                       group-copy])
                    ]
                (if (every? (fn [x] (not (empty? x))) possiblevalues)
                [operator subject operands (for [vals possiblevalues] (rand-nth vals)) updated-group]
                (recur)))))) ; Try again if there are operands with no possible values..
        
        [operator value operands operandvalues updated-group] (pick-operator)
        
        new-snippet (nth (snippetgroup/snippetgroup-snippetlist updated-group) snippetno) ; Cannot reuse snippet because it might've been replaced!!
        
        bindings
        (cons
          (operatorsrep/make-implicit-operandbinding-for-operator-subject updated-group new-snippet value operator)
          (map (fn [operand operandval]
                 (operatorsrep/make-binding operand updated-group new-snippet operandval))
               operands
               operandvalues))]
    (individual/make-individual
      (operatorsrep/apply-operator-to-snippetgroup updated-group new-snippet value operator bindings)
      {:mutation-operator (operatorsrep/operator-id operator)
       :mutation-node value
       :mutation-opvals operandvalues
       :id (str (gensym "0"))
       :original (individual/individual-info individual :id)}
      )))

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
  crossover
  "Performs a crossover between two templategroups:
   This means that two AST nodes are chosen at random, and both nodes (and their children) will be swapped.
   (In case this operation leads to invalid syntax, we try again..)
   Returns a vector containing the two crossed-over snippets"
  [ind1 ind2]
  (let
    [snippetgroup1 (individual/individual-templategroup ind1)
     snippetgroup2 (individual/individual-templategroup ind2)
     group-copy1 (persistence/copy-snippetgroup snippetgroup1)
     group-copy2 (persistence/copy-snippetgroup snippetgroup2)
     ; Get two random snippets
     snippet1 (rand-snippet group-copy1)
     snippet2 (rand-snippet group-copy2)
     ; Get two random AST nodes
     node-pair (find-compatible-ast-pair snippet1 snippet2)
     node1 (first node-pair)
     node2 (second node-pair) 
     new-snippet1 (operators/replace-node-with snippet1 node1 snippet2 node2)
     new-snippet2 (operators/replace-node-with snippet2 node2 snippet1 node1)]
    [(individual/make-individual
       (snippetgroup/replace-snippet group-copy1 snippet1 new-snippet1)
       {:crossover-old node1 :crossover-new node2})
     (individual/make-individual
       (snippetgroup/replace-snippet group-copy2 snippet2 new-snippet2)
       {:crossover-old node2 :crossover-new node1})]))

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

(defn select-with-new-id
  "See select
   Also attaches new info attributes, including a new :id"
  [population tournament-size]
  (let [selected (select population tournament-size)]
    (individual/individual-set-info 
      selected
      {:id (str (gensym "0"))
       :original (individual/individual-info selected :id)
       :selected true})))


(defn
  evolve
  "Look for a template that is able to match with a number of snippets, using genetic search
   @param verifiedmatches  There are the snippets we want to match with (@see make-verified-matches)
   @param conf             Configuration keyword arguments; see config-default for all default values"
  [verifiedmatches & {:as conf}]
  (let
    [config (merge config-default conf)
     output-dir (if (nil? (:output-dir config))
                  (str "evolve-" (util/current-date) "/")
                  (:output-dir config))

     ; If output-dir already contains previous generations, resume search from the last generation
     resume-generation (let [files (.list (clojure.java.io/file output-dir))
                             numbered-files (map (fn [file]
                                                   (try
                                                     (java.lang.Integer/parseInt file)
                                                     (catch Exception e -1)))
                                                 files)]
                         (if (> (count numbered-files) 0)
                           (inc (apply max numbered-files))
                           0))
     
     csv-name (str output-dir "results.csv")
     csv-columns ["Generation" "Total time" "Generation time"
                  "Best fitness" "Worst fitness" "Average fitness"
                  "Best fscore" "Worst fscore" "Average fscore"
                  "Best partial" "Worst partial" "Average partial"]
     gen-csv-name "population.csv"
     gen-csv-columns ["Id" "Original" "Fitness" "F1" "Partial" "Operator" "Subject" "Operands"]
     start-time (. System (nanoTime))
     
     
     fitness ((:fitness-function config) verifiedmatches config)
     sort-by-fitness (fn [population]
                       (sort-by
                         (fn [x] (individual/individual-fitness x))
                         (map (fn [ind] (individual/compute-fitness ind fitness)) 
                              population)))
     initial-pop (sort-by-fitness (if (> resume-generation 0)
                                    (let [files (.list (clojure.java.io/file (str output-dir (dec resume-generation))))
                                          templategroups (map (fn [file]
                                                                (persistence/slurp-snippetgroup (str output-dir (dec resume-generation) "/" file)))
                                                              files)] 
                                      (population-from-templates templategroups (:population-size config)))
                                    (if (nil? (:initial-population config))
                                      (population-from-snippets (:positives verifiedmatches) (:population-size config))
                                      (:initial-population config))))
     tournament-size (:tournament-rounds config)]
    (util/make-dir output-dir)
    (println "Writing results to:" output-dir)
    (util/append-csv csv-name csv-columns)
    
    ; Set up the map function used when matching templates to enable/disable concurrency
    (matching2/def-hmap-fn (:parallel-matching config) (:parallel-matching-threads config))
    
    (loop
      [generation resume-generation
       generation-start-time start-time
       population initial-pop
       history #{}]
      (let [new-history (atom history)
            history-hash (fn [individual] 
                           (hash (individual/individual-templategroup individual)))
            in-history (fn [individual]
                         (contains? @new-history (history-hash individual)))
            preprocess (fn [individual]
                         (if (not (in-history individual))
                           (let
                             [ind (individual/compute-fitness individual fitness)
                              filter-score (if ( = -1 (:fitness-filter-comp config))
                                             (individual/individual-fitness ind)
                                             (nth (individual/individual-fitness-components ind) (:fitness-filter-comp config)))]
                             (swap! new-history
                                    (fn [x] (clojure.set/union x #{(history-hash individual)})))
                             (if (pos? filter-score) ind))))
            best-fitness (individual/individual-fitness (last population))
            repeat-fn (case (:parallel-individuals config)
                        :sequential util/viable-repeat
                        :partitioned
                        (fn [cnt func test-func]
                          (util/parallel-viable-repeat cnt func test-func (:parallel-individuals-threads config)))
                        :reducer util/reducer-viable-repeat)
            
            total-elapsed (util/time-elapsed start-time)
            generation-elapsed (util/time-elapsed generation-start-time)]
        
        ; First print and store as much info as possible on the current generation
        (println "Generation:" generation)
        (println "Highest fitness:" (individual/individual-fitness (last population)))
;        (println "Fitnesses:" (map individual/individual-fitness-components population))
;        (println "Best specification:" (persistence/snippetgroup-string (individual/individual-templategroup (last population))))

        (util/append-csv csv-name [generation total-elapsed generation-elapsed
                                   best-fitness ; Fitness 
                                   (individual/individual-fitness (first population))
                                   (util/average (map (fn [ind] (individual/individual-fitness ind)) population))
                                   (first (individual/individual-fitness-components (last population))) ; F-score
                                   (first (individual/individual-fitness-components (first population)))
                                   (util/average (map (fn [ind] (first (individual/individual-fitness-components ind))) population))
                                   (second (individual/individual-fitness-components (last population))) ; Partial score
                                   (second (individual/individual-fitness-components (first population)))
                                   (util/average (map (fn [ind] (second (individual/individual-fitness-components ind))) population))])
        
        (println "Total time:" total-elapsed)
        (println "Generation time:" generation-elapsed)
        
        (util/make-dir (str output-dir generation))
        (persistence/spit-snippetgroup (str output-dir generation "/best.ekt") 
                                       (individual/individual-templategroup (last population)))
        (util/append-csv (str output-dir generation "/" gen-csv-name) gen-csv-columns)
        (doall (map-indexed
                 (fn [idx individual]
                   (util/append-csv (str output-dir generation "/" gen-csv-name)
                                    [(individual/individual-info (nth population idx) :id) 
                                     (individual/individual-info (nth population idx) :original)
                                     (individual/individual-fitness (nth population idx))
                                     (first (individual/individual-fitness-components (nth population idx)))
                                     (second (individual/individual-fitness-components (nth population idx)))
                                     (individual/individual-info (nth population idx) :mutation-operator)
                                     (pr-str (type (individual/individual-info (nth population idx) :mutation-node)))
                                     (pr-str (individual/individual-info (nth population idx) :mutation-opvals))])
;                   (persistence/spit-snippetgroup (str output-dir generation "/individual-" idx ".ekt") 
;                                                  (individual/individual-templategroup individual))
                   ) 
                 population))
        
        (if (not (nil? (:gui-editor config)))
          (let [editor (:gui-editor config)]
            (.onNewGeneration editor 
              (int generation)
              best-fitness
              (double (first (individual/individual-fitness-components (last population))))
              (double (second (individual/individual-fitness-components (last population))))
              (individual/individual-templategroup (last population))
              output-dir)))
        
        
        ; Core of the genetic search algorithm
        (cond
          (>= generation (:max-generations config))
          (do 
            (println "Maximum number of generations reached! Stopping genetic search..")
            (spit (str output-dir "done.txt") "Done!")
            total-elapsed)
          
          (> best-fitness (:fitness-threshold config))
          (do
              (println "Success:" (persistence/snippetgroup-string (individual/individual-templategroup (last population))))
              (persistence/spit-snippetgroup (str output-dir "success.ekt") 
                                             (individual/individual-templategroup (last population)))
              (spit (str output-dir "done.txt") "Done!")
              total-elapsed)
          
          (util/metaspace-almost-full?)
          (println "Java metaspace almost full! Stopping genetic search..")
          
          :rest 
          (recur
              (inc generation)
              (. System (nanoTime))
              (sort-by-fitness
                ; Produce the next generation using mutation, crossover and tournament selection
                (concat
                  ; Mutation
                  (repeat-fn
                    (* (:mutation-weight config) (count population))
                    #(preprocess (mutate (select population tournament-size) (:mutation-operators config)))
                    some?)
                  
                  ; Crossover (Note that each crossover operation produces a pair)
                  (apply concat
                         (repeat-fn 
                           (* (/ (:crossover-weight config) 2) (count population))
                           #(map preprocess
                                 (crossover
                                   (select population tournament-size)
                                   (select population tournament-size)))
                           (fn [[f s]]
                             (and f s))))
                  
                  ; Selection
                  (repeat-fn 
                    (* (:selection-weight config) (count population)) 
                    #(select-with-new-id population tournament-size) 
                    (fn [ind] (pos? (individual/individual-fitness ind))))))
              @new-history))))))


(defn
  hillclimb
  "Look for a template that is able to match with a number of snippets, using a simple hill climbing algorithm
   @param verifiedmatches  There are the snippets we want to match with (@see make-verified-matches)
   @param conf             Configuration keyword arguments; see config-default for all default values"
  [verifiedmatches & {:as conf}]
  (let
    [config (merge config-default conf)
     initial-template (first (:initial-population config))
     output-dir (if (nil? (:output-dir config))
                  (str "evolve-" (util/current-date) "/")
                  (:output-dir config))
     
     csv-name (str output-dir "results.csv")
     csv-columns ["Generation" "Total time" "Generation time"
                  "Fitness" "Fscore" "Partial"]
     start-time (. System (nanoTime))
     fitness ((:fitness-function config) verifiedmatches config)

    ]
    (util/make-dir output-dir)
    (spit (str output-dir "hillclimb.txt") "Hillclimbing run")
    (util/append-csv csv-name csv-columns)
    
    (loop
      [generation 0
       generation-start-time start-time
       best-template (individual/compute-fitness initial-template fitness)]
      (let [best-fitness (individual/individual-fitness best-template)]
        
        (println "Iteration:" generation)
        (println "Current best fitness:" best-fitness)
        
        (util/append-csv csv-name [generation (util/time-elapsed start-time) (util/time-elapsed generation-start-time) 
                                   best-fitness ; Fitness 
                                   (first (individual/individual-fitness-components best-template)) ; F-score
                                   (second (individual/individual-fitness-components best-template)) ; Partial score
                                   ])
        (persistence/spit-snippetgroup (str output-dir "/iteration-" generation ".ekt") 
                                       (individual/individual-templategroup best-template))
        
        
        (cond
          (>= generation (:max-generations config))
          (println "Maximum number of generations reached! Stopping search..")
          
          (> best-fitness (:fitness-threshold config))
          (do
              (println "Success:" (persistence/snippetgroup-string (individual/individual-templategroup best-template)))
              (persistence/spit-snippetgroup (str output-dir "success.ekt") 
                                             (individual/individual-templategroup best-template)))
          
          (util/metaspace-almost-full?)
          (println "Java metaspace almost full! Stopping genetic search..")
          
          :rest 
          (recur
              (inc generation)
              (. System (nanoTime))
              
              (let [mutant (mutate best-template (:mutation-operators config))
                    mutant-plus-fitness (individual/compute-fitness mutant fitness)
                    tmp (println (individual/individual-fitness mutant-plus-fitness))]
                (if (>= 
                      (individual/individual-fitness mutant-plus-fitness)
                      (individual/individual-fitness best-template))
                  mutant-plus-fitness
                  best-template))))))))

(defn
  randomsearch
  "Look for a template that is able to match with a number of snippets, using a random search algorithm
   (We generate a random point in the search space by taking one of the verified matches, and applying 
   a random number of mutations to it.)

   @param verifiedmatches  There are the snippets we want to match with (@see make-verified-matches)
   @param conf             Configuration keyword arguments; see config-default for all default values"
  [verifiedmatches & {:as conf}]
  (let
    [config (merge config-default conf)
     initial-template (first (:initial-population config))
     output-dir (if (nil? (:output-dir config))
                  (str "evolve-" (util/current-date) "/")
                  (:output-dir config))
     csv-name (str output-dir "results.csv")
     csv-columns ["Generation" "Total time" "Generation time"
                  "Fitness" "Fscore" "Partial"]
     start-time (. System (nanoTime))
     fitness ((:fitness-function config) verifiedmatches config)]
    (util/make-dir output-dir)
    (spit (str output-dir "random.txt") "Random search run")
    (util/append-csv csv-name csv-columns)
    
    (loop
      [generation 0
       generation-start-time start-time
       best-template (individual/compute-fitness initial-template fitness)]
      (let [best-fitness (individual/individual-fitness best-template)]
        
        (println "Iteration:" generation)
        (println "Current best fitness:" best-fitness)
        
        (util/append-csv csv-name [generation (util/time-elapsed start-time) (util/time-elapsed generation-start-time) 
                                   best-fitness ; Fitness 
                                   (first (individual/individual-fitness-components best-template)) ; F-score
                                   (second (individual/individual-fitness-components best-template)) ; Partial score
                                   ])
        (persistence/spit-snippetgroup (str output-dir "/iteration-" generation ".ekt") 
                                       (individual/individual-templategroup best-template))
        
        (cond
          (>= generation (:max-generations config))
          (println "Maximum number of generations reached! Stopping search..")
          
          (> best-fitness (:fitness-threshold config))
          (do
              (println "Success:" (persistence/snippetgroup-string (individual/individual-templategroup best-template)))
              (persistence/spit-snippetgroup (str output-dir "success.ekt") 
                                             (individual/individual-templategroup best-template)))
          
          (util/metaspace-almost-full?)
          (println "Java metaspace almost full! Stopping genetic search..")
          
          :rest 
          (recur
              (inc generation)
              (. System (nanoTime))
              
              (let [; Mutate a template multiple times
                    multi-mutate (fn [template count]
                                   (if (= count 0)
                                     template
                                     (recur (mutate template (:mutation-operators config)) (dec count))))
                    mutation-count (rand-int 30)
                    
                    mutant (multi-mutate initial-template mutation-count)
                    mutant-plus-fitness (individual/compute-fitness mutant fitness)
                    ]
                (println "(Applied" mutation-count "mutations to initial template)")
                (println "Current specification:" (persistence/snippetgroup-string (individual/individual-templategroup mutant)))
                (if (>= 
                      (individual/individual-fitness mutant-plus-fitness)
                      (individual/individual-fitness best-template))
                  mutant-plus-fitness
                  best-template))))))))

(defn templategroup-fitness
  "Compute the fitness of a single template group"
  [templategroup verifiedmatches & {:as conf}]
  (let [config (merge config-default conf)
        fitness ((:fitness-function config) verifiedmatches config)
        ind (individual/make-individual templategroup)
        new-ind (individual/compute-fitness ind fitness)]
    {:fitness (individual/individual-fitness new-ind) :components (individual/individual-fitness-components new-ind)}))

(defn evolve-gui [templategroup matches gui config-string]
  (let [verifiedmatches (make-verified-matches (into [] matches) [])
        config-parsed (read-string config-string)
        config (merge
                 config-parsed
                 {:initial-population 
                  (population-from-templates [templategroup] (:population-size config-parsed))
                  :gui-editor gui})]
    (future 
      (apply evolve verifiedmatches (mapcat identity (vec config))))))

(defn register-callbacks []
  (set! (damp.ekeko.snippets.gui.RecommendationEditor/FN_EVOLVE) evolve-gui))
(register-callbacks)

(comment
  (do 
    ; Don't uncomment this one, as it creates a dependency on the testing project..
    (defn slurp-from-resource [pathrelativetobundle]
      (persistence/slurp-snippetgroup (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile pathrelativetobundle)))
    
    (defn run-example 
      "Runs an example genetic search"
      []
      (def templategroup (slurp-from-resource "/resources/EkekoX-Specifications/invokes.ekt"))
      (def matches (into [] (fitness/templategroup-matches templategroup)))
      (def verifiedmatches (make-verified-matches matches []))
      (evolve verifiedmatches
              :parallel-individuals :reducer
              :parallel-individuals-threads 8
              :parallel-matching :reducer
              :parallel-matching-threads 2
              :quick-matching false
              :partial-matching true
              :selection-weight 1/4
              :mutation-weight 3/4
              :crossover-weight 0/4
              :max-generations 25
              :fitness-threshold 0.999
              :population-size 100
              :tournament-rounds 3))
    )
  
  (run-example)
  
  
  (defn 
    benchmark-parallel
    [maxevo maxmatch repetition]
    (let [templategroup 
          (slurp-from-resource "/resources/EkekoX-Specifications/invokes.ekt")
          matches 
          (into [] (fitness/templategroup-matches templategroup))
          verifiedmatches 
          (make-verified-matches matches [])
          options
          {:parallel-individuals true
           :parallel-individuals-threads 8
           :parallel-matching true
           :parallel-matching-threads 2
           :quick-matching false
           :partial-matching true
           :selection-weight 1/4
           :mutation-weight 3/4
           :crossover-weight 0/4
           :max-generations 25
           :fitness-threshold 0.99
           :population-size 100
           :tournament-rounds 3}]
      (for [evot (range 1 (inc maxevo))]
        [evot 
         (for [matcht (range 1 (inc maxmatch))]
           [matcht
            (for [_ (range 0 repetition)]
              (apply evolve verifiedmatches
                     (apply concat 
                            (merge options
                                   {:parallel-individuals-threads evot
                                    :parallel-matching-threads matcht}))))
            ])])))
         
          
 (benchmark-parallel 10 10 10)
    
    
  
  
  
  (fitness/templategroup-matches (persistence/slurp-snippetgroup "/Users/soft/Documents/workspace-runtime2/error1460860107148.ekt"))
  
  
  
  
  (let [path 
        "/resources/EkekoX-Specifications/experiments/strategy-jhotdraw/solution3.ekt" ; OK!
;        "/resources/EkekoX-Specifications/experiments/observer-jhotdraw/solution.ekt" ; OK! 21 VS 16 matches .. it's actually the new impl that's correct!
;        "/resources/EkekoX-Specifications/experiments/prototype-jhotdraw/solution.ekt" ; OK!
;        "/resources/EkekoX-Specifications/experiments/templatemethod-jhotdraw/solution.ekt" ; OK!
;        "/resources/EkekoX-Specifications/experiments/factorymethod-jhotdraw/solution_take4-reorder2.ekt" ; OK! 22 VS 17 matches .. new impl gets more correct results
        
        matches-new (time (fitness/templategroup-matches (slurp-from-resource path)))
        matches-old (time (fitness/templategroup-matches-old (slurp-from-resource path)))
        ]
    (println "New:" (count matches-new) "VS Old:" (count matches-old))
;    (inspector-jay.core/inspect matches-new)
;    (inspector-jay.core/inspect matches-old)
    (inspector-jay.core/inspect (clojure.set/difference matches-old matches-new))
    (inspector-jay.core/inspect (clojure.set/difference matches-new matches-old))
    nil)
  
  (inspector-jay.core/inspect
    (.parameters (first (nth (damp.ekeko/ekeko [?m]
                                           ( damp.ekeko.jdt.ast/ast :MethodDeclaration ?m)) 2) )))
  
  (let [all-method-declarations (damp.ekeko/ekeko [?m] (damp.ekeko.jdt.ast/ast :MethodDeclaration ?m))
        some-method-declaration (first (first all-method-declarations))]
    (.parameters some-method-declaration))
  
  (inspector-jay.core/inspect
    (count (into #{} (damp.ekeko.snippets.matching2/query-templategroup 
                 (slurp-from-resource "/resources/EkekoX-Specifications/experiments/strategy-nutch/solution3.ekt")))))
  
  (defn transform-by-snippetgroups
    "Performs the program transformation defined by the lhs and rhs snippetgroups." 
    [snippetgroup|lhs snippetgroup|rhs]
    (let [qinfo (querying/snippetgroup-snippetgroupqueryinfo snippetgroup|lhs)
          defines (:preddefs qinfo)
          lhsuservars (into #{} (querying/snippetgroup-uservars snippetgroup|lhs))
          rhsuservars (into #{} (querying/snippetgroup-uservars snippetgroup|rhs))
          rhsconditions (querying/snippetgroup-conditions|rewrite snippetgroup|rhs lhsuservars)
          query (querying/snippetgroupqueryinfo-query qinfo 'damp.ekeko/ekeko rhsconditions rhsuservars false)] ;should these be hidden?
      (querying/pprint-sexps (conj defines query))
      (doseq [define defines]
        (eval define))
      (eval query)
      ; (rewrites/apply-and-reset-rewrites)
      ))
  
  ; Test matching a templategroup  
  
  (def templategroup (slurp-from-resource "/resources/EkekoX-Specifications/invokes.ekt"))
  (def templategroup (slurp-from-resource "/resources/EkekoX-Specifications/experiments/strategy-nutch/solution3.ekt"))
  (time (def matches (into [] (fitness/templategroup-matches templategroup))))
  
  
  (def snippet (first (snippetgroup/snippetgroup-snippetlist templategroup)))
  (snippet/snippet-node-parent|conceptually snippet (snippet/snippet-root snippet))
  
  ; Hillclimbing test
  (defn slurp-from-resource [pathrelativetobundle]
      (persistence/slurp-snippetgroup (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile pathrelativetobundle)))
  (def initial (slurp-from-resource "/resources/EkekoX-Specifications/experiments/strategy-jhotdraw/initial-protected2.ekt"))
  (def solution (slurp-from-resource "/resources/EkekoX-Specifications/experiments/strategy-jhotdraw/solution3.ekt"))
  (def matches (into [] (fitness/templategroup-matches solution)))
  (def verifiedmatches (make-verified-matches matches []))
  (hillclimb initial verifiedmatches
             :output-dir "strategy-test2/"          
             :quick-matching false
             :partial-matching true
             :max-generations 400
             :match-timeout 120000
             :fitness-threshold 0.95
             :mutation-operators
             (filter 
               (fn [op] 
                 (some #{(operatorsrep/operator-id op)} 
                       ["replace-by-variable"
                        "add-directive-invokes" 
                        "add-directive-overrides" 
                        "add-directive-type"
                        "isolate-expr-in-method" 
                        "replace-by-wildcard" 
                        ]))
               (operatorsrep/registered-operators)))
  
  
  ; Reorder templates in a template group (for increased performance)
  (let [path "/resources/EkekoX-Specifications/experiments/strategy-nutch/initial2.ekt"
        tgroup (slurp-from-resource path)
        templates (snippetgroup/snippetgroup-snippetlist tgroup)
        reordered (replace templates [2 0 1])
        reordered-tgroup (snippetgroup/snippetgroup-update-snippetlist tgroup reordered)]
    (persistence/spit-snippetgroup 
      (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile path) reordered-tgroup))
  
  ; Spit the matches of a group
  (def templategroup (slurp-from-resource "/resources/EkekoX-Specifications/dbg/templatemethod-jhotdraw/solution3.ekt"))
  (spit-templategroup-matches templategroup "test-output6")
  
  ; Test a nested transformation
  (def transfogroup 
    (persistence/slurp-transformation "/Users/soft/Documents/Github/damp.ekeko.snippets/damp.ekeko.snippets.plugin.test/resources/EkekoX-Specifications/dbg/sandbox-move.ekx"))
  (transform-by-snippetgroups (:lhs transfogroup) (:rhs transfogroup))
  (def transfogroup
    (persistence/slurp-transformation "/Users/soft/Documents/workspace-runtime/ToyExample/test.ekt"))
  
  ; Test a particular mutation operator (on a random subject)
  (do
    (def templategroup (persistence/slurp-snippetgroup "/Users/soft/Documents/experiments/factorymethod-8/37/individual-31.ekt"))
    (def templategroup (persistence/slurp-snippetgroup "/Users/soft/Documents/workspace-runtime2/timeout1455546559910.ekt"))
    (def templategroup (slurp-from-resource "/resources/EkekoX-Specifications/experiments/strategy-nutch/solution.ekt"))
    (def mutant
      (mutate (damp.ekeko.snippets.geneticsearch.individual/make-individual templategroup)
                    (filter (fn [op] (= (operatorsrep/operator-id op) "generalize-invocations")) (operatorsrep/registered-operators))))
    (def mutant-template (individual/individual-templategroup mutant))
    
    (def paths [["obs1" "Documents/experiments/observer-6"]
                ["obs2" "Documents/experiments/observer-7"]
                ["obs3" "Documents/experiments/observer-8"]
                ["obs4" "Documents/experiments/observer-10"]
                ["obs5" "Documents/experiments/observer-11"]
                ["obs6" "Documents/experiments/observer-12"]
                ["obs7" "Documents/experiments/observer-14"]
                
                ["proto1" "Documents/experiments/prototype-3"]
                ["proto2" "Documents/experiments/prototype-4"]
                ["proto3" "Documents/experiments/prototype-6"]
                ["proto4" "Documents/experiments/prototype-10"]
                
                ["tmp1" "Documents/experiments-bertha/template-method-12"]
                ["tmp2" "Documents/experiments-bertha/template-method-11"]
                ["tmp3" "Documents/experiments-bertha/template-method-10"]
                ["tmp4" "Documents/experiments-bertha/template-method-9"]
                ["tmp5" "Documents/experiments-bertha/template-method-6"]
                
                ["strat1" "Documents/experiments/strategy-14"]
                ["strat2" "Documents/experiments-bertha/strategy-3"]
                
                ["fac1" "Documents/experiments/factorymethod-11"]
                ["fac2" "Documents/experiments/factorymethod-2"]
                
                ["ntmp1" "Documents/experiments-bertha2/template-method-nutch-1"]
                ["ntmp2" "Documents/experiments-bertha2/template-method-nutch-2"]
                ["ntmp3" "Documents/experiments-bertha2/template-method-nutch-4"]
                ["ntmp4" "Documents/experiments-bertha2/template-method-nutch-5"]
                ["ntmp5" "Documents/experiments-bertha2/template-method-nutch-6"]
                ["ntmp6" "Documents/experiments-bertha2/template-method-nutch-7"]
                ["ntmp7" "Documents/experiments-bertha2/template-method-nutch-8"]
                ["ntmp8" "Documents/experiments-bertha2/template-method-nutch-9"]
                ["ntmp9" "Documents/experiments-bertha2/template-method-nutch-10"]
                
                ["nstrat1" "Documents/experiments-bertha4/strategy-nutch-4"]])
    
    (doseq [pair paths]
      (spit (str (first pair) ".txt")
            (persistence/snippetgroup-string
              (persistence/slurp-snippetgroup (str "/Users/soft/" (second pair) "/success.ekt")))
            ))
    
    (let [group (persistence/slurp-snippetgroup (str "/Users/soft/" (second (first paths)) "/success.ekt"))])
    
    
    (println (persistence/snippetgroup-string (individual/individual-templategroup mutant)))
    (fitness/templategroup-matches (individual/individual-templategroup mutant))
    nil)
  
  ; Test tournament selection 
  (select (population-from-snippets matches 10) 2)
  
  ; Fetch all operator names
  (inspector-jay.core/inspect
    (map (fn [op] (operatorsrep/operator-id op))
          (operatorsrep/registered-operators)))
  
  ; Calculate the fitness of one templategroup
  (do
    (def templategroup (slurp-from-resource "/resources/EkekoX-Specifications/experiments/strategy-jhotdraw/initial-protected.ekt"))
    (def solution (slurp-from-resource "/resources/EkekoX-Specifications/experiments/factorymethod-jhotdraw/solution_take4-reorder.ekt"))
    (def matches (into [] (fitness/templategroup-matches templategroup)))
    (def verifiedmatches (make-verified-matches matches []))
    (time (templategroup-fitness 
           templategroup
           verifiedmatches
           :match-timeout 48000
           :fitness-weights [18/20 2/20 0/20]
           :quick-matching false
           :partial-matching true
           :thread-group (new ThreadGroup "invokes"))))
  )