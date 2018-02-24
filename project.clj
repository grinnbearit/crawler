(defproject crawler "0.2.0"
  :description "website crawler"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [enlive "1.1.6"]
                 [http-kit "2.2.0"]
                 [hiccup "1.0.5"]
                 [swissknife "1.1.0"]
                 [cheshire "5.8.0"]]
  :profiles {:dev {:dependencies [[midje "1.9.0"]]}})
