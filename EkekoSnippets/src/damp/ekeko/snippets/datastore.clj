(ns 
    ^{:doc "Save Snippet Data to File."
    :author "Coen De Roover, Siltvani"}
    damp.ekeko.snippets.datastore
  (:require [damp.ekeko.snippets 
             [representation :as representation]
             [util :as util]
             [parsing :as parsing]]))


;; Data Format
;; --------------------------------------------------------------------------

;; Snippet Data Type
;; [ast ast2var ast2groundf ast2constrainf ast2userfs var2ast var2uservar 
;;  userquery document rewrite track2ast ast2track flag])

;; SnippetMap Data Type
;; Map {track2var track2groundf track2constrainf track2userfs track2uservar 
;;      userquery document flag}

(defn
  snippet-to-snippetmap
  "Convert snippet to snippetmap with track node."
  [snippet]
  (defn update-userfs [snippetmap track value]
    (let [userfs (get-in snippet [:ast2userfs value])]
      (if (not (nil? userfs)) 
        (assoc-in snippetmap [:track2userfs track] (str userfs))
        snippetmap)))
  (defn update-uservar [snippetmap track value]
    (let [uservar (get-in snippet [:var2uservar (representation/snippet-var-for-node snippet value)])]
      (if (not (nil? uservar)) 
        (assoc-in snippetmap [:track2uservar track] (str uservar))
        snippetmap)))
  (defn update-snippetmap [snippetmap value]
    (let [track (str (representation/snippet-track-for-node snippet value))]
      (->
        snippetmap
        (assoc-in [:track2var track]        (str (get-in snippet [:ast2var value])))
        (assoc-in [:track2groundf track]    (str (get-in snippet [:ast2groundf value])))
        (assoc-in [:track2constrainf track] (str (get-in snippet [:ast2constrainf value])))
        (update-userfs  track value)
        (update-uservar track value))))
  (let [snippetmap (atom {})]
    (util/walk-jdt-node 
      (:ast snippet)
      (fn [astval]  (swap! snippetmap update-snippetmap astval))
      (fn [lstval]  (swap! snippetmap update-snippetmap lstval))
      (fn [primval] (swap! snippetmap update-snippetmap primval))
      (fn [nilval]  (swap! snippetmap update-snippetmap nilval)))
    (swap! snippetmap assoc-in [:userquery]   
           (if (empty? (:userquery snippet))
             ""
             (str (:userquery snippet))))
    (swap! snippetmap assoc-in [:document] (.get (:document snippet)))
    (swap! snippetmap assoc-in [:flag] (str (:flag snippet)))
    @snippetmap))

(defn
  snippetmap-to-snippet
  "Convert snippetmap with track node to snippet."
  [snippetmap]
  (defn update-userfs [snippet value strTrack]
    (let [userfs (get-in snippetmap [:track2userfs strTrack])]
      (if (not (nil? userfs)) 
        (assoc-in snippet [:ast2userfs value] (util/string-to-list-of-list userfs))
        snippet)))
  (defn update-uservar [snippet var strTrack]
    (let [uservar (get-in snippetmap [:track2uservar strTrack])]
      (if (not (nil? uservar)) 
        (assoc-in snippet [:var2uservar var] (symbol uservar))
        snippet)))
  (defn update-snippet [snippet value track]
    (let [arrTrack [(representation/snippet-property-for-node snippet value) 
                    (.getStartPosition track) 
                    (.getLength track)]
          strTrack (str arrTrack)
          var (symbol (get-in snippetmap [:track2var strTrack]))]
      (->
        snippet
        (update-in [:ast2var value]        (fn [x] var))
        (update-in [:ast2groundf value]    (fn [x] (util/string-to-list (get-in snippetmap [:track2groundf strTrack]))))
        (update-in [:ast2constrainf value] (fn [x] (util/string-to-list (get-in snippetmap [:track2constrainf strTrack]))))
        (util/dissoc-in [:var2ast (representation/snippet-var-for-node snippet value)])      ;new variable replaced by variable in snippetmap
        (assoc-in  [:var2ast var] value)
        (update-userfs  value strTrack)
        (update-uservar var strTrack))))
  (let [doc (parsing/parse-string-to-document (:document snippetmap))
        snippet (atom (representation/document-as-snippet doc))
        rw (:rewrite @snippet)
        userquery (:userquery snippetmap)]
    (util/walk-jdt-node 
      (:ast @snippet)
      (fn [astval]  (swap! snippet update-snippet astval  (.track rw astval)))
      (fn [lstval]  (swap! snippet update-snippet lstval  (.track rw (:owner lstval))))
      (fn [primval] (swap! snippet update-snippet primval (.track rw (:owner primval))))
      (fn [nilval]  (swap! snippet update-snippet nilval  (.track rw (:owner nilval)))))
    (swap! snippet update-in [:userquery] 
           (fn [x] (if (empty? userquery)
                     '()
                     (list (symbol (.substring userquery 1 (- (.length userquery) 1)))))))
    (swap! snippet update-in [:flag] (fn [x] (util/string-to-keyword (:flag snippetmap))))
    @snippet))


(defn 
  get-in-json
  [jsonobj [key key2]]
  (let [el (.get jsonobj (str key))]
    (if (nil? el)
      nil
      (.get el (str key2)))))
  
(defn 
  snippet-to-snippetjson
  "Convert snippet to jsonobject."
  [snippet]
  (damp.ekeko.snippets.db.JSONFile/mapToJson (snippet-to-snippetmap snippet)))

(defn
  snippetjson-to-snippet
  "Convert jsonobject to snippet."
  ;;note: cannot convert directly from result of snippet-to-snippetjson, 
  ;;but need to use json parser first, to convert json string to json. (??)
  [snippetjson]
  (defn update-userfs [snippet value strTrack]
    (let [userfs (get-in-json snippetjson [:track2userfs strTrack])]
      (if (not (nil? userfs)) 
        (assoc-in snippet [:ast2userfs value] (util/string-to-list-of-list userfs))
        snippet)))
  (defn update-uservar [snippet var strTrack]
    (let [uservar (get-in-json snippetjson [:track2uservar strTrack])]
      (if (not (nil? uservar)) 
        (assoc-in snippet [:var2uservar var] (symbol uservar))
        snippet)))
  (defn update-snippet [snippet value track]
    (let [arrTrack [(representation/snippet-property-for-node snippet value) 
                    (.getStartPosition track) 
                    (.getLength track)]
          strTrack (str arrTrack)
          var (symbol (get-in-json snippetjson [:track2var strTrack]))]
      (->
        snippet
        (update-in [:ast2var value]        (fn [x] var))
        (update-in [:ast2groundf value]    (fn [x] (util/string-to-list (get-in-json snippetjson [:track2groundf strTrack]))))
        (update-in [:ast2constrainf value] (fn [x] (util/string-to-list (get-in-json snippetjson [:track2constrainf strTrack]))))
        (util/dissoc-in [:var2ast (representation/snippet-var-for-node snippet value)])      ;new variable replaced by variable in snippetjson
        (assoc-in  [:var2ast var] value)
        (update-userfs  value strTrack)
        (update-uservar var strTrack))))
  (let [doc (parsing/parse-string-to-document (.get snippetjson ":document"))
        snippet (atom (representation/document-as-snippet doc))
        rw (:rewrite @snippet)
        userquery (.get snippetjson ":userquery")]
    (util/walk-jdt-node 
      (:ast @snippet)
      (fn [astval]  (swap! snippet update-snippet astval  (.track rw astval)))
      (fn [lstval]  (swap! snippet update-snippet lstval  (.track rw (:owner lstval))))
      (fn [primval] (swap! snippet update-snippet primval (.track rw (:owner primval))))
      (fn [nilval]  (swap! snippet update-snippet nilval  (.track rw (:owner nilval)))))
    (swap! snippet update-in [:userquery] 
           (fn [x] (if (empty? userquery)
                     '()
                     (list (symbol (.substring userquery 1 (- (.length userquery) 1)))))))
    (swap! snippet update-in [:flag] (fn [x] (util/string-to-keyword (.get snippetjson ":flag"))))
    @snippet))

(defn 
  read-json-file
  "Read file, returns json object."
  [filename]
  (damp.ekeko.snippets.db.JSONFile/read filename))

(defn 
  write-json-file
  "Write json object to file."
  [filename json]
  (damp.ekeko.snippets.db.JSONFile/write filename json))

(defn 
  snippetgroup-to-map
  [group]
  (let [grp-map {}]
    (->
      grp-map
      (assoc-in [:name]  (:name group))
      (assoc-in [:userquery]  (if (empty? (:userquery group))
                                ""
                                (str (:userquery group))))
      (assoc-in [:snippetlist] (map snippet-to-snippetjson (:snippetlist group))))))
         
(defn 
  snippetgroup-to-json
  "Convert snippet group to jsonobject."
  [group]
  (damp.ekeko.snippets.db.JSONFile/mapToJson (snippetgroup-to-map group)))

(defn
  json-to-snippetgroup
  "Convert jsonobject to snippet group."
  [json]
  (let [group (representation/make-snippetgroup (.get json ":name"))
        userquery (.get json ":userquery")]
    (->
      group
      (update-in [:userquery] (fn [x] (if (empty? userquery)
                                        '()
                                        (list (symbol (.substring userquery 1 (- (.length userquery) 1)))))))
      (update-in [:snippetlist] (fn [x] (map snippetjson-to-snippet (seq (.get json ":snippetlist"))))))))

