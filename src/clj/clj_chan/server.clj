(ns clj-chan.server
  "Entry point of an app."
  (:require [clj-chan.config :as conf]
            [clj-chan.data-source :as ds]
            [clj-chan.views :as views]
            [compojure.core :as c]
            [compojure.route :as r]
            [compojure.handler :as h]
            [org.httpkit.server :as s]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds]))
  (:gen-class :main true))


(defn topic-handler
  "Generates a WebSocket+HTTP handler for a topic page. Uses a given data
base and atom (map) for storing clients' subscriptions to topics."
  [db subscriptions]
  (fn [request]
    (let [{:keys [topic]} (:params request)]
      (s/if-ws-request
       request ws-conn
       ;; TODO should be separate function(s)
       (do (s/on-mesg
            ws-conn
            (fn [m]
              (let [message (read-string m)
                    {:keys [topic action]} message]
                ;; TODO refactor this - should be multi-method (or protocol)
                (condp = action
                  :post
                  (let [post (ds/add-post db topic (:post message))]
                    (doseq [c (get @subscriptions topic [])]
                      (s/send-mesg c (pr-str post))))
                  :init
                  (do
                    (when-not (ds/topic-exists? db topic)
                      (ds/add-topic db topic))
                    (swap! subscriptions update-in [topic]
                           #(into #{} (conj % ws-conn)))
                    (doseq [p (ds/get-posts db topic)]
                      (s/send-mesg ws-conn (pr-str p))))))))
           (s/on-close
            ws-conn
            (fn [status]
              (swap!
               subscriptions
               (fn [s]
                 (into
                  {}
                  (map #(let [[k v] %]
                          [k (if (contains? v ws-conn) (disj v ws-conn) v)])
                       s)))))))
       (views/board-view topic)))))

(defn app [db subs]
  (c/routes
   (c/GET
    ["/boards/:topic", :topic #"[a-zA-Z0-9_\-]+"] [topic]
    (friend/authorize
     #{:user}
     (topic-handler db subs)))
   (c/GET "/login" req views/login-view)
   (friend/logout
    (c/ANY "/logout" request (ring.util.response/redirect "/login")))
   (r/resources "/")
   (r/not-found "Page not found")))

(defn wrapped-app
  [settings db subs]
  (-> (app db subs)
      (friend/authenticate
       {:credential-fn (partial creds/bcrypt-credential-fn (:users settings))
        :workflows [(workflows/interactive-form)]
        :default-landing-uri "/boards/hello"})
      h/site))

;; TODO find a way to propagate settings to app
;; possibly use c/routes and not c/defroutes
(defn start-server
  [settings]
  (let [settings (merge conf/default-config settings)
        {:keys [port db-connection-string]} settings]
    (s/run-server
     (wrapped-app
      settings
      (ds/->MongoDBBoard (ds/get-mongo-db db-connection-string))
      (atom {}))
     {:port port})))

(defn -main [& args]
  (start-server {}))
