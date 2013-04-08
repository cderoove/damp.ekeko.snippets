(ns damp.ekeko.snippets.rewrite
  ^{:doc "Rewrite AST in Compilation Unit."
    :author "Coen De Roover, Siltvani"}
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko.snippets 
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


; GENERATE EKEKO LOGIC
; ----------------------------------------------
; Generate ekeko logic based on changes history 
; Operator history format
; history --> vector of [applied operator-id, var-node, args]

(defn 
  rewrite-query
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
  snippetgrouphistory-rewrite-query
  "Generate query the Ekeko projects for rewrite of the given snippetgrouphistory."
  [snippetgrouphistory]
  (defn snippetgroup-rewrite-query-rec [snippetgroup changes-history changes-query]
    (if (empty? changes-history)
      changes-query
      (let [history (first changes-history)]
        (snippetgroup-rewrite-query-rec 
          snippetgroup 
          (rest changes-history)
          (concat 
            changes-query 
            (rewrite-query 
              snippetgroup
              (representation/history-operator history)
              (representation/history-varnode history)
              (representation/history-args history)))))))
  (let [query (querying/snippetgroup-query 
                (representation/snippetgrouphistory-original snippetgrouphistory)
                'damp.ekeko/ekeko)]
    `(~@(concat 
          (butlast query)
          (list (concat
                  (last query) ;;get query inside fresh and concat it with rewrite query
                  (snippetgroup-rewrite-query-rec 
                    (representation/snippetgrouphistory-original snippetgrouphistory)
                    (representation/snippetgrouphistory-history snippetgrouphistory)
                    '())))))))  
      
(defn
  rewrite-query-by-snippetgrouphistory
  "Excecute rewrite query of the given snippetgrouphistory."
  ;;note, if print is removed, when apply-rewrite is not working
  [snippetgrouphistory]
  (print
    (eval (snippetgrouphistory-rewrite-query snippetgrouphistory))))


