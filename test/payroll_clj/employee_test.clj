(ns payroll-clj.employee-test
  (:require [payroll-clj.employee :as employee]
            [payroll-clj.payment-classification.salaried-classification :as salaried-classification]
            [payroll-clj.union :as union]
            [java-time :as time]
            [clojure.test :refer :all]))

(deftest employee-test
  (let [e (employee/employee {:name "Bob" :address "Home"
                                   :payment-classification (salaried-classification/salaried-classification {:salary 2000.0 })
                                   :union (union/->ServiceProviderUnion 20.0)})]
    (testing "if employee payment is computed correctly"
      (let [payment (employee/payment e (time/local-date 2019 4 30))]
        (is (= 2000.0 (:gross-pay payment)))
        (is (= 80.0 (:deductions payment)))
        (is (= 1920.0 (:net-pay payment)))))))

(deftest employee-validation-test
  (let [valid-employee {:name "Felipe" :address "Home" :payment-classification (salaried-classification/salaried-classification {:salary 2000.0 }) :union nil}]
    (testing "if name is validate into string"
      (is (thrown? AssertionError (employee/employee (assoc valid-employee :name :not-string)))))
    (testing "if name is validate into string"
      (is (thrown? AssertionError (employee/employee (assoc valid-employee :address :not-string)))))
    (testing "if payment-classification implements protocol"
      (is (thrown? AssertionError (employee/employee (assoc valid-employee :payment-classification :not-a-payment-classification)))))
    (testing "if union implements protocol or it is null"
      (is (thrown? AssertionError (employee/employee (assoc valid-employee :union :not-union-nor-nil)))))))

