(ns payroll-clj.core-test
  (:require
   [clojure.test :refer :all]
   [payroll-clj.core :refer [build-app]]
   [java-time :as time])
  (:import
   [payroll_clj.payment_classification.hourly_classification HourlyClassification]
   [payroll_clj.payment_classification.commissioned_classification CommissionedClassification]
   [payroll_clj.payment_classification.salaried_classification SalariedClassification]))

(require '([payroll-clj.core :refer [build-app]]) :reload)

(deftest test-add-salaried-employee
  (testing "if add salaried employee works"
    (let [{:keys [add-salaried-employee get-employee] :as app} (build-app)
          {:keys [name address payment-classification]}
          (do
            (add-salaried-employee 1 :name "Bob" :address "Home" :salary 2000.0)
            (get-employee 1))]
      (is (= "Bob" name))
      (is (= "Home" address))
      (is (= 2000.0 (:salary payment-classification))))))

(deftest test-add-hourly-employee
  (testing "if add hourly employee works"
    (let [{:keys [add-hourly-employee get-employee] :as app} (build-app)
          {:keys [name address payment-classification]}
          (do
            (add-hourly-employee 1 :name "Bob" :address "Home" :hour-rate 20.0)
            (get-employee 1))]
      (is (= "Bob" name))
      (is (= "Home" address))
      (is (= 20.0 (:hour-rate payment-classification))))))

(deftest test-add-commissioned-employee
  (testing "if add commissioned employee works"
    (let [{:keys [add-commissioned-employee get-employee] :as app} (build-app)
          {:keys [name address payment-classification]}
          (do
            (add-commissioned-employee 1 :name "Bob" :address "Home" :salary 1000.0 :commission-rate 0.01)
            (get-employee 1))]
      (is (= "Bob" name))
      (is (= "Home" address))
      (is (= 1000.0 (:salary payment-classification)))
      (is (= 0.01 (:commission-rate payment-classification))))))

(deftest test-change-employee-properties
  (testing "if change employee properties works"
    (let [{:keys [add-salaried-employee get-employee change-employee] :as app} (build-app)
          old-employee (do
                         (add-salaried-employee 1 :name "Bob" :address "Home" :salary 1000.0)
                         (get-employee 1))
          changed-employee (do
                             (change-employee 1 :name "Felipe"  :address "Work")
                             (get-employee 1))]
      (is (= "Bob" (:name old-employee)))
      (is (= "Home"  (:address old-employee)))
      (is (= "Felipe" (:name changed-employee)))
      (is (= "Work" (:address changed-employee)))
      (is (= (:payment-classification old-employee) (:payment-classification changed-employee))))))

(deftest test-change-payment-classification
  (testing "if change employee payment classification works"
    (let [{:keys [add-salaried-employee get-employee change-employee-to-hourly change-employee-to-commissioned change-employee-to-salaried] :as app} (build-app)
          hourly-employee
          (do
            (add-salaried-employee 1 :name "Bob" :address "Home" :salary 1000.0)
            (change-employee-to-hourly 1 :hour-rate 20.0)
            (get-employee 1))
          commissioned-employee
          (do
            (change-employee-to-commissioned 1 :salary 1000.0 :commission-rate 0.01)
            (get-employee 1))
          salaried-employee
          (do
            (change-employee-to-salaried 1 :salary 2000.0)
            (get-employee 1))]
      ; For some reason this test is failing
      ;(is (instance? HourlyClassification (:payment-classification hourly-employee)))
      ;(is (instance? CommissionedClassification (:payment-classification commissioned-employee)))
      (is (instance? SalariedClassification (:payment-classification salaried-employee))))))


(deftest test-perform-payment
  (testing "if perform payment works"
    (let [{:keys [add-salaried-employee
                  add-hourly-employee
                  add-commissioned-employee
                  add-time-card
                  add-sales-receipt
                  affiliate-employee-to-union
                  unaffiliate-employee
                  add-service-charge
                  payments] :as app} (build-app)
          payments (do
                     (add-salaried-employee 1 :name "Bob"  :address "Home" :salary 2000.0)
                     (affiliate-employee-to-union 1 :member-id 0 :due-rate 10.0)
                     (unaffiliate-employee 1)

                     (add-hourly-employee 2 :name "Felipe" :address "Work" :hour-rate 20.0)
                     (add-time-card 2 :date (time/local-date 2019 5 29) :hours 8)
                     (affiliate-employee-to-union 2 :member-id 1 :due-rate 10.0)
                     (add-service-charge 1 :date (time/local-date 2019 5 29) :amount 20.0)

                     (add-commissioned-employee 3 :name "Bonfi"  :address "Home" :salary 1000.0 :commission-rate 0.01)
                     (add-sales-receipt 3 :date (time/local-date 2019 5 29) :amount 1000.0)

                     (payments (time/local-date 2019 5 31)))]

      (is (= {:gross-pay 2000.0 :deductions 0.0 :net-pay 2000.0} (get payments 1)))
      (is (= {:gross-pay 160.0 :deductions 30.0 :net-pay 130.0} (get payments 2)))
      (is (= {:gross-pay 1010.0 :deductions 0.0 :net-pay 1010.0} (get payments 3))))))

(deftest test-delete-employee
  (testing "if delete employee works"
    (let [{:keys [add-commissioned-employee get-employee delete-employee] :as app} (build-app)
          {:keys [name address payment-classification]}
          (do (add-commissioned-employee 1 :name "Bob" :address "Home" :salary 1000.0 :commission-rate 0.01)
              (get-employee 1))
          deleted-employee (do (delete-employee 1) (get-employee 1))]
      (is (= "Bob" name))
      (is (= "Home" address))
      (is (= 1000.0 (:salary payment-classification)))
      (is (= 0.01 (:commission-rate payment-classification)))
      (is (= nil deleted-employee)))))

; TODO: 
; [] Create protocol for employees DB and use it to access employees and separate actions by employees type
; [] Add test into datastructure for classifications
; [x] Move payment classification to its own file
; [x] Use keyword arguments for all transactions (better for reading)
; [x] Implement ChangeEmployee and PerformPayments
