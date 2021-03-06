(ns 
  ^{:doc "Conversion of snippets to Ekeko queries."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets.querying
  (:require [clojure.core.logic :as cl]) 
  (:require [damp.ekeko.snippets 
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [operators :as operators]
             [util :as util]
             [directives :as directives]
             [matching :as matching] 
             [rewriting :as rewriting]
             [persistence :as persistence]
             [runtime :as runtime]
             [parsing :as parsing]
             ]) 
  (:require 
    [damp.ekeko 
     [logic :as el]]
    [damp.ekeko.jdt
     [astnode :as astnode]]))

;; Converting a snippet to a query
;; -------------------------------


(defn
  snippet-conditions 
  ([snippet]
    (snippet-conditions snippet identity))
  ([snippet bounddirectivesfilterf]
    (snippet-conditions snippet bounddirectivesfilterf (constantly '())))
  ([snippet bounddirectivesfilterf extraconditionsafter]
    (matching/snippet-node-conditions snippet 
                                      (snippet/snippet-root snippet)
                                      :bdfilter 
                                      bounddirectivesfilterf
                                      :extraconditionsafter
                                      extraconditionsafter
                                      )))
(defn
  snippet-uservars
  [snippet]
  (sort (into #{} 
              (matching/snippet-vars-among-directivebindings snippet))))
  
(defn
  snippetgroup-uservars
  [snippetgroup]
  (sort (into #{}
              (mapcat
                snippet-uservars
                (snippetgroup/snippetgroup-snippetlist snippetgroup)))))


(defn
  snippet-queryinfo
  [snippet]
  (let [potentialbodies
        (partition-by 
          (fn [condition] (= (first condition) `cl/fresh))
          (snippet-conditions snippet))
        bodies
        (rest potentialbodies)
        standaloneconditions
        (first potentialbodies)
        uservars
        (snippet-uservars snippet)
        publicvars
        (conj 
          uservars
          (snippet/snippet-var-for-root snippet))
        conditionsvars
        (matching/snippet-node-matchvarsforproperties snippet (snippet/snippet-root snippet))
        predvars
        (concat
          publicvars
          conditionsvars)]
    (let [prednames
          (map (fn [bodyconditions]
                 (gensym "match"))
               bodies)
          preddefinitions 
          (map (fn [predname bodyconditions]
                 `(defn
                    ~predname 
                    [varvector#]
                    (cl/fresh [~@predvars]
                              (cl/== varvector# [~@predvars])
                              ~@bodyconditions)))
               prednames
               bodies)]
      {:conditions 
       standaloneconditions
       :conditionsvars 
       conditionsvars
       :prednames
       prednames
       :preddefs
       preddefinitions
       :publicvars
       publicvars
       :predvars
       predvars
       })))

(defn
  snippetqueryinfo-query
  [snippetinfo ekekolaunchersymbol]
  (let [{conditions :conditions
         conditionsvars :conditionsvars
         publicvars :publicvars
         predvars :predvars
         prednames :prednames} snippetinfo]
    (let [varvector
          (gensym "varvector")
          predcalls 
          (map (fn [predname]
                 `(~predname ~varvector))
               prednames)]
      `(~ekekolaunchersymbol 
         [~@publicvars]
         (cl/fresh [~varvector ~@conditionsvars]
                   ~@conditions
                   (cl/== ~varvector [~@predvars])
                   ~@predcalls
                   )))))  

(def ^:dynamic *print-queries-to-console* false) 

(defn
  pprint-sexps
  [sexps]
  (when 
    *print-queries-to-console* 
    (binding [clojure.pprint/*print-right-margin* 200]
      (doseq [sexp sexps]
        (clojure.pprint/pprint sexp)))))
            

(defn
  query-by-snippet
  [snippet launchersymbol]
  (let [qinfo (snippet-queryinfo snippet)
        defines (:preddefs qinfo)
        query (snippetqueryinfo-query qinfo launchersymbol)]
    (pprint-sexps (conj defines query))
    (doseq [define defines]
      (eval define))
    (eval query)))

(defn query-by-snippetgroup-fast-noeval [snippetgroup launchersymbol]
  (map
    (fn [snippet]
      (let [qinfo (snippet-queryinfo snippet)
            defines (:preddefs qinfo)
            query (snippetqueryinfo-query qinfo launchersymbol)]
        [defines query]
        ))
    (snippetgroup/snippetgroup-snippetlist snippetgroup)))

(defn
  combine-noeval-results
  "Take the individual snippet query results of query-by-snippetgroup-fast-noeval
   and combine them into matching results for the entire snippetgroup"
  [snippetgroup results launchersymbol]
  (let [snippets (snippetgroup/snippetgroup-snippetlist snippetgroup)
        
        results-with-vars ; Returns [variable-names results] pairs
        (map-indexed
          (fn [idx result]
            [(cons
               (snippet/snippet-var-for-root (nth snippets idx))
               (snippet-uservars (nth snippets idx)))
             result])
          results)]
    
    (if (= 1 (count results))
      results
      
      ; Combine the results of each snippet by intersecting over the common variables
      (reduce
       (fn [[vars1 results1] [vars2 res2]]
         (let [common-vars (clojure.set/intersection (set vars1) (set vars2))
               common-vars-indices (fn [vars] (vec (for [common-var common-vars] (.indexOf vars common-var))))
               indices1 (common-vars-indices vars1)
               indices2 (common-vars-indices vars2)
               vars1-without-commons (vec (remove (fn [var] (.contains common-vars var)) vars1))
               
               vars1-without-commons-indices ; Indices of non-common variables within vars1
               (for [var vars1-without-commons] (.indexOf vars1 var))
               
               combined-vars (concat vars1-without-commons vars2)] ;
           ; Iterate over results1
           (loop [res1 results1
                  output []]
             (if (or (empty? res1) (empty? res2))
               [combined-vars output]
               (let [result1 (first res1)
                     
                     ; Iterate over results2
                     combined-results
                     (loop [worklist res2
                        combined-results output]
                       (if (empty? worklist) 
                         combined-results
                         
                         (let [result2 (first worklist)
                               is-matching ; Do the common vars in result1 and 2 match? (also works if there are 0 common vars)
                               (every?
                                 (fn [idx]
                                   (= (nth result1 (nth indices1 idx))
                                      (nth result2 (nth indices2 idx))))
                                 (range 0 (count common-vars)))]
                           (if is-matching
                             (recur (rest worklist)
                                    (conj combined-results
                                          (concat
                                            (replace (vec result1) vars1-without-commons-indices)
                                            result2)))
                             (recur (rest worklist)
                                    combined-results)))))]
                 (recur 
                   (rest res1)
                   combined-results))))))
       results-with-vars))))

(defn
  query-by-snippetgroup-fast
  "Query each snippet in a snippetgroup separately/concurrently and combine the results afterwards
   by intersecting over the common variables of the snippets"
  [snippetgroup launchersymbol]
  (let [snippets (snippetgroup/snippetgroup-snippetlist snippetgroup)
        
        results ; List of match results for each snippet
        (pmap
          (fn [snippet]
            (let [qinfo (snippet-queryinfo snippet)
                  defines (:preddefs qinfo)
                  query (snippetqueryinfo-query qinfo launchersymbol)]
              (doseq [define defines]
                (eval define))
              (eval query)))
          snippets)
        
        results-with-vars ; Returns [variable-names results] pairs
        (map-indexed
          (fn [idx result]
            [(cons
               (snippet/snippet-var-for-root (nth snippets idx))
               (snippet-uservars (nth snippets idx)))
             result])
          results)]
    
    (if (= 1 (count results))
      results
      
      ; Combine the results of each snippet by intersecting over the common variables
      (reduce
       (fn [[vars1 results1] [vars2 res2]]
         (let [common-vars (clojure.set/intersection (set vars1) (set vars2))
               common-vars-indices (fn [vars] (vec (for [common-var common-vars] (.indexOf vars common-var))))
               indices1 (common-vars-indices vars1)
               indices2 (common-vars-indices vars2)
               vars1-without-commons (vec (remove (fn [var] (.contains common-vars var)) vars1))
               
               vars1-without-commons-indices ; Indices of non-common variables within vars1
               (for [var vars1-without-commons] (.indexOf vars1 var))
               
               combined-vars (concat vars1-without-commons vars2)] ;
           ; Iterate over results1
           (loop [res1 results1
                  output []]
             (if (or (empty? res1) (empty? res2))
               [combined-vars output]
               (let [result1 (first res1)
                     
                     ; Iterate over results2
                     combined-results
                     (loop [worklist res2
                        combined-results output]
                       (if (empty? worklist) 
                         combined-results
                         
                         (let [result2 (first worklist)
                               is-matching ; Do the common vars in result1 and 2 match? (also works if there are 0 common vars)
                               (every?
                                 (fn [idx]
                                   (= (nth result1 (nth indices1 idx))
                                      (nth result2 (nth indices2 idx))))
                                 (range 0 (count common-vars)))]
                           (if is-matching
                             (recur (rest worklist)
                                    (conj combined-results
                                          (concat
                                            (replace (vec result1) vars1-without-commons-indices)
                                            result2)))
                             (recur (rest worklist)
                                    combined-results)))))]
                 (recur 
                   (rest res1)
                   combined-results))))))
       results-with-vars))))

(defn strip-uservars 
  "Removes the user variables from the results of a query-by-snippetgroup-fast call"
  [snippetgroup query-by-snippetgroup-fast-return-val]
  (let [[all-vars results] query-by-snippetgroup-fast-return-val
        snippets (snippetgroup/snippetgroup-snippetlist snippetgroup)
        rootvars (reduce
                   (fn [vars snippet]
                     (conj vars (snippet/snippet-var-for-root snippet)))
                   [] snippets)
        rootvar-indices (vec (for [var rootvars] (.indexOf all-vars var)))]
    (for [result results]
      (replace (vec result) rootvar-indices))))

(defn query-by-snippetgroup-fast-roots
  "@see query-by-snippetgroup-fast , but also hides user variables from results"
  [snippetgroup]
  (let [result (query-by-snippetgroup-fast snippetgroup 'damp.ekeko/ekeko)]
    (strip-uservars snippetgroup result)))

; Converting snippet group to query
;------------------------------------

(defn-
   snippetgroup-conditions
   "Returns a list of logic conditions that will retrieve matches for the given snippet group."
   [snippetgroup]
   (mapcat snippet-conditions (snippetgroup/snippetgroup-snippetlist snippetgroup)))


(defn
  snippetgroup-snippetgroupqueryinfo
  [snippetgroup-rev]
  (let [; Reverse the list of templates in the template group.. not sure why, but otherwise the templates get resolved in the reversed order
        ; .. which can be very bad for performance if you assume that a variable gets bound in a template before it's used in a succeeding template..
        snippetgroup (snippetgroup/make-snippetgroup (snippetgroup/snippetgroup-name snippetgroup-rev)
                       (reverse (snippetgroup/snippetgroup-snippetlist snippetgroup-rev)))]
    (letfn [(snippetingroup-queryinfo ;similar to snippet-queryinfo, except that snippets in same group need to share user variables
             [publicvars snippet]
             (let [potentialbodies
                   (partition-by 
                     (fn [condition] (= (first condition) `cl/fresh))
                     (snippet-conditions snippet))
                  
                   ; Uncomment to inspect your query
;                  tmp (inspector-jay.core/inspect (snippet-conditions snippet))
                  
                  bodies
                  (rest potentialbodies)
                  standaloneconditions
                  (first potentialbodies)
                  conditionsvars
                  (matching/snippet-node-matchvarsforproperties snippet (snippet/snippet-root snippet))
                  
                  predvars
                  (concat
                    publicvars
                    conditionsvars)]
              (let [prednames
                    (map (fn [bodyconditions]
                           (gensym "match"))
                         bodies)
                    preddefinitions 
                    (map (fn [predname bodyconditions]
                           `(defn
                              ~predname 
                              [varvector#]
                              (cl/fresh [~@predvars]
                                        (cl/== varvector# [~@predvars]) ;differs per predicate!
                                        ~@bodyconditions)))
                         prednames
                         bodies)]
                {:predvars predvars
                 :prednames  prednames
                 :conditions standaloneconditions
                 :conditionsvars conditionsvars
                 :preddefs preddefinitions
                })))]
    (let [uservars
          (mapcat snippet-uservars (snippetgroup/snippetgroup-snippetlist snippetgroup))
          matchvars
          (snippetgroup/snippetgroup-rootvars snippetgroup)
          publicvars
          (concat 
            matchvars
            uservars)
          queryinfos 
          (map (partial snippetingroup-queryinfo publicvars) (snippetgroup/snippetgroup-snippetlist snippetgroup))
          ]
  (reduce
    (fn [groupqueryinfosofar queryinfo]
      (let [varvector
            (gensym "varvector")
            predcalls 
            (map (fn [predname]
                   `(~predname ~varvector))
                 (:prednames queryinfo))
            conditionsvars 
            (:conditionsvars queryinfo)
            conditions
            (:conditions queryinfo)
            predvars
            (:predvars queryinfo)
            ]
        (->
          groupqueryinfosofar
          (update-in 
            [:conditions]
            conj
            `(cl/fresh [~varvector ~@conditionsvars]
                       ~@conditions
                       (cl/== ~varvector [~@predvars]) ;;differ per predicate!
                       ~@predcalls))
          (update-in 
            [:preddefs]
            concat
            (:preddefs queryinfo)))))
    {:conditions '()
     :preddefs '()
     :publicvars publicvars
     :matchvars matchvars
     :uservars uservars
     }
    queryinfos)))))

      
(defn
  snippetgroupqueryinfo-query
  ([groupqueryinfo ekekolaunchersymbol]
    (snippetgroupqueryinfo-query groupqueryinfo ekekolaunchersymbol '() '() false))
  ([groupqueryinfo ekekolaunchersymbol additionalconditions additionalrootvars hideuservars]
    (let [{conditions_ :conditions
           matchvars :matchvars
           uservars :uservars
           } groupqueryinfo
          
          conditions (take (- (count conditions_) 0) conditions_)
          
          launchervars
          (distinct 
            (concat matchvars additionalrootvars 
                    (if hideuservars '() uservars)))]
      (if 
        hideuservars
        `(~ekekolaunchersymbol 
           [~@launchervars]
           (cl/fresh [~@uservars]
                     ~@conditions
                     ~@additionalconditions))
        `(~ekekolaunchersymbol 
           [~@launchervars]
           ~@conditions
           ~@additionalconditions)))))


(defn query-by-snippetgroup-noeval
  ([snippetgroup launchersymbol]
    (query-by-snippetgroup-noeval snippetgroup launchersymbol '() '() false))
  ([snippetgroup launchersymbol additionalconditions additionalrootvars hideuservars]
    (let [qinfo (snippetgroup-snippetgroupqueryinfo snippetgroup)
          defines (:preddefs qinfo)
          query (snippetgroupqueryinfo-query qinfo launchersymbol additionalconditions additionalrootvars hideuservars)]
      [defines query])))

(defn query-by-snippetgroup
  ([snippetgroup launchersymbol]
    (query-by-snippetgroup snippetgroup launchersymbol '() '() false))
  ([snippetgroup launchersymbol additionalconditions additionalrootvars hideuservars]
    (let [qinfo (snippetgroup-snippetgroupqueryinfo snippetgroup)
          defines (:preddefs qinfo)
          query (snippetgroupqueryinfo-query qinfo launchersymbol additionalconditions additionalrootvars hideuservars)]
;      (pprint-sexps (conj defines query))
      (doseq [define defines]
        (eval define))
      (eval query))))

(defn print-snippetgroup
  ([snippetgroup launchersymbol]
    (print-snippetgroup snippetgroup launchersymbol '() '() false))
  ([snippetgroup launchersymbol additionalconditions additionalrootvars hideuservars]
    (let [qinfo (snippetgroup-snippetgroupqueryinfo snippetgroup)
          defines (:preddefs qinfo)
          query (snippetgroupqueryinfo-query qinfo launchersymbol additionalconditions additionalrootvars hideuservars)]
      (doseq [define defines]
        (clojure.pprint/pprint define))
      (clojure.pprint/pprint query))))

; Converting snippet group to rewrite query
;------------------------------------------


(defn
  newnode-from-template
  [template runtime-template-var]
  (let [root
        (snippet/snippet-root template)
        var-generatedcode 
        (snippet/snippet-var-for-node template root)
        stemplate
        (persistence/snippet-as-persistent-string template)
        
        chunks (clojure.string/split stemplate #"#")
        
;        tmp (println (read-string (clojure.string/join "#" chunks)))
        
;        tmp (spit "spit.ekt" stemplate)
        ]
    `((cl/== ~runtime-template-var (read-string (clojure.string/join "#" [~@chunks])) ;(read-string (slurp "spit.ekt")) ;(read-string ~stemplate) ;(persistence/snippet-from-persistent-string ~stemplate)
             )
       (el/equals ~var-generatedcode  (snippet/snippet-root ~runtime-template-var)))))
  

(defn
  snippet-node-conditions|rewriting
  [snippet value snippetruntimevar]
  (let [rewriting-bounddirectives
        (filter 
          (fn [bounddirective]
            (rewriting/registered-rewriting-directive? (directives/bounddirective-directive bounddirective)))
          (snippet/snippet-bounddirectives-for-node snippet value))]
    (mapcat
      (fn [bounddirective]
        (directives/snippet-bounddirective-conditions snippet bounddirective))
      rewriting-bounddirectives)))

(defn gAST [node]
  (if (astnode/value? node)
    (.getAST (astnode/owner node))
    (.getAST node))
  )

(defn
  snippet-node-conditions|replacedby
  [snippet subject snippetruntimevar]
  (let [replacedby-bounddirectives
        (filter 
          (fn [bounddirective]
            (matching/registered-replacedby-directive? (directives/bounddirective-directive bounddirective)))
          (snippet/snippet-bounddirectives-for-node snippet subject))]
    (mapcat
      (fn [bounddirective]
        (let [directive (directives/bounddirective-directive bounddirective)
              opbindingvals (map directives/directiveoperandbinding-value (directives/bounddirective-operandbindings bounddirective))
              varsubject (snippet/snippet-var-for-node snippet subject)
              compatiblevaluevar (util/gen-lvar 'compatiblevalue)]
          (assert (= (nth opbindingvals 0) subject) "Something went horribly wrong.")
          (condp = directive
            matching/directive-replacedbyvariable
            ;at this point in the query, ~varsubject should be bound to the node generated for subject
            ;but the binding for the replacement var can stem from a different ast (e.g., the one of the unmodified base program, or from a different meta-model) 
            (let [varoperand (symbol (nth opbindingvals 1))]
              `(;(cl/fresh [~compatiblevaluevar]
                 ;        (el/equals ~compatiblevaluevar (rewriting/clone-compatible-with-ast ~varoperand (.getAST ~varsubject)))
                 ;nil should be replaced by runtime snippet, like before
                 (el/perform 
                   (damp.ekeko.snippets.operators/snippet-jdt-replace ~snippetruntimevar ~varsubject  (rewriting/clone-compatible-with-ast ~varoperand (gAST ~varsubject))))
                 ;cannot use replace for this... astrewrite api 
                 ;(el/perform (rewrites/replace-node ~varsubject ~compatiblevaluevar))
                 ))
            matching/directive-replacedbyexp
            (let [sexpoperand (read-string (nth opbindingvals 1))]
              `((el/perform (damp.ekeko.snippets.operators/snippet-jdtvalue-replace ~snippetruntimevar ~varsubject ~sexpoperand))))
            )))
      replacedby-bounddirectives)))

 


;new strategy:
;generate conditions for instantiating template
;then generate querying conditions over the code in the instantiated template

(defn
  snippetgroup-conditions|rewrite
  [snippetgrouprhs lhsuservars]
  (let [snippets
        (snippetgroup/snippetgroup-snippetlist snippetgrouprhs)
        ;these vars hold the actual run-time snippet (different from match-var for root of run-time snippet)
        snippetsruntimevars
        (map (fn [snippet]
               (util/gen-lvar 'runtimesnippet))
             snippets)
      
        ;create run-time instance of snippet and an ast for its root
        instantiations
        (mapcat (fn [snippet runtimevar] 
                  (newnode-from-template snippet runtimevar))
                snippets
                snippetsruntimevars)
        
        ;bind match-vars of run-time snippet to ast components, such that they can be rewritten later on
        ;ignoring replacedbysexp and replacedbyvar
        conditions-on-instantiations
        (binding [rewriting/*sgroup-rhs* snippets] ; Because we want access to the RHS snippetgroup from rewrite directives.. TODO Should refactor this and pass around an (optional) snippetgroup param instead..
          (apply concat
                (map 
                  (fn [snippet snippetruntimevar] 
                    (let [root (snippet/snippet-root snippet)]
                      (snippet-conditions 
                        snippet
                        (fn [bounddirective]
                          (let [directive (directives/bounddirective-directive bounddirective)]
                            (not (or (matching/registered-replacedby-directive? directive)
                                     (rewriting/registered-rewriting-directive? directive)))))
                        (fn [val]
                          (concat
                            (snippet-node-conditions|replacedby snippet val snippetruntimevar) ;node replaced by var in template being instantiated
                            (when
                              (= val root)
                              (snippet-node-conditions|rewriting snippet val snippetruntimevar)))) ;program change
                               )))
                  snippets
                  snippetsruntimevars)))
        ;todo: fix this hack .. 
        ;none of these conditions should ground against the base program
        ;they are walking a newly generated piece of code
        ;for now, simply ignoring the first condition, which corresponds to the grounding of the root node
        ;other conditions use nongrounding variants of has etc
        conditions-on-instantiations-without-grounding-of-root-node
        (rest conditions-on-instantiations)
        
        
        instantiationconditionsvars
        (mapcat 
          (fn [snippet]
            (matching/snippet-node-matchvarsforproperties snippet (snippet/snippet-root snippet)))
          snippets)
        
        rootvars
        (into #{} (snippetgroup/snippetgroup-rootvars snippetgrouprhs)) 
        ;vars
        ;(into #{} (snippetgroup/snippetgroup-vars snippetgrouprhs))
        ;allvarsexceptrootsandlhsandusers
        ;(clojure.set/difference vars lhsuservars)
        ]
    `((cl/fresh [~@snippetsruntimevars ~@rootvars]; ~@allvarsexceptrootsandlhsandusers] 
                ~@instantiations
                (cl/fresh [~@instantiationconditionsvars]
                          ~@conditions-on-instantiations-without-grounding-of-root-node
                          )))))



(defn
  snippetgroup-matchvariables|normalized
  [snippetgroup]
  (map
    str
    (snippetgroup/snippetgroup-rootvars snippetgroup)))
  





(defn
  register-callbacks 
  []
  ; (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPETGROUP_QUERY) snippetgroup-query|usingpredicates)
  ; (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPET_QUERY) snippet-query|usingpredicate)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPETGROUP_NORMALIZED_MATCH_VARS) snippetgroup-matchvariables|normalized))

  

(register-callbacks)






