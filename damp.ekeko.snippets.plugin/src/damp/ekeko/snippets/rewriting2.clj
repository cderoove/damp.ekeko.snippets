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
     [astnode :as astnode]
     [ast :as ast]])
  (:import [org.eclipse.jdt.core.dom ASTNode]))

(defn apply-rewrite-directive! 
  [directive cu concrete-subject concrete-rhs params]
  (case (snippet/directive-name directive)
      "create-file" (rewrites/create-file concrete-rhs)
      "replace" (rewrites/replace-node cu concrete-subject concrete-rhs)
      "replace-value" (rewrites/replace-node cu concrete-subject concrete-rhs)
      "add-element" (rewrites/add-element cu concrete-subject concrete-rhs -1)
      "insert-before" (rewrites/insert-before cu concrete-subject concrete-subject concrete-rhs)
      "insert-after" (rewrites/insert-after cu concrete-subject concrete-subject concrete-rhs)
      "remove-element" (rewrites/remove-element-alt cu concrete-subject concrete-subject)
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

;(defn list-subject? [template]
;  (some
;    (fn [bd]
;      (if 
;        (or
;          (directives/bounddirective-for-directive [bd] rewriting/directive-remove-element))
;        true))
;    (snippet/snippet-bounddirectives-for-node template (snippet/snippet-root template))))

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
        
        lvar-binding (second (directives/bounddirective-operandbindings rw-bd))
        lvar (if (nil? lvar-binding)
               nil
               (directives/directiveoperandbinding-value (second (directives/bounddirective-operandbindings rw-bd))))
        params (map 
                 (fn [binding] (directives/directiveoperandbinding-value binding))
                 (drop 2 (directives/bounddirective-operandbindings rw-bd)))
        uservars (querying/snippet-uservars rhs-template)
        lvar-index (.indexOf uservars lvar) ; Is -1 if there are no parameters!
        has-params (> lvar-index -1)
        
        var-values-list 
        (into #{} 
              (mapcat 
                (fn [bindings]
                  (let [seperated-values (for [var uservars] (get bindings var))]
                    (util/cartesian seperated-values)))
                bindings-list))]
    ; Do a rewrite for each valid combination of the uservars' values
    (doseq [var-values var-values-list]
      (let [ast (snippet/snippet-root rhs-template)
            template-copy (persistence/copy-snippet rhs-template)
            subject (if has-params (nth var-values lvar-index))
            rewrite-cu (if has-params (rewrites/determine-rewrite-cu subject))
            
            ; Fill in the metavariables of the template
            concrete-rhs
            (reduce 
              (fn [cur-template index]
                (let [nodes (template-metavar-nodes cur-template (nth uservars index))
                      value (nth var-values index)]
                  (replace-nodes cur-template nodes value)
                  ))
              template-copy
              (range 0 (count uservars)))
            ]
        (apply-rewrite-directive! rw-dir rewrite-cu subject (snippet/snippet-root concrete-rhs) params)))))

(defn
  apply-transformation
  "Performs the given program transformation."
  ([transformation]
    (apply-transformation transformation [{}]))
  ([transformation initial-bindings]
    (let [lhs-templategroup (transfo/transformation-lhs transformation)
          rhs-templategroup (transfo/transformation-rhs transformation)
          lhs-bindings-list (matching2/query-templategroup lhs-templategroup initial-bindings)]
      (doseq [rhs-template (snippetgroup/snippetgroup-snippetlist rhs-templategroup)]
        (apply-rhs-template rhs-template lhs-bindings-list))
      (rewrites/apply-and-reset-rewrites))))

(comment
  (defn slurp-from-resource [pathrelativetobundle] (persistence/slurp-snippetgroup (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile pathrelativetobundle)))
  
  (defn run-test-batch [path-testfn]
    (doseq [[path testfn] path-testfn]
      (let [input (slurp-from-resource path)
            start (. System (nanoTime))
            
             lhs-templategroup (transfo/transformation-lhs input)  
;            matches (apply-transformation input)
             bindings (matching2/query-templategroup lhs-templategroup [{}])
;             _ (println bindings)
            end (/ (double (- (. System (nanoTime)) start)) 1000000.0)
            test-result true]
        (if test-result
          (println "pass (" end "ms -" path ")")
          (println "FAIL (" end "ms -"  path ")")))))
  
  (run-test-batch
    [
;     ["/resources/EkekoX-Specifications/rewriting2/add-element.ekx" not-empty]
     ["/resources/EkekoX-Specifications/rewriting2/remove-element.ekx" not-empty]
;     ["/resources/EkekoX-Specifications/rewriting2/insert-before.ekx" not-empty]
;     ["/resources/EkekoX-Specifications/rewriting2/insert-after.ekx" not-empty]
;     ["/resources/EkekoX-Specifications/rewriting2/replace.ekx" not-empty]
     ])
  
  (run-test-batch
    [
;     ["/resources/EkekoX-Specifications/rewriting2/add-element.ekx" not-empty]
     ["/resources/EkekoX-Specifications/chaq_demo/chaq-demo8.ekx" not-empty]
;     ["/resources/EkekoX-Specifications/rewriting2/insert-before.ekx" not-empty]
;     ["/resources/EkekoX-Specifications/rewriting2/insert-after.ekx" not-empty]
;     ["/resources/EkekoX-Specifications/rewriting2/replace.ekx" not-empty]
     ])
  
  (apply-transformation (persistence/slurp-transformation "/Users/soft/Desktop/Inventive/inventive2.ekx"))
  
  
    ; Test for Jonas's resource usage analysis (which requires some logic vars to be prebound)
    (let [src-cls "Test"
          src-meth "hello"
          src-call "println"
          
          tgt-cls "Test"
          tgt-meth "bye"
          
          src-tgroup (slurp-from-resource "/resources/EkekoX-Specifications/prebind/findcall.ekt")
          tgt-tgroup (slurp-from-resource "/resources/EkekoX-Specifications/prebind/findmethod.ekt")
          
          transfo (slurp-from-resource "/resources/EkekoX-Specifications/prebind/movestmt.ekx")
          
          find-simplename (fn [name]
                            (let [simple-names (ast/nodes-of-type :SimpleName)]
                              (filter
                                (fn [sname] (= (.toString sname) name))
                                simple-names)))
          src-matches (matching2/query-templategroup src-tgroup [{(symbol "?cls") (find-simplename src-cls)
                                                                  (symbol "?meth") (find-simplename src-meth)
                                                                  (symbol "?call") (find-simplename src-call)}])
          
          call (-> (first (get (first src-matches) (symbol "?call")))
                 (.getParent)
                 (.getParent))
          
          tgt-matches (matching2/query-templategroup tgt-tgroup [{(symbol "?cls") (find-simplename tgt-cls)
                                                        (symbol "?meth") (find-simplename tgt-meth)}])
          
          method (-> (first (get (first tgt-matches) (symbol "?meth")))
                   (.getParent))
          
;          transfo-matches (matching2/query-templategroup (:lhs transfo)
;                                               [{(symbol "?meth") [method]
;                                                 (symbol "?call") [call]}])
          ]
      (apply-transformation 
        transfo 
        [{(symbol "?meth") [method]
          (symbol "?call") [call]}])
      )
  
  )