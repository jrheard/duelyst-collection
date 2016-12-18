(ns duelyst-collection.html

  )

(defn render-card-list [cards]
  [:ul
   (for [card cards]
     ; XXX not all cards have an id, sometimes cards have the same name (fix?)
     ^{:key (card :id)} [:li (card :name)])])

(defn cards-you-own [app-state]
  (render-card-list (@app-state :my-cards)))

(defn all-cards [app-state]
  (render-card-list (@app-state :all-cards)))

(defn missing-cards [app-state]
  (let [all-cards (@app-state :all-cards)
        my-cards (@app-state :my-cards)
        my-card-names (into #{} (map :name) my-cards)
        missing-cards (filter
                        (fn [a-card]
                          (not (contains? my-card-names (a-card :name))))
                        all-cards)]
    (js/console.log "foo")
    (js/console.log (count all-cards))
    (js/console.log (count my-cards))
    (js/console.log (clj->js (take 10 my-card-names)))
    (render-card-list missing-cards)))

(defn render-app [app-state]
  [:div
   [:h1 "your collection"]
   [cards-you-own app-state]
   [:h1 "all cards"]
   [all-cards app-state]
   [:h1 "missing cards"]
   [missing-cards app-state]])

