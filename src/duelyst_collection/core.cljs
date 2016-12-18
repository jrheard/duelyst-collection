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
  {:count     js/parseInt
   :prismatic boolean
   :cost      js/parseInt
   :de-value  js/parseInt})

(defn parse-card-csv-line [header-line card-line]
  (let [pairs (map vector
                   (split header-line #",")
                   (split card-line #","))]
    (into {}
          (map (fn [[name value]]
                 (let [parsed-name (-> name
                                       read-string
                                       lower-case
                                       (replace #" " "-")
                                       keyword)
                       parsed-value (read-string value)]

                   [parsed-name
                    (if (contains? csv-field-parsers parsed-name)
                      ((csv-field-parsers parsed-name) parsed-value)
                      parsed-value)])))
          pairs)))

(defn handle-my-cards [response]
  (let [csv-lines (as-> response $
                        (split $ #"\n")
                        (drop 3 $)
                        (take 10 $))
        header-line (first csv-lines)
        card-lines (rest csv-lines)
        parsed-cards (map #(parse-card-csv-line header-line %)
                          card-lines)]

    (js/console.log (take 10 parsed-cards))

    (swap! app-state assoc :my-cards parsed-cards)))
(defn foo []
  ;(js/console.log (clj->js (first (@app-state :all-cards))))
  [:ul
   (for [card (@app-state :all-cards)]
     ^{:key (card :id)} [:li (card :name)]
     )
   ]

  )

(defn ^:export main []
  (GET "/all_cards.json" {:handler         #(swap! app-state assoc :all-cards %)
                          :response-format :json
                          :keywords?       true})

  (GET "/my_collection.csv" {:handler handle-my-cards})

  (r/render-component [foo]
                      (js/document.getElementById "app")))

