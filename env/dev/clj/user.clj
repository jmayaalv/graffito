(ns user
  (:require [vlaaad.reveal :as reveal]
            [vlaaad.reveal.ext :as reveal.ext]))

(require 'hashp.core)

(defn reveal! []
  (add-tap (reveal/ui)))


(defn reveal-clear!
  []
  (tap> {:vlaaad.reveal/command (reveal.ext/clear-output)}))
