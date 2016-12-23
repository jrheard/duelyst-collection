(ns duelyst-collection.html
  (:require [duelyst-collection.card-list :as card-list]
            [duelyst-collection.collection :as collection]))

(defn render-card-list [cards]
  [:ul
   (for [card cards]
     ^{:key (card :card/id)} [:li (card :card/name)])])

(defn cards-you-own [app-state]
  (render-card-list (map :card/card (@app-state :my-cards))))

(defn all-cards []
  (render-card-list card-list/all-cards))

(defn missing-cards [app-state]
  (let [missing (->> @app-state
                     :my-cards
                     collection/missing-cards
                     (sort-by :collection/count)
                     reverse)]

    [:ul
     (for [card missing]
       ^{:key (-> card :card/card :card/id)}
       [:li
        [:span.card-name (-> card :card/card :card/name)]
        [:span.missing-count (str " " (- 3 (card :collection/count)))]])]))

(defn render-app [app-state]
  [:div
   [:h1 "your collection"]
   [cards-you-own app-state]
   [:h1 "all cards"]
   [all-cards]
   [:h1 "missing cards (card name: number missing)"]
   [missing-cards app-state]])

