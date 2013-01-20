(ns clj-chan.client.web-socket
  "Short package description."
  (:require [clj-chan.client.utils :as u]
            [cljs.reader :as r]
            [clojure.string :as s]))


(defn init-ws
  "Adds event handlers to a web socket from a map keys of which correspond
to names of event handlers (JS web socket API)."
  [ws handlers]
  (dorun (map (fn [[n f]] (aset ws (name n) f)) handlers)))

(defn decode-post
  "Transforms string to a data structure."
  [post-event]
  (r/read-string (.-data post-event)))

(defn send-post
  [post]
  (.send ws (pr-str post)))

(def ws-handlers
  {:onopen    #(u/log "Connection established.")
   :onclose   #(u/log "Connection closed.")
   :onmessage (fn [i] (let [posts (decode-posts i)] (u/log (.-data i))))
   :onerror   #(u/log (str "Something bad happened:" %))})

;; ## Web socket connection

(def ws (js/WebSocket.
         (s/replace-first (str (.-location js/window)) #"[^/]+" "ws:")))


