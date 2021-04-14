(ns graffito.board-game-geek.bgg-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [graffito.core :as graffito]
            [graffito.board-game-geek.resolver :as resolver]
            [graffito.test-utils :as t.utils]))


(use-fixtures :once (t.utils/with-env
                      (graffito/load-schema! "cgg-schema.edn")
                      (resolver/index)))

(deftest querys
  (testing "A simple query"
    (is (= {:data {:game_by_id {:id   "1236"
                                :name "Tiny Epic Galaxies"}}}
           (t.utils/query "{ game_by_id (id: \"1236\") { id name }}"))))

  (testing "An Attribute override"
    (is (= {:data {:member_by_id {:id          "1410"
                                  :member_name "bleedingedge"}}}
           (t.utils/query "{ member_by_id(id: \"1410\") { id member_name}}"))))

  (testing "Multiple queries"
    (is (= {:data {:game_by_id   {:id   "1236"
                                  :name "Tiny Epic Galaxies"}
                   :game_by_name {:id   "1234"
                                  :name "Zertz"
                                  :summary
                                  "Two player abstract with forced moves and shrinking board"}}}
           (t.utils/query "{ game_by_id (id: \"1236\") { id name }
                             game_by_name(name: \"Zertz\") { id name summary}}"))))

  (testing "With joins"
    (is (= {:data {:game_by_id
                   {:id        "1237"
                    :name      "7 Wonders: Duel"
                    :designers [{:name "Antoine Bauza" :games [{:name "7 Wonders: Duel"}]}
                                {:name "Bruno Cathala" :games [{:name "7 Wonders: Duel"}]}]}}}
         (t.utils/query "{ game_by_id (id: \"1237\") { id name designers { name games { name }}}}")))))
