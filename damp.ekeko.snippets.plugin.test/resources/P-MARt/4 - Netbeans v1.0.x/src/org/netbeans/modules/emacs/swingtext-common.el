(require 'cl)
(require 'swingtext-vars)

(defun swingtext-get-buffer (bufnum)
  (let ((pair (assq bufnum *swingtext-buffers*)))
    (when (not pair) (signal 'swingtext-nobuffer-error (list bufnum)))
    (cdr pair)))

(defmacro swingtext-with-buffer (bufnum &rest body)
  `(save-excursion
     (set-buffer (swingtext-get-buffer ,bufnum))
     ,@body))

(defun swingtext-update-mode ()
  (condition-case err
      (progn
        (normal-mode)
        (when (eq major-mode 'fundamental-mode)
          (let ((mode (cdr (assoc swingtext-mime-type swingtext-modes-by-mime-type))))
            (if (and mode (fboundp mode))
                (funcall mode)))))
    ;; Errors setting the mode are probably not important enough
    ;; to be dealt with harshly (throwing remote exceptions etc.).
    (error (message "%s" (error-message-string err))))
  ;;  (when font-lock-mode (font-lock-fontify-buffer))
  (swingtext-hooks-and-stuff))

(defun swingtext-hooks-and-stuff ()
  (make-local-hook 'after-change-functions)
  (add-hook 'after-change-functions 'swingtext-after-change nil t)
  (make-local-hook 'before-change-functions)
  (add-hook 'before-change-functions 'swingtext-before-change nil t)
  (make-local-hook 'kill-buffer-hook)
  (add-hook 'kill-buffer-hook 'swingtext-kill-hook t)
  ;; Does not look like it is possible to have dot + mark work properly
  ;; unless this mode is turned on:
  (if (boundp 'running-xemacs)
      (progn
        (make-local-variable 'zmacs-regions)
        (setq zmacs-regions t))
    (progn
      (make-local-variable 'transient-mark-mode)
      (transient-mark-mode 1))))

(defun swingtext-update-dot ()
  (let* ((dot (1- (point)))
         (mark (1- (or (swingtext-the-mark) (point)))))
    (if (not swingtext-caret-last)
        (setq swingtext-caret-last (cons dot mark))
      (when (/= dot (car swingtext-caret-last))
        (setq swingtext-caret-last (cons dot (cdr swingtext-caret-last)))))))

(defun swingtext-update-mark ()
  (let* ((dot (1- (point)))
         (mark (1- (or (swingtext-the-mark) (point)))))
    (if (not swingtext-caret-last)
        (setq swingtext-caret-last (cons dot mark))
      (when (/= mark (cdr swingtext-caret-last))
        (setq swingtext-caret-last (cons (car swingtext-caret-last) mark))))))

(defun swingtext-check-guard (overlay after beg end &optional len)
  (when (and (not inhibit-read-only) (not after))
    (signal 'swingtext-guarded-error (list beg))))

(defun swingtext-debug (the-format &rest args)
  (when swingtext-debugging
    (let ((text (apply 'format the-format args)))
      (message "[SwingText] %s" text)
      (when swingtext-debugging-logfile
        (write-region (concat text "\n") nil swingtext-debugging-logfile t 'nomessage)))))

(defun swingtext-make-idle-timer (name time func)
  (if (boundp 'running-xemacs)
      (start-itimer name func time time t)
    (run-with-idle-timer time t func)))

(defun swingtext-cancel-timer (timer)
  (if (boundp 'running-xemacs)
      (delete-itimer timer)
    (cancel-timer timer)))

(defun swingtext-the-mark ()
  (if (boundp 'running-xemacs)
      (mark)
    (if mark-active (mark))))

(defun swingtext-deactivate-mark ()
  (if (boundp 'running-xemacs)
      (zmacs-deactivate-region)
    (deactivate-mark)))

(provide 'swingtext-common)
