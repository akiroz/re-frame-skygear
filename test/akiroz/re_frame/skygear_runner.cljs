(ns akiroz.re-frame.skygear-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [akiroz.re-frame.skygear-tests]
            ))

(enable-console-print!)
(doo-tests 'akiroz.re-frame.skygear-tests)
