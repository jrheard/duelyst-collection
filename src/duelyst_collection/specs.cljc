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


; so that's a card, that makes sense
; and then there's a :collection/card
; and it's got a :card/card and a :collection/count
; that sounds good to me
; composition yo
