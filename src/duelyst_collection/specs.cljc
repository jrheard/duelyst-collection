(ns duelyst-collection.specs
  (:require [clojure.spec :as s]))

(s/def :card/faction #{"Magmar Aspects"
                       "Songhai Empire"
                       "Lyonar Kingdoms"
                       "Vanar Kindred"
                       "Neutral"
                       "Abyssian Host"
                       "Vetruvian Imperium"})

(s/def :card/rarity #{"Common" "Legendary" "Epic" "Rare" "Basic" "Token"})

(s/def :card/type #{"Unit" "Spell" "Artifact"})

(s/def :card/set #{"Base" "Rise of the Bloodborn" "Denizens of Shim'Zar"})

(s/def :card/id nat-int?)
(s/def :card/name string?)
(s/def :card/cost nat-int?)

(s/def :card/card (s/keys :req [:card/faction
                                :card/rarity
                                :card/id
                                :card/name
                                :card/cost
                                :card/type
                                :card/set]))

(s/def :collection/count nat-int?)
; TODO - consider having :collection/prismatic-count and :collection/regular-count
(s/def :collection/spirit-cost nat-int?)
(s/def :collection/card (s/keys :req [:card/card :collection/count :collection/spirit-cost]))
