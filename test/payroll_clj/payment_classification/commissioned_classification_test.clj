(ns payroll-clj.payment-classification.commissioned-classification-test
  (:require
   [payroll-clj.payment-classification :as payment-classification]
   [payroll-clj.payment-classification.commissioned-classification :as commissioned-classification]
   [java-time :as time]
   [clojure.test :refer :all]))

(deftest test-commissioned-classification-validation
  (let [valid-commissioned-classification { :salary 2000.0 :commission-rate 0.01}]
    (testing "if salary is flaot"
      (is (thrown? AssertionError (commissioned-classification/commissioned-classification (assoc valid-commissioned-classification :salary :not-float)))))
    (testing "if salary is bigger than 0.0"
      (is (thrown? AssertionError (commissioned-classification/commissioned-classification (assoc valid-commissioned-classification :salary -1.0)))))
    (testing "if commission-rate is float"
      (is (thrown? AssertionError (commissioned-classification/commissioned-classification (assoc valid-commissioned-classification :commission-rate :not-float)))))
    (testing "if commission-rate is between 0.0 and 1.0"
      (is (thrown? AssertionError (commissioned-classification/commissioned-classification (assoc valid-commissioned-classification :commission-rate 1.01)))))))

(deftest test-commissioned-classification
  (let [commissioned-classification
        (-> (commissioned-classification/commissioned-classification { :salary 2000.0 :commission-rate 0.01 })
            (commissioned-classification/add-sales-receipt (time/local-date 2019 4 30) 100.0)
            (commissioned-classification/add-sales-receipt (time/local-date 2019 5 14) 200.0))]
    (testing "if friday is payday" (is (payment-classification/is-pay-day? commissioned-classification (time/local-date 2019 5 24))))
    (testing "if other weekdays are not payday" (is (not (payment-classification/is-pay-day? commissioned-classification (time/local-date 2019 5 23)))))
    (testing "if monday of the week before is start period"
      (is (= (time/local-date 2019 5 13) (payment-classification/period-start-date commissioned-classification (time/local-date 2019 5 24)))))
    (testing "if payment takes sales in period into account"
      (is (= 2002.0 (payment-classification/payment commissioned-classification (time/local-date 2019 5 13) (time/local-date 2019 5 24)))))))

