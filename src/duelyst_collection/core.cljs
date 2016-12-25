(ns duelyst-collection.core
  (:require [reagent.core :as r]
            [duelyst-collection.html :as html]
            [duelyst-collection.parse :as parse]))

(defn make-app-state []
  {:collection []})

(defonce app-state
         (r/atom (make-app-state)))

(defn ^:export main []
  (r/render-component [html/render-app app-state]
                      (js/document.getElementById "app")))

