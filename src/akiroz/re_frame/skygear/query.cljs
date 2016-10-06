(ns akiroz.re-frame.skygear.query
  (:require [cljsjs.skygear]))

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
                  (throw (str "[skygear] Unrecognized query condition " clause))
                  ))))
          query))
       ((fn apply-limits [query]
          (when limit (set! query.limit limit))
          (when page  (set! query.page  page))
          query))
       (.query skygear.publicDB)))
