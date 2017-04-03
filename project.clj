(defproject akiroz.re-frame/skygear "0.1.5-SNAPSHOT"
  :description "re-frame fx & cofx handlers for the skygear BaaS"
  :url "https://github.com/akiroz/re-frame-skygear"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[cljsjs/skygear "0.22.2-0"]
                 [binaryage/oops "0.5.3"]]
  
  :profiles {:test {:plugins [[lein-cljsbuild "1.1.4"]
                              [lein-doo "0.1.7"]]
                    :dependencies [[org.clojure/clojure "1.9.0-alpha15"]
                                   [org.clojure/clojurescript "1.9.494"]
                                   [org.clojure/core.async "0.3.442"]
                                   [re-frame "0.8.0"]
                                   [reagent "0.6.0"]
                                   ]
                    :cljsbuild {:builds [{:id "test"
                                          :source-paths ["src" "test"]
                                          :compiler {:output-dir "target/js/out"
                                                     :output-to "target/js/testable.js"
                                                     :main akiroz.re-frame.skygear-runner
                                                     :language-in :ecmascript5
                                                     :optimizations :simple}}]}}}

  )
