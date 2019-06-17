(defproject payroll-clj "0.1.0"
  :description "Clojure implementation of Payroll application described and Uncle Bob's book Agile Software Development"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[clojure.java-time "0.3.2"]
                 [org.clojure/clojure "1.10.0"]]
  :main ^:skip-aot payroll-clj.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
