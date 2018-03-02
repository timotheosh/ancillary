(ns ancillary.modules
  (:require [cemerick.pomegranate :as pom]
            [clojure.reflect :as reflect]))

(def dcl (clojure.lang.DynamicClassLoader.))

(defn dynamically-load-class!
  [class-loader class-name]
  (let [class-reader (clojure.asm.ClassReader. class-name)]
    (when class-reader
      (let [bytes (.-b class-reader)]
        (.defineClass class-loader
                      class-name
                      bytes
                      "")))))

(defn load-module
  "Loads a custom module given the path and class name."
  [url-string classname]
  (try
    (require (symbol classname))
    (catch java.io.FileNotFoundException e
      (pom/add-classpath url-string)
      (dynamically-load-class! dcl classname))))

(defn available-methods
  "Returns a list of available methods of a class/namespace"
  [classname]
  (let [cn (symbol classname)]
    (eval
     `(sort
       (map :name
            (filter :return-type
                    (:members
                     (reflect/reflect ~cn))))))))

(defn allowed-methods
  "Returns the functions that correspond to http methods for a givemn class."
  [classname]
  (let [methods #{'GET 'HEAD 'PUT 'POST 'DELETE 'OPTIONS 'TRACE 'PATCH}]
    (vec
     (map #(keyword (.toLowerCase (str %)))
          (clojure.set/intersection
           methods
           (set (available-methods classname)))))))

(defn mod-func
  "Executes the given method and arguments to that method for the loaded class.
   Should return something for ring."
  [classname function args]
  ((resolve
    (symbol
     (str (clojure.string/replace classname "_" "-") "/" function))) args))
