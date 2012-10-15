(ns 
  ^{:doc "Snippet-driven querying of Java projects."
    :author "Coen De Roover, Siltvani"}
  damp.ekeko.snippets.snippets
  (:refer-clojure :exclude [== type])
  (:use clojure.core.logic)
  (:import [org.eclipse.jdt.core.dom ASTParser AST ASTNode CompilationUnit])
  (:use [damp ekeko])
  (:use [damp.ekeko logic])
  (:use [damp.ekeko.jdt reification astnode]))


; Parsing strings as Java code
; ----------------------------

(defn 
  jdt-node-malformed?
  "Returns whether a JDT ASTNode has its MALFORMED bit set or is a CU which has an IProblem which is an error."
  [^ASTNode n]
   (or (not= 0 (bit-and (.getFlags n) (ASTNode/MALFORMED)))
       (and (instance? CompilationUnit n)
            (some (fn [p] (.isError p))
                  (.getProblems n)))))

(defn 
  jdt-node-valid? 
  "Returns whether a JDT ASTNode is valid (i.e., is not malformed)."
  [n]
  (not (jdt-node-malformed? n)))

(declare jdt-parse-snippet)

(defn 
  parse-snippet-statements
  "Parses the given string as a sequence of Java statements."
  [snippet]
  (jdt-parse-snippet snippet (ASTParser/K_STATEMENTS)))

(defn 
  parse-snippet-expression 
  "Parses the given string as a Java expression."
  [snippet]
  (jdt-parse-snippet snippet (ASTParser/K_EXPRESSION)))

(defn 
  parse-snippet-unit 
  "Parses the given string as a Java compilation unit."
  [snippet]
  (jdt-parse-snippet snippet (ASTParser/K_COMPILATION_UNIT)))

(defn 
  parse-snippet-declarations 
  "Parses the given string as a sequence of Java class body declarations."
  [snippet]
  (jdt-parse-snippet snippet (ASTParser/K_CLASS_BODY_DECLARATIONS)))

(defn 
  jdt-parse-snippet 
  "Parses the given string as a Java construct of the given kind
   (expression, statements, class body declarations, compilation unit),
   or as the first kind for which the JDT parser returns a valid ASTNode."
  ([^String snippet snippet-kind]
    (let [parser (ASTParser/newParser AST/JLS3)]                
      (.setSource parser (.toCharArray snippet))
      (.setKind parser snippet-kind)
      (.createAST parser nil)))
  ([snippet]
    (let [kinds (list (ASTParser/K_EXPRESSION) (ASTParser/K_STATEMENTS) (ASTParser/K_CLASS_BODY_DECLARATIONS) (ASTParser/K_COMPILATION_UNIT))]
      (some (fn [k] 
              (let [result (jdt-parse-snippet snippet k)]
                (when (jdt-node-valid? result)
                  result)))
            kinds))))


; Actual snippets
; ---------------


(defn 
  gen-lvar
  "Generates a unique symbol starting with ?v 
   (i.e., a symbol to be used as the name for a logic variable)."
  [] 
  (gensym '?v))

;(defn snippet []
;  (Snippet. nil {} {} {}))


;ast: complete snippet 
;ast2groundv: variable bound to corresponding child in parent  
;ast2ungroundv: variable on which all constraints generated by ast2constrainf apply, not yet bound, to be bound by ast2groundf (through, for instance, unification with ast2groundv)
;ast2groundf: function that generations conditions that will ground ast2unground, takes 2 arguments (groundv en ungroundv)
;ast2constrainf: function that generates conditions that will constrain ast2ungroundv
;(defrecord Snippet [ast ast2groundv ast2ungroundv ast2groundf ast2constrainf])


;alternatief: bij het doorwandelen van een snippet om er een query van te genereren, ast2groundf gewoon de var van + de parent meegeven, zo moet er geen aparte ast2ungroundv meegegeven worden
(defrecord Snippet [ast ast2var ast2groundf ast2constrainf ])

(defn snippet-var-for-node [snippet template-ast]
  (get-in snippet [:ast2var template-ast]))

(defn snippet-grounder-for-node [snippet template-ast]
  (get-in snippet [:ast2groundf template-ast]))

(defn snippet-constrainer-for-node [snippet template-ast]
  (get-in snippet [:ast2constrainf template-ast]))

(defn snippet-nodes [snippet]
  (keys (:ast2var snippet)))

(defn snippet-vars [snippet]
  (vals (:ast2var snippet)))


(defn ast-primitive-as-string [primitive]
  ;used on things like Modifier.ModifierKeyword                        
  ;could dispatch on this as well
  (cond (nil? primitive)  nil
        (or (true? primitive)  (false? primitive)) primitive
        :else (str "\"" (.toString primitive) "\"")))

(defn make-constraining-function [template-ast]
 (if 
   (instance? ASTNode template-ast)
   (let [template-keyw (ekeko-keyword-for-class-of template-ast)
         template-properties (node-ekeko-properties template-ast)]
     (fn [snippet]
       (let [var-match (snippet-var-for-node snippet template-ast)
             child-conditions (for [[property-keyw retrievalf] 
                                    (seq template-properties)
                                    :let [child (retrievalf)
                                          var-child (or (snippet-var-for-node snippet child)
                                                         (ast-primitive-as-string child))]]
                                `(has ~property-keyw ~var-match ~var-child))]
         `((ast ~template-keyw ~var-match)
            ~@child-conditions))))
   (let [template-list-size (.size template-ast)]
     (fn [snippet]
       (let [var-match (snippet-var-for-node snippet template-ast)
             element-conditions (for [element template-ast
                                      :let [idx-el (.indexOf template-ast element)
                                            var-el (or 
                                                     (snippet-var-for-node snippet element)
                                                     (ast-primitive-as-string element))]]
                                  `(equals ~var-el (get ~var-match ~idx-el)))]
         `((equals ~template-list-size (.size ~var-match))
           ~@element-conditions))))))
         
 
(defn make-grounding-function [template-ast]
  (let [template-owner (owner template-ast)
        template-keyw (ekeko-keyword-for-class-of template-ast)]
    (fn [snippet] 
      (let [var-match (snippet-var-for-node snippet template-ast)] 
          (if 
            (nil? template-owner)
            `((ast ~template-keyw ~var-match))
            (let [var-match-owner (snippet-var-for-node snippet template-owner)
                  owner-property (owner-property template-ast) 
                  owner-property-keyw (ekeko-keyword-for-property-descriptor owner-property)]
              (cond
                (property-descriptor-child? owner-property) 
                `((has ~owner-property-keyw ~var-match-owner ~var-match))
                (property-descriptor-list? owner-property) 
                (if 
                  (instance? ASTNode template-ast)
                  (let [template-list (node-property-value template-owner owner-property)
                        var-list (snippet-var-for-node snippet template-list)
                        template-position (.indexOf template-list template-ast)]
                    `((has ~owner-property-keyw ~var-match-owner ~var-list)
                       (equals ~var-match (.get ~var-list ~template-position))))
                  `((has ~owner-property-keyw ~var-match-owner ~var-match)))
                :else (throw (Exception. "make-grounding-function should only be called for NodeLists and Nodes. Not simple values.")))))))))

          
(defn assoc-snippet-ast [snippet ast]
  (->
    snippet
    (assoc-in [:ast2var ast] (gen-lvar))
    (assoc-in [:ast2groundf ast] (make-grounding-function ast))
    (assoc-in [:ast2constrainf ast] (make-constraining-function ast))))


;(defn jdt-node-as-snippet   
;  ([ast]
;    (jdt-node-as-snippet 
 ;     (Snippet. ast {ast (gen-lvar)} { ast (id ast) } { ast (id ast) })
 ;;     (node-children ast)))
 ; ([snippet ast-children]
 ;   (if 
  ;    (empty? ast-children)
  ;;    snippet
  ;    (let [ast (first ast-children)
  ;          others (rest ast-children)]
  ;      (cond 
   ;       (instance? ASTNode ast) (recur (assoc-snippet-ast snippet ast)
   ;;                                      (concat (node-children ast) others))
   ;       (instance? java.util.AbstractList ast) (recur (assoc-snippet-ast snippet ast)
   ;                                                     (concat ast others))
   ;       :else (recur snippet others)))))) ;primitive values are not added, nodes constrain themselves to non-ast/list things
        


(defn walk-jdt-node [ast node-f list-f primitive-f]
  (defn walk-jdt-nodes [nodes]
    (when-not (empty? nodes)
      (let [ast (first nodes)
            others (rest nodes)]
        (cond 
          (instance? ASTNode ast) 
            (do
              (node-f ast)
              (recur (concat (node-children ast) others)))
          (instance? java.util.AbstractList ast) 
            (do 
              (list-f ast)
              (recur (concat ast others)))
          :else 
            (do 
              (primitive-f ast)
              (recur others))))))
  (walk-jdt-nodes (list ast)))
    
    
(defn jdt-node-as-snippet [n]
  (let [snippet (atom (Snippet. n {} {} {}))]
    (walk-jdt-node 
      n
      (fn [ast] (swap! snippet assoc-snippet-ast ast))
      (fn [ast-list] (swap! snippet assoc-snippet-ast ast-list))
      (fn [primitive]))
    @snippet))
    
    

;(defn snippet-conditions [snippet]
; (apply concat 
;   (for [ast (snippet-nodes snippet)]
;     (concat ((snippet-grounder-for-node snippet ast) snippet)
;             ((snippet-constrainer-for-node snippet ast) snippet)))))

(defn snippet-query [snippet]
  (defn conditions [ast-or-list]
    (concat ((snippet-grounder-for-node snippet ast-or-list) snippet)
            ((snippet-constrainer-for-node snippet ast-or-list) snippet)))
  (let [ast (:ast snippet)
        query (atom '())]
    (walk-jdt-node 
      ast
      (fn [ast] (swap! query concat (conditions ast)))
      (fn [ast-list] (swap! query concat (conditions ast)))
      (fn [primitive]))
    @query))


(defn eval-snippet [snippet]
  (let [conditions (snippet-query snippet)
        ast-var (snippet-var-for-node snippet (:ast snippet))
        vars (disj (ekeko-extract-vars conditions) ast-var)
        query `(ekeko* [~ast-var]
                       (fresh [~@vars]
                              ~@conditions))]
    (println "Evaluating: " query)
    (eval query)))
      
(defn eval-snippet-condition-by-condition [snippet]
  (let [ast-var (snippet-var-for-node snippet (:ast snippet))
        conditions (snippet-query snippet)
        vars (disj (ekeko-extract-vars conditions) ast-var)]
    (defn eval-conditions [remaining]
      (when-not (nil? remaining)
        (let [query `(ekeko* [~ast-var]
                             (fresh [~@vars]
                                    ~@remaining))]
          (eval query)
          (recur (butlast remaining)))))
    (eval-conditions conditions)))  



; a compilationUnit
; (ast :CompilationUnit ?ast)
; (child ?ast ... ?child1)
; (child ?ast ... ?childn)



; next: use /* */ after or before concrete syntax of a node to specity what condition generator function to use for that node (since jdt uses that syntax)










                  
  
  

