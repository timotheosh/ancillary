(ns ancillary.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [clojure.data.json :as json]
            [ancillary.config :as config]
            [ancillary.execution-handler :as exec]))

(defmacro call [^String nm & args]
    (when-let [fun (ns-resolve *ns* (symbol nm))]
       (conj args fun)))

(defn endpoint-route
  "Generates a map suitable for use by Compojure based on endpoint
  configuration."
  [endpoint]
  (let [epconfig (first (keys endpoint))
        config-data (get endpoint epconfig)
        context (or (get config-data :context) "")
        path (str context "/" (name epconfig))
        method (or (get config-data :method) "GET")]
    (cond
      (contains? config-data :command)
      (let [result (exec/exec-sh
                    (get config-data :command))]
        (if (= (get result :status) 200)
          ((symbol method) path (json/write-str result)))))))

(defn generate-routes
  []
  (let [conf (config/read-config)
        endpoints (reduce #'endpoint-route (get conf :endpoints))
        secure-endpoints (get conf :secure_endpoints)]
    endpoints))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes api-defaults))
