(ns
  ^{:doc "Utility to read the clones described in the .csv files in the Qualitas Corpus Clone Collection
          (http://qualitascorpus.com/clones) , and convert these clones into Ekeko/X snippets"
    :author "Tim Molderez"}
  ec.util.clonecsv2snippet
  (:require
    [damp.ekeko.snippets.parsing :as p]
    [damp.ekeko.snippets.matching :as m]
    [clojure.java.io :as io]
    [clojure.string :as s]))

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

(defn- parse-clone-pair
  [clone-data src-path]
  (let [file1 (nth clone-data 1)
        file2 (nth clone-data 6)
        [start1 end1] (get-line-pair (nth clone-data 3))
        [start2 end2] (get-line-pair (nth clone-data 8))
        to-snippet (fn [file start end]
                     (let [ast-node (p/parse-string-declaration 
                                      (get-range-from-file (str src-path file) start end))]
                       (if (nil? ast-node)
                         nil
                         (m/jdt-node-as-snippet ast-node)))
                     )]
    [(to-snippet file1 start1 end1) (to-snippet file2 start2 end2)]))

(defn clonecsv2snippets
  "Read .csv file and convert its clone clusters into snippets
   @param csv-path  path to .csv file
   @param src-path  path to folder containing the source code that the .csv refers to"
  [csv-path src-path]
  (with-open [rdr (io/reader csv-path)]
    (loop [lines (drop 34 (line-seq rdr)) ; Skip the first 34 lines with header info..
           snippets {}]
      (let [line (first lines)
            clone-data (s/split line #"\t")
            cluster (nth clone-data 0)]
        (if (or (= cluster "Cluster Information") (empty? lines)) ; We're done once we reach this line
          snippets
          (let
            [cluster-key (keyword cluster)
             new-cluster (concat 
                           (cluster-key snippets)
                           (parse-clone-pair clone-data src-path))]
            (recur
              (rest lines)
              (assoc snippets cluster-key new-cluster))))))))

(comment
  (get-range-from-file "jgrapht-0.8.1-mete-cmcd.csv" 3 7)
  (clonecsv2snippets "jedit-4.3pre14-mete-cmcd.csv" "bla")
  (clonecsv2snippets "jgrapht-0.8.1-mete-cmcd.csv" "/Users/soft/Downloads/QualitasCorpus-20130901r-pt1/Systems/jgrapht/jgrapht-0.8.1/compressed")
  
  (time (clonecsv2snippets "jgrapht-0.8.1-mete-cmcd.csv" "/Users/soft/Downloads/QualitasCorpus-20130901r-pt1/Systems/jgrapht/jgrapht-0.8.1/compressed")))