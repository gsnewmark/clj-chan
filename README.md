# clj-chan

A simple imageboard with dynamic post loading.

Server is implemented in Clojure, client - in Clojurescript. Communications
are done through a WebSocket connection.

[http-kit](http://http-kit.org/index.html) is used for the server-side
WebSocket handling.

## Installation

1. Install [Leiningen](http://leiningen.org/).
2. Compile `<checkout_dir>/resources/public/css/board.less` to a css version
`<checkout_dir>/resources/public/css/board.css` (you could use online
compiler).
3. Execute

        lein deps
        lein cljsbuild once

4. If you want to create a jar, execute

        lein uberjar

This will create runnable jar called `clj-chan-0.1.8-SNAPSHOT-standalone.jar`
in a `<checkout_dir>/targer` directory.

## Usage

If you created jar, run

        java -jar clj-chan-0.1.8-SNAPSHOT-standalone.jar

Or simply execute `lein run` in a `<checkout_dir>`.

The server would be started on the port 1337 of a current host.

To access a specific topic open the `http://your_host:1337/boards/topic`
page in a browser. `topic` in URL could be substituted for any alphanumeric
string. Each topic has its separate list of posts.

## License

Copyright © 2013 gsnewmark

Distributed under the Eclipse Public License, the same as Clojure.
