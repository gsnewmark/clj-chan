(ns clj-chan.server
  (:require [clj-chan.model :as model]
            [clj-chan.board-handler :as board-handler]
            [compojure.core :as c]
            [compojure.route :as r]
            [aleph.http :as ah])
  (:gen-class))


(def board-db (model/->MortalBoard (atom {})))

(c/defroutes app
  (c/GET
   ["/boards/:topic", :topic #"[a-zA-Z0-9_\-]+"] {}
   (ah/wrap-aleph-handler (board-handler/board-handler-factory board-db)))
  (r/resources "/")
  (r/not-found "Page not found"))

(def wrapped-app
  (-> app
      ah/wrap-ring-handler))

(defn -main [& args]
  (ah/start-http-server #(wrapped-app %1 %2) {:port 1337 :websocket true}))
