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
     [rewrites :as rewrites]
     [ast :as ast]])
  (:import
    [org.eclipse.jdt.core.dom ASTNode Comment]
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

(defn 
  replace-by-wildcard 
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
  generalize-directive
  "Generalize a directive (e.g. convert an existing 'type' directive to 'type*')"
  [snippet node]
  (let [bds
        (snippet/snippet-bounddirectives-for-node snippet node)
        new-bds
        (for [bd bds]
          (let [bdname (directives/directive-name (directives/bounddirective-directive bd))
                bdops (.getOperandBindings bd)] 
            (case bdname
              "child" (directives/make-bounddirective matching/directive-child* bdops)
              "type" (directives/make-bounddirective matching/directive-subtype* bdops)
              "type|sname" (directives/make-bounddirective matching/directive-subtype*|sname bdops)
              "type|qname" (directives/make-bounddirective matching/directive-subtype*|qname bdops)
              bd)))]
    (snippet/update-bounddirectives snippet node new-bds)))

(defn
  remove-directive
  "Generalize a directive (e.g. convert an existing 'type' directive to 'type*')"
  [snippet node directive-name]
  (let [new-bds
        (remove (fn [bd] (= directive-name 
                            (directives/directive-name (directives/bounddirective-directive bd))))
                (snippet/snippet-bounddirectives-for-node snippet node))]
    (snippet/update-bounddirectives snippet node new-bds)))

;todo: delete template elements other than nodes
(defn
  remove-node
  "Removes node from snippet. "
  [snippet node]
  (let [newsnippet 
        (atom snippet)] 
    (do 
      (snippet/walk-snippet-element ;dissoc children 
                                    snippet
                                    node 
                                    (fn [val] 
                                      (swap! newsnippet matching/remove-value-from-snippet val)))
      (.delete node))   ;remove node
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
  "Replaces a node within a snippet with another node from another snippet"
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
              destbds
              (map
                (fn [bounddirective]
	                 (directives/make-bounddirective
                    (directives/bounddirective-directive bounddirective)
                    (cons 
                      (directives/make-implicit-operand destval)
                      (rest (directives/bounddirective-operandbindings bounddirective)))))
                   srcbds)]
          (swap! newsnippet snippet/update-bounddirectives  destval destbds))))
    @newsnippet))

(defn
  replace-parent
  "Make an expression node replace its parent."
  [snippet node]
  (let [parent-node (snippet/snippet-node-parent|conceptually snippet node)]
    (replace-node-with snippet parent-node snippet node)))

(defn
  generalize-references|vardec
  "Generalizes all references to given variable declaration node in the snippet."
  [snippet node binding]
  (let [referredvar (util/gen-lvar "vardec")]
    (replace-by-variable 
      (reduce 
        (fn [snippetsofar resolvingnode]
          (if 
            (or 
              (= node resolvingnode)
              (some #{resolvingnode} (snippet/snippet-node-children|conceptually snippet node))) ;e.g., simplename of a vardecfragment
            snippetsofar
            (add-directive-refersto 
              (replace-by-wildcard snippetsofar resolvingnode)
              resolvingnode referredvar)))
        snippet
        (snippet/snippet-children-resolvingto snippet (snippet/snippet-root snippet) binding))
      node
      referredvar)))

(defn
  generalize-references|name
  "Generalizes all references to given variable declaration node in the snippet."
  [snippet node binding]
  (let [referredvar (util/gen-lvar "vardecname")]
    (replace-by-variable 
      (reduce 
        (fn [snippetsofar resolvingnode]
          (if 
            (or 
              (= node resolvingnode)
              (= resolvingnode (snippet/snippet-node-parent|conceptually snippet node))) ;e.g., vardecfragment parent of simplename
            snippetsofar
            (add-directive-refersto 
              (replace-by-wildcard snippetsofar resolvingnode)
              resolvingnode referredvar)))
        snippet
        (snippet/snippet-children-resolvingto snippet (snippet/snippet-root snippet) binding))
      node
      referredvar)))



(defn
  generalize-references
  "Generalizes all references in the snippet o given variable declaration node or its name."
  [snippet node]
  (if-let [binding (snippet/snippet-node-resolvedbinding snippet node)]
    (when 
      (astnode/binding-variable? binding)
      (if 
        (some #{(astnode/ekeko-keyword-for-class-of node)} [:SimpleName :QualifiedName])
        (generalize-references|name snippet node binding)
        (generalize-references|vardec snippet node binding)))))


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
    (snippet/snippet-node-children|conceptually snippet value)))


(defn
  erase-comments
  "Removes all JavaDoc and comments inside of node."
  [snippet value]
  (let [newsnippet (atom snippet)]
    (snippet/walk-snippet-element 
      snippet value
      (fn [value] 
        (when (instance? Comment value)
          (swap! newsnippet remove-node value))))
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
  register-callbacks 
  []
  
  
  )

(register-callbacks)


