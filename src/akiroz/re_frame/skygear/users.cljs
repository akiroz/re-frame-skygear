(ns akiroz.re-frame.skygear.users
  (:require [cljs.spec :as s]
            [cljsjs.skygear]))

(def skygear js/skygear)

(defn login [{:keys [email username password]}]
  (if username
    (.loginWithUsername skygear username password)
    (.loginWithEmail skygear email password)))

(defn logout [_]
  (.logout skygear))

(defn signup [{:keys [email username password]}]
  (if username
    (.signupWithUsername skygear username password)
    (.signupWithEmail skygear email password)))

(defn change-password [{:keys [old-password new-password]}]
  (.changePassword skygear old-password new-password))

(defn whoami [_]
  (.whoami skygear))


(defn anything? [_] true)

(s/def ::email string?)
(s/def ::username string?)
(s/def ::password string?)

(s/def ::old-password ::password)
(s/def ::new-password ::password)

(s/def ::login (s/keys :req-un [(or ::email ::username) ::password]))
(s/def ::signup (s/keys :req-un [(or ::email ::username) ::password]))
(s/def ::change-password (s/keys :req-un [::old-password ::new-password]))
(s/def ::logout anything?)
(s/def ::whoami anything?)
