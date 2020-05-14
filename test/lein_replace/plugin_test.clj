(ns lein-replace.plugin-test
  (:require [clojure.test :refer :all]
            [lein-replace.plugin :refer :all]))

(deftest path-test
  (is (= [:a] (make-path :a)))
  (is (= [:a] (make-path [:a])))
  (is (= '(:a) (make-path '(:a))))

  (is (= :b (get-path {:a :b} :a)))
  (is (= :b (get-path {:a {:a {:a :b}}} [:a :a :a]))))

(deftest replace-in-coord-test
  (is (= [:a :b "1.0" :c :d]
         (replace-in-coord {:version "1.0"}
                           [:a :b :version :c :d]))))

(deftest replace-in-path-test
  (is (= {:dependencies [[:a :b "1.0" :c :d]]}
         (replace-in-path {:version "1.0"}
                          {:dependencies [[:a :b "1.0" :c :d]]}
                          :dependencies)))

  (is (= {:dependencies [[:a :b "1.0" :c :d]]}
         (replace-in-path {:version "1.0"}
                          {:dependencies [[:a :b "1.0" :c :d]]}
                          [:dependencies]))))

(deftest build-replace-map-test

  (is (= {:version "1.0"}
         (build-replace-map {:version "1.0"}
                            {:version :version})))

  (is (= {::version "1.0"}
         (build-replace-map {:version "1.0"}
                            {::version :version})))

  (is (= {::version "1.0"}
         (build-replace-map {:nested {:version "1.0"}}
                            {::version [:nested :version]}))))

(deftest replace-with-expr-test
  (is (= {:version      "1.0"
          :dependencies [['foo/bar "1.0"]]}
         (replace-with-expr {:version      "1.0"
                             :dependencies [['foo/bar :version]]}
                            version-replace))))

(deftest middleware-test

  (is (= {:version      "1.0"
          :dependencies [['foo/bar "1.0"]]}
         (middleware {:version      "1.0"
                      :dependencies [['foo/bar :version]]})))

  (is (= {:version             "1.0"
          :dependencies        [['same/version "1.0"]
                                ['group1/artifact1 "1.2"]
                                ['group1/artifact2 "1.2"]
                                ['group2/artifact1 "1.3"]
                                ['group2/artifact2 "1.3"]]
          :versions            {:group1 "1.2" :group2 "1.3"}
          :replace-expressions [{:replacements {:group1-version
                                                [:versions :group1]
                                                :group2-version
                                                [:versions :group2]}
                                 :paths        [:dependencies]}]}
         (middleware {:version             "1.0"
                      :dependencies        [['same/version :version]
                                            ['group1/artifact1 :group1-version]
                                            ['group1/artifact2 :group1-version]
                                            ['group2/artifact1 :group2-version]
                                            ['group2/artifact2 :group2-version]]
                      :versions            {:group1 "1.2" :group2 "1.3"}
                      :replace-expressions [{:replacements {:group1-version
                                                            [:versions :group1]
                                                            :group2-version
                                                            [:versions :group2]}
                                             :paths        [:dependencies]}]})))

  (is (= {:version          "1.0"
          :replace-version? false
          :dependencies     [['foo/bar :version]]}
         (middleware {:version          "1.0"
                      :replace-version? false
                      :dependencies     [['foo/bar :version]]}))))
