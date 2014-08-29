(ns crawler.fanfiction
  (:require [crawler.core :refer [fetch]]
            [net.cgrand.enlive-html :as e]
            [clojure.string :as str])
  (:use [hiccup.core :only [html]])
  (:import [java.util Date]))


(defn story-page
  ([id]
     (story-page id 1))
  ([id chapter]
     (format "https://www.fanfiction.net/s/%d/%d/" id chapter)))


(defn author-page
  [id]
  (format "https://www.fanfiction.net/u/%d/" id))


(defn index
  [id]
  (for [option (-> (take 1 (e/select (fetch (story-page id)) [:#chap_select]))
                   (e/select [:option e/text-node]))
        :let [[_ title] (re-find #"\d+\. (.*)$" option)]]
    title))


(defn chapter
  [id chpt]
  (e/select (fetch (story-page id chpt)) [:#storytext :p e/text-node]))


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


;;; metafic


(defn author
  [id]
  (letfn [(parse-date [txt]
            (Date. (Long/parseLong txt)))

          (extract-long [pattern txt]
            (-> (re-find pattern txt)
                (second)
                (str/replace "," "")
                (Long/parseLong)))

          (story [node]
            (let [blurb (->> (e/select node [:div.z-padtop2.xgray]) first :content (apply str))
                  favourites (extract-long #"Favs: ((\d|,)+) -" blurb)
                  follows (extract-long #"Follows: ((\d|,)+) -" blurb)
                  [_ rating language _ genres] (re-find #"Rated: (.+?) - (.+?) (- (.+?) )?- Chapters:" blurb)]
              {:id (get-in node [:attrs :data-storyid])
               :author (or (some #(when-let [[_ u] (re-find #"/u/(\d+)/" (get-in % [:attrs :href]))] u)
                                 (e/select node [:a]))
                           id)
               :category (get-in node [:attrs :data-category])
               :title (get-in node [:attrs :data-title])
               :word-count (Long/parseLong (get-in node [:attrs :data-wordcount]))
               :date-submitted (parse-date (get-in node [:attrs :data-datesubmit]))
               :date-updated (parse-date (get-in node [:attrs :data-dateupdate]))
               :reviews (Long/parseLong (get-in node [:attrs :data-ratingtimes]))
               :chapters (Long/parseLong (get-in node [:attrs :data-chapters]))
               :complete? (= (get-in node [:attrs :data-status]) "2")
               :follows follows
               :favourites favourites
               :rating rating
               :language language
               :genres (if genres (str/split genres #"/") [])}))

          (author [node]
            {:id (second (re-find #"/(\d+)/" (get-in node [:attrs :href])))
             :name (apply str (:content node))})]

    (let [page (fetch (author-page id))
          name (->> (e/select page [:div#content_wrapper_inner :span]) first :content (apply str) str/trim)
          author-stories (map story (e/select page [:div.z-list.mystories]))
          favourite-stories (map story (e/select page [:div.z-list.favstories]))
          favourite-authors (map author (e/select page [:div#fa :td :a]))]

      {:id id
       :name name
       :stories author-stories
       :favourite-stories favourite-stories
       :favourite-authors favourite-authors})))
