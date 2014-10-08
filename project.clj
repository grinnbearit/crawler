(defproject crawler "0.1.0"
  :description "website crawler"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [enlive "1.1.5"]
                 [http-kit "2.1.16"]
                 [hiccup "1.0.5"]]
  :profiles {:dev {:plugins [[lein-midje "3.1.3"]]
                   :dependencies [[midje "1.6.3"]
                                  [cheshire "5.3.1"]]}})
