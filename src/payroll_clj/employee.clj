(ns payroll-clj.employee
  (:require [payroll-clj.payment-classification :as payment-classification]
            [payroll-clj.union :as union]
            [payroll-clj.employee :as employee]))

(defn payment [{ :keys [payment-classification union] :as employee} pay-day]
  (let [start-date (payment-classification/period-start-date payment-classification pay-day)
        gross-pay (payment-classification/payment payment-classification start-date pay-day)
        deductions (if (some? union) (union/deductions union start-date pay-day) 0.0)]
    {:gross-pay gross-pay :deductions deductions :net-pay (- gross-pay deductions)}))

(defrecord Employee [name address payment-classification union])

(alter-meta! #'->Employee assoc :private true)
(alter-meta! #'map->Employee assoc :private true)

(defn employee
  "Creates a new employee record"
  [{:keys [name address payment-classification union]}]
  {:pre [(string? name)
         (string? address)
         (and (satisfies? payment-classification/PaymentClassification payment-classification))
         (or (= nil union) (satisfies? union/Union union))]}
  (->Employee name address payment-classification union))


