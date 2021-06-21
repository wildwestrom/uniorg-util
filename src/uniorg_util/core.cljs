(ns uniorg-util.core
  (:require ["unified" :as unified]
            ["rehype-stringify" :as html]
            ["uniorg-parse" :as uniorg]
            ["uniorg-rehype" :as rehype]
            ["uniorg-extract-keywords" :refer (extractKeywords)]
            [cljs-node-io.core :as io :refer [slurp spit]]
            [cljs-node-io.fs :as fs]
            [uniorg-util.helpers :as h]
            [uniorg-util.cli :as cli]))

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

(defn get-from-meta
  [file key]
  (-> file :meta key))

(defn- gen-file-title
  [file]
  (get-from-meta file :filename))

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

(defn add-filename-to-metadata
  [filepath]
  (map #(assoc (blog-post %) :meta
               (conj (:meta (blog-post %))
                     {:filename (h/remove-extension
                                  (fs/basename %))}))
       filepath))

(defn create-files
  ([in-path out-path json? manifest]
   (create-out-dir out-path)
   (let [input     (h/filter-ext (h/files-in-dir in-path) ".org")
         files     (add-filename-to-metadata input)
         extension (if json? ".json" ".edn")]
     (dorun
       (gen-all-posts out-path files extension json?)
       (when manifest
         (gen-list-of-posts out-path files extension json? manifest))))))

(defn -main [& args]
  (let [{:keys [options exit-message ok?]} (cli/validate-args args)]
    (if exit-message
      (cli/exit (if ok? 0 1) exit-message)
      (create-files
        (:input options)
        (:output options)
        (not (:edn options))
        (:manifest options)))))
