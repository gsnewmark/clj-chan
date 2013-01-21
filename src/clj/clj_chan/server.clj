(ns clj-chan.server
  (:require [clj-chan.config :as conf]
            [clj-chan.data-source :as ds]
            [clj-chan.board-handler :as board-handler]
            [compojure.core :as c]
            [compojure.route :as r]
            [aleph.http :as ah])
  (:gen-class))


(def board-db (ds/->InMemoryBoard (atom {})))

(c/defroutes app
  (c/GET
   ["/boards/:topic", :topic #"[a-zA-Z0-9_\-]+"] {}
   (ah/wrap-aleph-handler (board-handler/board-handler-factory board-db)))
  (r/resources "/")
  (r/not-found "Page not found"))

(def wrapped-app
  (-> app
      ah/wrap-ring-handler))

;; TODO find a way to propagate settings to app
;; possibly use c/routes and not c/defroutes
(defn start-server
  [settings]
  (let [{:keys [port]} (merge conf/default-config settings)]
    (ah/start-http-server #(wrapped-app %1 %2)
                          {:port port :websocket true})))

(defn -main [& args]
  (start-server {}))






