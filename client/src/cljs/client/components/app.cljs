(ns client.components.app
  (:require
   [om.core :as om :include-macros true]
   [sablono.core :refer-macros [html]]
   [client.components.form :as form]
   [client.components.table :as table]
   [client.components.notifications :as notifications]))

(defn component [state owner]
  (reify
    om/IDisplayName
    (display-name [_] "App")
    om/IRender
    (render [_]
      (html
       [:div.container
        [:div.row
         [:div.col-lg-12
          [:ul.nav.nav-tabs
           {:role "tablist"}
           [:li
            {:role "presentation"
             :class "active"}
            [:a
             {:href "#form"
              :aria-controls "form"
              :role "tab"
              :data-toggle "tab"}
             "Form"]]
           [:li {:role "presentation"}
            [:a
             {:href "#predictions"
              :aria-controls "predictions"
              :role "tab"
              :data-toggle "tab"}
             "Predictions"]]]
          (om/build
           notifications/component
           state
           {:key [:components :notifications]})
          [:div.tab-content
           [:div
            {:role "tabpanel"
             :id "form"
             :class "tab-pane active"}
            (om/build form/component state {:key [:components :form]})]
           [:div
            {:role "tabpanel"
             :id "predictions"
             :class "tab-pane"}
            (om/build table/component state {:key :predictions})]]]]]))))
