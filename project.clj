(defproject clj-chan "0.1.0-SNAPSHOT"
  :description "Simple 'RESTy' imageboard."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0-RC1"]
                 [compojure "1.1.3"]
                 [liberator "0.8.0"]
                 [cheshire "5.0.1"]
                 [enlive "1.0.1"]]
  :ring {:handler clj-chan.handler/app})
