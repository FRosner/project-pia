(ns client.handlers.api
  (:require
   [plumbing.core :as p]
   [cljs.core.async :refer [put!]]
   [clojure.string :as string]))

(defn extract-id [location]
  (last
   (string/split location #"/")))

(defmulti handle
  (fn [msg state]
    (p/safe-get msg :type)))

(defmethod handle :get-predictions
  [msg state]
  (let [success? (p/safe-get-in msg [:response :success])
        notify-ch (p/safe-get-in @state [:channels :notify-ch])
        predictions (p/safe-get @state :predictions)
        body-map (->> (p/safe-get-in msg [:response :body])
                      (reduce
                       (fn [acc k]
                         (assoc acc k nil)) {}))]
    (if success?
      (do
        (swap! state #(->>
                       (merge body-map predictions)
                       (assoc % :predictions)))
        (put! notify-ch {:msg "All predictions loaded successfully!"
                         :type "success"}))
      (put! notify-ch {:msg "Something went wrong while retrieving prediction ids. Please try again later."
                       :type "danger"}))))

(defmethod handle :get-prediction
  [msg state]
  (let [status (p/safe-get-in msg [:response :status])
        body (p/safe-get-in msg [:response :body])
        id (-> msg
               (p/safe-get-in [:response :trace-redirects])
               (first)
               (extract-id))
        notify-ch (p/safe-get-in @state [:channels :notify-ch])]
    (case status
      200 (do
            (swap! state assoc-in [:predictions id] body)
            (put! notify-ch {:msg (str
                                   "Prediction result for "
                                   id
                                   " loaded successfully!")
                             :type "success"}))
      404 (put! notify-ch {:msg "Prediction is not found on server!"
                           :type "danger"})
      500 (put! notify-ch {:msg "Prediction errored on server!"
                           :type "danger"})
      204 (put! notify-ch {:msg "Prediction is not yet ready."
                           :type "warning"}))))


(defmethod handle :post-prediction
  [msg state]
  (let [notify-ch (p/safe-get-in @state [:channels :notify-ch])
        success? (p/safe-get-in msg [:response :success])]
    (if success?
      (let [id (-> msg
                   (p/safe-get-in [:response :headers "location"])
                   (extract-id))]
        (swap! state assoc-in [:predictions id] nil)
        (put! notify-ch {:msg "Prediction was successfully submitted!"
                         :type "success"}))
      (put! notify-ch {:msg "Something went wrong while posting the prediction. Please try again later."
                       :type "danger"}))))
