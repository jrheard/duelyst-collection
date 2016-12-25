(ns duelyst-collection.html
  (:require [clojure.spec :as s]
            [clojure.set :refer [difference]]
            [clojure.string :refer [split lower-case]]
            [duelyst-collection.collection :as collection]
            [duelyst-collection.parse :as parse]
            [duelyst-collection.specs :as specs]))

(defn format-percentage [percentage]
  (str (int percentage) "%"))

(defn render-card-list [cards]
  [:ul
   (for [card cards]
     ^{:key (card :card/id)} [:li (card :card/name)])])

(defn missing-cards [cards]
  (let [missing (->> cards
                     collection/missing-cards
                     (sort-by #(-> % :card/card :card/cost)))]

    [:ul.missing-cards
     (for [card missing]
       ^{:key (-> card :card/card :card/id)}
       [:li
        [:span.mana-cost
         (str "("
              (-> card :card/card :card/cost)
              ") ")]
        [:span.set-name
         (str "["
              (-> card :card/card :card/set first)
              "] ")]
        [:a.card-name
         {:class  (-> card :card/card :card/rarity lower-case)
          :href   (str "http://kit.listlyst.com/database/cards/"
                       (-> card :card/card :card/id))
          :target "_blank"}
         (-> card :card/card :card/name)]
        [:span.missing-count (str ": " (- 3 (card :collection/count)))]])]))

(defn progress-bar [percentage]
  [:div.progress-bar
   [:progress {:value percentage :max 100}]
   [:p.percentage (format-percentage percentage)]])

(defn completion-progress-bars [cards]
  [:div.completion-bars
   [:div.completion-bar
    [:h2 "Card-based completion"]
    [progress-bar (collection/card-completion-percentage cards)]]
   [:div.completion-bar
    [:h2 "Spirit-based completion"]
    [progress-bar (collection/dust-completion-percentage cards)]]])

(defn overall-completion [cards]
  [:div.overall-completion.section
   [:h1 "Overall"]
   [completion-progress-bars cards]
   [:div.legend
    [:p "Missing cards look like this:"]
    [:p "(mana cost) [card set] card name: number of cards missing"]
    [:p "Card set can be [B] for Base, [R] for Rise of the Bloodborn, or [D] for Denizens of Shim'Zar."]]])

(defn faction-completion [faction cards]
  [:div.faction-completion.section {:class (-> faction
                                               (split #" ")
                                               first
                                               lower-case)}
   [:h1 faction]
   [completion-progress-bars cards]
   [missing-cards cards]])

(defn render-collection [cards]
  [:div
   [overall-completion cards]

   (for [faction (-> :card/faction
                     s/form
                     (difference #{"Neutral"})
                     sort)]
     (let [faction-cards (filter #(= (-> % :card/card :card/faction)
                                     faction)
                                 cards)]
       ^{:key faction} [faction-completion faction faction-cards]))

   [faction-completion
    "Neutral"
    (filter #(= (-> % :card/card :card/faction) "Neutral") cards)]])

(defn get-file-contents [file-input app-state]
  (let [file (aget (.-files file-input)
                   0)
        reader (js/FileReader.)]
    (.addEventListener reader
                       "loadend"
                       #(swap! app-state assoc :collection (parse/parse-collection-csv (.-result reader))))
    (.readAsText reader file)))

(defn initial-ui [app-state]
  [:div.initial-ui
   [:p "hello!"]

   [:input {:type      "file"
            :on-change (fn [e]
                         (get-file-contents (.-target e) app-state))}]])

(defn render-app [app-state]
  (let [cards (@app-state :collection)]
    (if (seq cards)
      [render-collection cards]
      [initial-ui app-state])))

