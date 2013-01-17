(ns clj-chan.client.web-socket
  "Short package description."
  (:require [clj-chan.client.utils :as u]
            [cljs.reader           :as r]))

(defn init-ws
  "Adds event handlers to a web socket from a map keys of which correspond
to names of event handlers (JS web socket API)."
  [ws handlers]
  (dorun (map (fn [[n f]] (aset ws (name n) f)) handlers)))

;; ## Vars
(def messages (atom []))

(defn decode-posts
  "Transforms string received on web socket connection to a data structure.
Message is a vector of posts (Post record)."
  [posts-string]
  (r/read-string (.-data posts-string)))

(def ws-handlers
  {:onopen    #(u/log "Connection established.")
   :onclose   #(u/log "Connection closed.")
   :onmessage (fn [i] (let [posts (decode-posts i)] (u/log (.-data i))))
   :onerror   #(u/log (str "Something bad happened:" %))})

;; ## Web socket connection

;; TODO move connection string to env parameters
(def ws (js/WebSocket. "ws://localhost:8008/chatsocket"))

(defn send-post
  [post]
  (.send ws (pr-str post)))
