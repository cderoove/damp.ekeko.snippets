(ns 
  ^{:doc "Representation for directives and their operands."
    :author "Coen De Roover"}
  damp.ekeko.snippets.directives
  )
;; Matching directives and their operands  information

(defrecord 
  Directive
  [name operands generator description])

(defn
  make-directive
  [name operands generator description]
  (Directive. name operands generator description))

(defn
  directive-name
  [directive]
  (:name directive))

(defn
  directive-description
  [directive]
  (:description directive))

(defn 
  directive-generator
  "Returns function generating conditions for given directive."
  [directive]
  (:generator directive))

(defn 
  directive-operands 
  "Returns operands for given directive."
  [directive]
  (:operands directive))

(defn
  directive-arity
  [directive]
  (count (directive-operands directive)))

(defn
  directive?
  "Checks whether a value is a Directive instance."
  [value]
  (instance? Directive value))

;; Operand information

(defrecord 
  DirectiveOperand
  [description])

(defn
  make-directiveoperand 
  [description]
  (DirectiveOperand. description))

(defn 
  directiveoperand-description
  "Returns description of operand."
  [directiveoperand]
  (:description directiveoperand))

(defn
  make-directiveoperand-binding
  [directiveoperand value]
  (damp.ekeko.snippets.DirectiveOperandBinding. directiveoperand value))

(defn
  directiveoperandbinding-directiveoperand
  [binding]
  (.operand binding))
  
(defn
  directiveoperandbinding-value
  [binding]
  (.value binding))

(defn
  set-directiveoperandbinding-value!
  [binding val]
  (set! (.value binding) val))
 

(defn
  directive-bindings-for-directiveoperands
  "Returns fresh bindings for the operands of the given directive."
  [directive]
  (map (fn [operand]
         (make-directiveoperand-binding operand  "?operand"))
       (directive-operands directive)))


(defn
  make-implicit-operand
  [subject-template-node]
  (make-directiveoperand-binding
      (make-directiveoperand "Template element")
      subject-template-node))

(defn
  directive-bindings-for-directiveoperands-and-match
  "Returns fresh bindings for a directive's match variable and its additional operands."
  [template subject-template-node directive]
  (cons
    (make-implicit-operand subject-template-node)
    (directive-bindings-for-directiveoperands directive)))

;; Directive with bindings for its operands (implemented imperatively to support JFace viewers)

(defn
  make-bounddirective
  [directive operandbindings]
  (damp.ekeko.snippets.BoundDirective. directive operandbindings))

(defn
  bounddirective-directive
  [bounddirective]
  (.directive bounddirective))

(defn
  bounddirective-operandbindings
  [bounddirective]
  (.operandBindings bounddirective))
  
(defn
  set-bounddirective-operandbindings!
  [bounddirective bindings]
  (set! (.operandBindings bounddirective) bindings))



(defn 
  bind-directive-with-defaults
  [directive snippet value]
  (make-bounddirective 
    directive
    (directive-bindings-for-directiveoperands-and-match
      snippet
      value
      directive)))



(defn
  snippet-bounddirective-conditions
  "Generatings matching conditions for the given snippet's bound directive."
  [snippet bounddirective]
  (let [generator (directive-generator (bounddirective-directive bounddirective))
        opvals (map directiveoperandbinding-value (bounddirective-operandbindings bounddirective))]
  ((apply generator opvals) snippet)))



(defn
  bounddirective-string
  "Returns a human-readable sexp string for the bound directive."
  [bounddirective]
  (let [name 
        (directive-name (bounddirective-directive bounddirective))
        operandbindings
        (rest (bounddirective-operandbindings bounddirective)) ;first=match
        ]
    (if 
      (not-empty operandbindings)
      (str "("
           name 
           " "
           (clojure.string/join " " 
                                (map (fn [operandbinding]
                                       (str (directiveoperandbinding-value operandbinding)))
                                     operandbindings))
           ")")
      (str name))))
      
;not comparing based on directive itself, directives seems to differ when produced by Clojure reader
(defn
  bounddirective-for-directive
  [bounddirectives directive]
  (let [name (directive-name directive)]
    (some (fn [bounddirective]
          (when
            (= name 
               (directive-name (bounddirective-directive bounddirective)))
            bounddirective))
        bounddirectives)))

(defn
  bounddirective-directive-description
  [bounddirective]
  (directive-description (bounddirective-directive bounddirective)))


(defn
  register-callbacks
  []
  (set! (damp.ekeko.snippets.BoundDirective/FN_BOUNDDIRECTIVE_DESCRIPTION) bounddirective-directive-description)
  (set! (damp.ekeko.snippets.BoundDirective/FN_BOUNDDIRECTIVE_STRING) bounddirective-string)
  
  (set! (damp.ekeko.snippets.gui.DirectiveSelectionDialog/FN_DIRECTIVE_NAME) directive-name)
  (set! (damp.ekeko.snippets.gui.DirectiveSelectionDialog/FN_DIRECTIVE_DESCRIPTION) directive-description)
  (set! (damp.ekeko.snippets.gui.DirectiveSelectionDialog/FN_DIRECTIVE_ARITY) directive-arity)


  
  )

  (register-callbacks)



