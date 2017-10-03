(ns ancillary.handler
  (:require [ring.util.response :as res]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [bidi.ring :refer [make-handler]]
            [liberator.core :refer [resource defresource]]
            [liberator.dev :refer [wrap-trace]]
            [ancillary.config :as config]
            [ancillary.execution-handler :as exec]))

(defmacro call [^String nm & args]
    (when-let [fun (ns-resolve *ns* (symbol nm))]
       (conj args fun)))

(defresource index-handler
  :allowed-methods [:get]
  :available-media-types ["text/html"]
  :handle-ok "Hello from liberator (and bidi)")

(defresource command [cmd]
  :allowed-methods [:get]
  :available-media-types ["text/html"
                          "application/json"]
  :handle-ok (fn [_] (exec/exec-sh cmd)))

(def app-routes
  {"" index-handler
   "index.html" index-handler
   "hello" (command "echo \"Hello from Liberator!\"")})

(defn endpoint-route
  "Generates a map suitable for use by Bidi based on endpoint
  configuration."
  [endpoint]
  (let [epconfig (first (keys endpoint))
        config-data (get endpoint epconfig)
        context (or (get config-data :context) "")
        path (str context (name epconfig))]
    (cond
      (contains? config-data :command)
      {path (command (get config-data :command))})))

(defn generate-routes
  []
  (let [conf (config/read-config)
        endpoints (into app-routes (map endpoint-route (get conf :endpoints)))
        secure-endpoints (get conf :secure_endpoints)]
    ["/" endpoints]))

(def app
  (wrap-defaults (wrap-trace (make-handler (generate-routes))) api-defaults))
