(ns ancillary.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults
                                              api-defaults
                                              secure-api-defaults]]
            [taoensso.timbre :as timbre]
            [ring.logger.timbre :as logger.timbre]
            [bidi.ring :refer [make-handler]]
            [clojure.tools.cli :refer [parse-opts]]
            [ancillary.config :as config]
            [ancillary.handler :as handler]
            [clojure.java.io :as io])
  (:gen-class))

(def cli-options
  [["-c" "--config PATH" "Config file to use"
    :id :config
    :default "doc/example-ancillary.yml"
    :validate [#(not (nil? (config/read-config %)))
               "Config file not existant!"]]
   ["-h" "--help"]])

(defn- get-resource
  [path]
  (if (= (get path 0) \/)
    path
    (io/file (io/resource path))))

(defn -main
  "Starts service based on parameters in config file. We defer defining
  the handler until we have processed the config specified from the
  command line."
  [& args]
  (config/read-config
   (:config (:options (parse-opts args cli-options))))
  (let [conf (config/show-confdata)
        loglevel (keyword (.toLowerCase (get (get conf :logging "") :loglevel "DEBUG")))
        mainconf (:main conf)]
    ;; Set up logging
    (timbre/set-level! loglevel)

    (def app (wrap-defaults
              (make-handler (handler/generate-routes))
              api-defaults))
    (defonce server (jetty/run-jetty
                     (logger.timbre/wrap-with-logger app)
                     {:port (:port mainconf)
                      :ssl-port (:ssl-port mainconf)
                      :keystore (get-resource (:keystore mainconf))
                      :key-password (:key-password mainconf)
                      :send-server-version? false
                      :join? false}))))

(defn pre-load-routes
  "Loads the default config before generating routes for bidi. This is
  used for running lein ring server."
  []
  (config/read-config)
  (handler/generate-routes))

;; For use with lein ring server
(def test-app
  (wrap-defaults
   (make-handler (pre-load-routes))
   api-defaults))
