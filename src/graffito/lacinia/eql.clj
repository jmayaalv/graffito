(ns graffito.lacinia.eql
  (:require [clojure.set :as set]
            [clojure.walk :as walk]
            [graffito.lacinia.schema :as schema]))

(defn- selection-vec
  [tree]
  (reduce-kv (fn [r k v]
               (if (seq (keep identity v))
                 (conj r {k (selection-vec (first v))})
                 (conj r k)))
             []
             tree))

(defn selection-type
  [selection]
  (get-in selection [:field-definition :type :type]))


(defn selection-fields
  [selection]
  (->> (walk/postwalk (fn [{:keys [selections] :as form}]
                        (cond
                          (qualified-keyword? form) form
                          :else
                          (if (seq selections)
                            selections
                            form)))
                      selection)
       selection-vec))

(defn attribute-to-field
  [schema fields-vec]
  (loop [attribute->field {}
         fields           fields-vec]
    (if (seq fields)
      (let [field    (first fields)
            join     (map? field)
            field'   (if join (key (first field)) field)
            type-def (schema/type-def schema (keyword (namespace field')))]
        (recur (assoc attribute->field (schema/attribute type-def field') field')
               (if join (val (first field)) (rest fields))))
      attribute->field)))

(defn attributes-vec
  [attribute->field fields-vec]
  (let [field->attribute (reduce-kv (fn [m k v] (assoc m v k)) {} attribute->field)]
  (walk/postwalk (fn [form]
                    (if (keyword? form)
                      (get field->attribute form form)
                      form))
                  fields-vec)))


(defn with-lacinia-fields
  [attribute->field form]
  (walk/postwalk (fn [x]
                   (if (map? x)
                     (set/rename-keys x attribute->field)
                     x))
                 form))
