(ns duelyst-collection.listlyst
  (:require [ajax.core :refer [GET]]
            [clojure.spec :as s]
            [duelyst-collection.specs]))

; i don't usually check API keys into version control, but it doesn't seem worth the trouble
; of doing anything fancier than this here - what do i care if someone steals my listlyst API key?
(def all-cards-url "http://listlyst.com/api/v1/cards?apikey=6bf31f13f655df3d8626c601a919f925")

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

(defn get-master-card-list [callback-fn]
  (GET all-cards-url
       {:handler         (fn [result]
                           (let [parsed-cards (map parse-card result)]
                             (if (s/valid? (s/coll-of :card/card) parsed-cards)
                               (callback-fn parsed-cards)
                               (js/console.log "Listlyst API returned cards in a format we didn't recognize, falling back to cached card database"))))
        :error-handler   (fn [{:keys [status status-text failure]}]
                           (js/console.log "Error talking to the Listlyst API, falling back to cached card database:" status status-text failure))
        :response-format :json}))
