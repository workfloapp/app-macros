(ns workflo.macros.specs.types
  (:refer-clojure :exclude [bigdec? double? float?])
  (:require #?(:cljs [cljs.spec :as s]
               :clj  [clojure.spec :as s])))

;;;; Helpers

(defn long? [x]
  #?(:cljs (and (number? x)
                (not (js/isNaN x))
                (not (identical? x js/Infinity))
                (= 0 (rem x 1)))
     :clj  (instance? java.lang.Long x)))

(defn bigint? [x]
  #?(:cljs (long? x)
     :clj  (instance? clojure.lang.BigInt x)))

(defn float? [x]
  #?(:cljs (and (number? x)
                (not (js/isNaN x))
                (not (identical? x js/Infinity))
                (not= (js/parseFloat x) (js/parseInt x 10)))
     :clj  (clojure.core/float? x)))

(defn double? [x]
  #?(:cljs (float? x)
     :clj  (clojure.core/double? x)))

(defn bigdec? [x]
  #?(:cljs (float? x)
     :clj  (clojure.core/bigdec? x)))

;;;; Fundamental types

(s/def ::keyword keyword?)
(s/def ::string string?)
(s/def ::boolean boolean?)
(s/def ::long long?)
(s/def ::bigint bigint?)
(s/def ::float float?)
(s/def ::double double?)
(s/def ::bigdec bigdec?)
(s/def ::instant inst?)
(s/def ::uuid uuid?)
(s/def ::uri uri?)
;;(s/def ::bytes ::TODO)
;;(s/def ::enum ::TODO)

;;;; Reference types

;; ref
;; ref-many

;;;; Type options

(defn add-options
  [spec-k & kvs]
  (apply (partial vary-meta (s/get-spec spec-k) assoc) kvs))
