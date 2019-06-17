(ns payroll-clj.payment-classification.hourly-classification-test
  (:require
   [payroll-clj.payment-classification :as payment-classification]
   [payroll-clj.payment-classification.hourly-classification :as hourly-classification]
   [java-time :as time]
   [clojure.test :refer :all]))

(deftest test-hourly-classification
  (let [hourly-classification
        (-> (hourly-classification/hourly-classification {:hour-rate 20.0 })
            (hourly-classification/add-time-card (time/local-date 2019 4 30) 8.0)
            (hourly-classification/add-time-card (time/local-date 2019 5 06) 7.0))]
    (testing "if friday is payday" (is (payment-classification/is-pay-day? hourly-classification (time/local-date 2019 5 10))))
    (testing "if other weekdays are not payday" (is (not (payment-classification/is-pay-day? hourly-classification (time/local-date 2019 5 9)))))
    (testing "if monday of the payday week is start period"
      (is (= (time/local-date 2019 5 5) (payment-classification/period-start-date hourly-classification (time/local-date 2019 5 9)))))
    (testing "if payment takes hours in period into accounr"
      (is (= 140.0 (payment-classification/payment hourly-classification (time/local-date 2019 5 5) (time/local-date 2019 5 9)))))))

(deftest test-validation
  (testing "if hour rate is float"
    (is (thrown? AssertionError (hourly-classification/hourly-classification { :hour-rate "not-float"}))))
  (testing "if hour rate is bigger than 0.0"
    (is (thrown? AssertionError (hourly-classification/hourly-classification { :hour-rate -1.0 })))))
