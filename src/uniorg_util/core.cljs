(ns uniorg-util.core
  (:require ["unified" :as unified]
            ["rehype-stringify" :as html]
            ["uniorg-parse" :as uniorg]
            ["uniorg-rehype" :as rehype]
            ["uniorg-extract-keywords" :refer (extractKeywords)]
            [cljs-node-io.core :as io :refer [slurp spit]]
            [cljs-node-io.fs :as fs]
            [uniorg-util.helpers :as h]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; The actual program.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^:private processor
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

(defn org->metadata
  [org-string]
  (js->clj (.-data (process-org org-string))
           :keywordize-keys :true))

(defn post-map
  [org-string]
  {:content (org->html org-string)
   :meta    (org->metadata org-string)})

(defn- create-out-dir
  [out-path]
  (when-not (fs/dir? out-path)
    (fs/mkdir out-path)))

(defn blog-post
  [file]
  (post-map (slurp file)))

(defn- gen-file-title [file]
  (let [get-from-meta (fn [file key]
                        (-> file :meta key))]
    (first
      (filter #(->> % nil? not)
              (list
                (get-from-meta file :id)
                (get-from-meta file :title)
                (get-from-meta file :original-filename))))))

(defn jsonify [file]
  (-> file
      clj->js
      js/JSON.stringify))

(defn- apply-jsonify?
  "Returns the result of `(jsonify)` on `exp`."
  [json? file]
  (cond-> file
    json? jsonify))

(defn gen-all-posts
  [out-path files extension json?]
  (doseq [file files]
    (spit (str out-path "/" (gen-file-title file) extension)
          (apply-jsonify? json? file))))

(defn gen-list-of-posts
  [out-path files extension json? out-filename]
  (spit (str out-path "/" out-filename extension)
        (apply-jsonify?
          json?
          (for [file files]
            (str (gen-file-title file) extension)))))

(defn create-files
  ([in-path out-path json?]
   (create-files in-path out-path json? nil))
  ([in-path out-path json? manifest]
   (create-out-dir out-path)
   (let [input     (h/filter-ext (h/files-in-dir in-path) ".org")
         files     (map #(assoc (blog-post %) :meta
                                (conj (:meta (blog-post %))
                                      {:original-filename (h/remove-extension (fs/basename %))})) input)
         ;;TODO Clean up `files`
         extension (if json? ".json" ".edn")]
     (dorun
       (gen-all-posts out-path files extension json?)
       (when manifest
         (gen-list-of-posts out-path files extension json? manifest))))))

(defn ^:export main "
  Set `json?` to `false` for edn output.
  Both `in-path` and `out-path` take strings
  without trailing slashes as file paths."
  ([in-path out-path]
   (main in-path out-path true))
  ([in-path out-path json?]
   (create-files in-path out-path json? "_ALL_FILES"))
  ([in-path out-path json? manifest]
   (create-files in-path out-path json? manifest)))
