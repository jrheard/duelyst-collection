(ns duelyst-collection.collection
  (:require [clojure.spec :as s]
            [duelyst-collection.specs]))

(defn missing-cards [cards]
  (filter #(< (% :collection/count) 3)
          cards))

(s/fdef missing-cards
  :args (s/cat :cards (s/coll-of :collection/card))
  :ret (s/coll-of :collection/card))

(defn card-completion-percentage [cards]
  (* 100
     (/ (apply + (map :collection/count cards))
        (* 3 (count cards)))))

(s/fdef card-completion-percentage
  :args (s/cat :cards (s/coll-of :collection/card))
  :ret number?)

(defn dust-remaining [cards]
  (- (* 3 (apply + (map :collection/spirit-cost cards)))
     (apply + (map (fn [card]
                     (* (card :collection/count)
                        (card :collection/spirit-cost)))
                   cards))))

(defn dust-completion-percentage [cards]
  (* 100
     (/ (apply + (map (fn [card]
                        (* (card :collection/count)
                           (card :collection/spirit-cost)))
                      cards))
        (* 3 (apply + (map :collection/spirit-cost cards))))))

(s/fdef dust-completion-percentage
  :args (s/cat :cards (s/coll-of :collection/card))
  :ret number?)
