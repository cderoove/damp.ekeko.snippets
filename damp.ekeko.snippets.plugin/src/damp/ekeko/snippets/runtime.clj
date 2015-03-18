(ns 
  ^{:doc "Runtime predicates for snippet-driven querying."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets.runtime
  (:refer-clojure :exclude [type])
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko [logic :as el]]
            [damp.ekeko.jdt
             [ast :as ast]
             [aststructure :as aststructure]
             [structure :as structure]
             [astbindings :as astbindings]
             [rewrites :as rewrites]
             ]
            [damp.ekeko.snippets 
             [snippetgroup :as snippetgroup]
             [snippet :as snippet]])
  (:import 
    [org.eclipse.jdt.core.dom PrimitiveType Modifier$ModifierKeyword Assignment$Operator
     InfixExpression$Operator PrefixExpression$Operator PostfixExpression$Operator
     SimpleName VariableDeclarationFragment Type MethodDeclaration]
    [damp.ekeko.snippets.gui TemplateCodeGenerator]
    [damp.ekeko.snippets.data TemplateGroup]))



;referring has to be bound, referred to not necessarily
(defn
  refersto
  [?referring ?referred]
  (cl/all
    (el/v+ ?referring)
    (cl/conde
      [(aststructure/ast|fieldaccess-ast|referred ?referring ?referred)]
      [(aststructure/ast|localvariable-ast|referred ?referring ?referred)]
      )))


;going to be slow
(defn
  referredby
  [?referred ?referring]
  (cl/all
    (el/v+ ?referred)
    (cl/conde 
      [(aststructure/ast|fieldaccess-ast|referred ?referring ?referred)]
      [(aststructure/ast|localvariable-ast|referred ?referring ?referred)]
      )))



;ast has to be bound
(defn
  type
  [?ast ?itype]
  (cl/all
    (el/v+ ?ast)
    (cl/conde 
      [(aststructure/ast|type-type ?ast ?itype)]
      [(aststructure/ast|annotation-type ?ast ?itype)]
      [(aststructure/ast|expression-type ?ast ?itype)]
      ;perhaps not the declarations?
      ;[(aststructure/typedeclaration-type ?ast ?itype)]
      )))


(defn
  invokes
  [?ast ?methoddeclarationorname]
  (cl/all
    (el/v+ ?ast)
    (cl/conda [(ast/ast :SimpleName ?ast)
               (cl/fresh [?parentinvocation]
                         (ast/ast-parent ?ast ?parentinvocation)
                         (invokes ?parentinvocation ?methoddeclarationorname))] 
              [(cl/conde [(ast/ast :MethodInvocation ?ast)]
                         [(ast/ast :SuperMethodInvocation ?ast)])
               (cl/fresh [?methoddeclaration]
                         (aststructure/methodinvocation-methoddeclaration ?ast ?methoddeclaration)
                         (cl/conde
                           [(cl/== ?methoddeclaration ?methoddeclarationorname)]
                           [(ast/has :name ?methoddeclaration ?methoddeclarationorname)]))])))

(defn
  invokedby
  [?ast ?invocationorname]
  (cl/all
    (el/v+ ?ast)	
    (cl/conda [(ast/ast :SimpleName ?ast)
               (cl/fresh [?parentdeclaration]
                         (ast/ast-parent ?ast ?parentdeclaration)
                         (invokedby ?parentdeclaration ?invocationorname))]
              [(ast/ast :MethodDeclaration ?ast)
               (cl/fresh [?invocation]
                         (aststructure/methodinvocation-methoddeclaration ?invocation ?ast)
                         (cl/conde
                           [(cl/== ?invocation ?invocationorname)]
                           [(ast/has :name ?invocation ?invocationorname)]))])))


(defn
  constructs
  [?ast ?constructordeclarationorname]
  (cl/all
    (el/v+ ?ast)
    (cl/fresh [?constructordeclaration]
              (aststructure/constructorinvocation-constructordeclaration ?ast ?constructordeclaration)
              (cl/conde
                [(cl/== ?constructordeclaration ?constructordeclarationorname)]
                [(ast/has :name ?constructordeclaration ?constructordeclarationorname)]))))

(defn
  constructedby
  [?ast ?invocation]
  (cl/all
    (el/v+ ?ast)
    (cl/fresh [?constructordeclaration]
              (cl/conda [(ast/ast :SimpleName ?ast)
                         (cl/fresh [?parentdeclaration]
                                   (ast/ast-parent ?ast ?parentdeclaration)
                                   (constructedby ?parentdeclaration ?invocation))]
                        [(ast/ast :MethodDeclaration ?ast)
                         (aststructure/constructorinvocation-constructordeclaration ?invocation ?ast)]))))
                                  
          

(defn
  overrides
  [?ast ?overridden]
  (cl/all
    (el/v+ ?ast)
    (aststructure/methoddeclaration-methoddeclaration|overrides ?overridden ?ast)))

  




(defn to-primitive-type-code
  [string]
  (PrimitiveType/toCode string)) 

(defn to-modifier-keyword
  [string]
  (Modifier$ModifierKeyword/toKeyword string)) 

(defn to-assignment-operator
  [string]
  (Assignment$Operator/toOperator string)) 

(defn to-infix-expression-operator
  [string]
  (InfixExpression$Operator/toOperator string)) 

(defn to-prefix-expression-operator
  [string]
  (PrefixExpression$Operator/toOperator string)) 

(defn to-postfix-expression-operator
  [string]
  (PostfixExpression$Operator/toOperator string)) 

(defn 
  template-to-string|projected
  [template variables values]
  (let [var2value
        (zipmap variables values)
        templategroup
        (snippetgroup/make-snippetgroup "Dummy group" [template])
        jtemplategroup
        (TemplateGroup/newFromClojureGroup templategroup)
        generator 
        (TemplateCodeGenerator. jtemplategroup var2value)]
    (.prettyPrintSnippet generator template)))

(defn
  qwal-graph-from-list
  [^java.util.List jdt-lst]
  (let
    [length
     (.size jdt-lst)]
    {:nodes 
     (concat
       (map-indexed vector jdt-lst)
       [[-1 nil] ;before first element
        [length nil] ;after last element
        ])
     :predecessors
     (fn 
       [?from ?to]
       (cl/all 
         (el/perform (throw (Exception. "Backwards traversal not supported."))))) ;skipping because not needed by snippets
     :successors
     (fn 
       [?from ?to]
       (cl/all
         (cl/project [?from]
                     (cl/== ?to
                            (let [idx (nth ?from 0)
                                  nidx (inc idx)]
                              (cond
                                (< nidx length)
                                [[nidx (.get jdt-lst nidx)]]
                                (= nidx length)
                                [[nidx nil]]
                                (> nidx length)
                                []))))))
     }))


(defn
  value|list-qwal-start-end
  "Used in list matching using regular expresisons.

  (damp.ekeko/ekeko [?block ?el]
           (cl/fresh [?nodes ?start ?end ?lst ?q]
                  (ast/ast :Block ?block)
                  (ast/has :statements ?block ?lst)
                  (damp.ekeko.snippets.runtime/value|list-qwal-start-end ?lst ?q ?start ?end)
                  (damp.qwal/qwal ?q ?start ?end
                                  [] 
                                  (damp.qwal/q=>*)
                                  (damp.qwal/qcurrent [[idx el]]
                                                      (el/equals ?el el)
                                                      (ast/ast :ReturnStatement ?el)
                                                      )
                                  (damp.qwal/q=>*)
                                  )                           
                  ))"
  [?lst ?qwal ?start ?end]
  (cl/fresh [?raw]
         (ast/value|list ?lst)
         (ast/value-raw ?lst ?raw)
         (el/equals ?qwal (qwal-graph-from-list ?raw))
         (el/equals ?start [-1 nil])
         (el/equals ?end [(.size ?raw) nil])))



(defn
  list-nth-element
  [?lstval ?nth ?element]
  ;on purpose not using value-raw because it will bind lstval when unbound, 
  ;while an unbound lstval indicates an error in the generated query
  (cl/fresh [?raw]
            (ast/value-raw ?lstval ?raw)
            (el/equals ?element (.get ?raw ?nth))))


(defn
  list-size
  [?lstval ?size]
  ;on purpose not using value-raw because it will bind lstval when unbound, 
  ;while an unbound lstval indicates an error in the generated query
  (cl/fresh [?raw]
            (ast/value-raw ?lstval ?raw)
            (el/equals ?size (.size ?raw))))


(defn
  rawlist-element-remaining 
  [?lst ?el ?rem]
  (cl/all
    (el/contains ?lst ?el) 
    (cl/project [?el ?lst]
                (cl/== ?rem (remove (fn [e] (identical? e ?el)) ?lst)))))
              
    
  
  
  




    
    
        

  


