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
             [rewrites :as rewrites]])
  (:require 
    [damp.ekeko [logic :as el]]
    [damp.ekeko.jdt 
     [astnode :as astnode]]))

(declare directives-rewriting)

(def ^:dynamic *sgroup-rhs* nil) ; Returns the RHS snippetgroup; its value is bound dynamically by querying/snippetgroup-conditions|rewrite TODO Would be better to refactor this

(defn find-equals-rhs-snippet [uservar rhs-snippets]
  "Find the snippet in which an equals/replace-by-variable directive occurs with uservar as its operand"
  (some 
    (fn [snippet]
      (some
        (fn [node]
          (let [bds (snippet/snippet-bounddirectives-for-node snippet node)
                equals-rhs (directives/bounddirective-for-directive bds damp.ekeko.snippets.matching/directive-equals)
                metavar-rhs (directives/bounddirective-for-directive bds damp.ekeko.snippets.matching/directive-replacedbyvariable)
                bd-rhs (if (nil? equals-rhs) 
                         metavar-rhs
                         equals-rhs)
                ]
            (if (and 
                  (not (nil? bd-rhs))
                  (= uservar (symbol (.getValue (second (directives/bounddirective-operandbindings bd-rhs))))))
              snippet)))
        (snippet/snippet-nodes snippet)))
    rhs-snippets))

(defn determine-rewrite-cu
  "Determine which compilation unit should be rewritten.
   This function actually returns a meta-variable. Its value is an AST node in the compilation unit that should be rewritten."
  [var]
  (let [snippet (find-equals-rhs-snippet var *sgroup-rhs*)]
    (if (nil? snippet)
      ; Base case: If we can't find an equals directive referring to var, then it must be bound in the LHS
      var
      ; Recursive step: If we find a RHS snippet in which the desired equals occurs, then check the variable in the rewrite directive (assumed to be in the root)
      (let [root-bds (snippet/snippet-bounddirectives-for-node snippet (snippet/snippet-root snippet))
            rewrite-bd (first (filter (fn [bd]
                                        (some (fn [rewr-dir]
                                                (= (directives/directive-name rewr-dir)
                                                   (directives/directive-name (directives/bounddirective-directive bd))))
                                              directives-rewriting))
                                      root-bds))
            new-var (symbol (.getValue (second (directives/bounddirective-operandbindings rewrite-bd))))]
        (recur new-var)))))

(def replace-node `rewrites/replace-node)
(def replace-value `rewrites/replace-value)
(def add-element `rewrites/add-element)
(def remove-element `rewrites/remove-element)
(def remove-element-alt `rewrites/remove-element-alt)
(def move-element `rewrites/move-element)
(def copy-node `rewrites/copy-node)

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
    (let [cu-var (determine-rewrite-cu (symbol replacement-var-string))
          var-generatedcode (snippet/snippet-var-for-node snippet val)
          var (symbol replacement-var-string)]
      `((el/perform (~replace-node ~cu-var ~var ~var-generatedcode))))))

(defn
  rewrite-replace-value
  [val replacement-var-string]
  (fn [snippet]
    (let [cu-var (determine-rewrite-cu (symbol replacement-var-string))
          var-generatedcode (snippet/snippet-var-for-node snippet val)
          var (symbol replacement-var-string)]
      `((el/perform 
          (~replace-value ~cu-var ~var ~var-generatedcode))))))
    
(defn
  rewrite-add-element
  [val target-list-var-string]
  (fn [snippet]
    (let [cu-var (determine-rewrite-cu (symbol target-list-var-string))
          var-generatedcode (snippet/snippet-var-for-node snippet val)
          var (symbol target-list-var-string)]
      `((el/perform 
          (~add-element ~cu-var ~var ~var-generatedcode -1))))))

(defn
  rewrite-remove-element
  [val idx]
  (fn [snippet]
    (let [bds (snippet/snippet-bounddirectives-for-node snippet val)
          repl-by-var (directives/bounddirective-for-directive bds damp.ekeko.snippets.matching/directive-replacedbyvariable)
          lst-var (symbol (.getValue (second (directives/bounddirective-operandbindings repl-by-var))))
          cu-var (determine-rewrite-cu (symbol lst-var))]
      `((el/perform
          (~remove-element ~cu-var ~lst-var ~idx))))))

(defn
  rewrite-remove-element-alt
  [val target-list-var-string]
  (fn [snippet]
    (let [cu-var (determine-rewrite-cu (symbol target-list-var-string))
          var-generatedcode (snippet/snippet-var-for-node snippet val)
          var (symbol target-list-var-string)
;          idx-var (util/gen-lvar "idx")
          ]
      `((cl/fresh []
;                  (el/contains ~var ~var-generatedcode)
;                  (runtime/list-nth-element ~var 0 ~var-generatedcode) 
                  (el/perform
                    (~remove-element-alt ~cu-var ~var ~var-generatedcode)))))))

(defn
  rewrite-move-element
  [source-elem target-list idx]
  (fn [snippet]
    (let [bds (snippet/snippet-bounddirectives-for-node snippet source-elem)
          repl-by-var (directives/bounddirective-for-directive bds damp.ekeko.snippets.matching/directive-replacedbyvariable)
          source-lst-var (symbol (.getValue (second (directives/bounddirective-operandbindings repl-by-var))))
          src-cu-var (determine-rewrite-cu source-lst-var)
          tgt-cu-var (determine-rewrite-cu (symbol target-list))
;          source-elem-val (snippet/snippet-var-for-node snippet source-elem)
          target-list-val (symbol target-list)]
      `((el/perform
          (~move-element ~src-cu-var ~tgt-cu-var 
                         ~source-lst-var ~target-list-val ~idx))))))

(defn
  rewrite-copy-node
  [source-elem target-list idx]
  (fn [snippet]
    (let [bds (snippet/snippet-bounddirectives-for-node snippet source-elem)
          repl-by-var (directives/bounddirective-for-directive bds damp.ekeko.snippets.matching/directive-replacedbyvariable)
          source-lst-var (symbol (.getValue (second (directives/bounddirective-operandbindings repl-by-var))))
          src-cu-var (determine-rewrite-cu source-lst-var)
          tgt-cu-var (determine-rewrite-cu (symbol target-list))
          target-list-val (symbol target-list)]
      `((el/perform
          (~copy-node ~src-cu-var ~tgt-cu-var 
                         ~source-lst-var ~target-list-val ~idx))))))

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
    [(directives/make-directiveoperand "Target list")
     (directives/make-directiveoperand "Target index")]
    rewrite-add-element
    "Adds the instantiated template to the list operand at the given index (-1 inserts at the end)."))

(def
  directive-remove-element
  (directives/make-directive
    "remove-element"
    [(directives/make-directiveoperand "Target index")]
    rewrite-remove-element
    "Removes the element of the given index from the list in the subject."))

(def
  directive-remove-element-alt
  (directives/make-directive
    "remove-element-alt"
    [(directives/make-directiveoperand "Target list")]
    rewrite-remove-element-alt
    "Removes the given template to the list operand."))

(def
  directive-move-element
  (directives/make-directive
    "move-element"
    [(directives/make-directiveoperand "Target list")
     (directives/make-directiveoperand "Target index")]
    rewrite-move-element
    "Moves the subject (a list element) into a target list at the given index."))

(def
  directive-copy-node
  (directives/make-directive
    "copy-node"
    [(directives/make-directiveoperand "Target list")
     (directives/make-directiveoperand "Target index")]
    rewrite-copy-node
    "Copies the subject into a target list at the given index."))

(def 
  directives-rewriting
  [directive-replace
   directive-replace-value
   directive-add-element
   directive-remove-element
   directive-remove-element-alt
   directive-move-element
   directive-copy-node])

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