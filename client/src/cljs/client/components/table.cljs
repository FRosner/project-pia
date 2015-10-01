(ns client.components.table
  (:require
   [om.core :as om :include-macros true]
   [sablono.core :refer-macros [html]]
   [plumbing.core :as p :include-macros true]
   [client.api :as api]))


(defn request-button [predictions id api-ch]
  [:button
   {:class "btn btn-sm btn-primary"
    :on-click (fn [_]
                (om/update! predictions id :request-in-progress)
                (api/get-prediction api-ch id))}
   "Request"])

(p/defnk component [predictions [:channels api-ch]]
  (reify
    om/IDisplayName
    (display-name [_] "Table")
    om/IRender
    (render [_]
      (html
       [:div
        [:div.row
         [:div.col-lg-3]
         [:div.col-lg-6
          [:table.table.table-striped.table-hover
           [:thead
            [:th "ID"]
            [:th "Result"]
            [:th "Action"]]
           [:tbody
            (for [[id res] predictions]
              [:tr {:key id}
               [:td id]
               [:td
                (case res
                  nil ""
                  :error "Server error"
                  :pending "No result yet"
                  :request-in-progress ""
                  res)]
               [:td
                (case res
                  nil (request-button predictions id api-ch)
                  :pending (request-button predictions id api-ch)
                  :request-in-progress [:img {:src "/imgs/ajax-loader.gif"}]
                  "")]])]]]
         [:div.col-lg-3]]
        [:div.row
         [:div.col-lg-3]
         [:div.col-lg-6
          [:button
           {:class "btn btn-primary"
            :on-click #(api/get-predictions api-ch)}
           "List all"]]
         [:div.col-lg-3]]]))))
