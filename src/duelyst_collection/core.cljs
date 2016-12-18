(ns duelyst-collection.core
  (:require [reagent.core :as r]))

(defonce app-state (atom {:text "Hello world!"}))

(defn foo []
  [:h2 "bar"]


  )

(defn ^:export main []
  (r/render-component [foo]
                      (js/document.getElementById "app")))

