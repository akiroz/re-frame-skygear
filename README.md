# re-frame-skygear

[![Clojars Project](https://img.shields.io/clojars/v/akiroz.re-frame/skygear.svg)](https://clojars.org/akiroz.re-frame/skygear)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/akiroz/re-frame-skygear/master/LICENSE)
[![Build Status](https://travis-ci.org/akiroz/re-frame-skygear.svg?branch=master)](https://travis-ci.org/akiroz/re-frame-skygear)


A re-frame, data-based API for the Skygear JS SDK.

Depends on `re-frame >= 0.8.0`. 

**IMPORTANT: This project is still WIP, It does not work yet!**

## Usage

Registering the skygear (co)fx handler:

```clojure
(ns your-ns
  (:require [akiroz.re-frame.skygear :refer [reg-co-fx!]]))

;; both :fx and :cofx keys are optional, they will not be registered if unspecified.
(reg-co-fx! {:fx   :skygear   ;; re-frame fx ID
             :cofx :skygear}) ;; re-frame cofx ID

```

### Skygear fx

Here's the complete effects map:

```clojure

;; <fx>      <action>        <args>

{:skygear [:init        {:end-point "..."
                         :api-key "..."}
           :login       {:username "foo"
                         :password "bar"}
           :logout      {}
           :signup      {:username "foo"
                        :password "bar"}
           :passwd      {:old-pass ""
                         :new-pass ""}
           :whoami      {}
           :access      {:public :rw}
           :save        {:records [^:rec
                                   {:_type "person"
                                    :name "bob"
                                    :age 20}]}
           :query       {:record "person"
                         :where {:age {:>         20
                                       :sort-asc  true}}
                         :limit 20
                         :page 0}
           :subscribe   {"my-channel" :my-event
                         "your-channel" [:your :events]}
           :unsubscribe {"your-channel" :your}
           :publish     {"my-channel" {:hello "world"}}]}
```

Every async action also supports 2 additional arguments; `:success-event` and `:fail-event`.
They expect re-frame event IDs and will be dispatched accordingly.

#### Access control
The `:access` action sets the default permission on publicDB,
possible values are: `:none`, `:ro`, and `:rw`.

#### Save

The `:save` action uses metadata to build requests.
Since native JS objects don't support metadata,
they must be wraped inside a vector: `^:ref [<JS Object>]`

```clojure
;; Creates a new record and save it
{:save {:records [                                ;; vec of items to save
                  ^:rec
                  {:_type ""                      ;; record type
                   :owner ^:ref [user]            ;; reference existing skygear record
                   :photo ^:asset {:file obj}     ;; upload a file (JS File / URL)
                   :place ^:geo [0,0]             ;; geolocation (vec / skygear obj)
                   :related ^:rec                 ;; save & reference new record
                            {:_type ""
                             :field "value"}}]}}

;; Updates an existing record
{:save {:records [^:rec {:_type "person"
                         :_id "person/4bc21b46-26f3-4474-afbb-280eee501db2"
                         :age 21}]}}
```


#### Query
The `:query` action supports the following `:where` clauses:

```clojure
{:=            ""       ;; equal
 :not=         ""       ;; not equal
 :<            0        ;; less than
 :<=           0        ;; less than or equal
 :>            0        ;; greater than
 :>=           0        ;; greather than or equal
 :in           #{1 2 3} ;; value is in set
 :not-in       #{1 2 3} ;; value is not in set
 :contains     ""       ;; set contain
 :not-contains ""       ;; set does not contain
 :like         "%"      ;; match wildcard expression
 :not-like     "%"      ;; does not match wildcard expression
 :sort-asc     true     ;; sort ascending
 :sort-dec     true     ;; sort descending
 :transient    true     ;; transient include reference
 }
```


### Skygear cofx / db-interceptor

Here's the complete skygear cofx map:

```clojure
{:skygear {:user <user-object>}}

```

It is also possible to inject the cofx map into an `app-db` key:


```clojure
(ns your-ns
  (:require [akiroz.re-frame.skygear :refer [inject-db]]))

;; inject the cofx map into the :skygear key inside db.
(reg-event-fx
  :read-current-user
  [(inject-db :skygear)]
  (fn [{:keys [db]} _]
    (println (get-in db [:skygear :user]))
    {}))

```
