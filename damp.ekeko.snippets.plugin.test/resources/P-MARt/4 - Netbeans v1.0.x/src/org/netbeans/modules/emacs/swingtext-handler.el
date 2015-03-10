;;; -*- eval: (put 'swingtext-with-buffer 'lisp-indent-function 1) -*-

(require 'cl)
(require 'swingtext-vars)
(require 'swingtext-common)
(require 'swingtext-protocol)

(defun swingtext-cmd-create (bufnum)
  (let ((buf (generate-new-buffer "*SwingText new buffer*")))
    (let ((pair (assq bufnum *swingtext-buffers*)))
      (if pair
          (progn
            (message "Warning--replacing buffer for index #%S" bufnum)
            (rplacd pair buf))
        (push (cons bufnum buf) *swingtext-buffers*)))
    (save-excursion
      (set-buffer buf)
      (setq swingtext-buffer-number bufnum)
      (swingtext-hooks-and-stuff))))

(defun swingtext-cmd-close (bufnum)
  (swingtext-debug "To close: %S..." bufnum)
  (let ((pair (assq bufnum *swingtext-buffers*)))
    (if pair
        (progn
          (let ((buf (cdr pair)))
            (when (not (buffer-name buf)) (error "Buffer %S was already dead" bufnum))
            (swingtext-cmd-setLocAndSize bufnum 0 0 0 0)
            (when (buffer-modified-p buf)
              (save-excursion
                (swingtext-debug "Turning off mod status...")
                (set-buffer buf)
                (set-buffer-modified-p nil)))
            (swingtext-debug "Killing buf...")
            (let ((swingtext-suppress-killhooks t))
              (kill-buffer buf)))
          (setq *swingtext-buffers* (delq pair *swingtext-buffers*))
          (swingtext-debug "Finished closing."))
      (swingtext-debug "Buffer %S had already been closed." bufnum))))

(defun swingtext-fun-getLength (bufnum)
  (swingtext-with-buffer bufnum
    (list (buffer-size))))

(defun swingtext-cmd-startDocumentListen (bufnum)
  (swingtext-with-buffer bufnum
    (setq swingtext-doc-listen t)))

(defun swingtext-cmd-stopDocumentListen (bufnum)
  (condition-case nil
      (swingtext-with-buffer bufnum
        (setq swingtext-doc-listen nil))
    (swingtext-nobuffer-error nil)))

(defun swingtext-cmd-startCaretListen (bufnum)
  (swingtext-with-buffer bufnum
    (setq swingtext-caret-listen t)))

(defun swingtext-cmd-stopCaretListen (bufnum)
  (swingtext-with-buffer bufnum
    (setq swingtext-caret-listen nil)))

(defun swingtext-cmd-ignore (bufnum what)
  (swingtext-debug "Ignoring command %S" what))

(defun swingtext-cmd-setTitle (bufnum title)
  (swingtext-with-buffer bufnum
    (rename-buffer title t)
    ;; Set visited name, so that auto-mode-alist etc. will be used in preference to MIME type:
    (setq buffer-file-name title)
    (swingtext-update-mode)))

(defun swingtext-fun-remove (bufnum pos length)
  (swingtext-with-buffer bufnum
    (when swingtext-doc-pending
      (error "Pending document changes in Emacs were not yet fired when external remove request received"))
    (let ((swingtext-nofire t)
          (inhibit-read-only (or inhibit-read-only (not swingtext-as-user))))
      (cond ((> (+ pos length) (buffer-size)) (list "Buffer pos too large" (+ pos length)))
            (t (condition-case err
                   (progn
                     (delete-region (1+ pos) (+ pos length 1))
                     (setq buffer-undo-list nil))
                 (swingtext-guarded-error (list "Attempt to remove from a read-only area" (1- (cadr err))))
                 (buffer-read-only (list "Attempt to remove from a read-only area" pos))))))))

(defun swingtext-fun-insert (bufnum pos text)
  (swingtext-with-buffer bufnum
    (when swingtext-doc-pending
      (error "Pending document changes in Emacs were not yet fired when external insert request received"))
    (let ((swingtext-nofire t)
          (inhibit-read-only (or inhibit-read-only (not swingtext-as-user))))
      (cond ((> pos (buffer-size)) (list "Buffer pos too large" pos))
            (t (condition-case err
                   (save-excursion
                     (goto-char (1+ pos))
                     (insert text)
                     (setq buffer-undo-list nil))
                 (swingtext-guarded-error (list "Attempt to insert into read-only area" (1- (cadr err))))
                 (buffer-read-only (list "Attempt to insert into read-only area" pos))))))))

(defun swingtext-fun-getText (bufnum pos length)
  (swingtext-with-buffer bufnum
    (cond ((> (+ pos length) (buffer-size)) (list "Buffer pos too large" (+ pos length)))
          (t (list (buffer-substring-no-properties (1+ pos) (+ pos length 1)))))))

(defun swingtext-fun-createPosition (bufnum which pos forward-bias)
  (swingtext-with-buffer bufnum
    (let ((pair (assq which *swingtext-positions*))
          (marker (make-marker)))
      (set-marker marker (1+ pos))
      (set-marker-insertion-type marker forward-bias)
      (if pair
          (progn
            (message "Warning--replacing position #%S" which)
            (rplacd pair marker))
        (push (cons which marker) *swingtext-positions*))
;;; XXX does not currently trap bad positions
      nil)))

(defun swingtext-fun-lookupPosition (bufnum which)
  (swingtext-with-buffer bufnum
;;; XXX trap missing position
    (list (1- (marker-position (cdr (assq which *swingtext-positions*)))))))

(defun swingtext-cmd-destroyPosition (bufnum which)
  (swingtext-with-buffer bufnum
    (let ((pair (assq which *swingtext-positions*)))
      (when (not pair) (error "Marker #%S did not exist to destroy!"))
      (setq *swingtext-positions* (delq pair *swingtext-positions*)))))

(defun swingtext-fun-countLines (bufnum)
  (swingtext-with-buffer bufnum
    (list (count-lines 1 (1+ (buffer-size))))))

(defun swingtext-fun-findLineFromOffset (bufnum pos)
  (swingtext-with-buffer bufnum
    (list (if (= pos (buffer-size))
              (count-lines 1 (1+ pos))
            (1- (count-lines 1 (+ pos 2)))))))

(defun swingtext-fun-getLineStartOffset (bufnum line)
  (swingtext-with-buffer bufnum
    (save-excursion
      (goto-char 1)
      (forward-line line)
      (list (1- (point))))))

(defun swingtext-cmd-setContentType (bufnum type)
  (swingtext-with-buffer bufnum
    (setq swingtext-mime-type type)
    (swingtext-update-mode)))

(defun swingtext-cmd-setDot (bufnum pos)
  (let ((curr (current-buffer))
        (swingtext-nofire t))
    (set-buffer (swingtext-get-buffer bufnum))
    (unwind-protect
        (progn
          (goto-char (1+ pos))
          (when (and (swingtext-the-mark) (= (mark) (point)))
            (swingtext-deactivate-mark))
          (swingtext-update-dot))
      (set-buffer curr))))

(defun swingtext-cmd-setMark (bufnum pos)
  (let ((curr (current-buffer))
        (swingtext-nofire t))
    (set-buffer (swingtext-get-buffer bufnum))
    (unwind-protect
        (progn
          (set-mark (1+ pos))
          (when (and (swingtext-the-mark) (= (mark) (point)))
            (swingtext-deactivate-mark))
          (swingtext-update-mark))
      (set-buffer curr))))

(defun swingtext-fun-getDot (bufnum)
  (swingtext-with-buffer bufnum
    (swingtext-update-dot)
    (list (1- (point)))))

(defun swingtext-fun-getMark (bufnum)
  (swingtext-with-buffer bufnum
    (swingtext-update-mark)
    (list (if (swingtext-the-mark)
              (1- (mark))
            (1- (point))))))

;; Try to find where this buffer is, and show it on an appropriate frame.
(defun swingtext-cmd-setLocAndSize (bufnum x y w h)
;;; XXX should be much more smart about deiconifying, etc.
  (swingtext-debug "setLocAndSize %S %S %S %S %S" bufnum x y w h)
  (let* ((buf (swingtext-get-buffer bufnum))
         (the-win (get-buffer-window buf 'visible)))
    (if (and (= x 0) (= y 0) (= w 0) (= h 0))
        (progn
          (swingtext-debug "Burying %S..." buf)
          (bury-buffer buf)
          (swingtext-debug "Replacing...")
          (replace-buffer-in-windows buf)
          (swingtext-debug "Replaced.")
          (when (and the-win (eq the-win (next-window the-win 'no-minibuf 'just-this-frame)))
            (swingtext-debug "Have a win %S and sole one" the-win)
            (swingtext-debug "Will kill frame...")
            (delete-frame (window-frame the-win))
            (swingtext-debug "Killed.")))
      (progn
        (unless the-win
          (setq the-win (frame-selected-window (make-frame)))
          (set-window-buffer the-win buf))
        (let ((the-frame (window-frame the-win)))
          ;; XXX more annoying than useful:
;           (set-frame-position the-frame
;                               (+ x swingtext-frame-x-adjust)
;                               (+ y swingtext-frame-y-adjust))
;           (set-frame-width the-frame
;                            (/ (+ w swingtext-frame-width-adjust (- swingtext-frame-x-adjust)) (frame-char-width the-frame)))
;           (set-frame-height the-frame
;                             (/ (+ h swingtext-frame-height-adjust (- swingtext-frame-y-adjust)) (frame-char-height the-frame)))
          (select-frame the-frame)
          (raise-frame the-frame))))))

(defun swingtext-cmd-guard (bufnum off len)
  (let ((roff (1+ off)))
    (swingtext-with-buffer bufnum
      (swingtext-debug "swingtext-cmd-guard %s %s %S" roff len (buffer-substring-no-properties roff (+ roff len)))
      (if (boundp 'running-xemacs)
          (let ((ext (make-extent roff (+ roff len))))
            (set-extent-properties ext (symbol-plist 'swingtext-guarded)))
        (let ((ov (make-overlay roff (+ roff len) (current-buffer) t nil)))
          (overlay-put ov 'category 'swingtext-guarded))))))

;;; XXX currently does not seem to be called, but should still be handled...
(defun swingtext-cmd-unguard (bufnum off len)
  (let ((roff (1+ off)))
    (swingtext-with-buffer bufnum
      (swingtext-debug "swingtext-cmd-unguard %s %s %S" roff len (buffer-substring-no-properties roff (+ roff len)))
      (error "Unguarding not yet implemented"))))

(defun swingtext-cmd-setModified (bufnum mod)
  (swingtext-with-buffer bufnum
    (set-buffer-modified-p (setq swingtext-is-modified mod))))

(defun swingtext-cmd-startAtomic (bufnum)
  (incf swingtext-atomic-level))

(defun swingtext-cmd-endAtomic (bufnum)
  (decf swingtext-atomic-level))

(defun swingtext-cmd-setAsUser (bufnum asuser)
  (swingtext-with-buffer bufnum
    (setq swingtext-as-user asuser)))

(defun swingtext-cmd-setStyle (bufnum pos name)
  (swingtext-with-buffer bufnum
    (let* ((rpos (1+ pos))
           (style (cdr (assoc name '(("breakpoint" . swingtext-breakpoint-style)
                                     ("current" . swingtext-current-style)
                                     ("error" . swingtext-error-style)))))
           replacing
           (bol (save-excursion
                  (goto-char rpos)
                  (beginning-of-line 1)
                  (point)))
           (eol (save-excursion
                  (goto-char rpos)
                  (beginning-of-line 2)
                  (point))))
      (if (boundp 'running-xemacs)
          (let ((extent (extent-at rpos (current-buffer) 'style nil 'after)))
            (unless extent (when style (setq extent (make-extent bol eol))))
            (when extent
              (if style
                  (set-extent-properties extent (symbol-plist style))
                (delete-extent extent))))
        (progn
          (dolist (ov (overlays-at rpos))
            (let ((categ (overlay-get ov 'category)))
              (when (memq categ '(swingtext-breakpoint-style swingtext-current-style swingtext-error-style))
                (setq replacing t)
                (when (not (eq categ style))
                  (if style
                      (overlay-put ov 'category style)
                    (delete-overlay ov))))))
          (unless replacing
            (when style
              (let ((ov (make-overlay bol eol)))
                (overlay-put ov 'category style)))))))))

(provide 'swingtext-handler)
