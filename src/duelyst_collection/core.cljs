(ns duelyst-collection.core
  (:require [ajax.core :refer [GET]]
            [clojure.reader :refer [read-string]]
            [clojure.string :refer [split lower-case replace]]
            [reagent.core :as r]))

(defn make-app-state []
  {:all-cards []
   :my-cards  []})

; here's an example card:
; {
; "description": "Turn a 2x2 area into Shadow Creep.",
; "faction": "Abyssian Host",
; "race": "Spell",
; "name": "Shadow Nova",
; "lastModifed": 1.71,
; "type": "Spell",
; "created": 0.01,
; "manaCost": 4,
; "id": 20051,
; "set": "Base",
; "rarity": "Basic"
; }

(defonce app-state (r/atom (make-app-state)))

(def csv-field-parsers
  [[:count js/parseInt]
   [:name str]
   [:faction str]
   [:rarity str]
   [:prismatic boolean]
   [:cost js/parseInt]
   [:de-value js/parseInt]])

(defn parse-csv-card-line [card-line]
  (js/console.log card-line)

  (let [fields (-> card-line
                   (subs 1 (- (count card-line)
                              2))
                   (split #"\",\""))]
    (into {}
          (map (fn [[[name parse-fn] value]]
                 [name (parse-fn value)])

               (map vector csv-field-parsers fields)))))

(defn validate-csv-header [header-line]
  (assert (= (as-> header-line $
                   (split $ #",")
                   (map read-string $)
                   (map lower-case $)
                   (map #(replace % #" " "-") $)
                   (map keyword $))
             [:count :name :faction :rarity :prismatic :cost :de-value])))

(defn handle-my-cards [response]
  (let [csv-lines (as-> response $
                        (split $ #"\n")
                        (drop 3 $))
        _ (validate-csv-header (first csv-lines))
        card-lines (rest csv-lines)
        parsed-cards (map parse-csv-card-line card-lines)]

    (swap! app-state assoc :my-cards parsed-cards)))

; XXXX no standardized card format yet
(defn render-card-list [cards]
  [:ul
   (for [card cards]
     ^{:key (card :id)} [:li (card :name)])])

(defn cards-you-own []
  (render-card-list (@app-state :my-cards)))

(defn all-cards []
  (render-card-list (@app-state :all-cards)))

(defn missing-cards []
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

(defn render-app []
  [:div
   [:h1 "your collection"]
   [cards-you-own]
   [:h1 "all cards"]
   [all-cards]
   [:h1 "missing cards"]
   [missing-cards]])

(defn ^:export main []
  (GET "/all_cards.json" {:handler         #(swap! app-state assoc :all-cards %)
                          :response-format :json
                          :keywords?       true})

  (GET "/my_collection.csv" {:handler handle-my-cards})

  (r/render-component [render-app]
                      (js/document.getElementById "app")))

