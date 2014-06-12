(ns 
  ^{:doc "Test suite for snippet operators."
    :author "Siltvani, Coen De Roover"}
  test.damp.ekeko.snippets.opperators
  (:refer-clojure :exclude [== type declare record?])
  (:require  [clojure.core.logic :exclude [is] :as l])
  (:require [damp.ekeko.snippets 
             [querying :as querying]
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [matching :as matching]
             [parsing :as parsing]
             [util :as util]
             [persistence :as persistence]
             [operators :as operators]
             ])
  (:require [test.damp [ekeko :as test]])
  (:require [damp.ekeko.jdt 
             [astnode :as astnode]
             [ast :as ast]
             ])
  (:require [damp.ekeko 
             [logic :as el]
             [snippets :as snippets]])
  (:use clojure.test))


;erase list

;add regexp
;insert node


(defn
  snippet-values-for-property
  [snippet propertyidstr]
  (filter 
    (fn [val]
      (when-let [ownerprop (astnode/owner-property val)]
        (= propertyidstr (astnode/property-descriptor-id ownerprop))))
    (snippet/snippet-nodes snippet)))
  
(deftest
  test-opsequence-eraselist|insertemptystatement|replacebywildcard
  (for [[method] 
        (damp.ekeko/ekeko [?m] 
                          (l/fresh [?block ?statements ?raw]
                                 (ast/ast :MethodDeclaration ?m)
                                 (ast/has :body ?m ?block)
                                 (ast/ast :Block ?block)))]
    (let [snippet
          (matching/jdt-node-as-snippet method) ;create snippet
          snippet-lst
          (some (fn [val]
                  (when
                    (and
                      val
                      (astnode/lstvalue? val))
                    val))
                (snippet-values-for-property snippet "statements"))
          
          newsnippet
          (->
            snippet
            (operators/erase-list snippet-lst) 
            (operators/insert-newnodefromclasskeyw-atindex snippet-lst :EmptyStatement 0)
            (operators/replace-by-wildcard (.get (astnode/value-unwrapped snippet-lst) 0))
            (operators/update-multiplicity (.get (astnode/value-unwrapped snippet-lst) "*"))
            )]
      (snippets/query-by-snippet newsnippet))))
      
    
;; Test suite
;; ----------
(deftest
   test-suite 
   (let [testproject "TestCase-JDT-CompositeVisitor"]
;     (test/against-project-named testproject false operator-eraselist)

   ))

(defn 
  test-ns-hook 
  []
  (test/with-ekeko-disabled test-suite))


(comment  
  ;;Example repl session 
  (run-tests)
  )
  

