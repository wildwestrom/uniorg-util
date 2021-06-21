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
                     :list    '("here" "is" "stuff")
                     :vec     [1 2 3 4 "string"]
                     :regex   #"this is a regex"
                     :char    \c}]
    (testing "is the output valid json?"
      (is (valid-json?
            (sut/apply-jsonify? true clojure-map))))
    (testing "is the output valid edn?"
      (is (valid-edn?
            (sut/apply-jsonify? false clojure-map))))))

(defn file-ext-test
  [edn? test-type ext]
  (sut/-main "-i" test-in-dir "-o" test-out-dir edn?)
  (let [dirs      (h/files-in-dir test-out-dir)
        is-or-not (condp = test-type
                    '=    "is"
                    'not= "is not")]
    (testing (str "when \"-e\" " is-or-not "used, output to `" ext "`.")
      (is (test-type (h/filter-ext dirs ext)
                     dirs))))
  (cleanup test-out-dir))

(deftest file-extensions
  (file-ext-test nil '=    ".json")
  (file-ext-test "-e"'=    ".edn")
  (file-ext-test nil 'not= ".edn")
  (file-ext-test "-e"'not= ".json"))

(deftest all-posts-generated
  (testing "Each input file corresponds to an output file when generating the posts."
    (do
      (sut/-main "-i" test-in-dir "-o" test-out-dir)
      (is (= (count (h/files-in-dir test-in-dir))
             (count (h/files-in-dir test-out-dir))))
      (cleanup test-out-dir))))
