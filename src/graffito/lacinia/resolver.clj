(ns graffito.lacinia.resolver
  (:require [graffito.lacinia.eql :as g.eql]
            [graffito.lacinia.schema :as schema]
            [com.wsscode.pathom3.interface.eql :as p.eql]
            [com.walmartlabs.lacinia.executor :as executor]
            [graffito.util :as util]))

(defn- with-pathom-attributes
  "Adds a set with the list of all known pathom attributes if not yet set"
  [{:keys [pathom/index] :as context}]
  (if (seq (:pathom/attributes index))
    context
    (assoc context :pathom/attributes (g.eql/pathom-attributes index))))

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
        context'               (with-pathom-attributes context)
        fields                 (->> context' executor/selections-tree g.eql/selection-fields)
        attribute->field       (g.eql/attribute-to-field context' fields)]
    (->> (p.eql/process (:pathom/index context) input (g.eql/attributes-vec attribute->field fields))
         (g.eql/with-lacinia-fields attribute->field)
         util/unnamespaced)))
