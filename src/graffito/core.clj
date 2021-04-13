(ns graffito.core
  (:refer-clojure :exclude [compile])
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [com.walmartlabs.lacinia.schema :as schema]
   [com.walmartlabs.lacinia.util :as util]
   [graffito.lacinia.resolver :as lacinia.resolver]))


(defn compile
  "Compile a lacinia schema setting the default pathom resolvers. Return the compiled lacinia schema.
  Options:
   - `resolvers`:  a map lacinia resolvers"
  [schema & {:keys [resolvers]}]
  (-> schema
      (util/attach-resolvers (merge resolvers {:pathom/resolver  lacinia.resolver/pathom}))
      schema/compile))

(defn load-schema!
  "Load  a lacinia schema from a edn file."
  [schema]
  (-> (io/resource schema)
      slurp
      edn/read-string))
