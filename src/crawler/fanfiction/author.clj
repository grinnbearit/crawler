(ns crawler.fanfiction.author
  (:require [crawler.core :refer [fetch]]
            [clojure.string :as str]
            [net.cgrand.enlive-html :as e])
  (:import [java.util Date]))


(defn page
  [id]
  (format "https://www.fanfiction.net/u/%s/" id))


(defn author-name
  [root]
  (->> (e/select root [:div#content_wrapper_inner :span]) first :content
       (apply str) str/trim))


(defn- parse-date
  [txt]
  (Date. (Long/parseLong txt)))


(defn- parse-long
  [txt]
  (Long/parseLong (str/replace txt "," "")))


(defn story
  [node]
  (letfn [(extract-author [n]
            (first
             (for [anchor (e/select n [:a])
                   :let [href (get-in anchor [:attrs :href])]
                   :when (not= -1 (.indexOf href "/u/"))]
               (second (re-find #"/u/(\d+)/" href)))))

          (extract-categories [n]
            (-> (get-in n [:attrs :data-category])
                (str/split #" & ")))

          (complete? [n]
            (= "2" (get-in node [:attrs :data-status])))

          (extract-metadata [n]
            (let [blurb (->> (e/select node [:div.z-padtop2.xgray])
                             first :content (apply str))
                  [_ rating language _ genres] (re-find #"Rated: ([^\s]+) - ([^\s]+)( - ([^\s]+)?)? - Chapters" blurb)
                  [_ favs] (re-find #"Favs: ((\d|,)+)" blurb)
                  [_ follows] (re-find #"Follows: ((\d|,)+)" blurb)]
              {:rating rating
               :language language
               :genres (if genres (str/split genres #"/") [])
               :favourites (if favs (parse-long favs) 0)
               :follows (if follows (parse-long follows) 0)}))]

    (let [metadata (extract-metadata node)]
      {:id (get-in node [:attrs :data-storyid])
       :author (extract-author node)
       :categories (extract-categories node)
       :title (get-in node [:attrs :data-title])
       :word-count (parse-long (get-in node [:attrs :data-wordcount]))
       :date-submitted (parse-date (get-in node [:attrs :data-datesubmit]))
       :date-updated (parse-date (get-in node [:attrs :data-dateupdate]))
       :reviews (parse-long (get-in node [:attrs :data-ratingtimes]))
       :chapters (parse-long (get-in node [:attrs :data-chapters]))
       :complete? (complete? node)
       :rating (:rating metadata)
       :language (:language metadata)
       :genres (:genres metadata)
       :favourites (:favourites metadata)
       :follows (:follows metadata)})))


(defn author-stories
  [root]
  (->> (e/select root [:div.z-list.mystories])
       (map story)))


(defn favourite-stories
  [root]
  (->> (e/select root [:div.z-list.favstories])
       (map story)))


(defn favourite-authors
  [root]
  (for [anchor (e/select root [:div#fa :td :a])
        :let [href (get-in anchor [:attrs :href])]]
    (second (re-find #"/u/(\d+)/" href))))


(defn author
  [id]
  (let [root (fetch (page id))]
    {:id id
     :name (author-name root)
     :author-stories (->> (author-stories root)
                          (map #(assoc % :author id)))
     :favourite-stories (favourite-stories root)
     :favourite-authors (favourite-authors root)}))


(defn author-seq
  "lazy sequence of authors, populated by a depth first search starting with a seed list of ids"
  [seed-ids]
  (letfn [(fetch-authors [seen unseen]
            (lazy-seq
             (when (seq unseen)
               (let [id (first unseen)
                     auth (author id)
                     fav-auths (->> (:favourite-authors auth)
                                    (remove seen))
                     fav-story-auths (->> (:favourite-stories auth)
                                          (map :author)
                                          (remove :seen))]
                 (cons auth (fetch-authors (conj seen id) (->> (disj unseen id)
                                                               (into fav-auths)
                                                               (into fav-story-auths))))))))]
    (fetch-authors #{} (set seed-ids))))
