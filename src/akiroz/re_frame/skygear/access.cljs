(ns akiroz.re-frame.skygear.access
  (:require [cljs.spec :as s]
            [cljsjs.skygear]))

(def skygear js/skygear)

(defn access [{:keys [permission]}]
  (->> (new skygear.ACL)
       ((fn [acl]
          (case permission
            :none (.setPublicNoAccess acl)
            :ro   (.setPublicReadOnly acl)
            :rw   (.setPublicReadWriteAccess acl)
            )
          acl))
       (.setDefaultACL skygear)))

(s/def ::permission #{:none :ro :rw})
(s/def ::access (s/keys :req [::permission]))
