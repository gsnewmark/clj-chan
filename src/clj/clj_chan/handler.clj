(ns clj-chan.handler
  "Entry point of an app - Ring handler."
  (:require [compojure.core :as c]
            [compojure.handler :as handler]
            [compojure.route :as r])
  (:import [org.webbitserver WebServer WebServers WebSocketHandler])
  (:gen-class :main true))


;; ## ~Model~

(defrecord Post [id author date content])

(def posts "In-memory storage of posts." (atom []))

(defn add-post [post posts]
  "Add a post to a DB. Argument is a map with optional keys :author and
:content."
  (let [{:keys [author content] :or {author "Anon" content "sth"}} post
        post (->Post (str (java.util.UUID/randomUUID))
                     author (java.util.Date.) content)]
    (swap! posts conj post)
    (dissoc post :id)))

;; ## Web socket

(def csrv (WebServers/createWebServer 8008))
(def connections (atom #{}))

(defn update-posts
  "Updates a posts atom with a message and send it to all connections."
  [message posts connections]
  ;; TODO add some validation to message
  (let [post (read-string message)]
    (when (map? post)
      (let [post (add-post post posts)]
        (doseq [conn @connections] (.send conn (pr-str [post])))))))

(.add csrv "/chatsocket"
      (proxy [WebSocketHandler] []
        (onOpen    [c]   (do
                           (swap! connections conj c)
                           (.send c (pr-str (map (partial into {}) @posts)))))
        (onClose   [c]   (swap! connections disj c))
        (onMessage [c m] (update-posts m posts connections))))

(defn -main [& m]
  (.start csrv))

;; ## Routes

(c/defroutes chan-routes
  (r/resources "/")
  (r/not-found "Page not found"))

;; ## App's ring handler

(def app
  (handler/site chan-routes))
