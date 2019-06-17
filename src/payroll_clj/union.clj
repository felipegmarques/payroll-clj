(ns payroll-clj.union
  (:require
   [java-time :as time]
   [payroll-clj.time-extended :as time-extended]))

(defprotocol Union
  (deductions [union start-date end-date] "Returns the deductions for that period"))

(defn add-service-charge [union date amount] (update union :service-charges #(conj % { :date date :amount amount})))

(defrecord ServiceProviderUnion [due-rate]
  Union
  (deductions [union start-date end-date]
    (+
     (* (time-extended/num-fridays start-date end-date) due-rate)
     (->> union
          (:service-charges)
          (filter #(time-extended/contains? start-date end-date (:date %)))
          (map :amount)
          (reduce +)))))


