(ns clj-chan.server
  (:require [clj-chan.config :as conf]
            [clj-chan.data-source :as ds]
            [clj-chan.board-handler :as board-handler]
            [compojure.core :as c]
            [compojure.route :as r]
            [compojure.handler :as h]
            [ring.middleware.session :as s]
            [aleph.http :as ah]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds]))
  (:gen-class))


(def board-db (ds/->InMemoryBoard (atom {})))

(c/defroutes app
  (c/GET
   ["/boards/:topic", :topic #"[a-zA-Z0-9_\-]+"] {}
   (friend/authorize
    #{:user}
    (ah/wrap-aleph-handler (board-handler/board-handler-factory board-db))))
  (c/GET "/login" req (do (println req) board-handler/login-view))
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
      h/site
      ah/wrap-ring-handler))

;; TODO find a way to propagate settings to app
;; possibly use c/routes and not c/defroutes
(defn start-server
  [settings]
  (let [settings (merge conf/default-config settings)
        {:keys [port]} settings]
    (ah/start-http-server #((wrapped-app settings) %1 %2)
                          {:port port :websocket true})))

(defn -main [& args]
  (start-server {}))






