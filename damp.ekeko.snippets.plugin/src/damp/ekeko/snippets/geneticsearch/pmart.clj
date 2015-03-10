(ns
  ^{:doc "Design patterns experiment: Use our genetic search algorithm to infer design pattern templates, using the P-MARt data set.
          http://www.iro.umontreal.ca/~labgelo/p-mart/"
  :author "Tim Molderez"}
  damp.ekeko.snippets.geneticsearch.pmart
  (:refer-clojure :exclude [rand-nth rand-int rand])
  (:import 
    [damp.ekeko JavaProjectModel]
    [org.eclipse.jface.text Document]
    [org.eclipse.text.edits TextEdit]
    [org.eclipse.jdt.core ICompilationUnit IJavaProject]
    [org.eclipse.jdt.core.dom BodyDeclaration Expression Statement ASTNode ASTParser AST CompilationUnit]
    [org.eclipse.jdt.core.dom.rewrite ASTRewrite])
  (:require [clojure.xml])
  (:require [clojure.core.logic :as cl])
  (:require [damp.ekeko]
            [damp.ekeko.jdt
             [astnode :as astnode]
             [rewrites :as rewrites]])
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
             [transformation :as transformation]])
  (:require [damp.ekeko.snippets.geneticsearch 
             [individual :as individual]
             [fitness :as fitness]]))

(def program-names 
  {:uml "1 - QuickUML 2001"
   :lexi "2 - Lexi v0.1.1 alpha"
   :refactoring "3 - JRefactory v2.6.24"
   :netbeans "4 - Netbeans v1.0.x"
   :junit "5 - JUnit v3.7"
   :jhotdraw "6 - JHotDraw v5.1"
   :mapperxml "8 - MapperXML v1.9.7"
   :nutch "10 - Nutch v0.4"
   :pmd "11 - PMD v1.8"})

(def pmart-xml (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile "/resources/P-MARt/P-MARt.xml"))
(def pmart (clojure.xml/parse pmart-xml))

(defn program [pmart-xml name]
  (let [all-programs (get-in pmart-xml [:content])
        find-program (fn [[head & tail]]
                       (if (nil? head)
                         nil
                         (if (= name (first (:content (first (:content head))))) ; Check the <name> tag in <program>, assuming it comes first..
                           head
                           (find-program tail))))]
    (find-program all-programs)))

(defn pattern-instances [program-xml]
  "Given a <program>, 
   return a map from a design pattern name to a list of <microArchitecture> of that pattern.
   (A <microArchitecture> is a design pattern instance)"
  (let [patterns (rest (:content program-xml))
        keys (for [pattern patterns]
               (keyword (:name (:attrs pattern))))
        vals (for [pattern patterns]
               (:content (first (:content pattern))))]
    (zipmap keys vals)))

(defn pattern-roles [microarchitecture-xml]
  "Given a <microArchitecture>, (i.e. a design pattern instance)
   return a map from a role name to a list of class names implementing that role"
  (let [roles (:content (first (:content microarchitecture-xml)))
        keys (for [role roles] 
               (:tag (first (:content role))))
        values (for [role roles]
                 (for [cls (:content role)]
                   (first (:content (first (:content cls))))))]
    (zipmap keys values)))


(comment 
  (inspector-jay.core/inspect (pattern-roles (first (:Builder (pattern-instances (program pmart "1 - QuickUML 2001")))))))