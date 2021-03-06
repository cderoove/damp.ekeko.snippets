(ns
  ^{:doc "Matching directives for template-based program transformation."
    :author "Coen De Roover, Siltvani"}
damp.ekeko.snippets.matching
  (:require [clojure.core.logic :as cl])
  (:require [clojure.zip :as zip])
  (:require [damp.ekeko.snippets 
             [directives :as directives]
             [util :as util]
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [parsing :as parsing]
             [runtime :as runtime]])
  (:require
    [damp.ekeko [logic :as el]]
    [damp.ekeko.jdt 
     [astnode :as astnode]
     [ast :as ast]
     [structure :as structure]
     [aststructure :as aststructure]
     ])
  (:import 
    [java.util List]
    [org.eclipse.jdt.core.dom.rewrite ASTRewrite]
    [org.eclipse.jdt.core.dom ASTNode MethodInvocation Expression Statement BodyDeclaration CompilationUnit ImportDeclaration]))

(declare directive-equals)

;;These expose flaws in the query generation process
(defn
  nongrounding-value-raw
  [?value ?raw]
  (el/equals ?raw (astnode/value-unwrapped ?value)))

(defn
  nongrounding-value|null
  [?value]
  (el/succeeds (astnode/nilvalue? ?value)))

(defn
  nongrounding-equivalent
  [?a ?b]
  (cl/conde 
    [(cl/== ?a ?b)]
    [(el/succeeds (= (.toString ?a) (.toString ?b)))]))

(defn
  nongrounding-value|list
  [?value]
  (el/succeeds (astnode/lstvalue? ?value)))

(defn
  nongrounding-has
  [?keyw ?owner ?value]
  (cl/conda 
    [(el/v- ?owner)
     (el/perform (throw (Exception. (str "Generated query should have already ground the owner of the property. "
                                         ?keyw 
                                         ?owner
                                         ?value))))]
    [(el/v+ ?owner)
     (ast/has ?keyw ?owner ?value)]))

(defn
  nongrounding-ast
  [?keyw ?node]
  (cl/conda 
    [(el/v- ?node)
     (el/perform (throw (Exception. (str "Generated query should have already ground this node."
                                         ?keyw
                                         ?node))))]
    [(el/v+ ?node)
     (ast/ast ?keyw ?node)]))


(def ast `ast/ast)


;These can be safely replaced once query generation is completely sound.
;(def has `ast/has)
(def has `nongrounding-has)


;(def value-raw `ast/value-raw)
(def value-raw `nongrounding-value-raw)


;(def value|null `ast/value|null)
(def value|null `nongrounding-value|null)

(def value|list `nongrounding-value|list)



(def to-modifier-keyword `runtime/to-modifier-keyword)
(def to-primitive-type-code `runtime/to-primitive-type-code)
(def to-assignment-operator `runtime/to-assignment-operator)
(def to-infix-expression-operator `runtime/to-infix-expression-operator)
(def to-prefix-expression-operator `runtime/to-prefix-expression-operator)
(def to-postfix-expression-operator `runtime/to-postfix-expression-operator)

(def generic-nongrounding-ast `nongrounding-ast)
(declare ast-primitive-as-expression)

;; Aux
;; ---

(defn
  root-of-snippet?
  [value snippet]
  (= (snippet/snippet-root snippet) value))


(defn
  represents-variable?
  [string]
  (@#'el/ekeko-lvar-sym? string))


(defn 
  freshornot
  [vars conditions]
  (if 
    (seq conditions)
    (if 
      (seq vars)
      `((cl/fresh [~@vars] ~@conditions))
      conditions)
    '()))


;; Grounding Functions
;; -------------------


(declare directive-child)
(declare snippet-list-containing)
(declare snippet-parent-match|set?)
(declare valuelistmember?)

(defn 
  ground-relativetoparent
  [snippet-val]
  (fn [snippet]
    (let [var-match
          (snippet/snippet-var-for-node snippet snippet-val)] 
      (cond 
        ;root of snippet
        (root-of-snippet? snippet-val snippet)
        (let [snippet-ast-keyw
              (astnode/ekeko-keyword-for-class-of snippet-val)]
          `((~ast ~snippet-ast-keyw ~var-match)))
        ;member of list (no set matching on list)
        (and (valuelistmember? snippet snippet-val) (not (snippet-parent-match|set? snippet snippet-val)))
        (let [bounddirectives
              (snippet/snippet-bounddirectives-for-node snippet snippet-val)
              list-owner  
              (snippet-list-containing snippet snippet-val)
              list-owner-directives 
              (snippet/snippet-bounddirectives-for-node snippet list-owner)
              list-match       
              (snippet/snippet-var-for-node snippet list-owner)
              list-raw  
              (snippet/snippet-node-children|conceptually-refs snippet list-owner)
              index-match     
              (.indexOf ^List list-raw snippet-val)]
          ;could check for parent list directives that might already have ground the element
          ;but for now rely on operators to switch correctly between directives (and e.g., remove ground-relative-to-parent from all list elements)
          `((runtime/list-nth-element ~list-match ~index-match ~var-match)))
        ;member of list (set matching on list)
        (and (valuelistmember? snippet snippet-val) (snippet-parent-match|set? snippet snippet-val))
        `() ; The list node has already done the grounding work for us..
        (or 
          (astnode/ast? snippet-val)
          (astnode/lstvalue? snippet-val)
          (astnode/nilvalue? snippet-val))
        (let [owner 
              (if (snippet/has-ignore-parent snippet snippet-val)
                (astnode/owner (astnode/owner snippet-val))
                (astnode/owner snippet-val))
              owner-match
              (snippet/snippet-var-for-node snippet owner)
              owner-property
              (if (snippet/has-ignore-parent snippet snippet-val)
                (astnode/owner-property (astnode/owner snippet-val))
                (astnode/owner-property snippet-val))
              owner-property-keyword
              (astnode/ekeko-keyword-for-property-descriptor owner-property)]
          `((~has ~owner-property-keyword ~owner-match ~var-match)))
        (astnode/primitivevalue? snippet-val)
        `() ;constraining the parent has already ground primitive values
        :default
        (throw (Exception. (str "Unexpected snippet element to create constraining conditions for." snippet-val)))))))

;child+
(defn
  ground-relativetoparent+ 
  ;arity 0: reside within arbitraty depth for their normal ground-relativetoparent match
  [val]
  (fn [snippet]
    (let [var-match (snippet/snippet-var-for-node snippet val)
          conditions-regular ((ground-relativetoparent val) snippet)
          var-match-regular (util/gen-lvar "nonTranMatch")
          in-match|set (and (valuelistmember? snippet val) (snippet-parent-match|set? snippet val))]
      (if in-match|set
        (let [list-owner (snippet-list-containing snippet val)
              lstvar (:lstvar (meta snippet))
              remaining (:remaininglstvar (meta snippet))] 
          `((cl/fresh [~var-match-regular]
                      (runtime/rawlist-element-remaining ~lstvar ~var-match-regular ~remaining)
                      (cl/fresh [~var-match] 
                                ~@conditions-regular
                                (cl/== ~var-match ~var-match-regular))
                      (ast/astorvalue-offspring+ ~var-match-regular ~var-match))))
        ;these conditions will refer to var-match, therefore shadow var-match
        ;such that is is completely fresh
        `((cl/fresh [~var-match-regular]
                    (cl/fresh [~var-match] 
                              ~@conditions-regular
                              (cl/== ~var-match ~var-match-regular))
                    (ast/astorvalue-offspring+ ~var-match-regular ~var-match)))))))

;child* 
(defn
  ground-relativetoparent*
  [val]
  (fn [snippet]
    (let [var-match-regular (util/gen-lvar "nonTranMatch")
          var-match (snippet/snippet-var-for-node snippet val)
          conditions-regular ((ground-relativetoparent val) snippet)
          in-match|set (and (valuelistmember? snippet val) (snippet-parent-match|set? snippet val))]
      (if in-match|set
        (let [list-owner (snippet-list-containing snippet val)
              lstvar (:lstvar (meta snippet))
              remaining (:remaininglstvar (meta snippet))] 
          `((cl/fresh [~var-match-regular]
                      (runtime/rawlist-element-remaining ~lstvar ~var-match-regular ~remaining)
                      (cl/fresh [~var-match]
                                ~@conditions-regular
                                (cl/== ~var-match ~var-match-regular))
                      (cl/conde 
                        [(cl/== ~var-match-regular ~var-match)]
                        [(ast/astorvalue-offspring+ ~var-match-regular ~var-match)]))))
        ; Regular case
        `((cl/fresh [~var-match-regular]
                    (cl/fresh [~var-match] 
                              ~@conditions-regular
                              (cl/== ~var-match ~var-match-regular))
                    (cl/conde 
                      [(cl/== ~var-match-regular ~var-match)]
                      [(ast/astorvalue-offspring+ ~var-match-regular ~var-match)])))))))


(defn
  ground-element
  [val]
  (fn [template]
    (let [match
          (snippet/snippet-var-for-node template val)
          lst 
          (snippet-list-containing template val)
          list-match
          (snippet/snippet-var-for-node template lst)
          list-match-raw
          (util/gen-lvar "lstraw")]
      `((cl/fresh [~list-match-raw] 
                  (~value-raw ~list-match ~list-match-raw)
                  (el/contains ~list-match-raw ~match))))))






;(;arity 1: reside within arbitraty depth of given variable binding (should be ancestor)
;  [snippet-val ancestorvar]
;  (fn [snippet]
;    (let [var-match (snippet/snippet-var-for-node snippet snippet-val)
;          var (symbol ancestorvar)
;          var-match-owner (snippet/snippet-var-for-node snippet (astnode/owner snippet-val))]
;      `((runtime/ground-relativetoparent+|match-ownermatch-userarg  ~var-match ~var-match-owner ~var))))))


;; Constraining Functions
;; ----------------------

;;TODO: not in sync with node-filtered-ekeko-properties (or something like that)


(defn 
  is-ignored-property?
  [property-keyw]
  (= property-keyw :javadoc))



;todo: er is geen ast-conditie meer om type te checken, moet die er wel nog komen voor variabelen?
(defn 
  constrain-exact
  [snippet-val]
  (fn [snippet]
    (let [var-match
          (snippet/snippet-var-for-node snippet snippet-val)]
      (cond
        ;constrain primitive-valued properties of node
        (astnode/ast? snippet-val)
        (let [ast-keyw
              (astnode/ekeko-keyword-for-class-of snippet-val)
              snippet-properties 
              (astnode/node-ekeko-properties snippet-val)
              child-conditions 
              (mapcat
                (fn [[property-keyw retrievalf]]
                  (let [value     
                        (retrievalf snippet-val) 
                        var-value
                        (snippet/snippet-var-for-node snippet value)
                        property-descriptor
                        (astnode/node-property-descriptor-for-ekeko-keyword snippet-val property-keyw)]
                    (if
                      (or 
                        (is-ignored-property? property-keyw)
                        (not (astnode/property-descriptor-simple? property-descriptor)))
                      `()
                      `((~has ~property-keyw ~var-match ~var-value)))))
                snippet-properties)]
          ;ast should not ground, only perform an instanceof check ;TODO: make separate predicate
          `((~generic-nongrounding-ast ~ast-keyw ~var-match)
             ~@child-conditions))
        ;constrain lists
        (astnode/lstvalue? snippet-val)
        (let [lst
              (snippet/snippet-node-children|conceptually-refs snippet snippet-val)
              snippet-list-size 
              (.size ^List lst)]
          `((~nongrounding-value|list ~var-match)
             (runtime/list-size ~var-match ~snippet-list-size)))
        ;constrain primitive values
        (astnode/primitivevalue? snippet-val)
        (let [exp 
              (ast-primitive-as-expression (astnode/value-unwrapped snippet-val))]
          `(;(ast/value|primitive ~var-match)
             (~value-raw ~var-match ~exp)))
        ;constrain null-values
        (astnode/nilvalue? snippet-val)
        `((~value|null ~var-match))))))


(defn-
  replace-conditions
  [query conditionpred replacementf]
  (loop [loc (zip/seq-zip query)]
    (if 
      (zip/end? loc)
      (zip/root loc)
      (recur 
        (zip/next 
          (if
            (conditionpred (zip/node loc))
            (zip/replace loc (replacementf (zip/node loc)))
            loc))))))

(declare  snippet-node-conditions)

(declare snippet-node-conditions+)

(defn 
  constrain-orimplicit
  "Like constrain-exact for MethodInvocation receivers, but allows implicit this-receiver."
  [snippet-val]
  (fn [snippet]
    (let [exactnodeconditions
          ((constrain-exact snippet-val) snippet)
          offspringconditions
          (snippet-node-conditions+ snippet snippet-val)
          normalconditions
          (concat exactnodeconditions offspringconditions)
          var-match
          (snippet/snippet-var-for-node snippet snippet-val)]          
      `((cl/conde [~@normalconditions]
                  [(~value|null ~var-match)])))))

(defn 
  constrain-notnil
  "Like constrain-exact for MethodInvocation receivers, but allows implicit this-receiver."
  [snippet-val]
  (fn [snippet]
    (let [exactnodeconditions
          ((constrain-exact snippet-val) snippet)
          offspringconditions
          (snippet-node-conditions+ snippet snippet-val)
          var-match
          (snippet/snippet-var-for-node snippet snippet-val)
          nilcheck
          `(el/succeeds (not (astnode/nilvalue? ~var-match)))
          allconditions
          (concat exactnodeconditions offspringconditions)]
      `( ~nilcheck
         ~@allconditions))))

(defn
  constrain-orsimple
  "Allows simple types and names to match their fully qualified equivalent in the template."
  [snippet-val]
  (fn [snippet]
    (let [exactnodeconditions
          ((constrain-exact snippet-val) snippet)
          offspringconditions
          (snippet-node-conditions+ snippet snippet-val)
          normalconditions
          (concat exactnodeconditions offspringconditions)
          var-match
          (snippet/snippet-var-for-node snippet snippet-val)
          val-string
          (.toString snippet-val)
          var-match-type
          (util/gen-lvar "type")
          ]
      `((cl/conde [~@normalconditions]
                  [(cl/fresh [~var-match-type]
                             (aststructure/ast|type-type ~var-match ~var-match-type)
                             (structure/type-name|qualified|string ~var-match-type ~val-string))])))))

(declare represents-variable?)

(defn 
  conditions-variables
  [conditions]
  (filter represents-variable? (flatten conditions)))


(declare snippet-value-multiplicity)


   

(defn
  constrain-lst|regexp 
  "Requires candidate matches to match a regular expresison. 
   Will ground elements. Therefore, elements should no longer have a grounding directive.
   Constraining conditions of elements will be feature as conditions inside the regular expression."
  [val]
  (fn [template]
    (let [var-match
          (snippet/snippet-var-for-node template val)
          var-match-qwalgraph
          (util/gen-lvar "qgraph")
          var-match-qwalgraph-start
          (util/gen-lvar "qgraphstart")
          var-match-qwalgraph-end
          (util/gen-lvar "qgraphend")
          
          elements 
          (snippet/snippet-node-children|conceptually-refs template val)
          
          idx-last 
          (dec (.size ^List elements))
          
          element-conditions
          (apply concat
                 (map-indexed
                   (fn [idx element]
                     (let [var-elmatch                    
                           (snippet/snippet-var-for-node template element)
                           elmatchidx
                           (gensym 'elmatchidx)
                           elmatch 
                           (gensym 'elmatch)
                           
                           multiplicity
                           (snippet-value-multiplicity template element)
                           
                           ;these conditions no longer need to be included in the query 
                           ;querying/snippet-conditions takes care of this
                           ;using snippet-value-conditions-already-generated? predicate
                           elconditions 
                           (snippet-node-conditions template element)
                           
                           elconditionsvars
                           (conj (conditions-variables elconditions)
                                 var-elmatch)
                           
                           qcurrentcondition
                           (if 
                             (= "1" multiplicity)
                             `(damp.qwal/qcurrent [[~elmatchidx ~elmatch]]
                                                  (cl/== ~var-elmatch ~elmatch) 
                                                  ~@elconditions)
                             `(damp.qwal/qcurrent [[~elmatchidx ~elmatch]]
                                                  ;same var cannot match different elements multiple times, hence fresh
                                                  (cl/fresh [~@elconditionsvars] ;perhaps not include uservars
                                                            (cl/== ~var-elmatch ~elmatch) 
                                                            ~@elconditions)))]
                       (condp = multiplicity
                         "1" 
                         `(~qcurrentcondition 
                            damp.qwal/q=>) ;to prepare for matching of next template element
                         "+"
                         `((damp.qwal/q+ 
                             ~qcurrentcondition
                             damp.qwal/q=> ;to prepare for matching of next template element
                             ))
                         "*"
                         `((damp.qwal/q* 
                             ~qcurrentcondition
                             damp.qwal/q=> ;to prepare for matching of next template element
                             ))
                         )))
                   elements))]
      `((cl/fresh 
          [~var-match-qwalgraph ~var-match-qwalgraph-start ~var-match-qwalgraph-end]
          (damp.ekeko.snippets.runtime/value|list-qwal-start-end
            ~var-match
            ~var-match-qwalgraph
            ~var-match-qwalgraph-start
            ~var-match-qwalgraph-end)
          (damp.qwal/qwal   
            ~var-match-qwalgraph
            ~var-match-qwalgraph-start
            ~var-match-qwalgraph-end
            [] 
            damp.qwal/q=> ;to jump inside list 
            ~@element-conditions))))))


;todo: eliminate code duplication
(defn
  constrain-lst|cfgregexp 
  "Requires control flow graph of this statement list to to match a regular expression. 
   Will ground elements. Therefore, elements should no longer have a grounding directive.
   Constraining conditions of elements will be feature as conditions inside the regular expression."
  [val]
  (fn [template]
    (let [block
          (snippet/snippet-node-parent|conceptually template val)
          method 
          (snippet/snippet-node-parent|conceptually template block)
          var-method
          (snippet/snippet-var-for-node template method)
          
          var-match
          (snippet/snippet-var-for-node template val)
          var-match-qwalgraph
          (util/gen-lvar "qgraph")
          var-match-qwalgraph-start
          (util/gen-lvar "qgraphstart")
          var-match-qwalgraph-end
          (util/gen-lvar "qgraphend")
          
          elements 
          (snippet/snippet-node-children|conceptually-refs template val)
          
          cfgnode
          (gensym 'cfgnode)
          
          element-conditions
          (mapcat
            (fn [element]
              (let [var-elmatch                    
                    (snippet/snippet-var-for-node template element)
                    
                    elmatch 
                    (gensym 'elmatch)
                           
                    multiplicity
                    (snippet-value-multiplicity template element)
                           
                    ;these conditions no longer need to be included in the query 
                    elconditions 
                    (snippet-node-conditions template element)
                           
                    elconditionsvars
                    (conj (conditions-variables elconditions)
                          var-elmatch)
                    
                    cfgnode
                    (gensym 'cfgnode)
                           
                    qcurrentcondition
                    (if 
                      (= "1" multiplicity)
                      `(damp.qwal/qcurrent [~elmatch]
                                           (ast/node|cfg-node|ast ~elmatch ~var-elmatch)
                                           ~@elconditions)
                      `(damp.qwal/qcurrent [~elmatch]
                                           ;same var cannot match different elements multiple times, hence fresh
                                           (cl/fresh [~@elconditionsvars] ;perhaps not include uservars
                                                     (ast/node|cfg-node|ast ~elmatch ~var-elmatch)
                                                     ~@elconditions)))]
                (condp = multiplicity
                  "1" 
                  `(~qcurrentcondition 
                     damp.qwal/q=>  ;to prepare for matching of next template element
                     (damp.qwal/q? (damp.qwal/qcurrent [~cfgnode] (ast/node|cfg|syntethic ~cfgnode)) damp.qwal/q=>)) ;also skip possible synthetic nodes
                  "+"
                  `((damp.qwal/q+ 
                      ~qcurrentcondition
                      damp.qwal/q=>  ;to prepare for matching of next template element
                      (damp.qwal/q? (damp.qwal/qcurrent [~cfgnode] (ast/node|cfg|syntethic ~cfgnode)) damp.qwal/q=>))) ;also skip possible synthetic nodes
                  "*"
                  `((damp.qwal/q* 
                      ~qcurrentcondition
                      damp.qwal/q=>  ;to prepare for matching of next template element
                      (damp.qwal/q? (damp.qwal/qcurrent [~cfgnode] (ast/node|cfg|syntethic ~cfgnode)) damp.qwal/q=>))) ;also skip possible synthetic nodes
                  )))
            elements)]
      `((cl/fresh 
          [~var-match-qwalgraph ~var-match-qwalgraph-start ~var-match-qwalgraph-end]
          (ast/method-cfg-entry-exit ~var-method ~var-match-qwalgraph ~var-match-qwalgraph-start ~var-match-qwalgraph-end)
          (damp.qwal/qwal   
            ~var-match-qwalgraph
            ~var-match-qwalgraph-start
            ~var-match-qwalgraph-end
            [] 
            ;damp.qwal/q=> ;for now, not jumping inside cfg  ... (although this will cause ..* to match incorrectly)
            ~@element-conditions
            (damp.qwal/qcurrent [~cfgnode] (ast/node|cfg|syntethic ~cfgnode))  ;before-last node is a synthetic uber-return or throws 
            damp.qwal/q=> ;to skip to the actual last node
            ))))))


;todo: allow additional operand that determines where the list comes from
;e.g. super declarations


(declare snippet-child*+?)

(defn
  constrain-inherited
  "Similar to constrain-lst|set, but applies only to type declarations,
   includes inherited members, and ignores the ordering of these members"
  [val]
  (fn [template]
    (let [typedecl (snippet/snippet-node-parent|conceptually template val)
          typedeclvar (snippet/snippet-var-for-node template typedecl)
          lstvar (snippet/snippet-var-for-node template val)
          elements (snippet/snippet-node-children|conceptually-refs template val)
          listrawvar (util/gen-lvar "lstraw")
          
          element-conditions 
          (mapcat
            (fn [elem]
              (let [elemvar (snippet/snippet-var-for-node template elem)
                    normal-conditions (snippet-node-conditions template elem)]
                (concat
;                  `((el/succeeds (do (println (.getName ~typedeclvar)) true)))
                  `((aststructure/typedeclaration-member ~typedeclvar ~elemvar))
                  `((cl/all ~@normal-conditions))
                  )))
            elements)
          
          ]
      `((~nongrounding-value|list ~lstvar)
         (cl/fresh [~listrawvar]
                   (~value-raw ~lstvar ~listrawvar)
                   ~@element-conditions)))))

(defn
  constrain-lst|set
  "Considers candidate matches as a set in which elements are matched.
   Once an element from the list has been matched, remaining template elements cannot match it anymore.
   Will ground elements. Therefore, elements should no longer have a grounding directive.
   Constraining conditions of elements will feature as conditions for the set matching."
  [val]
  (fn [template]
    (let [lstvar
          (snippet/snippet-var-for-node template val)
          elements
          (snippet/snippet-node-children|conceptually-refs template val)
          listrawvar
          (util/gen-lvar "lstraw")]
      (letfn [(generate [lstvar elements]
                (if 
                  (empty? elements)
                  '()
                  (let [element 
                        (first elements)
                        
                        elmatch                    
                        (snippet/snippet-var-for-node template element)
                        
                        remaininglstvar (gensym "remaining")
                        
                        elconditions ;snippet-node-conditions does not recurse into elements of lists with set matching
                        (snippet-node-conditions (with-meta template {:lstvar lstvar :remaininglstvar remaininglstvar}) element)
                        ]
                    (if (snippet-child*+? template element)
                      (freshornot [remaininglstvar elmatch]
                                  (concat
                                    elconditions
                                    (generate remaininglstvar (rest elements))))
                      (freshornot [remaininglstvar elmatch]
                                  (concat
                                    `((runtime/rawlist-element-remaining ~lstvar ~elmatch ~remaininglstvar))
                                    elconditions
                                    (generate remaininglstvar (rest elements))))))))]
        `((~nongrounding-value|list ~lstvar)
           (cl/fresh [~listrawvar]
                     (~value-raw ~lstvar ~listrawvar)
                     ~@(generate listrawvar elements)))))))


(defn
  constrain-size|atleast
  "Requires candidate matches to have at least as many elements as the template list."
  [val]
  (fn [template]
    (let [var-match
          (snippet/snippet-var-for-node template val)
          lst
          (snippet/snippet-node-children|conceptually-refs template val)
          template-list-size 
          (.size ^List lst)
          var-match-raw (util/gen-lvar "lstraw")]
      `((~nongrounding-value|list ~var-match)
         (cl/fresh [~var-match-raw] 
                   (~value-raw ~var-match ~var-match-raw)
                   (el/succeeds (>= (.size ~var-match-raw) ~template-list-size)))))))

(defn
  constrain-emptybody
  "Requires a Block to be nil or have no statements."
  [val]
  (fn [template]
    (let [var-match (snippet/snippet-var-for-node template val)
          stmts-match (util/gen-lvar "stmts")]
      `((cl/conde
          [(~value|null ~var-match)]
          [(cl/fresh [~stmts-match]
                     (~has :statements ~var-match ~stmts-match)
                     (~nongrounding-value|list ~stmts-match)
                     (damp.ekeko.snippets.runtime/list-size ~stmts-match 0))
           ]
          )))))

(defn
  ground-orblock 
  [val]
  (fn [snippet]
    (let [var-match (snippet/snippet-var-for-node snippet val)
          conditions-regular ((ground-relativetoparent val) snippet)
          var-match-regular (util/gen-lvar "nonTranMatch")
          var-stmts (util/gen-lvar "stmts")
          var-slist (util/gen-lvar "slist")
          ]
      `((cl/fresh [~var-match-regular]
                  (cl/fresh [~var-match] 
                           ~@conditions-regular
                           (cl/== ~var-match ~var-match-regular))
                  (cl/conde 
                    [(cl/== ~var-match-regular ~var-match)]
                    
                    [(cl/fresh [~var-slist ~var-stmts]
                               (ast/ast :Block ~var-match-regular)
                               (ast/has :statements ~var-match-regular ~var-slist)
                               (ast/value-raw ~var-slist ~var-stmts)
                               (el/contains ~var-stmts ~var-match)
                               )]
                    
;                    [(ast/astorvalue-offspring+ ~var-match-regular ~var-match)]
                    ))))))

(defn
  constrain-refersto
  "Requires candidate matches to refer to the given variable."
  [val var-string]
  (fn [template]
    (let [var-match
          (snippet/snippet-var-for-node template val)
          var
          (symbol var-string)]
      `((runtime/refersto ~var-match ~var)))))

(defn
  constrain-referredby
  "Requires candidate match variables to be referred by the given variable node."
  [val var-string]
  (fn [template]
    (let [var-match
          (snippet/snippet-var-for-node template val)
          var
          (symbol var-string)]
      `((runtime/referredby ~var-match ~var)))))



        
;todo: een refersto voor types en type declarations (die bindt aan ast nodes)
;todo: een var analoog aan type (die bindt aan de ivariablebinding) 


;todo: could also provide a binary one that binds ?itype

(defn
  generic-constrain-type
  ([val stringorvar itypeconstrainerf]
    (generic-constrain-type val
                            stringorvar
                            (fn [var-match var-type] 
                              `((runtime/type ~var-match ~var-type)))
                            itypeconstrainerf))
  ([val stringorvar itypegrounderf itypeconstrainerf]
    (fn [template]
      (let [var-match
            (snippet/snippet-var-for-node template val)
            var-type
            (util/gen-lvar "itype")
            resolvedstringorvar
            (if 
              (represents-variable? stringorvar)
              (symbol stringorvar)
              stringorvar)]
        `((cl/fresh [~var-type]
                    ~@(itypegrounderf var-match var-type)
                    ~@(itypeconstrainerf var-type resolvedstringorvar)))))))



(defn
  constrain-type
  "Requires candidate matches to resolve to the binding for the given variable (an IType)."
  [val uservarstring]
  (generic-constrain-type  val
                           uservarstring
                           (fn [var-type uservar]
                             `((cl/== ~var-type ~uservar)))))

(defn
  constrain-type|qname
  "Requires candidate matches to resolve to an IType with the given qualifiedname."
  [val stringorvar]
  (generic-constrain-type
    val
    stringorvar
    (fn [var-type resolvedstringorvar]
      `((structure/type-name|qualified|string ~var-type ~resolvedstringorvar)))))
  

(defn
  constrain-type|sname
  "Requires candidate matches to resolve to an IType with the given simple name."
  [val stringorvar]
  (generic-constrain-type
    val
    stringorvar
    (fn [var-type resolvedstringorvar]
      `((structure/type-name|simple|string ~var-type ~resolvedstringorvar)))))


(defn
  generic-constrain-subtype+
  [val uservarorstring itypeconstrainf]
  (generic-constrain-type
    val
    uservarorstring
    (fn [var-match var-type] 
      (let [var-match-type
            (util/gen-lvar "matchtype")]
      `((cl/fresh [~var-match-type]
         (runtime/type ~var-match ~var-match-type)
         (structure/type-type|super+ ~var-match-type ~var-type))
         )))
    itypeconstrainf))

(defn
  generic-constrain-subtype*
  [val uservarorstring itypeconstrainf]
  (generic-constrain-type
    val
    uservarorstring
    (fn [var-match var-type] 
      (let [var-match-type
            (util/gen-lvar "matchtype")]
      `((cl/fresh [~var-match-type]
         (runtime/type ~var-match ~var-match-type)
         (cl/conde [(cl/== ~var-match-type ~var-type)]
                   [(structure/type-type|super+ ~var-match-type ~var-type)])
         ))))
    itypeconstrainf))


;todo: add non-transitive variant of subtype+, for an immediate subtype

(defn
  constrain-subtype+
  "Requires candidate matches to resolve to a subtype of the binding for the given variable (an IType)."
  [val uservarorstring]
  (generic-constrain-subtype+
    val
    uservarorstring
    (fn [var-type resolveduservarorstring]
      `((cl/== ~var-type ~resolveduservarorstring)))))

(defn
  constrain-subtype*
  "Requires candidate matches to resolve to a subtype or the type of the binding for the given variable (an IType)."
  [val uservarorstring]
  (generic-constrain-subtype*
    val
    uservarorstring
    (fn [var-type resolveduservarorstring]
      `((cl/== ~var-type ~resolveduservarorstring)))))


(defn
  constrain-subtype+|qname
  "Requires candidate matches to resolve to a subtype of the type with the given name."
  [val uservarorstring]
  (generic-constrain-subtype+
    val
    uservarorstring
    (fn [var-type resolvedstringorvar]
      `((structure/type-name|qualified|string ~var-type ~resolvedstringorvar)))))

(defn
  constrain-subtype*|qname
  "Requires candidate matches to resolve to the type or a subtype of the type with the given name."
  [val uservarorstring]
  (generic-constrain-subtype*
    val
    uservarorstring
    (fn [var-type resolvedstringorvar]
      `((structure/type-name|qualified|string ~var-type ~resolvedstringorvar)))))


(defn
  constrain-subtype+|sname
  "Requires candidate matches to resolve to a subtype of the type with the given name."
  [val uservarorstring]
  (generic-constrain-subtype+
    val
    uservarorstring
    (fn [var-type resolvedstringorvar]
      `((structure/type-name|simple|string ~var-type ~resolvedstringorvar)))))

(defn
  constrain-subtype*|sname
  "Requires candidate matches to resolve to the type or a subtype of the type with the given name."
  [val uservarorstring]
  (generic-constrain-subtype*
    val
    uservarorstring
    (fn [var-type resolvedstringorvar]
      `((structure/type-name|simple|string ~var-type ~resolvedstringorvar)))))



;(defn
;  constrain-subtype*
;  "Requires candidate matches to resolve to the binding for the given variable (an IType) or a subtype thereof."
;  [val var]
;  (generic-constrain-type
 ;   val
 ;;   var
 ;   (fn [var-match var-type] 
 ;     (let [var-match-type
 ;           (util/gen-lvar 'matchtype)]
 ;     `((cl/fresh [~var-match-type]
  ;       (runtime/type ~var-match ~var-match-type)
  ;;       (structure/type-type|super+ ~var-match-type ~var-type))
  ;       )))
  ;  (fn [var-type resolvedstringorvar]
   ;   `((cl/== ~var-type ~resolvedstringorvar)))))


(defn
  constrain-invokes
  [val var-string]
  (fn [template]
    (let [var-match
          (snippet/snippet-var-for-node template val)
          var
          (symbol var-string)]
      `((runtime/invokes ~var-match ~var)))))

(defn
  constrain-constructs
  [val uservar]
  (fn [template]
    (let [var-match
          (snippet/snippet-var-for-node template val)
          var
          (symbol uservar)]
      `((runtime/constructs ~var-match ~var)))))

(defn
  constrain-constructedby
  [val uservar]
  (fn [template]
    (let [var-match
          (snippet/snippet-var-for-node template val)
          var
          (symbol uservar)]
      `((runtime/constructedby ~var-match ~var)))))

(defn
  constrain-invokes|sname
  [val var-string]
  (fn [template]
    (let [var-match
          (snippet/snippet-var-for-node template val)
          var
          (symbol var-string)
          var-name
          (util/gen-lvar "name")
          ]
      `(cl/fresh [~var-name]
                 (~has :name ~var-match ~var-name)  
                 (ast/name|simple-string ~var-name ~var-string)
                 ))))

(defn
  constrain-invokedby
  [val var-string]
  (fn [template]
    (let [var-match
          (snippet/snippet-var-for-node template val)
          var
          (symbol var-string)]
      `((runtime/invokedby ~var-match ~var)))))

(defn
  constrain-overrides
  [val var-string]
  (fn [template]
    (let [var-match (snippet/snippet-var-for-node template val)
          var (symbol var-string)]
      `((runtime/overrides ~var-match ~var)))))




;; Functions related to nodes that have been replaced by logic variable
;; --------------------------------------------------------------------

(declare directive-replacedbyvariable)

(defn 
  snippet-replacement-var-for-node 
  "For the given AST node of the given snippet, returns the name of the user logic
   variable that will be bound to a matching AST node from the Java project."
  [snippet node]
  (let [bds
        (snippet/snippet-bounddirectives-for-node snippet node)]
    (if-let [replaced-bd 
             (directives/bounddirective-for-directive 
               bds
               directive-replacedbyvariable)]
      (symbol (directives/directiveoperandbinding-value (nth (directives/bounddirective-operandbindings replaced-bd) 1))))))

(defn 
  snippet-equals-var-for-node 
  "If this node has an @equals directive, return its operand value"
  [snippet node]
  (let [bds (snippet/snippet-bounddirectives-for-node snippet node)]
    (if-let [replaced-bd (directives/bounddirective-for-directive bds directive-equals)]
      (symbol (directives/directiveoperandbinding-value (nth (directives/bounddirective-operandbindings replaced-bd) 1))))))

(declare directive-replacedbyexp)

(defn 
  snippet-replacement-exp-for-node 
  [snippet node]
  (let [bds
        (snippet/snippet-bounddirectives-for-node snippet node)]
    (if-let [replaced-bd 
             (directives/bounddirective-for-directive 
               bds
               directive-replacedbyexp)]
      (directives/directiveoperandbinding-value (nth (directives/bounddirective-operandbindings replaced-bd) 1)))))

(defn 
  snippet-node-replaced-by-var?
  [snippet node]
  (boolean (snippet-replacement-var-for-node snippet node))) 

(defn 
  snippet-node-replaced-by-exp?
  [snippet node]
  (boolean (snippet-replacement-exp-for-node snippet node))) 

(defn
  snippet-replacement-vars
  [snippet] 
  (distinct
    (remove nil? 
            (map
              (fn [node] 
                (snippet-replacement-var-for-node snippet node))
              (snippet/snippet-nodes snippet)))))


(defn
  snippet-replacement-node2var
  [snippet]
  (reduce
    (fn [sofar node]
      (if-let [var (snippet-replacement-var-for-node snippet node)]
        (assoc sofar node var)
        sofar))
    {}
    (snippet/snippet-nodes snippet)))

(defn
  snippet-replacement-node2exp
  [snippet]
  (reduce
    (fn [sofar node]
      (if-let [var (snippet-replacement-exp-for-node snippet node)]
        (assoc sofar node var)
        sofar))
    {}
    (snippet/snippet-nodes snippet)))
  
  
  
(defn
  snippet-vars-among-directivebindings-for-node
  "Returns all variables that feature as the binding for a directive operand of the node.
   Includes replacement vars."
  [snippet node]
  (let [bds (snippet/snippet-bounddirectives-for-node snippet node)]
    (mapcat
      (fn [bounddirective]
        (filter represents-variable?
                (map directives/directiveoperandbinding-value 
                     (directives/bounddirective-operandbindings bounddirective))))
      bds)))

(defn
  snippet-vars-among-directivebindings
  "Returns all variables that feature as the binding for a directive operand of the snippet.
   Includes replacement vars."
  [snippet]
  (mapcat
    (fn [node]
      (snippet-vars-among-directivebindings-for-node snippet node))
    (snippet/snippet-nodes snippet)))



(defn
  snippetgroup-vars-among-directivebindings
  "Returns all variables that feature as the binding for a directive operand among the snippets in the snippet group.
   Includes replacement vars."
  [snippetgroup]
  (mapcat snippet-vars-among-directivebindings (snippetgroup/snippetgroup-snippetlist snippetgroup)))




(defn
  constrain-replacedbyvariable
  "Constraining directive for nodes that have been replaced by a variable."
  [snippet-ast replacement-var-string]
  (fn [snippet]
    (let [var-match (snippet/snippet-var-for-node snippet snippet-ast)
          replacement-var (symbol replacement-var-string)]
      `((cl/== ~replacement-var ~var-match)))))


;only used during template instantiation, hence no constraining at all
(defn
  constrain-replacedbyexp
  "Constraining directive for nodes that have been replaced by an s-expression."
  [snippet-ast replacement-string]
  (fn [snippet]
    ;(let [var-match (snippet/snippet-var-for-node snippet snippet-ast)
    ;      replacement-exp (read-string replacement-string)
    ;      var-replacementvalue (util/gen-lvar 'replacement)]
    ;   `((cl/fresh [~var-replacementvalue]
     ;              ;to project vars in replacementexp
     ;             (el/equals ~var-replacementvalue ~replacement-exp))))))
     `())) 


(defn
  constrain-replacedbywildcard
  [snippet-val]
  (fn [snippet]
    `()))

(defn
  constrain-replacedbywildcard-checked
  [snippet-val]
  (fn [snippet]
    (let [var-match (snippet/snippet-var-for-node snippet snippet-val)]
      (cond 
        (astnode/ast? snippet-val)
        (let [ast-keyw (astnode/ekeko-keyword-for-class-of snippet-val)]
          `((~generic-nongrounding-ast ~ast-keyw ~var-match)))
        
        (astnode/lstvalue? snippet-val)
        `((~nongrounding-value|list ~var-match))
        
        (astnode/primitivevalue? snippet-val)
        (ast/value|primitive ~var-match)
        
        (astnode/nilvalue? snippet-val)
        `((~value|null ~var-match))))))

;constraining/grounding will be done by parent regexp in which snippet-val resides
(defn
  constrain-multiplicity|regexp
  [snippet-val multiplicity]
  (fn [snippet]
    `()))


(defn
  constrain-equals
  "Constraining directive that will unify the node's match with the given variable."
  [snippet-ast var-string]
  (fn [snippet]
    (let [var-match (snippet/snippet-var-for-node snippet snippet-ast)
          var (symbol var-string)]
      `((cl/== ~var ~var-match)))))

(defn
  constrain-equivalent
  "Constraining directive that tests whether the subject and meta-variable have the same .toString value."
  [snippet-ast var-string]
  (fn [snippet]
    (let [var-match (snippet/snippet-var-for-node snippet snippet-ast)
          var (symbol var-string)]
      `((nongrounding-equivalent ~var-match ~var)
         ))))

(defn
  constrain-identity
  "Does nothing."
  [snippet-ast]
  (fn [snippet] `()))

(declare directive-replacedbywildcard)
(declare directive-replacedbywildcard-checked)

(defn 
  snippet-node-replaced-by-wilcard?
  [snippet node]
  (let [bds
        (snippet/snippet-bounddirectives-for-node snippet node)]
    (boolean (directives/bounddirective-for-directive bds directive-replacedbywildcard))))

(defn snippet-node-replaced?
  [snippet node]
  (or
    (snippet-node-replaced-by-var? snippet node)
    (snippet-node-replaced-by-wilcard? snippet node)
    (snippet-node-replaced-by-exp? snippet node)))

(defn reachable-nodes
  "Retrieve all children of a given node (taking into account that the children of wildcard/metavariable nodes are hidden)"
  [snippet node]
  (let [walk
        (fn walk [val]
          (cond 
            (or (astnode/ast? val) (astnode/lstvalue? val))
            (if (snippet-node-replaced? snippet val)
              [val]
              (cons val (mapcat walk (snippet/snippet-node-children|conceptually-refs snippet val))))
            
            (or (astnode/primitivevalue? val) (astnode/nilvalue? val))
            [val]
            
            :default
            (throw (Exception. (str "Don't know how to walk this value:" val)))))]
    (walk node)))


(defn reachable-nodes-of-type
  "Among all reachable nodes, find those of a particular type"
  [snippet node types]
  (filter 
    (fn [node] (if (some #{(astnode/ekeko-keyword-for-class-of node)} types)
                 node))
    (reachable-nodes snippet node)))

(defn node-protected?
  "Is this node protected from being removed/abstracted away? This is the case if one of its children has a @protected directive."
  [snippet val]
  (let [protected-bd?
        (fn [val]
          (some
            (fn [bd] (= (directives/directive-name (directives/bounddirective-directive bd)) "protect"))
            (snippet/snippet-bounddirectives-for-node snippet val)))
        
        walk
        (fn walk [val]
          (cond 
            (or (astnode/ast? val) (astnode/lstvalue? val))
            (if (protected-bd? val)
              true
              (some 
                (fn [child] (walk child))
                (snippet/snippet-node-children|conceptually-refs snippet val)))
            (or (astnode/primitivevalue? val) (astnode/nilvalue? val))
            (protected-bd? val)
            
            :default
            false))]
    (walk val)))

(defn
  to-literal-string
  [value]
  (let [qstr (.toString value)]
    `~qstr))

(defn 
  ast-primitive-as-expression
  "Returns the string representation of a primitive-valued JDT node (e.g., instances of Modifier.ModifierKeyword)."
  [primitive]
  ;could dispatch on this as well
  (cond (or (true? primitive) (false? primitive))
        primitive
        (number? primitive)
        primitive
        (instance? org.eclipse.jdt.core.dom.Modifier$ModifierKeyword primitive)
        `(~to-modifier-keyword ~(to-literal-string primitive))
        (instance? org.eclipse.jdt.core.dom.PrimitiveType$Code primitive)
        `(~to-primitive-type-code ~(to-literal-string primitive))
        (instance? org.eclipse.jdt.core.dom.Assignment$Operator primitive)
        `(~to-assignment-operator ~(to-literal-string primitive))
        (instance? org.eclipse.jdt.core.dom.InfixExpression$Operator primitive)
        `(~to-infix-expression-operator ~(to-literal-string primitive))
        (instance? org.eclipse.jdt.core.dom.PrefixExpression$Operator primitive)
        `(~to-prefix-expression-operator ~(to-literal-string primitive))
        (instance? org.eclipse.jdt.core.dom.PostfixExpression$Operator primitive)
        `(~to-postfix-expression-operator ~(to-literal-string primitive))
        (nil? primitive) 
        (throw (Exception. (str "Encountered a null-valued property value that should have been wrapped by Ekeko.")))
        :else (to-literal-string primitive)))


;;Registering directives
;;----------------------

(def
  directive-exact
  (directives/make-directive
    "match"
    []
    constrain-exact 
    "Type and properties match exactly."))

(def 
  directive-child
  (directives/make-directive
    "child"
    []
    ground-relativetoparent
    "Match is the corresponding child for the parent match."))



;(def 
;  directive-offspring
;  (directives/make-directive
;    "anydepth"
;    []
;    ground-relativetoparent+ ;arity 0
;    "Finds match candidates among the offspring of the match for the parent."))


(def
  directive-child+
  (directives/make-directive
    "child+" ; or "nested" because child is not shown either
    []
    ground-relativetoparent+
    "Match is nested within the corresponding child for the parent match."))

(def
  directive-child*
  (directives/make-directive
    "child*" ; or "nested*" because child is not shown either
    []
    ground-relativetoparent*
    "Match is the corresponding child for the parent match, or nested therein."))

(def 
  directive-size|atleast
  (directives/make-directive
    "orlarger"
    []
    constrain-size|atleast 
    "Requires candidate matches to have at least as many elements as the corresponding list in the template."))

(def 
  directive-emptybody
  (directives/make-directive
    "empty-body"
    []
    constrain-emptybody 
    "Requires candidate matches to be an empty block (or nil)."))

(def 
  directive-orblock
  (directives/make-directive
    "or-block"
    []
    ground-orblock 
    "Matches with a statement itself, or the statement wrapped in a block."))

(def 
  directive-replacedbyvariable
  (directives/make-directive
    "replaced-by-variable"
    [(directives/make-directiveoperand "Meta-variable")]
    constrain-replacedbyvariable
    "Node and children have been replaced by a variable."
    ))


;only used during template instantiation
(def 
  directive-replacedbyexp
  (directives/make-directive
    "replaced-by-exp"
    [(directives/make-directiveoperand "Expression")]
    constrain-replacedbyexp
    "During template instantiation, value will be replaced by the result of an expression."
    ))

(def 
  directive-equals
  (directives/make-directive
    "equals"
    [(directives/make-directiveoperand "Meta-variable")]
    constrain-equals
    "Match unifies with variable."
    ))

(def 
  directive-if
  (directives/make-directive
    "if"
    [(directives/make-directiveoperand "Clojure expression")]
    constrain-identity
    "Match should satisfy the given Clojure boolean expression."
    ))

(def 
  directive-equivalent
  (directives/make-directive
    "equivalent"
    [(directives/make-directiveoperand "Meta-variable")]
    constrain-equivalent
    "Match should be equivalent to the variable's value."
    ))

(def 
  directive-protect
  (directives/make-directive
    "protect"
    []
    constrain-identity
    "Identity directive (does nothing)."
    ))

(def 
  directive-member
  (directives/make-directive
    "anyindex"
    []
    ground-element
    "Match is a member of the match for the parent list."))

(def 
  directive-replacedbywildcard
  (directives/make-directive
    "replaced-by-wildcard"
    []
    constrain-replacedbywildcard
    "Match is unconstrained."))

(def 
  directive-replacedbywildcard-checked
  (directives/make-directive
    "!"
    []
    constrain-replacedbywildcard-checked
    "Match is unconstrained, except the AST node type."))

(def 
  directive-invokes
  (directives/make-directive
    "invokes"
    [(directives/make-directiveoperand "Meta-variable")]
    constrain-invokes
    "Match invokes a method, which is the binding for the meta-variable."))

(def 
  directive-constructs
  (directives/make-directive
    "constructs"
    [(directives/make-directiveoperand "Meta-variable")]
    constrain-constructs
    "Match invokes a constructor, which is the binding for the meta-variable."))

(def
  directive-invokes|sname
  (directives/make-directive
    "invokes|sname"
    [(directives/make-directiveoperand "Simple name")]
    constrain-invokes|sname
    "Match invokes a method, with the given string as simple name."))

(def 
  directive-invokedby
  (directives/make-directive
    "invoked-by"
    [(directives/make-directiveoperand "Meta-variable")]
    constrain-invokedby
    "Match is invoked by an invocation expression, bound to the meta-variable."))

(def 
  directive-constructedby
  (directives/make-directive
    "constructed-by"
    [(directives/make-directiveoperand "Meta-variable")]
    constrain-constructedby
    "Match is a constructor invoked by an instance creation expression or super constructor invocation bound to the meta-variable."))

(def 
  directive-overrides
  (directives/make-directive
    "overrides"
    [(directives/make-directiveoperand "Meta-variable")]
    constrain-overrides
    "Match overrides a method, which is the binding for the meta-variable."))

(def 
  directive-refersto
  (directives/make-directive
    "refers-to"
    [(directives/make-directiveoperand "Meta-variable")]
    constrain-refersto
    "Match refers to a variable, which is the binding for the meta-variable."))

(def 
  directive-referredby
  (directives/make-directive
    "referred-by"
    [(directives/make-directiveoperand "Meta-variable")]
    constrain-referredby
    "Match declares a variable, referred to by the meta-variable binding."))

(def 
  directive-type
  (directives/make-directive
    "type"
    [(directives/make-directiveoperand "Meta-variable")]
    constrain-type
    "Match resolves to a type, which is the binding for the meta-varibale."))

(def 
  directive-type|qname
  (directives/make-directive
    "type|qname"
    [(directives/make-directiveoperand "Qualified name")]
    constrain-type|qname
    "Match resolves to a type with the given string as qualified name."))

(def 
  directive-type|sname
  (directives/make-directive
    "type|sname"
    [(directives/make-directiveoperand "Simple name")]
    constrain-type|sname
    "Match resolves to a type with the given string as simple name."))

(def 
  directive-subtype+
  (directives/make-directive
    "subtype+"
    [(directives/make-directiveoperand "Meta-variable")]
    constrain-subtype+
    "Match resolves to a transitive subtype of the binding for the meta-variable."))

(def 
  directive-subtype+|qname
  (directives/make-directive
    "subtype+|qname"
    [(directives/make-directiveoperand "Qualified name")]
    constrain-subtype+|qname
    "Match resolves to a transitive subtype with the given string as qualified name."))

(def 
  directive-subtype+|sname
  (directives/make-directive
    "subtype+|sname"
    [(directives/make-directiveoperand "Simple name")]
    constrain-subtype+|sname
    "Match resolves to a transitive subtype with the given string as simple name."))


(def 
  directive-subtype*
  (directives/make-directive
    "subtype*"
    [(directives/make-directiveoperand "Meta-variable")]
    constrain-subtype*
    "Match resolves to the type or a transitive subtype of the binding for the meta-variable."))

(def 
  directive-subtype*|qname
  (directives/make-directive
    "subtype*|qname"
    [(directives/make-directiveoperand "Qualified name")]
    constrain-subtype*|qname
    "Match resolves to the type or a transitive subtype with the given string as qualified name."))

(def 
  directive-subtype*|sname
  (directives/make-directive
    "subtype*|sname"
    [(directives/make-directiveoperand "Simple name")]
    constrain-subtype*|sname
    "Match resolves to the type or a transitive subtype with the given string as simple name."))

(def 
  directive-consider-as-set|lst
  (directives/make-directive
    "match|set"
    []
    constrain-lst|set
    "Use set matching for list elements."))

(def
  directive-include-inherited
  (directives/make-directive
    "withinherited"
    []
    constrain-inherited
    "Include inherited members."))

(def 
  directive-consider-as-regexp|lst
  (directives/make-directive
    "match|regexp"
    []
    constrain-lst|regexp
    "Use regexp matching for list elements."))

(def 
  directive-consider-as-regexp|cfglst
  (directives/make-directive
    "match|regexp-cfg"
    []
    constrain-lst|cfgregexp
    "Use regexp matching over control flow graph for list matching."))

(def
  directive-multiplicity
  (directives/make-directive
    "multiplicity"
    []
    constrain-multiplicity|regexp
    "Determines multiplicity of matches."))

(def
  directive-orimplicit
  (directives/make-directive
    "orimplicit"
    []
    constrain-orimplicit
    "Invocations with implicit this-receiver match as well."))

(def
  directive-notnil
  (directives/make-directive
    "notnil"
    []
    constrain-notnil
    "Nodes with a nil value will not match."))

(def
  directive-orsimple
  (directives/make-directive
    "orsimple"
    []
    constrain-orsimple
    "Simple types resolving to name of qualified type in template will match as well."))

(def
  directive-ignore
  (directives/make-directive
    "ignore"
    []
    constrain-identity
    "During matching, this node is substituted by its child."))

(def
  directives-replacedby
  [directive-replacedbyvariable
   directive-replacedbywildcard
   directive-replacedbywildcard-checked
   directive-replacedbyexp])


(def 
  directives-match
  [directive-exact 
   directive-orimplicit
   directive-orsimple
   directive-emptybody])

(def
  directives-constraining|mutuallyexclusive
  (concat
    directives-replacedby
    directives-match))
 

(def 
  directives-constraining|optional
  [;can be added to the above
   directive-size|atleast
   directive-equals
   directive-equivalent
   directive-notnil
   directive-consider-as-regexp|lst
   directive-consider-as-regexp|cfglst
   directive-consider-as-set|lst
   directive-include-inherited
   directive-multiplicity
   directive-invokes
   directive-invokedby
   directive-constructs
   directive-constructedby
   directive-overrides
   directive-refersto
   directive-referredby
   directive-type
   directive-type|qname
   directive-type|sname
   directive-subtype+
   directive-subtype+|qname
   directive-subtype+|sname
   directive-subtype*
   directive-subtype*|qname
   directive-subtype*|sname
   directive-protect
   directive-if])


(def 
  directives-constraining
  (concat directives-constraining|mutuallyexclusive directives-constraining|optional))


(def
  directives-grounding
  [directive-child
   directive-child+
   directive-child*
   directive-member
   directive-orblock
   directive-ignore
   ])

(def
  directives-list-nongrounding
  [directive-consider-as-set|lst
   directive-consider-as-regexp|lst
   directive-consider-as-regexp|cfglst])

(defn 
  registered-constraining-directives
  "Returns collection of registered constraining directives."
  []
  directives-constraining)

(defn 
  registered-grounding-directives
  "Returns collection of registered grounding directives."
  []
  directives-grounding)

(defn 
  registered-nongrounding-list-directives
  "Returns collection of directives on list nodes, which remove grounding directives from its elements."
  []
  directives-list-nongrounding)

(defn
  registered-grounding-directive?
  "Succeeds for registered directives that are grounding."
  [directive]
  (some #{directive} directives-grounding))

(defn
  registered-constraining-directive?
  "Succeeds for registered directives that are constraining."
  [directive]
  (some #{directive} directives-constraining))

(defn
  registered-replacedby-directive?
  "Succeeds for registered replacedby directives."
  [directive]
  (some #{directive} directives-replacedby))

(defn 
  registered-directives
  []
  (concat (registered-grounding-directives) 
          (registered-constraining-directives)))


(defn
  registered-directive-for-name
  [name]
  (some
    (fn [directive]
      (when (= name (directives/directive-name directive))
        directive))
    (registered-directives)))



;; Auxiliary functions related to regular expression matching
;; ---------------------------------------------------------------



(defn
  snippet-list-regexp?
  "Returns true for lists that have list or cfg regexp matching enabled."
  [snippet value]
  (let [bds (snippet/snippet-bounddirectives-for-node snippet value)]
    (or (directives/bounddirective-for-directive 
          bds
          directive-consider-as-regexp|lst)
        (directives/bounddirective-for-directive 
          bds
          directive-consider-as-regexp|cfglst))))


(defn
  snippet-list-setmatch?
  "Returns true for lists that have set matching enabled."
  [snippet value]
  (let [bds (snippet/snippet-bounddirectives-for-node snippet value)]
    (directives/bounddirective-for-directive 
      bds
      directive-consider-as-set|lst)))
    
(defn
  snippet-child*+?
  "Returns true for nodes with a child* directive."
  [snippet value]
  (let [bds (snippet/snippet-bounddirectives-for-node snippet value)]
    (or
      (directives/bounddirective-for-directive bds directive-child*)
      (directives/bounddirective-for-directive bds directive-child+))))

; These three functions consider the @ignore directive .. TODO there should be a cleaner way to implement this..
(defn
  snippet-parent-match|set?
  "Returns true if this node's parent has a match|set directive."
  [snippet value]
  (let [list-owner (snippet-list-containing snippet value)
        bds (snippet/snippet-bounddirectives-for-node snippet list-owner)]
    (directives/bounddirective-for-directive bds directive-consider-as-set|lst)))
(defn valuelistmember?
  "Returns true if this node is a list element"
  [snippet value]
  (let [val (if (snippet/has-ignore-parent snippet value)
              (snippet/snippet-node-parent|conceptually snippet value)
              value)]
    (astnode/valuelistmember? val)))
(defn snippet-list-containing
  [snippet value]
  (let [val (if (snippet/has-ignore-parent snippet value)
              (snippet/snippet-node-parent|conceptually snippet value)
              value)]
    (snippet/snippet-list-containing snippet val)))

(defn
  snippet-value-element-of-list-satisfying 
  [snippet value satisfyingf]
  (and (snippet/snippet-value-node? snippet value)
       (if-let [owninglst (snippet-list-containing snippet value)]
         (when 
           (satisfyingf snippet owninglst)
           owninglst))))

(defn
  snippet-value-regexp-element
  [snippet value]
  (snippet-value-element-of-list-satisfying snippet value snippet-list-regexp?))



(defn
  snippet-value-regexp-offspring-of-list-satisfying
  [snippet value satisfyingf]
  (loop [val value]
    (if
      (= val (snippet/snippet-root snippet))
      nil
      (if-let
        [lst (snippet-value-element-of-list-satisfying snippet val satisfyingf)]
        lst
        (if-let [owner (snippet/snippet-node-owner snippet val)]
          (recur owner))))))



(defn
  snippet-value-regexp-offspring
  [snippet value]
  (snippet-value-regexp-offspring-of-list-satisfying snippet value snippet-list-regexp?))



(defn
  snippet-value-multiplicity
  [snippet value]
  (let [bds (snippet/snippet-bounddirectives-for-node snippet value)]
    (if-let [mbd (directives/bounddirective-for-directive 
                   bds
                   directive-multiplicity)]
      (directives/directiveoperandbinding-value 
        (nth (directives/bounddirective-operandbindings mbd) 1))
      "1")))

(defn
  snippet-value-directive?
  "Returns bounddirective for given value corresponding to given directive, if present."
  [snippet val directive]
  (if-let [bds (snippet/snippet-bounddirectives-for-node snippet val)]
    (directives/bounddirective-for-directive 
      bds
      directive)))



;; Constructing Snippet instances with default matching directives
;; ---------------------------------------------------------------

(def default-directives [directive-exact 
                         directive-child 
                         ;ensures these aren't pretty-printed
                         directive-replacedbyvariable 
                         directive-replacedbywildcard
                         directive-replacedbyexp
                         ])

(defn
  default-directive?
  [directive]
  (some #{directive} default-directives))

(declare snippet-value-regexp-element)

(defn
  default-bounddirectives
  "Returns default matching directives for given snippet and element of the snippet element.
   These are directive-exact and directive-child, or directive-exact when the value resides within a regular expression."
  [snippet value]
  (if
    (snippet-value-regexp-element snippet value)
    (list 
      (directives/bind-directive-with-defaults directive-exact snippet value))
    (list 
      (directives/bind-directive-with-defaults directive-exact snippet value)
      (directives/bind-directive-with-defaults directive-child snippet value))))

(defn
  nondefault-bounddirectives
  [snippet value]
  (remove 
    (fn [bounddirective] 
      (default-directive? (directives/bounddirective-directive bounddirective)))
    (snippet/snippet-bounddirectives-for-node snippet value)))

(defn
  has-nondefault-bounddirectives?
  [snippet value]
  (boolean (not-empty (nondefault-bounddirectives snippet value))))

(defn
  snippet-nondefault-bounddirectives-string-for-node
  [snippet node]
  (let [bounddirectives (nondefault-bounddirectives snippet node)]
    ;todo: incorporate arguments
    (clojure.string/join " " (map directives/bounddirective-string bounddirectives)))) 

(defn
  remove-all-directives
  [template value]
  (snippet/update-bounddirectives template value []))

(defn
  remove-all-directives*
  [template value]
  (let [snippet (atom template)]
    (snippet/walk-snippet-element
      template
      value
      (fn [val]
        (swap! snippet remove-all-directives val)))
    @snippet))

(defn
  remove-all-directives+
  [template value]
  (let [snippet (atom template)]
    (snippet/walk-snippet-element 
      template
      value
      (fn [val]
        (when (not= val value)
          (swap! snippet remove-all-directives val))))
    @snippet))

(defn
  remove-directives 
  [template value directives]
  (let [bounddirectives 
        (snippet/snippet-bounddirectives-for-node template value)
        remainingbounddirectives
        (remove (fn [bounddirective]
                  (some #{(directives/bounddirective-directive bounddirective)}
                        directives))
                bounddirectives)]
    (snippet/update-bounddirectives template value remainingbounddirectives)))

(defn
  remove-directive
  [template value directive]
  (remove-directives template value [directive]))

(defn 
  remove-value-from-snippet
  "Removes a single value from snippet, does not remove subvalues."
  [s value]
  (->
    (update-in s [:var2ast] dissoc (get-in s [:ast2var value])) 
    (update-in [:ast2var] dissoc value)
    (update-in [:ast2bounddirectives] dissoc value)
    (update-in [:ast2meta] dissoc value)
    (update-in [:ast2anchoridentifier] dissoc value)))


(defn-
  jdt-node-anchor
  [n]
  (try 
    (astnode/project-value-identifier n)
    (catch Exception e 
      (do 
        (.printStackTrace e)
        nil))))


(defn 
  add-value-to-snippet
  "Adds a single value to snippet, does not add subvalues."
  [snippet value]
  (let [lvar (util/gen-readable-lvar-for-value value)]
    (->
      snippet
      (assoc-in [:ast2var value] lvar)
      (assoc-in [:ast2bounddirectives value] 
                (default-bounddirectives snippet value))
      (assoc-in [:var2ast lvar] value)
      )))

  
(defn 
  jdt-node-as-snippet
  "Interpretes a copy of the given ASTNode as a snippet with default matching 
   strategies (i.e., grounding=:exact, constaining=:exact)
   for the values of its properties."
  [n]
  (let [copy (org.eclipse.jdt.core.dom.ASTNode/copySubtree (.getAST ^ASTNode n) n)
        snippet (atom (snippet/make-snippet copy))]
    (util/walk-jdt-nodes 
      copy ;copy in snippet 
      n ;original node in project
      (fn [[val projectval]] 
        (let [projectanchor (jdt-node-anchor projectval)]
          (swap! snippet add-value-to-snippet val)
          (swap! snippet assoc-in [:ast2anchoridentifier val] projectanchor))))
    (swap! snippet snippet/update-anchor (jdt-node-anchor n))
    @snippet))

(defn
  snippet-from-string
  [string]
  (let [parsed (parsing/parse-string-ast string)
        normalized (parsing/parse-string-ast (str parsed))]
    (jdt-node-as-snippet normalized)
    ))

(def
  snippet-from-node
  jdt-node-as-snippet)


(def ^:dynamic *partial-matching* true)

(def ^:dynamic node-count (atom 0))
@node-count

(defn 
  snippet-node-conditions|withoutfresh
  "Generates default (regardless of whether conditions have already been generated by a parent node)
   conditions for node itself (not including children of node)."
  ([snippet ast-or-list]
    (snippet-node-conditions|withoutfresh snippet ast-or-list identity))
  ([snippet ast-or-list bounddirectivesfilterf]
    (swap! node-count inc)
    (let [matchvar
          (str (snippet/snippet-var-for-node snippet ast-or-list))
          bounddirectives
          (filter bounddirectivesfilterf (snippet/snippet-bounddirectives-for-node snippet ast-or-list))
         
          bounddirectives-grounding
          (filter (fn [bounddirective]
                    (registered-grounding-directive? (directives/bounddirective-directive bounddirective)))
                  bounddirectives)
          bounddirectives-constraining
          (filter (fn [bounddirective]
                    (registered-constraining-directive? (directives/bounddirective-directive bounddirective)))
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
            bounddirectives-constraining)
          ]
      (concat conditions-grounding
              conditions-constraining 
              (if *partial-matching*
                `((damp.ekeko.snippets.geneticsearch.fitness/add-match ~matchvar))
                '())
              ))))

(defn
  snippet-node-matchvarsforproperties
  [snippet val]
  (map (partial snippet/snippet-var-for-node snippet)
       (snippet/snippet-node-children|conceptually-refs snippet val)))

(def
  snippet-node-matchvars
  snippet-node-matchvarsforproperties)

(defn
  snippet-value-freshvars 
  [snippet value]
  (let [root (snippet/snippet-root snippet)]
    (if 
      (= root value)
      '() ;to be generated by query producer 
      (snippet-node-matchvars snippet value))))   
  
    

(defn
  snippet-node-conditions
  "Returns a list of logic conditions that will retrieve matches for the node and its children."
  [snippet node 
   & {:keys
      [extraconditions bdfilter generatep extraconditionsafter] 
      :or 
      {generatep (fn [val] (not 
                             (or 
                               (snippet-list-setmatch? snippet val)
                               (snippet-value-directive? snippet val directive-orimplicit)
                               (snippet-value-directive? snippet val directive-orsimple)
                               (snippet-list-regexp? snippet val))))
       extraconditions (constantly '())
       extraconditionsafter  (constantly '())
       bdfilter identity}}]
  (letfn [(conditions [val]
            (let [bds (snippet/snippet-bounddirectives-for-node snippet val)]
              (if 
                ;assumption: no need to generate fresh vars for something that won't generate conditions,
                ;_and_ neither for its children
                (seq bds) 
                (freshornot  (snippet-value-freshvars snippet val) 
                             (concat 
                               (extraconditions val)
                               (snippet-node-conditions|withoutfresh snippet val bdfilter) 
                               (if 
                                 (generatep val)
                                 ; Generating conditions for child nodes..
                                 ; Performance optimization! Depending on the node type, we can reorder its children
                                 ; such that the children with cheap constraints are handled first, 
                                 ; which reduces the search space before we need to handle more expensive children.
                                 ; For example, in a TypeDeclaration, it's better to handle its name before its members.
                                 (let [children (snippet/snippet-node-children|conceptually-refs snippet val)
                                       
                                       remove-children
                                       (fn [lst property-names]
                                         (remove (fn [child]
                                                   (some 
                                                     (fn [property-name] (= property-name (.getId (astnode/owner-property child))))
                                                     property-names))
                                                 lst))
                                       
                                       move-child-to-back
                                       (fn [lst property-name]
                                         (concat
                                           (remove (fn [x] (= property-name (.getId (astnode/owner-property x)))) lst)
                                           (filter (fn [x] (= property-name (.getId (astnode/owner-property x)))) lst)))
                                       
                                       reordered-children
                                       (cond (astnode/typedeclaration? val)
                                             (-> children
                                               (move-child-to-back "bodyDeclarations")
                                               (remove-children ["javadoc"]))
                                             
                                             (astnode/methoddeclaration? val)
                                             (-> children
                                               (move-child-to-back "body")
                                               ; (remove-children ["extraDimensions2" "javadoc"]) ; Hm maybe not.. it messes up partial matching since now nodes are missing..
                                               )
                                             :else
                                             children)
                                       ]
                                   (mapcat conditions reordered-children))
                                 '())
                               (extraconditionsafter val)
                               ))
                '())))]
            (conditions node)))



(defn 
  snippet-node-conditions+
  "Generates default conditions for offspring of node.
   Assumes fresh variables already exist for immediate children."
  [snippet node]
  (mapcat
    (partial snippet-node-conditions snippet)
    (snippet/snippet-node-children|conceptually-refs snippet node)))
    
    
   


  (defn
    register-callbacks
    []
    (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPET_FROM_STRING) snippet-from-string)
    (set! (damp.ekeko.snippets.data.TemplateGroup/FN_SNIPPET_FROM_NODE) snippet-from-node)
  
    (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_NONDEFAULT_BOUNDDIRECTIVES) nondefault-bounddirectives)
    (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_HAS_NONDEFAULT_BOUNDDIRECTIVES) has-nondefault-bounddirectives?)
  
    (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_BOUNDDIRECTIVES_STRING) snippet-nondefault-bounddirectives-string-for-node)
  
    (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_USERVAR_FOR_NODE) snippet-replacement-var-for-node)
  
    (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_EXP_FOR_NODE) snippet-replacement-exp-for-node)
  
  
    (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_ELEMENT_REPLACEDBY_WILDCARD) snippet-node-replaced-by-wilcard?)
  
  
    (set! (damp.ekeko.snippets.gui.DirectiveSelectionDialog/FN_REGISTERED_DIRECTIVES) registered-directives)
  
  
    )

  (register-callbacks)

  
