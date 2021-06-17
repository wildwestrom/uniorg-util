(ns uniorg-util
  (:require ["unified" :as unified]
            ["rehype-stringify" :as html]
            ["uniorg-parse" :as uniorg]
            ["uniorg-rehype" :as rehype]
            ["uniorg-extract-keywords" :refer (extractKeywords)]
            ;; [clojure.string :as string]
            [cljs-node-io.core :as io :refer [slurp spit file-seq]]
            [cljs-node-io.fs :as fs]))

;; Command line stuff

;; (def cli-options
;;   [["-o" "--output" "Directory to output to"
;;     :default "."]
;;    ["-e" "--edn" "Output files as edn instead of json"
;;     :default :false
;;     :update-fn :true]
;;    ["-h" "--help"]])

;; (defn usage [options-summary]
;; (->> ["This is a tool designed to make it easy to turn org-documents into html and metadata."
;;       ""
;;       "Usage: org-util [OPTIONS] [FILES...]"
;;       ""
;;       "By default it will process everything in the current directory."
;;       ""
;;       "Options:"
;;       options-summary]
;;      (string/join \newline)))

;; (defn error-msg [errors]
;; (str "The following errors occurred while parsing your command:\n\n"
;;      (string/join \newline errors)))

;; (defn validate-args [args]
;; (let [{:keys [options errors summary]} (parse-opts args cli-options)]
;;   (cond
;;     (:help options) {:exit-message (usage summary) :ok? true}
;;     errors          {:exit-message (error-msg errors)}
;;     :else           {:exit-message (usage summary)})))

;; (defn exit [status msg]
;; (println msg)
;; (.exit js/process status))

;; The actual program.

(def processor
  (->
    (unified)
    (.use uniorg)
    (.use extractKeywords)
    (.use rehype)
    (.use html)))

(defn process-org
  [org]
  (.processSync processor org))

(defn org->html
  [org-string]
  (.-contents (process-org org-string)))

(defn org->data
  [org-string]
  (js->clj (.-data (process-org org-string))
           :keywordize-keys :true))

(defn post-map
  [org-string]
  {:content (org->html org-string)
   :data    (org->data org-string)})

(defn create-out-dir
  [out-path]
  (when-not (.isDirectory (io/file out-path))
    (fs/mkdir out-path)))

(defn blog-post
  [file]
  (post-map (slurp file)))

(defn get-id [path-to-file]
  (-> path-to-file
      :data
      :id))

(defn jsonify [path-to-file]
  (-> path-to-file
      clj->js
      js/JSON.stringify))

(defn gen-all-posts
  [out-path files extension]
  (doseq [file files]
    (spit (str out-path "/" (get-id file) extension)
          (jsonify file))))

(defn gen-list-of-posts
  [out-path files extension]
  (spit (str out-path "/_ALL_POSTS" extension)
        (jsonify (for [file files]
                   (get-id file)))))

(defn create-files [in-path out-path]
  (doall
    (create-out-dir out-path)
    (let [input     (rest (file-seq in-path))
          files     (map blog-post input)
          extension ".json"]
      (gen-all-posts out-path files extension)
      (gen-list-of-posts out-path files extension))))

(defn ^:export -main
  [in-path out-path]
  (create-files in-path out-path))

;; (-main "test-posts" "json-data")
