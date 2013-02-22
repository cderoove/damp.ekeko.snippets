(ns 
  ^{:doc "Runtime predicates for snippet-driven querying."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets.runtime
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko [logic :as el]]
            [damp.ekeko.jdt
             [basic :as basic]
             [reification :as reification]]))


(defn
  type-relaxmatch-subtype
   "Predicate to check type match ?ltype = ?type or ?ltype = subtype of ?type."
  [?keyword ?type ?ltype]
  (cl/conde [(el/equals ?type ?ltype)]
            [(cl/fresh [?itype ?iltype]
                       (reification/ast-type-type ?keyword ?type ?itype)
                       (reification/ast-type-type ?keyword ?ltype ?iltype)
                       (reification/type-super-type ?iltype ?itype))]))            
             
(defn
  assignment-relaxmatch-variable-declaration
  "Predicate to check relaxmatch of VariableDeclarationStatement (+ initializer) with Assignment."
  [?statement ?left ?right]
  (cl/conde [(cl/fresh [?assignment]
                       (reification/ast :ExpressionStatement ?statement)
                       (reification/has :expression ?statement ?assignment)                        
                       (reification/has :leftHandSide ?assignment ?left)
                       (reification/has :rightHandSide ?assignment ?right))]
            [(cl/fresh [?fragments ?fragmentsraw ?fragment ?fvalue ?avalue ?fname ?aname]
                       (reification/ast :VariableDeclarationStatement ?statement)
                       (reification/has :fragments ?statement ?fragments)
                       (reification/listvalue ?fragments)
                       (reification/value-raw ?fragments ?fragmentsraw)
                       (el/equals 1 (.size ?fragmentsraw))
                       (el/equals ?fragment (.get ?fragmentsraw 0))
                       (reification/ast :VariableDeclarationFragment ?fragment)
                       (reification/has :name ?fragment ?left)
                       (reification/has :initializer ?fragment ?right))]))

(defn
  ast-invocation-declaration
   "Relation between ASTNode invocation with it's declaration."
  [?inv ?dec]
  (cl/fresh [?k-inv ?k-dec ?b]
            (reification/ast-invocation-binding ?k-inv ?inv ?b)
            (reification/ast-declares-binding ?k-dec ?dec ?b)))



