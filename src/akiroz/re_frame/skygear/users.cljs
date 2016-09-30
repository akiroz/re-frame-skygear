(ns akiroz.re-frame.skygear.users
  (:require [cljsjs.skygear]))

(def skygear js/skygear)

(defn login [{:keys [username password]}]
  (.loginWithUsername skygear username password))

(defn logout [_]
  (.logout skygear))

(defn signup [{:keys [username password]}]
  (.signupWithUsername skygear username password))

(defn passwd [{:keys [old-pass new-pass]}]
  (.changePassword skygear old-pass new-pass))
