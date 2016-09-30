(ns akiroz.re-frame.skygear.access
  (:require [cljsjs.skygear]))

(def skygear js/skygear)

(defn default [permission]
  (->> (new skygear.ACL)
       ((fn [acl]
          (case permission
            :none (.setPublicNoAccess acl)
            :ro   (.setPublicReadOnly acl)
            :rw   (.setPublicReadWriteAccess acl)
            (throw (str "[skygear] Unrecognized permission " permission)))
          acl))
       (.setDefaultACL skygear)))
