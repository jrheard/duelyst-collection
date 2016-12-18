(ns duelyst-collection.core
  (:require [ajax.core :refer [GET]]
            [reagent.core :as r]))

(defn make-app-state []
  {:all-cards []})

(defonce app-state (r/atom (make-app-state)))

(defn foo []
  (js/console.log (first (@app-state :all-cards)))
  [:ul
   (for [card (@app-state :all-cards)]
     ^{:key (card :id)} [:li (card :name)]
     )
   ]



  )

(defn ^:export main []
  (GET "/all_cards.json" {:handler #(swap! app-state assoc :all-cards %)
                          :response-format :json
                          :keywords? true})

  (r/render-component [foo]
                      (js/document.getElementById "app")))

