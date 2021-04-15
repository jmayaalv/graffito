(ns graffito.lacinia.eql
  (:require [clojure.set :as set]
            [clojure.walk :as walk]
            [graffito.lacinia.schema :as schema]
            [com.wsscode.pathom3.connect.indexes :as pci]))

(defn- attribute? [index reachable-attributes attribute]
  (or (pci/attribute-available? index attribute)
      (contains? reachable-attributes attribute)))

(defn- reachable-attributes
  [index available-data]
  (when (seq available-data)
    (->> (pci/reachable-paths index available-data)
               (tree-seq seqable? seq)
               (filter keyword?)
               set)))


(defn- selection-vec
  "Flattens a selection tree to a valid eql vector"
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
  "Tranform a lacinia execution seletion to a vector of selected lacinia fields."
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
  "Build a equivalence map from pathom atributes to lacinia fields.
  Use the `:pathom/available-data` to determine which attributes are accesible by pathom."
  [{:pathom/keys [index available-data] :as context} fields-vec]
  (let [schema          (schema/compiled-schema context)
        reachable-attrs (reachable-attributes index available-data)]
   (->> fields-vec
        (tree-seq seqable? seq)
        (filterv (complement seqable?))
        (reduce (fn [m field]
                  (let [type-def  (schema/type-def schema (keyword (namespace field)))
                        attribute (schema/attribute type-def field)]
                    (if (attribute? index reachable-attrs attribute)
                      (assoc m attribute field)
                      m)))
                {}))))

(defn attributes-vec
  "Transform a `fields-vec` with the selection from laciina fields to pathom attributes"
  [attribute->field fields-vec]
  (let [field->attribute (reduce-kv (fn [m k v] (assoc m v k)) {} attribute->field)]
  (walk/postwalk (fn [form]
                    (if (keyword? form)
                      (get field->attribute form (keyword ">" (name form)))
                      form))
                  fields-vec)))


(defn with-lacinia-fields
  "Transform a result from pathom attributes to lacinia fields as specified on `atribute->field`"
  [attribute->field form]
  (walk/postwalk (fn [x]
                   (if (map? x)
                     (set/rename-keys x attribute->field)
                     x))
                 form))
