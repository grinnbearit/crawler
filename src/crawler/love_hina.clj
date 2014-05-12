(ns crawler.love-hina
  (:require [net.cgrand.enlive-html :as e]))


(defn fetch
  [volume]
  (->> (format "http://lovehina.wikia.com/wiki/Love_Hina_(manga)_Volume_%d" volume)
       (java.net.URL.)
       (e/html-resource)))


(defn cover
  [volume]
  (let [[img] (e/select (fetch volume) [:figure :a :img])]
    (get-in img [:attrs :src])))
