(ns clj-chan.handler
  "Entry point of an app - Ring handler."
  (:require [compojure.core :as c]
            [compojure.handler :as h]
            [liberator.core :as l]
            [cheshire.core :as j]))


;; ## ~Model~

(defrecord Post [id author date content])

(def posts "In-memory storage of posts." (atom #{}))

(defn add-post [post]
  "Add a post to a DB. Argument is a map with optional keys :author and
:content."
  (let [{:keys [author content] :or {author "Anon" content "sth"}} post]
    (swap! posts conj (->Post 0 author (java.util.Date.) content))))

;; ## Resources

;; TODO add html representation
;; List of all posts.
(l/defresource posts-list
  :method-allowed? (l/request-method-in :get)
  :available-media-types ["application/json"]
  :handle-ok (fn [ctx] (j/generate-string @posts {:escape-non-ascii true})))

;; curl request to test:
;; curl -v -H "Accept: application/json" -H "Content-type: application/json" -X POST -d ' {"post":{"author":"o","content":"c"}}'  http://localhost:3000/imageboard/posts/create
;; Creation of a new post. Expects an AJAX request with body
;; {"post":{["author":name],["content":content]}}, where "author" and
;; "content" elements are optional.
(l/defresource create-post
  :method-allowed? (l/request-method-in :post)
  :available-media-types ["application/json"]
  :handle-created "Submission accepted"
  :post! (fn [ctx]
           (-> (:request ctx)
               :body
               clojure.java.io/reader
               (j/parse-stream true)
               :post
               add-post)))

;; ## Routes

(c/defroutes chan-routes
  (c/ANY "/imageboard/posts" [] posts-list)
  (c/ANY "/imageboard/posts/create" [] create-post))

;; ## App's ring handler

(def app
  (h/site chan-routes))
