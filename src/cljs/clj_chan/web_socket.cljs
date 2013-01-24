(ns clj-chan.client.web-socket
  "Short package description."
  (:require [clj-chan.client.utils :as u]
            [cljs.reader :as r]
            [clojure.string :as s]))


;; ## Web socket connection

(defn ws
  "Opens a new connection to a websocket that corresponds to a current page."
  []
  (js/WebSocket.
   ;; TODO read port from config
   (str "ws://" (.-hostname (.-location js/window)) ":1338/websocket")))

(defn init-ws
  "Adds event handlers to a web socket from a map keys of which correspond
to names of event handlers (JS web socket API)."
  [ws handlers]
  (dorun (map (fn [[n f]] (aset ws (name n) f)) handlers)))

(defn decode-post
  "Transforms string to a data structure."
  [post-event]
  (r/read-string (.-data post-event)))

(defn send-message
  [ws message]
  (.send ws (pr-str message)))

(def ws-handlers
  {:onopen #(u/log "Connection established.")
   :onclose #(u/log "Connection closed.")
   :onmessage (fn [i] (let [posts (decode-post i)] (u/log (.-data i))))
   :onerror #(u/log (str "Something bad happened:" %))})
