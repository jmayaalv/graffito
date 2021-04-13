(ns graffito.resolver
  (:require [com.wsscode.pathom3.connect.operation :as pco]
            [com.wsscode.pathom3.connect.indexes :as pci]))

(def data {:games
           [#:board-game{:id          "1234"
                         :name        "Zertz"
                         :summary     "Two player abstract with forced moves and shrinking board"
                         :min-players 2
                         :max-players 2
                         :designers   #{"200"}}
            #:board-game{:id          "1235"
                         :name        "Dominion"
                         :summary     "Created the deck-building genre; zillions of expansions"
                         :designers   #{"204"}
                         :min-players 2}
            #:board-game{:id          "1236"
                         :name        "Tiny Epic Galaxies"
                         :summary     "Fast dice-based sci-fi space game with a bit of chaos"
                         :designers   #{"203"}
                         :min-players 1
                         :max-players 4}
            #:board-game{:id          "1237"
                         :name        "7 Wonders: Duel"
                         :summary     "Tense, quick card game of developing civilizations"
                         :designers   #{"201" "202"}
                         :min-players 2
                         :max-players 2}]
           :members
           [#:member{:id   "37"
                     :name "curiousattemptbunny"}
            #:member{:id   "1410"
                     :name "bleedingedge"}
            #:member{:id   "2812"
                     :name "missyo"}]

           :ratings
           [{:member/id "37" :board-game/id "1234" :rating/value 3}
            {:member/id "1410" :board-game/id "1234" :rating/value 5}
            {:member/id "1410" :board-game/id "1236" :rating/value 4}
            {:member/id "1410" :board-game/id "1237" :rating/value 4}
            {:member/id "2812" :board-game/id "1237" :rating/value 4}
            {:member/id "37" :board-game/id "1237" :rating/value 5}]

           :designers
           [#:designer{:id   "200"
                       :name "Kris Burm"
                       :url  "http://www.gipf.com/project_gipf/burm/burm.html"}
            #:designer{:id   "201"
                       :name "Antoine Bauza"
                       :url  "http://www.antoinebauza.fr/"}
            #:designer{:id   "202"
                       :name "Bruno Cathala"
                       :url  "http://www.brunocathala.com/"}
            #:designer{:id   "203"
                       :name "Scott Almes"}
            #:designer{:id   "204"
                       :name "Donald X. Vaccarino"}]})

(pco/defresolver game-by-id [{:keys [:board-game/id]}]
  {::pco/output [:board-game/id :board-game/name :board-game/summary :board-game/min-players :board-game/max-players]}
  (some #(when (= id (:board-game/id %)) %)
        (get data :games)))

(pco/defresolver game-by-name [input]
  {::pco/input [:board-game/name]
   ::pco/output [:board-game/id :board-game/name :board-game/summary :board-game/min-players :board-game/max-players]}
  (some #(when (= (:borad-game/name input) (:board-game/name %)) %)
        (get data :games)))

(defn index []
  (pci/register [game-by-id game-by-name]))

(index)
