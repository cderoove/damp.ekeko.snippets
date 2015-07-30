(ns
  ^{:doc "Functions to convert a .patch file into an Ekeko/X template"
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
           [changenodes Differencer])
  (:require [damp.ekeko.snippets 
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
            [clojure.java.io :as io]))

(defn extract-filepath [line prefix-length]
  "Extract the "
  (let [source-file (subs line (+ 6 prefix-length))
        assert1 (assert (.endsWith source-file ".java"))
        source-file-no-ext (first (clojure.string/split source-file #"\."))
        source-cls-absolute (clojure.string/replace source-file-no-ext #"/" ".")]
    source-cls-absolute))

(defn diff-to-snippet [project-name prefix-length lines]
  (let [; Get the 3rd line of diff output, 6th character .. and strip the prefix off the file path
        source-file1 (extract-filepath (nth lines 2) prefix-length)
        source-file2 (extract-filepath (nth lines 3) prefix-length)
        
        cu1 (-> (util/find-compilationunit project-name source-file1)
                           damp.ekeko.jdt.astnode/jdt-parse-icu
;                          .types
;                          first ; Get the first type declaration in this ICU
                          )
        
        cu2 (-> (util/find-compilationunit project-name source-file2)
                           damp.ekeko.jdt.astnode/jdt-parse-icu
;                          .types
;                          first ; Get the first type declaration in this ICU
                          )
        
        differencer (new changenodes.Differencer cu1 cu2)
;        node (-> cu .types first .getMethods second)
        ]
    (.difference differencer)
    (.getOperations differencer)
    
;    (-> cu (.getLineNumber (.getStartPosition node)) )

;    (matching/snippet-from-node cu)
    ))

(defn patch-to-template
  "Convert a unified .patch file into an Ekeko/X template"
  [project-name prefix-length patch-file]
  (let [snippets
        (with-open [rdr (io/reader patch-file)]
          (loop [lines (line-seq rdr)
                 snippets []]
            (if (empty? lines)
              snippets
              (if (.startsWith (first lines) "diff ")
                ; We're at the start of a diff; parse its output into a snippet
                (recur 
                  (rest lines)
                  (conj snippets (diff-to-snippet project-name prefix-length lines)))
                ; Otherwise skip this line..
                (recur 
                  (rest lines)
                  snippets)))))]
    snippets
;    (snippetgroup/make-snippetgroup 
;      (str "Converted from:" patch-file)
;      snippets)
    ))


(comment
  (patch-to-template 
    "TestCase-JDT-CompositeVisitor"
    (count "TestCase-JDT-CompositeVisitor.src.")
    "/Users/soft/Desktop/test.patch")
  
  "/Users/soft/Documents/Github/damp.ekeko.snippets/damp.ekeko.snippets.plugin.test/resources/Ekeko-JDT"
  )