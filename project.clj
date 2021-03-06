(defproject ancillary "0.1.3"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "The MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.fzakaria/slf4j-timbre "0.3.8"]
                 [clj-yaml "0.4.0"]
                 [ring/ring-jetty-adapter "1.6.3"]
                 [ring/ring-defaults "0.3.1"]
                 [ring-logger-timbre "0.7.6"]
                 [bidi "2.1.3"]
                 [liberator "0.15.1"]
                 [org.clojure/data.json "0.2.6"]
                 [com.cemerick/pomegranate "0.4.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [clj-http "3.7.0"]]
  :main ^:skip-aot ancillary.core
  :target-path "target/%s"
  :resource-paths ["resources"]
  :plugins [[brightnorth/uberjar-deploy "1.0.1"]]
  :aliases {"jar"    "uberjar"
            "deploy" "uberjar-deploy"}
  :ring {:port 8880
         :init ancillary.config/read-config
         :handler ancillary.core/test-app
         :auto-reload? true}
  :profiles {:uberjar {:aot :all}
             :dev {:plugins [[lein-ring "0.12.3"]]}})
