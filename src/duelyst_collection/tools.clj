(ns duelyst-collection.tools
  (:require [clj-http.client :as client]
            [cheshire.core :refer [parse-string]]
            [clojure.spec :as s]
            [duelyst-collection.specs]))

; i don't usually check API keys into version control, but it doesn't seem worth the trouble
; of doing anything fancier than this here - what do i care if someone steals my listlyst API key?
(def all-cards-url "http://listlyst.com/api/v1/cards?apikey=6bf31f13f655df3d8626c601a919f925")

(defn get-raw-cards []
  (-> all-cards-url
      client/get
      :body
      parse-string))

(defn parse-card [raw-card]
  {:card/faction (raw-card "faction")
   :card/rarity  (raw-card "rarity")
   :card/id      (raw-card "id")
   :card/name    (raw-card "name")
   :card/cost    (raw-card "manaCost")
   :card/type    (raw-card "type")
   :card/set     (raw-card "set")})

(s/fdef parse-card
  :args (s/cat :raw-card map?)
  :ret :card/card)

(comment
  (let [parsed (map parse-card (get-raw-cards))]
    (spit "foo.cljs" (pr-str parsed))))


