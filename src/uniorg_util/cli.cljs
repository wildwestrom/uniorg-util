(ns uniorg-util.cli
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as string]
            [uniorg-util.helpers :refer [version-string]]
            [cljs-node-io.fs :as fs]))

(def ^:private cli-options
  [["-i" "--input [DIR]" "Directory to read from"
    :default "."
    :validate [#(fs/dir? %) "Not a directory"]]
   ["-o" "--output [DIR]" "Directory to output to"
    :default "."]
   ["-e" "--edn" "Output files as EDN instead of JSON"
    :default false]
   ["-m" "--manifest [NAME]" "Create a list of all-files processed"
    :default false]
   ["-V" "--version"]
   ["-h" "--help"]])

(defn- usage [options-summary]
  (->> ["Easily convert your Org Mode documents into HTML and metadata."
        ""
        "Usage: uniorg-util [OPTIONS] [FILES...]"
        ""
        "By default it will process everything in the current working directory."
        ""
        "Options:"
        options-summary]
       (string/join \newline)))

(defn- error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args [args]
  (let [{:keys [options errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options)    {:exit-message (usage summary) :ok? true}
      (:version options) {:exit-message version-string :ok? true}
      errors             {:exit-message (error-msg errors)}
      options            {:options options}
      :else              {:exit-message (usage summary)})))

(defn exit [status msg]
  (println msg)
  (.exit js/process status))
