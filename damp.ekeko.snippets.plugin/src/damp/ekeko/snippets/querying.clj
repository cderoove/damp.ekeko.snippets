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
    [damp.ekeko [logic :as el]]))

;; Converting a snippet to a query
;; -------------------------------


(defn-
  snippet-conditions
  "Returns a list of logic conditions that will retrieve matches for the given snippet."
  [snippet]
  (defn 
    conditions
    [ast-or-list]
    (let [bounddirectives
          (snippet/snippet-bounddirectives-for-node snippet ast-or-list)
          bounddirectives-grounding
          (filter (fn [bounddirective]
                    (matching/registered-grounding-directive? (directives/bounddirective-directive bounddirective)))
                  bounddirectives)
          bounddirectives-constraining
          (filter (fn [bounddirective]
                    (matching/registered-constraining-directive? (directives/bounddirective-directive bounddirective)))
                  bounddirectives)
          conditions-grounding
          (mapcat
            (fn [bounddirective]
              (directives/snippet-bounddirective-conditions snippet bounddirective))
            bounddirectives-grounding)
          conditions-constraining
          (mapcat
            (fn [bounddirective]
              (directives/snippet-bounddirective-conditions snippet bounddirective))
            bounddirectives-constraining)]
      (concat conditions-grounding conditions-constraining)))
  (let [ast (snippet/snippet-root snippet)
        query (atom '())]
    (util/walk-jdt-node 
      ast
      (fn [astval]  (swap! query concat (conditions astval)))
      (fn [lstval] (swap! query concat (conditions lstval)))
      (fn [primval] (swap! query concat (conditions primval)))
      (fn [nilval] (swap! query concat (conditions nilval))))
    @query))



(defn-
  snippet-query-with-conditions
  [snippet ekekolaunchersymbol conditions userconditions]
  (let [root-var (snippet/snippet-var-for-root snippet)
        uservars-exact (into #{} (matching/snippet-vars-among-directivebindings snippet))
        ;uservars-var (into #{} (matching/snippet-replacement-vars snippet))
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
        uservars-exact (into #{} (matching/snippetgroup-vars-among-directivebindings snippetgroup))
        ;uservars-var (into #{} (snippetgroup/snippetgroup-uservars-for-variable snippetgroup))
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
        additionalconditions
        )
      additionalrootvars
      )))


; Converting snippet group to rewrite query
;------------------------------------------


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
                (cl/project [~runtime-template-var ~@replacement-vars|symbols]
                            (cl/== ~var-match 
                                   (parsing/parse-string-ast
                                     (runtime/template-to-string|projected 
                                       ~runtime-template-var
                                       [~@replacement-vars|quotedstrings]
                                       [~@replacement-vars|symbols])
                                     )))
                ))))




(defn-
  snippet-conditions|rewrite
  [snippet]
  (let [root 
        (snippet/snippet-root snippet)
        conditions-codegeneration
        (newnode-from-template snippet)
        ;rewrite directives only feature at the root of a template
        root-bounddirectives
        (filter 
          (fn [bounddirective]
              (rewriting/registered-rewriting-directive? (directives/bounddirective-directive bounddirective)))
          (snippet/snippet-bounddirectives-for-node snippet root))
        conditions-rewriting
        (mapcat
            (fn [bounddirective]
              (directives/snippet-bounddirective-conditions snippet bounddirective))
            root-bounddirectives)]
    (concat conditions-codegeneration conditions-rewriting)))


;todo: user-defined variables

(defn-
  snippetgroup-conditions|rewrite
  [snippetgroup]
  (mapcat snippet-conditions|rewrite (snippetgroup/snippetgroup-snippetlist snippetgroup)))
  
(defn
  transformation-query
  [snippetgroup|lhs snippetgroup|rhs]
  (let [q (snippetgroup-query snippetgroup|lhs 
                              'damp.ekeko/ekeko* 
                              (snippetgroup-conditions|rewrite snippetgroup|rhs)
                              (snippetgroup/snippetgroup-rootvars snippetgroup|rhs)
                              )]
    (println q)
    q))


(defn
  register-callbacks 
  []
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPETGROUP_QUERY) snippetgroup-query)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPET_QUERY) snippet-query))

(register-callbacks)


