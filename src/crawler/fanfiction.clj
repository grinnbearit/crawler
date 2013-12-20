(ns crawler.fanfiction
  (:require [net.cgrand.enlive-html :as e]
            [clojure.string :as str]))


(defn fetch
  ([id]
     (fetch id 1))
  ([id chapter]
     (e/html-resource (java.net.URL. (format "https://www.fanfiction.net/s/%d/%d/" id chapter)))))


(defn index
  [id]
  (for [option (-> (take 1 (e/select (fetch id) [:#chap_select]))
                   (e/select [:option e/text-node]))
        :let [[_ title] (re-find #"\d+\. (.*)$" option)]]
    title))


(defn chapter
  [id chpt]
  (str/join "\n\n" (e/select (fetch id chpt) [:#storytext :p e/text-node])))


(defn chapters
  [id]
  (for [x (range 1 (inc (count (index id))))]
    (chapter id x)))
