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
  typebinding-extends-typebinding-with-depth
  [?type ?stype depth]
  (if (> depth 0)
    (cl/conde [(basic/typebinding-extends-typebinding ?type ?stype)]
              [(cl/fresh [?mtype]
                         (basic/typebinding-extends-typebinding ?type ?mtype)
                         (typebinding-extends-typebinding-with-depth ?mtype ?stype (- depth 1)))])))

(defn
  type-relaxmatch-subtype-with-depth
   "Predicate to check type match ?ltype = ?type or ?ltype = subtype of ?type."
  [?keyword ?type ?ltype depth]
  (cl/conde [(el/equals ?type ?ltype)]
            [(cl/fresh [?itype ?iltype]
                       (reification/ast-type-binding ?keyword ?type ?itype)
                       (reification/ast-type-binding ?keyword ?ltype ?iltype)
                       (typebinding-extends-typebinding-with-depth ?iltype ?itype depth))]))
             
(defn 
  name-exactmatch
  "Predicate to check whether SimpleName name1 has the same identifier with name2."
  [?name1 ?name2]
  (cl/fresh [?id1 ?id2 ?val]
         (reification/ast :SimpleName ?name1)
         (reification/ast :SimpleName ?name2)
         (reification/has :identifier ?name1 ?id1)
         (reification/has :identifier ?name2 ?id2)
         (reification/value-raw ?id1 ?val)
         (reification/value-raw ?id2 ?val)))

(defn 
  value-exactmatch
  "Predicate to check whether value val1 has the same value-raw with val2."
  [?val1 ?val2]
  (cl/fresh [?primval1 ?primval2 ?val]
         (cl/conde [(reification/ast :NumberLiteral ?val1)
                    (reification/ast :NumberLiteral ?val2)
                    (reification/has :token ?val1 ?primval1)
                    (reification/has :token ?val2 ?primval2)
                    (reification/value-raw ?primval1 ?val)
                    (reification/value-raw ?primval2 ?val)]
                   [(reification/ast :StringLiteral ?val1)
                    (reification/ast :StringLiteral ?val2)
                    (reification/has :escapedValue ?val1 ?primval1)
                    (reification/has :escapedValue ?val2 ?primval2)
                    (reification/value-raw ?primval1 ?val)
                    (reification/value-raw ?primval2 ?val)]
                   [(reification/ast :CharacterLiteral ?val1)
                    (reification/ast :CharacterLiteral ?val2)
                    (reification/has :escapedValue ?val1 ?primval1)
                    (reification/has :escapedValue ?val2 ?primval2)
                    (reification/value-raw ?primval1 ?val)
                    (reification/value-raw ?primval2 ?val)])))

(defn
  assignment-relaxmatch-variable-declaration
  "Predicate to check relaxmatch of VariableDeclarationStatement (+ initializer) with Assignment."
  [?statement ?assignment]
  (cl/conde [(reification/ast :ExpressionStatement ?statement)
             (reification/has :expression ?statement ?assignment)]
            [(cl/fresh [?fragments ?fragmentsraw ?fragment ?fvalue ?avalue ?fname ?aname]
                       (reification/ast :VariableDeclarationStatement ?statement)
                       (reification/has :fragments ?statement ?fragments)
                       (reification/listvalue ?fragments)
                       (reification/value-raw ?fragments ?fragmentsraw)
                       (el/equals 1 (.size ?fragmentsraw))
                       (el/equals ?fragment (.get ?fragmentsraw 0))
                       (reification/ast :VariableDeclarationFragment ?fragment)
                       (reification/has :initializer ?fragment ?fvalue)
                       (reification/has :rightHandSide ?assignment ?avalue)
                       (value-exactmatch ?fvalue ?avalue)
                       (reification/has :name ?fragment ?fname)
                       (reification/has :leftHandSide ?assignment ?aname)
                       (name-exactmatch ?fname ?aname))])) 


;;not used
(comment
(defn
   val-exactmatch-logiclist
   "Predicate to check exact match java property value ?val with snippet list ?llist."
   [?val ?llist]
   (list-exactmatch-logiclist (:value ?val) ?llist))

(defn
   list-exactmatch-logiclist
   "Predicate to check exact match java list ?list with snippet list ?llist."
   [?list ?llist]
   (cl/conde [(cl/emptyo ?list)
           (el/equals true (empty? ?llist))]
          [(cl/fresh [?head ?tail ?ltail]
                  (cl/conso ?head ?tail ?list)
                  (el/equals ?head (first ?llist))
                  (el/equals ?ltail (rest ?llist))
                  (cl/== ?tail ?ltail)
                  (list-exactmatch-logiclist ?tail ?ltail))]))

(defn
   list-element-before
   "Predicate to check exact match java property value ?val with snippet list ?llist."
   [?list ?el1 ?el2]
   (< (.indexOf ?list ?el1) (.indexOf ?list ?el2)))

)

