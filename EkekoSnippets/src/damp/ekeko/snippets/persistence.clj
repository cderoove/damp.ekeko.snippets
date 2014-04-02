(ns 
  damp.ekeko.snippets.persistence
  (:require 
    [damp.ekeko.jdt
     [astnode :as astnode]]
    [damp.ekeko.snippets 
     [util :as util]
     [directives :as directives]
     [snippet :as snippet]
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
  [ownerclass pdid]                
  (some (fn [pd]
          (= pdid
             (astnode/property-descriptor-id pd)))
        (astnode/nodeclass-property-descriptors ownerclass)))


(defmethod
  clojure.core/print-dup 
  StructuralPropertyDescriptor
  [pd w]
  (let [id 
        (astnode/property-descriptor-id pd)
        ownerclass
        (astnode/property-descriptor-owner-node-class pd)]
    (.write w (str  "#=" `(class-propertydescriptor-with-id ~ownerclass ~id)))))

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


(defmethod 
  clojure.core/print-dup 
  Snippet
  [snippet w]
  (.write w (str  "#=" `(matching/jdt-node-as-snippet ~(snippet/snippet-root snippet)))))


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
      (when (= (snippet-value-identifier value)
               identifier)
        value))
    (snippet/snippet-nodes snippet)))

(defn
  snippet-persistable-data
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









(comment
  
  (def node (parsing/parse-string-ast "public Integer field;"))
  (def snippet (matching/jdt-node-as-snippet node))
  
  (snippet-persistable-data snippet)
  
  (def serialized
    (binding [*print-dup* true]
      (pr-str snippet)))
  
  (def 
    deserialized
    (binding [*read-eval* true] 
      (read-string serialized)))
  
  deserialized
  
  
  )