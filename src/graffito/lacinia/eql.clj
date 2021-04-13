(ns graffito.lacinia.eql
  (:require [clojure.walk :as walk]
            [graffito.util :as util]
            [com.walmartlabs.lacinia.schema :as lacinia.schema]
            [com.walmartlabs.lacinia :as lacinia]
            [com.walmartlabs.lacinia.constants :as constants]
            [graffito.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.executor :as executor]))



(defn  remove-nils  [q] ;;TODO make this fn cleaner
  (->> (if (sequential? q) q [q])
       (mapcat
        #(reduce-kv
          (fn [m k v]
            (if-not (if (sequential? v)
                      (->> v (filter (complement nil?)) seq)
                      v)
              (conj m k)
              (conj m {k (remove-nils v)})))
          []
          %))
       vec))

(defn selection-type
  [selection]
  (get-in selection [:field-definition :type :type]))

(defn from-selection-tree
  [context q]
  (->> q
       (walk/postwalk
        (fn [{:keys [selections] :as x}]
          (cond
            (qualified-keyword? x)
            (let [field-type (keyword (namespace x))
                  field-def  (schema/type-def (schema/compiled-schema context) field-type)]
              (schema/attribute field-def x))
            :else
            (if selections
              selections
              x))))
       remove-nils))
