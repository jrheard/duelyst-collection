(ns duelyst-collection.collection
  (:require [clojure.spec :as s]
            [clojure.string :refer [lower-case split]]
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

; map of
; card type ->
; [amount of spirit that this card progresses your collection by, disenchant value]
;
; if you open a prismatic kron and you only have 1 kron already, it advanced your collection
; by 900 spirit, because now you don't have to craft a kron in order to get a full collection.
; if you open a prismatic kron and you already have 3 krons, you can dust it for 900 spirit.
; if you open a regular kron and you already have 3 krons, you can dust it for 350 spirit.
(def card-rarity-values
  {:prismatic-legendary [900 900]
   :legendary           [900 350]
   :prismatic-epic      [350 350]
   :epic                [350 100]
   :prismatic-rare      [100 100]
   :rare                [100 20]
   :prismatic-common    [40 40]
   :common              [40 10]})

(def card-rarity-probabilities
  ; per https://www.reddit.com/r/duelyst/comments/4uiknr/postprismatics_orbs_content_preliminary_breakdown/
  {"Base"                 {:prismatic-legendary 0.0047
                           :legendary           0.0436
                           :prismatic-epic      0.007
                           :epic                0.0924
                           :prismatic-rare      0.0129
                           :rare                0.2401
                           :prismatic-common    0.0247
                           :common              0.5746}
   ; per https://www.reddit.com/r/duelyst/comments/50npsl/denizens_of_shimzar_orbs_content_preliminary/
   "Denizens of Shim'Zar" {:prismatic-legendary 0.0029
                           :legendary           0.0362
                           :prismatic-epic      0.0054
                           :epic                0.0871
                           :prismatic-rare      0.0115
                           :rare                0.2663
                           :prismatic-common    0.0177
                           :common              0.5730}})

(defn expected-value-of-a-card [card-set]
  (let [cards-by-rarity (reduce (fn [acc card]
                                  (let [rarity (-> card :card/card :card/rarity)]
                                    (if (contains? acc rarity)
                                      (update acc rarity conj card)
                                      (assoc acc rarity [card]))))
                                {}
                                card-set)

        percent-owned-by-rarity (into {}
                                      (for [[rarity cards] cards-by-rarity]
                                        [(keyword (lower-case rarity))
                                         (card-completion-percentage cards-by-rarity)]))

        chances-to-open-by-rarity (card-rarity-probabilities
                                    (-> card-set first :card/card :card/set))]

    (apply + (for [[rarity chance-to-open] chances-to-open-by-rarity]
               (let [[value-of-new value-of-dupe] (card-rarity-values rarity)
                     rarity-regardless-of-prismatic-status (if (>= (.indexOf (name rarity) "-") 0)
                                                             (-> rarity
                                                                 name
                                                                 (split #"-")
                                                                 second
                                                                 keyword)
                                                             rarity)

                     chance-of-dupe (percent-owned-by-rarity rarity-regardless-of-prismatic-status)
                     chance-of-new (- 1 chance-of-dupe)]

                 (* chance-to-open
                    (+ (* chance-of-new value-of-new)
                       (* chance-of-dupe value-of-dupe))))))))

(defn packs-to-complete [cards]
  ; all these cards should be from the same set.
  (assert (= (count (set (map #(-> % :card/card :card/set) cards)))
             1))

  (js/console.log (* (expected-value-of-a-card cards)
                5))

  (int (* (/ (dust-remaining cards)
             (* (expected-value-of-a-card cards)
                5))

          ; arbitrarily chosen fudge factor - as your collection becomes more complete,
          ; the value of a new orb diminishes.
          1.2)))

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
