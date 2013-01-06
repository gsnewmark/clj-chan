(defproject clj-chan "0.1.0-SNAPSHOT"
  :description  "Simple 'RESTy' imageboard."
  :url          "http://example.com/FIXME"
  :license      {:name "Eclipse Public License"
                 :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure       "1.5.0-RC1"]
                 [compojure                 "1.1.3"]
                 [liberator                 "0.8.0"]
                 [cheshire                  "5.0.1"]
                 [enlive                    "1.0.1"]]
  :ring         {:handler clj-chan.handler/app}
  :plugins      [[lein-cljsbuild "0.2.10"]]
  :source-paths ["src/clj"]
  :cljsbuild    {:builds
                 [{:source-path "src/cljs"
                   :compiler    {:output-to     "resources/public/hello.js"
                                 :optimizations :whitespace
                                 :pretty-print  true}}]})
