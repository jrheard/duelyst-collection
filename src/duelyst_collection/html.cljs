(ns duelyst-collection.html
  (:require [ajax.core :refer [GET]]
            [clojure.spec :as s]
            [clojure.set :refer [difference]]
            [clojure.string :refer [split lower-case]]
            [duelyst-collection.collection :as collection]
            [duelyst-collection.parse :as parse]
            [duelyst-collection.specs :as specs]))

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
   [:p.percentage (str (int percentage) "%")]])

(defn completion-progress-bars [cards]
  [:div.completion-bars
   [:div.completion-bar
    [:h2 "Completion (in cards):"]
    [progress-bar (collection/card-completion-percentage cards)]]
   [:div.completion-bar
    [:h2 "Completion (in spirit):"]
    [progress-bar (collection/dust-completion-percentage cards)]]])

(defn set-completion [cards]
  [:div.set-completion
   (for [a-set (sort (s/form :card/set))]
     (let [set-cards (filter (fn [card]
                               (= (-> card :card/card :card/set)
                                  a-set))
                             cards)]
       ^{:key a-set}
       [:div.a-set
        [:h1 a-set]

        (let [remaining (collection/dust-remaining set-cards)]
          (when (and (> remaining 0)
                     (#{"Base" "Denizens of Shim'Zar"} a-set))
            ; got these figures from thunder-god on discord
            (let [spirit-per-pack (if (= a-set "Base")
                                    220
                                    200)]
              [:p (str "It will cost "
                       (int remaining)
                       " spirit to complete this set. That's about "
                       (int (/ remaining spirit-per-pack))
                       " packs.")])))

        [completion-progress-bars set-cards]]))])

(defn overall-completion [cards]
  [:div.overall-completion.section
   [set-completion cards]
   [:div.legend
    [:p "The following sections on page will show you which cards you're missing for each faction."]
    [:p "Missing cards are formatted like this:"]
    [:strong "(mana cost) [card set] card name: number of cards missing"]
    [:p "[card set] can be [B] for Base, [R] for Rise of the Bloodborn, or [D] for Denizens of Shim'Zar."]]])

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
                       #(swap! app-state
                               assoc
                               :collection
                               (parse/parse-collection-csv (.-result reader)
                                                           (@app-state :listlyst-cards))))
    (.readAsText reader file)))

(defn initial-ui [app-state]
  [:div.initial-ui
   [:h1 "Duelyst Collection Completion Tool"]

   [:p
    "I really like playing "
    [:a {:href "http://www.duelyst.com"} "Duelyst"]
    ", but one thing that bugs me about the game's UI is that it's really difficult to tell how many cards I've got."]
   [:p "I can't easily tell which factions I've got the most cards for, how many more battle pets I've got to unlock,
    and most importantly how long it's going to be until I've got enough legendaries that I can start netdecking like a pro."]

   [:p "I built this little tool to answer all those questions. It takes a CSV file generated by "
    [:a {:href "https://duelyststats.info/scripts/scriptlist.html#scriptlist-collection-enhancements"} "this script's"]
    " collection export feature, and it shows you a bunch of progress bars and tells you which cards you're missing for each faction."]

   [:p "To use it, just click this button and select your collection's exported CSV file (go generate one if you haven't already):"]

   [:input {:type      "file"
            :on-change (fn [e]
                         (get-file-contents (.-target e) app-state))}]

   [:p.demo "If you don't have a CSV file and want to see what the tool looks like, "]

   [:input.demo {:type     "button"
                 :on-click #(GET "/my_collection_2.csv"
                                 {:handler (fn [result]
                                             (swap! app-state
                                                    assoc
                                                    :collection
                                                    (parse/parse-collection-csv result
                                                                                (@app-state :listlyst-cards))))})
                 :value    "click here to see it operate on my collection."}]

   [:p "If this tool breaks, falls out of date, or seems like it's doing something wrong, open an issue on the"
    [:a {:href "https://github.com/jrheard/duelyst-collection"} "GitHub repo"]
    " or hit me up on "
    [:a {:href "https://twitter.com/jrheard"} "Twitter"]
    "."]])

(defn render-app [app-state]
  (let [cards (@app-state :collection)]
    (if (seq cards)
      [render-collection cards]
      [initial-ui app-state])))

