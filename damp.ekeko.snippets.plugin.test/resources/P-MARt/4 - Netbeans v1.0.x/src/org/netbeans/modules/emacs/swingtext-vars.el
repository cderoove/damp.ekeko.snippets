(defgroup swing-text nil
  "Customization for Swing Text compatibility package."
  :prefix "swingtext-"
  :group 'files)

;;; XXX the type editor does not work right now...why?
(defcustom swingtext-modes-by-mime-type
;;; XXX add more here--properties, IDL, ...
  '(("text/x-java" . java-mode)
    ("text/plain" . text-mode)
    ("text/html" . html-mode))
  "*Mapping of MIME types to Emacs major modes.
Should be used to supply a major mode to use for a file (or
fragment of text) when the filename or contents do not suffice
for Emacs to guess."
  :type '(list (cons string function))
  :group 'swing-text)

; (defcustom swingtext-frame-x-adjust 0
;   "*Adjustment in pixels to make for the x position of a window."
;   :type 'integer
;   :group 'swing-text)

; (defcustom swingtext-frame-y-adjust 30
;   "*Adjustment in pixels to make for the y position of a window."
;   :type 'integer
;   :group 'swing-text)

; (defcustom swingtext-frame-width-adjust -20
;   "*Adjustment in pixels to make for the width of a window."
;   :type 'integer
;   :group 'swing-text)

; (defcustom swingtext-frame-height-adjust -5
;   "*Adjustment in pixels to make for the height of a window."
;   :type 'integer
;   :group 'swing-text)

(defcustom swingtext-debugging nil
  "*Whether to generate debug messages for SwingText code."
  :type 'boolean
  :group 'swing-text)

(defcustom swingtext-debugging-logfile nil
  "*When debugging, if this is non-nil then log messages will also be appended to the specified filename."
  :type '(choice file (const nil :tag "Disable"))
  :group 'swing-text)

(defface swingtext-guarded-face
  '((((class grayscale) (background light)) (:inverse-video t))
    (((class grayscale) (background dark)) (:inverse-video t))
    (((class color) (background light)) (:background "light blue"))
    (((class color) (background dark)) (:background "dark blue"))
    (t (:inverse-video t)))
  "Face to use for read-only areas."
  :group 'swing-text)

(defface swingtext-current-face
  '((((class grayscale) (background light)) (:bold t))
    (((class grayscale) (background dark)) (:bold t))
    (((class color) (background light)) (:background "magenta"))
    (((class color) (background dark)) (:background "magenta"))
    (t (:bold t)))
  "Face to use for lines current in the debugger."
  :group 'swing-text)

(defface swingtext-error-face
  '((((class grayscale) (background light)) (:bold t))
    (((class grayscale) (background dark)) (:bold t))
    (((class color) (background light)) (:background "red"))
    (((class color) (background dark)) (:background "red"))
    (t (:bold t)))
  "Face to use for lines marked erroneous by the compiler."
  :group 'swing-text)

(defface swingtext-breakpoint-face
  '((((class grayscale) (background light)) (:italic t))
    (((class grayscale) (background dark)) (:italic t))
    (((class color) (background light)) (:background "medium slate blue"))
    (((class color) (background dark)) (:background "medium slate blue"))
    (t (:italic t)))
  "Face to use for lines broken by the debugger."
  :group 'swing-text)



(defvar *swingtext-the-proc* nil
  "Current SwingText connection, if any.")

(defvar *swingtext-buffers* nil
  "Alist from buffer numbers to the actual buffers.")

(defvar *swingtext-positions* nil
  "Alist from position numbers to the actual markers.")

(defvar *swingtext-filter-buffer* nil
  "Buffer of stored strings to process as incoming.")

(defvar *swingtext-sequence-number* nil
  "Last-seen sequence number.")

(defvar *swingtext-doc-timer* nil
  "Idle timer to send insert/delete events from.")

(defvar *swingtext-caret-timer* nil
  "Idle timer to send caret movement events from.")



(defvar swingtext-nofire nil
  "When true (dynamic scope), do not fire changes.")

;; XXX this could also just be an integer (stack depth), ignore all
;; non-top-level changes
(defvar swingtext-change-stack nil
  "Stack of pending change texts, as strings (no text properties).")

(defvar swingtext-atomic-level 0
  "Number of outstanding atomic-runnable requests (possibly in different documents).")

(defvar swingtext-suppress-killhooks nil
  "When true (dynamic scope), do not fire kill events when a buffer is killed.")


(defvar swingtext-buffer-number nil
  "Number of this buffer.")
(make-variable-buffer-local 'swingtext-buffer-number)
(put 'swingtext-buffer-number 'permanent-local t)

(defvar swingtext-mime-type nil
  "MIME type of this buffer, if specified.")
(make-variable-buffer-local 'swingtext-mime-type)
(put 'swingtext-mime-type 'permanent-local t)

(defvar swingtext-doc-listen nil
  "Whether to fire doc changes in this buffer.")
(make-variable-buffer-local 'swingtext-doc-listen)
(put 'swingtext-doc-listen 'permanent-local t)

(defvar swingtext-caret-listen nil
  "Whether to fire caret changes in this buffer.")
(make-variable-buffer-local 'swingtext-caret-listen)
(put 'swingtext-caret-listen 'permanent-local t)

(defvar swingtext-doc-pending nil
  "Pending document event, as a cons.
Car is position; cdr is a string for an insert, an integer (length) for a remove.
Nil if none pending.")
(make-variable-buffer-local 'swingtext-doc-pending)
(put 'swingtext-doc-pending 'permanent-local t)

(defvar swingtext-caret-last nil
  "Last established caret position, as cons of point and mark.
Nil if never specified.")
(make-variable-buffer-local 'swingtext-caret-last)
(put 'swingtext-caret-last 'permanent-local t)

(defvar swingtext-is-modified nil
  "Whether a buffer should be considered modified.
Only changed as instructed remotely.")
(make-variable-buffer-local 'swingtext-is-modified)
(put 'swingtext-is-modified 'permanent-local t)

(defvar swingtext-as-user nil
  "Locally turned on when in run-as-user mode.")
(make-variable-buffer-local 'swingtext-as-user)
(put 'swingtext-as-user 'permanent-local t)



(put 'swingtext-guarded-error 'error-conditions '(swingtext-guarded-error buffer-read-only error))
(put 'swingtext-guarded-error 'error-message "You cannot type in this region, it is protected")
(put 'swingtext-nobuffer-error 'error-conditions '(swingtext-nobuffer-error error))
(put 'swingtext-nobuffer-error 'error-message "Buffer not found")

(defvar swingtext-keymap (make-sparse-keymap))
(define-key swingtext-keymap [pause] 'swingtext-keyboard-event)

(pushnew '(swingtext-buffer-number " Remote-Buffer") minor-mode-alist :test 'equal)
(pushnew (cons 'swingtext-buffer-number swingtext-keymap) minor-mode-map-alist :test 'equal)

;;; XXX give options to use before-string/after-string instead of faces
;; Common
(put 'swingtext-guarded 'face 'swingtext-guarded-face)
(put 'swingtext-guarded 'priority 0)
(put 'swingtext-current-style 'face 'swingtext-current-face)
(put 'swingtext-current-style 'priority 1)
(put 'swingtext-error-style 'face 'swingtext-error-face)
(put 'swingtext-error-style 'priority 1)
(put 'swingtext-breakpoint-style 'face 'swingtext-breakpoint-face)
(put 'swingtext-breakpoint-style 'priority 1)
;; FSF Emacs
(put 'swingtext-guarded 'modification-hooks '(swingtext-check-guard))
(put 'swingtext-guarded 'insert-in-front-hooks '(swingtext-check-guard))
(put 'swingtext-guarded 'evaporate t)
(put 'swingtext-current-style 'evaporate t)
(put 'swingtext-error-style 'evaporate t)
(put 'swingtext-breakpoint-style 'evaporate t)
;; XEmacs
(put 'swingtext-guarded 'read-only t)
(put 'swingtext-guarded 'detachable t)
(put 'swingtext-guarded 'start-closed t)
(put 'swingtext-guarded 'end-open t)
(put 'swingtext-current-style 'detachable t)
(put 'swingtext-current-style 'start-closed t)
(put 'swingtext-current-style 'end-open t)
(put 'swingtext-current-style 'style t)
(put 'swingtext-error-style 'detachable t)
(put 'swingtext-error-style 'start-closed t)
(put 'swingtext-error-style 'end-open t)
(put 'swingtext-error-style 'style t)
(put 'swingtext-breakpoint-style 'detachable t)
(put 'swingtext-breakpoint-style 'start-closed t)
(put 'swingtext-breakpoint-style 'end-open t)
(put 'swingtext-breakpoint-style 'style t)

(provide 'swingtext-vars)
