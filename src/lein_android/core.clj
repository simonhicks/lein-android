(ns lein-android.core
  (:require [lein-android.create :as cr]))

(defn create 
  [options]
  (let [options (apply hash-map options)]
    (doto (-> options (cr/validate) (cr/apply-defaults))
      (cr/gen-src-file)
      (cr/gen-default-properties)
      (cr/gen-local-properties)
      (cr/gen-string-xml)
      (cr/gen-build-xml)
      (cr/gen-manifest)
      (cr/gen-lein-file)
      (cr/gen-build-properties)
      (cr/copy-icons)
      (cr/final-message))))

(defn help
  [[cmd]]
  (println ({
  "create"
  "
  'lein android create' is used to generate an empty android app project.
  Options are :project, :package, :target, :path and :activity. You must 
  specify at least :package and either :project or :activity. The rest 
  are optional.

  Example: 
  lein android create :project my-android-app :package org.example.app :activity ExampleActivity :path my_android_app :target android-9

  "
  } cmd (str "No help found for task " cmd))))
