(ns duelyst-collection.core
  (:require [ajax.core :refer [GET]]
            [reagent.core :as r]
            [duelyst-collection.html :as html]
            [duelyst-collection.parse :as parse]))

(defn make-app-state []
  {:all-cards []
   :my-cards  []})

(defonce app-state
         (r/atom (make-app-state)))

(defn handle-my-cards [response]
  (swap! app-state assoc :my-cards (parse/parse-collection-csv response)))

(defn ^:export main []
  (GET "/all_cards.json" {:handler         #(swap! app-state assoc :all-cards %)
                          :response-format :json
                          :keywords?       true})

  (GET "/my_collection.csv" {:handler handle-my-cards})

  (r/render-component [html/render-app app-state]
                      (js/document.getElementById "app")))

