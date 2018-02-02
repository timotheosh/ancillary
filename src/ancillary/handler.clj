(ns ancillary.handler
  (:require [ring.util.response :as res]
            [ring.middleware.defaults :refer [wrap-defaults
                                              api-defaults
                                              secure-api-defaults]]
            [bidi.ring :refer [make-handler]]
            [liberator.core :refer [resource defresource]]
            [liberator.representation :refer [ring-response]]
            [liberator.dev :refer [wrap-trace]]
            [ancillary.config :as config]
            [ancillary.execution-handler :as exec]))

(defn default-response
  [req]
  {:status 403
   :body "Access denied"})

(defresource default-handler
  :available-media-types ["text/plain"]
  :handle-not-found (fn [_] (default-response)))

(defresource index-handler
  :allowed-methods [:get]
  :available-media-types ["text/html"]
  :handle-ok "Hello from Ancillary!")

(defresource command [cmd]
  :allowed-methods [:get]
  :available-media-types ["application/json"]
  :handle-ok (fn [ctx] (ring-response
                        (exec/exec-sh cmd))))

(def app-routes
  [["" index-handler]
   ["index.html" index-handler]
   ["hello" (command "echo \"Hello from Liberator!\"")]])

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
      [path (command (get config-data :command))])))

(defn generate-routes
  []
  (let [endpoints-config (:endpoints (config/read-config))
        endpoints (into app-routes (map #(endpoint-route %) endpoints-config))]
    ["/" (conj endpoints [true default-response])]))

(def app
  (-> (make-handler (generate-routes))
      (wrap-defaults secure-api-defaults)))

(def test-app
  (-> (make-handler (generate-routes))
      (wrap-defaults api-defaults)))
