(ns duelyst-collection.html
  (:require [clojure.spec :as s]
            [duelyst-collection.card-list :as card-list]
            [duelyst-collection.collection :as collection]
            [duelyst-collection.specs :as specs]))

(defn format-percentage [percentage]
  (int (* percentage 100)))

(defn render-card-list [cards]
  [:ul
   (for [card cards]
     ^{:key (card :card/id)} [:li (card :card/name)])])

(defn cards-you-own [cards]
  (render-card-list (map :card/card cards)))

(defn completion [cards]
  (js/console.log (s/form :card/faction))
  [:div.completion
   [:h1 "collection completion:"]
   [:p (str "overall: "
            (format-percentage (collection/completion-percentage cards))
            "%")]
   (for [faction (s/form :card/faction)]
     (let [faction-cards (filter #(= (-> % :card/card :card/faction)
                                     faction)
                                 cards)]

       ^{:key faction}
       [:p (str faction
                ": "
                (format-percentage
                  (/ (apply + (map :collection/count faction-cards))
                     (* 3 (count faction-cards))))
                "%")]))])

(defn missing-cards [cards]
  (let [missing (->> cards
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
  (let [cards (@app-state :my-cards)]
    [:div
     [completion cards]
     [:h1 "your collection"]
     [cards-you-own cards]
     [:h1 "missing cards (card name: number missing)"]
     [missing-cards cards]]))

