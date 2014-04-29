(ns crawler.tripletriad
  (:require [net.cgrand.enlive-html :as e]))


(defn fetch-cards
  []
  (e/html-resource (java.net.URL. "http://www.galbadiax.com/ff8/card-list.php")))


(defn fetch-images
  []
  (e/html-resource (java.net.URL. "http://finalfantasy.wikia.com/wiki/List_of_Triple_Triad_Cards")))


(defn parse-score
  [txt]
  (if (= "A" txt) 10 (Integer/parseInt txt)))


(defn parse-element
  [txt]
  (case txt
    "Poison" :poison
    "Ice" :ice
    "Wind" :wind
    "Fire" :fire
    "Thunder" :thunder
    "Water" :water
    "Earth" :earth
    "Holy" :holy
    nil))


(defn cards
  []
  (for [row (drop 1 (e/select (fetch-cards) [:tr]))
        :let [[[lvl] [name] [top] [bottom] [left] [right] [element] [location]]
              (map :content (:content row))]]
    {:level (Integer/parseInt lvl)
     :name name
     :top (parse-score top)
     :bottom (parse-score bottom)
     :left (parse-score left)
     :right (parse-score right)
     :element (parse-element element)
     :location location}))


(defn images
  []
  (for [tag (e/select (fetch-images) [:table :img])
        :let [url (get-in tag [:attrs :src])
              alt (get-in tag [:attrs :alt])
              [_ name] (re-find #"TT(.*)$" alt)]
        :when (not (re-find #"^data:" url))]
    {:name name
     :url url}))
