(ns client.components.form
  (:require
   [om.core :as om :include-macros true]
   [sablono.core :refer-macros [html]]
   [plumbing.core :as p :include-macros true]
   [client.state :as cs]
   [client.api :as api]))

(defn handle-change [e data edit-key cast-f]
  (om/update! data edit-key (cast-f (.. e -target -value))))

(defn handle-float [e data edit-key]
  (handle-change e data edit-key js/parseFloat))

(p/defnk component [[:components form] [:channels api-ch]]
  (reify
    om/IDisplayName
    (display-name [_] "Form")
    om/IRender
    (render [_]
      (html
       [:div.row
        [:div.col-lg-3]
        [:div.col-lg-6
         [:div.well.bs-component
          [:form.form-horizontal
           [:fieldset
            [:legend "Prediction form"]
            [:div.form-group
             [:label
              {:for "double-feature-input"
               :class "col-lg-2 control-label"}
              "Double"]
             [:div.col-lg-10
              [:input
               {:class "form-control"
                :id "double-feature-input"
                :placeholder "Double feature"
                :type "number"
                :value (p/safe-get form :doubleFeature)
                :on-change #(handle-float % form :doubleFeature)}]]]
            [:div.form-group
             [:div.col-lg-10.col-lg-offset-2
              [:button
               {:type "reset"
                :class "btn btn-default"
                :on-click
                #(->>
                  (p/safe-get-in
                   cs/default
                   [:components :form :doubleFeature])
                  (om/update! form :doubleFeature))}
               "Reset"]
              [:button
               {:type "button"
                :class "btn btn-primary pull-right"
                :on-click #(api/post-prediction api-ch (om/value form))}
               "Submit"]]]]]]]
        [:div.col-lg-3]]))))
