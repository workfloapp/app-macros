(ns workflo.macros.service
  (:require-macros [workflo.macros.service :refer [defservice]])
  (:require [clojure.spec :as s]
            [workflo.macros.bind]
            [workflo.macros.config :refer [defconfig]]
            [workflo.macros.query :as q]
            [workflo.macros.registry :refer [defregistry]]))

;;;; Configuration options for the defservice macro

(defconfig service
  ;; Supports the following options:
  ;;
  ;; :query - a function that takes a parsed query and the context
  ;;          that was passed to `deliver-to-service-component!` or
  ;;          `deliver-to-services!` by the caller;
  ;;          this function is used to query a data store for data
  ;;          that the service needs to process its input.
  {:query nil})

;;;; Service registry

(defregistry service)

;;;; Service components

(defregistry service-component)

(defn new-service-component
  ([name]
   (new-service-component name {}))
  ([name config]
   (let [service (resolve-service name)]
     ((:component-ctor service) {:service service
                                 :config config}))))

;;;; Service interface

(defprotocol IService
  (process [this query-result data context]))

;;;; Delivery to services

(defn deliver-to-service-component!
  ([component data]
   (deliver-to-service-component! component data nil))
  ([component data context]
   (let [query  (some-> component :service :query
                        (q/bind-query-parameters data))
         result (when query
                  (some-> (get-service-config :query)
                          (apply [query context])))]
     (process component result data context))))

(defn deliver-to-services!
  ([data]
   (deliver-to-services! data nil))
  ([data context]
   {:pre [(s/valid? (s/map-of keyword? any?) data)]}
   (doseq [[service-kw service-data] data]
     (let [service-name (symbol (name service-kw))
           component    (try
                          (resolve-service-component service-name)
                          (catch js/Error e
                            (println "WARN:" (.-message e))))]
       (some-> component
               (deliver-to-service-component! service-data
                                              context))))))
