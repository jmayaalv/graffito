(ns graffito.board-game-geek.bgg-test
  (:require
   [clojure.test :refer [deftest is testing use-fixtures]]
   [graffito.board-game-geek.resolver :as resolver]
   [graffito.core :as graffito]
   [graffito.test-utils :as t.utils]))

(use-fixtures :once (t.utils/with-env
                      (graffito/load-schema! "cgg-schema.edn")
                      (resolver/index)
                      resolver/data))

(deftest querys
  #_(testing "A simple query"
      (is (= {:data {:game_by_id {:id   "1236"
                                  :name "Tiny Epic Galaxies"}}}
           (t.utils/query "{ game_by_id (id: \"1236\") { id name }}"))))

  #_(testing "with attribute overrides and pathom placeholder"
    (is (= {:data {:member_by_id {:id          "1410"
                                  :member_name "bleedingedge"
                                  :ratings     [{:rating 4 :game {:name "7 Wonders: Duel"}}
                                                {:rating 4 :game {:name "Tiny Epic Galaxies"}}
                                                {:rating 5 :game {:name "Zertz"}}]}}}
           (t.utils/query "{ member_by_id(id: \"1410\") { id member_name ratings { rating game { name }}}}"))))

  #_(testing "Multiple queries"
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
                     {:id             "1237"
                      :name           "7 Wonders: Duel"
                      :rating_summary {:count 3 :average 4.333333333333333}
                      :designers      [{:name "Antoine Bauza" :games [{:name "7 Wonders: Duel"}]}
                                       {:name "Bruno Cathala" :games [{:name "7 Wonders: Duel"}]}]}}}
             (t.utils/query "{ game_by_id (id: \"1237\") { id name rating_summary { count average }  designers { name games { name }}}}")))))

(deftest mutations
  (testing "single mutation"
    (is (= {}
           (t.utils/query "mutation { rate_game(member_id: \"1410\", game_id: \"1235\", rating: 4) { name rating_summary { count average }}}")))))
