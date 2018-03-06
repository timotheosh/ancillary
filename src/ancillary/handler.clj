(ns ancillary.handler
  (:require [ring.util.response :as res]
            [liberator.core :refer [resource defresource]]
            [liberator.representation :refer [ring-response]]
            [liberator.dev :refer [wrap-trace]]
            [ancillary.config :as config]
            [ancillary.execution-handler :as exec]
            [ancillary.modules :as modules]))

(defn default-response
  [req]
  {:status 403
   :body "Access denied"})

(defresource default-handler
  :available-media-types ["text/plain"]
  :handle-not-found (fn [_] (default-response "")))

(defresource index-handler
  :allowed-methods [:get]
  :available-media-types ["text/html"]
  :handle-ok "Hello from Ancillary!")

(defresource command [cmd]
  :allowed-methods [:get]
  :available-media-types ["application/json"]
  :handle-ok (fn [ctx] (ring-response
                        (exec/exec-sh cmd))))

(defresource customclass [classname]
  :allowed-methods (modules/allowed-methods classname)
  :available-media-types ["application/json"]
  :new? false
  :respond-with-entity? true
  :handle-ok
  (fn [ctx] (ring-response
             (modules/mod-func classname
                               (.toUpperCase
                                (name (:request-method (:request ctx))))
                               ctx))))

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
      (do
        (println "command: " (get config-data :command))
        [path (command (get config-data :command))])
      (contains? config-data :file)
      (let [conf (:main (config/show-confdata))
            jarfile (str (:module_dir conf) "/" (:file config-data))
            class (:class config-data)
            args (get config-data :args "")]
        (println "jarfile: " jarfile
                 "\tclass: " class)
        (modules/load-module jarfile class)
        [path (customclass class)]))))

(defn context-endpoints
  "Generates a list of routes for Bidi under a specific path (context)."
  [context]
  (let [path (first (keys context))
        endpoints (vec (map #(endpoint-route %) ((keyword path) context)))]
    [(str (name path) "/") endpoints]))

(defn generate-routes
  []
  (let [endpoints-config (:endpoints (config/show-confdata))
        endpoints (into app-routes (map #(context-endpoints %) endpoints-config))]
    ["/" (conj endpoints [true default-response])]))
