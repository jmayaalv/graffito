(ns graffito.test-utils
  (:require
   [clojure.walk :as walk]
   [com.walmartlabs.lacinia :as lacinia]
   [graffito.core :as graffito])
  (:import
   (clojure.lang IPersistentMap)))

(def ^:dynamic *schema*)

(def ^:dynamic *pathom*)

(def db+ (atom {}))


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
(defn with-env [schema index initial-data & options]
  (fn [f]
    (binding [*schema* (graffito/compile schema options)
              *pathom* index]
      (reset! db+ initial-data)
      (f)
      (reset! db+ {}))))

(defn query [query-string]
  (-> (lacinia/execute *schema* query-string nil {:pathom/index (assoc *pathom* :db db+)})
      simplify))
