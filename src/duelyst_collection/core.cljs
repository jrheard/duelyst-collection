(ns duelyst-collection.core
  (:require [clojure.string :refer [lower-case]]
            [ajax.core :refer [GET]]
            [cemerick.url :as url]
            [reagent.core :as r]
            [duelyst-collection.card-list :refer [all-cards-by-name]]
            [duelyst-collection.html :as html]
            [duelyst-collection.listlyst :refer [get-master-card-list]]
            [duelyst-collection.parse :as parse]))

(defn make-app-state []
  {:collection     []
   :listlyst-cards all-cards-by-name})

(defonce app-state
         (r/atom (make-app-state)))

(defn use-demo-collection []
  (GET "/my_collection_2.csv"
       {:handler (fn [result]
                   (swap! app-state
                          assoc
                          :collection
                          (parse/parse-collection-csv result
                                                      (@app-state :listlyst-cards))))}))

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

  (let [query-params (:query (url/url (-> js/window .-location .-href)))]
    (if (contains? query-params "demo")
      (use-demo-collection)))

  (r/render-component [html/render-app app-state use-demo-collection]
                      (js/document.getElementById "app")))
