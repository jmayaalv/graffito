(ns graffito.lacinia.eql-test
  (:require [clojure.test :refer [deftest testing is]]
            [graffito.lacinia.eql :as g.eql]
            [graffito.core :as graffito]))

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
           (g.eql/attribute-to-field  (graffito/compile (graffito/load-schema! "cgg-schema.edn"))
                                      [:BoardGame/id :BoardGame/name
                                       {:BoardGame/rating_summary [:GameRatingSummary/count :GameRatingSummary/average]}
                                       {:BoardGame/designers [:Designer/name {:Designer/games [:BoardGame/name]}]}])))))
