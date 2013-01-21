(ns clj-chan.board-handler
  (:require [clj-chan.data-source :as ds]
            [hiccup.page :as hp]
            [hiccup.form :as hf]
            [lamina.core :as lc]))


(declare board-websocket-handler board-html-handler board-init board-view)

;; ## Public interface

(defn board-handler-factory
  "Creates a handler for a board stored in a board-db.

Handler generates a response based on a request type - either a basic HTML
page which serves as a 'ground' for Clojurescript code, or opens a websocket
connection for a specific board."
  [board-db]
  (fn [ch request]
    (if (:websocket request)
      (board-websocket-handler
       board-db ch (get-in request [:route-params :topic] "/b"))
      (lc/enqueue ch (board-html-handler request)))))

;; ## Web socket
;; based on http://alexkehayias.tumblr.com/post/28783286946/a-simple-real-time-chat-server-using-clojure-and-aleph

(defn board-websocket-handler
  "Initializes and opens a web socket for a specific board."
  [board-db ch topic]
  (let [board (lc/named-channel topic (partial board-init topic))
        posts (ds/get-posts board-db topic)]
    (ds/add-topic board-db topic)
    ;; send already present posts to user
    ;; TODO shouldn't do this when pagination will be present
    (when (seq posts)
      (apply lc/enqueue ch (map (comp pr-str (partial into {})) posts)))
    (lc/siphon
     (lc/map* #(pr-str (ds/add-post board-db topic (read-string %))) ch)
     board)
    (lc/siphon board ch)))

(defn board-init [topic ch]
  (lc/receive-all ch #(println topic %)))

;; ## HTML

(defn board-html-handler
  "Renders an HTML page for a specific board."
  [request]
  {:status  200
   :headers {"content-type" "text/html"}
   :body    (board-view (get-in request [:route-params :topic] "/b"))})

(defn board-view
  "HTML base for a specific board."
  [topic]
  (hp/html5
   [:head
    [:title (str "Best chan ever - /" topic)]
    (hp/include-css (str "http://fonts.googleapis.com/css?"
                         "family=Press+Start+2P&subset=latin,cyrillic"))
    (hp/include-css "/css/board.css")
    (hp/include-js "/js/board.js")]
   [:body
    [:header (str "/" topic)]
    [:div#new-post
     [:div
      (hf/label "new-author" "Name")
      (hf/text-field {:id "new-author"} "new-author")]
     [:div
      (hf/label "new-image" "Image link")
      (hf/text-field {:id "new-image"} "new-image")]
     [:div
      (hf/label "new-text" "Comment")
      (hf/text-area {:id "new-text"} "new-text")]
     [:div (hf/submit-button {:id "post-submit"} "Add post")]]
    [:hr]
    [:div#posts [:div#post-anchor]]]))
