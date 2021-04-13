(ns graffito.test-utils
  (:require  [graffito.core :as graffito]
             [com.walmartlabs.lacinia :as lacinia]
             [clojure.walk :as walk])
  (:import (clojure.lang IPersistentMap)))

(def ^:dynamic *schema*)

(def ^:dynamic *pathom*)


(defn simplify
  "Converts all ordered maps nested within the map into standard hash maps, and
   sequences into vectors, which makes for easier constants in the tests, and eliminates ordering problems."
  [m]
  (walk/postwalk
    (fn [node]
      (cond
        (instance? IPersistentMap node)
        (into {} node)

        (seq? node)
        (vec node)

        :else
        node))
    m))

;;fixtures
(defn with-env [schema index & options]
  (fn [f]
    (binding [*schema* (graffito/compile schema options)
              *pathom* index]
      (f))))

(defn query [query-string]
  (-> (lacinia/execute *schema* query-string nil {:pathom/index  *pathom*})
      simplify))
