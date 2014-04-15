(ns 
  damp.ekeko.snippets.persistence
  (:require 
    [damp.ekeko.jdt
     [astnode :as astnode]]
    [damp.ekeko.snippets 
     [util :as util]
     [directives :as directives]
     [snippet :as snippet]
     [snippetgroup :as snippetgroup]
     [parsing :as parsing]
     [matching :as matching]
     ])
  (:import [org.eclipse.jdt.core.dom 
            ASTNode
            StructuralPropertyDescriptor
            Modifier$ModifierKeyword]
           [damp.ekeko.snippets
            BoundDirective
            DirectiveOperandBinding]
           [damp.ekeko.snippets.snippet
            Snippet]
           [damp.ekeko.snippets.snippetgroup
            SnippetGroup]
           [damp.ekeko.snippets.directives
            Directive DirectiveOperand]
           ))

;;TODO: dispatch on node type, call parse-string with correct node type
(defmethod 
  clojure.core/print-dup 
  ASTNode
  [node w]
  (.write w (str  "#=" `(parsing/parse-string-ast ~(str node)))))

(defn
  class-propertydescriptor-with-id
  [ownerclasskeyword pdid]                
  (some (fn [pd]
          (when (= pdid
                   (astnode/property-descriptor-id pd))
            pd))
        (astnode/nodeclass-property-descriptors 
          (astnode/class-for-ekeko-keyword ownerclasskeyword)
          )))


(defmethod
  clojure.core/print-dup 
  StructuralPropertyDescriptor
  [pd w]
  (let [id 
        (astnode/property-descriptor-id pd)
        ownerclass
        (astnode/property-descriptor-owner-node-class pd)
        ownerclass-keyword
        (astnode/ekeko-keyword-for-class ownerclass)
        ]
    (.write w (str  "#=" `(class-propertydescriptor-with-id ~ownerclass-keyword ~id)))))

(defmethod 
  clojure.core/print-dup 
  Modifier$ModifierKeyword
  [node w]
  (let [flagvlue (.toFlagValue node)]
    (.write w (str  "#=" `(Modifier$ModifierKeyword/fromFlagValue ~flagvlue)))))


(defmethod 
  clojure.core/print-dup 
  BoundDirective
  [bd w]
  (let [directive 
        (directives/bounddirective-directive bd) 
        opbindings
        (directives/bounddirective-operandbindings bd)
        opbindings-without-implicit-operandbinding
        (rest opbindings)
        ]
    (.write w (str  "#=" `(directives/make-bounddirective ~directive ~opbindings-without-implicit-operandbinding)))))

(defmethod 
  clojure.core/print-dup 
  DirectiveOperandBinding
  [bd w]
  (let [directiveoperand
        (directives/directiveoperandbinding-directiveoperand bd) 
        value
        (directives/directiveoperandbinding-value bd)]
    (.write w (str  "#=" `(directives/make-directiveoperand-binding ~directiveoperand ~value)))))



(defrecord 
  AbsoluteIdentifier 
  [start 
   length])

(defrecord
  RelativeIdentifier
  [ownerdata
   property])

(defn
  make-absolute-identifier
  ([node]
    (make-absolute-identifier (.getStartPosition node) (.getLength node)))
  ([start length]
    (AbsoluteIdentifier. start length)))

(defn
  make-relative-identifier
  [owner property]
  (cond 
    (instance? ASTNode owner)
    (RelativeIdentifier. (make-absolute-identifier owner) property)
    (instance? AbsoluteIdentifier owner)
    (RelativeIdentifier. owner property)
    :else
    (throw (Exception. (str "Unknown owner for relative identifier: " owner)))))



(defn
  snippet-value-identifier
  [snippet value]
  (cond 
    (astnode/ast? value)
    (make-absolute-identifier value)
    (or 
      (astnode/lstvalue? value)
      (astnode/nilvalue? value)
      (astnode/primitivevalue? value))
    (let [owner (astnode/owner value)
          property (astnode/owner-property value)]
      (make-relative-identifier owner property))
    :else
    (throw (Exception. (str "Unknown value to create identifier for:" value)))))

(defn
  snippet-value-corresponding-to-identifier
  [snippet identifier]
  (some 
    (fn [value] 
      (let [value-id (snippet-value-identifier snippet value)]
        (when (= value-id identifier)
          value)))
    (snippet/snippet-nodes snippet)))

(defn
  snippet-persistable-directives
  [snippet]
  (reduce
    (fn [sofar value] 
      (let [bounddirectives
            (snippet/snippet-bounddirectives-for-node snippet value)            
            identifier
            (snippet-value-identifier snippet value)]
        (assoc sofar identifier bounddirectives)))
    {}
    (snippet/snippet-nodes snippet)))



(defn
  snippet-from-node-and-persisted-directives
  [node data]
  (let [result 
        (reduce
          (fn [sofar [identifier bounddirectives]]
            (let [value 
                  (snippet-value-corresponding-to-identifier sofar identifier)
          
                  bounddirectives-with-implicit-operand
                  (map
                    (fn [bounddirective]
                      (directives/make-bounddirective
                        (directives/bounddirective-directive bounddirective)
                        (cons 
                          (directives/make-implicit-operand value)
                          (directives/bounddirective-operandbindings bounddirective))))
                    bounddirectives)
                  ]
              (snippet/update-bounddirectives sofar value bounddirectives-with-implicit-operand)))
          (matching/jdt-node-as-snippet node)
          (seq data))]
    result))

    



(defmethod 
  clojure.core/print-dup 
  Snippet
  [snippet w]
  (let [root 
        (snippet/snippet-root snippet)
        directives
        (snippet-persistable-directives snippet)]
  (.write w (str  "#=" `(snippet-from-node-and-persisted-directives 
                          ~root
                          ~directives)))))

(defmethod 
  clojure.core/print-dup 
  SnippetGroup
  [snippetgroup w]
  (let [name
        (snippetgroup/snippetgroup-name snippetgroup)
        snippets
        (snippetgroup/snippetgroup-snippetlist snippetgroup)]
  (.write w (str  "#=" `(snippetgroup/make-snippetgroup 
                          ~name 
                          ~snippets)))))


(defmethod 
  clojure.core/print-dup 
  AbsoluteIdentifier
  [identifier w]
  (let [pos (:start identifier)
        len (:length identifier)]
  (.write w (str  "#=" `(make-absolute-identifier ~pos ~len)))))


(defmethod 
  clojure.core/print-dup 
  RelativeIdentifier
  [identifier w]
  (let [absolute (:ownerdata identifier)
        property (:property identifier)]
  (.write w (str  "#=" `(make-relative-identifier ~absolute ~property)))))


(defmethod 
  clojure.core/print-dup 
  Directive
  [d w]
  (let [name 
        (directives/directive-name d)]
    (.write w (str  "#=" `(matching/registered-directive-for-name ~name)))))


(defmethod 
  clojure.core/print-dup 
  DirectiveOperand
  [d w]
  (let [description 
        (directives/directive-description d)]
    (.write w (str  "#=" `(directives/make-directiveoperand ~description)))))


(defn
  snippet-as-persistent-string
  [snippet]
  (binding [*print-dup* true]
    (pr-str snippet)))

(defn
  snippet-from-persistent-string
  [string]
  (binding [*read-eval* true]
    (read-string string)))



(defn
  spit-snippet
  [filename snippet]
  (binding [*print-dup* true]
    (spit filename (pr-str snippet))))

(defn
  slurp-snippet
  [filename]
  (binding [*read-eval* true]
    (read-string (slurp filename))))


(def
  spit-snippetgroup
  spit-snippet)


(def
  slurp-snippetgroup
  slurp-snippet)


(defn
  register-callbacks
  []
  (set! (damp.ekeko.snippets.gui.TemplateEditorInput/serializeClojureTemplateGroup) spit-snippetgroup)
  (set! (damp.ekeko.snippets.gui.TemplateEditorInput/deserializeClojureTemplateGroup) slurp-snippetgroup)
  )

(register-callbacks)

(comment
  
  (def node (parsing/parse-string-ast "public Integer field;"))
  (def snippet (matching/jdt-node-as-snippet node))
  
  (def pers (snippet-persistable-directives snippet))
  (def newsnippet (snippet-from-node-and-persisted-directives node pers))

  (def serialized
    (binding [*print-dup* true]
      (pr-str snippet)))
  
  (def 
    deserialized
    (binding [*read-eval* true] 
      (read-string serialized)))
  
  deserialized

  (spit-snippetgroup "test.snippetgroup" (snippetgroup/make-snippetgroup "Test" [snippet]))
  
  (slurp-snippetgroup "test.snippetgroup")
  
  
  
  
  )
