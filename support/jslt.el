
; The beginnings of an emacs mode for JSLT

(require 'generic-x)

(define-generic-mode 'jslt-mode
  '("//")                           ;; comments
  '("let" "if" "for" "else" "def")  ;; keywords
  '(
    ("\\$[-A-Za-z0-9_]+" . 'font-lock-variable-name-face)
    ("true\\|false"      . 'font-lock-constant-face)
    )
  '("\\.jslt$")                     ;; files for which to activate this mode
  nil                               ;; other functions to call
  "A mode for JSLT templates")
