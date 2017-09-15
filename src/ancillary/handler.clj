(ns ancillary.handler
  (:require [ring.util.response :as res]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [bidi.ring :refer [make-handler]]
            [liberator.core :refer [resource defresource]]
            [clojure.data.json :as json]
            [ancillary.config :as config]
            [ancillary.execution-handler :as exec]))

(defmacro call [^String nm & args]
    (when-let [fun (ns-resolve *ns* (symbol nm))]
       (conj args fun)))

(defresource index-handler
  :allowed-methods [:get]
  :available-media-types ["text/html"]
  :handle-ok "Hello from liberator (and bidi)")

(def app-routes
  ["/" {"" index-handler
        "index.html" index-handler}])

(defn endpoint-route
  "Generates a map suitable for use by Compojure based on endpoint
  configuration."
  [data]
  (let [endpoint (eval data)
        epconfig (first (keys endpoint))
        config-data (get endpoint epconfig)
        context (or (get config-data :context) "")
        path (str context "/" (name epconfig))]
    (cond
      (contains? config-data :command)
      (let [funcall `(json/write-str
                      (exec/exec-sh
                       ~(get config-data :command)))]
        [path funcall]))))

(defn generate-routes
  [request]
  (let [conf (config/read-config)
        endpoints (map endpoint-route (get conf :endpoints))
        secure-endpoints (get conf :secure_endpoints)]
    (conj endpoints 'ancillary.handler/app-routes)))

(def app
  (wrap-defaults (make-handler app-routes) api-defaults))
