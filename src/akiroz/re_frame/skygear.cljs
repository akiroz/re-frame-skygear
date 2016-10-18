(ns akiroz.re-frame.skygear
  (:require [cljs.spec :as s]
            [re-frame.core :refer [reg-fx reg-cofx dispatch ->interceptor]]
            [akiroz.re-frame.skygear.users :as sg-users]
            [akiroz.re-frame.skygear.access :as sg-access]
            [akiroz.re-frame.skygear.records :as sg-records]
            [akiroz.re-frame.skygear.query :as sg-query]
            [akiroz.re-frame.skygear.events :as sg-events]
            [cljsjs.skygear]))

(def skygear js/skygear)


(defn- sg-init [{:keys [end-point api-key]}]
  (.config skygear #js {:endPoint end-point :apiKey api-key}))


(defn- promises->dispatch [{:keys [success-event fail-event]} promises]
  (-> (if (= (count promises) 1)
        (first promises)
        (.all js/Promise (clj->js promises)))
      (.then  (if success-event
                (fn [data] (dispatch [success-event data]))
                (fn [])))
      (.catch (if fail-event
                (fn [err] (dispatch [fail-event err]))
                (fn [])))))

(defn- do-fx [fx-map]
  (when-not (s/valid? ::fx-map fx-map)
    (throw (with-out-str (s/explain ::fx-map fx-map))))
  (->> (for [[op args] fx-map]
         (case op
           :init         (sg-init                args)
           :login        (sg-users/login         args)
           :logout       (sg-users/logout        args)
           :signup       (sg-users/signup        args)
           :passwd       (sg-users/passwd        args)
           :access       (sg-access/access       args)
           :save         (sg-records/save        args)
           :query        (sg-query/query         args)
           ;:subscribe    (sg-events/subscribe    args)
           ;:unsubscribe  (sg-events/unsubscribe  args)
           ;:publish      (sg-events/publich      args)
           #_else        nil))
       (filter (partial instance? js/Promise))
       (promises->dispatch fx-map)))

(defn- cofx-map []
  {:user skygear.currentUser})



(s/def ::end-point string?)
(s/def ::api-key string?)
(s/def ::init (s/keys :req-un [::end-point ::api-key]))

(s/def ::success-event keyword?)
(s/def ::fail-event keyword?)

(s/def ::fx-map
  (s/keys :req-un [(or ::init
                       ::sg-users/login
                       ::sg-users/logout
                       ::sg-users/signup
                       ::sg-users/passwd
                       ::sg-access/access
                       ::sg-records/save
                       ::sg-query/query
                       ;::sg-events/subscribe
                       ;::sg-events/unsubscribe
                       ;::sg-events/publish
                       )]
          :opt-un [::success-event ::fail-event]))


;; Public API ==================================================

(defn reg-co-fx! [{:keys [fx cofx]}]
  (when fx (reg-fx fx do-fx))
  (when cofx (reg-cofx cofx #(assoc % cofx (cofx-map)))))


(defn inject-db [db-key]
  (->interceptor
    :id (keyword (str "skygear->" db-key))
    :before #(assoc-in % [:coeffects :db db-key] (cofx-map))))
