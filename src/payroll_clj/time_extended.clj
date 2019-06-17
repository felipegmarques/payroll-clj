(ns payroll-clj.time-extended
  (:require [java-time :as time]))

(defn next-day [date] (time/plus date (time/days 1)))
(defn day-before [date] (time/minus date (time/days 1)))
(defn contains? [start-date end-date date] (time/before? (day-before start-date) date (next-day end-date)))

(defn days-period
  ([date] (cons date (lazy-seq (days-period (next-day date)))))
  ([start-date end-date] (take-while #(not (time/after? % end-date)) (days-period start-date))))

(defn month-last-day? [date] (not= (time/month date) (time/month (time/plus date (time/days 1)))))

(defn num-fridays [start-date end-date] (count (filter time/friday? (days-period start-date end-date))))

