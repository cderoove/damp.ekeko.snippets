(ns
  ^{:doc "Convert a .patch file into an Ekeko/X template; see patch-to-template function"
    :author "Tim Molderez"}
damp.ekeko.snippets.patches.templateconverter
  (:refer-clojure :exclude [== type declare record?])
  (:import [damp.ekeko JavaProjectModel]
           [org.eclipse.jface.text Document]
           [org.eclipse.text.edits TextEdit]
           [org.eclipse.jdt.core ICompilationUnit IJavaProject]
           [org.eclipse.jdt.core.dom BodyDeclaration Expression Statement ASTNode ASTParser AST CompilationUnit]
           [org.eclipse.jdt.core.dom.rewrite ASTRewrite]
           [org.eclipse.core.resources ResourcesPlugin IWorkspace]
           [org.eclipse.jdt.core  IMember IJavaElement ITypeHierarchy JavaCore IType IJavaModel IJavaProject IPackageFragment ICompilationUnit]
           [org.eclipse.ui PlatformUI IWorkingSet IWorkingSetManager]
           [org.eclipse.core.runtime.jobs Job]
           [org.eclipse.core.runtime Status Path]
           [damp.ekeko EkekoModel JavaProjectModel ProjectModel]
           [changenodes Differencer]
           [changenodes.operations Delete Insert Move Update])
  (:require [damp.ekeko.jdt
             [astnode :as astnode]] 
            [damp.ekeko.snippets 
             [snippet :as snippet]
             [snippetgroup :as snippetgroup]
             [persistence :as persistence]
             [querying :as querying]
             [matching :as matching]
             [operators :as operators]
             [operatorsrep :as operatorsrep]
             [util :as util]
             [directives :as directives]
             [transformation :as transformation]]
            [qwalkeko.clj.functionalnodes :as qwal]
            [clojure.java.io :as io]))

(defn- extract-filepath [line prefix-length]
  "Extract the file path mentioned in a --- or +++ line of a .patch file"
  (let [source-file (subs line (+ 6 prefix-length))
        assert1 (assert (.endsWith source-file ".java"))
        source-file-no-ext (first (clojure.string/split source-file #"\."))
        source-cls-absolute (clojure.string/replace source-file-no-ext #"/" ".")]
    source-cls-absolute))

(defn find-compatible-node
  "Find a node (part of some AST) within a clone of that AST (tgt-ast)"
  [node tgt-ast]
  (let [path (astnode/path-from-root node)]
    (astnode/node-from-path tgt-ast path)))

(defn- minimize-node [node-orig]
  "Makes a minimized copy of this node (i.e. in which all non-mandatory properties are cleared)
   @see Operation.minimizeNode in ChangeNodes"
  (let [node (ASTNode/copySubtree (.getAST node-orig) node-orig)]
    (doseq [prop (.structuralPropertiesForType node)]
      (cond 
        (.isChildProperty prop)
        (if (not (.isMandatory prop))
          (.setStructuralProperty node prop nil)
          (minimize-node (.getStructuralProperty node prop)))
        
        (.isSimpleProperty prop)
        (if (not (.isMandatory prop))
          (.setStructuralProperty node prop nil))
        
        (.isChildListProperty prop)
        (.clear (.getStructuralProperty node prop))))
    node))

(defmulti process-change
  "Process a change and adjust the given transformation template accordingly"
  (fn [change changedeps change-snippetmap transfo] (clojure.core/type change)))

; Deletion changes
(defmethod process-change Delete [change changedeps change-snippetmap transfo]
  [transfo change-snippetmap])

; Insertion changes
(defmethod process-change Insert [change changedeps change-snippetmap transfo]
  (let [change-idx (first (get change-snippetmap change))      
        lhs (first (snippetgroup/snippetgroup-snippetlist (:lhs transfo)))
        parent (.getAffectedNode change)
        compat-parent (find-compatible-node parent (snippet/snippet-root lhs))
        inserted (minimize-node (.getRightNode change)) 
        
        parent-change (let [deps (nth (:dependencies changedeps) change-idx)
                            idx-to-change (zipmap 
                                            (map (fn [[idx snip]] idx) (vals change-snippetmap)) 
                                            (keys change-snippetmap))]
                        (get idx-to-change (first deps)))
        
        prop (.getProperty change)
        insert-idx (.getIndex change)
        eq-var (str (util/gen-lvar "insert"))
        
        init-insert-rhs (matching/snippet-from-node inserted)
        new-insert-rhs (operators/add-directive-add-element init-insert-rhs (snippet/snippet-root init-insert-rhs) eq-var)
        new-change-snippetmap (assoc change-snippetmap change [change-idx new-insert-rhs])
        ]
    (if (nil? parent-change)
      
      ; Affected node is in the LHS
      (let [compat-parent-lst (snippet/snippet-node-child|conceptually lhs compat-parent prop)
            new-lhs (operators/add-directive-equals lhs compat-parent-lst eq-var)]
        [(transformation/make-transformation (snippetgroup/replace-snippet (:lhs transfo) lhs new-lhs) 
                                             (snippetgroup/add-snippet (:rhs transfo) new-insert-rhs))
         new-change-snippetmap])
      
      ; Affected node is in one of the RHSs
      (let [ctxt-rhs (second (get change-snippetmap parent-change))
            ctxt-lst (snippet/snippet-node-child|conceptually ctxt-rhs (snippet/snippet-root ctxt-rhs) prop)
            new-ctxt-rhs (operators/add-directive-equals ctxt-rhs ctxt-lst eq-var)
            new-rhs-group (snippetgroup/replace-snippet (:rhs transfo) ctxt-rhs new-ctxt-rhs)
            ]
        [(transformation/make-transformation (:lhs transfo)
                                             (snippetgroup/add-snippet new-rhs-group new-insert-rhs))
         new-change-snippetmap]
        ))))


; Move changes
(defmethod process-change Move [change changedeps change-snippetmap transfo]
  [transfo change-snippetmap])

; Update changes
(defmethod process-change Update [change changedeps change-snippetmap transfo]
  [transfo change-snippetmap])

(defn changes-to-template [changes changedeps cu]
  "Convert a list of distilled Changes into a CU"
  (let [init-lhs (matching/snippet-from-node cu)
        init-transfo (transformation/make-transformation
                       (snippetgroup/make-snippetgroup "LHS" [init-lhs])
                       (snippetgroup/make-snippetgroup "RHS" []))
        init-change-snippetmap (zipmap
                                 changes
                                 (map-indexed (fn [idx change] [idx nil]) changes)
                                 )]
    (first (reduce 
             (fn [[transfo change-snippetmap] change] (process-change change changedeps change-snippetmap transfo))
             [init-transfo init-change-snippetmap]
             (take 2 changes)))))

(defn diff-to-snippet [project-name1 project-name2 prefix lines]
  "Convert the output of a diff command into an Ekeko/X template
   project-name1 - Eclipse project name, where the patch hasn't been applied yet
   project-name2 - Eclipse project name, after applying the patch
   prefix - Strip this prefix from the absolute file paths used in the .patch file, to get a relative path that works within the Eclipse projects
   lines - Output of a diff command"
  (let [prefix-length (count prefix)
        source-file1 (extract-filepath (nth lines 2) prefix-length)
        source-file2 (extract-filepath (nth lines 3) prefix-length)
        cu1 (-> (util/find-compilationunit project-name1 source-file1)
              damp.ekeko.jdt.astnode/jdt-parse-icu)
        cu2 (-> (util/find-compilationunit project-name2 source-file2)
              damp.ekeko.jdt.astnode/jdt-parse-icu)
        differencer (new changenodes.Differencer cu1 cu2)
        changedeps (qwal/ast-ast-graph cu1 cu2)]
    (.difference differencer)
    (changes-to-template (.getOperations differencer) changedeps cu1)
    ))

(defn patch-to-template
  "Convert a .patch file (produced by git) into an Ekeko/X template
   project-name1 - Eclipse project name, where the patch hasn't been applied yet
   project-name2 - Eclipse project name, after applying the patch
   prefix - Strip this prefix from the absolute file paths used in the .patch file, to get a relative path that works within the Eclipse projects
   patch-file - Path to the .patch file"
  [project-name1 project-name2 prefix patch-file]
  (let [transfos
        (with-open [rdr (io/reader patch-file)]
          (loop [lines (line-seq rdr)
                 snippets []]
            (if (empty? lines)
              snippets
              (if (.startsWith (first lines) "diff ")
                ; We're at the start of a diff; parse its output into a snippet
                (recur 
                  (rest lines)
                  (conj snippets (diff-to-snippet project-name1 project-name2 prefix lines)))
                ; Otherwise skip this line..
                (recur 
                  (rest lines)
                  snippets)))))]
    transfos))


(comment
  (let [transfos (patch-to-template 
                   "TestCase-JDT-CompositeVisitor"
                   "TestCase-JDT-CompositeVisitor2"
                   "TestCase-JDT-CompositeVisitor.src."
                   "/Users/soft/Desktop/test.patch")]
    (persistence/spit-transformation 
      "/Users/soft/Documents/Github/damp.ekeko.snippets/damp.ekeko.snippets.plugin.test/resources/EkekoX-Specifications/dbg/dbg.ekx"
      (first transfos)))
  
  
  (do
    (inspector-jay.core/inspect 
      (patch-to-template 
           "TestCase-JDT-CompositeVisitor"
           "TestCase-JDT-CompositeVisitor2"
           "TestCase-JDT-CompositeVisitor.src."
           "/Users/soft/Desktop/test.patch"))
    nil))