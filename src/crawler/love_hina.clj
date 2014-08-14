(ns crawler.love-hina
  (:require [crawler.core :refer [fetch]]
            [net.cgrand.enlive-html :as e]))


(defn page
  [volume]
  (format "http://lovehina.wikia.com/wiki/Love_Hina_(manga)_Volume_%d" volume))


(defn cover
  [volume]
  (let [[img] (e/select (fetch (page volume)) [:figure :a :img])]
    (get-in img [:attrs :src])))
