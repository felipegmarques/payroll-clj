(ns payroll-clj.payment-classification.hourly-classification
  (:require
   [payroll-clj.payment-classification :as payment-classification]
   [payroll-clj.time-extended :as time-extended]
   [java-time :as time]
   [payroll-clj.payment-classification.hourly-classification :as hourly-classification]))

(defn add-time-card [hourly-classification date hours]
  (update hourly-classification :time-cards #(conj % {:date date :hours hours})))

(defrecord HourlyClassification [hour-rate]
  payment-classification/PaymentClassification
  (payment [classification start-date end-date]
    (* (hours-in-period classification start-date end-date) (:hour-rate classification)))
  (is-pay-day? [schedule date] (time/friday? date))
  (period-start-date [schedule date] (time/minus date (time/days 4))))

(alter-meta! #'->HourlyClassification assoc :private true)
(alter-meta! #'map->HourlyClassification assoc :private true)

(defn hourly-classification
  "creates hourly classification record"
  [{:keys [hour-rate]}]
  {:pre [(float? hour-rate)
         (< 0 hour-rate)]}
  (->HourlyClassification hour-rate))

(defn- hours-in-period
  [hourly-classification start-date end-date]
  (->> hourly-classification
       (:time-cards)
       (filter #(time-extended/contains? start-date end-date (:date %)))
       (map :hours)
       (reduce + 0)))


