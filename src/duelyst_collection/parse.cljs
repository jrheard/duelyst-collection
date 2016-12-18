(ns duelyst-collection.parse
  (:require
    [clojure.reader :refer [read-string]]
    [clojure.string :refer [split lower-case replace]]))

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

(defn parse-collection-csv [csv-collection-string]
  (let [csv-lines (as-> csv-collection-string $
                        (split $ #"\n")
                        (drop 3 $))
        _ (validate-csv-header (first csv-lines))
        card-lines (rest csv-lines)
        parsed-cards (map parse-csv-card-line card-lines)]
    parsed-cards))

