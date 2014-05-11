(ns crawler.negima
  (:require [net.cgrand.enlive-html :as e]))


(defn fetch
  []
  (e/html-resource (java.net.URL. "http://negima.wikia.com/wiki/Volumes_and_Chapters")))


(defn covers
  []
  (for [a (e/select (fetch) [:td :a])
        :let [href (get-in a [:attrs :href])]
        :when (re-find #"Negima" href)
        :let [vol (second (re-find #"Negima(\d+)" href))]
        :when vol]
    [(Integer/parseInt vol) href]))
