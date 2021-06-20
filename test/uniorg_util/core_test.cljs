(ns uniorg-util.core-test
  (:require [uniorg-util.core :as sut]
            [uniorg-util.helpers :as h]
            [cljs-node-io.fs :as fs]
            [cljs.reader :as reader]
            [cljs.test :refer-macros [deftest testing is]]))

(def test-in-dir "test-posts")
(def test-out-dir "test-output")

(defn cleanup
  [path]
  (when (fs/dir? path)
    (fs/rm-r path)))

(deftest remove-extension
  (is (= (h/remove-extension "~/.config/README.org") "README")))


(defn file-ext-test
  [json? test-type ext]
  (sut/create-files test-in-dir test-out-dir json? false)
  (let [dirs      (h/files-in-dir test-out-dir)
        is-or-not (condp = test-type
                    '=    "is"
                    'not= "is not")]
    (testing (str "when `json?` " is-or-not " `" json? "`, output to `" ext "`.")
      (is (test-type (h/filter-ext dirs ext)
                     dirs))))
  (cleanup test-out-dir))

(deftest file-extensions
  (file-ext-test true  '=    ".json")
  (file-ext-test false '=    ".edn")
  (file-ext-test true  'not= ".edn")
  (file-ext-test false 'not= ".json"))

(deftest all-posts-generated
  (testing "Each input file corresponds to an output file when generating the posts."
    (do
      (sut/create-files test-in-dir test-out-dir true nil)
      (is (= (count (h/files-in-dir test-in-dir))
             (count (h/files-in-dir test-out-dir))))
      (cleanup test-out-dir))))

(defn valid-json?
  [str]
  (try
    (js/JSON.parse str)
    (catch js/Object e false))
  true)

(defn valid-edn?
  [str]
  (try
    (reader/read-string (sut/jsonify str))
    (catch js/Object e false))
  true)

(deftest jsonify-creates-valid-json
  (let [clojure-map {:keyword :kw
                     :list    '("here" "is" "some")
                     :vec     [1 2 3 4 "fuckerfuck"]
                     :regex   #"assbags"}]
    (testing "is the output valid json?"
      (is (valid-json?
            (sut/apply-jsonify? true clojure-map))))
    (testing "is the output valid edn?"
      (is (valid-edn?
            (sut/apply-jsonify? false clojure-map))))))
