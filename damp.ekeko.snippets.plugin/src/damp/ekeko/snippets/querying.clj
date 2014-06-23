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
  snippet-value-conditions-already-generated?
  "Regexp matching of list elements will already have generated conditions for all their offspring
   (inside the corresponding qwal query)."
  [snippet value]
  (or
    (matching/snippet-value-regexp-offspring? snippet value)
    (matching/snippet-value-setmatch-offspring? snippet value)))


(defn
  snippet-conditions
  "Returns a list of logic conditions that will retrieve matches for the given snippet."
  [snippet]
  (let [ast (snippet/snippet-root snippet)
        query (atom '())]
    (snippet/walk-snippet-element
      snippet
      ast
      (fn [val]
        (when-not 
          (snippet-value-conditions-already-generated? snippet val)
          (swap! query concat (matching/snippet-node-conditions snippet val)))))
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


(defn
  snippet-predicate
  "Returns a logic goal retrieving matches for the given snippet."
  ([snippet]
    (let [root-var
          (snippet/snippet-var-for-root snippet)
          fname
          (symbol (str "match" root-var)) ;has to be same for call and definition
          uservars-exact
          (into #{} (matching/snippet-vars-among-directivebindings snippet))
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
          (into #{} (matching/snippet-vars-among-directivebindings snippet))]
      (snippet-predicatecall snippet  fname root-var uservars-exact))))


(defn
  snippet-query|usingpredicate
  [snippet ekekolaunchersymbol]
  (let [root-var
        (snippet/snippet-var-for-root snippet)
        uservars-exact
        (into #{} (matching/snippet-vars-among-directivebindings snippet))]
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
        additionalconditions)
      additionalrootvars)))


(defn-
  snippetgroup-query-with-conditions|usingpredicates
  [snippetgroup ekekolaunchersymbol conditions userconditions additionalrootvars]
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
        (into #{} (matching/snippetgroup-vars-among-directivebindings snippetgroup))]
    `(do
       ~@predicates
       (~ekekolaunchersymbol 
         [~@root-vars ~@uservars]
         ~@calls
         ~@userconditions))))
         
              
(defn
  snippetgroup-query|usingpredicates
  "Returns an Ekeko query that that will retrieve matches for the given snippet group."
  ([snippetgroup ekekolaunchersymbol]
    (snippetgroup-query|usingpredicates snippetgroup ekekolaunchersymbol '() '()))
  ([snippetgroup ekekolaunchersymbol additionalconditions additionalrootvars]
    (snippetgroup-query-with-conditions|usingpredicates 
      snippetgroup ekekolaunchersymbol 
      (snippetgroup-conditions snippetgroup) 
      (concat 
        (snippetgroup/snippetgroup-snippets-userqueries snippetgroup)
        additionalconditions)
      additionalrootvars)))

        

             

; Converting snippet group to rewrite query
;------------------------------------------

(defn
  template-root|projected 
  "Projects a template replacing each of its variables by a clone of their binding.
   All variables have to be bound."
  [template variables values]
  (let [var2value
        (zipmap variables values)
        node2var
        (damp.ekeko.snippets.matching/snippet-replacement-node2var template)
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
                                   ))
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





(defn
  snippet-node-conditions|rewriting
  [snippet value]
  (let [rewriting-bounddirectives
        (filter 
          (fn [bounddirective]
            (rewriting/registered-rewriting-directive? (directives/bounddirective-directive bounddirective)))
          (snippet/snippet-bounddirectives-for-node snippet value))
        conditions-rewriting
        (mapcat
          (fn [bounddirective]
            (directives/snippet-bounddirective-conditions snippet bounddirective))
          rewriting-bounddirectives)]
    (mapcat
         (fn [bounddirective]
           (directives/snippet-bounddirective-conditions snippet bounddirective))
         rewriting-bounddirectives)))

  

(defn 
  snippet-node-conditions+|rewriting
  [snippet node]
  (let [query (atom '())]
    (snippet/walk-snippet-element
      snippet
      node
      (fn [val]
        (swap! query concat (snippet-node-conditions|rewriting snippet val))))
    @query))


;new strategy:
;generate conditions for instantiating template
;then generate querying conditions over the code in the instantiated template
;this way, there can also be rewriting directives that refer to non-root targets


(defn
  snippetgroup-uservars
  [snippetgroup]
  (mapcat
    (fn [snippet]
      (matching/snippet-vars-among-directivebindings snippet))
    (snippetgroup/snippetgroup-snippetlist snippetgroup)))

(defn-
  snippetgroup-conditions|rewrite
  [snippetgrouprhs lhsuservars]
  (let [snippets
        (snippetgroup/snippetgroup-snippetlist snippetgrouprhs)
        instantiations
        (mapcat newnode-from-template snippets)
        conditions-on-instantiations
        (mapcat snippet-conditions snippets)
        changes
        (mapcat (fn [snippet] 
                  (snippet-node-conditions+|rewriting snippet (snippet/snippet-root snippet)))
                snippets)
        rootvars
        (into #{} (snippetgroup/snippetgroup-rootvars snippetgrouprhs)) ;already introduced in scope through (ekeko [..]
        uservars 
        (into #{} (snippetgroup-uservars snippetgrouprhs))
        vars
        (into #{} (snippetgroup/snippetgroup-vars snippetgrouprhs))
        allvarsexceptrootsandlhs
        (clojure.set/difference (clojure.set/union uservars vars)
                                (clojure.set/union lhsuservars rootvars))]
    `((cl/fresh [~@allvarsexceptrootsandlhs] 
           ~@instantiations
           ~@conditions-on-instantiations
           ~@changes))))


(defn
  transformation-query|usingpredicates
  [snippetgroup|lhs snippetgroup|rhs]
  (let [lhsuservars
        (snippetgroup-uservars snippetgroup|lhs)
        q (snippetgroup-query|usingpredicates
            snippetgroup|lhs 
            'damp.ekeko/ekeko* 
            (snippetgroup-conditions|rewrite snippetgroup|rhs (into #{} lhsuservars))
            (snippetgroup/snippetgroup-rootvars snippetgroup|rhs))]
    (clojure.pprint/pprint q)
    q))


(defn
  register-callbacks 
  []
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPETGROUP_QUERY) snippetgroup-query|usingpredicates)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPET_QUERY) snippet-query|usingpredicate))

(register-callbacks)


