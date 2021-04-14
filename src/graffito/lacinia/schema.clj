(ns graffito.lacinia.schema
  (:require [com.walmartlabs.lacinia.schema :as schema]
            [graffito.util :as util]
            [camel-snake-kebab.core :as csk]
            [com.walmartlabs.lacinia.constants :as constants]))


(defn compiled-schema
  [context]
  (get context ::constants/schema))

(defn type-def
  [schema type]
  (schema/select-type schema type))

(defn attribute
  [type-def field]
  (let [un-field  (util/unnamespaced field)
        type-ns   (or (some-> type-def :pathom/namespace name)
                      (-> type-def :type-name name csk/->kebab-case))
        field-def (some-> type-def :fields (get un-field))]
    (when field-def
      (or (get  field-def :pathom/attribute)
          (some-> field-def :pathom/namespace name)
          (keyword type-ns (csk/->kebab-case (name un-field)))))))

(defn attributes
  "Build a map of lacinia fields to pathom attributes using the compiled schema."
  [context]
  (schema/select-type)

  )
