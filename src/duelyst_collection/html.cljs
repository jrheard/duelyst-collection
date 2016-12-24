(ns duelyst-collection.html
  (:require [clojure.spec :as s]
            [clojure.set :refer [difference]]
            [clojure.string :refer [split lower-case]]
            [duelyst-collection.collection :as collection]
            [duelyst-collection.specs :as specs]))

(defn format-percentage [percentage]
  (str (int percentage) "%"))

(defn render-card-list [cards]
  [:ul
   (for [card cards]
     ^{:key (card :card/id)} [:li (card :card/name)])])

(defn progress-bar [percentage]
  [:div.progress-bar
   [:progress {:value percentage :max 100}]
   [:p.percentage (format-percentage percentage)]])

(defn completion-progress-bars [cards]
  [:div.completion-bars
   [:div.completion-bar
    [:h2 "Card-based completion"]
    [progress-bar (collection/card-completion-percentage cards)]]
   [:div.completion-bar
    [:h2 "Spirit-based completion"]
    [progress-bar (collection/dust-completion-percentage cards)]]])

(defn overall-completion [cards]
  [:div.overall-completion.section
   [:h1 "Overall"]
   [completion-progress-bars cards]])

(defn faction-completion [faction cards]
  [:div.faction-completion.section {:class (-> faction
                                       (split #" ")
                                       first
                                       lower-case)}
   [:h1 faction]
   [completion-progress-bars cards]])

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
     [overall-completion cards]

     (for [faction (-> :card/faction
                       s/form
                       (difference #{"Neutral"})
                       sort)]
       (let [faction-cards (filter #(= (-> % :card/card :card/faction)
                                       faction)
                                   cards)]
         ^{:key faction} [faction-completion faction faction-cards]))

     [faction-completion
      "Neutral"
      (filter #(= (-> % :card/card :card/faction) "Neutral") cards)]

     [:h1 "missing cards (card name: number missing)"]
     #_[missing-cards cards]]))

