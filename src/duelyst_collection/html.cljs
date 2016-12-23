(ns duelyst-collection.html
  (:require [duelyst-collection.card-list :as card-list])
  )

(defn render-card-list [cards]
  (js/console.log cards)
  [:ul
   (for [card cards]
     ^{:key (card :card/id)} [:li (card :card/name)])])

(defn cards-you-own [app-state]
  (render-card-list (map :card/card (@app-state :my-cards))))

(defn all-cards []
  (render-card-list card-list/all-cards))

(defn missing-cards [app-state]
  ;xxxxxx
  (let [all-cards (@app-state :all-cards)
        my-cards (@app-state :my-cards)
        my-card-names (into #{} (map :name) my-cards)
        missing-cards (filter
                        (fn [a-card]
                          (not (contains? my-card-names (a-card :name))))
                        all-cards)]
    ;(js/console.log "foo")
    ;(js/console.log (count all-cards))
    ;(js/console.log (count my-cards))
    ;(js/console.log (clj->js (take 10 my-card-names)))
    #_(render-card-list missing-cards)))

(defn render-app [app-state]
  [:div
   [:h1 "your collection"]
   [cards-you-own app-state]
   [:h1 "all cards"]
   [all-cards]
   #_[:h1 "missing cards"]
   #_[missing-cards app-state]])

