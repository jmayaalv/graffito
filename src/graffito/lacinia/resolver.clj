(ns graffito.lacinia.resolver
  (:require
   [com.walmartlabs.lacinia.executor :as executor]
   [com.wsscode.pathom3.interface.eql :as p.eql]
   [graffito.lacinia.eql :as g.eql]
   [graffito.util :as util]))

(defn pathom [context args value]
  (let [{:keys [input attributes attribute->field]} (g.eql/eql context (executor/selections-tree context) args)]
    (->> (p.eql/process (:pathom/index context) input attributes)
         (g.eql/with-lacinia-fields attribute->field)
         util/unnamespaced)))
