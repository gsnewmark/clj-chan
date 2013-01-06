(ns clj-chan.handler
  "Entry point of an app - Ring handler."
  (:require [compojure.core :as c]
            [compojure.handler :as h]
            [liberator.core :as l]
            [cheshire.core :as j]))


(def postbox-counter (atom 0))

(l/defresource postbox
  :method-allowed? (l/request-method-in :post)
  :post! (swap! postbox-counter inc)
  :handle-created "Your submission was accepted."
  :available-media-types ["application/json"])

(l/defresource postbox-list
  :method-allowed? (l/request-method-in :get)
  :handle-ok (fn [ctx]
               (j/generate-string
                {:posts @postbox-counter} {:escape-non-ascii true}))
  :available-media-types ["application/json"])

(c/defroutes chan-routes
  (c/POST "/post" [] postbox)
  (c/GET "/post" [] postbox-list))

(def app
  (h/site chan-routes))
