(ns graffito.lacinia.mutation
  (:require
   [com.walmartlabs.lacinia.executor :as executor]
   [com.wsscode.pathom3.interface.eql :as p.eql]
   [graffito.lacinia.eql :as g.eql]
   [graffito.lacinia.schema :as schema]
   [graffito.util :as util]))

(defn parameters
  [context args]
  (let [mutation-def (-> context executor/selection)]
    (reduce-kv (fn [m arg value]
                 (assoc m (schema/argument (schema/argument-def  mutation-def arg)) value))
               {}
               args)))

(defn pathom
  "Resolver factory for pathom mutations"
  [mutation]
  (fn [context args _value]
    (let [parameters                                  (parameters context args)
          {:keys [_input attributes attribute->field]} (g.eql/eql context (executor/selections-tree context) args)
          op                                          (if attributes
                                                        {(seq [mutation parameters]) attributes}
                                                        (seq [mutation parameters]))]
      (->> (p.eql/process (:pathom/index context) [op])
           (g.eql/with-lacinia-fields attribute->field)
           util/unnamespaced
           vals
           first))))
