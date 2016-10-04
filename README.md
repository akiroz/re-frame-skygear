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

{:skygear {:init        {:end-point "..."
                         :api-key "..."}
           :login       {:username "foo"
                         :password "bar"}
           :logout      {}
           :signup      {:username "foo"
                        :password "bar"}
           :passwd      {:old-pass ""
                         :new-pass ""}
           :access      :rw
           :save        ^:record
                        {:_type "person"
                         :name "bob"
                         :age 20
                         :sister ^:record
                                 {:_type "person"
                                  :name "alice"
                                  :age 12}
                         :pet ^:record
                              {:_type "animal"
                               :species "cat"
                               :name "lucy"}}
           :query       {:record "person"
                         :where {:age {:>         20
                                       :sort-asc  true}}
                         :limit 20
                         :page 0}
           :subscribe   {"my-channel" :my-event
                         "your-channel" [:your :events]}
           :unsubscribe {"your-channel" :your}
           :publish     {"my-channel" {:hello "world"}}}}
```

Every async action also supports 2 additional arguments; `:success-event` and `:fail-event`.
They expect re-frame event IDs and will be dispatched accordingly.

#### Access control
The `:access` action sets the default permission on publicDB,
possible values are: `:none`, `:ro`, and `:rw`.

#### Save
The `:save` action identifies records using a metadata `^:record`,
the record type is specified by a special field `:_type`.
You can provide more than one record at the root level using a vector.
Although you can't save 2 identical records in a single `:save` operation.

Records can also be nested as long as no loops exists,
they will become skygear references. Native Skygear Record objects
may also be saved by tagging them with the `^:record` metadata.

File assets are supported by passing either a JS file object
or URL string that has the `^:file` metadata attached.

Geolocation can be saved by adding the `^:geo` metadata to a 
2-element vector: `[<latitude> <longitude>]` or native Skygear Geolocation object

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
