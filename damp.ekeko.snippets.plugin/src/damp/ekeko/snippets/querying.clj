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
     [rewrites :as rewrites]
     [astnode :as astnode]]))

;; Converting a snippet to a query
;; -------------------------------


(defn
  snippet-value-conditions-already-generated?
  "Regexp matching of list elements will already have generated conditions for all their offspring
   (inside the corresponding qwal query)."
  [snippet value]
  (or
    (matching/snippet-value-regexp-offspring? snippet value)
    (matching/snippet-value-setmatch-offspring? snippet value)
    (matching/snippet-value-orimplicit-offspring? snippet value)
    (matching/snippet-value-orsimple-offspring? snippet value)
    ))



(defn
  snippet-conditions
  "Returns a list of logic conditions that will retrieve matches for the given snippet."
  ([snippet]
    (snippet-conditions snippet identity))
  ([snippet-orig bounddirectivesfilterf]
    (let [snippet (snippet/add-depth-info snippet-orig)
          ast (snippet/snippet-root snippet)
          query (atom '())]
      (snippet/walk-snippet-element
        snippet
        ast
        (fn [val]
          (when-not 
            (snippet-value-conditions-already-generated? snippet val)
            (swap! query concat (matching/snippet-node-conditions snippet val bounddirectivesfilterf)))))
      
      ; Partial matching CCC; reset the counter if we reach the end of the query
;      (swap! query concat [`(el/succeeds (do (matching/reset-matched-nodes) true))])
      @query)))

(declare snippet-uservars)
(declare snippetgroup-uservars)

(defn-
  snippet-query-with-conditions
  [snippet ekekolaunchersymbol conditions userconditions]
  (let [root-var (snippet/snippet-var-for-root snippet)
        uservars-exact (snippet-uservars snippet)
        vars (disj (into #{} (snippet/snippet-vars snippet)) root-var)]
    (if 
      (not-empty vars) 
      `(~ekekolaunchersymbol 
         [~root-var ~@uservars-exact]
         (cl/fresh [~@vars]
                   ~@conditions
                   ~@userconditions))
      `(~ekekolaunchersymbol 
         [~root-var ~@uservars-exact]
         ~@conditions
         ~@userconditions))))

(defn
  snippet-query
  "Returns an Ekeko query that that will retrieve matches for the given snippet."
  [snippet ekekolaunchersymbol]
  (snippet-query-with-conditions 
    snippet ekekolaunchersymbol 
    (snippet-conditions snippet) 
    (snippet/snippet-userquery snippet)))


(defn
  snippet-predicates
  "Returns a logic goal retrieving matches for the given snippet."
  ([snippet]
    (let [root-var
          (snippet/snippet-var-for-root snippet)
          fname
          (symbol (str "match" root-var)) ;has to be same for call and definition
          uservars-exact
          (snippet-uservars snippet)
          vars
          (disj (into #{} (snippet/snippet-vars snippet)) root-var)
          conditions
          (snippet-conditions snippet) 
          userconditions
          (snippet/snippet-userquery snippet)]
      (snippet-predicates snippet fname root-var uservars-exact vars conditions userconditions)))
  ([snippet fname matchvar uservars vars conditions userconditions]
    (let [allvars 
          (conj (concat uservars vars) matchvar)
          bodies
          (partition-all 50 conditions) ;;CUTOFF suffices to avoid "Method too Large" raised by Clojure compiler, but introduces overhead
          maxprednumber
          (count bodies)
          defines
          (map-indexed 
            (fn [idx body]  
              (let [predname (symbol (str fname "_" idx))
                    nextpredname (symbol (str fname "_" (inc idx)))]
                (if 
                  (< idx (dec maxprednumber))
                  `(defn
                     ~predname 
                     [varvector#]
                     (cl/fresh [~@allvars]
                               (cl/== varvector# [~@allvars])
                               ~@body
                               (~nextpredname varvector#)))
                  `(defn
                     ~predname 
                     [varvector#]
                     (cl/fresh [~@allvars]
                               (cl/== varvector# [~@allvars])
                               ~@body)))))
            bodies)
          revdefines
          (reverse defines)
          predname 
          (symbol (str fname "_" 0))
          define
          `(defn
             ~fname 
             [~matchvar ~@uservars]
             (cl/fresh [varvector# ~@vars]
                       (cl/== varvector# [~@allvars])
                       (~predname varvector#)
                       ~@userconditions))]
      `(~@revdefines ~define))))

(defn
  snippet-predicatecall 
  ([snippet fname matchvar uservars]
    `(~fname ~matchvar ~@uservars))
  ([snippet]
    (let [root-var
          (snippet/snippet-var-for-root snippet)
          fname
          (symbol (str "match" root-var)) ;has to be same for call and definition
          uservars-exact
          (snippet-uservars snippet)]
      (snippet-predicatecall snippet fname root-var uservars-exact))))


(defn
  snippet-query|usingpredicate
  [snippet ekekolaunchersymbol]
  (let [root-var
        (snippet/snippet-var-for-root snippet)
        uservars-exact
        (snippet-uservars snippet)
        defines
        (snippet-predicates snippet)]
    `(do 
       ~@defines
       (~ekekolaunchersymbol 
         [~root-var ~@uservars-exact]
         ~(snippet-predicatecall snippet)))))




; Converting snippet group to query
;------------------------------------


(defn-
   snippetgroup-conditions
   "Returns a list of logic conditions that will retrieve matches for the given snippet group."
   [snippetgroup]
   (mapcat snippet-conditions (snippetgroup/snippetgroup-snippetlist snippetgroup)))

(defn-
  snippetgroup-query-with-conditions
  ([snippetgroup ekekolaunchersymbol conditions userconditions]
    (snippetgroup-query-with-conditions snippetgroup ekekolaunchersymbol conditions userconditions '()))
  ([snippetgroup ekekolaunchersymbol conditions userconditions additionalrootvars]
    (let [root-vars 
          (concat (snippetgroup/snippetgroup-rootvars snippetgroup)
                  additionalrootvars)
         uservars-exact
         (snippetgroup-uservars snippetgroup)
          vars (into #{} (remove (set root-vars) (snippetgroup/snippetgroup-vars snippetgroup)))]
      (if 
        (not-empty vars) 
        `(~ekekolaunchersymbol 
           [~@root-vars ~@uservars-exact]
           (cl/fresh [~@vars]
                     ~@conditions
                     ~@userconditions))
        `(~ekekolaunchersymbol 
           [~@root-vars ~@uservars-exact]
           ~@conditions
           ~@userconditions)))))
        

(defn
  snippetgroup-query
  "Returns an Ekeko query that that will retrieve matches for the given snippet group."
  ([snippetgroup ekekolaunchersymbol]
    (snippetgroup-query snippetgroup ekekolaunchersymbol '() '()))
  ([snippetgroup ekekolaunchersymbol additionalconditions additionalrootvars]
    (snippetgroup-query-with-conditions 
      snippetgroup ekekolaunchersymbol 
      (snippetgroup-conditions snippetgroup) 
      (concat 
        (snippetgroup/snippetgroup-snippets-userqueries snippetgroup)
        additionalconditions)
      additionalrootvars)))


(defn-
  snippetgroup-query-with-conditions|usingpredicates
  [snippetgroup ekekolaunchersymbol conditions userconditions additionalrootvars hideuservars]
  (let [snippets
        (snippetgroup/snippetgroup-snippetlist snippetgroup)
        predicates
        (concat (mapcat snippet-predicates snippets))
        calls
        (map snippet-predicatecall snippets)
        root-vars 
        (concat (snippetgroup/snippetgroup-rootvars snippetgroup)
                additionalrootvars)
        uservars
        (snippetgroup-uservars snippetgroup)]
    (if
      hideuservars
      `(do
         ~@predicates
         (~ekekolaunchersymbol 
           [~@root-vars]
           (cl/fresh [~@uservars]
                     ~@calls
                     ~@userconditions)))
      `(do
         ~@predicates
         (~ekekolaunchersymbol 
           [~@root-vars ~@uservars]
           ~@calls
           ~@userconditions)))))
         
              
(defn
  snippetgroup-query|usingpredicates
  "Returns an Ekeko query that that will retrieve matches for the given snippet group."
  ([snippetgroup ekekolaunchersymbol hideuservars]
    (snippetgroup-query|usingpredicates snippetgroup ekekolaunchersymbol '() '() hideuservars))
  ([snippetgroup ekekolaunchersymbol additionalconditions additionalrootvars hideuservars]
    (snippetgroup-query-with-conditions|usingpredicates 
      snippetgroup ekekolaunchersymbol 
      (snippetgroup-conditions snippetgroup) 
      (concat 
        (snippetgroup/snippetgroup-snippets-userqueries snippetgroup)
        additionalconditions)
      additionalrootvars
      hideuservars
      )))


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
        (persistence/snippet-as-persistent-string template)]
    `((cl/== ~runtime-template-var (persistence/snippet-from-persistent-string ~stemplate))
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
                   (damp.ekeko.snippets.operators/snippet-jdt-replace ~snippetruntimevar ~varsubject  (rewriting/clone-compatible-with-ast ~varoperand (.getAST ~varsubject))))
                 ;cannot use replace for this... astrewrite api 
                 ;(el/perform (rewrites/replace-node ~varsubject ~compatiblevaluevar))
                 ))
            matching/directive-replacedbyexp
            (let [sexpoperand (read-string (nth opbindingvals 1))]
              `((el/perform (damp.ekeko.snippets.operators/snippet-jdtvalue-replace ~snippetruntimevar ~varsubject ~sexpoperand))))
            
            
            )))
      replacedby-bounddirectives
      )))


 
(defn 
  snippet-node-conditions+|rewritingandreplacedby
  [snippet node snippetruntimevar]
  (let [snippetchanges (atom '())
        programchanges (atom '())]
    (snippet/walk-snippet-element
      snippet
      node
      (fn [val]
        (swap! snippetchanges 
               concat
               (snippet-node-conditions|replacedby snippet val snippetruntimevar))
        (swap! programchanges
               concat
               (snippet-node-conditions|rewriting snippet val snippetruntimevar))
               ))
    (concat @snippetchanges @programchanges)))


;new strategy:
;generate conditions for instantiating template
;then generate querying conditions over the code in the instantiated template
;this way, there can also be rewriting directives that refer to non-root targets


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


(defn-
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
        (mapcat (fn [snippet] 
                  (snippet-conditions 
                    snippet
                    (fn [bounddirective]
                      (let [directive (directives/bounddirective-directive bounddirective)]
                        (not (or (matching/registered-replacedby-directive? directive)
                                 (rewriting/registered-rewriting-directive? directive)))
                        ))))
                  snippets)
        ;todo: fix this hack .. 
        ;none of these conditions should ground against the base program
        ;they are walking a newly generated piece of code
        ;for now, simply ignoring the first condition, which corresponds to the grounding of the root node
        ;other conditions use nongrounding variants of has etc
        conditions-on-instantiations-without-grounding-of-root-node
        (rest conditions-on-instantiations)
        
        changes
        (mapcat (fn [snippet snippetruntimevar] 
                  (snippet-node-conditions+|rewritingandreplacedby snippet (snippet/snippet-root snippet) snippetruntimevar)) 
                snippets snippetsruntimevars)
        
        rootvars
        (into #{} (snippetgroup/snippetgroup-rootvars snippetgrouprhs)) 
        uservars 
        (snippetgroup-uservars snippetgrouprhs) ;uservars should be added to the ekeko [..] instead of here 
        vars
        (into #{} (snippetgroup/snippetgroup-vars snippetgrouprhs))
        allvarsexceptrootsandlhsandusers
        (clojure.set/difference vars lhsuservars)]
    (println changes)

    `((cl/fresh [~@snippetsruntimevars ~@rootvars  ~@allvarsexceptrootsandlhsandusers] 
           ~@instantiations
           ~@conditions-on-instantiations-without-grounding-of-root-node
           ~@changes))))


(defn
  transformation-query|usingpredicates
  [snippetgroup|lhs snippetgroup|rhs]
  (let [lhsuservars
        (snippetgroup-uservars snippetgroup|lhs)
        q 
        (snippetgroup-query|usingpredicates
          snippetgroup|lhs 
          'damp.ekeko/ekeko ;TODO: serious bug in GUI, getting indexoutofbounds exception when using ekeko*  
          (snippetgroup-conditions|rewrite snippetgroup|rhs (into #{} lhsuservars))
          (snippetgroup-uservars snippetgroup|rhs)
          false)]
    (clojure.pprint/pprint q)
    q))


(defn
  uservars
  [template]
  (fn [a]
    (let [snippet
          (clojure.core.logic.protocols/walk a template)
          uservars 
          (snippet-uservars snippet)]
      
      (doseq [uservar  uservars]
        (println
          (clojure.core.logic.protocols/walk
            a
            (cl/lvar uservar false))))
      
      
          
      
    
    
      ))
  
  )


(defn
  snippetgroup-matchvariables|normalized
  [snippetgroup]
  (map
    str
    (snippetgroup/snippetgroup-rootvars snippetgroup)))
  





(defn
  register-callbacks 
  []
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPETGROUP_QUERY) snippetgroup-query|usingpredicates)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPET_QUERY) snippet-query|usingpredicate)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPETGROUP_NORMALIZED_MATCH_VARS) snippetgroup-matchvariables|normalized))

  

(register-callbacks)






