(ns duelyst-collection.core
  (:require [clojure.string :refer [lower-case]]
            [reagent.core :as r]
            [duelyst-collection.html :as html]
            [duelyst-collection.card-list :refer [all-cards-by-name]]
            [duelyst-collection.listlyst :refer [get-master-card-list]]))

(defn make-app-state []
  {:collection     []
   :listlyst-cards all-cards-by-name})

(defonce app-state
         (r/atom (make-app-state)))

(defn ^:export main []
  (get-master-card-list (fn [listlyst-cards]
                          (swap! app-state
                                 assoc
                                 :listlyst-cards
                                 (into {}
                                       (map (juxt #(-> %
                                                       :card/name
                                                       lower-case)
                                                  identity))
                                       (filter (fn [card]
                                                 (not= (card :card/rarity)
                                                       "Token"))
                                               listlyst-cards)))))

  (r/render-component [html/render-app app-state]
                      (js/document.getElementById "app")))

