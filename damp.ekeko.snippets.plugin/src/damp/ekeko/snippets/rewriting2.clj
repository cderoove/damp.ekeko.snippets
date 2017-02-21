(ns 
  ^{:doc "Implementation of rewriting directives, compatible with matching2.clj"
    :author "Coen De Roover"}
  damp.ekeko.snippets.rewriting2
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko.snippets 
             [matching :as matching]
             [matching2 :as matching2]
             [rewriting :as rewriting]
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [directives :as directives]
             [util :as util]
             [parsing :as parsing]
             [persistence :as persistence]
             [querying :as querying]
             [runtime :as runtime]
             [rewrites :as rewrites]
             [transformation :as transfo]
             [operators :as operators]])
  (:require
    [damp.ekeko [logic :as el]]
    [damp.ekeko.jdt 
     [astnode :as astnode]])
  (:import [org.eclipse.jdt.core.dom ASTNode]))

(defn apply-rewrite-directive! 
  [directive cu concrete-subject concrete-rhs params]
  (case (snippet/directive-name directive)
      "replace" (rewrites/replace-node cu concrete-subject concrete-rhs) ; OK
      "replace-value" (rewrites/replace-node cu concrete-subject concrete-rhs)
      "add-element" (rewrites/add-element cu concrete-subject concrete-rhs -1) ; OK
      "insert-before" (rewrites/insert-before cu concrete-subject concrete-rhs)
      "insert-after" (rewrites/insert-after cu concrete-subject concrete-rhs)
      "remove-element" (rewrites/remove-element cu concrete-subject (first params))
      "remove-element-alt" (rewrites/remove-element-alt cu concrete-subject concrete-rhs)
      "move-element" (let [tgt-cu (rewriting/determine-rewrite-cu concrete-rhs)]
                       (rewrites/move-element cu tgt-cu concrete-subject concrete-rhs (first params)))
      "copy-node" (let [tgt-cu (rewriting/determine-rewrite-cu concrete-rhs)]
                       (rewrites/move-element cu tgt-cu concrete-subject concrete-rhs (first params)))))

(defn rewrite-bd [template]
  (some
    (fn [bd]
      (if 
        (not (or
              (directives/bounddirective-for-directive [bd] matching/directive-exact)
              (directives/bounddirective-for-directive [bd] matching/directive-protect)
              (directives/bounddirective-for-directive [bd] matching/directive-replacedbyvariable)
              (directives/bounddirective-for-directive [bd] matching/directive-child)))
        bd))
    (snippet/snippet-bounddirectives-for-node template (snippet/snippet-root template))))

(defn apply-rhs-instance
  [rhs-template])

(defn template-metavar-nodes
  "Retrieve all template nodes which have been replaced by a metavariable"
  [template metavar]
  (let [has-metavariable 
        (fn [node]
          (let [var (matching/snippet-replacement-var-for-node template node)]
            (if (= metavar var)
              node)))]
    (filter
      has-metavariable
      (snippet/snippet-nodes template))))

(defn replace-nodes
  "Replace the given nodes with a specific value"
  [template nodes value]
  (reduce
    (fn [cur-template node]
      (let
        [templ-copy (persistence/copy-snippet cur-template)
         value-copy (ASTNode/copySubtree (.getAST (snippet/snippet-root cur-template)) value)]
        (cond ; Has side-effect on cur-template
         (or (astnode/nilvalue? node) (astnode/primitivevalue? node))
         (operators/snippet-jdtvalue-replace template node value-copy)
         (astnode/lstvalue? node)
         (operators/snippet-jdtlist-replace template node value-copy)
         :else
         (operators/snippet-jdt-replace template node value-copy))
        cur-template))
    template
    nodes))

(defn apply-rhs-template
  "Add the transformations specified in an RHS template, given the bindings of the LHS and a map of "
  [rhs-template bindings-list]
  (let [rw-bd (rewrite-bd rhs-template)
        rw-dir (snippet/bounddirective-directive rw-bd)
        
        lvar (directives/directiveoperandbinding-value (second (directives/bounddirective-operandbindings rw-bd)))
        params (map 
                 (fn [binding] (directives/directiveoperandbinding-value binding))
                 (drop 2 (directives/bounddirective-operandbindings rw-bd)))
        uservars (querying/snippet-uservars rhs-template)
        lvar-index (.indexOf uservars lvar)
        
        var-values-list 
        (into #{} 
              (mapcat 
                (fn [bindings]
                  (let [seperated-values (for [var uservars] (get bindings var))]
                    (util/cartesian seperated-values)))
                bindings-list))
        ]
    ; Do a rewrite for each valid combination of the uservars' values
    (doseq [var-values var-values-list]
      (let [ast (snippet/snippet-root rhs-template)
            template-copy (persistence/copy-snippet rhs-template)
            subject (nth var-values lvar-index)
            rewrite-cu (rewrites/determine-rewrite-cu subject)
            
            ; Fill in the metavariables of the template
            concrete-rhs
            (reduce 
              (fn [cur-template index]
                (let [nodes (template-metavar-nodes cur-template (nth uservars index))
                      value (nth var-values index)]
                  (replace-nodes cur-template nodes value)
                  ))
              template-copy
              (range 0 (count uservars)))]
        (apply-rewrite-directive! rw-dir rewrite-cu subject (snippet/snippet-root concrete-rhs) params)))))

(defn
  apply-transformation
  "Performs the given program transformation." 
  [transformation]
  (let [lhs-templategroup (transfo/transformation-lhs transformation)
        rhs-templategroup (transfo/transformation-rhs transformation)
        lhs-bindings-list (matching2/query-templategroup lhs-templategroup)
;        tmp (inspector-jay.core/inspect lhs-bindings-list)
        ]
    (doseq [rhs-template (snippetgroup/snippetgroup-snippetlist rhs-templategroup)]
      (apply-rhs-template rhs-template lhs-bindings-list))
    (rewrites/apply-and-reset-rewrites)))

(comment
  (defn slurp-from-resource [pathrelativetobundle] (persistence/slurp-snippetgroup (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile pathrelativetobundle)))
  
  (defn run-test-batch [path-testfn]
    (doseq [[path testfn] path-testfn]
      (let [input (slurp-from-resource path)
            start (. System (nanoTime))
            matches (apply-transformation input)
            end (/ (double (- (. System (nanoTime)) start)) 1000000.0)
            test-result true]
        (if test-result
          (println "pass (" end "ms -" path ")")
          (println "FAIL (" end "ms -"  path ")")))))
  
  (run-test-batch
    [["/resources/EkekoX-Specifications/rewriting2/add-element.ekx" not-empty]])
  
  (apply-transformation (persistence/slurp-transformation "/Users/soft/Desktop/addParam.ekx"))
  )