(ns payroll-clj.union-test
  (:require [payroll-clj.union :as union]
            [java-time :as time]
            [clojure.test :refer :all]))


(deftest test-deductions-for-no-friday
  (testing "Tests if ServiceProviderUnion returns 0 as deduction when there is no friday on the period"
    (let [empty-union (union/->ServiceProviderUnion 20.0)]
      (is (= 0.0 (union/deductions empty-union (time/local-date 2019 6 8) (time/local-date 2019 6 13)))))))

(deftest test-deductions-for-one-friday
  (testing "Tests if it computs correct deductions "
    (let [empty-union (union/->ServiceProviderUnion 20.0)]
      (is (= 20.0 (union/deductions empty-union (time/local-date 2019 6 6) (time/local-date 2019 6 13)))))))

(deftest test-deductions-for-two-fridays
  (testing "Tests if it computs correct deductions "
    (let [empty-union (union/->ServiceProviderUnion 20.0)]
      (is (= 40.0 (union/deductions empty-union (time/local-date 2019 6 6) (time/local-date 2019 6 20)))))))


(deftest test-deductions-with-service-charges
  (testing "Tests if ServiceProviderUnion calculates the deductions with the service charges involved"
    (let [union-with-services
          (-> (union/->ServiceProviderUnion 20.0)
              (union/add-service-charge (time/local-date 2019 6 5) 10.0)
              (union/add-service-charge (time/local-date 2019 6 7) 10.0))]
      (is (= 30.0 (union/deductions union-with-services (time/local-date 2019 6 6) (time/local-date 2019 6 13)))))))




