(ns akiroz.re-frame.skygear.records
  (:require [clojure.walk :refer [postwalk]]
            [oops.core :refer [oget]]
            [cljsjs.skygear]))

(def skygear js/skygear)

(defn save [{:keys [records]}]
  (let [linearized (atom [])
        references (atom {})]
    (postwalk
      (fn [elm]
        (let [tags (meta elm)]
          (cond
            (:rec tags)   (if (map? elm)
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
                            (if-let [stored-ref (get @references (oget (elm 0) "id"))]
                              stored-ref
                              (let [rec-obj (elm 0)
                                    ref-obj (new skygear.Reference rec-obj)]
                                (swap! linearized conj rec-obj)
                                (swap! references assoc (oget rec-obj "id") ref-obj)
                                ref-obj)))
            (:ref tags)   (let [ref-obj (new skygear.Reference (elm 0))]
                            (swap! references assoc (oget elm "id") ref-obj)
                            ref-obj)
            (:file tags)  (->> (assoc elm :name (str (random-uuid)))
                               (clj->js)
                               (new skygear.Asset))
            (:geo tags)   (if (= (count elm) 2)
                            (new skygear.Geolocation (elm 0) (elm 1))
                            (elm 0))
            :else elm)))
      records)
    (.save skygear.publicDB (clj->js @linearized) #js{:atomic true})))
