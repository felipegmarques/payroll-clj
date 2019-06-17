(ns payroll-clj.payment-classification.salaried-classification
  (:require
   [payroll-clj.payment-classification :as payment-classification]
   [payroll-clj.time-extended :as time-extended]
   [java-time :as time]
   [payroll-clj.payment-classification.salaried-classification :as salaried-classification]))

(defrecord SalariedClassification [salary]
  payment-classification/PaymentClassification
  (payment [classification start-date end-date] (:salary classification))
  (is-pay-day? [schedule date] (time-extended/month-last-day? date))
  (period-start-date [schedule date]
    (time/local-date (time/year date) (time/month date) 01)))

(alter-meta! #'->SalariedClassification assoc :private true)
(alter-meta! #'map->SalariedClassification assoc :private true)

(defn salaried-classification
  "constructs salaried classification record"
  [{:keys [salary]}]
  {:pre [(float? salary)
         (< 0 salary)]}
  (->SalariedClassification salary))

