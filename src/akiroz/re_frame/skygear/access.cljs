(ns akiroz.re-frame.skygear.access
  (:require [cljs.spec :as s]
            [cljsjs.skygear]))

(def skygear js/skygear)

(defn access [{:keys [public]}]
  (->> (new skygear.ACL)
       ((fn [acl]
          (case public
            :none (.setPublicNoAccess acl)
            :ro   (.setPublicReadOnly acl)
            :rw   (.setPublicReadWriteAccess acl)
            )
          acl))
       (.setDefaultACL skygear)))

(s/def ::public #{:none :ro :rw})
(s/def ::access (s/keys :req-un [::public]))
