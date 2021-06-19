(ns uniorg-util.core-test
  (:require [uniorg-util.core :as sut]
            [uniorg-util.helpers :as h]
            [cljs-node-io.fs :as fs]
            [cljs.reader :as reader]
            [cljs.test :refer-macros [deftest testing is are]]))

(def test-in-dir "test-posts")
(def test-out-dir "test-output")

(defn cleanup
  [path]
    (when (fs/dir? path)
      (fs/rm-r path)))

(deftest file-extensions
  (let [file-ext-test
        (fn [json? ext test-type]
            (sut/create-files test-in-dir test-out-dir json?)
            (let [dirs (h/files-in-dir test-out-dir)]
              (testing (str "when `json?` is `" json? "`, output to `" ext "`.")
                (is (test-type (h/filter-ext dirs ext) dirs))))
            (cleanup test-out-dir))]
    (file-ext-test true  ".json" =)
    (file-ext-test false ".edn"  =)
    (file-ext-test true  ".edn"  not=)
    (file-ext-test false ".json" not=)))

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

(deftest jsonification
  (let [clojure-map {:keyword :kw
                     :list    '("here" "is" "some")
                     :vec     [1 2 3 4 "fuckerfuck"]
                     :regex   #"assbags"}]
    (testing "is it valid json?"
      (is (valid-json?
            (sut/apply-jsonify? true clojure-map))))
    (testing "is it valid edn?"
      (is (valid-edn?
            (sut/apply-jsonify? false clojure-map))))
    ))

#_(cljs.test/run-all-tests)
