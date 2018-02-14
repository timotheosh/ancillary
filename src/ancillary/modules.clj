(ns ancillary.modules
  (:require [cemerick.pomegranate :as pom]))

(defn load-module
  "Loads a custom module given the path and class name."
  [url-string classname]
  (try
    (require (symbol classname))
    (catch java.io.FileNotFoundException e
      (pom/add-classpath url-string)
      (require (symbol classname)))))

(defn available-methods
  "Returns a list of available methods of a class/namespace"
  [classname]
  (keys (ns-publics (symbol classname))))

(defn mod-func
  "Executes the given method and arguments to that method for the loaded class.
   Should return something for ring."
  [classname function args]
  ((resolve (symbol (str classname "/" function))) args))
