(ns graffito.lacinia.eql-test
  (:require [clojure.test :refer [deftest testing is]]
            [graffito.lacinia.eql :as g.eql]
            [graffito.core :as graffito]
            [com.walmartlabs.lacinia.constants :as constants]
            [graffito.board-game-geek.resolver :as resolver]
            [graffito.lacinia.schema :as schema]
            [com.walmartlabs.lacinia :as lacinia]
            [com.walmartlabs.lacinia.schema :as lacinia.schema]
            [com.wsscode.pathom3.connect.indexes :as pci]))

(deftest selection-fields
  (testing "joined selection"
    (is (= [:BoardGame/id :BoardGame/name {:BoardGame/rating_summary [:GameRatingSummary/count :GameRatingSummary/average]} {:BoardGame/designers [:Designer/name {:Designer/games [:BoardGame/name]}]} ]
           (g.eql/selection-fields #:BoardGame{:id   [nil]
                                               :name [nil]
                                               :rating_summary
                                               [{:selections
                                                 #:GameRatingSummary{:count [nil] :average [nil]}}]
                                               :designers
                                               [{:selections
                                                 #:Designer{:name [nil]
                                                            :games
                                                            [{:selections #:BoardGame{:name [nil]}}]}}]}))))
  (testing "Placeholder"
    (is (= [:Member/id :Member/member_name #:Member{:ratings [:GameRating/rating #:GameRating{:game [:BoardGame/name]}]}]
           (g.eql/selection-fields #:Member{:id          [nil],
                                            :member_name [nil],
                                            :ratings
                                            [{:selections
                                              #:GameRating{:rating [nil],
                                                           :game
                                                           [{:selections #:BoardGame{:name [nil]}}]}}]})))))


(deftest attribute-to-fields
  (testing "joined selection"
      (is (= {:board-game/id               :BoardGame/id
              :board-game/name             :BoardGame/name
              :board-game/rating-summary   :BoardGame/rating_summary
              :board-game/designers        :BoardGame/designers
              :game-rating-summary/count   :GameRatingSummary/count
              :game-rating-summary/average :GameRatingSummary/average
              :designer/name               :Designer/name
              :designer/games              :Designer/games}
             (g.eql/attribute-to-field  {::constants/schema     (graffito/compile (graffito/load-schema! "cgg-schema.edn"))
                                         :pathom/index          (resolver/index)
                                         :pathom/available-data {:board-game/id {}}}
                                      [:BoardGame/id :BoardGame/name
                                       {:BoardGame/rating_summary [:GameRatingSummary/count :GameRatingSummary/average]}
                                       {:BoardGame/designers [:Designer/name {:Designer/games [:BoardGame/name]}]}]))))
 (testing "placeholder"
   (is (= {:member/id       :Member/id
           :member/name     :Member/member_name
           :member/ratings  :Member/ratings
           :rating/value    :GameRating/rating
           :board-game/name :BoardGame/name}
          (g.eql/attribute-to-field {::constants/schema     (graffito/compile (graffito/load-schema! "cgg-schema.edn"))
                                     :pathom/index          (resolver/index)
                                     :pathom/available-data {:member/id {}}}
                                    [:Member/id :Member/member_name #:Member{:ratings [:GameRating/rating #:GameRating{:game [:BoardGame/name]}]}])))))



(deftest attributes-vec
  (testing "joined selection"
    (is (= [:board-game/id :board-game/name
            {:board-game/rating-summary [:game-rating-summary/count :game-rating-summary/average]}
            {:board-game/designers [:designer/name #:designer{:games [:board-game/name]}]}]
           (g.eql/attributes-vec  {:board-game/id               :BoardGame/id
                                   :board-game/name             :BoardGame/name
                                   :board-game/rating-summary   :BoardGame/rating_summary
                                   :board-game/designers        :BoardGame/designers
                                   :game-rating-summary/count   :GameRatingSummary/count
                                   :game-rating-summary/average :GameRatingSummary/average
                                   :designer/name               :Designer/name
                                   :designer/games              :Designer/games}
                                  [:BoardGame/id :BoardGame/name
                                   {:BoardGame/rating_summary [:GameRatingSummary/count :GameRatingSummary/average]}
                                   {:BoardGame/designers [:Designer/name {:Designer/games [:BoardGame/name]}]}]))))
  (testing "joined selection with placeholder"
    (is (=  [:member/id  :member/name {:member/ratings [:rating/value {:>/game [:board-game/name]}]}]
            (g.eql/attributes-vec  {:member/id       :Member/id
                                    :member/name     :Member/member_name
                                    :member/ratings  :Member/ratings
                                    :rating/value    :GameRating/rating
                                    :board-game/name :BoardGame/name}
                                   [:Member/id :Member/member_name #:Member{:ratings [:GameRating/rating #:GameRating{:game [:BoardGame/name]}]}])))))
