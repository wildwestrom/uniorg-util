(ns uniorg-util.cli-test
  (:require
   [clojure.tools.cli :refer [parse-opts]]
   [cljs-node-io.fs :as fs]
   [uniorg-util.cli :as sut]
   [uniorg-util.helpers :refer [version-string]]
   [cljs.test :refer-macros [deftest testing is]]
   [clojure.string :as string]))

(defn options-test
  [& opts]
  (:options (parse-opts opts sut/cli-options)))

(defn errors-test
  [& opts]
  (:errors (parse-opts opts sut/cli-options)))

(deftest edn
  (testing "EDN flag parses properly"
    (is (true?  (:edn (options-test "-e"))))
    (is (true?  (:edn (options-test "--edn"))))
    (is (true?  (:edn (options-test "-em"))))
    (is (true?  (:edn (options-test "-e" "-m"))))
    (is (true?  (:edn (options-test "--edn" "-m"))))
    (is (true?  (:edn (options-test "--edn" "--manifest" "filename"))))
    (is (false? (:edn (options-test "-m" "filename"))))))

(deftest manifest
  (testing "Manifest flag works correctly"
    (is (not (nil?    (errors-test "-m"))))
    (is (not (nil?    (errors-test "--manifest"))))
    (is (= "filename" (:manifest (options-test "-m" "filename"))))
    (is (= "filename" (:manifest (options-test "--manifest" "filename"))))
    (is (= "filename" (:manifest (options-test "--edn" "--manifest" "filename"))))))

(deftest input-output
  (testing "Make sure input/output works\n"
    (testing "Give errors on invalid input dir:"
      (is (not (nil? (errors-test "-i" "directory"))))
      (is (not (nil? (errors-test "--input" "directory")))))
    (testing "Return path on correct input dir:"
      (is (= "."          (:input  (options-test nil))))
      (is (= "test-posts" (:input  (options-test "-i" "test-posts"))))
      (is (= "test-posts" (:input  (options-test "--input" "test-posts")))))
    (testing "Output:"
      (is (= "."         (:output (options-test nil))))
      (is (= "directory" (:output (options-test "-o" "directory"))))
      (is (= "directory" (:output (options-test "--output" "directory")))))))

(deftest help-menu
  (testing "Parse help"
    (is (true? (:help (options-test "-h"))))
    (is (true? (:help (options-test "--help"))))
    (is (true? (:help (options-test "--edn" "-h"))))))

(deftest version-info
  (testing "Parse version"
    (is (true? (:version (options-test "-V"))))
    (is (true? (:version (options-test "--version"))))
    (is (true? (:version (options-test "--edn" "-V"))))
    (is (true? (:version (options-test "--help" "--version")))))
  (testing "Version output"
    (is (= version-string (:exit-message (sut/validate-args ["--version"]))))))
