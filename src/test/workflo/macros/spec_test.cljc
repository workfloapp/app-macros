(ns workflo.macros.spec-test
  (:require #?(:cljs [cljs.test :refer-macros [deftest is]]
               :clj  [clojure.test :refer [deftest is]])
            #?(:cljs [cljs.spec :as s]
               :clj  [clojure.spec :as s])
            #?(:cljs [cljs.spec.test :as st]
               :clj  [clojure.spec.test :as st])
            [workflo.macros.view]
            [workflo.macros.query]
            [workflo.macros.query.util]
            [workflo.macros.command]
            [workflo.macros.command.util]))

#?(:cljs (deftest test-specs
           (doseq [v (s/speced-vars)]
             (println "  Testing" v)
             (let [result (st/check-var var)]
               (println "  >" result)
               (and (is (map? result))
                    (is (true? (:result result))))))))

#?(:clj (deftest test-specs
          (doseq [s (st/testable-syms)]
            (println "  Testing" s)
            (let [result (first (st/test s {:clojure.spec.test.check/opts
                                            {:num-tests 10}}))]
              (and (is (map? result))
                   (is (true? (:result result))))))))
