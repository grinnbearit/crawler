(ns crawler.fanfiction
  (:require [crawler.core :refer [fetch]]
            [net.cgrand.enlive-html :as e])
  (:use [hiccup.core :only [html]]))


(defn page
  ([id]
     (page id 1))
  ([id chapter]
     (format "https://www.fanfiction.net/s/%d/%d/" id chapter)))


(defn index
  [id]
  (for [option (-> (take 1 (e/select (fetch (page id)) [:#chap_select]))
                   (e/select [:option e/text-node]))
        :let [[_ title] (re-find #"\d+\. (.*)$" option)]]
    title))


(defn chapter
  [id chpt]
  (e/select (fetch (page id chpt)) [:#storytext :p e/text-node]))


(defn chapters
  [id]
  (for [[idx title] (map list (drop 1 (range)) (index id))]
    {:title title :content (chapter id idx)}))


(defn ->html
  [chpts]
  (html
   [:html
    (->> (map (fn [{:keys [title content]}]
                (conj (map #(vec [:p %]) content)
                      [:h1 title]))
              chpts)
         (apply concat)
         (cons :body)
         (vec))]))
