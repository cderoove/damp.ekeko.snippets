(ns 
  ^{:doc "Representation for directives and their operands."
    :author "Coen De Roover"}
  damp.ekeko.snippets.directives
  )
;; Matching directives and their operands  information

(defrecord 
  Directive
  [description operands generator])

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
         (make-directiveoperand-binding operand  nil))
       (directive-operands directive)))

(defn
  directive-bindings-for-directiveoperands-and-match
  "Returns fresh bindings for a directive's match variable and its additional operands."
  [template subject-template-node directive]
  (cons
    (make-directiveoperand-binding
      (make-directiveoperand "Match for template node")
      subject-template-node)
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
  snippet-bounddirective-conditions
  [snippet bounddirective]
  (let [generator (directive-generator (bounddirective-directive bounddirective))
        opvals (map directiveoperandbinding-value (bounddirective-operandbindings bounddirective))]
  ((apply generator opvals) snippet)))

  
(defn
  bounddirective-string
  "Returns a human-readable sexp string for the bound directive."
  [bounddirective]
  (let [generatorname 
        (:name (meta (directive-generator (bounddirective-directive bounddirective))))
        operandbindings
        (rest (bounddirective-operandbindings bounddirective)) ;first=match
        ]
    (if 
      operandbindings
      (str "("
           generatorname 
           " "
           (clojure.string/join " " 
                                (map (fn [operandbinding]
                                       (str (directiveoperandbinding-value operandbinding)))
                                     operandbindings))
           ")")
      (str generatorname))))
      
(defn
  bounddirective-for-directive
  [bounddirectives directive]
  (some (fn [bounddirective]
          (when
            (= directive (bounddirective-directive bounddirective))
            bounddirective))
        bounddirectives))
