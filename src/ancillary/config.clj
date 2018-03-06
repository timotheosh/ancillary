(ns ancillary.config
  (:require [clj-yaml.core :as yaml]
            [clojure.java.io :as io]))

(def confdata (atom nil))

(defn show-confdata
  []
  @confdata)

(defn read-config
  "Loads a config file in yaml format and returns a key-word vector."
  ([] (if [(nil? @confdata)]
        (do
          (reset! confdata (read-config
                            (io/resource "example-ancillary.yml")))
          @confdata)
        @confdata))
  ([conf]
   (try
     (reset! confdata (yaml/parse-string (slurp conf)))
     (catch java.io.FileNotFoundException e
       (println (str "Cannot read file " conf "!"))
       (read-config))
     (finally
       @confdata))))
