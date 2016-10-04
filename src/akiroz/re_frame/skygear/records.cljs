(ns akiroz.re-frame.skygear.records
  (:require [clojure.walk :refer [postwalk]]
            [cljsjs.skygear]))

(def skygear js/skygear)

(defn save [arg]
  (let [records (if (vector? arg) arg [arg])
        linearized (atom [])
        references (atom {})]
    (postwalk
      (fn [elm]
        (let [tags (meta elm)]
          (cond
            (:record tags)  (if (map? elm)
                              (if-let [stored-ref (get @references (hash elm))]
                                stored-ref
                                (let [rec-obj (->> (:_type elm)
                                                   (.extend skygear.Record)
                                                   ((fn [cls]
                                                      (->> (dissoc elm :_type)
                                                           (clj->js)
                                                           (new cls)))))
                                      ref-obj (new skygear.Reference rec-obj)]
                                  (swap! linearized conj rec-obj)
                                  (swap! references assoc (hash elm) ref-obj)
                                  ref-obj))
                              elm)
            (:file tags)    (->> (if (string? elm)
                                   {:name (str (random-uuid)) :url elm}
                                   {:name (str (random-uuid)) :file elm})
                                 (clj->js)
                                 (new skygear.Asset))
            (:geo tags)     (if (vector? elm)
                              (new skygear.Geolocation (elm 0) (elm 1))
                              elm)
            :else elm)))
      records)
    (.save skygear.publicDB (clj->js @linearized) #js{:atomic true})))
