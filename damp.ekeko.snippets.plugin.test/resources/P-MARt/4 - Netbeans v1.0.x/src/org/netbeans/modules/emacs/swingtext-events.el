(require 'cl)
(require 'swingtext-vars)
(require 'swingtext-protocol)
(require 'swingtext-common)

(defun swingtext-kill-hook ()
  (unless swingtext-suppress-killhooks
    (swingtext-debug "swingtext-kill-hook on %S" swingtext-buffer-number)
    (swingtext-event swingtext-buffer-number "killed")
    (setq *swingtext-buffers*
          (delq (assq swingtext-buffer-number *swingtext-buffers*)
                *swingtext-buffers*))))

(defun swingtext-before-change (beg end)
  (let ((old-chg-text (if (= beg end) nil (buffer-substring-no-properties beg end))))
    (swingtext-debug "swingtext-before-change %s %s %S" beg end old-chg-text)
;;; XXX this is not too good...if swingtext-check-guards signals an error,
;;; the stack will grow needlessly and never be cleared
    (push old-chg-text swingtext-change-stack)))

(defun swingtext-after-change (beg end prevlen)
  (unless swingtext-nofire
;    (swingtext-debug "swingtext-after-change %s %s %s" beg end prevlen)
    (let ((new-chg-text (if (= beg end) nil (buffer-substring-no-properties beg end))))
      (swingtext-debug "new-chg-text=%S" new-chg-text)
      (if (equal new-chg-text (pop swingtext-change-stack))
          (swingtext-debug "ignoring textprops-only change")
        (progn
          (when (not (zerop prevlen))
            (swingtext-append-change (cons (1- beg) prevlen)))
          (when new-chg-text
            (swingtext-append-change (cons (1- beg) new-chg-text)))))))
  (set-buffer-modified-p swingtext-is-modified))

(defun swingtext-append-change (chg)
  (let ((merged (swingtext-merge-changes swingtext-doc-pending chg)))
    (swingtext-debug "swingtext-append-change: merging %S + %S = %S" swingtext-doc-pending chg merged)
    (if merged
        (progn
          (setq swingtext-doc-pending merged))
      (progn
        (swingtext-send-chg swingtext-doc-pending)
        (setq swingtext-doc-pending chg)))))

(defun swingtext-merge-changes (chg1 chg2)
  (if (not chg1)
      chg2
    (if (not chg2)
        chg1
      (let ((is-r-1 (numberp (cdr chg1)))
            (is-r-2 (numberp (cdr chg2)))
            (pos-1 (car chg1))
            (pos-2 (car chg2)))
        ;; XXX many more merges are possible, focussing here only on
        ;; common case of user typing continuously -> many 1-char
        ;; inserts
        (if (and (not is-r-1) (not is-r-2)
                 (= pos-2 (+ pos-1 (length (cdr chg1)))))
            (cons pos-1 (concat (cdr chg1) (cdr chg2)))
          nil)))))

(defun swingtext-send-chg (chg)
;  (swingtext-debug "swingtext-send-chg chg=%S swingtext-doc-listen=%S" chg swingtext-doc-listen)
  (when (and swingtext-doc-listen chg)
    (swingtext-event swingtext-buffer-number (if (numberp (cdr chg)) "remove" "insert") (car chg) (cdr chg))))

(defun swingtext-doc-idle-flush ()
;  (swingtext-debug "swingtext-doc-idle-flush")
  (save-excursion
;    (swingtext-debug "swingtext-doc-idle-flush: *swingtext-buffers*=%S" *swingtext-buffers*)
    (dolist (pair *swingtext-buffers*)
      (set-buffer (cdr pair))
;      (swingtext-debug "swingtext-doc-idle-flush: pair=%S swingtext-doc-pending=%S" pair swingtext-doc-pending)
      (when swingtext-doc-pending
;        (swingtext-debug "swingtext-doc-idle-flush: have changes to send")
        (swingtext-send-chg swingtext-doc-pending)
        (setq swingtext-doc-pending nil)))))
;    (swingtext-debug "swingtext-doc-idle-flush (done)")))

(defun swingtext-caret-idle-flush ()
;  (swingtext-debug "swingtext-caret-idle-flush")
  (let (tokill)
    (save-excursion
      (dolist (pair *swingtext-buffers*)
        (let ((buffer (cdr pair)))
          (if (buffer-name buffer)
              (progn
                (set-buffer buffer)
                (let ((caret (cons (1- (point)) (1- (if (swingtext-the-mark) (mark) (point))))))
                  (when (and swingtext-caret-listen (not (equal caret swingtext-caret-last)))
;          (swingtext-debug "swingtext-caret-idle-flush %S -> %S" swingtext-caret-last caret)
                    (swingtext-event swingtext-buffer-number "newDotAndMark" (car caret) (cdr caret))
                    (setq swingtext-caret-last caret))))
            (push pair tokill)))))
    (dolist (kill tokill)
      (swingtext-debug "Removing dead buffer from *swingtext-buffers*: %S" kill)
      (setq *swingtext-buffers* (delq kill *swingtext-buffers*)))))

(defun swingtext-keyboard-event ()
  (interactive)
  (let* ((prompt "Key to send to remote application: ")
         (ev (if (boundp 'running-xemacs)
                 (next-command-event nil prompt)
               (read-event prompt)))
         (mods (event-modifiers ev))
         (type (if (boundp 'running-xemacs)
                   (event-key ev)
                 (event-basic-type ev))))
    (swingtext-event swingtext-buffer-number "keyCommand" (swingtext-encode-key type mods))))

;; XXX make customizable?
(defconst swingtext-key-encoding-mapping
  '(
    (?& . ampersand)
    (?* . asterisk)
    (?@ . at)
    (?` . back_quote)
    (?\\ . back_slash)
    (?{ . brace_left)
    (?} . brace_right)
    (?^ . circumflex)
    (?] . close_bracket)
    (?: . colon)
    (?, . comma)
    ;; delete
    (?/ . divide)
    (?$ . dollar)
    ;; down
    ;; end
    (return . enter)
    (?= . equals)
    ;; escape
    (?! . exclamation_mark)
    ;; f1, etc.
    (?> . greater)
    ;; home
    ;; insert
    ;; left
    (?( . left_parenthesis)
    (?< . less)
    (?- . minus)
    (?# . number_sign)
    (?[ . open_bracket)
    (next . page_down)
    (prior . page_up)
    ;; pause
    (?. . period)
    (?+ . plus)
    (?' . quote)
    (34 . quotedbl) ; ?"
    (?) . right_parenthesis)
    (59 . semicolon) ; ?;
    (?/ . slash)
    (?  . space)
    ;; tab
    (?_ . underscore)
    ;; up
    ;; XXX tilde, percent-sign, pipe, question-mark all send shift-something
    ;; and thus will need to be specially handled
    ;; XXX have not bothered with e.g. kp_down and so on
    ;; XXX backspace vs. delete...
    )
  "Bindings from ASCII characters or Emacs key event symbols, to the proper Java VK_* suffixes.
Not necessary if the symbol/char already stands for itself.
Everything is automatically upcased as is required.")

(defun swingtext-encode-key (type mods)
  (swingtext-debug "swingtext-encode-key %S %S" type mods)
  (let ((modstring (mapconcat (lambda (mod)
                                (cdr (assq mod '((meta . "M") (control . "C") (shift . "S") (alt . "A")))))
                              mods nil)))
    (concat modstring (if (string-equal modstring "") nil "-")
            (let ((basekey (or (cdr (assq type swingtext-key-encoding-mapping)) type)))
              (if (numberp basekey)
                  (char-to-string (upcase basekey))
                (upcase (symbol-name basekey)))))))

(provide 'swingtext-events)
