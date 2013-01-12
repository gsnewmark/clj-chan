(ns clj-chan.client.board
  (:require [clj-chan.client.utils :as u]))


;; ## Vars
(def messages (atom []))

(def ws-handlers
  {:onopen    #(u/log "Connection established.")
   :onclose   #(u/log "Connection closed.")
   :onmessage #(js/alert (.-data %))
   :onerror   #(u/log (str "Something bad happened:" %))})

;; ## Web socket connection

;; TODO move connection string to env parameters
(def ws (js/WebSocket. "ws://localhost:8008/chatsocket"))
(u/init-ws ws ws-handlers)
;; TODO close web socket connection when page is closed
