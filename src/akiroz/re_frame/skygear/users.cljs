(ns akiroz.re-frame.skygear.users
  (:require [cljs.spec :as s]
            [cljsjs.skygear]))

(def skygear js/skygear)

(defn login [{:keys [username password]}]
  (.loginWithUsername skygear username password))

(defn logout [_]
  (.logout skygear))

(defn signup [{:keys [username password]}]
  (.signupWithUsername skygear username password))

(defn change-password [{:keys [old-password new-password]}]
  (.changePassword skygear old-password new-password))

(defn whoami [_]
  (.whoami skygear))


(defn anything? [_] true)

(s/def ::username string?)
(s/def ::password string?)

(s/def ::old-password ::password)
(s/def ::new-password ::password)

(s/def ::login (s/keys :req-un [::username ::password]))
(s/def ::signup (s/keys :req-un [::username ::password]))
(s/def ::change-password (s/keys :req-un [::old-password ::new-password]))
(s/def ::logout anything?)
(s/def ::whoami anything?)
