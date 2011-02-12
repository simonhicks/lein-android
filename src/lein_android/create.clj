(ns lein-android.create
  (:use lein-android.templates :reload)
  (:use [clojure.java.io :only (file make-parents)])
  (:use [clojure.contrib.duck-streams :only (copy)])
  (:use [clojure.string :only (lower-case join capitalize)])
  (:use [clojure.contrib.str-utils :only (re-split re-gsub)]))

(defn src-file
  "returns a string containing the source code for a clojure/android hello world
  program"
  [{:keys [package activity]}]
  (format src-file-template package activity))

(defn default-properties 
  "returns the string used for building a default.properties file"
  [{:keys [target]}]
  (str auto-generated-file-warning "# Project target.\ntarget=" target))

(def sdk-dir
  (System/getenv "ANDROID_SDK_HOME"))

(defn local-properties 
  "returns the string used for building a local.properties file"
  [] (str auto-generated-file-warning "# Android SDK location.\nsdk.dir=" sdk-dir))

(defn strings-xml
  "returns the string used for building res/values/strings.xml"
  [{:keys [activity]}]
  (format strings-xml-template activity))

(defn build-xml
  "returns a string of the contents of the build.xml file"
  [{:keys [project package]}]
  (format build-xml-template project package))

(defn manifest
  "returns a string of the contents of the AndroidManifest.xml file"
  [{:keys [package activity]}]
  (format manifest-template package activity))

(defn lein-file
  [{:keys [project]}]
  (format lein-file-template project))

(defn build-properties []
  build-properties-template)

(defn validate 
  [{:keys [project target package activity] :as options}]
  (cond
    (not (or project activity))
      (throw (Exception. "Invalid options. Either activity or project must be specified"))
    (not package)
      (throw (Exception. "Package must be specified"))
    (and activity (not (re-matches #"\S+" activity)))
      (throw (Exception. "Activity must be a valid java classname"))
    (and activity (not (re-matches #"\A[A-Z].+" activity)))
      (throw (Exception. "Activity must start with a capital letter"))
    (and project (not (re-matches #"\S+" project)))
      (throw (Exception. "Project must be a valid clojure symbol"))
    (and target (not (re-matches #"android-\d" target)))
      (throw (Exception. "Invalid target. Try 'android-9'"))
    (and package (not (re-matches #"\w+(\.\w+)*" package)))
      (throw (Exception. "Package must contain only alphanumeric, dot and underscore characters"))
    :else 
      options))

(defn projectify [word]
  (->> word
    (re-seq #"[A-Z][a-z0-9]*")
    (join "-")
    (apply str)
    (lower-case)))

(defn camel-case [word]
  (->> word
    (filter #(re-matches #"[\-A-Za-z0-9]" (str %)))
    (apply str)
    (re-split #"-")
    (map capitalize)
    (join)))

(defn apply-defaults
  [{:keys [project path activity] :as options}]
  (let [default-proj (if activity (projectify activity))
        default-actv (if project (camel-case project))
        default-path (re-gsub #"-" "_" (or project default-proj))]
    (merge {:target "android-9" :path default-path :project default-proj :activity default-actv} 
           options)))

(defn gen-file
  [file-name string {path :path}]
  (let [full-path (str path "/" file-name)]
    (make-parents full-path)
    (when-not (.exists (file full-path))
      (spit full-path string))))

(defn copy-file
  [[from to] {path :path}]
  (let [full-path (str path "/" to)]
    (make-parents full-path)
    (copy (file from) (file full-path))))

(defn gen-src-file
  [{:keys [path package] :as options}]
  (let [full-path (str path "/src/" (re-gsub #"\." "/" package) ".clj")]
    (make-parents full-path)
    (spit full-path (src-file options))))

(defn gen-default-properties [options]
  (gen-file "default.properties" (default-properties options) options))

(defn gen-local-properties [options]
  (gen-file  "local.properties" (local-properties) options))

(defn gen-string-xml [options]
  (gen-file "res/values/strings.xml" (strings-xml options) options))

(defn gen-build-xml [options]
  (gen-file "build.xml" (build-xml options) options))

(defn gen-manifest [options]
  (gen-file "AndroidManifest.xml" (manifest options) options))

(defn gen-lein-file [options]
  (gen-file "project.clj" (lein-file options) options))

(defn gen-build-properties [options]
  (gen-file "build.properties" (build-properties) options))

(defn icon-locations [density]
  [(str sdk-dir "/platforms/android-9/data/res/drawable-" density "/sym_def_app_icon.png")
   (str "res/drawable-" density "/icon.png")])

(defn copy-icons [options]
  (doseq [d ["hdpi" "mdpi" "ldpi"]] 
    (copy-file (icon-locations d) options)))

(defn final-message [{:keys [path project]}]
  (println "Created" project "in" path)
  (println "Remember to download dependencies before compiling"))

