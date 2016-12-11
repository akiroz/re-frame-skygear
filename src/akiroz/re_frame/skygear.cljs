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


(defn- sg-config [{:keys [end-point api-key]}]
  (.config skygear #js {:endPoint end-point :apiKey api-key}))

(defn- sg-lambda [{:keys [action args keywordize-keys]}]
  (-> (.lambda skygear action (clj->js args))
      (.then #(js->clj % :keywordize-keys (or keywordize-keys false)))))

(defn- promises->dispatch [fx-vec promises]
  (let [success-events  (->> (partition 2 fx-vec)
                             (filter #(= :success-dispatch (first %)))
                             (map (fn [[_ e]] e)))
        fail-events     (->> (partition 2 fx-vec)
                             (filter #(= :fail-dispatch (first %)))
                             (map (fn [[_ e]] e)))]
    (-> (case (count promises)
          0       (.resolve js/Promise)
          1       (first promises)
          #_else  (.all js/Promise (clj->js promises)))
        (.then (fn [data]
                 (doseq [event-vec success-events]
                   (dispatch (conj event-vec data)))))
        (.catch (fn [err]
                 (doseq [event-vec fail-events]
                   (dispatch (conj event-vec err))))))))

(defn- do-fx [fx-vec]
  (when-not (s/valid? ::fx-vec fx-vec)
    (throw (with-out-str (s/explain ::fx-vec fx-vec))))
  (->> (for [[op args] (partition 2 fx-vec)]
         (case op
           :config          (sg-config                args)
           :lambda          (sg-lambda                args)
           :login           (sg-users/login           args)
           :logout          (sg-users/logout          args)
           :signup          (sg-users/signup          args)
           :change-password (sg-users/change-password args)
           :whoami          (sg-users/whoami          args)
           :access          (sg-access/access         args)
           :save            (sg-records/save          args)
           :query           (sg-query/query           args)
           ;:subscribe   (sg-events/subscribe    args)
           ;:unsubscribe (sg-events/unsubscribe  args)
           ;:publish     (sg-events/publish      args)
           #_else        nil))
       (filter (partial instance? js/Promise))
       (promises->dispatch fx-vec)))

(defn- cofx-map []
  {:user skygear.currentUser})



(s/def ::end-point string?)
(s/def ::api-key string?)
(s/def ::config (s/keys :req-un [::end-point ::api-key]))

(s/def ::action string?)
(s/def ::args vector?)
(s/def ::keywordize-keys boolean?)
(s/def ::lambda (s/keys :req-un [::action ::args]))

(s/def ::fx-vec
  (s/* (s/alt :config           (s/cat :action (partial = :config)            :args ::config)
              :lambda           (s/cat :action (partial = :lambda)            :args ::lambda)
              :login            (s/cat :action (partial = :login)             :args ::sg-users/login)
              :logout           (s/cat :action (partial = :logout)            :args ::sg-users/logout)
              :signup           (s/cat :action (partial = :signup)            :args ::sg-users/signup)
              :change-password  (s/cat :action (partial = :change-password)   :args ::sg-users/change-password)
              :whoami           (s/cat :action (partial = :whoami)            :args ::sg-users/whoami)
              :access           (s/cat :action (partial = :access)            :args ::sg-access/access)
              :save             (s/cat :action (partial = :save)              :args ::sg-records/save)
              :query            (s/cat :action (partial = :query)             :args ::sg-query/query)
              :subscribe        (s/cat :action (partial = :subscribe)         :args ::sg-events/subscribe)
              :unsubscribe      (s/cat :action (partial = :unsubscribe)       :args ::sg-events/unsubscribe)
              :publish          (s/cat :action (partial = :publish)           :args ::sg-events/publish)
              :success-dispatch (s/cat :action (partial = :success-dispatch)  :args vector?)
              :fail-dispatch    (s/cat :action (partial = :fail-dispatch)     :args vector?)
              )))


;; Public API ==================================================

(defn reg-co-fx! [{:keys [fx cofx]}]
  (when fx (reg-fx fx do-fx))
  (when cofx (reg-cofx cofx #(assoc % cofx (cofx-map)))))


(defn inject-db [db-key]
  (->interceptor
    :id (keyword (str "skygear->" db-key))
    :before #(assoc-in % [:coeffects :db db-key] (cofx-map))))
