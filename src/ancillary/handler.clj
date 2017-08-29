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

(defmacro endpoint-route
  "Generates a map suitable for use by Compojure based on endpoint
  configuration."
  [data]
  (let [endpoint (eval data)
        epconfig (first (keys endpoint))
        config-data (get endpoint epconfig)
        context (or (get config-data :context) "")
        path (str context "/" (name epconfig))
        method (or (get config-data :method) "GET")]
    (cond
      (contains? config-data :command)
      (let [funcall `(json/write-str
                      (exec/exec-sh
                       ~(get config-data :command)))]
        `(~(symbol (str "compojure.core/" method)) ~path [] ~funcall)))))

(defn generate-routes
  []
  (let [conf (config/read-config)
        endpoints (map #(endpoint-route %) (into [] (get conf :endpoints)))
        secure-endpoints (get conf :secure_endpoints)]
    endpoints))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (generate-routes)
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes api-defaults))
