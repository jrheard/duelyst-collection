(ns duelyst-collection.parse
  (:require
    [clojure.reader :refer [read-string]]
    [clojure.spec :as s]
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


(s/def ::card (s/keys :req [::count ::name ::faction ::rarity ::pristmatic ::cost ::de-value]))

(def csv-field-parsers
  [[:count js/parseInt]
   [:name str]
   [:faction str]
   [:rarity str]
   [:prismatic boolean]
   [:cost js/parseInt]
   [:de-value js/parseInt]])

(defn parse-csv-card-line [card-line]
  ;(js/console.log card-line)

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
                   (str $)
                   (split $ #",")
                   (map read-string $))
             ["Count", "Name", "Faction", "Rarity", "Prismatic", "Cost", "DE Value"])))

(defn parse-collection-csv [csv-collection-string]
  (let [csv-lines (as-> csv-collection-string $
                        (split $ #"\n")
                        (drop 3 $))
        _ (validate-csv-header (first csv-lines))
        card-lines (rest csv-lines)
        parsed-cards (map parse-csv-card-line card-lines)]
    parsed-cards))

