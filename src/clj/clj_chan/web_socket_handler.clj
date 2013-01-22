(ns clj-chan.web-socket-handler
  "Short package description."
  (:require [clj-chan.config :as conf]
            [clj-chan.data-source :as ds])
  (:import [org.webbitserver WebServer WebServers WebSocketHandler]))


(def board-db (ds/->InMemoryBoard (atom {})))
;; TODO should specify to which topics each channel is listening
(def channels (atom #{}))

(defn generate-ws-handler
  "Generates a Webbit web socket handler which uses a given DB object and
atom with currently existing connection."
  [db channels]
  (proxy [WebSocketHandler] []
    (onOpen [c] (swap! channels conj c))
    (onClose [c] (swap! channels disj c))
    ;; TODO refactor this - should be multi-method (maybe protocol)
    (onMessage [c m] (let [message (read-string m)
                           {:keys [topic action]} message]
                       (condp = action
                         :post
                         (doseq [c @channels]
                           (.send c (pr-str
                                     (ds/add-post db topic (:post message)))))
                         :init
                         (doseq [p (ds/get-posts db topic)]
                           (.send c ((comp pr-str (partial into {})) p))))))))

(defn generate-server
  "Generates a Webbit server with given parameters."
  ;; TODO should be map or ~named parameters~
  [port ws-path ws-handler]
  (let [web-server (WebServers/createWebServer port)]
    (doto web-server
      (.add ws-path ws-handler))))

(defn start-server
  "Starts a web socket server with a given settings (specified as map)."
  [settings]
  (let [settings (merge conf/default-config settings)
        {:keys [ws-port ws-path]} settings
        handler (generate-ws-handler board-db channels)]
    (.start (generate-server ws-port ws-path handler))))
