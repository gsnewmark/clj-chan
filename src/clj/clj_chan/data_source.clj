(ns clj-chan.data-source
  "Describes interactions with basic imageboard entities (board, topic
post).")


;; Post on a board.
(defrecord Post [id author date image text])

;; ## DB protocols

;; TODO create few separate protocols
(defprotocol BoardDAO
  "Describes an interface to a chan's board - collection of posts."
  (add-topic [self topic]
    "Adds a new topic to board.")
  (get-topics [self]
    "Returns all existing topics on board.")
  (add-post [self topic post]
    "Adds a new post to a board's topic.")
  (get-posts [self topic]
    "Returns all posts from a board's topic in a form of list of
Post instances."))

;; ## In-memory 'DB' implementation

;; Stores a list of posts in an atom.
(defrecord InMemoryBoard [posts-atom]
  BoardDAO
  (add-topic [self topic]
    (when-not (get @posts-atom topic)
      (swap! posts-atom assoc topic [])))
  (get-topics [self]
    (keys @posts-atom))
  (add-post [self topic post]
    (let [{:keys [author image text]
           :or {author "Anon" text "" image ""}} post
          text (if (and (empty? text) (empty? image)) "sth" text)
          post (->Post (str (java.util.UUID/randomUUID))
                       author (java.util.Date.) image text)]
      (swap! posts-atom update-in [topic] #(conj % post))
      (into {} post)))
  (get-posts [self topic]
    (get @posts-atom topic [])))
