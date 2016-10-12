(ns akiroz.re-frame.skygear.query
  (:require [cljs.spec :as s]
            [cljsjs.skygear]))

(def skygear js/skygear)

(defn query
  [{:keys [record   ;; string type
           where    ;; {:<field> {:<constrant> <value>}} (optional)
           limit    ;; int number of results (optional)
           page]}]  ;; int page offset (optional)
  (->> (.extend skygear.Record record)
       (new skygear.Query)
       ((fn apply-constrants [query]
          (when where
            (doseq [[field constrants] where]
              (doseq [[clause arg] constrants]
                (case clause
                  :=            (.equalTo               query (name field) arg)
                  :not=         (.notEqualTo            query (name field) arg)
                  :<            (.lessThan              query (name field) arg)
                  :<=           (.lessThanOrEqualTo     query (name field) arg)
                  :>            (.greaterThan           query (name field) arg)
                  :>=           (.greaterThanOrEqualTo  query (name field) arg)
                  :in           (.contains              query (name field) (clj->js arg))
                  :not-in       (.notContains           query (name field) (clj->js arg))
                  :contains     (.containsValue         query (name field) arg)
                  :not-contains (.notContainsValue      query (name field) arg)
                  :like         (.like                  query (name field) arg)
                  :not-like     (.notLike               query (name field) arg)
                  :sort-asc     (.addAscending          query (name field))
                  :sort-dec     (.addDescending         query (name field))
                  :transient    (.transientInclude      query (name field))
                  ))))
          query))
       ((fn apply-limits [query]
          (when limit (set! query.limit limit))
          (when page  (set! query.page  page))
          query))
       (.query skygear.publicDB)))



(def js-object? (partial instance? js/Object))
(def date?      (partial instance? js/Date))

(s/def ::Ord  (s/or :number   number?
                    :date     date?))
(s/def ::Eq   (s/or :nil      nil?
                    :boolean  boolean?
                    :Ord      ::Ord
                    :string   string?
                    :js-obj   js-object?))

(s/def ::=            ::Eq)
(s/def ::not=         ::Eq)
(s/def ::<            ::Ord)
(s/def ::<=           ::Ord)
(s/def ::>            ::Ord)
(s/def ::>=           ::Ord)
(s/def ::in           (s/coll-of ::Eq))
(s/def ::not-in       (s/coll-of ::Eq))
(s/def ::contains     (s/coll-of ::Eq))
(s/def ::not-contains (s/coll-of ::Eq))
(s/def ::like         string?)
(s/def ::not-like     string?)
(s/def ::sort-asc     #(= true %))
(s/def ::sort-dec     #(= true %))
(s/def ::transient    #(= true %))

(s/def ::record string?)
(s/def ::where
  (s/map-of keyword?
            (s/keys :req [(or ::= ::not= ::< ::<= ::> ::>= ::in ::not-in
                              ::contains ::not-contains ::like ::not-like
                              ::sort-asc ::sort-dec ::transient
                              )])))
(s/def ::limit nat-int?)
(s/def ::page nat-int?)

(s/def ::query
  (s/keys :req [::record]
          :opt [::where ::limit ::page]))
