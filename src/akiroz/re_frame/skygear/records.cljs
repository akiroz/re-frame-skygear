(ns akiroz.re-frame.skygear.records
  (:require [cljs.spec :as s]
            [clojure.walk :refer [postwalk]]
            [oops.core :refer [oget]]
            [cljsjs.skygear]))

(def skygear js/skygear)

(defn save [records]
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
                                                         ((fn [data]
                                                            (if (:_id data)
                                                              (update
                                                                data :_id
                                                                #(str (:_type elm) "/" %))
                                                              data)))
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
            (:asset tags) (->> (assoc elm :name (str (random-uuid)))
                               (clj->js)
                               (new skygear.Asset))
            (:geo tags)   (if (= (count elm) 2)
                            (new skygear.Geolocation (elm 0) (elm 1))
                            (elm 0))
            :else elm)))
      records)
    (.save skygear.publicDB (clj->js @linearized) #js{:atomic true})))




(s/def ::primitive (s/or :nil      nil?
                         :boolean  boolean?
                         :number   number?
                         :string   string?
                         :object   (partial instance? js/Object)))

(s/def ::geo (s/and #(:geo (meta %))
                    (s/or :tuple  (s/tuple number? number?)
                          :object (partial instance? skygear.Geolocation))))

(s/def ::url string?)
(s/def ::file (partial instance? js/File))
(s/def ::asset (s/and #(:asset (meta %))
                      (s/keys :req-un [(or ::url ::file)])))

(s/def ::ref (s/and #(:ref (meta %))
                    (partial instance? skygear.Reference)))

(s/def ::_id string?)
(s/def ::_type string?)
(s/def ::rec (s/and #(:rec (meta %))
                    (s/or :object (partial instance? skygear.Record)
                          :map    (s/and (s/keys :req-un [::_type]
                                                 :opt-un [::_id])
                                         (s/map-of keyword?
                                                   (s/or :record      ::rec
                                                         :reference   ::ref
                                                         :asset       ::asset
                                                         :geolocation ::geo
                                                         :primitive   ::primitive))))))

(s/def ::save (s/coll-of ::rec :kind vector?))
