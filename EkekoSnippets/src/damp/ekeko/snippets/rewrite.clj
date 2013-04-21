(ns damp.ekeko.snippets.rewrite
  ^{:doc "Rewrite AST in Compilation Unit."
    :author "Coen De Roover, Siltvani"}
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko.snippets 
             [gui :as gui]
             [querying :as querying]
             [operatorsrep :as operatorsrep]
             [representation :as representation]
             [parsing :as parsing]])
  (:require 
    [damp.ekeko [logic :as el]]
    [damp.ekeko.jdt 
     [astnode :as astnode]
     [reification :as reification]])
  (:import 
    [damp.ekeko JavaProjectModel]
    [org.eclipse.jface.text Document]
    [org.eclipse.text.edits TextEdit]
    [org.eclipse.jdt.core ICompilationUnit IJavaProject]
    [org.eclipse.jdt.core.dom BodyDeclaration Expression Statement ASTNode ASTParser AST CompilationUnit]
    [org.eclipse.jdt.core.dom.rewrite ASTRewrite]))


; AST modification data

(defn make-rewrite-for-cu 
  [cu]
  (ASTRewrite/create (.getAST cu)))

(def current-rewrites (atom {}))

(defn reset-rewrites! []
  (reset! current-rewrites {}))

(defn current-rewrite-for-cu [cu]
  (if-let [rw (get @current-rewrites cu)]
    rw
    (let [nrw (make-rewrite-for-cu cu)]
      (swap! current-rewrites assoc cu nrw)
      nrw))) 

(defn apply-rewrite-to-node [rw node]
  (JavaProjectModel/applyRewriteToNode rw node))

(defn apply-rewrites []
  (doseq [[cu rw] @current-rewrites]
    (apply-rewrite-to-node rw cu)))

(defn apply-and-reset-rewrites []
  (do
    (apply-rewrites)
    (reset-rewrites!)))


; Interface

(defn replace-node 
  "Replace node with newnode."
  [node newnode]
  (let [cu (.getRoot node)
        rewrite (current-rewrite-for-cu cu)] 
    (.replace rewrite node newnode nil)))

(defn replace-node-in-rewrite-code
  "Replace node with new string code as new node."
  [node string]
  (let [newnode (parsing/parse-string-ast string)] 
    (replace-node node newnode)))

(defn remove-node 
  "Remove node."
  [node]
  (let [cu (.getRoot node)
        rewrite (current-rewrite-for-cu cu)
        list-rewrite (.getListRewrite rewrite (.getParent node) (.getLocationInParent node))]
    (.remove list-rewrite node nil)))

(defn add-node 
  "Add newnode to propertyList of the given parent at idx position."
  [parent propertykey newnode idx]
  (let [cu (.getRoot parent)
        rewrite (current-rewrite-for-cu cu)
        property (astnode/node-property-descriptor-for-ekeko-keyword parent propertykey) 
        list-rewrite (.getListRewrite rewrite parent property)
        index (if (instance? java.lang.String idx)
               (Integer/parseInt idx)
               idx)] 
    (.insertAt list-rewrite newnode index nil)))

(defn add-node-in-rewrite-code
  "Add new string code as new node to propertyList of the given parent at idx position."
  [parent property string idx]
  (let [newnode (parsing/parse-string-ast string)]
    (add-node parent property newnode idx)))

(defn change-property-node
  "Change property node."
  [node propertykey value]
  (let [cu (.getRoot node)
        rewrite (current-rewrite-for-cu cu)
        property (astnode/node-property-descriptor-for-ekeko-keyword node propertykey)] 
    (.set rewrite node property value))) 


; MAP FOR REWRITE SNIPPET
; -----------------------------
; Map {template-snippet rewrite-snippet}


; ADD REWRITE SNIPPET
; -----------------------

(defn 
  make-rewritemap
  []
  {})

(defn
  add-rewrite-snippet
  [rewrite-map template-snippet rewrite-snippet]
  (assoc rewrite-map template-snippet rewrite-snippet))

(defn 
  update-rewrite-snippet
  [rewrite-map template-snippet rewrite-snippet]
  (assoc rewrite-map template-snippet rewrite-snippet))
  
(defn 
  get-rewrite-snippet
  [rewrite-map template-snippet]
  (get rewrite-map template-snippet))

(defn 
  remove-rewrite-snippet
  [rewrite-map template-snippet]
  (dissoc rewrite-map template-snippet))


; GENERATE EKEKO REWRITE QUERY
; ----------------------------------------------
; Generate ekeko rewrite query based on changes history 
; Operator history format
; history --> vector of [applied operator-id, var-node, args]

(defn 
  rewrite-query-by-operator
  [snippetgroup op-id var-match args]
  (let [snippet (representation/snippetgroup-snippet-for-var snippetgroup var-match)
        node (representation/snippet-node-for-var snippet var-match)] 
    (cond 
      (= op-id :add-node)
      (let [var-parent (representation/snippet-var-for-node snippet (:owner node))
            property (astnode/ekeko-keyword-for-property-descriptor (:property node))
            newnode (first args)
            idx (fnext args)] 
        `((el/perform (add-node-in-rewrite-code ~var-parent ~property ~newnode ~idx))))
      (= op-id :remove-node)
      `((el/perform (remove-node ~var-match)))
      (= op-id :replace-node)
      (let [newnode (first args)] 
        `((el/perform (replace-node-in-rewrite-code ~var-match ~newnode))))
      (= op-id :change-property-node)
      (let [var-parent (representation/snippet-var-for-node snippet (:owner node))
            property (astnode/ekeko-keyword-for-property-descriptor (:property node))
            value (first args)] 
        `((el/perform (change-property-node ~var-parent ~property ~value))))
      :default
      (throw (Exception. (str "Unknown changes: " op-id))))))

(defn 
  replace-node-with-logic-vars
  [node str-new-node user-vars]
  (defn 
    generate-rewrite-snippet
    [str-snippet user-vars]
    (if (empty? user-vars)
      str-snippet
      (let [var (first (first user-vars))
            node (fnext (first user-vars))]
        (generate-rewrite-snippet
          (clojure.string/replace str-snippet var (str node))
          (rest user-vars)))))
  (replace-node-in-rewrite-code 
    node 
    (generate-rewrite-snippet str-new-node user-vars)))

(defn
  snippet-rewrite-query
  [template-snippet rewrite-snippet grp]
  (let [var-match (representation/snippet-var-for-root template-snippet)
        str-rewrite-snippet (clojure.string/replace (gui/print-snippet rewrite-snippet) "?" "*")             
        user-vars (representation/snippet-uservars rewrite-snippet)
        user-vars-condition
        (for [var user-vars
              :let [str-var (clojure.string/replace (str var) "?" "*")]]
          `[~str-var ~var]) 
        query `((el/perform 
                  (replace-node-with-logic-vars 
                    ~var-match 
                    ~str-rewrite-snippet 
                    [~@user-vars-condition])))]
    (querying/add-query 
      (querying/snippet-in-group-query template-snippet grp 'damp.ekeko/ekeko)      
      query))) 
  
(defn
  snippet-rewrite-import-declaration-query
  [template-snippet rewrite-snippet grp]
  (let [var-match (representation/snippet-var-for-root template-snippet)
        str-rewrite-snippet (gui/print-snippet rewrite-snippet)
        var-cu '?cu
        query `((cl/fresh [~var-cu]
                          (el/equals ~var-cu (.getRoot ~var-match))
                          (el/perform (add-node-in-rewrite-code ~var-cu :imports ~str-rewrite-snippet 0))))]
    (querying/add-query 
      (querying/snippet-in-group-query template-snippet grp 'damp.ekeko/ekeko)      
      query))) 

(defn
  internal-snippetgrouphistory-rewrite-query
  "Generate query the Ekeko projects for rewrite of the given snippetgrouphistory and rewritemap."
  [snippetgrouphistory rewritemap function-query]
  (defn rewrite-query-rec [rewrite-map rewrite-query]
    (if (empty? rewrite-map)
      rewrite-query
      (let [template-snippet (key (first rewrite-map))
            rewrite-snippet (val (first rewrite-map))
            query (function-query 
                    template-snippet 
                    rewrite-snippet 
                    (representation/snippetgrouphistory-current snippetgrouphistory))]
        (rewrite-query-rec 
          (remove-rewrite-snippet rewrite-map template-snippet)
          (cons query rewrite-query))))) 
  `(~@(rewrite-query-rec rewritemap '())))
      
(defn
  snippetgrouphistory-rewrite-query
  "Generate query the Ekeko projects for rewrite of the given snippetgrouphistory and rewritemap."
  ;note : the string ? should be changed to other character, otherwise
  ;error: Unsupported binding form: ?...
  [snippetgrouphistory rewritemap]
  (internal-snippetgrouphistory-rewrite-query
    snippetgrouphistory rewritemap snippet-rewrite-query))

(defn
  snippetgrouphistory-rewrite-import-declaration-query
  "Generate query the Ekeko projects for rewrite (add import declaration) of the given snippetgrouphistory and rewritemap."
  [snippetgrouphistory rewritemap]
  (internal-snippetgrouphistory-rewrite-query
    snippetgrouphistory rewritemap snippet-rewrite-import-declaration-query))

(defn
  rewrite-query-by-snippetgrouphistory
  "Excecute rewrite query of the given snippetgrouphistory and rewritemap."
  ;;note, if print is removed, when apply-rewrite is not working
  [snippetgrouphistory rewritemap rewritemap-import]
  (print
    (map eval (snippetgrouphistory-rewrite-query snippetgrouphistory rewritemap)))
  (print
    (map eval (snippetgrouphistory-rewrite-import-declaration-query snippetgrouphistory rewritemap-import))))


