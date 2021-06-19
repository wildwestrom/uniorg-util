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
  (filter fs/file? (fs/readdir ".")))

(defn filter-for-org-documents
  [files]
  (filter #(-> % fs/ext (= ".org"))
          files))

(defn remove-extension
  [file]
  (first (string/split file #"\.")))
