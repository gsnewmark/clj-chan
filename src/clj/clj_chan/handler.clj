(ns clj-chan.handler
  "Entry point of an app - Ring handler."
  (:require [compojure.core :as c]
            [compojure.handler :as handler]
            [compojure.route :as r]
            [liberator.core :as l]
            [liberator.representation :as lr]
            [cheshire.core :as j]
            [net.cgrand.enlive-html :as h]))


;; ## ~Model~

(defrecord Post [id author date content])

(def posts "In-memory storage of posts." (atom #{}))

(defn add-post [post]
  "Add a post to a DB. Argument is a map with optional keys :author and
:content."
  (let [{:keys [author content] :or {author "Anon" content "sth"}} post]
    (swap! posts conj (->Post (str (java.util.UUID/randomUUID))
                              author (java.util.Date.) content))))

;; ## Views

;; Takes a list of Post instances as an argument.
(h/deftemplate index "public/index.html"
  [posts]
  [:header]              (h/do->
                          (h/content "topic1"))
  [:div#posts :div.post] (h/clone-for
                          [post posts]
                          [:div.author]  (h/content (:author post))
                          [:div.content] (h/content (:content post))))

;; ## Resources

;; List of all posts.
(l/defresource posts-list
  :method-allowed?       (l/request-method-in :get)
  :available-media-types ["application/json" "text/html"]
  :handle-ok             (fn [ctx]
                           (case (get-in ctx [:representation :media-type])
                             "text/html" (apply str (index @posts))
                             (j/generate-string @posts
                                                {:escape-non-ascii true}))))

;; Access to particular post.
(l/defresource post
  :method-allowed?       (l/request-method-in :get)
  :available-media-types ["application/json"]
  :handle-ok             (fn [ctx]
                           (let [id (get-in ctx [:request :params :id])]
                             (j/generate-string
                              (first (filter #(= id (:id %)) @posts))
                              {:escape-non-ascii true}))))

;; curl request to test:
;; curl -v -H "Accept: application/json" -H "Content-type: application/json" -X POST -d ' {"post":{"author":"o","content":"c"}}'  http://localhost:3000/imageboard/posts/create
;; Creation of a new post. Expects an AJAX request with body
;; {"post":{["author":name],["content":content]}}, where "author" and
;; "content" elements are optional.
(l/defresource create-post
  :method-allowed?       (l/request-method-in :post)
  :available-media-types ["application/json"]
  :handle-created        "Submission accepted"
  :post!                 (fn [ctx]
                           (-> (get-in ctx [:request :body])
                               clojure.java.io/reader
                               (j/parse-stream true)
                               :post
                               add-post)))

;; ## Routes

(c/defroutes chan-routes
  (c/ANY       "/imageboard/posts"        [] posts-list)
  (c/ANY       "/imageboard/posts/create" [] create-post)
  (c/ANY       "/imageboard/posts/:id"    [] post)
  (r/resources "/")
  (r/not-found "Page not found"))

;; ## App's ring handler

(def app
  (handler/site chan-routes))
