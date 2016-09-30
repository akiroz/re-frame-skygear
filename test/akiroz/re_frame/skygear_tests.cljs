(ns akiroz.re-frame.skygear-tests
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [chan <! put!]]
            [cljs.test :refer-macros [deftest is async]]
            [re-frame.core :refer [dispatch reg-event-fx inject-cofx trim-v]]
            [akiroz.re-frame.skygear :refer [reg-co-fx!]]
            ))

(deftest hello
  (is (= 1 1)))
