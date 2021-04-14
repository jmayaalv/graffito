(ns graffito.lacinia.eql-test
  (:require [clojure.test :refer [deftest testing is]]
            [graffito.lacinia.eql :as g.eql]
            [graffito.core :as graffito]))

(deftest selection-fields
  (testing "joined selection"
    (is (= [:BoardGame/id :BoardGame/name {:BoardGame/designers [:Designer/name {:Designer/games [:BoardGame/name]}]} ]
           (g.eql/selection-fields  #:BoardGame{:id [nil],
                                                  :name [nil],
                                                  :designers
                                                  [{:selections
                                                    #:Designer{:name [nil],
                                                               :games
                                                               [{:selections #:BoardGame{:name [nil]}}]}}]})))))


(deftest attribute-to-fields
  (testing "joined selection"
    (is (= {:board-game/id        :BoardGame/id,
            :board-game/name      :BoardGame/name,
            :board-game/designers :BoardGame/designers,
            :designer/name        :Designer/name,
            :designer/games       :Designer/games}
           (g.eql/attribute-to-field  (graffito/compile (graffito/load-schema! "cgg-schema.edn"))
                                      [:BoardGame/id :BoardGame/name {:BoardGame/designers [:Designer/name {:Designer/games [:BoardGame/name]}]}])))))
