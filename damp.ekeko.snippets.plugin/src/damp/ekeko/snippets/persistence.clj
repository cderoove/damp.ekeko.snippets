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
    [org.eclipse.jdt.core.dom BodyDeclaration Expression Statement ASTNode ASTParser AST CompilationUnit]
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
  snippet-associativeinfo
  ([snippet]
    (reduce 
      (fn [{bdmap :bdmap 
            pidmap :pidmap
            :as sofar} value] 
        (let [bounddirectives
              (snippet/snippet-bounddirectives-for-node snippet value)            
              identifier
              (snippet/snippet-value-identifier snippet value)
              correspondingprojectvalueidentifier
              (snippet/snippet-value-projectanchoridentifier snippet value)]
          (when (contains? sofar identifier)
            (throw (Exception. (str "While serializing snippet, encountered duplicate identifier among snippet values:" value identifier))))
          (when 
            (nil? bounddirectives)
            (throw (Exception. (str "While serializing snippet, encountered invalid bound directives for snippet value:"  bounddirectives value snippet))))
          (when 
            (nil? identifier)
            (throw (Exception. (str "While serializing snippet, encountered invalid identifier for snippet value:" value))))
          (-> 
            sofar
            (assoc-in [:bdmap identifier]  bounddirectives)
            (assoc-in [:pidmap identifier] correspondingprojectvalueidentifier))))
      {;bounddirectives
       :bdmap {} 
       ;projectidentifiers
       :pidmap {} 
       }
      (snippet/snippet-nodes snippet))))
 

(defn
  snippet-persistable-associativeinfo	
  [snippet]
  (snippet-associativeinfo snippet)) ;;print-dup of BoundDirective removes the implicit operand




   
;called from old persisted snippets, for which only the map of bounddirectives was persisted
(defn
  snippet-from-node-and-persisted-directives
  ([node data anchor] 
    (if-let [snippet (snippet-from-node-and-persisted-directives node data)]
      (snippet/update-anchor snippet anchor)))
  ([node data] 
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


;new persistence in which a map of associativeinfo is persisted
(defn
  snippet-from-node-and-persisted-associativeinfo
  [node 
   { bdmap :bdmap ;astid->bounddirectives
    pidmap :pidmap ;astid->correspondingprojectvalueidentifiers
   } 
   anchor]
  (reduce
    (fn [sofar [snippetvalueidentifier projectvalueidentifier]]
      (let [snippetvalue (snippet/snippet-value-corresponding-to-identifier sofar snippetvalueidentifier)]
        (snippet/update-projectanchoridentifier sofar snippetvalue projectvalueidentifier)))
    (snippet-from-node-and-persisted-directives node bdmap anchor)
    (seq pidmap)))
        


(defmethod 
  clojure.core/print-dup 
  Snippet
  [snippet w]
  (let [root 
        (snippet/snippet-root snippet)
        info
        (snippet-persistable-associativeinfo snippet)
        anchor
        (snippet/snippet-anchor snippet)
        ]
    (.write ^Writer w (str  "#=" `(snippet-from-node-and-persisted-associativeinfo 
                                    ~root
                                    ~info
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


(defn-
  remove-implicit-opbindings
  "Removes every implicit operand binding from a snippet-associativeinfo.
   Called when copying a snippet from this info, 
   as slurping a snippet assumes that print-dup on BoundDirective has not spat this implicit operand."
  [{ 
    bdmap :bdmap ;astid->bounddirectives
    pidmap :pidmap ;astid->correspondingprojectvalueidentifiers
    }]
  {
   :pidmap pidmap
   :bdmap 
   (reduce 
     (fn [sofar [astid bounddirectives]]
       (assoc-in 
         sofar 
         [astid]
         (map 
           (fn [bounddirective]
             (directives/make-bounddirective
               (directives/bounddirective-directive bounddirective)
               (rest (directives/bounddirective-operandbindings bounddirective))))
           bounddirectives)))
     bdmap
     (seq bdmap))
   })

(defn 
  copy-snippet
  "Duplicates the given snippet. 
   No mutable data is shared between the original and the copy."
  [snippet]
  ;(let [s (snippet-as-persistent-string snippet)
  ;     copy (snippet-from-persistent-string s)]
  ;copy)
  (let [;bypassing astnode/*ast-for-newlycreatednodes* for performance reasons
       associnfo (remove-implicit-opbindings (snippet-associativeinfo snippet)) ;do not want implicit subject operand of directive bindings
       root (snippet/snippet-root snippet)
       newast (AST/newAST damp.ekeko.JavaProjectModel/JLS) 
       newroot (ASTNode/copySubtree ^AST newast root)
       anchor (snippet/snippet-anchor snippet)]
    (snippet-from-node-and-persisted-associativeinfo newroot associnfo anchor))) 
  
           
   
   
   

(defn
  copy-snippetgroup
  "Duplicates the given snippet group. 
   No data is shared between the original and the copy."
  ;copy-snippet)
  [snippetgroup]
  (snippetgroup/make-snippetgroup (snippetgroup/snippetgroup-name snippetgroup)
                                  (map copy-snippet (snippetgroup/snippetgroup-snippetlist snippetgroup))))
  

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
