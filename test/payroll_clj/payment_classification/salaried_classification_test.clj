(ns payroll-clj.payment-classification.salaried-classification-test
  (:require
   [payroll-clj.payment-classification :as payment-classification]
   [payroll-clj.payment-classification.salaried-classification :as salaried-classification]
   [java-time :as time]
   [clojure.test :refer :all]))

(deftest test-salaried-classification
  (let [salaried-classification (salaried-classification/salaried-classification {:salary 2000.0})]
    (testing "if last day of month is pay day" (is (payment-classification/is-pay-day? salaried-classification (time/local-date 2019 5 31))))
    (testing "if other day than last day is not pay day"
      (is (not (payment-classification/is-pay-day? salaried-classification (time/local-date 2019 5 30)))))
    (testing "if first day of month is the start of period"
      (is (= (time/local-date 2019 5 1) (payment-classification/period-start-date salaried-classification (time/local-date 2019 5 31)))))
    (testing "if for a month, salary is correct"
      (is (= 2000.0 (payment-classification/payment salaried-classification (time/local-date 2019 5 1) (time/local-date 2019 5 31)))))))

(deftest test-salaried-classification-validation
  (testing "if validates if salary is float"
    (is (thrown? AssertionError (salaried-classification/salaried-classification {:salary :not-flaot}))))
  (testing "if validates if salary is bigger than 0"
    (is (thrown? AssertionError (salaried-classification/salaried-classification {:salary -1.0})))))


