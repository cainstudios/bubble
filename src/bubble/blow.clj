(ns bubble.blow
  (:refer-clojure :exclude [pop])
  (:require [bubble.mangle :refer [mangle]]))

(def ^:private names
  (partial map (comp str second first)))

(defn- eval-nss!
  [nss]
  (doseq [ns nss]
    (eval (first ns))
    (eval ns)))

(defn- all-publics
  [nss]
  (mapcat #(map second (ns-publics (second (first %))))
          nss))

(defn- demangle-vars
  [postfix vars]
  (map #(symbol (.replace (subs (str %) 2) (str \. postfix) ""))
       vars))

(defn- remove-old-nss!
  [nss]
  (doseq [ns nss]
    (remove-ns (symbol ns))))

(defn blow
  "Blow a bubble. the ones which you blew before should be gone
  before you even notice."
  [bubble nss]
  (let [prev-ns *ns*
        postfix (gensym "bubble-")
        mangled (mangle nss :postfix postfix)
        new-nss (names mangled)]
    (try
      (eval-nss! mangled)
      (let [vars (all-publics mangled)
            var-map (zipmap (demangle-vars postfix vars) vars)
            old-nss (:nss @bubble)]
        (swap! bubble assoc :vars var-map :nss new-nss)
        (remove-old-nss! old-nss))
      (finally
        (in-ns (ns-name prev-ns))))))
