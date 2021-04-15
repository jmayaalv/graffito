(ns graffito.lacinia.mutation
  (:require [com.wsscode.pathom3.interface.eql :as p.eql]
            [graffito.lacinia.eql :as g.eql]
            [graffito.util :as util]
            [com.walmartlabs.lacinia.executor :as executor]
            [graffito.lacinia.schema :as schema]))

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
  (fn [context args value]
    (let [parameters                                 (parameters context args)
          op                                         (seq [mutation parameters]) ]
      (p.eql/process (:pathom/index context) [op]))))

(comment

  )
