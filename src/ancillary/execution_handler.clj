(ns ancillary.execution-handler
  (:require [clojure.java.shell :as shell]))

(defn exec-sh
  "Executes and handles shell commands."
  [command]
  (let [result (shell/sh "bash" "-c" command)]
    (cond (= (get result :exit) 0) (def status 200)
          :else (def status 510))
    {:status status
      :stdout (get result :out)
      :stderr (get result :err)}))
