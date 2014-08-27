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
  ([snippet bounddirectivesfilterf]
    (let [ast (snippet/snippet-root snippet)
          query (atom '())]
      (snippet/walk-snippet-element
        snippet
        ast
        (fn [val]
          (when-not 
            (snippet-value-conditions-already-generated? snippet val)
            (swap! query concat (matching/snippet-node-conditions snippet val bounddirectivesfilterf)))))
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
  snippet-predicate
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
      (snippet-predicate snippet fname root-var uservars-exact vars conditions userconditions)))
  ([snippet fname matchvar uservars vars conditions userconditions]
    (if 
      (not-empty vars) 
      `(defn
         ~fname 
         [~matchvar ~@uservars]
         (cl/fresh [~@vars]
                   ~@conditions
                   ~@userconditions))
      `(defn
         ~fname 
         [~matchvar ~@uservars]
         (cl/all
           ~@conditions
           ~@userconditions)))))

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
        (snippet-uservars snippet)]
    `(do
       ~(snippet-predicate snippet)
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
        (map snippet-predicate snippets)
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

;BEGIN OLD VERSION based on entire template

(comment
  
(defn
  template-root|projected-eager 
  "Projects a template replacing each of its variables by a clone of their binding.
   All variables have to be bound."
  [template variables values]
  (let [var2value
        (zipmap variables values)
        node2var
        (damp.ekeko.snippets.matching/snippet-replacement-node2var template)
        
        val2exp 
        (damp.ekeko.snippets.matching/snippet-replacement-node2exp template)
        
        root
        (snippet/snippet-root template)
        ;either root or the value it should be replaced by (in case the root itself was replaced by a variable)
        ;(although this seems impossible, once replaced .. not sure what will happen with the other replacements)
        projected
        (if 
          (contains? node2var root)
          (get var2value (str (get node2var root)))
          root)]
      (doseq [[node var] node2var
              :when (not= node root)] ;already taken care of above
        (let [varstr (str var)]
          (when-not (contains? var2value varstr)
            (throw (IllegalArgumentException. (str "While instantiating template, encountered unbound variable: " varstr))))
          (let [value (get var2value varstr)]
            (cond 
               
              (astnode/ast? value)
              (damp.ekeko.snippets.operators/snippet-jdt-replace 
                template
                node 
                (org.eclipse.jdt.core.dom.ASTNode/copySubtree (.getAST projected) 
                                                              value))
              (astnode/lstvalue? value)
              (damp.ekeko.snippets.operators/snippet-jdtlist-replace 
                template
                node 
                (org.eclipse.jdt.core.dom.ASTNode/copySubtrees (.getAST projected)
                                                               (astnode/value-unwrapped value)))
              (or 
                (astnode/primitivevalue? value)
                (astnode/nilvalue? value))
              (damp.ekeko.snippets.operators/snippet-jdtvalue-replace template
                                                                      node
                                                                      value)
              :else
              (throw (IllegalArgumentException.
                       (str "While instantiating template, encountered illegal binding for variable: " varstr "->" value)))))))
      (doseq [[val exp] val2exp] ;arf .. somehow need to get variables out of here, and passed to this thing
        (let [newvalue (eval (read-string exp))]
          (damp.ekeko.snippets.operators/snippet-jdtvalue-replace template
                                                                  val
                                                                  newvalue)))
      projected))
          

(defn
  newnode-from-template
  [template]
  (let [root
        (snippet/snippet-root template)
        
        var-match 
        (snippet/snippet-var-for-node template root)
        
        replacement-vars|strings
        (matching/snippet-replacement-vars template) 
        
        replacement-vars|quotedstrings
        (map matching/to-literal-string replacement-vars|strings)
        
        replacement-vars|symbols
        (map symbol replacement-vars|strings)
        
        stemplate
        (persistence/snippet-as-persistent-string template)
        
        runtime-template-var  
        (util/gen-readable-lvar-for-value root)
        ]
    `((cl/fresh [~runtime-template-var] 
                (cl/== ~runtime-template-var
                       (persistence/snippet-from-persistent-string ~stemplate))
                
                ;(cl/== ~runtime-template-var ~template)
                (cl/project [~runtime-template-var ~@replacement-vars|symbols]
                            (cl/== ~var-match 
                                   (template-root|projected 
                                     ~runtime-template-var
                                     [~@replacement-vars|quotedstrings]
                                     [~@replacement-vars|symbols])
                                   )
                                   )
                ))))





;(defn-
;  snippet-conditions|rewrite
;  [snippet]
;  (let [root 
 ;       (snippet/snippet-root snippet)
 ;       conditions-codegeneration
;        (newnode-from-template snippet)
;        ;rewrite directives only feature at the root of a template
;        root-bounddirectives
;        (filter 
;          (fn [bounddirective]
;            (rewriting/registered-rewriting-directive? (directives/bounddirective-directive bounddirective)))
;          (snippet/snippet-bounddirectives-for-node snippet root))
;        conditions-rewriting
;        (mapcat
;          (fn [bounddirective]
;            (directives/snippet-bounddirective-conditions snippet bounddirective))
;          root-bounddirectives)]
;    (concat conditions-codegeneration conditions-rewriting)))


   
); END OLD VERSION based on entire template


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
  (let [query (atom '())]
    (snippet/walk-snippet-element
      snippet
      node
      (fn [val]
        (swap! query 
               concat
               (snippet-node-conditions|rewriting snippet val snippetruntimevar)
               (snippet-node-conditions|replacedby snippet val snippetruntimevar)
               )))
    @query))


;new strategy:
;generate conditions for instantiating template
;then generate querying conditions over the code in the instantiated template
;this way, there can also be rewriting directives that refer to non-root targets


(defn
  snippet-uservars
  [snippet]
  (sort (into #{}  (matching/snippet-vars-among-directivebindings snippet))))
  
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



;user vars need to have been declared already in lexical scope
;(defn
; match
; [?var ?template]
; (cl/fresh [?solutions ?solution]
;           substition-map
;   (fresh [?varseq]
;          (el/equals ?varseq (concat [?var] (snippet-uservars ?template)))
;          (el/equals nil (map println (map  
;          
;          )
;   
;   (el/equals ?solutions (eval (snippet-query ?template 'damp.ekeko/ekeko)))
;   (el/contains ?solutions ?solution)
;   (el/equals nil (println (count ?solution)))
;   ;(cl/== ?varseq ?solution)
;   (uservars ?template)
;   
;           ))






(defn
  snippetgroup-matchvariables|normalized
  [snippetgroup]
  (map
    str
    (snippetgroup/snippetgroup-rootvars snippetgroup)))
  

(def
  t 
  (matching/jdt-node-as-snippet (parsing/parse-string-expression "3")))




(defn
  register-callbacks 
  []
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPETGROUP_QUERY) snippetgroup-query|usingpredicates)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPET_QUERY) snippet-query|usingpredicate)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPETGROUP_NORMALIZED_MATCH_VARS) snippetgroup-matchvariables|normalized))

  

(register-callbacks)


