(ns clj-chan.config
  "Application's default configuration."
  (:require [cemerick.friend.credentials :as creds]))

(def default-config
  {:port 1337
   :ws-port 1338
   :ws-path "/websocket"
   ;; TODO shoudln't be here
   :users {"root" {:username "root"
                   :password (creds/hash-bcrypt "root")
                   :roles #{:user}}}})
