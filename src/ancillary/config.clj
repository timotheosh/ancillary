(ns ancillary.config
  (:require [clj-yaml.core :as yaml]))

(defn read-config
  "Loads a config file in yaml format and returns a key-word vector."
  ([] (read-config "doc/example-ancillary.yml"))
  ([conf]
   (yaml/parse-string
    (slurp conf))))
