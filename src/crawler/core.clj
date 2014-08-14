(ns crawler.core
  (:require [net.cgrand.enlive-html :refer [html-resource]]))


(defn fetch
  [url]
  (html-resource (java.net.URL. url)))
