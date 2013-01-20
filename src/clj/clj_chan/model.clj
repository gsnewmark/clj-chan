(ns clj-chan.model
  "Describes basic entities of a chan and function on them.")


;; ## Abstraction

;; Post on a board.
(defrecord Post [id author date image text])

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
(defrecord MortalBoard [posts-atom]
  BoardDAO
  (add-topic [self topic]
    (when-not (get @posts-atom topic)
      (swap! posts-atom assoc topic [])))
  (get-topics [self]
    (keys @posts-atom))
  (add-post [self topic post]
    (let [{:keys [author image text]
           :or {author "Anon" text "sth" image ""}} post
          post (->Post (str (java.util.UUID/randomUUID))
                       author (java.util.Date.) image text)]
      (swap! posts-atom update-in [topic] #(conj % post))
      (into {} post)))
  (get-posts [self topic]
    (get @posts-atom topic [])))
