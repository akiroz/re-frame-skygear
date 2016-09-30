(ns akiroz.re-frame.skygear
  (:require [re-frame.core :refer [reg-fx reg-cofx dispatch ->interceptor]]
            [akiroz.re-frame.skygear.users :as sg-users]
            [akiroz.re-frame.skygear.access :as sg-access]
            [akiroz.re-frame.skygear.records :as sg-records]
            [akiroz.re-frame.skygear.query :as sg-query]
            [akiroz.re-frame.skygear.events :as sg-events]
            [cljsjs.skygear]))

(def skygear js/skygear)

(defn- promise->dispatch [{:keys [success-event fail-event]} promise-obj]
  (let [then-fn   (if (keyword? success-event)
                    (fn [data] (dispatch [success-event data]))
                    (fn []))
        catch-fn  (if (keyword? fail-event)
                    (fn [err] (dispatch [fail-event err]))
                    (fn []))]
    (-> promise-obj
        (.then then-fn)
        (.catch catch-fn))))

(defn- sg-init [{:keys [end-point api-key]}]
  (.config skygear #js {:endPoint end-point :apiKey api-key}))

(defn- do-fx [fx-map]
  (doseq [[op args] fx-map]
    (case op
      :init         (promise->dispatch args (sg-init                args))
      :login        (promise->dispatch args (sg-users/login         args))
      :logout       (promise->dispatch args (sg-users/logout        args))
      :signup       (promise->dispatch args (sg-users/signup        args))
      :passwd       (promise->dispatch args (sg-users/passwd        args))
      :access       (promise->dispatch args (sg-access/default      args))
      :save         (promise->dispatch args (sg-records/save        args))
      :query        (promise->dispatch args (sg-query/query         args))
      ;:subscribe    (promise->dispatch args (sg-events/subscribe    args))
      ;:unsubscribe  (promise->dispatch args (sg-events/unsubscribe  args))
      ;:publish      (promise->dispatch args (sg-events/publich      args))
      (throw (str "[skygear] Unrecognized action " op)))))

(defn- cofx-map []
  {:user skygear.currentUser})

;; Public API ==================================================

(defn reg-co-fx! [{:keys [fx cofx]}]
  (when fx (reg-fx fx do-fx))
  (when cofx (reg-cofx cofx #(assoc % cofx (cofx-map)))))


(defn inject-db [db-key]
  (->interceptor
    :id (keyword (str "skygear->" db-key))
    :before #(assoc-in % [:coeffects :db db-key] (cofx-map))))
