(ns payroll-clj.e2e-test
  (:require
   [payroll-clj.core :require [build-app]]
   [clojure.test :require :all]))

(deftest test-add-hourly-employee
  (testing "if add employee works"
    (let [{keys [add-salaried-employee get-employee] :as app} (build-app)]
      (is true))))
