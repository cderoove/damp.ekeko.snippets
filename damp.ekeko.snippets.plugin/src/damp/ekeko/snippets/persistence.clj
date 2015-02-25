(ns 
  ^{:doc "Persistence of snippets. Relies heavily on persistence in damp.ekeko.jdt.astnode." 
    :author "Coen De Roover"}
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
 ;;Does not seem to work with print-duped values (see explicit def aliases below)
 ; (:use [damp.ekeko.jdt.astnode
 ;       :only
 ;       [newnode-propertyvalues
 ;        class-propertydescriptor-with-id
 ;        make-property-value-identifier
 ;        make-list-element-identifier
 ;        ]]) ;for future reference: also supports :rename
  (:import 
    [java.io Writer] 
    [java.util List]
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
           [damp.ekeko JavaProjectModel]
           ))

(set! *warn-on-reflection* true)


;; Aliases to functions that from this namespace moved to there, required to support reading in old ekt and ekx files.
;; did not work using :use :only above
(def newnode-propertyvalues astnode/newnode-propertyvalues)
(def class-propertydescriptor-with-id astnode/class-propertydescriptor-with-id)
(def make-property-value-identifier astnode/make-property-value-identifier)
(def make-list-element-identifier astnode/make-list-element-identifier)
(def modifierkeyword-for-flagvalue astnode/modifierkeyword-for-flagvalue)
(def primitivetypecode-for-string astnode/primitivetypecode-for-string)
(def infixexpressionoperator-for-string astnode/infixexpressionoperator-for-string)
(def assignmentoperator-for-string astnode/assignmentoperator-for-string)
(def prefixexpressionoperator-for-string astnode/prefixexpressionoperator-for-string)
(def postfixexpressionoperator-for-string astnode/postfixexpressionoperator-for-string)

(def make-root-identifier snippet/make-root-identifier)

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
    (.write ^Writer w (str  "#=" `(directives/make-bounddirective ~directive ~opbindings-without-implicit-operandbinding)))))

(defmethod 
  clojure.core/print-dup 
  DirectiveOperandBinding
  [bd w]
  (let [directiveoperand
        (directives/directiveoperandbinding-directiveoperand bd) 
        value
        (directives/directiveoperandbinding-value bd)]
    (.write ^Writer w (str  "#=" `(directives/make-directiveoperand-binding ~directiveoperand ~value)))))



(defn
  snippet-persistable-directives
  [snippet]
  (reduce 
    (fn [sofar value] 
      (let [bounddirectives
            (snippet/snippet-bounddirectives-for-node snippet value)            
            identifier
            (snippet/snippet-value-identifier snippet value)]
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
  ([node data anchor] ;current persistence
                      (if-let [snippet (snippet-from-node-and-persisted-directives node data)]
                        (snippet/update-anchor snippet anchor)))
  ([node data] ;pre-anchor persistence
               (let [result 
                     (reduce
                       (fn [sofar [identifier bounddirectives]]
                         (let [value 
                               (snippet/snippet-value-corresponding-to-identifier sofar identifier)
                               
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
                 result)))

    


(defmethod 
  clojure.core/print-dup 
  Snippet
  [snippet w]
  (let [root 
        (snippet/snippet-root snippet)
        directives
        (snippet-persistable-directives snippet)
        anchor
        (snippet/snippet-anchor snippet)
        ]
  (.write ^Writer w (str  "#=" `(snippet-from-node-and-persisted-directives 
                           ~root
                           ~directives
                           ~anchor
                           )))))

(defmethod 
  clojure.core/print-dup 
  SnippetGroup
  [snippetgroup w]
  (let [name
        (snippetgroup/snippetgroup-name snippetgroup)
        snippets
        (snippetgroup/snippetgroup-snippetlist snippetgroup)]
  (.write ^Writer w (str  "#=" `(snippetgroup/make-snippetgroup 
                           ~name 
                           ~snippets)))))



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
    (.write ^Writer w (str  "#=" `(registered-directive-for-name ~name)))))


(defmethod 
  clojure.core/print-dup 
  DirectiveOperand
  [d w]
  (let [description 
        (directives/directive-description d)]
    (.write ^Writer w (str  "#=" `(directives/make-directiveoperand ~description)))))


(defmethod 
  clojure.core/print-dup 
  Transformation
  [t w]
  (let [lhs (transformation/transformation-lhs t)
        rhs (transformation/transformation-rhs t)]
  (.write ^Writer w (str  "#=" `(transformation/make-transformation ~lhs ~rhs)))))



(def 
  snippet-as-persistent-string
  astnode/astnode-as-persistent-string)

(def 
  snippet-from-persistent-string
  astnode/astnode-from-persistent-string)


(defn
  template-string
  [snippet]
  (let [grp (snippetgroup/make-snippetgroup "dummy" [snippet])
        pp  (damp.ekeko.snippets.gui.TemplatePrettyPrinter. 
              (damp.ekeko.snippets.data.TemplateGroup/newFromClojureGroup
                grp))]
    (.prettyPrintSnippet pp snippet)))

(defn
  snippetgroup-string
  [snippetgroup]
  (let [pp  
        (damp.ekeko.snippets.gui.TemplatePrettyPrinter. 
          (damp.ekeko.snippets.data.TemplateGroup/newFromClojureGroup
            snippetgroup))]
    (.prettyPrint pp)))

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
  slurp-from-resource
  [pathrelativetobundle]
  (slurp-snippetgroup (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile pathrelativetobundle)))


(defn
  register-callbacks
  []
  (set! (damp.ekeko.snippets.gui.TemplateEditorInput/FN_SERIALIZE_TEMPLATEGROUP) spit-snippetgroup)
  (set! (damp.ekeko.snippets.gui.TemplateEditorInput/FN_DESERIALIZE_TEMPLATEGROUP) slurp-snippetgroup)
  (set! (damp.ekeko.snippets.gui.TransformationEditor/FN_SERIALIZE_TRANSFORMATION) spit-transformation)
  (set! (damp.ekeko.snippets.gui.TransformationEditor/FN_DESERIALIZE_TRANSFORMATION) slurp-transformation)
  
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_ADD_COPY_OF_SNIPPET_TO_SNIPPETGROUP) snippetgroup-add-copy-of-snippet)
  (set! (damp.ekeko.snippets.data.TemplateGroup/FN_ADD_COPY_OF_SNIPPETGROUP_TO_SNIPPETGROUP) snippetgroup-add-copy-of-snippetgroup)

  
  (set! (damp.ekeko.snippets.gui.TemplatePrettyPrinter/FN_SNIPPET_VALUE_IDENTIFIER) snippet/snippet-value-identifier)
  (set! (damp.ekeko.snippets.gui.TemplateGroupViewer/FN_SNIPPET_VALUE_FOR_IDENTIFIER) snippet/snippet-value-corresponding-to-identifier)
  
  (set! (damp.ekeko.snippets.gui.IntendedResultsEditor/FN_PROJECT_VALUE_IDENTIFIER) astnode/project-value-identifier)
  (set! (damp.ekeko.snippets.gui.IntendedResultsEditor/FN_PROJECT_TUPLE_IDENTIFIER) astnode/project-tuple-identifier)
  
  (set! (damp.ekeko.snippets.gui.IntendedResultsEditor/FN_IDENTIFIER_CORRESPONDING_PROJECT_VALUE) astnode/corresponding-project-value)
  

  
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
  
  
  ;conversion from JLS4 to JLS8 based templates
  (doseq [file (file-seq (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile "/resources/EkekoX-Specifications-DesignPatterns/"))]
    (when 
      (and 
        (not (nil? file))
        (.isFile file))
      (try 
        (let [snippet (damp.ekeko.snippets.persistence/slurp-snippetgroup file)]
          (damp.ekeko.snippets.persistence/spit-snippetgroup  file snippet))
        (catch Exception e (println e)))))
  

  
  
  )
