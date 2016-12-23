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

; note that this doesn't include:
; cost (to craft, in spirit)
; de value
; prismatic
(s/def :card/card (s/keys :req [:card/faction
                                :card/rarity
                                :card/id
                                :card/name
                                :card/cost
                                :card/type
                                :card/set]))

(s/def :collection/count nat-int?)
(s/def :collection/card (s/keys :req [:card/card :collection/count]))
