(ns
  ^{:doc "Utility to read the clones described in the .csv files in the Qualitas Corpus Clone Collection
          (http://qualitascorpus.com/clones) , and convert these clones into Ekeko/X snippets"
    :author "Tim Molderez"}
  ec.util.clonecsv2snippet
  (:require
    [damp.ekeko.jdt.javaprojectmodel :as jmodel]
    [damp.ekeko.snippets.parsing :as p]
    [damp.ekeko.snippets.persistence :as per]
    [damp.ekeko.snippets.matching :as m]
    [damp.ekeko.snippets.search :as search]
    [damp.ekeko.snippets.jmetal :as jmetal]
    [clojure.java.io :as io]
    [clojure.string :as s])
  (:import
    [org.eclipse.jdt.core ICompilationUnit]))

(defmacro dbg[x] `(let [x# ~x] (println "dbg:" '~x "=" x#) x#))

(defn- get-line-pair
  "Get integers x and y from a string formatted as '(x,y)'"
  [pair]
  (let [without-parens (subs pair 1 (dec (.length pair)))
        split (s/split without-parens #",")]
    (for [x split]
      (new Integer x))))

(defn- get-range-from-file
  "Retrieve lines [start-end] from a file"
  [file start end]
  (with-open [rdr (io/reader file)]
    (s/join "\n" 
            (take (inc (- end start))
             (drop (dec start) (doall (line-seq rdr)))))))

(defn to-ast-node
  "Try to convert a method declaration in a Java source code file (on lines [start - end]) 
   into an AST node. Returns nil on failure."
  [file start end]
  (let [ast-node (p/parse-string-declaration 
                   (get-range-from-file file start end))]
    (if (nil? ast-node)
      nil
;      (m/jdt-node-as-snippet ast-node)
      ast-node
      )))

(defn- parse-clone-pair
  [clone-data]
  (let [file1 (nth clone-data 1)
        file2 (nth clone-data 6)
        [start1 end1] (get-line-pair (nth clone-data 3))
        [start2 end2] (get-line-pair (nth clone-data 8))]
    [[file1 start1 end1] [file2 start2 end2]]))

(defn- add-to-cluster 
  [cluster file start end history]
  (let [clone-id (keyword (str file "-" start "-" end))]
    (if (contains? history clone-id)
      [cluster
       history]
      [(conj cluster (to-ast-node file start end)) 
       (conj history clone-id)]))
  )

(defn clonecsv2ast
  "Read .csv file and convert its clone clusters into AST nodes (grouped by cluster)
   @param csv-path  path to .csv file
   @param src-path  path to folder containing the source code that the .csv refers to"
  [csv-path src-path]
  (with-open [rdr (io/reader csv-path)]
    (loop [lines (drop 34 (line-seq rdr)) ; Skip the first 34 lines with header info..
           ast-nodes {}
           files {}
           history #{}]
      (let [line (first lines)
            clone-data (s/split line #"\t")
            cluster (nth clone-data 0)]
        (if (or (= cluster "Cluster Information") (empty? lines)) ; We're done once we reach this line
          [ast-nodes files]
          (let
            [cluster-key (keyword cluster)
             [[file1 start1 end1] [file2 start2 end2]] (parse-clone-pair clone-data)
             [new-cluster1 new-history1] (add-to-cluster (cluster-key ast-nodes) (str src-path file1) start1 end1 history)
             [new-cluster2 new-history2] (add-to-cluster new-cluster1 (str src-path file2) start2 end2 new-history1)]
            (recur
              (rest lines)
              (assoc ast-nodes cluster-key new-cluster2)
              (assoc files cluster-key (conj (cluster-key files) file1 file2))
              new-history2)))))))

(defn- disable-other-compilation-units!
  "Given a number of file paths to Java files of a project,
   disable all other files of that project, so Ekeko won't query them
   @return the list of compilation "
  [files]
  (let [project-model (first (jmodel/java-project-models))
        disable-method (let [meth (-> project-model 
                                    .getClass 
                                    (.getDeclaredMethod "processRemovedCompilationUnit" 
                                      (into-array Class [ICompilationUnit])))]
                         (-> meth (.setAccessible true))
                         meth)
        all-units (seq (.getCompilationUnits project-model))]
    (loop [units-remaining all-units
           units-removed []]
      (if (empty? units-remaining)
        ; Base case
        units-removed
        ; Recursive step; check whether the current unit should be disabled, and do so
        (let [unit (first units-remaining)
              type-root (.getTypeRoot unit)
              file-name (new String (.getFileName type-root))]
          (if (not (contains? (set files) file-name))
            (do 
              (-> disable-method (.invoke project-model (to-array [type-root])))
              (recur (rest units-remaining) (conj units-removed unit)))
            (recur (rest units-remaining) units-removed)))))))

(defn enable-compilation-units!
  "Add the given compilation units to the Ekeko database"
  [units]
  (let [project-model (first (jmodel/java-project-models))
        enable-method (let [meth (-> project-model 
                                   .getClass 
                                   (.getDeclaredMethod "processNewCompilationUnit" 
                                     (into-array Class [ICompilationUnit])))]
                        (-> meth (.setAccessible true))
                        meth)]
    (doseq [unit units]
      (let [type-root (.getTypeRoot unit)]
        (-> enable-method (.invoke project-model (to-array [type-root])))))))

(def src-prefix "/Users/soft/Downloads/QualitasCorpus-20130901r-pt1/Systems/")
(def csv-prefix "/Users/soft/Downloads/QCCC/Systems/")

(defn run-qualitas-clone-suite
  [system-name system-version]
  (let [full-system-name (str system-name "-" system-version)
        src-path (str src-prefix system-name "/" full-system-name "/compressed")
        csv-path (str csv-prefix system-name "/provenance/" full-system-name "-mete-cmcd.csv" )
        [ast-nodes files] (clonecsv2ast csv-path src-path)]
    (let [tmp (inspector-jay.core/inspect ast-nodes)
          cluster-no 3
          cluster (get ast-nodes (nth (keys ast-nodes) cluster-no))
          cluster-files (get files (nth (keys files) cluster-no))
          tgroups (search/population-from-tuples (set (for [x cluster] [x])))
          ; Note that you can't directly use the AST nodes from the .csv file as verified-matches..
          ; I should first turn each node into a template, then match it..
          ; I'm guessing this is because the .csv's AST nodes are objects not listed in Ekeko's database?..
          verified-matches (search/make-verified-matches
                             (for [x tgroups] (first (search/templategroup-matches-nomemo x)))
                             [])
          disabled-units (disable-other-compilation-units! cluster-files)]
      
      ;(per/spit-snippetgroup "test.ekt" (first tgroups))
      (search/evolve verified-matches 200)
      (println "Done!")
      (enable-compilation-units! disabled-units)
      verified-matches
      )))

(comment
  (def verified-matches (run-qualitas-clone-suite "jgrapht" "0.8.1"))
;  (search/evolve verified-matches 200)
  (jmetal/ibea-evolve verified-matches 400)
  
  (inspector-jay.core/inspect 
    (run-qualitas-clone-suite "jgrapht" "0.8.1"))
  
  (set-other-compilation-units-enabled! [] false)
  (set-other-compilation-units-enabled! [] true)
  (inspector-jay.core/inspect
    (jmodel/java-project-models))
  
  ; (get (System/getenv) "QUALITAS-SRC") ; Maybe make the code a little more portable using environment vars..
  
  (get-range-from-file (str csv-prefix "jgrapht/provenance/jgrapht-0.8.1-mete-cmcd.csv") 3 7)
  (clonecsv2snippets "jedit-4.3pre14-mete-cmcd.csv" "bla")
  (clonecsv2snippets "jgrapht-0.8.1-mete-cmcd.csv" "/Users/soft/Downloads/QualitasCorpus-20130901r-pt1/Systems/jgrapht/jgrapht-0.8.1/compressed")
  
  (keys (clonecsv2snippets "jgrapht-0.8.1-mete-cmcd.csv" "/Users/soft/Downloads/QualitasCorpus-20130901r-pt1/Systems/jgrapht/jgrapht-0.8.1/compressed"))
  (let []
    (inspector-jay.core/inspect (clonecsv2snippets "jgrapht-0.8.1-mete-cmcd.csv" "/Users/soft/Downloads/QualitasCorpus-20130901r-pt1/Systems/jgrapht/jgrapht-0.8.1/compressed"))
    0
    )
  )