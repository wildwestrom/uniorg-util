(ns uniorg-util.helpers
  (:require [cljs-node-io.fs :as fs]
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
  (filter #(-> % fs/ext (= ext)) files))

(defn remove-extension
  [file]
  (first (string/split (fs/basename file) #"\.")))

(def version-string
  "0.2.1")
