(ns leiningen.android
  "A set of tasks for android development. See 'lein help android' for more info."
  (:use lein-android.core :reload)
  (:use [clojure.contrib.str-utils2 :only (join)]))

(defn- get-options [more]
  (->> more 
    (map read-string) 
    (map #(if (symbol? %) (str %) %))))

(defn android
  "
  Lein-android is a set of tools to facilitate android development in clojure.
  Usage:     lein android <sub-task> <options>
   
  eg. lein android create :project hello-world :package org.hello.world :activity HelloWorld
   
  Currently, the only subtasks are create & help, but hopefully more will be added 
  when I get round to it.
  "
  [& args]
  (let [[cmd & more] args
        options (get-options more)]
    ((resolve (symbol (str "lein-android.core/" cmd))) options)))
