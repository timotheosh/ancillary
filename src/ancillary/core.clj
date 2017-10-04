(ns ancillary.core
  (:require [ring.adapter.jetty :as jetty]
            [ancillary.config :as config]
            [ancillary.handler :as handler])
  (:gen-class))


(defn -main
  "Starts service based on parameters in config file."
  [& args]
  (let [conf (:main (config/read-config))]
    (defonce server (jetty/run-jetty
                     #'handler/app
                     {:port (:port conf)
                      :ssl-port (:ssl-port conf)
                      :keystore (:keystore conf)
                      :key-password (:key-password conf)
                      :send-server-version? false
                      :join? false}))))
