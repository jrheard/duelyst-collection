(ns duelyst-collection.collection
  (:require [clojure.spec :as s]
            [duelyst-collection.specs]))

(defn missing-cards [cards]
  (filter #(< (% :collection/count) 3)
          cards))

(s/fdef missing-cards
  :args (s/cat :cards (s/coll-of :collection/card))
  :ret (s/coll-of :collection/card))

(defn completion-percentage [cards]
  (* 100
     (/ (apply + (map :collection/count cards))
        (* 3 (count cards)))))

(s/fdef completion-percentage
  :args (s/cat :cards (s/coll-of :collection/card))
  :ret number?)
