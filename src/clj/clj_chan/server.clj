(ns clj-chan.server
  "Entry point of an app."
  (:require [clj-chan.ring-handler :as crh]
            [clj-chan.web-socket-handler :as cwh])
  (:gen-class :main true))


(defn -main [& args]
  (cwh/start-server {})
  (crh/start-server {}))
