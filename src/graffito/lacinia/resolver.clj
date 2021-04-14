(ns graffito.lacinia.resolver
  (:require [graffito.lacinia.eql :as g.eql]
            [graffito.lacinia.schema :as schema]
            [com.wsscode.pathom3.interface.eql :as p.eql]
            [com.walmartlabs.lacinia.executor :as executor]
            [com.walmartlabs.lacinia.resolve :as resolve]
            [graffito.util :as util]))

(defn- input-and-params
  [context args]
  (let [selection-type (-> context executor/selection g.eql/selection-type)
        type-def       (-> context schema/compiled-schema (schema/type-def selection-type))]
    (reduce-kv (fn [m arg value]
                 (if-let [attribute (schema/attribute type-def arg)]
                   (assoc-in m [:input attribute] value)
                   (assoc-in m [:parameters arg] value)))
               {}
               args)))
(defn pathom [context args value]
  (let [{:keys [input params]} (input-and-params context args)
        fields                 (->> context executor/selections-tree g.eql/selection-fields)
        attribute->field       (g.eql/attribute-to-field (schema/compiled-schema context) fields)]
    (->> (p.eql/process (:pathom/index context) input (g.eql/attributes-vec attribute->field fields))
         (g.eql/with-lacinia-fields attribute->field)
         util/unnamespaced)))
