(ns damp.ekeko.snippets.parsing-template
  (:require [damp.ekeko.snippets
             [parsing :as parsing]
             [util :as util]
             [snippetgroup :as snippetgroup]
             [transformation :as transformation]])
  (:import [org.eclipse.jdt.core.dom ASTParser]))

; Shorter aliases for some of the directive names to speed up writing templates in text form
(def directive-aliases
  {"..." "replaced-by-wildcard"
   "var" "replaced-by-variable"
   "eq" "equals"
   "set" "match|set"})
(def wildcard-placeholders
  ["x" ";" "{}" "public void _x_ () {}" "public class _X_ {}" "import java.util.Vector;"])

(defn ch 
  "Retrieve the character at a certain line-no and character-no.
   Returns nil if the position is invalid."
  [lines line char]
  (try 
    (nth (nth lines line) char)
    (catch IndexOutOfBoundsException e nil)))

(defn next-ch
  "Given a position, return the position of the next character.
   Returns a nil position if the end is reached."
  [lines line char]
  (if (>= (inc char) (count (nth lines line)))
    (if (>= (inc line) (count lines))
      [nil nil]
      [(inc line) 0])
    [line (inc char)]))

(defn prev-ch
  "Given a position, return the position of the previous character.
   Returns a nil position if the beginning is reached."
  [lines line char]
  (if (< (dec char) 0) 
    (if (< (dec line) 0)
      [nil nil]
      [(dec line) (dec (count (lines (dec line))))])
    [line (dec char)]))

(defn replace-code 
  "Replace a piece of code at the given [line char] position"
  [lines [line char] length new-code]
  (let [old-line (nth lines line)
        new-line (str (subs old-line 0 char) new-code (subs old-line (+ char length) (count old-line)))] 
    (assoc (vec lines) line new-line)))

(defn remove-from-lines [lines start-line start-char end-line end-char]
  "Removes a section from a list of strings, given a starting line no. and character,
   and an end line no. and index"
  (if (= start-line end-line)
    ; Base case
    (let [line (nth lines start-line)
          new-line (str 
                     (subs line 0 start-char) 
                     (subs line end-char (count line)))]
      (assoc (vec lines) start-line new-line))
    ; Recursive step
    (let [line (nth lines start-line)
          new-line (subs line 0 start-char)
          new-lines (assoc (vec lines) start-line new-line)]
      (recur new-lines (inc start-line) 0 end-line end-char))))

(defn matching-bracket [lines line-no char-no]
  "Given the position of an opening bracket, return the position of the corresponding closing bracket
   and the contents in between the two brackets"
  (let [opening-char (ch lines line-no char-no)
        closing-char (case opening-char
                       \{ \}
                       \[ \]
                       \( \)
                       (println "Not a bracket!"))]
    (loop [cur-line line-no 
           cur-char (inc char-no)
           cur-contents ""
           open-brackets 1]
      (let [[cur-line2 cur-char2] ; If at the end of a line, move to the next
            (if (>= cur-char (count (nth lines cur-line)))
              [(inc cur-line) 0]
              [cur-line cur-char])
            cur-character (ch lines cur-line2 cur-char2)]
        (cond
          (= cur-character opening-char) 
          (recur cur-line2 (inc cur-char2) (str cur-contents cur-character) (inc open-brackets))
          
          (= cur-character closing-char)
          (if (= 1 open-brackets)
            [cur-line2 cur-char2 cur-contents]
            (recur cur-line2 (inc cur-char2) (str cur-contents cur-character) (dec open-brackets)))
          
          :else
          (recur cur-line2 (inc cur-char2) (str cur-contents cur-character) open-brackets))))))

(defn next-open-bracket 
  "Starting from a given position, find the next bracket"
  [lines line char bracket-character]
  (cond 
    (>= line (count lines))
    [nil nil]
    
    (>= char (count (nth lines line)))
    (recur lines (inc line) 0 bracket-character)
    
    (= bracket-character (ch lines line char))
    [line char]
    
    :else
    (recur lines line (inc char) bracket-character)))

(defn next-non-whitespace 
  "Given a position, move just before the next non-whitespace"
  [lines line char forward?]
  (let [[init-line init-char] (if forward? (next-ch lines line char) (prev-ch lines line char))]
    (cond 
      (nil? init-char)
      [line char]
      (java.lang.Character/isWhitespace (ch lines init-line init-char))
      (recur lines init-line init-char forward?)
      :else
      [line char])))

(defn hide-comments 
  "Replaces template comments with empty lines..
   A template comment is any line that starts with a ';' "
  [lines]
  (map 
    (fn [line]
      (if (.startsWith (clojure.string/trim line) ";") "" line))
    lines))

(defn join-lines [lines]
  (clojure.string/join "\n" lines))

(defn position-from-start 
  "Convert a line+character position into a single number, which counts how many characters we've seen from
   the very beginning of lines"
  [lines line char]
  (reduce
    (fn [i cur-line]
      (if (= line cur-line)
        (+ i char)
        (+ i (count (nth lines cur-line)) 1))) ; + 1 because of the newline
    0
    (range 0 (inc line))))

(defn line-pos [lines position-from-start]
  "Inverse of position-from-start; converts a position-from-start index into a line+character position"
  (loop [remaining position-from-start
         line 0]
    (if (> (count (nth lines line)) remaining)
      [line remaining]
      (recur (- remaining (count (nth lines line))) (inc line)))))

(defn length-without-directives 
  ([lines start-line start-char end-line end-char]
    (let [[nxt-line nxt-char] (next-ch lines start-line start-char)
          start-pos (position-from-start lines nxt-line nxt-char)
          end-pos (position-from-start lines end-line end-char)
          merged (subs (clojure.string/join "-" lines) start-pos end-pos)] ; "The '-' takes up space to account for newlines.."
      (length-without-directives merged)))
  ([merged]
    (loop [cur-merged merged
           cur-count 0]
      (let [[ignore open-bracket] (next-open-bracket [cur-merged] 0 0 \[)]
        (if (nil? open-bracket) 
          ; Reached the end
          (+ cur-count (count cur-merged))
          (let [[ignore close-bracket txt] (matching-bracket [cur-merged] 0 open-bracket)
                  txt-count (length-without-directives txt)]
              (if (= \@ (nth cur-merged (inc close-bracket)))
                ; We're at a subject's initial [
                (let [[ignore close-dirs dirs-txt] (matching-bracket [cur-merged] 0 (+ close-bracket 2))]
                  (recur (subs cur-merged close-dirs) (+ cur-count (dec open-bracket) txt-count)))
                ; Not a directives list
                (recur (subs cur-merged close-bracket) (+ cur-count (dec open-bracket) txt-count))))
          
          ; Potentially reached a directives list
;          (if (= \@ (prev-ch cur-merged 0 open-bracket))
;            
;            ; We bumped into the [ of "...]@[..." , without seeing the subject's initial [ 
;            (let [[ignore close-dirs dirs-txt] (matching-bracket [cur-merged] 0 open-bracket)]
;              (recur (subs cur-merged close-dirs) (+ cur-count (- open-bracket 2))))
;            
;            (let [[ignore close-bracket txt] (matching-bracket [cur-merged] 0 open-bracket)
;                  txt-count (length-without-directives txt)]
;              (if (= \@ (nth cur-merged (inc close-bracket)))
;                ; We're at a subject's initial [
;                (let [[ignore close-dirs dirs-txt] (matching-bracket [cur-merged] 0 (+ close-bracket 2))]
;                  (recur (subs cur-merged close-dirs) (+ cur-count (dec open-bracket) txt-count)))
;                ; Not a directives list
;                (recur (subs cur-merged close-bracket) (+ cur-count (dec open-bracket) txt-count)))))
          )))))

(defn split-directives-list [directives-string]
  (if (empty? directives-string) 
    []
    (let [[line char] (next-open-bracket [directives-string] 0 0 \()]
      (if (nil? char) 
        [directives-string]
        (let [[close-line close-char directive] (matching-bracket [directives-string] line char)]
          (cons directive 
                (split-directives-list (subs directives-string (inc close-char) (count directives-string)))))))))

(defn- attach-directives [template root node dir-map]
  (let [template-processed-children
        (reduce
          (fn [cur-template child] (attach-directives cur-template root child dir-map))
          template
          (damp.ekeko.snippets.snippet/snippet-node-children|conceptually-refs template node))
        
        
        [start length]
        (cond
          ; Skip primitive wrappers, since their parent already is an equivalent SimpleName, NumberLiteral, ...
          (damp.ekeko.jdt.astnode/primitivevalue? node)
          [-1 -1]
          (damp.ekeko.jdt.astnode/nilvalue? node)
          [-1 -1]
          (damp.ekeko.jdt.astnode/lstvalue? node)
          (let [elements (damp.ekeko.snippets.snippet/snippet-node-children|conceptually-refs template node)]
            (if (empty? elements)
              [-1 -1] ; Tricky to determine the location of something that isn't there... To attach directives to an empty list, attach it to e.g. a wildcard
              (let [start (.getExtendedStartPosition root (first elements))
                    end (+ (.getExtendedStartPosition root (last elements)) (.getExtendedLength root (last elements)))]
                [start (- end start)])))
          :rest
          [(.getExtendedStartPosition root node) (.getExtendedLength root node)])
        dirs-txt (get dir-map [start length])]
    (reduce 
      (fn [template dir-txt]
        (let [dir-parts (clojure.string/split dir-txt #" ")
              dir-name (let [alias-lookup (get directive-aliases (first dir-parts))]
                         (if (nil? alias-lookup) (first dir-parts) alias-lookup))
              directive (some
                          (fn [dir] (if (= dir-name (damp.ekeko.snippets.directives/directive-name dir)) dir))
                          (damp.ekeko.snippets.matching/registered-directives))
              operands (damp.ekeko.snippets.directives/directive-operands directive)
              operand-values (rest dir-parts)]
          (damp.ekeko.snippets.snippet/add-bounddirective 
            template 
            node
            (damp.ekeko.snippets.directives/make-bounddirective 
              directive
              (cons (damp.ekeko.snippets.operators/make-directiveoperandbinding-for-match node)
                      (for [i (range 0 (count operands))]
                        (damp.ekeko.snippets.directives/make-directiveoperand-binding (nth operands i) (nth operand-values i)))
                      )))))
      template-processed-children
      (split-directives-list dirs-txt))))

(defn parse-var-defs
  "Given a template group (in text form), parse the variable declarations (if any) at the top.
   First, it returns a map, mapping each variable name to a list of possible values.
   Second, it returns the template group, without the variable declarations section."
  [templategroup-string]
  (loop [cur-lines (clojure.string/split-lines templategroup-string)
         cur-var nil
         cur-values nil
         cur-valuelist []]
    (let [cur-line (if (> (count cur-lines) 0)
                     (clojure.string/trim (first cur-lines)))]
      (cond
        (or (nil? cur-line) (= cur-line "---")) ; Reached the end
        (if (empty? cur-var) 
          [{} templategroup-string] ; If there were no variable declarations
          (let 
            [final-valuelist (conj cur-valuelist [cur-var cur-values])
             ; Can't simply eval each variable's value separately, as we allow one variable to be defined in terms of others
             process-string (str "(let ["
                                 (apply str 
                                        (for [[var val] final-valuelist]
                                          (str var " " val "\n")))
                                 "] (zipmap ["
                                 (apply str 
                                        (for [[var val] final-valuelist]
                                          (str "\"" var "\" ")))
                                 "] ["
                                 (apply str 
                                        (for [[var val] final-valuelist]
                                          (str var " ")))
                                 "]))")
             processed-valuemap (load-string process-string)]
            [processed-valuemap (clojure.string/join "\n" (rest cur-lines))]))
        
        (.startsWith cur-line "?") ; Start of a new variable declaration
        (let [new-valuelist (if (nil? cur-var) []
                              (conj cur-valuelist [cur-var cur-values]))
              index (clojure.string/index-of cur-line \=)
              new-var (clojure.string/trim (subs cur-line 0 index))
              new-value (subs cur-line (inc index))]
          (recur (rest cur-lines) new-var new-value new-valuelist))
        
        :rest
        (recur (rest cur-lines) cur-var (str cur-values cur-line) cur-valuelist)
        )
      )
    ))

(defn parse-java-with-wildcards-and-metavars 
  "Try to parse a snippet of plain Java code, which may still contain wildcards and metavariables.
   Returns a pair:
    - The parsed AST
    - The given directives-map, but extended with any wildcards or metavariables that were found.
      Note that the location of any existing directives can shift, because the code was modified 
      to replace any wildcards or metavariables with valid Java syntax."
  [code-lines directives-map]
  (let [all-parse-types [ASTParser/K_CLASS_BODY_DECLARATIONS
                         ASTParser/K_STATEMENTS
                         ASTParser/K_COMPILATION_UNIT
                         ASTParser/K_EXPRESSION]
        
        parse-type 
        (some 
          (fn [ptype]
            (let [parse-result (parsing/jdt-parse-string (join-lines code-lines) ptype)
                  errors (.getProblems (.getRoot parse-result))]
              (if (or
                    (= 0 (count errors))
                    ; If the error is due to a wildcard/metavariable, that's OK .. assuming the template doesn't have actual syntax errors!
                    (let [error (first errors)
                          line (dec (.getSourceLineNumber error))
                          pos (dec (.getSourceColumnNumber error)) ; Column no. starts at 1 in Eclipse
                          is-wcard (= "..." (subs (nth code-lines line) pos (+ pos 3)))
                          is-metavar (not (nil? (re-find #"^\?[a-z0-9_]*" (subs (nth code-lines line) pos))))]
                      (or is-wcard is-metavar)))
                ptype)))
          all-parse-types)
        
        matcher (re-matcher #"(\?[a-z0-9_]*)|(\.\.\.)" (join-lines code-lines))
        
        matches 
        (loop [match-map {}]
          (if (.find matcher)
            (recur (assoc match-map (.group matcher) (.start matcher)))
            match-map))
        
        ; If a wildcard/metavariable is used in a template, we need to replace it with valid Java syntax to make it parseable by Eclipse
        ; This list contains a list of possible things to try and replace it with.      
        placeholders ["x" ";" "{}" "public void _x_ () {}" "public class _X_ {}" "import java.util.Vector;"]
        
        shift-matches ; Adjust matches to the fact that char-shift characters were added/removed at pos-from-start
        (fn [matches pos-from-start char-shift]
          (into {} 
                (map 
                  (fn [[name pos]]
                    (if (< pos-from-start pos) ; If the shift happens before this match
                      [name (+ pos char-shift)]
                      [name pos]))
                  matches)))
        
        shift-dirmap ; Adjust dir-map to the fact that char-shift characters were added/removed at pos-from-start
        (fn [dir-map pos-from-start char-shift]
          (into {}
                (map
                  (fn [[[pos length] dir]]
                    (cond
                      (< pos-from-start pos) ; If the shift happens before this directive
                      [[(+ pos char-shift) length] dir]
                      (< pos-from-start (+ pos length)) ; If the shift happens inside the directive's subject
                      [[pos (+ length char-shift)] dir]
                      :else
                      [[pos length] dir]))
                  dir-map)))
        
        ; Go over each wildcard/metavariable, replace it with valid syntax, and update the directives-map
        [final-lines final-dir-map final-matches]
        (reduce
          (fn [[lines dir-map matches] wcard-mvar-name]
            (some 
              (fn [placeholder]
                (let [char-shift (- (count placeholder) (count wcard-mvar-name))
                      pos-from-start (get matches wcard-mvar-name)
                      inlined (replace-code lines (line-pos lines pos-from-start) (count wcard-mvar-name) placeholder)
                      parse-result (parsing/jdt-parse-string (join-lines inlined) parse-type)
                      errors (.getProblems (.getRoot parse-result))]
                  (if (or (= 0 (count errors))
                          (> (.getSourceStart (first errors)) pos-from-start))
                    (let [shifted-dir-map (shift-dirmap dir-map pos-from-start char-shift)
                          dir-map-with-var (if (= wcard-mvar-name "...")
                                             (assoc shifted-dir-map [pos-from-start (count placeholder)] "replaced-by-wildcard")
                                             (assoc shifted-dir-map [pos-from-start (count placeholder)] (str "(replaced-by-variable " wcard-mvar-name ")")))] 
                      [inlined dir-map-with-var (shift-matches matches pos-from-start char-shift)]))))
              placeholders))
          [code-lines directives-map matches]
          (keys matches))]
    [(parsing/extract-unit (parsing/jdt-parse-string (join-lines final-lines) parse-type) parse-type)
     final-dir-map]
    
    ))

(defn parse-template
  "Parses a template in text form
   A few shorthands available in text form:
   - If there's only one directive, parentheses are optional
   - There are shorter aliases available for some directive names; see directive-aliases
   There are a few caveats to take into account:
   - Replacing by wildcard or by metavariable must be written in the [code]@[directives] notation!
   - Must distinguish between a list and a list element"
  [template-string]
  (let [init-lines (clojure.string/split-lines template-string)
        lines (hide-comments init-lines)
        [stripped-lines directives-map]
        (loop [cur-line 0
               cur-char 0
               cur-lines lines
               cur-dirs {}]
          (let [[open-line open-char] (next-open-bracket cur-lines cur-line cur-char \[)]
            (cond 
              ; Reached the end
              (nil? open-char)
              [cur-lines cur-dirs]
              
              ; Reached a directives list; retrieve it, then remove it from the template text
              (let [[close-line close-char txt] (matching-bracket cur-lines open-line open-char)]
                (= \@ (ch cur-lines close-line (inc close-char))))
              (let [[close-line close-char txt] (matching-bracket cur-lines open-line open-char)
                    [dir-line dir-char directives-txt] (matching-bracket cur-lines close-line (+ close-char 2))
                    
                    [open-ext-line open-ext-char] (next-non-whitespace cur-lines open-line open-char true)
                    [close-ext-line close-ext-char] (next-non-whitespace cur-lines close-line close-char false)
                    
                    extended-position (if (= (inc open-ext-char) (count (nth cur-lines open-ext-line))) 
                                        (inc (position-from-start cur-lines open-ext-line open-ext-char)) ; If at the end of a line, +1 to include the newline char
                                        (position-from-start cur-lines open-ext-line open-ext-char))
                    
                    extended-length (length-without-directives cur-lines open-ext-line open-ext-char close-ext-line close-ext-char)
                    new-dirs (assoc cur-dirs [extended-position extended-length] directives-txt)
                    new-cur-lines (-> cur-lines
                                    (remove-from-lines close-line close-char dir-line (inc dir-char))
                                    (remove-from-lines open-line open-char open-line (inc open-char)))]
                (recur open-line open-char new-cur-lines new-dirs))
              
              ; Not a directives list; moving on
              :rest
              (recur open-line (inc open-char) cur-lines cur-dirs))))
        [ast final-dir-map] (parse-java-with-wildcards-and-metavars stripped-lines directives-map)
        snippet (damp.ekeko.snippets.matching/jdt-node-as-snippet ast)]
    (attach-directives 
      snippet 
      (.getRoot ast)
      (damp.ekeko.snippets.snippet/snippet-root snippet)
      final-dir-map)))


(defn parse-templategroup
  "Parses an Ekeko/X template group.
   Templates are separated by '---' lines.
   Optionally, variable definitions can be provided at the top of the template group."
  ([templategroup-string]
    (parse-templategroup templategroup-string (str "Template group " (java.lang.System/nanoTime))))
  ([templategroup-string name]
    (let [lines (clojure.string/split-lines templategroup-string)]
      (loop [cur-lines lines
             cur-line 0
             cur-templategroup []]
        (if (>= cur-line (count cur-lines))
          (snippetgroup/make-snippetgroup 
            name 
            (conj cur-templategroup (parse-template (clojure.string/join "\n" cur-lines))))
          (let [contents (clojure.string/trim (nth lines cur-line))]
            (if (= contents "---")
              (recur 
                (drop (inc cur-line) cur-lines)
                0
                (conj cur-templategroup (parse-template (clojure.string/join "\n" (take cur-line cur-lines)))))
              (recur 
                cur-lines
                (inc cur-line)
                cur-templategroup))))))))

(defn parse-transformation 
  "Parses an Ekeko/X transformation. The LHS and RHS template groups are separated with a '=>' line."
  [transformation-string]
  (let [lines (clojure.string/split-lines transformation-string)]
    (loop [cur-line 0]
      (if (>= cur-line (count lines))
        nil ; No => was found..
        (let [contents (clojure.string/trim (nth lines cur-line))]
          (if (= contents "=>")
            (transformation/make-transformation 
              (parse-templategroup (clojure.string/join "\n" (take cur-line lines)))
              (parse-templategroup (clojure.string/join "\n" (drop (inc cur-line) lines))))
            (recur (inc cur-line))))))))

(comment
  (parse-template (util/clipboard-string))
  (parse-templategroup (util/clipboard-string))
  (parse-template "[System.[out]@[eq ?x].println(\"Hello world\");]@[eq ?y]")
  (parse-template "System.[out]@[eq ?x].println(\"Hello world\");")
  (def templ
    (parse-templategroup "class\n hello  \n    {public void\n foo () { \n [  \n[int]@[(var ?hello)] [bla]@[...] = 5; 
int foo;]@[set]
	}
}"))
  (inspector-jay.core/inspect (parse-templategroup (util/clipboard-string)))
  
  (matching-bracket ["[System.[out]@[eq ?x].println(\"Hello world\");]@[eq ?y]"] 0 0)
  (length-without-directives "bla[b[blfa]@[bla]la]@[bla]bla")
  )