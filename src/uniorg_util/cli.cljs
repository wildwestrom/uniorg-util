(ns uniorg-util.cli
  (:require
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.string :as string]
   [uniorg-util.core :as core]))

(def ^:private cli-options
  [["-o" "--output" "Directory to output to"
    :default "."]
   ["-e" "--edn" "Output files as edn instead of json"
    :default false
    :update-fn true]
   ["-h" "--help"]])

(defn- usage [options-summary]
  (->> ["This is a tool designed to make it easy to turn org-documents into html and metadata."
        ""
        "Usage: org-util [OPTIONS] [FILES...]"
        ""
        "By default it will process everything in the current directory."
        ""
        "Options:"
        options-summary]
       (string/join \newline)))

(defn- error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn- validate-args [args]
  (let [{:keys [options errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) {:exit-message (usage summary) :ok? true}
      errors          {:exit-message (error-msg errors)}
      :else           {:exit-message (usage summary)})))

(defn- exit [status msg]
  (println msg)
  (.exit js/process status))

;; TODO: Finish CLI
;; (defn ^:export main
;;   []
;;   ())

(comment
  ("uniorg-util -e")
  (create-files "." "." )


  )
