(ns crawler.michaelochurch
  (:require [net.cgrand.enlive-html :as e]
            [crawler.core :refer [fetch]])
  (:use [hiccup.core :only [html]]))


(def BASE "http://michaelochurch.wordpress.com")


(defn archives
  []
  (for [node (reverse (e/select (fetch BASE) [:aside#archives :ul :li :a]))]
    {:href (get-in node [:attrs :href])
     :title (apply str (get-in node [:content]))}))


(defn entries
  [page]
  (for [node (e/select (fetch page) [:h1.entry-title :a])]
    {:href (get-in node [:attrs :href])
     :title (apply str (get-in node [:content]))}))


(defn post
  [page]
  (let [nodes (fetch page)
        title (->> (e/select nodes [:h1.entry-title]) first :content (apply str))

        timestamp (-> (e/select nodes [:time.entry-date]) first :attrs :datetime)

        content (for [node (e/select nodes [:div.entry-content :p])
                      para (:content node)]
                  (if (string? para)
                    para
                    (apply str (e/emit* para))))

        comments (for [node (e/select nodes [:li.comment])
                       :let [author (-> (e/select node [:cite.fn]) first :content)
                             datetime (-> (e/select node [:time]) first :attrs :datetime)
                             content (-> (e/select node [:div.comment-content :p]))]]
                   {:author (if (string? (first author))
                              (apply str author)
                              (apply str (e/emit* author)))
                    :datetime (apply str datetime)
                    :content (for [para content]
                               (if (string? para)
                                 para
                                 (apply str (e/emit* para))))})]
    {:title title
     :timestamp timestamp
     :content content
     :comments comments}))


(defn scrape
  []
  (for [archive (archives)
        entry (entries (:href archive))
        :let [article (post (:href entry))]]
    article))


(defn ->html
  [articles]
  [:html
   [:head
    [:h1 "Michaeal O Church"]]
   [:body
    (for [article articles]
      [:div
       [:h2 (:title article) "-" (:timestamp article)]
       [:div
        (for [para (:content article)]
          [:p para])]])]])
