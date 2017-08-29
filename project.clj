(defproject ancillary "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-yaml "0.4.0"]
                 [ring/ring-jetty-adapter "1.2.1"]
                 [ring/ring-defaults "0.3.1"]
                 [compojure "1.6.0"]
                 [org.clojure/data.json "0.2.6"]]
  :main ^:skip-aot ancillary.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
