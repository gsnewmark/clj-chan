(ns clj-chan.client.board
  (:require        [clj-chan.client.web-socket :as socket]
                   [clj-chan.client.utils :as u]
                   [enfocus.core :as ef])
  (:require-macros [enfocus.macros :as em]))


;; TODO close web socket connection when page is closed

(defn gen-post-html [post]
  (str "<div><div class=\"post-author\">" (:author post) "</div>"
       "<div class=\"post-content\">" (:content post) "<hr></div></div>"))

(defn show-post [post]
  (em/at js/document
         ["div#posts > div#post-anchor"]
         (em/after (gen-post-html post))))

(defn read-new-post-data []
  (em/from js/document
           :author ["#new-post #new-author"] (em/get-prop :value)
           :content ["#new-post #new-content"] (em/get-prop :value)))

(em/defaction setup []
  ["#post-submit"] (em/listen :click #(socket/send-post [(read-new-post-data)])))

(defn start []
  (socket/init-ws socket/ws
                  (assoc socket/ws-handlers :onmessage
                         (fn [i] (let [posts (socket/decode-posts i)]
                                  (doseq [post posts] (show-post post))))))
  (setup))

(set! (.-onload js/window) #(start))
