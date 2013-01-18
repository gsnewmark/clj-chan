(ns clj-chan.handler
  "Entry point of an app - Ring handler."
  (:require [compojure.core :as c]
            [compojure.handler :as handler]
            [compojure.route :as r]
            [lamina.core :as lc]
            [aleph.http :as ah]))


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

;; ## Aleph web socket (based on http://alexkehayias.tumblr.com/post/28783286946/a-simple-real-time-chat-server-using-clojure-and-aleph)

;; TODO debug post adding
(defn chat-init [ch]
  (lc/receive-all ch #(doseq [post %] (add-post post posts))))

;; TODO send back correct post (result from add-post)
(defn chat-handler [ch room]
  (let [chat (lc/named-channel room chat-init)]
    (println (pr-str (map (partial into {}) @posts)))
    ;; send 'history' to user
    ;; TODO find a way to send a history of messages to a newly connected user
    (lc/siphon (lc/channel (pr-str (map (partial into {}) @posts))) ch)
    ;; send subsequent messages from web socket to user
    (lc/siphon chat ch)
    ;; send subsequent messages (including this one) from user to web socket
    (lc/siphon ch chat)))

(defn app [request]
  {:status 200
   :headers {"content-type" "text/html"}
   :body "<script type=\"text/javascript\" src=\"/hello.js\"></script><b>oi</b>"})

(defn chat [ch request]
  (let [params (:route-params request)
        room (:room params)]
      (if (:websocket request)
        (chat-handler ch room)
        (lc/enqueue ch (app request)))))

(c/defroutes app-routes
  (c/GET ["/"] {} "Hello world!")
  (c/GET ["/chat/:room", :room #"[a-zA-Z]+"] {}
       (ah/wrap-aleph-handler chat))
  (r/resources "/")
  ;;Any url without a route handler will be served this response
  (r/not-found "Page not found"))

(defn -main [& args]
  "Main thread for the server which starts an async server with
  all the routes we specified and is websocket ready."
  (ah/start-http-server (ah/wrap-ring-handler app-routes)
                        {:port 1337 :websocket true}))
