(ns clj-chan.config
  "Application's default configuration."
  (:require [cemerick.friend.credentials :as creds]))

(def default-config
  {:port 1337
   :users {"root" {:username "root"
                   :password (creds/hash-bcrypt "theorem26")
                   :roles #{:user}}}})
