(ns graffito.lacinia.eql
  (:require [clojure.walk :as walk]
            [graffito.util :as util]
            [camel-snake-kebab.core :as csk]
            [com.walmartlabs.lacinia.schema :as lacinia.schema]
            [com.walmartlabs.lacinia :as lacinia]
            [com.walmartlabs.lacinia.constants :as constants]
            [graffito.lacinia.schema :as schema]
            [com.walmartlabs.lacinia.executor :as executor]))



(defn  remove-nils  [q]
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
  (let [field-type (-> context executor/selection selection-type)
        field-def  (schema/type-def (schema/compiled-schema context) field-type)]
   (->> q
        (walk/postwalk
         (fn [{:keys [selections] :as x}]
           (cond
             (qualified-keyword? x)
             (schema/attribute field-def x)
             :else
             (if selections
               selections
               x))))
        remove-nils)))
