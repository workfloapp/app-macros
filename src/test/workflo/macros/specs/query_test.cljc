(ns workflo.macros.specs.query-test
  (:require [clojure.test :refer [are deftest is]]
            [clojure.spec :as s]
            [workflo.macros.query-new :as q]
            [workflo.macros.specs.query]))

(deftest conforming-regular-properties
  (are [out in] (= out (q/conform in))
    '[[:property [:simple a]]]
    '[a]

    '[[:property [:simple a]]
      [:property [:simple b]]]
    '[a b]

    '[[:property [:simple a]]
      [:property [:simple b]]
      [:property [:simple c]]]
    '[a b c]))

(deftest parsing-regular-properties
  (are [out in] (= out (q/conform-and-parse in))
    '[{:name a :type :property}]
    '[a]

    '[{:name a :type :property}
      {:name b :type :property}]
    '[a b]

    '[{:name a :type :property}
      {:name b :type :property}
      {:name c :type :property}]
    '[a b c]))

(deftest conforming-link-properties
  (are [out in] (= out (q/conform in))
    '[[:property [:link [a _]]]]
    '[[a _]]

    '[[:property [:link [a _]]]
      [:property [:link [b 1]]]]
    '[[a _] [b 1]]

    '[[:property [:link [a _]]]
      [:property [:link [b 1]]]
      [:property [:link [c :x]]]]
    '[[a _] [b 1] [c :x]]))

(deftest parsing-link-properties
  (are [out in] (= out (q/conform-and-parse in))
    '[{:name a :type :link :link-id _}]
    '[[a _]]

    '[{:name a :type :link :link-id _}
      {:name b :type :link :link-id 1}]
    '[[a _] [b 1]]

    '[{:name a :type :link :link-id _}
      {:name b :type :link :link-id 1}
      {:name c :type :link :link-id :x}]
    '[[a _] [b 1] [c :x]]))

(deftest conforming-joins-with-a-simple-property-source
  (are [out in] (= out (q/conform in))
    '[[:property [:join [:properties {[:simple a]
                                      [[:property [:simple b]]]}]]]]
    '[{a [b]}]

    '[[:property [:join [:properties {[:simple a]
                                      [[:property [:simple b]]
                                       [:property [:simple c]]]}]]]]
    '[{a [b c]}]

    '[[:property [:join [:properties {[:simple a]
                                      [[:property [:simple b]]
                                       [:property [:simple c]]]}]]]
      [:property [:simple d]]]
    '[{a [b c]} d]

    '[[:property [:join [:properties {[:simple a]
                                      [[:property [:simple b]]
                                       [:property [:simple c]]]}]]]
      [:property [:join [:properties {[:simple d]
                                      [[:property [:simple e]]
                                       [:property [:simple f]]]}]]]]
    '[{a [b c]} {d [e f]}]

    '[[:property [:join [:recursive {[:simple a]
                                     [:unlimited ...]}]]]]
    '[{a ...}]

    '[[:property [:join [:recursive {[:simple a]
                                     [:limited 5]}]]]]
    '[{a 5}]

    '[[:property [:join [:model {[:simple a]
                                 [:model User]}]]]]
    '[{a User}]))

(deftest parsing-joins-with-a-simple-property-source
  (are [out in] (= out (q/conform-and-parse in))
    '[{:name a :type :join
       :join-source {:name a :type :property}
       :join-target [{:name b :type :property}]}]
    '[{a [b]}]

    '[{:name a :type :join
       :join-source {:name a :type :property}
       :join-target [{:name b :type :property}
                     {:name c :type :property}]}]
    '[{a [b c]}]

    '[{:name a :type :join
       :join-source {:name a :type :property}
       :join-target [{:name b :type :property}
                     {:name c :type :property}]}
      {:name d :type :property}]
    '[{a [b c]} d]

    '[{:name a :type :join
       :join-source {:name a :type :property}
       :join-target [{:name b :type :property}
                     {:name c :type :property}]}
      {:name d :type :join
       :join-source {:name d :type :property}
       :join-target [{:name e :type :property}
                     {:name f :type :property}]}]
    '[{a [b c]} {d [e f]}]

    '[{:name a :type :join
       :join-source {:name a :type :property}
       :join-target ...}]
    '[{a ...}]

    '[{:name a :type :join
       :join-source {:name a :type :property}
       :join-target 5}]
    '[{a 5}]

    '[{:name a :type :join
       :join-source {:name a :type :property}
       :join-target User}]
    '[{a User}]))

(deftest conforming-joins-with-a-link-source
  (are [out in] (= out (q/conform in))
    '[[:property [:join [:properties {[:link [a _]]
                                      [[:property [:simple b]]]}]]]]
    '[{[a _] [b]}]

    '[[:property [:join [:properties {[:link [a 1]]
                                      [[:property [:simple b]]
                                       [:property [:simple c]]]}]]]]
    '[{[a 1] [b c]}]

    '[[:property [:join [:properties {[:link [a :x]]
                                      [[:property [:simple b]]
                                       [:property [:simple c]]
                                       [:property [:simple d]]]}]]]]
    '[{[a :x] [b c d]}]))

(deftest parsing-joins-with-a-link-source
  (are [out in] (= out (q/conform-and-parse in))
    '[{:name a :type :join
       :join-source {:name a :type :link :link-id _}
       :join-target [{:name b :type :property}]}]
    '[{[a _] [b]}]

    '[{:name a :type :join
       :join-source {:name a :type :link :link-id 1}
       :join-target [{:name b :type :property}
                     {:name c :type :property}]}]
    '[{[a 1] [b c]}]

    '[{:name a :type :join
       :join-source {:name a :type :link :link-id :x}
       :join-target [{:name b :type :property}
                     {:name c :type :property}
                     {:name d :type :property}]}]
    '[{[a :x] [b c d]}]))

(deftest conforming-prefixed-properties
  (are [out in] (= out (q/conform in))
    '[[:prefixed-properties {:base a
                             :children [[:property [:simple b]]]}]]
    '[a [b]]

    '[[:prefixed-properties {:base a
                             :children [[:property [:simple b]]
                                        [:property [:simple c]]]}]]
    '[a [b c]]

    '[[:prefixed-properties {:base a
                             :children [[:property [:simple b]]
                                        [:property [:simple c]]]}]
      [:property [:simple d]]]
    '[a [b c] d]

    '[[:prefixed-properties {:base a
                             :children [[:property [:simple b]]
                                        [:property [:simple c]]]}]
      [[:prefixed-properties {:base d
                              :children [[:property [:simple e]]
                                         [:property [:simple f]]]}]]]
    '[a [b c] d [e f]]))

(deftest parsing-prefixed-properties
 (are [out in] (= out (q/conform-and-parse in))
   '[{:name a/b :type :property}]
   '[a [b]]

   '[{:name a/b :type :property}
     {:name a/c :type :property}]
   '[a [b c]]

   '[{:name a/b :type :property}
     {:name a/c :type :property}
     {:name d :type :property}]
   '[a [b c] d]

   '[{:name a/b :type :property}
     {:name a/c :type :property}
     {:name d/e :type :property}
     {:name d/f :type :property}]
   '[a [b c] d [e f]]))

(deftest conforming-aliased-regular-properties
  (are [out in] (= out (q/conform in))
    '[[:aliased-property {:property [:simple a] :as :as :alias b}]]
    '[a :as b]

    '[[:aliased-property {:property [:simple a] :as :as :alias b}]
      [:aliased-property {:property [:simple c] :as :as :alias d}]]
    '[a :as b c :as d]))

(deftest parsing-aliased-regular-properties
  (are [out in] (= out (q/conform-and-parse in))
    '[{:name a :type :property :alias b}]
    '[a :as b]

    '[{:name a :type :property :alias b}
      {:name c :type :property :alias d}]
    '[a :as b c :as d]))

(deftest conforming-aliased-links
  (are [out in] (= out (q/conform in))
    '[[:aliased-property {:property [:link [a _]] :as :as :alias b}]]
    '[[a _] :as b]

    '[[:aliased-property {:property [:link [a 1]] :as :as :alias b}]]
    '[[a 1] :as b]

    '[[:aliased-property {:property [:link [a :x]] :as :as :alias b}]]
    '[[a :x] :as b]

    '[[:aliased-property {:property [:link [a _]] :as :as :alias b}]
      [:aliased-property {:property [:link [c _]] :as :as :alias d}]]
    '[[a _] :as b [c _] :as d]))

(deftest parsing-aliased-links
  (are [out in] (= out (q/conform-and-parse in))
    '[{:name a :type :link :link-id _ :alias b}]
    '[[a _] :as b]

    '[{:name a :type :link :link-id 1 :alias b}]
    '[[a 1] :as b]

    '[{:name a :type :link :link-id :x :alias b}]
    '[[a :x] :as b]

    '[{:name a :type :link :link-id _ :alias b}
      {:name c :type :link :link-id _ :alias d}]
    '[[a _] :as b [c _] :as d]))

(deftest conforming-aliased-joins
  (are [out in] (= out (q/conform in))
    '[[:aliased-property
       {:property [:join [:properties {[:simple a]
                                       [[:property [:simple b]]]}]]
        :as :as :alias c}]]
    '[{a [b]} :as c]

    '[[:aliased-property
       {:property [:join [:properties {[:simple a]
                                       [[:property [:simple b]]
                                        [:property [:simple c]]]}]]
        :as :as :alias d}]
      [:aliased-property
       {:property [:join [:properties {[:simple e]
                                       [[:property [:simple f]]
                                        [:property [:simple g]]]}]]
        :as :as :alias h}]]
    '[{a [b c]} :as d {e [f g]} :as h]))

(deftest parsing-aliased-joins
  (are [out in] (= out (q/conform-and-parse in))
    '[{:name a :type :join
       :join-source {:name a :type :property}
       :join-target [{:name b :type :property}]
       :alias c}]
    '[{a [b]} :as c]

    '[{:name a :type :join
       :join-source {:name a :type :property}
       :join-target [{:name b :type :property}
                     {:name c :type :property}]
       :alias d}
      {:name e :type :join
       :join-source {:name e :type :property}
       :join-target [{:name f :type :property}
                     {:name g :type :property}]
       :alias h}]
    '[{a [b c]} :as d {e [f g]} :as h]))

(deftest conforming-aliased-prefixed-properties
  (are [out in] (= out (q/conform in))
    '[[:prefixed-properties {:base a
                             :children [[:aliased-property
                                         {:property [:simple b]
                                          :as :as :alias c}]]}]]
    '[a [b :as c]]

    '[[:prefixed-properties {:base a
                             :children [[:aliased-property
                                         {:property [:simple b]
                                          :as :as :alias c}]
                                        [:aliased-property
                                         {:property [:simple d]
                                          :as :as :alias e}]]}]]
    '[a [b :as c d :as e]]))

(deftest parsing-aliased-prefixed-properties
  (are [out in] (= out (q/conform-and-parse in))
    '[{:name a/b :type :property :alias c}]
    '[a [b :as c]]

    '[{:name a/b :type :property :alias c}
      {:name a/d :type :property :alias e}]
    '[a [b :as c d :as e]]))

(deftest conforming-parameterization
  (are [out in] (= out (q/conform in))
    '[[:parameterization {:query [:property [:simple a]]
                          :parameters {b c}}]]
    '[(a {b c})]

    '[[:parameterization {:query [:property [:simple a]]
                          :parameters {b c d e}}]]
    '[(a {b c d e})]

    '[[:parameterization
       {:query [:property [:join [:properties {[:simple a]
                                               [[:property [:simple b]]
                                                [:property [:simple c]]]}]]]
        :parameters {d e f g}}]]
    '[({a [b c]} {d e f g})]

    '[[:parameterization {:query [:aliased-property {:property [:simple a]
                                                     :as :as :alias b}]
                          :parameters {c d e f}}]]
    '[(a :as b {c d e f})]

    '[[:parameterization
       {:query [:aliased-property
                {:property [:join [:properties {[:simple a]
                                                [[:property [:simple b]]
                                                 [:property [:simple c]]]}]]
                 :as :as :alias d}]
        :parameters {e f g h}}]]
    '[({a [b c]} :as d {e f g h})]))

(deftest parsing-parameterization
  (are [out in] (= out (q/conform-and-parse in))
    '[{:name a :type :property :parameters {b c}}]
    '[(a {b c})]

    '[{:name a :type :property :parameters {b c d e}}]
    '[(a {b c d e})]

    '[{:name a :type :join
       :join-source {:name a :type :property}
       :join-target [{:name b :type :property}
                     {:name c :type :property}]
       :parameters {d e f g}}]
    '[({a [b c]} {d e f g})]

    '[{:name a :type :property :alias b :parameters {c d e f}}]
    '[(a :as b {c d e f})]

    '[{:name a :type :join
       :join-source {:name a :type :property}
       :join-target [{:name b :type :property}
                     {:name c :type :property}]
       :alias d
       :parameters {e f g h}}]
    '[({a [b c]} :as d {e f g h})]))
