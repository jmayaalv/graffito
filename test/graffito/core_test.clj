(ns graffito.core-test
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [graffito.core :as graffito]
            [graffito.resolver :as test-resolver]
            [graffito.test-utils :as t.utils]))



(use-fixtures :once (t.utils/with-env
                      (graffito/load-schema! "cgg-schema.edn")
                      (test-resolver/index)))

(deftest query
  (is (= {:data {:game_by_id {:id "1236", :name "Tiny Epic Galaxies"}}}
         (t.utils/query "{ game_by_id (id: \"1236\") { id name }}"))))
