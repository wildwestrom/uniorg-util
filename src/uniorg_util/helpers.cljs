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
  (let [p (fs/realpath path)]
    (map #(str p "/" %) (fs/readdir p))))


(defn filter-ext
  [files ext]
  (filter #(-> % fs/ext (= ext))
          files))

(defn remove-extension
  [file]
  (first (string/split (fs/basename file) #"\.")))
