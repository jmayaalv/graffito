(ns graffito.board-game-geek.resolver
  (:require [com.wsscode.pathom3.connect.operation :as pco]
            [com.wsscode.pathom3.connect.indexes :as pci]
            [com.wsscode.misc.coll :as coll]
            [com.wsscode.pathom3.interface.eql :as p.eql]
            [com.wsscode.pathom3.connect.planner :as pcp]))

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
           #{{:member/id "37" :board-game/id "1234" :rating/value 3}
             {:member/id "1410" :board-game/id "1234" :rating/value 5}
             {:member/id "1410" :board-game/id "1236" :rating/value 4}
             {:member/id "1410" :board-game/id "1237" :rating/value 4}
             {:member/id "2812" :board-game/id "1237" :rating/value 4}
             {:member/id "37" :board-game/id "1237" :rating/value 5}}

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

(pco/defresolver game-by-id [{:keys [db]} {:keys [:board-game/id]}]
  {::pco/input [:board-game/id]
   ::pco/output [:board-game/name :board-game/summary :board-game/min-players :board-game/max-players {:board-game/designers [:designer/id]}]}
  (some #(when (= id (:board-game/id %))
           (update % :board-game/designers (fn [designers]
                                             (mapv (partial hash-map :designer/id) designers))))
        (get @db :games)))

(pco/defresolver game-by-name [{:keys [db]} input]
  {::pco/input [:board-game/name]
   ::pco/output [:board-game/id :board-game/name :board-game/summary :board-game/min-players :board-game/max-players]}
  (some #(when (= (:board-game/name input) (:board-game/name %)) %)
        (get @db :games)))

(pco/defresolver designer-by-id [{:keys [db]} input]
  {::pco/input  [:designer/id]
   ::pco/output [:designer/id :designer/name :designer/url]
   ::pco/batch? true}
  (let [ids (set (map :designer/id input))]
    (->> (get @db :designers)
         (filter #(contains? ids (:designer/id %)))
         (coll/restore-order input :designer/id))))

(pco/defresolver designer-games [{:keys [db]} {:keys [designer/id]}]
  {::pco/input [:designer/id]
   ::pco/output [{:designer/games [:board-game/id]}]}
  {:designer/games (filter #(contains? (:board-game/designers %) id)
                           (get @db :games))})

(pco/defresolver game-rating-summary [{:keys [db]} {:keys [:board-game/id]}]
  {::pco/input  [:board-game/id]
   ::pco/output [{:board-game/rating-summary [:game-rating-summary/average :game-rating-summary/count]}]}
  (let [ratings (->> (get @db :ratings)
                     (filter #(= id (:board-game/id %)))
                     (map :rating/value))
        n       (count ratings)]
    {:board-game/rating-summary {:game-rating-summary/count   n
                                 :game-rating-summary/average (if (zero? n)
                                                                0
                                                                (/ (apply + ratings) n))}}))

(pco/defresolver member-by-id [{:keys [db]} {:keys [:member/id]}]
  {::pco/input  [:member/id]
   ::pco/output [:member/id :member/name]}
  (some  #(when (= (:member/id %) id) %)
         (get @db :members)))

(pco/defresolver member-ratings [{:keys [db]} {:keys [:member/id]}]
  {::pco/input [:member/id]
   ::pco/output [{:member/ratings [:board-game/id :rating/value]}]}
  {:member/ratings (filter #(= id (:member/id %))
                           (get @db :ratings))})


(defn- upsert-game-rating! [db game-rate]
  #p (swap! db update :ratings conj game-rate))

(pco/defmutation rate! [{:keys [db]} { gameid :board-game/id memberid :member/id value :rating}]
  (upsert-game-rating! db {:member/id memberid :board-game/id gameid :rating/value value}))

(defn index []
  (pci/register [game-by-id game-by-name designer-by-id designer-games member-by-id game-rating-summary member-ratings rate!]))

(comment
  (tap> (index))
  (p.eql/process (index)
                 {:member/id "1410"}
                 [:member/id :member/name {:member/ratings [:rating/value {:>/game [:board-game/name]}]}])


)
