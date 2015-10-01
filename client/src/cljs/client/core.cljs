(ns client.core
  (:require
   [om.core :as om :include-macros true]
   [cljs.core.async :refer [>! <! alts! chan close!]]
   [client.state :as state]
   [client.components.app :as app]
   [client.protobuf :as protobuf]
   [client.handlers.api :as api]
   [client.handlers.notifications :as notifications])
  (:require-macros
   [cljs.core.async.macros :as am :refer [alt! go]]))

(enable-console-print!)
(defonce api-ch (chan))
(defonce notify-ch (chan))

(defonce app-state
  (atom (assoc
         state/default
         :channels {:api-ch api-ch
                    :notify-ch notify-ch})))

(defn ^:export run []
  (go
    (protobuf/init))
  (go
    (while true
      (alt!
        api-ch ([msg] (api/handle msg app-state))
        notify-ch ([msg] (notifications/handle msg app-state)))))
  (om/root
   app/component
   app-state
   {:target (.getElementById js/document "app")}))
