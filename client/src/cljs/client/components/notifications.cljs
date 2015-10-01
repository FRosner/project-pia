(ns client.components.notifications
  (:require
   [om.core :as om :include-macros true]
   [sablono.core :refer-macros [html]]
   [plumbing.core :as p :include-macros true]))

(p/defnk component [[:components notification]]
  (reify
    om/IDisplayName
    (display-name [_] "Notifications")
    om/IRender
    (render [_]
      (html
       (if-not (nil? notification)
         [:div
          {:class (str
                   "alert alert-dismissible alert-"
                   (p/safe-get notification :type))}
          [:button
           {:type "button"
            :class "close"
            :data-dismiss "alert"} "×"]
          (p/safe-get notification :msg)]
         [:div])))))
