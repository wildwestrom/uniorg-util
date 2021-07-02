(ns uniorg-util.helpers
  (:require [cljs-node-io.fs :as fs]
            [cljs-node-io.core :as io]
            [clojure.string :as string]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helper functions.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn files-in-dir
  "Like `file-seq` but not recursive.
  Returns strings."
  [path]
  (map #(str path "/" %) (fs/readdir path)))


(defn filter-ext
  [files ext]
  (filter #(-> % fs/ext (= ext))
          files))

(defn remove-extension
  [file]
  (first (string/split (fs/basename file) #"\.")))

(defn get-version
  []
  (:version (js->clj (.parse js/JSON (io/slurp "package.json")) :keywordize-keys true)))

(def version-string
  (get-version))
