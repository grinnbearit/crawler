(ns crawler.fanfiction.author-test
  (:use [midje.sweet]
        [crawler.fanfiction.author]))


(facts
 (author-seq ["1"])
 => [{:id "1"
      :favourite-authors ["2"]
      :favourite-stories [{:author "3"}]}
     {:id "3"
      :favourite-authors ["1"]
      :favourite-stories [{:author "2"}]}
     {:id "2"
      :favourite-authors ["3"]
      :favourite-stories [{:author "1"}]}]

 (provided
  (author "1")
  => {:id "1"
      :favourite-authors ["2"]
      :favourite-stories [{:author "3"}]}

  (author "2")
  => {:id "2"
      :favourite-authors ["3"]
      :favourite-stories [{:author "1"}]}

  (author "3")
  => {:id "3"
      :favourite-authors ["1"]
      :favourite-stories [{:author "2"}]}))
