(ns ancillary.core
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults
                                              api-defaults
                                              secure-api-defaults]]
            [bidi.ring :refer [make-handler]]
            [clojure.tools.cli :refer [parse-opts]]
            [ancillary.config :as config]
            [ancillary.handler :as handler])
  (:gen-class))

(def cli-options
  [["-c" "--config PATH" "Config file to use"
    :id :config
    :default "doc/example-ancillary.yml"
    :validate [#(not (nil? (config/read-config %)))
               "Config file not existant!"]]
   ["-h" "--help"]])

(defn -main
  "Starts service based on parameters in config file. We defer defining
  the handler until we have processed the config specified from the
  command line."
  [& args]
  (config/read-config
   (:config (:options (parse-opts args cli-options))))
  (let [conf (config/show-confdata)
        mainconf (:main conf)]
    (def app (wrap-defaults
              (make-handler (handler/generate-routes))
              secure-api-defaults))
    (defonce server (jetty/run-jetty
                     #'app
                     {:port (:port mainconf)
                      :ssl-port (:ssl-port mainconf)
                      :keystore (:keystore mainconf)
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
