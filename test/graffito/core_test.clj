(ns graffito.core-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [graffito.core :as graffito]
            [graffito.resolver :as test-resolver]
            [graffito.test-utils :as t.utils]))



(use-fixtures :once (t.utils/with-env
                      (graffito/load-schema! "cgg-schema.edn")
                      (test-resolver/index)))

(deftest querys
  (testing "A simple query"
    (is (= {:data {:game_by_id {:id   "1236"
                                :name "Tiny Epic Galaxies"}}}
             (t.utils/query "{ game_by_id (id: \"1236\") { id name }}"))))

  (testing "Multiple queries "
    (is (= {:data {:game_by_id   {:id   "1236"
                                  :name "Tiny Epic Galaxies"}
                   :game_by_name {:id      "1234"
                                  :name    "Zertz"
                                  :summary "Two player abstract with forced moves and shrinking board"}}}
           (t.utils/query "{ game_by_id (id: \"1236\") { id name }
                             game_by_name(name: \"Zertz\") { id name summary}}"))))

  (testing "A join"
    (is (= {:data {:game_by_id {:id        "1236"
                                :name      "Tiny Epic Galaxies"
                                :designers [{:name "Scott Almes"}]}}}
         (t.utils/query "{ game_by_id (id: \"1236\") { id name designers { name }}}")))))
