(ns payroll-clj.payment-classification.commissioned-classification
  (:require
   [java-time :as time]
   [payroll-clj.payment-classification :as payment-classification]
   [payroll-clj.time-extended :as time-extended]))

(defn add-sales-receipt [commissioned-classification date amount]
  (update commissioned-classification :sales-receipt #(conj % {:date date :amount amount})))

(defn- sales-amount-in-period
  [commissioned-classification start-date end-date]
  (->> commissioned-classification
       (:sales-receipt)
       (filter #(time-extended/contains? start-date end-date (:date %)))
       (map :amount)
       (reduce +)))

(defrecord CommissionedClassification [salary commission-rate]
  payment-classification/PaymentClassification
  (payment [classification start-date end-date]
    (+
     (:salary classification)
     (*
      (:commission-rate classification)
      (sales-amount-in-period classification start-date end-date))))
  (is-pay-day? [schedule date] (time/friday? date))
  (period-start-date [schedule date] (time/minus date (time/days (+ 4 7)))))

(alter-meta! #'->CommissionedClassification assoc :private true)
(alter-meta! #'map->CommissionedClassification assoc :private true)

(defn commissioned-classification
  "creates commissioned classification record"
  [{:keys [salary commission-rate]}]
  {:pre [(float? salary)
         (< 0 salary)
         (float? commission-rate)
         (< 0.0 commission-rate 1.0)]}
  (->CommissionedClassification salary commission-rate))

