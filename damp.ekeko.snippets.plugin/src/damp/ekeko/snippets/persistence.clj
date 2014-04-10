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



;if node:
;find by range

;if list:
;find owner by range
;take property

;if primitive 
;find owner by range
;take property
;unless also list element

;could also record index in list


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
  [node]
  (AbsoluteIdentifier. (.getStartPosition node) (.getLength node)))

(defn
  make-relative-identifier
  [owner property]
  (RelativeIdentifier. (make-absolute-identifier owner) property))



(defn
  snippet-value-identifier
  [snippet value]
  (cond 
    (snippet/snippet-value-node? snippet value)
    (make-absolute-identifier value)
    (or 
      (snippet/snippet-value-list? snippet value)
      (snippet/snippet-value-null? snippet value)
      (snippet/snippet-value-primitive? snippet value))
    (let [owner (snippet/snippet-node-owner snippet value)
          owner-raw (snippet/snippet-value-node-unwrapped snippet owner)
          property (astnode/owner-property value)]
      (make-relative-identifier owner-raw property))))  

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