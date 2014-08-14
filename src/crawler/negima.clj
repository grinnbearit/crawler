(ns crawler.negima
  (:require [crawler.core :refer [fetch]]
            [net.cgrand.enlive-html :as e]))


(def BASE "http://negima.wikia.com/wiki/Volumes_and_Chapters")


(defn covers
  []
  (for [a (e/select (fetch BASE) [:td :a])
        :let [href (get-in a [:attrs :href])]
        :when (re-find #"Negima" href)
        :let [vol (second (re-find #"Negima(\d+)" href))]
        :when vol]
    [(Integer/parseInt vol) href]))
