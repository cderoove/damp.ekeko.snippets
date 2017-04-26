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
(declare directive-replace)
(declare directive-insert-after)
(declare directive-insert-before)
(declare directive-add-element)
(declare directive-move-element)
(declare directive-remove-element)

(def ^:dynamic *sgroup-rhs* nil) ; Returns the RHS snippetgroup; its value is bound dynamically by querying/snippetgroup-conditions|rewrite TODO Would be better to refactor this..

(defn find-equals-rhs-snippet [uservar rhs-snippets]
  "Find the snippet and node in which an equals/replace-by-variable directive occurs with uservar as its operand"
  (some 
    (fn [snippet]
      (some
        (fn [node]
          (let [bds (snippet/snippet-bounddirectives-for-node snippet node)
                has-exception-bds (some (fn [bd]
                                          (let [name (directives/directive-name (directives/bounddirective-directive bd))]
                                            (or
                                              (= name (directives/directive-name directive-move-element))
                                              (= name (directives/directive-name directive-remove-element)))))
                                          bds)
                
                equals-rhs (directives/bounddirective-for-directive bds damp.ekeko.snippets.matching/directive-equals)
                metavar-rhs (directives/bounddirective-for-directive bds damp.ekeko.snippets.matching/directive-replacedbyvariable)
                bd-rhs (if (nil? equals-rhs) 
                         (if (not has-exception-bds) ; Meta-variables as the subject of a move-element or remove-element should be excluded!
                           metavar-rhs)
                         equals-rhs)
                ]
            (if (and 
                  (not (nil? bd-rhs))
                  (= uservar (symbol (.getValue (second (directives/bounddirective-operandbindings bd-rhs))))))
              [snippet node])))
        (snippet/snippet-nodes snippet)))
    rhs-snippets))

(defn determine-rewrite-cu
  "Determine which compilation unit should be rewritten.
   This function actually returns a meta-variable. Its value is an AST node in the compilation unit that should be rewritten."
  [var]
  (let [snippet (first (find-equals-rhs-snippet var *sgroup-rhs*))]
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

(defn determine-rewrite-list
  "Determine the list in which a given element should be inserted.
   This function actually returns a meta-variable to which that list is bound .. or a list element of which you can directly get the owner."
  [var]
  (let [[snippet eq-node] (find-equals-rhs-snippet var *sgroup-rhs*)
        second-op (fn [rewrite-bd]
                    (symbol (.getValue (second (directives/bounddirective-operandbindings rewrite-bd)))))]
    (cond 
      ; Base case: If we can't find an equals directive referring to var, then it must be bound in the LHS
      (nil? snippet) 
      var
      
      ; Base case: If the node where the variable was bound is not the root (and so does not have a rewrite directive), then we should be able to directly get the owner
      (not (= eq-node (snippet/snippet-root snippet)))
      var
      
      :else
      (let [root-bds (snippet/snippet-bounddirectives-for-node snippet (snippet/snippet-root snippet))
            elem-bd (first (filter (fn [bd]
                                     (some (fn [rewr-dir]
                                             (= (directives/directive-name rewr-dir)
                                                (directives/directive-name (directives/bounddirective-directive bd))))
                                           [directive-replace directive-insert-after directive-insert-before])) ; These directives have a list element as their operand
                                   root-bds))]
        (if (nil? elem-bd)
          ; Base case: If the equals node has a rewrite directive, which has a list operand
          (let [list-bd (first (filter (fn [bd]
                                         (some (fn [rewr-dir]
                                                 (= (directives/directive-name rewr-dir)
                                                    (directives/directive-name (directives/bounddirective-directive bd))))
                                               [directive-add-element directive-move-element])) ; These directives have a list as their operand
                                       root-bds))]
            
            (second-op elem-bd))
          ; Recursive case: If the equals-node has a rewrite directive, which has a list-element as operand
          (recur (second-op elem-bd)))))))

(def replace-node `rewrites/replace-node)
(def replace-value `rewrites/replace-value)
(def add-element `rewrites/add-element)
(def insert-before `rewrites/insert-before)
(def insert-after `rewrites/insert-after)
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
  rewrite-insert-before
  [val elem-var-string]
  (fn [snippet]
    (let [cu-var (determine-rewrite-cu (symbol elem-var-string))
          list-var (determine-rewrite-list (symbol elem-var-string))
          
          var-generatedcode (snippet/snippet-var-for-node snippet val)
          var (symbol elem-var-string)]
      `((el/perform 
          (~insert-before ~cu-var ~list-var ~var ~var-generatedcode))))))

(defn
  rewrite-insert-after
  [val elem-var-string]
  (fn [snippet]
    (let [cu-var (determine-rewrite-cu (symbol elem-var-string))
          list-var (determine-rewrite-list (symbol elem-var-string))
          
          var-generatedcode (snippet/snippet-var-for-node snippet val)
          var (symbol elem-var-string)]
      `((el/perform 
          (~insert-after ~cu-var ~list-var ~var ~var-generatedcode))))))
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
  directive-insert-before
  (directives/make-directive
    "insert-before"
    [(directives/make-directiveoperand "Target element")]
    rewrite-insert-before
    "Adds the instantiated template before the operand, a list element."))

(def
  directive-insert-after
  (directives/make-directive
    "insert-after"
    [(directives/make-directiveoperand "Target element")]
    rewrite-insert-after
    "Adds the instantiated template after the operand, a list element."))

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
    "Removes the given list element."))

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
   directive-insert-before
   directive-insert-after
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