(ns clj-chan.ring-handler
  "Handler for a Ring-based part of an app."
  (:require [clj-chan.config :as conf]
            [clj-chan.views :as views]
            [compojure.core :as c]
            [compojure.route :as r]
            [compojure.handler :as h]
            [ring.adapter.jetty :as s]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])))


(c/defroutes app
  (c/GET
   ["/boards/:topic", :topic #"[a-zA-Z0-9_\-]+"] [topic]
   (friend/authorize
    #{:user}
    (views/board-view topic)))
  (c/GET "/login" req views/login-view)
  (friend/logout
   (c/ANY "/logout" request (ring.util.response/redirect "/login")))
  (r/resources "/")
  (r/not-found "Page not found"))

(defn wrapped-app
  [settings]
  (-> app
      (friend/authenticate
       {:credential-fn (partial creds/bcrypt-credential-fn (:users settings))
        :workflows [(workflows/interactive-form)]
        :default-landing-uri "/boards/hello"})
      h/site))

;; TODO find a way to propagate settings to app
;; possibly use c/routes and not c/defroutes
(defn start-server
  [settings]
  (let [settings (merge conf/default-config settings)
        {:keys [port]} settings]
    (s/run-jetty (wrapped-app settings) {:port port})))
