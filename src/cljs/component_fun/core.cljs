(ns component-fun.core
  (:require [reagent.core :as reagent :refer [atom]]
            [re-frame.core :as rf]))

(enable-console-print!)

#_(defonce app-state (atom {:text "Hello Chestnut!"}))

#_(defn greeting []
  [:h1 (:text @app-state)])

#_(reagent/render [greeting] (js/document.getElementById "app"))

(rf/reg-event-db
  :initialize
  (fn [_ _]
    {}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Collapsible Panel

(rf/reg-event-db
  :toggle-panel
  (fn [db [_ id]]
    (update-in db [:panels id] not)))

(rf/reg-sub
  :panel-state
  (fn [db [_ id]]
    (get-in db [:panels id])))

(defn example-component []
  (let [s (reagent/atom 0)]
    (js/setInterval #(swap! s inc) 1000)
    (fn []
      [:div @s])))

(defn panel [id title & children]
  (let [s (reagent/atom {:open false})]
    (fn [id title & children]
      (let [open? @(rf/subscribe [:panel-state id])
            child-height (:child-height @s)]
        [:div
         [:div {:on-click #(rf/dispatch [:toggle-panel id])
                :style {:background-color "#ddd"
                        :padding "0 1em"}}
          [:div {:style {:float "right"}}
           (if open? "-" "+")]
          title]
         [:div {:style  {:overflow "hidden"
                         :transition "max-height 0.8s"
                         :max-height (if open? child-height 0)}}
          [:div {:ref #(when %
                         (swap! s assoc :child-height (.-clientHeight %)))
                 :style {:background-color "#eee"
                         :padding "0 1em"}
                 }
           children]]]))))

;; End of Collapsible Panel component
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Progress Bar

(rf/reg-event-db
  :initialize
  (fn [_ _]
    {:done 0 :total 100}))

(rf/reg-event-db
  :set-total
  (fn [db [_ total]]
    (assoc db :total total :done 0)))

(rf/reg-event-db
  :inc-done
  (fn [db [_ done]]
    (if (>= (+ done (:done db)) (:total db))
      (assoc db :done (:total db))
      (update db :done + done))))

(rf/dispatch-sync [:set-total 100])

(defonce _interval (js/setInterval
                     #(rf/dispatch-sync [:inc-done 3])
                     1000))

(rf/reg-sub
  :total
  (fn [db] (:total db)))

(rf/reg-sub
  :done
  (fn [db] (:done db)))

(defn progress [done]
  (let [s (reagent/atom {})]
    (fn [done]
      (let [done (str (.toFixed (* 100 done) 1) "%")]
        [:div {:style {:position :relative
                       :line-height "1.3em"}}
         [:div {:style {:background-color :green
                        :top 0
                        :bottom 0
                        :transition "width 0.1s"
                        :width done
                        :position :absolute
                        :overflow :hidden}}
          [:span {:style {:margin-left (:left @s)
                          :color :white}}
           done]]
         [:div {:style {:text-align :center}}
          [:span
           {:ref #(if %
                    (swap! s assoc :left (.-offsetLeft %))
                    (swap! s assoc :left 0))}
           done]]]))))

;; End of Progress Bar
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn ui []
  [:div
   [panel :ex-1 "Example component" [example-component]]
   [progress (/ @(rf/subscribe [:done]) @(rf/subscribe [:total]))]])

(defonce _init (rf/dispatch-sync [:initialize]))
(reagent/render [ui] (js/document.getElementById "app"))
