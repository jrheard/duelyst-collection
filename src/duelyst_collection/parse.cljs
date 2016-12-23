(ns duelyst-collection.parse
  (:require
    [clojure.reader :refer [read-string]]
    [clojure.spec :as s]
    [clojure.string :refer [split lower-case replace]]
    [duelyst-collection.card-list :refer [all-cards-by-name]]
    [duelyst-collection.specs]))

; here's an example card csv line:
; headers ["Count", "Name", "Faction", "Rarity", "Prismatic", "Cost", "DE Value"]
; "0","Snow Rippler","Vanar","Common","false","40","10"
; note that set isn't included

(s/def :csv-dump/count nat-int?)
(s/def :csv-dump/name string?)
(s/def :csv-dump/card (s/keys :req [:csv-dump/count :csv-dump/name]))

(def csv-field-parsers
  [[:count js/parseInt]
   [:name str]
   [:faction str]
   [:rarity str]
   [:prismatic #(= % "true")]
   [:cost js/parseInt]
   [:de-value js/parseInt]])

(defn parse-csv-card-line
  "Takes a line from the CSV dump and turns it into a map."
  [card-line]
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
        parsed-cards (map parse-csv-card-line card-lines)
        card-pairs (partition 2 parsed-cards)]

    (map (fn [[nonprismatic-card prismatic-card]]
           (let [master-card (get all-cards-by-name
                                  (lower-case (nonprismatic-card :name)))]

             (assert (not (nil? master-card)))

             {:card/card        master-card
              :collection/count (+ (nonprismatic-card :count)
                                   (prismatic-card :count))}))
         card-pairs)))

(s/fdef parse-collection-csv
  :args (s/cat :csv-collection-string string?)
  :ret (s/coll-of :collection/card))

(comment
  )
