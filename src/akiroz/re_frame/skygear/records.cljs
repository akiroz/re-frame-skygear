(ns akiroz.re-frame.skygear.records
  (:require [cljs.spec :as s]
            [clojure.walk :refer [postwalk]]
            [oops.core :refer [oget]]
            [cljsjs.skygear]))

(def skygear js/skygear)

(defmulti parse-node
  (fn [elm _ _]
    [(let [tags (meta elm)]
       (cond (:rec tags)    :rec
             (:ref tags)    :ref
             (:asset tags)  :asset
             (:geo tags)    :geo
             :else          nil))
     (map? elm)]))

(defmethod parse-node [:rec true]
  [{:keys [_type _id] :as elm} recs refs]
  (or (get @refs elm)
      (let [attrs   (merge {:id (if _id (str _type "/" _id) nil)}
                           (dissoc elm :_type :_id))
            rec-obj (skygear.Record. _type (clj->js attrs))]
        (doto (skygear.Reference. rec-obj)
          (#(swap! recs conj rec-obj))
          (#(swap! refs assoc (oget % "_id") %))))))

(defmethod parse-node [:rec false]
  [[rec-obj] recs refs]
  (let [_id (oget rec-obj "_id")]
    (or (get @refs _id)
        (doto (skygear.Reference. rec-obj)
          (#(swap! recs conj rec-obj))
          (#(swap! refs assoc _id %))))))

(defmethod parse-node [:ref true]
  [{:keys [_id]} _ refs]
  (doto (skygear.Reference. _id)
    (#(swap! refs assoc _id %))))

(defmethod parse-node [:ref false]
  [[rec-obj] _ refs]
  (doto (skygear.Reference. rec-obj)
    (#(swap! refs assoc (oget % "_id") %))))

(defmethod parse-node [:asset true]
  [{:keys [file] filename :name} _ _]
  (skygear.Asset.
    #js{:file file
        :name (or filename (str (random-uuid)))}))

(defmethod parse-node [:geo false]
  [[a b] _ _]
  (if-not (instance? skygear.Geolocation a)
    (skygear.Geolocation. a b)
    a))

(defmethod parse-node :default
  [elm _ _]
  elm)

(defn save [records]
  (let [linearized (atom [])
        references (atom {})]
    (postwalk
      (fn [elm]
        (parse-node elm linearized references))
      records)
    (.save skygear.publicDB (clj->js @linearized))))


(s/def ::primitive (s/or :nil      nil?
                         :boolean  boolean?
                         :number   number?
                         :string   string?
                         :object   (partial instance? js/Object)))

(s/def ::geo (s/and #(:geo (meta %))
                    (s/or :tuple  (s/tuple number? number?)
                          :object (partial instance? skygear.Geolocation))))

(s/def ::name string?)
(s/def ::file (partial instance? js/Blob))
(s/def ::asset (s/and #(:asset (meta %))
                      (s/keys :req-un [::file]
                              :opt-un [::name])))

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
