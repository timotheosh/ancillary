(ns ancillary.core
  (:require [ring.adapter.jetty :as jetty]
            [ancillary.config :as config]
            [ancillary.handler :as handler])
  (:gen-class))


(defn -main
  "Starts service based on parameters in config file."
  [& args]
  (let [conf (config/read-config)]
    (defonce server (jetty/run-jetty
                     #'handler/app
                     {:port (get (get conf :main) :port)
                      :join? false}))))
