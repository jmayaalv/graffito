(ns graffito.core
  (:refer-clojure :exclude [compile])
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.walmartlabs.lacinia.util :as util]
   [graffito.lacinia.resolver :as resolver]
   [graffito.lacinia.mutation :as mutation]))


(defn with-maybe-pathom-resolver
  "Adds the default pathom resolver to all declared queries if resolver not declareed."
  [schema]
  (update schema :queries (partial reduce-kv (fn [m query-id config]
                                               (assoc m query-id (merge {:resolve :pathom/resolver} config)))
                                   {})))

(defn compile
  "Compile a lacinia schema setting the default pathom resolvers. Return the compiled lacinia schema.
  Options:
   - `resolvers`:  a map lacinia resolvers"
  [schema & {:keys [resolvers]}]
  (-> schema
      with-maybe-pathom-resolver
      (util/attach-resolvers (merge {:pathom/resolver resolver/pathom
                                     :pathom/mutation mutation/pathom}
                                    resolvers))
      schema/compile))

(defn load-schema!
  "Load  a lacinia schema from a edn file."
  [schema]
  (-> (io/resource schema)
      slurp
      edn/read-string))
