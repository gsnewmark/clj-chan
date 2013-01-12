(ns clj-chan.client.utils
  "A bunch of utils used in other namespaces."
  (:require [clojure.browser.repl :as repl]))


;; For live coding
(repl/connect "http://localhost:9000/repl")

(defn log [m] (.log js/console m))

(defn init-ws
  "Adds event handlers to a web socket from a map keys of which correspond
to names of JS web socket API."
  [ws handlers]
  (dorun (map (fn [[n f]] (aset ws (name n) f)) handlers)))
