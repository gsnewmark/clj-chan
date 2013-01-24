(ns clj-chan.web-socket-handler
  "Short package description."
  (:require [clj-chan.config :as conf]
            [clj-chan.data-source :as ds])
  (:import [org.webbitserver WebServer WebServers WebSocketHandler]))


(def board-db (ds/->InMemoryBoard (atom {})))

(defn generate-ws-handler
  "Generates a Webbit web socket handler which uses a given DB object and
atom with currently existing subscriptions (topic - connection)."
  [db]
  (let [subscriptions (atom {})]
    (proxy [WebSocketHandler] []
      (onOpen [c] (identity c))
      (onClose [c]
        (swap!
         subscriptions
         (fn [s]
           (into {} (map #(let [[k v] %]
                            [k (if (contains? v c) (disj v c) v)])
                         s)))))
      (onMessage [c m]
        (let [message (read-string m)
              {:keys [topic action]} message]
          ;; TODO refactor this - should be multi-method (maybe protocol)
          (condp = action
            :post
            (let [post (ds/add-post db topic (:post message))]
              (doseq [c (get @subscriptions topic [])]
                (.send c (pr-str post))))
            :init
            (do
              (when-not (ds/topic-exists? db topic)
                (ds/add-topic db topic))
              (swap! subscriptions update-in [topic] #(into #{} (conj % c)))
              (doseq [p (ds/get-posts db topic)]
                (.send c ((comp pr-str (partial into {})) p))))))))))

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
        handler (generate-ws-handler board-db)]
    (.start (generate-server ws-port ws-path handler))))
