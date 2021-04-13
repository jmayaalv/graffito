(ns graffito.util
  (:require [clojure.walk :as walk]))

(defn unnamespaced
  [form]
  (walk/postwalk (fn [k]
                   (if (qualified-keyword? k)
                     (keyword (name k))
                     k))
                 form))
