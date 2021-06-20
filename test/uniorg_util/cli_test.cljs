(ns uniorg-util.cli-test
  (:require
   [clojure.tools.cli :refer [parse-opts]]
   [uniorg-util.cli :as sut]
   [cljs.test :refer-macros [deftest testing is]]))

(defn parsed-opts
  [& args]
  (parse-opts args sut/cli-options))

(defn opts-map [args]
  (:options (parsed-opts args)))

(deftest edn
  (let [test-edn #(:edn (opts-map %))]
    (testing "Testing to see if EDN flag parses properly"
      (is (true?  (test-edn "-e")))
      (is (true?  (test-edn "-em")))
      (is (true?  (test-edn "-e -m")))
      (is (true?  (test-edn "--edn -m")))
      (is (true?  (test-edn "--edn --manifest filename")))
      (is (false? (test-edn "-m filename"))))))

(deftest manifest
  (let [test-manifest #(:manifest (opts-map %))]
    (testing "Testing to make sure the manifest flag works correctly"
      (is (false? (test-manifest "-e")))
      (is (true?  (test-manifest "-m")))
      (is (false? (test-manifest "--edn")))
      (is (true?  (test-manifest "--manifest")))
      (is (true?  (test-manifest "--manifest filename")))
      (is (true?  (test-manifest "--edn --manifest filename")))
      )))

;; TODO: Complete tests for the CLI.
