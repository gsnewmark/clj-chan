(defproject clj-chan "0.1.0-SNAPSHOT"
  :description  "Simple imageboard."
  :url          "https://github.com/gsnewmark/clj-chan"
  :license      {:name "Eclipse Public License"
                 :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure  "1.5.0-RC2"]
                 [compojure            "1.1.3"]
                 [hiccup               "1.0.2"]
                 [aleph                "0.3.0-SNAPSHOT"]
                 [enfocus              "1.0.0-beta2"]
                 [prismatic/dommy      "0.0.1"]]
  :main         clj-chan.server
  :plugins      [[lein-cljsbuild "0.2.10"]]
  :source-paths ["src/clj"]
  :cljsbuild    {:builds
                 [{:source-path "src/cljs"
                   :compiler    {:output-to     "resources/public/js/board.js"
                                 :optimizations :whitespace
                                 :pretty-print  true}}]})
