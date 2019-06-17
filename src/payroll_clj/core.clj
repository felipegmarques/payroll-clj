(ns payroll-clj.core
  (:require
   [payroll-clj.employee :as employee]
   [payroll-clj.payment-classification :as payment-classification]
   [payroll-clj.payment-classification.hourly-classification :as hourly-classification]
   [payroll-clj.payment-classification.commissioned-classification :as commissioned-classification]
   [payroll-clj.payment-classification.salaried-classification :as salaried-classification]
   [payroll-clj.union :as union]
   [java-time :as time])
  (:gen-class))


(defn update-in-employee [db id ks fn & rest]
  (apply swap! db update-in [:employees id] update-in ks fn rest))

(defn update-employee [db id fn & rest]
  (let [wrapped-fn (comp employee/employee fn)]
    (apply swap! db update-in [:employees id] wrapped-fn rest)))

(defn assoc-in-employee [db id ks val]
  (let [ks (concat [:employees id] ks)]
    (swap! db assoc-in ks val)))

(defn assoc-employees [db id val & kvals]
  (let [val (employee/employee val)
        kvals (map (fn [[id val]] [id (employee/employee val)]) kvals)]
    (swap! db update :employees #(apply assoc % id val kvals))))

(defn dissoc-employee [db id]
  (swap! db update :employees dissoc id))

(defn merge-employee [db id & maps]
  (apply swap! db update-in [:employees id] (comp employee/employee merge) maps))

(defn get-employee [db id]
  (get-in @db [:employees id]))

(defn build-app []
  (let [db (atom {})]
    {
     :add-salaried-employee
     (fn [id & {:keys [salary] :as employee }]
       (->> (salaried-classification/salaried-classification {:salary salary})
            (assoc employee :payment-classification)
            (assoc-employees db id)))

     :change-employee-to-salaried
     (fn [id & {:keys [salary]}]
       (->> (salaried-classification/salaried-classification {:salary salary})
            (assoc-in-employee db id [:payment-classification])))

     :add-hourly-employee
     (fn [id & {:keys [hour-rate] :as employee}]
       (->> (hourly-classification/hourly-classification { :hour-rate hour-rate })
            (assoc employee :payment-classification)
            (assoc-employees db id)))

     :change-employee-to-hourly
     (fn [id & {:keys [hour-rate] }]
       (->> (hourly-classification/hourly-classification { :hour-rate hour-rate })
            (assoc-in-employee db id [:payment-classification])))

     :add-time-card
     (fn [id & {:keys [date hours]}]
       (when-let [employee (get-employee db id)]
         (update-in-employee db id [:payment-classification] hourly-classification/add-time-card date hours)))
 
     :add-commissioned-employee
     (fn [id & {:keys [salary commission-rate] :as employee}]
       (->> (commissioned-classification/commissioned-classification {:salary salary :commission-rate commission-rate})
            (assoc employee :payment-classification)
            (assoc-employees db id)))

     :change-employee-to-commissioned
     (fn [id & {:keys [salary commission-rate]}]
       (->> (commissioned-classification/commissioned-classification {:salary salary :commission-rate commission-rate})
            (assoc-in-employee db id [:payment-classification])))

     :add-sales-receipt
     (fn [id & {:keys [date amount]}]
       (when-let [employee (get-employee db id)]
         (update-in-employee db id [:payment-classification] commissioned-classification/add-sales-receipt date amount)))

     :payments
     (fn [date]
       (->> @db
           (:employees)
           (filter (fn [[id employee]] (-> employee
                         (:payment-classification)
                         (payment-classification/is-pay-day? date))))
           (map (fn [[id employee]] [id (employee/payment employee date)]))
           (apply conj {})))

     :get-employee (fn [id] (get-employee db id))

     :change-employee
     (fn [id & employee]
       (->> employee
            (apply array-map)
            (merge-employee db id)))

     :delete-employee (fn [id] (dissoc-employee db id))

     :affiliate-employee-to-union
     (fn [id & union]
       (->> (apply array-map union)
            (union/map->ServiceProviderUnion)
            (assoc-in-employee db id [:union])))

     :unaffiliate-employee (fn [id] (assoc-in-employee db id [:union] nil))

     :add-service-charge
     (fn [member-id & {:keys [date amount]}]
       (->> (:employees @db)
            (filter (fn [[id {:keys [union] :as employee}]] (and (some? union) (= member-id (:member-id union)))))
            (map (fn [[id {:keys [union] :as employee}]] (update-in-employee db id [:union] union/add-service-charge date amount)))
            (dorun)))
    }))
