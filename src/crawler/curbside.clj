(ns crawler.curbside
  (:require [org.httpkit.client :as http]
            [cheshire.core :as json]
            [swissknife.collections :refer [queue]]
            [swissknife.core :refer [map-keys]]
            [clojure.string :as str]))


(defn get-session
  []
  (let [request (http/get "http://challenge.shopcurbside.com/get-session")]
    (:body @request)))


(defn get-message
  [session id]
  (let [request (http/get (str "http://challenge.shopcurbside.com/" id)
                          {:headers {"Session" session}})]
    (->> (json/parse-string (:body @request))
         (map-keys str/lower-case))))


(defn all-messages
  []
  (loop [session (get-session)
         remaining (queue "start")
         messages []]
    (if (empty? remaining)
      messages
      (let [next-id (peek remaining)
            message (get-message session next-id)]
        (cond (message "error")
              (recur (get-session) (pop remaining) messages)

              (message "next")
              (recur session
                     (into (pop remaining) (message "next"))
                     (conj messages (dissoc message "next")))

              :else
              (recur session
                     (pop remaining)
                     (conj messages message)))))))


(defn secrets
  [messages]
  (apply str (map #(% "secret") messages)))
