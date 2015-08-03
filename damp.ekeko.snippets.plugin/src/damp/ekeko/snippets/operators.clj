(ns 
  ^{:doc "Operators for generalizing and refining snippets."
    :author "Coen De Roover, Siltvani"}
damp.ekeko.snippets.operators
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko])
  (:require [damp.ekeko.snippets 
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [matching :as matching]
             [directives :as directives]
             [rewriting :as rewriting]
             [util :as util]
             [parsing :as parsing]])
  (:require 
    [damp.ekeko.jdt
     [astnode :as astnode]
     [ast :as ast]])
  (:import
    [org.eclipse.jdt.core.dom ASTNode Comment Statement MethodDeclaration]
    [org.eclipse.jdt.core.dom.rewrite ASTRewrite]))

;; Operators for Snippet
;; ---------------------

;remove-all-directives+ (on node and all children)
;remove-directives: constraining (fron node itsel_
;add-constraining directive: ignore, ignore-variable, maybe enought to have an entry in ast2uservar, is ast2uservar still needed? 
;could just use operandbindings of directive instead

(defn
  make-directiveoperand-for-match
  []
  (directives/make-directiveoperand "Match for template node"))

(defn
  make-directiveoperandbinding-for-match
  [node]
  (directives/make-directiveoperand-binding
    (make-directiveoperand-for-match)
    node))

;note: cannot physically delete the node's children, as some might be mandatory which is checked by the AST delete operations
;could replace mandatories by newly created nodes (of which missing information will show up as MISSNG when printed),
;but there is no benefit over keeping the original nodes
(defn 
  replace-by-variable 
  "Replace snippet AST node by a logic variable."
  [snippet node uservar]
  (let [purged-of-children ;remove all directives for children
        (matching/remove-all-directives+ snippet node)
        purged 
        ;remove incompatible constraining directives for node, keep grounding directives
        (matching/remove-directives 
          purged-of-children
          node 
          matching/directives-constraining|mutuallyexclusive)]                          
    (snippet/add-bounddirective
      purged
      node 
      (directives/make-bounddirective 
        matching/directive-replacedbyvariable
        [(make-directiveoperandbinding-for-match node)
         (directives/make-directiveoperand-binding
           (directives/make-directiveoperand "Variable replacing template node")
           uservar)]))))


(defn
  replace-by-exp
  "Add directive replacedby-exp to snippet primitive value"
  [snippet value expstring]
  (snippet/add-bounddirective
      (matching/remove-directives
        snippet
        value
        matching/directives-constraining|mutuallyexclusive)
      value 
      (directives/make-bounddirective 
        matching/directive-replacedbyexp
        [(make-directiveoperandbinding-for-match value)
         (directives/make-directiveoperand-binding
           (directives/make-directiveoperand "Expression")
            expstring)]
        )))

(defn replace-by-wildcard 
  "Replace snippet AST node by a wildcard."
  [snippet node]
  (let [purged-of-children
        (matching/remove-all-directives+ snippet node)
        purged 
        (matching/remove-directives
          purged-of-children
          node
           matching/directives-constraining|mutuallyexclusive)]
    (snippet/add-bounddirective
      purged
      node 
      (directives/make-bounddirective 
        matching/directive-replacedbywildcard
        [(make-directiveoperandbinding-for-match node)]))))

(defn replace-by-checked-wildcard 
  "Replace snippet AST node by a wildcard."
  [snippet node]
  (snippet/add-bounddirective
    (matching/remove-directive
      (replace-by-wildcard snippet node)
      matching/directive-replacedbywildcard
      node)
    node
    (directives/make-bounddirective 
      matching/directive-replacedbywildcard-checked
      [(make-directiveoperandbinding-for-match node)])))

(defn
  add-unary-directive-opname-opvalue
  [snippet node directive opname opvalue]
  (snippet/add-bounddirective snippet
                              node 
                              (directives/make-bounddirective 
                                directive 
                                [(make-directiveoperandbinding-for-match node)
                                 (directives/make-directiveoperand-binding
                                   (directives/make-directiveoperand opname)
                                   opvalue)])))



(defn 
  add-directive-equals 
  "Adds directive-equals to node."
  [snippet node uservar]
  (add-unary-directive-opname-opvalue snippet node matching/directive-equals "Meta-variable" uservar))


(defn 
  add-directive-invokes
  "Adds directive-invokes to node."
  [snippet node uservar]
  (add-unary-directive-opname-opvalue snippet node matching/directive-invokes "Meta-variable" uservar))

(defn 
  add-directive-invokedby
  "Adds directive-invokedby to node."
  [snippet node uservar]
  (add-unary-directive-opname-opvalue snippet node matching/directive-invokedby "Meta-variable" uservar))


(defn 
  add-directive-constructs
  "Adds directive-constructs to node."
  [snippet node uservar]
  (add-unary-directive-opname-opvalue snippet node matching/directive-constructs "Meta-variable" uservar))

(defn 
  add-directive-constructedby
  "Adds directive-constructedby to node."
  [snippet node uservar]
  (add-unary-directive-opname-opvalue snippet node matching/directive-constructedby "Meta-variable" uservar))

(defn 
  add-directive-overrides
  "Adds directive-overrides to node."
  [snippet node uservar]
  (add-unary-directive-opname-opvalue snippet node matching/directive-overrides "Meta-variable" uservar))

(defn 
  add-directive-refersto
  "Adds directive-refersto to node."
  [snippet node uservar]
  (add-unary-directive-opname-opvalue snippet node matching/directive-refersto "Meta-variable" uservar))

(defn 
  add-directive-referredby
  "Adds directive-referredby to node."
  [snippet node uservar]
  (add-unary-directive-opname-opvalue snippet node matching/directive-referredby "Meta-variable" uservar))

(defn 
  add-directive-type
  "Adds directive-type to node."
  [snippet node uservar]
  (add-unary-directive-opname-opvalue snippet node matching/directive-type "Meta-variable" uservar))

(defn 
  add-directive-type|qname
  "Adds directive-type|qname to node."
  [snippet node qnamestring]
  (add-unary-directive-opname-opvalue snippet node matching/directive-type|qname "Qualified name" qnamestring))

(defn 
  add-directive-type|sname
  "Adds directive-type|sname to node."
  [snippet node snamestring]
  (add-unary-directive-opname-opvalue snippet node matching/directive-type|sname "Simple name" snamestring))

(defn 
  add-directive-subtype+
  "Adds directive-subtype+ to node."
  [snippet node uservar]
  (add-unary-directive-opname-opvalue snippet node matching/directive-subtype+ "Meta-variable" uservar))


(defn 
  add-directive-subtype+|qname
  "Adds directive-subtype+|qname to node."
  [snippet node uservar]
  (add-unary-directive-opname-opvalue snippet node matching/directive-subtype+|qname "Qualified name" uservar))


(defn 
  add-directive-subtype+|sname
  "Adds directive-subtype+|sname to node."
  [snippet node uservar]
  (add-unary-directive-opname-opvalue snippet node matching/directive-subtype+|sname "Simple name" uservar))


(defn 
  add-directive-subtype*
  "Adds directive-subtype* to node."
  [snippet node uservar]
  (add-unary-directive-opname-opvalue snippet node matching/directive-subtype* "Meta-variable" uservar))


(defn 
  add-directive-subtype*|qname
  "Adds directive-subtype*|qname to node."
  [snippet node uservar]
  (add-unary-directive-opname-opvalue snippet node matching/directive-subtype*|qname "Qualified name" uservar))


(defn 
  add-directive-subtype*|sname
  "Adds directive-subtype*|sname to node."
  [snippet node uservar]
  (add-unary-directive-opname-opvalue snippet node matching/directive-subtype*|sname "Simple name" uservar))


(defn
  add-directive-orimplicit
  "Adds directive-orimplicit to node."
  [snippet node]
  (snippet/add-bounddirective 
    (matching/remove-directives snippet node matching/directives-match)
    node
    (directives/make-bounddirective matching/directive-orimplicit
                                    [(make-directiveoperandbinding-for-match node)])))


(defn
  add-directive-orsimple
  "Adds directive-orsimple to node."
  [snippet node]
  (snippet/add-bounddirective 
    (matching/remove-directives snippet node matching/directives-match)
    node
    (directives/make-bounddirective matching/directive-orsimple
                                    [(make-directiveoperandbinding-for-match node)])))

(defn
  add-directive-orexpression
  "Adds directive-orexpression to node."
  [snippet node]
  (snippet/add-bounddirective 
    (matching/remove-directives snippet node (conj matching/directives-match (matching/registered-grounding-directives)))
    node
    (directives/make-bounddirective matching/directive-orexpression
                                    [(make-directiveoperandbinding-for-match node)])))

(defn
  relax-size-to-atleast
  "Uses match-size|atleast/0 for constraining match candidates for a template list."
  [template lst]
  (snippet/add-bounddirective 
    (matching/remove-directives template lst [matching/directive-exact])
    lst
    (directives/make-bounddirective matching/directive-size|atleast
                                    [(make-directiveoperandbinding-for-match lst)])))
  
(defn
  empty-body
  [template lst]
  (snippet/add-bounddirective 
    (matching/remove-directives template lst [matching/directive-exact])
    lst
    (directives/make-bounddirective matching/directive-emptybody
                                    [(make-directiveoperandbinding-for-match lst)])))

(defn
  or-block
  [template value]
  (snippet/add-bounddirective 
    (matching/remove-directives 
      template value (matching/registered-grounding-directives))
    value
    (directives/make-bounddirective matching/directive-orblock
                                    [(make-directiveoperandbinding-for-match value)])))

(defn
  relax-scope-to-member
  "Uses member/0 for grounding of list members."
  [template value]
  (snippet/add-bounddirective 
    (matching/remove-directives template value (matching/registered-grounding-directives))
    value
    (directives/make-bounddirective matching/directive-member
                                    [(make-directiveoperandbinding-for-match value)])))

(defn
  relax-scope-to-child+
  "Uses child+/0 for grounding."
  [template value]
  (snippet/add-bounddirective 
    (matching/remove-directives 
      template
      value 
      (matching/registered-grounding-directives))
    value
    (directives/make-bounddirective matching/directive-child+
                                    [(make-directiveoperandbinding-for-match value)])))

(defn
  relax-scope-to-child*
  "Uses child*/0 for grounding."
  [template value]
  (snippet/add-bounddirective 
    (matching/remove-directives 
      template
      value 
      (matching/registered-grounding-directives))
    value
    (directives/make-bounddirective matching/directive-child*
                                    [(make-directiveoperandbinding-for-match value)])))

(defn
  restrict-scope-to-child
  "Uses child/0 for grounding."
  [template value]
  (snippet/add-bounddirective 
    (matching/remove-directives template value (matching/registered-grounding-directives))
    value
    (directives/make-bounddirective matching/directive-child
                                    [(make-directiveoperandbinding-for-match value)])))


  

(defn
  add-unary-directive-opname-opvalue|rewriting
  [snippet subject directive uservar]
  (let [root (snippet/snippet-root snippet)]
    ;(when-not (= root subjectshouldberoot)
    ;  (throw (IllegalArgumentException. "Rewriting operators are only valid for template roots.")))
    (add-unary-directive-opname-opvalue snippet subject directive "Rewrite target" uservar)))
    


    
(defn 
  add-directive-replace
  [snippet subject uservar]
  (add-unary-directive-opname-opvalue|rewriting snippet subject rewriting/directive-replace uservar))        

(defn 
  add-directive-replace-value
  [snippet subject uservar]
  (add-unary-directive-opname-opvalue|rewriting snippet subject rewriting/directive-replace-value uservar))        
    
(defn 
  add-directive-add-element
  [snippet subject uservar]
  (add-unary-directive-opname-opvalue|rewriting snippet subject rewriting/directive-add-element uservar))

(defn 
  add-directive-remove-element
  [snippet subject uservar]
  (add-unary-directive-opname-opvalue|rewriting snippet subject rewriting/directive-remove-element uservar))
  

(defn
  generalize-directive
  "Generalize a directive (e.g. convert an existing 'type' directive to 'type*')"
  [snippet node directive-name]
  (let [bds
        (snippet/snippet-bounddirectives-for-node snippet node)
        
        process-directive
        (fn [cur-directive-name bd new-bd]
          (let [bdops (.getOperandBindings bd)]
            (if (= cur-directive-name directive-name)
             (directives/make-bounddirective new-bd bdops)
             bd)))]
    (if (and (= directive-name "child") (not (astnode/lstvalue? node)))
      (relax-scope-to-child* snippet node)
      (snippet/update-bounddirectives 
        snippet node
        (for [bd bds]
          (let [bdname (directives/directive-name (directives/bounddirective-directive bd))] 
            (case bdname
              "type" (process-directive bdname bd matching/directive-subtype*)
              "type|sname" (process-directive bdname bd matching/directive-subtype*|sname)
              "type|qname" (process-directive bdname bd matching/directive-subtype*|qname)
              bd)))))))

(defn
  remove-directive
  [snippet node directive-name]
  (let [new-bds
        (remove (fn [bd] (= directive-name 
                            (directives/directive-name (directives/bounddirective-directive bd))))
                (snippet/snippet-bounddirectives-for-node snippet node))
        new-snippet (snippet/update-bounddirectives snippet node new-bds)]
    (cond
      ; If we're removing a grounding directive, we should put back a child directive
      (some #(= directive-name %) (map directives/directive-name (matching/registered-grounding-directives)))
      (restrict-scope-to-child new-snippet node)
      
      ; If we're removing a directive which had previously removed grounding directives from its list elements,
      ; then we should add a child directive to each element
      (some #(= directive-name %) (map directives/directive-name (matching/registered-nongrounding-list-directives)))
      (reduce
        (fn [cur-snippet lstel] (restrict-scope-to-child cur-snippet lstel))
        (snippet/add-bounddirective 
          new-snippet node
          (directives/make-bounddirective matching/directive-exact [(make-directiveoperandbinding-for-match node)]))
        (astnode/value-unwrapped node))
      
      :else 
      new-snippet)))

(defn
  remove-node
  "Removes node from snippet. "
  [snippet node]
  (let [newsnippet 
        (atom snippet)
        conceptualparent 
        (snippet/snippet-node-parent|conceptually snippet node)
        ownerproperty 
        (astnode/owner-property node)
        ]
    (do 
      (snippet/walk-snippet-element ;dissoc children 
                                    snippet
                                    node 
                                    (fn [val] 
                                      (swap! newsnippet matching/remove-value-from-snippet val)))
      (.delete node) ;remove node
      (when (astnode/ast? conceptualparent) ;;will now have nil where node used to be
        (let [newpropertyval (astnode/node-property-value|reified conceptualparent ownerproperty)]
          (swap! newsnippet matching/add-value-to-snippet newpropertyval)))
      )   
    @newsnippet))

(defn
  insert-at
  "Inserts node in list from snippet at the given index."
  [snippet node lst idx]
  (let [newsnippet 
        (atom snippet)]
    (do 
      (util/walk-jdt-node
        ;add cvalue hildren 
        node 
        (fn [val] 
          (swap! newsnippet matching/add-value-to-snippet val)))
      (.add lst idx node)) ;destructive add
    @newsnippet))



(defn-
  newnode|classkeyword
  [ast classkeyw]
  (let [clazz (astnode/class-for-ekeko-keyword classkeyw)]
    (.createInstance ast clazz)))


(defn
  modifierkeyword-from-string
  [string]
  (let [trimmed (clojure.string/trim string)]
    (some 
      (fn [keyword]
          (when (= trimmed (.toString keyword))
            keyword))
      parsing/jdt-modifier-keywords)))

(defn-
  newnode|string
  [string ast classkeyword]
  (let [clazz (astnode/class-for-ekeko-keyword classkeyword)] ;todo: parsing also supports multiple declarations and statements
    (cond 
      (.isAssignableFrom org.eclipse.jdt.core.dom.Statement clazz)
      (parsing/parse-string-statement string)
      
      (.isAssignableFrom org.eclipse.jdt.core.dom.Expression clazz)
      (parsing/parse-string-expression string)
      
      (.isAssignableFrom org.eclipse.jdt.core.dom.BodyDeclaration clazz)
      (parsing/parse-string-declaration string)
      
      (= org.eclipse.jdt.core.dom.Modifier clazz)
      (.newModifier
        ast 
        (modifierkeyword-from-string string))
      
      :default 
      (throw (IllegalArgumentException. (str "Cannot create node from string " string " compatible with " classkeyword))))))


          
(defn-
  insert-newnode-relative
  "Instantiates new node and inserts it relative to the given list element."
  [snippet relativenode classkeyw relativeindexf nodecreatorf]
  (let [lst (snippet/snippet-list-containing snippet relativenode)
        lst-raw (astnode/value-unwrapped lst)
        idx (.indexOf lst-raw relativenode)]
    (let [bds-before 
          (snippet/snippet-bounddirectives-for-node snippet lst)
          result (insert-at snippet  (nodecreatorf (.getAST relativenode) classkeyw) lst-raw (relativeindexf idx))]
      result)))

(defn
  insert-newnodefromclasskeyw-before
  "Instantiates new node and inserts it before the given list element."
  [snippet beforenode classkeyw]
  (insert-newnode-relative snippet beforenode classkeyw identity newnode|classkeyword))

(defn
  insert-newnodefromclasskeyw-after
  "Instantiates new node and inserts it after the given list element."
  [snippet afternode classkeyw]
  (insert-newnode-relative snippet afternode classkeyw inc newnode|classkeyword))

(defn
  insert-newnodefromstring-before
  "Instantiates new node and inserts it before the given list element."
  [snippet beforenode classkeyw string]
  (insert-newnode-relative snippet beforenode classkeyw identity (partial newnode|string string)))

(defn
  insert-newnodefromstring-after
  "Instantiates new node and inserts it after the given list element."
  [snippet afternode classkeyw string]
  (insert-newnode-relative snippet afternode classkeyw inc (partial newnode|string string)))


(defn
  insert-newnodefromclasskeyw-atindex
  "Instantiates new node and inserts it at the given index."
  [snippet lst classkeyw idx]
  (let [lst-raw (astnode/value-unwrapped lst)
        a (.getAST (snippet/snippet-root snippet))
        newnode (newnode|classkeyword a classkeyw)]
    (insert-at snippet newnode lst-raw idx)))  


;only does the jdt work, nothing else to snippet, only to be used for side effects on root astnode  
(defn
  snippet-jdtvalue-replace
  [snippet value newvalue]
  (when-not (or (astnode/nilvalue? value)
                (astnode/primitivevalue? value))
    (throw (IllegalArgumentException. (str "Can only replace wrappers for JDT nil or primitive values, given: " value))))
  (let [property
        (astnode/owner-property value)
        parent
        (astnode/owner value)]
    (.setStructuralProperty parent property newvalue)))

;only does the jdt work, nothing else to snippet, only to be used for side effects on root astnode  
(defn
  snippet-jdtlist-replace
  [snippet lstval newrawlst]
  (when-not (astnode/lstvalue? lstval)
    (throw (IllegalArgumentException. (str "Can only replace wrapper for JDT ASTNode$NodeList, given: " lstval))))
  (when-not (instance? java.util.List newrawlst)
    (throw (IllegalArgumentException. (str "Wrapper for JDT ASTNode$NodeList can only be replaced by an unwrapped ASTNode$NodeList, given: " newrawlst))))
  (let [lst (astnode/value-unwrapped lstval)]
    (.clear lst)
    (.addAll lst newrawlst)))

;only does the jdt work, nothing else to snippet, only to be used for side effects on root astnode  
(defn
  snippet-jdt-replace
  [snippet value newnode]
  (when-not (astnode/ast? value)
    (throw (IllegalArgumentException. (str "Can only replace JDT ASTNode, given: " value))))
  (when-not (astnode/ast? newnode)
    (throw (IllegalArgumentException. (str "JDT ASTNode can only be replaced by another JDT ASTNode, given: " newnode))))
  (let [property (astnode/owner-property value)]
    (cond 
      ;special case that can only occur when instantiating a snippet .. or when using the replace-parent operator
      (= value (snippet/snippet-root snippet))
      (throw (IllegalArgumentException. (str "Still to be implemented, replacing root node of snippet by creating new snippet: " value)))
      ; Can't do this as a side-effect.. All uses of snippet-jdt-replace would have to be refactored, as it would now return a new snippet
      
      (astnode/property-descriptor-child? property)
      (let [parent (astnode/owner value)]
        (.setStructuralProperty parent property newnode))
      (astnode/property-descriptor-list? property)
      (let [lst (snippet/snippet-list-containing snippet value)
            lst-raw (astnode/value-unwrapped lst)
            idx (.indexOf lst-raw value)]
        (.set lst-raw idx newnode))
      :default
      (throw (IllegalArgumentException. "Unexpected property descriptor.")))))

(defn
  replace-node
  "Replaces subject by new instance of the given classkeyw."
  [snippet value classkeyw]
  (let [newnode 
        (newnode|classkeyword (.getAST value) classkeyw)]
    (let [newsnippet 
          (atom snippet)] 
      ;dissoc value and its children 
      (snippet/walk-snippet-element
        snippet
        value 
        (fn [val] (swap! newsnippet matching/remove-value-from-snippet val)))
      ;perform replace (jdt side effects only)
      (snippet-jdt-replace  
        @newsnippet
        value 
        newnode)
      ;assoc node and children
      (util/walk-jdt-node 
        newnode 
        (fn [val] (swap! newsnippet matching/add-value-to-snippet val)))
      @newsnippet)))

(defn
  replace-node-with
  "Replaces a node within a snippet with another node from another snippet
   Returns a pair with first the new snippet, 
   and second the node that was replaced (in case you need to refer to this node in any subsequent operations; you can't use source-node for this)"
  [destination-snippet destination-node source-snippet source-node]
  (let [copy-of-source-node
        (ASTNode/copySubtree (.getAST destination-node) source-node) ;copy to ensure ASTs are compatible
        newsnippet (atom destination-snippet)] 
    ; dissoc destination-node and children in destination-snippet
    (snippet/walk-snippet-element
      destination-snippet 
      destination-node
      (fn [val] (swap! newsnippet matching/remove-value-from-snippet val)))
    
    ; do replacement of destination-node by source-node in the actual AST
    (snippet-jdt-replace  
      @newsnippet
      destination-node
      copy-of-source-node)
    
    ;assoc copy-of-source-node and children with default directives in destination-snippet
    (util/walk-jdt-node 
      copy-of-source-node
      (fn [val] (swap! newsnippet matching/add-value-to-snippet val)))
    
    ;update new-node and children with 
    (snippet/walk-snippets-elements
      @newsnippet
      copy-of-source-node
      source-snippet
      source-node
      (fn [[destval srcval]] 
        (let [srcbds
              (snippet/snippet-bounddirectives-for-node source-snippet srcval) 
              srcpid
              (snippet/snippet-value-projectanchoridentifier source-snippet srcval)
              destbds
              (map
                (fn [bounddirective]
	                 (directives/make-bounddirective
                    (directives/bounddirective-directive bounddirective)
                    (cons 
                      (directives/make-implicit-operand destval)
                      (rest (directives/bounddirective-operandbindings bounddirective)))))
                   srcbds)]
          (swap! newsnippet snippet/update-bounddirectives  destval destbds)
          (swap! newsnippet snippet/update-projectanchoridentifier destval srcpid)
          )))
    
    [@newsnippet copy-of-source-node]))


(defn
  replace-parent
  "Make an expression node replace its parent."
  [snippet node]
  (let [parent-node (snippet/snippet-node-parent|conceptually snippet node)]
    (first (replace-node-with snippet parent-node snippet node))))

(defn
  replace-parent-stmt
  "Make a statement replace its parent statement."
  [snippet node]
  (let [block (snippet/snippet-node-parent|conceptually snippet
                (snippet/snippet-node-parent|conceptually snippet node))
        block-parent (snippet/snippet-node-parent|conceptually snippet block)]
    (if (instance? Statement block-parent)
      (first (replace-node-with snippet block-parent snippet node))
      (first (replace-node-with snippet block snippet node)))))

(defn-
  withoutimmediateparents
  "Removes the parents of the elements in the given seq of nodes."
  [snippet nodes]
  (let [nodesset (set nodes)]
    (reduce 
      (fn [sofar node]
        (let [childrenset (set (snippet/snippet-node-children|conceptually snippet node))]
          (if 
            (empty? (clojure.set/intersection nodesset childrenset))
            (conj sofar node)
            sofar)))
      []
      nodes)))


  
;todo: also for var references rather than simply their declarations
  


(defn
  generalize-references
  "Generalizes all references to given variable declaration (name) node in the snippetgroup."
  [snippetgroup snippet node]
  (let [vardecnodename 
        (if 
          (= :SimpleName (astnode/ekeko-keyword-for-class-of node))
          node
          (.getName node))] ;to push replacement to lowest-level node (in line with other generalize-* operators)
    (letfn [(addreferences [template bindingtoresolveto referredvar]
              (let [resolvingnodes
                    (snippet/snippet-children-resolvingto 
                      template 
                      (snippet/snippet-root template) 
                      (fn [nodebinding]
                        (.isEqualTo bindingtoresolveto nodebinding)))
                    lowestlevelresolvingnodes
                    (withoutimmediateparents template resolvingnodes)]
                [;new template
                 (reduce 
                   (fn [snippetsofar resolvingnode]
                     (if 
                       (= vardecnodename resolvingnode)
                       ;vardec parent of simplename
                       (replace-by-variable 
                         snippetsofar
                         resolvingnode
                         referredvar)
                       (add-directive-refersto 
                         (replace-by-wildcard snippetsofar resolvingnode)
                         resolvingnode referredvar)))
                   template
                   lowestlevelresolvingnodes)
                 ;number of references in template
                 (count lowestlevelresolvingnodes)]))]
      (if-let [binding (snippet/snippet-node-resolvedbinding snippet node)]
        (when 
          (astnode/binding-variable? binding)
          (let [referredvar
                (util/gen-lvar "vardec")
                newtemplatesandcounts 
                (map (fn [snippet] (addreferences snippet binding referredvar))
                     (snippetgroup/snippetgroup-snippetlist snippetgroup))
                counts
                (map (fn [[t cnt]] cnt) newtemplatesandcounts)]
            (when (> (apply + counts) 1) 
              (snippetgroup/snippetgroup-update-snippetlist snippetgroup (map first newtemplatesandcounts)))))))))



(defn-
  generic-generalize-types 
  [snippetgroup snippet node snippetnodesreducerf]
  (letfn [(addtypes [template bindingtoresolveto]
            (let [resolvingnodes
                  (snippet/snippet-children-resolvingto 
                    template 
                    (snippet/snippet-root template) 
                    (fn [nodebinding]
                      (.isEqualTo bindingtoresolveto nodebinding)))
                  ;strategy: always apply generalization to lowest-level nodes only
                  ;this way: entire type/typedeclaration won't be replaced by ... , but its name will
                  lowestlevelresolvingnodes
                  (withoutimmediateparents template resolvingnodes)]
              [(reduce snippetnodesreducerf template lowestlevelresolvingnodes)
               (count lowestlevelresolvingnodes)]))]
    (if-let [binding (snippet/snippet-node-resolvedbinding snippet node)]
      (when (astnode/binding-type? binding)
        (let [newtemplatesandcounts 
              (map (fn [snippet] (addtypes snippet binding))
                   (snippetgroup/snippetgroup-snippetlist snippetgroup))
              counts
              (map (fn [[t cnt]] cnt) newtemplatesandcounts)]
          (when (> (apply + counts) 1)
            (snippetgroup/snippetgroup-update-snippetlist snippetgroup (map first newtemplatesandcounts))))))))

          
(defn 
  generalize-types 
  [snippetgroup snippet node]
  "Generalizes all references in the snippetgroup that refer to the same type as the given type reference."
  (let [typevar (util/gen-lvar "type")]
    (generic-generalize-types snippetgroup snippet node
                              (fn [snippetsofar resolvingnode]
                                (add-directive-type
                                  (replace-by-wildcard snippetsofar resolvingnode)  
                                  resolvingnode 
                                  typevar)))))

(defn 
  generalize-types|qname 
  [snippetgroup snippet node]
  "Generalizes all references in the snippetgroup that refer to the same type as the given type reference."
  (if-let [binding (snippet/snippet-node-resolvedbinding snippet node)]
    (when (astnode/binding-type? binding)
      ;getting name through IType rather than IBinding to mimick matching process
      (if-let [itype (.getJavaElement binding)] 
        (let[qnamestring (.getFullyQualifiedName itype)
             typevar (util/gen-lvar "type")]
          (generic-generalize-types snippetgroup snippet node
                                    (fn [snippetsofar resolvingnode] ;only lowest-level resolving nodes
                                      (let [newsnippet
                                            (add-directive-type
                                              (replace-by-wildcard snippetsofar resolvingnode)  
                                              resolvingnode 
                                              typevar)]
                                        (if 
                                          (or (= node resolvingnode)
                                              ;otherwise directive-type|qname can be added to parent of the one to which type was added before 
                                              (= node (snippet/snippet-node-parent|conceptually snippet resolvingnode))) 
                                          (add-directive-type|qname newsnippet resolvingnode qnamestring)
                                          newsnippet)))))))))

;;mogelijke varianten voor in de toekomst
;;1ste: overal type* ipv type inserten, vraag: object zou niet teruggeven mogen worden door type*?, nut? type->type* kan ook atomisch gebeuren
;;2de: resolvingnodes hebben gemeenschappelijk supertype, vooral belangrijk als je er meteen een qname bijzet



;;method declaration names en method invocation names resolven beiden naar IMethodBinding
;;kunnen dus opnieuw alleen laagst liggende nodes beschouwen
;;moet analoog aan generalize-references aangezien het om een dec en een inv gaat, 



;;todo: variant generalize-constructorinvocations?
;;superconstructorinvocations -> constructor? classinstancecreation -> constructor? 


;lukt niet via namen: probleem is dat invokes en invokedby beiden op invs en decs werken ipv op namen
;; tenzij ik dit aanpas, maar dan krijg ik een gelijkaardig probleem van duplicaten als bij de references en types
;vraag is; heeft het zin om een ganse inv / dec te vervangen door een var
;moeten uiteindelijk toch alleen naam abstraheren ... achteraf kan er over argumenten geabastraheerd worden
;vanuit dat opzicht heeft generalize-* in het algemeen steeds betrekking tot namen, wat conceptueel mooier zit

;todo: abstract because very similar to generalize-references
(defn
  generalize-invocations
  [snippetgroup snippet node]
  (let [methoddecnodename 
        (if 
          (= :SimpleName (astnode/ekeko-keyword-for-class-of node))
          node
          (.getName node))]
    ;to push replacement to lowest-level node (in line with other generalize-* operators)
    (letfn [(addinvokes [template bindingtoresolveto invokedvar]
              (let [resolvingnodes
                    (snippet/snippet-children-resolvingto 
                      template 
                      (snippet/snippet-root template) 
                      (fn [nodebinding]
                        (or (.isEqualTo bindingtoresolveto nodebinding)
                            (and (astnode/binding-method? nodebinding)
                                 (.overrides nodebinding bindingtoresolveto)))))
                    lowestlevelresolvingnodes
                    (withoutimmediateparents template resolvingnodes)]
                [;new template
                 (reduce 
                   (fn [snippetsofar resolvingnode]
                     (if 
                       (= methoddecnodename resolvingnode)
                       (replace-by-variable snippetsofar resolvingnode invokedvar)
                       (add-directive-invokes
                         (replace-by-wildcard snippetsofar resolvingnode)
                         resolvingnode invokedvar)))
                   template
                   lowestlevelresolvingnodes)
                 ;number of references in template
                 (count lowestlevelresolvingnodes)]))]
      (if-let [binding (snippet/snippet-node-resolvedbinding snippet node)]
        (when 
          (astnode/binding-method? binding)
          (let [referredvar
                (util/gen-lvar "methoddec")
                newtemplatesandcounts 
                (map (fn [snippet] (addinvokes snippet binding referredvar))
                     (snippetgroup/snippetgroup-snippetlist snippetgroup))
;                foobar
;                (println newtemplatesandcounts)
                counts
                (map (fn [[t cnt]] cnt) newtemplatesandcounts)]
;            (println newtemplatesandcounts)
            (when (> (apply + counts) 1) 
              (snippetgroup/snippetgroup-update-snippetlist snippetgroup (map first newtemplatesandcounts)))))))))



(defn 
  generalize-constructorinvocations
  [snippetgroup snippet node]
  (let [methoddecnodename 
        (if 
          (= :SimpleName (astnode/ekeko-keyword-for-class-of node))
          node
          (.getName node))] ;to push replacement to lowest-level node (in line with other generalize-* operators)
    (letfn [(addinvokes [template bindingtoresolveto invokedvar]
              (let [resolvingnodes
                    (snippet/snippet-children-resolvingto 
                      template 
                      (snippet/snippet-root template) 
                      (fn [nodebinding]
                        ;not necessary to check for overriding
                        ;(or 
                        ;   (and (astnode/binding-method? nodebinding)
                        ;       (.overrides nodebinding bindingtoresolveto))))
                        (.isEqualTo bindingtoresolveto nodebinding))
                      (fn [snippet node]
                        (snippet/snippet-node-resolvedbinding 
                          snippet node 
                          (fn [correspondingprojectnode] 
                            (let [nodetype (astnode/ekeko-keyword-for-class-of correspondingprojectnode)]
                              (when (some #{nodetype} [:ClassInstanceCreation :ConstructorInvocation :SuperConstructorInvocation])
                                (.resolveConstructorBinding correspondingprojectnode)))))))
                    lowestlevelresolvingnodes
                    (withoutimmediateparents template resolvingnodes)]
                [;new template
                 (reduce 
                   (fn [snippetsofar resolvingnode]
                       (add-directive-constructs
                         (replace-by-wildcard snippetsofar resolvingnode)
                         resolvingnode invokedvar))
                   (if
                     (= template snippet)
                     (replace-by-variable template node invokedvar)
                     template)
                   lowestlevelresolvingnodes)
                 ;number of references in template
                 (count lowestlevelresolvingnodes)]))]
      (if-let [binding (snippet/snippet-node-resolvedbinding snippet node)]
        (when 
          (astnode/binding-method? binding)
          (let [referredvar
                (util/gen-lvar "constructor")
                newtemplatesandcounts 
                (map (fn [snippet] (addinvokes snippet binding referredvar))
                     (snippetgroup/snippetgroup-snippetlist snippetgroup))
                counts
                (map (fn [[t cnt]] cnt) newtemplatesandcounts)]
            (when (> (apply + counts) 0) ;there is no reference in the constructor itself, so 1 suffices already 
              (snippetgroup/snippetgroup-update-snippetlist snippetgroup (map first newtemplatesandcounts)))))))))



(defn
  extract-template
  "Extract the given node into a new template. Node itself is replaced by a variable that links old and new templates together."
  [snippetgroup snippet node]
  (let [;create new snippet from node
        destination (atom (matching/snippet-from-node node))
        extractedvar (util/gen-readable-lvar-for-value node)
        ;replace node by variable in original snippet
        source (replace-by-variable snippet node extractedvar)]
    ;transfer bound directives
    (snippet/walk-snippets-elements
      @destination
      (snippet/snippet-root @destination)
      snippet
      node
      (fn [[destval srcval]] 
        (let [srcbds
              (snippet/snippet-bounddirectives-for-node snippet srcval)
              srcpid
              (snippet/snippet-value-projectanchoridentifier snippet srcval)
              destbds
              (map
                (fn [bounddirective]
	                 (directives/make-bounddirective
                    (directives/bounddirective-directive bounddirective)
                    (cons 
                      (directives/make-implicit-operand destval)
                      (rest (directives/bounddirective-operandbindings bounddirective)))))
                   srcbds)]
          (swap! destination snippet/update-bounddirectives destval destbds)
          (swap! destination snippet/update-projectanchoridentifier destval srcpid)
          )))
    (swap! destination snippet/update-anchor 
           (snippet/snippet-value-projectanchoridentifier snippet (snippet/snippet-root snippet))) 
    (snippetgroup/snippetgroup-update-snippetlist
      snippetgroup
      (conj 
        ;replace original snippet by one in which variable substitutes for a node
        (snippetgroup/snippetgroup-snippetlist
          (snippetgroup/replace-snippet snippetgroup snippet source))
        ;add new snippet to group
        (add-directive-equals @destination (snippet/snippet-root @destination) extractedvar)))
    ))
  
  
                     
                     



(defn
  newvalue|string
  [valuetype string]
  (condp = valuetype
    java.lang.Integer/TYPE
    (java.lang.Integer/parseInt string)
    java.lang.Boolean/TYPE
    (java.lang.Boolean/valueOf string)
    java.lang.String
    string
    org.eclipse.jdt.core.dom.Modifier$ModifierKeyword 
    (modifierkeyword-from-string string)))

(defn
  replace-value
  "Replaces a primitive value with the value corresponding to the given string."
  [snippet value string]
  (let [property
        (astnode/owner-property value)
        parent
        (astnode/owner value)
        clazz
        (astnode/property-descriptor-value-class property)
        newvalue 
        (newvalue|string clazz string)]
    (let [newsnippet (atom snippet)] 
      ;dissoc parent and all of its children, including value
      (snippet/walk-snippet-element 
        snippet
        parent 
        (fn [val] (swap! newsnippet matching/remove-value-from-snippet val)))
      (.setStructuralProperty parent property newvalue)
      ;re-add parent such that correct reifier is used for its new property value
      ;TODO: this is incorrect, old directives are lost
      ;should restore those of parent and all children, new child should get directives from old child
      ;perhaps by recording them in a map from  persistent identifier to directives
      (util/walk-jdt-node 
        parent 
        (fn [val] 
          (swap! newsnippet matching/add-value-to-snippet val))) 
      @newsnippet)))


(defn
  erase-list
  "Removes all elements of a list."
  [snippet value]
  (reduce 
    (fn [newsnippet child]
      (remove-node newsnippet child))
    snippet
    ;copy into seq to avoid NoSuchElementException
    (into [] (snippet/snippet-node-children|conceptually snippet value))))


(defn
  erase-comments
  "Removes all JavaDoc and comments inside of node."
  [snippet value]
  (let [newsnippet (atom snippet)]
    (snippet/walk-snippet-element 
      snippet value
      (fn [val] 
        (when (instance? Comment val)
          (swap! newsnippet remove-node val))))
    @newsnippet))


(def docclasskeywords [:BlockComment :Javadoc :LineComment :Comment])

(defn
  ignore-comments
  "Replaces all JavaDoc and comments inside node by wildcard."
  [snippet value]
  (let [newsnippet (atom snippet)]
    (snippet/walk-snippet-element 
      snippet value
      (fn [node]
        (when (some #{(astnode/ekeko-keyword-for-class-of node)} docclasskeywords)
          (swap! newsnippet replace-by-wildcard node)))
      (fn [lst])
      (fn [primitive])
      (fn [nullvalue]
        (let [ownerproperty 
              (astnode/owner-property nullvalue)
              valueclass
              (astnode/property-descriptor-child-node-class ownerproperty)]
          (when (some #{(astnode/ekeko-keyword-for-class valueclass)} docclasskeywords)
            (swap! newsnippet replace-by-wildcard nullvalue)))))
    @newsnippet))

(defn
  ignore-absentvalues
  "Ignores all null-values inside of node by replacing them by a wildcard. 
   Without, these values are also required to be absent in the match. "
  [snippet value]
  (let [newsnippet (atom snippet)]
    (snippet/walk-snippet-element 
      snippet value
      (fn [node])
      (fn [lst])
      (fn [primitive])
      (fn [nullvalue] 
        (swap! newsnippet replace-by-wildcard nullvalue)))
    @newsnippet)) 

;(defn
;  generalize-types
;  "Generalizes all references to the same type referred to by the argument."
;  [snippet value var]
;  ;vindt uit waar
;  (let [newsnippet (atom snippet)]
;    (snippet/walk-snippet-element 
 ;     snippet value
 ;;     (fn [value] 
 ;       (when (instance? Comment value)
 ;         (swap! newsnippet remove-node value))))
 ;   @newsnippet)) 

    


;TODO:operator to undo effect on children
(defn
  consider-regexp|list
  "Considers list as regular expression for matching elements."
  [snippet value]
  ;add regexp directive to lst
  (snippet/add-bounddirective
    ;remove grounding directives from elements 
    (reduce
      (fn [newsnippet lstel]
        (matching/remove-directives newsnippet lstel [matching/directive-child]));also directive-member?  
      ;remove exact match directive from list itself
      (matching/remove-directives snippet value [matching/directive-exact matching/directive-consider-as-regexp|lst matching/directive-consider-as-regexp|cfglst])
      (astnode/value-unwrapped value))
    value 
    (directives/make-bounddirective 
      matching/directive-consider-as-regexp|lst
      [(make-directiveoperandbinding-for-match value)])))

(defn
  consider-regexp|cfglist
  "Considers list as regular expression for control flow graph matching."
  [snippet value]
  ;add regexp directive to lst
  (snippet/add-bounddirective
    ;remove grounding directives from elements 
    (reduce
      (fn [newsnippet lstel]
        (matching/remove-directives newsnippet lstel [matching/directive-child]));also directive-member?  
      ;remove exact match directive from list itself
      (matching/remove-directives snippet value [matching/directive-exact matching/directive-consider-as-regexp|lst matching/directive-consider-as-regexp|cfglst])
      (astnode/value-unwrapped value))
    value 
    (directives/make-bounddirective 
      matching/directive-consider-as-regexp|cfglst
      [(make-directiveoperandbinding-for-match value)])))

(defn
  update-multiplicity
  "Associates given regexp matching multiplicity (n,'+,'*) with subject."
  [snippet subject multiplicity]
  (snippet/add-bounddirective
    ;remove pre-existing regexp multiplicities
    (matching/remove-directives snippet subject [matching/directive-multiplicity]);also directive-member?  
    subject
    (directives/make-bounddirective 
      matching/directive-multiplicity
      [(make-directiveoperandbinding-for-match subject)
       (directives/make-directiveoperand-binding
           (directives/make-directiveoperand "Multiplicity")
           multiplicity)
       ])))


(defn
  consider-set|list
  "Considers list as set from which elements are matched."
  [snippet value]
  (snippet/add-bounddirective
    ;remove grounding directives from elements 
    (reduce
      (fn [newsnippet lstel]
        (matching/remove-directives newsnippet lstel [matching/directive-child]));also directive-member?  
      ;remove exact match directive from list itself
      (matching/remove-directives snippet value [matching/directive-exact matching/directive-consider-as-regexp|lst matching/directive-consider-as-regexp|cfglst])
      (astnode/value-unwrapped value))
    value 
    (directives/make-bounddirective 
      matching/directive-consider-as-set|lst
      [(make-directiveoperandbinding-for-match value)])))

(defn
  include-inherited
  "Also include inherited class members in a type declaration.
   Also acts similar to match|set, except that the ordering of members is disregarded"
  [snippet value]
  (snippet/add-bounddirective
    ;remove grounding directives from elements 
    (reduce
      (fn [newsnippet lstel]
        (matching/remove-directives newsnippet lstel [matching/directive-child]));also directive-member?  
      ;remove exact match directive from list itself
      (matching/remove-directives snippet value [matching/directive-exact matching/directive-consider-as-regexp|lst matching/directive-consider-as-regexp|cfglst])
      (astnode/value-unwrapped value))
    value 
    (directives/make-bounddirective
      matching/directive-include-inherited
      [(make-directiveoperandbinding-for-match value)])))

(defn
  isolate-stmt-in-block
  "Removes all other statements in a block and adds set matching to the block."
  [snippet node]
  (let [parent-list (snippet/snippet-node-parent|conceptually snippet node)]
    (consider-set|list
      (reduce 
        (fn [newsnippet child]
          (if (= child node)
            newsnippet
            (remove-node newsnippet child)))
        snippet
        (into [] (snippet/snippet-node-children|conceptually snippet parent-list)))
      parent-list)))

(defn
  isolate-stmt-in-method
  "Removes all other statements in a block and adds set matching to the block, and child* to the selected statement."
  [snippet node]
  (let [[pulledup-snippet new-node]
        (loop [cur-node node]
          (let [parent3 (snippet/snippet-node-ancestor|conceptually snippet cur-node 3)]
            (if (instance? MethodDeclaration parent3)
              (replace-node-with snippet cur-node snippet node)
              (recur parent3))))]
    (relax-scope-to-child*
      (isolate-stmt-in-block pulledup-snippet new-node) 
      new-node)))

(defn
  isolate-expr-in-method
  "Replaces method body such that it matches with any method body containing the selected expression"
  [snippet node]
  (let [a (.getAST (snippet/snippet-root snippet))
        expr-stmt (newnode|classkeyword a :ExpressionStatement)
        dummy (newnode|classkeyword a :NullLiteral)
        side-eff (.setExpression expr-stmt dummy)
        [pulledup-snippet replaced-node]
        (loop [cur-node node]
          (let [parent (snippet/snippet-node-ancestor|conceptually snippet cur-node 1)
                parent2 (snippet/snippet-node-ancestor|conceptually snippet cur-node 2)]
            (if (instance? MethodDeclaration parent2)
              ; Once we found the method declaration
              ; Remove all statements, insert an expression statement, then replace the expression with the one we want
              (replace-node-with
                (insert-at 
                 (erase-list snippet cur-node)
                 expr-stmt (astnode/value-unwrapped cur-node) 0)
                dummy snippet node
                )
              (recur parent))))]
    
    pulledup-snippet
;    (relax-scope-to-child*
;      (isolate-stmt-in-block pulledup-snippet new-node) 
;      new-node)
    ))

(defn
  register-callbacks 
  []
  
  
  )

(register-callbacks)
