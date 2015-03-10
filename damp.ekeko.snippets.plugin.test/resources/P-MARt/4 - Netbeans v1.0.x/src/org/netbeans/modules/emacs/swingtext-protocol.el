(require 'cl)
(require 'swingtext-vars)
(require 'swingtext-common)

(defun swingtext-connect (host port auth)
  (swingtext-disconnect)
  (let ((proc (open-network-stream "connect" nil host port)))
    (setq *swingtext-filter-buffer* nil)
    (set-process-filter proc 'swingtext-filter)
    (set-process-sentinel proc 'swingtext-sentinel)
    (swingtext-debug "Sending auth %S" auth)
    (process-send-string proc (concat "AUTH " auth "\n"))
;;; XXX set-process-coding-system
    (setq *swingtext-the-proc* proc))
  (setq *swingtext-sequence-number* 0)
  (setq *swingtext-doc-timer* (swingtext-make-idle-timer "SwingText Doc Timer" 0.5 'swingtext-doc-idle-flush))
  (setq *swingtext-caret-timer* (swingtext-make-idle-timer "SwingText Caret Timer" 1.5 'swingtext-caret-idle-flush))
  nil)

(defun swingtext-disconnect ()
  (when *swingtext-the-proc*
    (delete-process *swingtext-the-proc*)
    (setq *swingtext-the-proc* nil))
  (setq *swingtext-sequence-number* nil)
  (when *swingtext-doc-timer*
    (swingtext-cancel-timer *swingtext-doc-timer*)
    (setq *swingtext-doc-timer* nil))
  (when *swingtext-caret-timer*
    (swingtext-cancel-timer *swingtext-caret-timer*)
    (setq *swingtext-caret-timer* nil))
  nil)

(defun swingtext-filter (proc string)
  (swingtext-debug "Got string %S" string)
  (let ((newlines (count ?\n string)))
    (swingtext-debug "Newlines: %S" newlines)
    (if (and (not *swingtext-filter-buffer*) (= newlines 1) (= (aref string (1- (length string))) ?\n))
        ;; Simple case.
        (swingtext-handle-line proc string)
      (progn
        (swingtext-debug "Old buffer: %S" *swingtext-filter-buffer*)
        (setq *swingtext-filter-buffer* (if *swingtext-filter-buffer* (concat *swingtext-filter-buffer* string) string))
        (swingtext-debug "New buffer: %S" *swingtext-filter-buffer*)
;;; XXX could be more efficient
        (let (pos)
          (while (and *swingtext-filter-buffer* (setq pos (position ?\n *swingtext-filter-buffer*)))
            (swingtext-debug "Current buffer: %S" *swingtext-filter-buffer*)
            (swingtext-debug "Current pos: %S" pos)
            (let ((to-handle (substring *swingtext-filter-buffer* 0 (1+ pos))))
              (setq *swingtext-filter-buffer* (if (= pos (1- (length *swingtext-filter-buffer*)))
                                                  nil
                                                (substring *swingtext-filter-buffer* (1+ pos))))
              (swingtext-debug "Trimmed buffer: %S" *swingtext-filter-buffer*)
              ;; In case of error in the handler, buffer will still have been trimmed.
              (swingtext-handle-line proc to-handle)))))))
  ;; XXX is this really safe to do within a filter function?
  (when (> swingtext-atomic-level 0)
    (unless (accept-process-output *swingtext-the-proc* 5)
      (swingtext-debug "WARNING--more than five seconds elapsed in atomic mode without news!"))))

(defun swingtext-send (string)
  (swingtext-debug "Sending: `%s'" string)
  (process-send-string *swingtext-the-proc* string)
)

(defun swingtext-handle-line (proc string)
  (swingtext-debug "Handling: %S" string)
  (if (string-equal string "DISCONNECT\n")
      (swingtext-disconnect)
    (let ((request (swingtext-parse-request string)))
      (let ((bufnum (car request))
            (cmd (cadr request))
            (isfun (caddr request))
            (seq (cadddr request))
            (args (cddddr request)))
        (setq *swingtext-sequence-number* seq)
        (if isfun
            (let ((sym (intern (concat "swingtext-fun-" cmd))))
              (let ((result (condition-case err
                                (apply sym bufnum args)
                              (error (let ((msg (error-message-string err)))
                                       (swingtext-debug "ERROR during function call: %s" msg)
                                       (list '! msg))))))
                (swingtext-debug "Will send %S %S" seq result)
                (swingtext-send (swingtext-create-response seq result))))
          (let ((sym (intern (concat "swingtext-cmd-" cmd))))
            (condition-case err
                (apply sym bufnum args)
              (error (swingtext-debug "ERROR during command call: %s" (error-message-string err))))))))))

(defun swingtext-parse-request (string)
  (unless (string-match "^\\([0-9]+\\):\\([a-zA-z_]+\\)\\([/!]\\)\\([0-9]+\\)\\(\\( \\(T\\|F\\|[0-9]+\\|\"\\([^\\\\\"\n]\\|\\\\\\\\\\|\\\\\"\\|\\\\n\\|\\\\r\\)*\"\\)\\)*\\)\n" string)
    (setq *swingtext-filter-buffer* nil)
    (swingtext-debug "MISPARSE: %S" string)
    (error "Malformed request: %S" string))
  (let ((res (list (string-to-number (match-string 1 string))
                   (match-string 2 string)
                   (string-equal "/" (match-string 3 string))
                   (string-to-number (match-string 4 string)))))
    (let ((args (match-string 5 string)))
      (let ((quickie (read (concat "(" args ")"))))
        (let ((filtered (substitute t 'T (substitute nil 'F quickie))))
          (nconc res filtered)
          res)))))

(defun swingtext-create-response (seq args)
  (concat (number-to-string seq) (swingtext-create-arguments args) "\n"))

(defun swingtext-create-event (bufnum cmd args)
  (concat (number-to-string bufnum) ":" cmd "="
          (number-to-string *swingtext-sequence-number*) (swingtext-create-arguments args) "\n"))

(defun* swingtext-create-arguments (args)
  (swingtext-debug "swingtext-create-arguments %S" args)
  (if args
      (let ((filtered (substitute 'T t (substitute 'F nil args))))
        (let ((almost (prin1-to-string filtered)))
          (setq almost (substring almost 1 (1- (length almost))))
          (let ((result "")
;                (end (length almost))
                (pos 0))
            (while t
              (let* ((uptonl (position ?\n almost :start pos))
                     (uptocr (position ?\r almost :start pos))
                     (upto (cond
                            ((and (not uptonl) (not uptocr)) nil)
                            ((and uptonl (not uptocr)) (cons uptonl t))
                            ((and (not uptonl) uptocr) (cons uptocr nil))
                            ((and uptonl uptocr) (if (< uptonl uptocr) (cons uptonl t) (cons uptocr nil))))))
                (if upto
                    (progn
                      (setq result (concat result (substring almost pos (car upto)) (if (cdr upto) "\\n" "\\r")))
                      (setq pos (1+ (car upto))))
                    (return-from swingtext-create-arguments (concat " " result (substring almost pos)))))))))
    ""))

(defun swingtext-sentinel (process event)
  (swingtext-debug "Process %S received event %S" process event)
  (swingtext-disconnect))

(defun* swingtext-event (bufnum cmd &rest args)
  (swingtext-debug "Event %S %S %S" bufnum cmd args)
  (swingtext-send (swingtext-create-event bufnum cmd args)))

(provide 'swingtext-protocol)
