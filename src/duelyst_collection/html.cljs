(ns duelyst-collection.html
  (:require [clojure.spec :as s]
            [duelyst-collection.collection :as collection]
            [duelyst-collection.specs :as specs]))

(defn format-percentage [percentage]
  (str (int percentage) "%"))

(defn render-card-list [cards]
  [:ul
   (for [card cards]
     ^{:key (card :card/id)} [:li (card :card/name)])])

(defn cards-you-own [cards]
  (render-card-list (map :card/card cards)))

(defn progress-bar [percentage]
  [:div.progress-bar
   [:progress {:value percentage :max 100}]
   [:p.percentage (format-percentage percentage)]])

(defn completion [cards]
  [:div.completion
   [:h1 "collection completion:"]
   [progress-bar (collection/card-completion-percentage cards)]
   [:h1 "dust completion:"]
   [progress-bar (collection/dust-completion-percentage cards)]

   (for [faction (s/form :card/faction)]
     (let [faction-cards (filter #(= (-> % :card/card :card/faction)
                                     faction)
                                 cards)]
       ^{:key faction}
       [:div.faction
        [:p faction]
        [progress-bar (collection/card-completion-percentage faction-cards)]]))])

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

