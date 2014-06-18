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
     [rewriting :as rewriting]
     [transformation :as transformation]
     ])
  (:import [org.eclipse.jdt.core.dom 
            AST
            Expression Statement BodyDeclaration CompilationUnit ImportDeclaration
            ASTNode
            ASTNode$NodeList
            StructuralPropertyDescriptor
            Modifier$ModifierKeyword
            PrimitiveType
            PrimitiveType$Code  
            InfixExpression$Operator
            InfixExpression
            PrefixExpression$Operator
            PrefixExpression
            Assignment$Operator
            Assignment            
            ]
           [damp.ekeko.snippets
            BoundDirective
            DirectiveOperandBinding]
           [damp.ekeko.snippets.snippet
            Snippet]
           [damp.ekeko.snippets.snippetgroup
            SnippetGroup]
           [damp.ekeko.snippets.directives
            Directive DirectiveOperand]
           [damp.ekeko.snippets.transformation 
            Transformation]
           ))

;;TODO: dispatch on node type, call parse-string with correct node type
;(defmethod 
;  clojure.core/print-dup 
;;  ASTNode
;  [node w]
;  (.write w (str "#="
;                 (cond 
;                   (instance? Expression node)
;                   `(parsing/parse-string-expression ~(str node))
;                   (instance? Statement node)
;                   `(parsing/parse-string-statement ~(str node))
;                   (instance? BodyDeclaration node)
;                   `(parsing/parse-string-declaration ~(str node))
;                   (instance? CompilationUnit node)
;                   `(parsing/parse-string-unit ~(str node))
;                   (instance? ImportDeclaration node)
 ;                  `(parsing/parse-string-importdeclaration ~(str node))
;                   :default 
;                   `(parsing/parse-string-ast ~(str node))))))


(def 
  ast-for-newlycreatednodes
  (AST/newAST AST/JLS4))

(defn
  newnode 
  ([ekekokeyword]
    (newnode ast-for-newlycreatednodes ekekokeyword))
  ([ast ekekokeyword]
    (let [nodeclass
          (astnode/class-for-ekeko-keyword ekekokeyword)]
      (.createInstance ast nodeclass))))
    
  
(defn
  set-property!
  [^ASTNode node ^StructuralPropertyDescriptor propertydescriptor value]
  (.setStructuralProperty node propertydescriptor value))
  
(defn
  set-lst-index!
  [^ASTNode$NodeList lst idx value]
  (.set lst idx value))

(defn
  newnode-propertyvalues
  [nodekeyword propertyvalues]
  (let [node (newnode nodekeyword)]
    (doseq [[property value] propertyvalues]
      (if
        (astnode/property-descriptor-list? property)
        (let [lst
              (astnode/node-property-value node property)]
          (loop [col lst
                 idx 0]
            (when (seq col)
              (set-lst-index! lst idx (first col))
              (recur (rest col) (inc idx)))))
        (set-property! node property value)))
    node))

(defmethod 
  clojure.core/print-dup 
  ASTNode
  [node w]
  (let [nodeclass
        (class node)
        nodeclasskeyword
        (astnode/ekeko-keyword-for-class nodeclass)]
    (let [propertyvalues
          (for [property (astnode/node-property-descriptors node)]
            (let [value (astnode/node-property-value node property)]
              [property
               (if
                 (astnode/property-descriptor-list? property)
                 (seq value)
                 value)]))]
      (.write w (str "#="
                     `(newnode-propertyvalues ~nodeclasskeyword ~propertyvalues))))))

(defn
  class-propertydescriptor-with-id
  [ownerclasskeyword pdid]         
  (let [found 
        (some (fn [pd]
                (when (= pdid
                         (astnode/property-descriptor-id pd))
                  pd))
              (astnode/nodeclass-property-descriptors 
                (astnode/class-for-ekeko-keyword ownerclasskeyword)))]
    (if
      (nil? found)
      (throw (Exception. (str "When deserializing, could not find property descriptor: " ownerclasskeyword pdid)))
      found)))

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
  PrimitiveType$Code  
  [node w]
  (let [codestr (.toString node)]
    (.write w (str  "#=" `(PrimitiveType/toCode ~codestr)))))

(defmethod 
  clojure.core/print-dup 
  InfixExpression$Operator
  [node w]
  (let [codestr (.toString node)]
    (.write w (str  "#=" `(InfixExpression$Operator/toOperator ~codestr)))))

(defmethod 
  clojure.core/print-dup 
  Assignment$Operator
  [node w]
  (let [codestr (.toString node)]
    (.write w (str  "#=" `(Assignment$Operator/toOperator ~codestr)))))

(defmethod 
  clojure.core/print-dup 
  PrefixExpression$Operator
  [node w]
  (let [codestr (.toString node)]
    (.write w (str  "#=" `(PrefixExpression$Operator/toOperator ~codestr)))))


(defmethod 
  clojure.core/print-dup 
  BoundDirective
  [bd w]
  (let [directive 
        (directives/bounddirective-directive bd) 
        name
        (directives/directive-name directive)
        opbindings
        (directives/bounddirective-operandbindings bd)
        opbindings-without-implicit-operandbinding
        (rest opbindings)]
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
  RootIdentifier []) 

(defrecord
  RelativePropertyValueIdentifier
  [ownerid
   property])

(defrecord
  RelativeListElementIdentifier
  [listid
   index
   ])

(defn
  make-root-identifier
  []
  (RootIdentifier.))

(defn
  make-property-value-identifier
  [ownerid property]
  (RelativePropertyValueIdentifier. ownerid property))

(defn
  make-list-element-identifier
  [listid index]
  (RelativeListElementIdentifier. listid index))

;memoize?
(defn
  snippet-value-identifier
  [snippet value]
  (let [owner (astnode/owner value) ;owner of list = node, owner of list element = node (never list)
        property (astnode/owner-property value)]
  (cond 
    ;root
    (= value (snippet/snippet-root snippet))
    (make-root-identifier)
    
    ;lists (keep before next clause, do not merge with before-last clause)
    (astnode/lstvalue? value)
    (make-property-value-identifier 
      (snippet-value-identifier snippet owner)
      property)
    
    ;list members
    (astnode/property-descriptor-list? property)
    (let [lst (snippet/snippet-list-containing snippet value)
          lst-raw (astnode/value-unwrapped lst)]
      (make-list-element-identifier 
        (snippet-value-identifier 
          snippet
          lst)
        (.indexOf lst-raw value)))
    
    ;non-list members
    (or 
      (astnode/ast? value)
      (astnode/nilvalue? value)
      (astnode/primitivevalue? value))
    (make-property-value-identifier
      (snippet-value-identifier snippet owner)
      property)
    
    :else
    (throw (Exception. (str "Unknown value to create identifier for:" value))))))

(defn
  snippet-value-corresponding-to-identifier
  [snippet identifier]
  (let [found 
        (some 
          (fn [value] 
            (let [value-id (snippet-value-identifier snippet value)]
              (when (= value-id identifier)
                value)))
          (snippet/snippet-nodes snippet))]
    (if
      (nil? found)
      (throw (Exception. (str "While deserializing snippet, could not locate node for identifier in snippet:" identifier snippet)))
      found)))  

(defn
  snippet-persistable-directives
  [snippet]
  (reduce 
    (fn [sofar value] 
      (let [bounddirectives
            (snippet/snippet-bounddirectives-for-node snippet value)            
            identifier
            (snippet-value-identifier snippet value)]
        (when (contains? sofar identifier)
          (throw (Exception. (str "While serializing snippet, encountered duplicate identifier among snippet values:" value identifier))))
        (when 
          (nil? bounddirectives)
          (throw (Exception. (str "While serializing snippet, encountered invalid bound directives for snippet value:"  bounddirectives value snippet))))
        (when 
          (nil? identifier)
          (throw (Exception. (str "While serializing snippet, encountered invalid identifier for snippet value:" value))))
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
  RootIdentifier
  [identifier w]
  (.write w (str  "#=" `(make-root-identifier))))


(defmethod 
  clojure.core/print-dup 
  RelativePropertyValueIdentifier
  [identifier w]
  (let [ownerid (:ownerid identifier)
        property (:property identifier)]
  (.write w (str  "#=" `(make-property-value-identifier ~ownerid ~property)))))


(defmethod 
  clojure.core/print-dup 
  RelativeListElementIdentifier
  [identifier w]
  (let [listid (:listid identifier)
        index (:index identifier)]
  (.write w (str  "#=" `(make-list-element-identifier ~listid ~index)))))


(defn
  registered-directive-for-name
  [name]
  (if-let
    [matching-directive (matching/registered-directive-for-name name)]
    matching-directive
    (if-let 
      [rewriting-directive (rewriting/registered-directive-for-name name)]
      rewriting-directive
      (throw (Exception. (str "Could not find a matching, nor a rewriting directive for the given name: " name))))))
    

(defmethod 
  clojure.core/print-dup 
  Directive
  [d w]
  (let [name 
        (directives/directive-name d)]
    (.write w (str  "#=" `(registered-directive-for-name ~name)))))


(defmethod 
  clojure.core/print-dup 
  DirectiveOperand
  [d w]
  (let [description 
        (directives/directive-description d)]
    (.write w (str  "#=" `(directives/make-directiveoperand ~description)))))


(defmethod 
  clojure.core/print-dup 
  Transformation
  [t w]
  (let [lhs (transformation/transformation-lhs t)
        rhs (transformation/transformation-rhs t)]
  (.write w (str  "#=" `(transformation/make-transformation ~lhs ~rhs)))))


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
  template-string
  [snippet]
  (let [grp (snippetgroup/make-snippetgroup "dummy" [snippet])
        pp  (damp.ekeko.snippets.gui.TemplatePrettyPrinter. 
              (damp.ekeko.snippets.data.TemplateGroup/newFromClojureGroup
                grp))]
    (.prettyPrintSnippet pp snippet)))


(defn 
  copy-snippet
  "Duplicates the given snippet. 
   No data is shared between the original and the copy."
  [snippet]
  (let [s (snippet-as-persistent-string snippet)
        copy (snippet-from-persistent-string s)]
    copy))
  


(def
  copy-snippetgroup
  "Duplicates the given snippet group. 
   No data is shared between the original and the copy."
  copy-snippet)
  

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


(def
  slurp-transformation
  slurp-snippet)

(defn
  spit-transformation
  [filename transformation]
  (spit-snippet filename transformation))
 
(defn
  snippetgroup-add-copy-of-snippet
  [snippetgroup snippet]
  (snippetgroup/add-snippet snippetgroup (copy-snippet snippet)))

(defn
  snippetgroup-add-copy-of-snippetgroup
  [snippetgroup tobecopied]
  (reduce 
    (fn [snippetgroupsofar snippet]
      (snippetgroup/add-snippet snippetgroupsofar (copy-snippet snippet)))
    snippetgroup
    (snippetgroup/snippetgroup-snippetlist tobecopied)))

(defn
  register-callbacks
  []
  (set! (damp.ekeko.snippets.gui.TemplateEditorInput/FN_SERIALIZE_TEMPLATEGROUP) spit-snippetgroup)
  (set! (damp.ekeko.snippets.gui.TemplateEditorInput/FN_DESERIALIZE_TEMPLATEGROUP) slurp-snippetgroup)
  (set! (damp.ekeko.snippets.gui.TransformationEditor/FN_SERIALIZE_TRANSFORMATION) spit-transformation)
  (set! (damp.ekeko.snippets.gui.TransformationEditor/FN_DESERIALIZE_TRANSFORMATION) slurp-transformation)
  
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_ADD_COPY_OF_SNIPPET_TO_SNIPPETGROUP) snippetgroup-add-copy-of-snippet)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_ADD_COPY_OF_SNIPPETGROUP_TO_SNIPPETGROUP) snippetgroup-add-copy-of-snippetgroup)

  
  
  
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
