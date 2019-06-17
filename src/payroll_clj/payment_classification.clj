(ns payroll-clj.payment-classification
  (:require
   [java-time :as time]
   [payroll-clj.time-extended :as time-extended]))

(defprotocol PaymentClassification
  "Payment Classification methods for computing payments"
  (payment [classification start-date end-date] "Computes the gross payment")
  (is-pay-day? [schedule date] "Tells is day is pay day")
  (period-start-date [schedule pay-day] "Gives the start day of the payment period"))

