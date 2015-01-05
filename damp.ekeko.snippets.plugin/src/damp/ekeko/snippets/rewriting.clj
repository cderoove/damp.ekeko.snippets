(ns 
  ^{:doc "Rewriting directives for template-based program transformations."
    :author "Coen De Roover"}
  damp.ekeko.snippets.rewriting
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko.snippets 
             [matching :as matching]
             [snippet :as snippet]
             [directives :as directives]
             [util :as util]
             [parsing :as parsing]
             [runtime :as runtime]
             ])
  (:require 
    [damp.ekeko [logic :as el]]
    [damp.ekeko.jdt 
     [astnode :as astnode]
     [rewrites :as rewrites]]))

(def replace-node `rewrites/replace-node)
(def replace-value `rewrites/replace-value)
(def add-element `rewrites/add-element)

;used to instantiate templates
(defn
  clone-compatible-with-ast
  [valuetobecloned ast]
  (cond (astnode/ast? valuetobecloned)
        (org.eclipse.jdt.core.dom.ASTNode/copySubtree ast valuetobecloned)
        (astnode/lstvalue? valuetobecloned)
        (org.eclipse.jdt.core.dom.ASTNode/copySubtrees ast (astnode/value-unwrapped valuetobecloned))
        :default
        (throw (IllegalArgumentException. (str "Don't know how to clone given value: " valuetobecloned)))))


;according to rewrite API
;~var-generatedcode has to be freshly generated, not be part of the ast of ~var!!!
(defn
  rewrite-replace
  [val replacement-var-string]
  (fn [snippet]
    (let [var-generatedcode (snippet/snippet-var-for-node snippet val)
          var (symbol replacement-var-string)]
      `((el/perform (~replace-node ~var ~var-generatedcode))))))

(defn
  rewrite-replace-value
  [val replacement-var-string]
  (fn [snippet]
    (let [var-generatedcode (snippet/snippet-var-for-node snippet val)
          var (symbol replacement-var-string)]
      `((el/perform 
          (~replace-value ~var ~var-generatedcode))))))


;(defn
;  rewrite-replace-value
 ; [val replacement-var-or-sexp-string]
 ; (fn [snippet]
  ;  (let [var-generatedcode
  ;;        (snippet/snippet-var-for-node snippet val)
  ;        replacementexp
  ;        (if 
  ;          (matching/string-represents-variable? replacement-var-or-sexp-string)
   ;         (symbol replacement-var-or-sexp-string)
   ;;         (read-string replacement-var-string))
   ;       var-replacementvalue
   ;       (util/gen-lvar 'replacement)]
    ;  `((cl/fresh [~var-replacementvalue]
    ;;              ;to project vars in replacementexp
    ;              (el/equals ~var-replacementvalue ~replacementexp) 
    ;              (el/perform (rewrites/replace-node ~var ~var-generatedcode)))))))


(defn
  rewrite-add-element
  [val target-list-var-string]
  (fn [snippet]
    (let [var-generatedcode 
          (snippet/snippet-var-for-node snippet val)
          var
          (symbol target-list-var-string)]
      `((el/perform 
          (~add-element ~var ~var-generatedcode -1)))))) ;-1 indicates last position for ListRewrite


(def
  directive-replace
  (directives/make-directive
    "replace"
    [(directives/make-directiveoperand "Replacement")]
    rewrite-replace 
    "Replaces its ASTNode operand by the instantiated template."))



(def
  directive-replace-value
  (directives/make-directive
    "replace-value"
    [(directives/make-directiveoperand "Replacement")]
    rewrite-replace-value 
    "Replaces its value operand by the instantiated template."))



(def
  directive-add-element
  (directives/make-directive
    "add-element"
    [(directives/make-directiveoperand "Target list")]
    rewrite-add-element
    "Adds the instantiated template to the list operand."))



(def 
  directives-rewriting
  [directive-replace
   directive-replace-value
   directive-add-element])


(defn 
  registered-directives
  []
  directives-rewriting)


(defn
  registered-rewriting-directive?
  [directive]
  (let [name (directives/directive-name directive)]
    (some #{name}
          (map directives/directive-name directives-rewriting))))


(defn
  registered-directive-for-name
  [name]
  (some
    (fn [directive]
      (when (= name (directives/directive-name directive))
        directive))
    (registered-directives)))
