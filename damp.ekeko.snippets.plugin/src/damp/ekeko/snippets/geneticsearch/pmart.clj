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
             [search :as search]
             [individual :as individual]
             [fitness :as fitness]])
  (:require [test.damp [ekeko :as test]])
  (:import [org.eclipse.core.resources ResourcesPlugin IWorkspace]
           [org.eclipse.jdt.core  IMember IJavaElement ITypeHierarchy JavaCore IType IJavaModel IJavaProject IPackageFragment ICompilationUnit]
           [org.eclipse.ui PlatformUI IWorkingSet IWorkingSetManager]
           [org.eclipse.core.runtime.jobs Job]
           [org.eclipse.core.runtime Status Path]
           [damp.ekeko EkekoModel JavaProjectModel ProjectModel]))

(defn apply-operator-to-root
  [templategroup snippet operator-id]
  (let [operator (first (filter 
                          (fn [op] (= (operatorsrep/operator-id op) operator-id))
                          (operatorsrep/registered-operators)))
        subject (snippet/snippet-root snippet)
        bindings (operatorsrep/make-implicit-operandbinding-for-operator-subject templategroup snippet subject operator)]
    (operatorsrep/apply-operator-to-snippetgroup templategroup snippet subject operator [bindings])))


(defn apply-operator-to-all-roots
  [templategroup operator-id]
  (loop [tg templategroup
         snippets (snippetgroup/snippetgroup-snippetlist templategroup)]
    (if (empty? snippets)
      tg
      (let [snippet (first snippets)]
        (recur 
          (apply-operator-to-root tg snippet operator-id)
          (rest snippets))))))

(defn preprocess-templategroup
  [templategroup]
  (-> templategroup
    (apply-operator-to-all-roots "erase-comments")
    (apply-operator-to-all-roots "ignore-comments")
    (apply-operator-to-all-roots "ignore-absentvalues")))

(defn program [pmart-xml name]
  (let [all-programs (get-in pmart-xml [:content])
        find-program (fn [[head & tail]]
                       (if (nil? head)
                         nil
                         (if (= name (first (:content (first (:content head))))) ; Check the <name> tag in <program>, assuming it comes first..
                           head
                           (recur tail))))]
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
    (dissoc (zipmap keys values) nil)))

(defn find-compilationunit [^String project-name ^String cls-name]
  "Find a compilation unit, given an Eclipse project name and an absolute class name"
  (let [all-projects (.getJavaProjects (JavaCore/create (.getRoot  (damp.ekeko.workspace.workspace/eclipse-workspace))))
        project (first (filter 
                        (fn [project]
                          (= project-name (.getElementName project)))
                        all-projects))]
    (.getCompilationUnit (.findType project cls-name))))



(defn templategroup-from-classes [name project-name class-list]
  (let [snippets (for [cls class-list]
                   (try (-> (find-compilationunit project-name cls)
                          damp.ekeko.jdt.astnode/jdt-parse-icu
                          .types
                          first ; Get the first type declaration in this ICU
                          matching/snippet-from-node)
                     (catch Exception e
                       (println "!!! Could not parse " cls)
                       nil)))]
    (snippetgroup/make-snippetgroup name (remove (fn [x] (nil? x)) snippets))))

(defn spit-pattern-instances [folder-name pmart-xml project-names pattern-name]
  (util/make-dir folder-name)
  (let [instances (apply concat
                         (for [name project-names]
                           (for [instance ((keyword pattern-name) (pattern-instances (program pmart-xml name)))]
                             [name instance])))]
    (doseq [[project-name instance] instances]
      (let [pattern-roles-map (pattern-roles instance)
                                 class-lists
                                 (doseq [role (keys pattern-roles-map)]
                                   (doseq [cls (role pattern-roles-map)]
                                     (persistence/spit-snippet 
                                       (str folder-name "/" (hash cls) ".ekt") 
                                       (preprocess-templategroup
                                         (templategroup-from-classes cls project-name [cls])))))]))))

(defn slurp-pattern-instances-as-templategroups
  [folder-name pmart-xml project-names pattern-name]
  (let [instances (apply concat
                         (for [name project-names]
                           (for [instance ((keyword pattern-name) (pattern-instances (program pmart-xml name)))]
                             [name instance])))
        templategroups (map-indexed
                         (fn [idx [project-name instance]]
                           (let [pattern-roles-map (pattern-roles instance)
                                 class-lists (util/combinations pattern-roles-map)]
                             (for [class-list class-lists]
                               (snippetgroup/make-snippetgroup 
                                 (str pattern-name "-" idx " --- " project-name)
                                 (for [cls class-list]
                                   (first 
                                     (snippetgroup/snippetgroup-snippetlist 
                                       (persistence/slurp-snippet (str folder-name "/" (hash cls) ".ekt")))))))))
                         instances)]
    (mapcat identity templategroups)))

(defn pattern-instances-as-templategroups
  "Given the parsed P-MARt XML, a list of projects and a design pattern name,
   produce a list of templategroups of all instances of that pattern in the given projects."
  [pmart-xml project-names pattern-name]
  (let [instances (apply concat
                         (for [name project-names]
                           (for [instance ((keyword pattern-name) (pattern-instances (program pmart-xml name)))]
                             [name instance])))
        templategroups (map-indexed
                         (fn [idx [project-name instance]]
                           (let [pattern-roles-map (pattern-roles instance)
                                 class-lists (util/combinations pattern-roles-map)
;                                 (for [role (keys pattern-roles-map)]
;                                   (first (role pattern-roles-map)))
                                 ]
                             (for [class-list class-lists]
                               (templategroup-from-classes (str pattern-name "-" idx " --- " project-name) project-name class-list))))
                         instances)]
    (for [x (mapcat identity templategroups)]
      (preprocess-templategroup x))))

(defn parse-pmart-xml []
  (clojure.xml/parse (test.damp.ekeko.snippets.EkekoSnippetsTest/getResourceFile "/resources/P-MARt/P-MARt.xml")))

(def projects 
    {:uml "1 - QuickUML 2001"
     :lexi "2 - Lexi v0.1.1 alpha"
;     :refactoring "3 - JRefactory v2.6.24"
;     :netbeans "4 - Netbeans v1.0.x"
     :junit "5 - JUnit v3.7"
     :jhotdraw "6 - JHotDraw v5.1"
     :mapperxml "8 - MapperXML v1.9.7"
     :nutch "10 - Nutch v0.4"
     :pmd "11 - PMD v1.8"})

(comment
  
  ; Try to infer an Observer template from a few instances
  (def results (pattern-instances-as-templategroups (parse-pmart-xml) [(:pmd projects)] "Observer"))
  (do (inspector-jay.core/inspect results) nil)
  (fitness/templategroup-matches (first results))
  
  (doseq [x results]
    (println (count (snippetgroup/snippetgroup-nodes x))))
  
  ; Print number of nodes for all design pattern instances in all projects..
  (doseq [project-key (keys projects)]
    (let [patterns (pattern-instances (program (parse-pmart-xml) (project-key projects)))]
      (doseq [pattern-key (keys patterns)]
        (util/log "pmart" (str (project-key projects) "---" (name pattern-key)))
        (doseq [tg (pattern-instances-as-templategroups (parse-pmart-xml) [(project-key projects)] (name pattern-key))]
          (util/log "pmart" (count (snippetgroup/snippetgroup-nodes tg)))
          (print ".")))))
  
  ; Spit all classes involved in the Singleton pattern to .ekt snippets
  (spit-pattern-instances "test" (parse-pmart-xml) [(projects :mapperxml)] "Singleton")
  
  (inspector-jay.core/inspect
    (slurp-pattern-instances-as-templategroups 
     "/Users/soft/Documents/Github/damp.ekeko.snippets/damp.ekeko.snippets.plugin.test/resources/EkekoX-Specifications/singleton-mapperxml" 
     (parse-pmart-xml) [(projects :mapperxml)] "Singleton"))
  
  ; Inspect which patterns are in a project..
  (do (inspector-jay.core/inspect (pattern-instances (program (parse-pmart-xml) (:jhotdraw projects)))) nil)
  
  
  
  
  
  
  
  (do (inspector-jay.core/inspect (preprocess-templategroup 
                                   (templategroup-from-classes "Test" (:uml projects) ["diagram.tool.ToolListener"]))) nil)
  
  (fitness/templategroup-matches (preprocess-templategroup 
                                   (templategroup-from-classes "Test" (:uml projects) ["diagram.tool.ToolListener"])))
    
  (fitness/templategroup-matches (templategroup-from-classes "Test" (projects :lexi) ["com.jmonkey.office.lexi.support.ActionToolBar"]))
  
  (inspector-jay.core/inspect (damp.ekeko.jdt.astnode/jdt-parse-icu (find-compilationunit (projects :lexi) "com.jmonkey.office.lexi.support.ActionManager")))
  
  (inspector-jay.core/inspect
    (remove-javadoc (templategroup-from-classes "Test" (projects :lexi) ["com.jmonkey.office.lexi.support.ActionManager"])))
  
  (fitness/templategroup-matches (first results))
  
  (clojure.pprint/pprint (querying/snippetgroup-query|usingpredicates (first results) 'damp.ekeko/ekeko true))
  
  (do (inspector-jay.core/inspect results) nil)
  
  (persistence/spit-snippetgroup "test.ekt" (first results))
  
  (inspector-jay.core/inspect (templategroup-from-classes "Design pattern" (:uml program-names) ["diagram.figures.FigureBorder"]))
  (inspector-jay.core/inspect (pattern-roles (first (:Builder (pattern-instances (program (parse-pmart-xml) "1 - QuickUML 2001"))))))
  (inspector-jay.core/inspect 
    (.getCompilationUnits (first 
                            (.getChildren (first (.getPackageFragmentRoots (first (.getJavaProjects (JavaCore/create (.getRoot  (damp.ekeko.workspace.workspace/eclipse-workspace))))))))))))