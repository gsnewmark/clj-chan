(ns clj-chan.handler
  "Entry point of an app - Ring handler."
  (:require [compojure.core :as c]
            [compojure.handler :as handler]
            [compojure.route :as r]
            [hiccup.page :as hp]
            [hiccup.form :as hf]
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
    ;(lc/siphon (lc/channel (pr-str (map (partial into {}) @posts))) ch)
    ;; send subsequent messages from web socket to user
    (lc/siphon chat ch)
    ;; send subsequent messages (including this one) from user to web socket
    (lc/siphon ch chat)))

;; ## Web views

(defn board
  "HTML base for a specific board."
  [topic]
  (hp/html5
   [:head [:title (str "Best chan ever - /" topic)]]
   [:body
    (hp/include-js "/board.js")
    [:header topic]
    [:div#new-post
     [:div (hf/text-field {:id "new-author"} "new-author")]
     [:div (hf/text-area {:id "new-content" :autofocus true} "new-content")]
     [:div (hf/submit-button {:id "post-submit"} "Add post")]]
    [:hr]
    [:div#posts [:div#post-anchor]]]))

(defn app [request]
  {:status  200
   :headers {"content-type" "text/html"}
   :body    (board (get-in request [:route-params :topic] "/b"))})

(defn board-app [ch request]
  (if (:websocket request)
    (chat-handler ch (get-in request [:route-params :topic] "/b"))
    (lc/enqueue ch (app request))))

(c/defroutes app-routes
  (c/GET ["/boards/:topic", :topic #"[a-zA-Z0-9_\-]+"] {}
       (ah/wrap-aleph-handler board-app))
  (r/resources "/")
  (r/not-found "Page not found"))

(defn -main [& args]
  (ah/start-http-server (ah/wrap-ring-handler app-routes)
                        {:port 1337 :websocket true}))
